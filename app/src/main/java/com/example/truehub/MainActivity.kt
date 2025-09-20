package com.example.truehub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.truehub.data.Client
import com.example.truehub.data.api_methods.Auth
import com.example.truehub.data.helpers.Prefs
import com.example.truehub.ui.LoginScreen
import com.example.truehub.ui.SetupScreen

class MainActivity : ComponentActivity() {
    private var client: Client? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val (savedUrl, savedInsecure) = Prefs.load(this)

        setContent{
            MaterialTheme{
                if (savedUrl == null) {
                    // first run, show setup
                    SetupScreen { url, insecure ->
                        Prefs.save(this, url, insecure)
                        client = Client(url, insecure)
                        client?.connect()
                        setContent { MaterialTheme { LoginScreen(Auth(client!!)) } }
                    }
                } else {
                    // already configured, connect directly
                    client = Client(savedUrl, savedInsecure)
                    client?.connect()
                    LoginScreen(Auth(client!!))
                }
            }
        }
    }
    override fun onDestroy(){
        super.onDestroy()
        client?.disconnect()
    }
}