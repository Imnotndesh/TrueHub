package com.imnotndesh.truehub.data.models

/** One provider-specific credential input. */
data class CloudSyncField(
    val key: String,
    val label: String,
    val type: CloudSyncFieldType,
    val required: Boolean = false,
    val defaultValue: String? = null,
    val options: List<String> = emptyList(),
    val secret: Boolean = false,
)

enum class CloudSyncFieldType { TEXT, NUMBER, BOOLEAN, ENUM }

data class CloudSyncProviderSpec(
    val type: String,
    val title: String,
    val oauth: Boolean,
    val fields: List<CloudSyncField>,
)

/**
 * Provider registry for the Cloud Sync credential wizard, generated from the TrueNAS
 * `cloudsync.credentials.create` schema. Kept as static data because the server does not
 * expose the credential schema through `cloudsync.providers`.
 */
object CloudSyncProviders {
    val all: List<CloudSyncProviderSpec> = listOf(
        CloudSyncProviderSpec(
            type = "AZUREBLOB",
            title = "Microsoft Azure Blob",
            oauth = false,
            fields = listOf(
                CloudSyncField(
                    key = "account",
                    label = "Account",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "key",
                    label = "Key",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "endpoint",
                    label = "Endpoint",
                    type = CloudSyncFieldType.ENUM,
                    defaultValue = "\"\"",
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "B2",
            title = "Backblaze B2",
            oauth = false,
            fields = listOf(
                CloudSyncField(
                    key = "account",
                    label = "Account",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "key",
                    label = "Key",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "BOX",
            title = "Box",
            oauth = true,
            fields = listOf(
                CloudSyncField(
                    key = "client_id",
                    label = "Client Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "client_secret",
                    label = "Client Secret",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "token",
                    label = "Token",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "DROPBOX",
            title = "Dropbox",
            oauth = true,
            fields = listOf(
                CloudSyncField(
                    key = "client_id",
                    label = "Client Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "client_secret",
                    label = "Client Secret",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "token",
                    label = "Token",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "FTP",
            title = "FTP",
            oauth = false,
            fields = listOf(
                CloudSyncField(
                    key = "host",
                    label = "Host",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "port",
                    label = "Port",
                    type = CloudSyncFieldType.NUMBER,
                    defaultValue = "21",
                ),
                CloudSyncField(
                    key = "user",
                    label = "User",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "pass",
                    label = "Pass",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "GOOGLE_CLOUD_STORAGE",
            title = "Google Cloud Storage",
            oauth = false,
            fields = listOf(
                CloudSyncField(
                    key = "service_account_credentials",
                    label = "Service Account Credentials",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "GOOGLE_DRIVE",
            title = "Google Drive",
            oauth = true,
            fields = listOf(
                CloudSyncField(
                    key = "client_id",
                    label = "Client Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "client_secret",
                    label = "Client Secret",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "token",
                    label = "Token",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "team_drive",
                    label = "Team Drive",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "GOOGLE_PHOTOS",
            title = "Google Photos",
            oauth = true,
            fields = listOf(
                CloudSyncField(
                    key = "client_id",
                    label = "Client Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "client_secret",
                    label = "Client Secret",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "token",
                    label = "Token",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "HTTP",
            title = "HTTP",
            oauth = false,
            fields = listOf(
                CloudSyncField(
                    key = "url",
                    label = "Url",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "HUBIC",
            title = "Hubic",
            oauth = true,
            fields = listOf(
                CloudSyncField(
                    key = "token",
                    label = "Token",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "MEGA",
            title = "MEGA",
            oauth = false,
            fields = listOf(
                CloudSyncField(
                    key = "user",
                    label = "User",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "pass",
                    label = "Pass",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "ONEDRIVE",
            title = "OneDrive",
            oauth = true,
            fields = listOf(
                CloudSyncField(
                    key = "client_id",
                    label = "Client Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "client_secret",
                    label = "Client Secret",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "token",
                    label = "Token",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "drive_type",
                    label = "Drive Type",
                    type = CloudSyncFieldType.ENUM,
                    required = true,
                    options = listOf("PERSONAL", "BUSINESS", "DOCUMENT_LIBRARY"),
                ),
                CloudSyncField(
                    key = "drive_id",
                    label = "Drive Id",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "PCLOUD",
            title = "pCloud",
            oauth = true,
            fields = listOf(
                CloudSyncField(
                    key = "client_id",
                    label = "Client Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "client_secret",
                    label = "Client Secret",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "token",
                    label = "Token",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "hostname",
                    label = "Hostname",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "S3",
            title = "Amazon S3",
            oauth = false,
            fields = listOf(
                CloudSyncField(
                    key = "access_key_id",
                    label = "Access Key Id",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "secret_access_key",
                    label = "Secret Access Key",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "endpoint",
                    label = "Endpoint",
                    type = CloudSyncFieldType.ENUM,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "region",
                    label = "Region",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "skip_region",
                    label = "Skip Region",
                    type = CloudSyncFieldType.BOOLEAN,
                    defaultValue = "false",
                ),
                CloudSyncField(
                    key = "signatures_v2",
                    label = "Signatures V2",
                    type = CloudSyncFieldType.BOOLEAN,
                    defaultValue = "false",
                ),
                CloudSyncField(
                    key = "max_upload_parts",
                    label = "Max Upload Parts",
                    type = CloudSyncFieldType.NUMBER,
                    defaultValue = "10000",
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "SFTP",
            title = "SFTP",
            oauth = false,
            fields = listOf(
                CloudSyncField(
                    key = "host",
                    label = "Host",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "port",
                    label = "Port",
                    type = CloudSyncFieldType.NUMBER,
                    defaultValue = "22",
                ),
                CloudSyncField(
                    key = "user",
                    label = "User",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "pass",
                    label = "Pass",
                    type = CloudSyncFieldType.TEXT,
                    secret = true,
                ),
                CloudSyncField(
                    key = "private_key",
                    label = "Private Key",
                    type = CloudSyncFieldType.NUMBER,
                    secret = true,
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "STORJ_IX",
            title = "Storj iX",
            oauth = false,
            fields = listOf(
                CloudSyncField(
                    key = "access_key_id",
                    label = "Access Key Id",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "secret_access_key",
                    label = "Secret Access Key",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "endpoint",
                    label = "Endpoint",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"https://gateway.storjshare.io/\"",
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "OPENSTACK_SWIFT",
            title = "OpenStack Swift",
            oauth = false,
            fields = listOf(
                CloudSyncField(
                    key = "user",
                    label = "User",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "key",
                    label = "Key",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "auth",
                    label = "Auth",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "user_id",
                    label = "User Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "domain",
                    label = "Domain",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "tenant",
                    label = "Tenant",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "tenant_id",
                    label = "Tenant Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "tenant_domain",
                    label = "Tenant Domain",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "region",
                    label = "Region",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "storage_url",
                    label = "Storage Url",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "auth_token",
                    label = "Auth Token",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "application_credential_id",
                    label = "Application Credential Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "application_credential_name",
                    label = "Application Credential Name",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "application_credential_secret",
                    label = "Application Credential Secret",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "WEBDAV",
            title = "WebDAV",
            oauth = false,
            fields = listOf(
                CloudSyncField(
                    key = "url",
                    label = "Url",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "vendor",
                    label = "Vendor",
                    type = CloudSyncFieldType.ENUM,
                    required = true,
                    options = listOf("NEXTCLOUD", "OWNCLOUD", "SHAREPOINT", "OTHER"),
                ),
                CloudSyncField(
                    key = "user",
                    label = "User",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "pass",
                    label = "Pass",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
            )
        ),
        CloudSyncProviderSpec(
            type = "YANDEX",
            title = "Yandex Disk",
            oauth = true,
            fields = listOf(
                CloudSyncField(
                    key = "client_id",
                    label = "Client Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "client_secret",
                    label = "Client Secret",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "token",
                    label = "Token",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "id",
                    label = "Id",
                    type = CloudSyncFieldType.NUMBER,
                    required = true,
                ),
                CloudSyncField(
                    key = "name",
                    label = "Name",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "account",
                    label = "Account",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "key",
                    label = "Key",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "endpoint",
                    label = "Endpoint",
                    type = CloudSyncFieldType.ENUM,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "account",
                    label = "Account",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "key",
                    label = "Key",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "client_id",
                    label = "Client Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "client_secret",
                    label = "Client Secret",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "token",
                    label = "Token",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "client_id",
                    label = "Client Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "client_secret",
                    label = "Client Secret",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "token",
                    label = "Token",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "host",
                    label = "Host",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "port",
                    label = "Port",
                    type = CloudSyncFieldType.NUMBER,
                    defaultValue = "21",
                ),
                CloudSyncField(
                    key = "user",
                    label = "User",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "pass",
                    label = "Pass",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "service_account_credentials",
                    label = "Service Account Credentials",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "client_id",
                    label = "Client Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "client_secret",
                    label = "Client Secret",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "token",
                    label = "Token",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "team_drive",
                    label = "Team Drive",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "client_id",
                    label = "Client Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "client_secret",
                    label = "Client Secret",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "token",
                    label = "Token",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "url",
                    label = "Url",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "token",
                    label = "Token",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "user",
                    label = "User",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "pass",
                    label = "Pass",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "client_id",
                    label = "Client Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "client_secret",
                    label = "Client Secret",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "token",
                    label = "Token",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "drive_type",
                    label = "Drive Type",
                    type = CloudSyncFieldType.ENUM,
                    required = true,
                    options = listOf("PERSONAL", "BUSINESS", "DOCUMENT_LIBRARY"),
                ),
                CloudSyncField(
                    key = "drive_id",
                    label = "Drive Id",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "client_id",
                    label = "Client Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "client_secret",
                    label = "Client Secret",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "token",
                    label = "Token",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "hostname",
                    label = "Hostname",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "access_key_id",
                    label = "Access Key Id",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "secret_access_key",
                    label = "Secret Access Key",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "endpoint",
                    label = "Endpoint",
                    type = CloudSyncFieldType.ENUM,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "region",
                    label = "Region",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "skip_region",
                    label = "Skip Region",
                    type = CloudSyncFieldType.BOOLEAN,
                    defaultValue = "false",
                ),
                CloudSyncField(
                    key = "signatures_v2",
                    label = "Signatures V2",
                    type = CloudSyncFieldType.BOOLEAN,
                    defaultValue = "false",
                ),
                CloudSyncField(
                    key = "max_upload_parts",
                    label = "Max Upload Parts",
                    type = CloudSyncFieldType.NUMBER,
                    defaultValue = "10000",
                ),
                CloudSyncField(
                    key = "host",
                    label = "Host",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "port",
                    label = "Port",
                    type = CloudSyncFieldType.NUMBER,
                    defaultValue = "22",
                ),
                CloudSyncField(
                    key = "user",
                    label = "User",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "pass",
                    label = "Pass",
                    type = CloudSyncFieldType.TEXT,
                    secret = true,
                ),
                CloudSyncField(
                    key = "private_key",
                    label = "Private Key",
                    type = CloudSyncFieldType.NUMBER,
                    secret = true,
                ),
                CloudSyncField(
                    key = "access_key_id",
                    label = "Access Key Id",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "secret_access_key",
                    label = "Secret Access Key",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "endpoint",
                    label = "Endpoint",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"https://gateway.storjshare.io/\"",
                ),
                CloudSyncField(
                    key = "user",
                    label = "User",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "key",
                    label = "Key",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "auth",
                    label = "Auth",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "user_id",
                    label = "User Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "domain",
                    label = "Domain",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "tenant",
                    label = "Tenant",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "tenant_id",
                    label = "Tenant Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "tenant_domain",
                    label = "Tenant Domain",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "region",
                    label = "Region",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "storage_url",
                    label = "Storage Url",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "auth_token",
                    label = "Auth Token",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "application_credential_id",
                    label = "Application Credential Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "application_credential_name",
                    label = "Application Credential Name",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "application_credential_secret",
                    label = "Application Credential Secret",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "url",
                    label = "Url",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "vendor",
                    label = "Vendor",
                    type = CloudSyncFieldType.ENUM,
                    required = true,
                    options = listOf("NEXTCLOUD", "OWNCLOUD", "SHAREPOINT", "OTHER"),
                ),
                CloudSyncField(
                    key = "user",
                    label = "User",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                ),
                CloudSyncField(
                    key = "pass",
                    label = "Pass",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
                CloudSyncField(
                    key = "client_id",
                    label = "Client Id",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                ),
                CloudSyncField(
                    key = "client_secret",
                    label = "Client Secret",
                    type = CloudSyncFieldType.TEXT,
                    defaultValue = "\"\"",
                    secret = true,
                ),
                CloudSyncField(
                    key = "token",
                    label = "Token",
                    type = CloudSyncFieldType.TEXT,
                    required = true,
                    secret = true,
                ),
            )
        ),
    )

    fun byType(type: String?): CloudSyncProviderSpec? = all.find { it.type == type }
}
