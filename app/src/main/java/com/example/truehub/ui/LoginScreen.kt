package com.example.truehub.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.truehub.data.api_methods.Auth
import com.example.truehub.helpers.models.LoginMode
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(auth: Auth) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var loginMode by remember { mutableStateOf(LoginMode.PASSWORD) }

    // TODO: Add the api_key section
    val context = LocalContext.current

    // coroutine scope tied to Compose
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter your login credentials",
            modifier = Modifier.padding(4.dp),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (loginMode == LoginMode.PASSWORD){
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
        }else{
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Login Process
        Button(
            onClick = {
                scope.launch {
                    // Username and password flow
                    try {
                        if (loginMode == LoginMode.PASSWORD){
                            val success = auth.loginUser(
                                Auth.DefaultAuth(username, password)
                            )
                            Toast.makeText(
                                context,
                                if (success) "Login successful" else "Login failed",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else{
                          val success = auth.loginWithApiKey(apiKey)
                          Toast.makeText(
                              context,
                              if (success) "Login successful" else "Login failed",
                              Toast.LENGTH_SHORT
                          ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        // Selector for login
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ){
            Button(
                onClick = {
                    loginMode = LoginMode.PASSWORD
                }
            ){
                Text("Login with password")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    loginMode = LoginMode.API_KEY
                }
            ) {
                Text("Login with API key")
            }
            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}

