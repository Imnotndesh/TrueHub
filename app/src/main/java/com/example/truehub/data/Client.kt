package com.example.truehub.data

// Unsafe client import
import android.util.Log
import com.example.truehub.helpers.models.Request
import com.example.truehub.helpers.models.Response
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CompletableDeferred
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import okhttp3.Request as wsRequest

class Client (private val serverUrl: String, insecure : Boolean = false){
    private val client : OkHttpClient=if (insecure){
        createUnsafeClient()
    }else{
        OkHttpClient()
    }
    private var webSocket: WebSocket? = null
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val responseAdapter = moshi.adapter<Response<Any>>(
        Types.newParameterizedType(Response::class.java, Any::class.java)
    )
    private val requestAdapter = moshi.adapter(Request::class.java)

    private val pendingRequests = ConcurrentHashMap<Int, CompletableDeferred<Response<Any>>>()
    private val idCounter = AtomicInteger(1)
    private val logName = "Ws-Client"

    fun connect(){
        val request = wsRequest.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request,object : WebSocketListener(){
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                super.onOpen(webSocket, response)

                // DEBUG: Will remove
                Log.e(logName, "Connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                val resp = responseAdapter.fromJson(text)
                if (resp != null){
                    val deferred = pendingRequests[resp.id]
                    if (deferred != null){
                        @Suppress("UNCHECKED_CAST")
                        deferred.complete(resp)
                        pendingRequests.remove(resp.id)
                    }
                }
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: okhttp3.Response?
            ) {
                super.onFailure(webSocket, t, response)

                // DEBUG: Will remove
                Log.e(logName, "Error: ${t.message}")

                pendingRequests.values.forEach {
                    it.completeExceptionally(t)
                }
                pendingRequests.clear()
            }
        })
    }

    fun createUnsafeClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        val sslSocketFactory = sslContext.socketFactory

        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    suspend fun <T> call(method: String, params: List<Any?>, resultClass: Class<T>):T{
        val id = idCounter.getAndAdd(1)
        val request = Request(id=id,method=method,params=params)
        val deferred = CompletableDeferred<Response<Any>>()
        pendingRequests[id] = deferred

        val json = requestAdapter.toJson(request)
        Log.e(logName, "Sending: $json")
        webSocket?.send(json)

        val response = deferred.await()
        response.error?.let {
            throw RuntimeException("RPC Error: ${it.error}")
        }
        // DEBUG: Will remove
        Log.e(logName, "Received: $response")

        val adapter = moshi.adapter(resultClass)
        return adapter.fromJsonValue(response.result!!)!!
    }

    fun disconnect(){
        webSocket?.close(1000,"Client disconnected")
        webSocket = null
    }

}