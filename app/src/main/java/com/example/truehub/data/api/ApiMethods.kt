package com.example.truehub.data.api

object ApiMethods {
    object Auth {
        const val AUTH_LOGIN = "auth.login"
        const val AUTH_API_LOGIN = "auth.login_with_api_key"
        const val AUTH_TOKEN_LOGIN = "auth.login_with_token"
        const val AUTH_LOGOUT = "auth.logout"
        const val AUTH_ME = "auth.me"
        const val GEN_AUTH_TOKEN = "auth.generate_token"

    }
    object Connection{
        const val CONNECTION_KEEP_ALIVE = "core.ping"
    }
    object System {
        const val SYSTEM_INFO = "system.info"
    }
    object Apps {
        const val QUERY_APPS = "app.query"
    }
}