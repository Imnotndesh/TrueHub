package com.imnotndesh.truehub.data

import com.imnotndesh.truehub.data.helpers.TrueHubLogger
import com.imnotndesh.truehub.data.models.Config.ClientConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.lang.reflect.Type
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import com.imnotndesh.truehub.data.models.RpcRequest as Request
import com.imnotndesh.truehub.data.models.RpcResponse as Response
import okhttp3.Request as wsRequest

class TrueNASRpcException(val code: Int, message: String) : RuntimeException(message)

class TrueNASClient(private val config: ClientConfig) {
    private val client: OkHttpClient = if (config.insecure) {
        createUnsafeClient()
    } else {
        OkHttpClient.Builder()
            .connectTimeout(config.connectionTimeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()
    }
    init {
        TrueHubLogger.isLoggingEnabled = config.enableDebugLogging
    }
    private val logName = "TrueNAS-Client"

    private var webSocket: WebSocket? = null
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val responseAdapter = moshi.adapter<Response<Any>>(
        Types.newParameterizedType(Response::class.java, Any::class.java)
    )
    private val requestAdapter = moshi.adapter(Request::class.java)

    private val pendingRequests = ConcurrentHashMap<Int, CompletableDeferred<Response<Any>>>()
    private val idCounter = AtomicInteger(1)
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun connect(): Boolean {
        if (_connectionState.value is ConnectionState.Connected) {
            return true
        }

        _connectionState.value = ConnectionState.Connecting

        return try {
            val request = wsRequest.Builder().url(config.serverUrl).build()
            val connectionDeferred = CompletableDeferred<Boolean>()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                    super.onOpen(webSocket, response)
                    _connectionState.value = ConnectionState.Connected
                    TrueHubLogger.e(logName,"Connected to ${config.serverUrl}")
                    connectionDeferred.complete(true)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    TrueHubLogger.e(logName,"Received message: $text")
                    handleMessage(text)
                }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                    super.onFailure(webSocket, t, response)
                    val errorMsg = "Connection failed: ${t.message}"
                    _connectionState.value = ConnectionState.Error(errorMsg, t)
                    TrueHubLogger.e(logName,errorMsg, t)

                    pendingRequests.values.forEach { it.completeExceptionally(t) }
                    pendingRequests.clear()

                    if (!connectionDeferred.isCompleted) {
                        connectionDeferred.complete(false)
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosed(webSocket, code, reason)
                    _connectionState.value = ConnectionState.Disconnected
                    TrueHubLogger.e(logName,"Connection closed: $code - $reason")
                }
            })

            connectionDeferred.await()
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error("Failed to connect", e)
            TrueHubLogger.e(logName,"Connection error", e)
            false
        }
    }

    private fun handleMessage(text: String) {
        try {
            val resp = responseAdapter.fromJson(text)

            when {
                resp == null -> TrueHubLogger.e(logName,"Null response from server")

                resp.id == null && resp.method != null -> {
                    TrueHubLogger.e(logName,"Notification: ${resp.method} ${resp.params}")
                }
                resp.error != null -> {
                    val deferred = pendingRequests[resp.id]
                    if (deferred != null) {
                        deferred.complete(resp)
                        pendingRequests.remove(resp.id)
                    }
                }
                else -> {
                    val deferred = pendingRequests[resp.id]
                    if (deferred != null) {
                        deferred.complete(resp)
                        pendingRequests.remove(resp.id)
                    } else {
                        TrueHubLogger.e(logName,"Received response for unknown request ID: ${resp.id}")
                    }
                }
            }
        } catch (e: Exception) {
            TrueHubLogger.e(logName,"Error parsing message", e)
        }
    }


    suspend fun ping(): Boolean {
        if (!config.enablePing) return true
        return try {
            val response = call<String>(
                method = "core.ping",
                params = listOf(),
                resultType = String::class.java
            )
            response == "pong"
        } catch (e: Exception) {
            TrueHubLogger.e(logName,"Ping failed", e)
            false
        }
    }
    suspend fun <T> call(method: String, params: List<Any?>, resultType: Type): T {
        if (method.startsWith("auth.login") || method == "auth.generate_token") {
            return performRpcCall(method, params, resultType)
        }

        if (_connectionState.value !is ConnectionState.Connected) {
            if (!connect()) {
                throw RuntimeException("Cannot connect to server")
            }
        }

        return performRpcCall(method, params, resultType)
    }

    private suspend fun <T> performRpcCall(method: String, params: List<Any?>, resultType: Type): T {
        val id = idCounter.getAndIncrement()
        val request = Request(id = id, method = method, params = params)
        val deferred = CompletableDeferred<Response<Any>>()
        pendingRequests[id] = deferred

        return try {
            val json = requestAdapter.toJson(request)
            TrueHubLogger.e(logName,"Sending: $json")

            webSocket?.send(json) ?: throw RuntimeException("WebSocket not connected")

            val response = deferred.await()

            // FIX: Robust Error Parsing
            if (response.error != null) {
                var finalCode = response.error.code
                var finalMessage = response.error.message

                // Extract nested error code (e.g., 207 from -32001)
                if (response.error.data is Map<*, *>) {
                    val dataMap = response.error.data as Map<*, *>

                    if (dataMap.containsKey("error")) {
                        val innerCode = dataMap["error"]
                        if (innerCode is Number) {
                            finalCode = innerCode.toInt()
                        }
                    }
                    if (dataMap.containsKey("reason")) {
                        finalMessage = dataMap["reason"] as? String ?: finalMessage
                    }
                }
                throw TrueNASRpcException(finalCode, finalMessage)
            }
            if (response.result == null) {
                if (resultType == Unit::class.java || resultType == Void::class.java) {
                    @Suppress("UNCHECKED_CAST")
                    return Unit as T
                }
                throw RuntimeException("Server returned null result")
            }

            val adapter = moshi.adapter<T>(resultType)
            adapter.fromJsonValue(response.result)
                ?: throw RuntimeException("Failed to deserialize response")
        } finally {
            pendingRequests.remove(id)
        }
    }

    suspend fun <T> callWithResult(method: String, params: List<Any?>, resultType: Type): ApiResult<T> {
        return try {
            _isLoading.value = true
            val result = call<T>(method, params, resultType)
            ApiResult.Success(result)
        } catch (e: Exception) {
            TrueHubLogger.e(logName,"API call failed: $method -> $e")
            ApiResult.Error(e.message ?: "Unknown error", e)
        } finally {
            _isLoading.value = false
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnected")
        webSocket = null
        _connectionState.value = ConnectionState.Disconnected
        pendingRequests.clear()
        TrueHubLogger.e(logName,"Disconnected")
    }

    private fun createUnsafeClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(config.connectionTimeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()
    }

    fun getCurrentConnectionState(): ConnectionState = _connectionState.value
}