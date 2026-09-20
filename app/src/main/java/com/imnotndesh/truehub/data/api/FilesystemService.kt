package com.imnotndesh.truehub.data.api

import com.imnotndesh.truehub.data.ApiResult
import com.imnotndesh.truehub.data.models.Filesystem
import com.squareup.moshi.Types

class FilesystemService(private val manager: TrueNASApiManager) {

    suspend fun acltemplateWithResult(): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Filesystem.FILESYSTEM_ACLTEMPLATE, listOf(), Any::class.java)

    suspend fun acltemplateByPathWithResult(filesystemAcl: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Filesystem.FILESYSTEM_ACLTEMPLATE_BY_PATH, listOf(filesystemAcl), Any::class.java)

    suspend fun acltemplateCreateWithResult(acltemplateCreate: Any?): ApiResult<Filesystem.AclTemplateEntry> =
        manager.callWithResult(ApiMethods.Filesystem.FILESYSTEM_ACLTEMPLATE_CREATE, listOf(acltemplateCreate), Filesystem.AclTemplateEntry::class.java)

    suspend fun acltemplateDeleteWithResult(id: Any?): ApiResult<Unit> =
        manager.callWithResult(ApiMethods.Filesystem.FILESYSTEM_ACLTEMPLATE_DELETE, listOf(id), Unit::class.java)

    suspend fun acltemplateGetInstanceWithResult(id: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Filesystem.AclTemplateEntry> =
        manager.callWithResult(ApiMethods.Filesystem.FILESYSTEM_ACLTEMPLATE_GET_INSTANCE, listOf(id, options), Filesystem.AclTemplateEntry::class.java)

    suspend fun acltemplateQueryWithResult(filters: List<Any> = emptyList(), options: Map<String, Any?> = emptyMap()): ApiResult<List<Filesystem.AclTemplateEntry>> =
        manager.callWithResult(ApiMethods.Filesystem.FILESYSTEM_ACLTEMPLATE_QUERY, listOf(filters, options), Types.newParameterizedType(List::class.java, Filesystem.AclTemplateEntry::class.java))

    suspend fun acltemplateUpdateWithResult(id: Any?, acltemplateUpdate: Any?): ApiResult<Filesystem.AclTemplateEntry> =
        manager.callWithResult(ApiMethods.Filesystem.FILESYSTEM_ACLTEMPLATE_UPDATE, listOf(id, acltemplateUpdate), Filesystem.AclTemplateEntry::class.java)

    suspend fun chownWithResult(filesystemChown: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Filesystem.FILESYSTEM_CHOWN, listOf(filesystemChown), Any::class.java)

    suspend fun getWithResult(path: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Filesystem.FILESYSTEM_GET, listOf(path), Any::class.java)

    suspend fun getZfsAttributesWithResult(path: Any?): ApiResult<Filesystem.ZfsFileAttrs> =
        manager.callWithResult(ApiMethods.Filesystem.FILESYSTEM_GET_ZFS_ATTRIBUTES, listOf(path), Filesystem.ZfsFileAttrs::class.java)

    suspend fun getaclWithResult(path: Any?, simplified: Any?, resolveIds: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Filesystem.FILESYSTEM_GETACL, listOf(path, simplified, resolveIds), Any::class.java)

    suspend fun listdirWithResult(path: Any?, queryFilters: Any?, queryOptions: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Filesystem.FILESYSTEM_LISTDIR, listOf(path, queryFilters, queryOptions), Any::class.java)

    suspend fun putWithResult(path: Any?, options: Map<String, Any?> = emptyMap()): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Filesystem.FILESYSTEM_PUT, listOf(path, options), Any::class.java)

    suspend fun setZfsAttributesWithResult(setZfsFileAttributes: Any?): ApiResult<Filesystem.ZfsFileAttrs> =
        manager.callWithResult(ApiMethods.Filesystem.FILESYSTEM_SET_ZFS_ATTRIBUTES, listOf(setZfsFileAttributes), Filesystem.ZfsFileAttrs::class.java)

    suspend fun setaclWithResult(filesystemAcl: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Filesystem.FILESYSTEM_SETACL, listOf(filesystemAcl), Any::class.java)

    suspend fun setpermWithResult(filesystemSetperm: Any?): ApiResult<Any?> =
        manager.callWithResult(ApiMethods.Filesystem.FILESYSTEM_SETPERM, listOf(filesystemSetperm), Any::class.java)
}
