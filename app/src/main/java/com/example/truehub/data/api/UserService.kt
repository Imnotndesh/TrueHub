package com.example.truehub.data.api

import com.example.truehub.data.ApiResult
import com.example.truehub.data.TrueNASClient
import com.example.truehub.data.models.User
import com.squareup.moshi.Types

class UserService(client: TrueNASClient): BaseApiService(client) {
    // Change Password
    suspend fun changeUserPassword(username : String, oldPass: String, newPass:String){
        return client.call(
            method = ApiMethods.User.CHANGE_PASSWORD,
            params = listOf(username,oldPass,newPass),
            resultType = Any::class.java
        )
    }
    suspend fun changeUserPasswordWithResult(username : String, oldPass: String, newPass:String): ApiResult<Any>{
        return try{
            val result = changeUserPassword(username,oldPass,newPass)
            ApiResult.Success(result)
        }catch (e: Exception){
            ApiResult.Error("Unable to change password: ${e.message}",e)
        }
    }

    // Update user
    suspend fun updateUser(userId: Int, update: User.UserUpdate): User.UserUpdateResponse {
        return client.call(
            method = ApiMethods.User.USER_UPDATE,
            params = listOf(userId, update),
            resultType = User.UserUpdateResponse::class.java
        )
    }
    suspend fun updateUserWithResult(userId: Int, update: User.UserUpdate): ApiResult<User.UserUpdateResponse>{
        return try{
            val result = updateUser(userId,update)
            ApiResult.Success(result)
        } catch (e: Exception){
            ApiResult.Error("Unable to update user: ${e.message}",e)
        }
    }

    // Get User info (different from auth.me)
    suspend fun getUserObj(request: User.GetUserObjRequest): User.UserObjResponse {
        return client.call(
            method = ApiMethods.User.GET_USER_OBJ,
            params = listOf(request),
            resultType = User.UserObjResponse::class.java
        )
    }
    suspend fun getUserObjWithResult(request: User.GetUserObjRequest): ApiResult<User.UserObjResponse>{
        return try{
            val result = getUserObj(request)
            ApiResult.Success(result)
        } catch (e: Exception){
            ApiResult.Error("Unable to get user object: ${e.message}",e)
        }
    }

    suspend fun getShellChoices(groupIds: List<Int> = emptyList()): List<User.ShellChoice> {
        val type = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            String::class.java
        )
        val map = client.call<Map<String, String>>(
            method = "user.shell_choices",
            params = listOf(groupIds),
            resultType = type
        )

        return map.map { (path, name) -> User.ShellChoice(path, name) }
    }
    suspend fun getShellChoicesWithResult(groupIds: List<Int> = emptyList()): ApiResult<List<User.ShellChoice>>{
        return try{
            val result = getShellChoices(groupIds)
            ApiResult.Success(result)
        }catch (e: Exception){
            ApiResult.Error("Unable to get shell choices: ${e.message}",e)
        }
    }

}