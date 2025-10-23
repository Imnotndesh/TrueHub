package com.example.truehub.data.api

import android.util.Log
import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.models.User
import com.squareup.moshi.Types

class UserService(var manager: TrueNASApiManager) {
    // Change Password
    suspend fun changeUserPasswordWithResult(username : String, oldPass: String, newPass:String): ApiResult<Any>{
        val passChangeRequest = User.ChangeUserPasswordRequest(username,oldPass,newPass)
        return manager.callWithResult(
            method = ApiMethods.User.CHANGE_PASSWORD,
            params = listOf(passChangeRequest),
            resultType = Any::class.java
        )
    }

    // Update user
    suspend fun updateUserWithResult(userId: Int, update: User.UserUpdate): ApiResult<User.UserUpdateResponse>{
        return manager.callWithResult(
            method = ApiMethods.User.USER_UPDATE,
            params = listOf(userId, update),
            resultType = User.UserUpdateResponse::class.java
        )
    }

    // Get User info (different from auth.me)
    suspend fun getUserObjWithResult(request: User.GetUserObjRequest): ApiResult<User.UserObjResponse>{
        return manager.callWithResult(
            method = ApiMethods.User.GET_USER_OBJ,
            params = listOf(request),
            resultType = User.UserObjResponse::class.java
        )
    }

}