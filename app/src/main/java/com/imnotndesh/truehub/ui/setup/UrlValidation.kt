package com.imnotndesh.truehub.ui.setup

import java.util.regex.Pattern

data class UrlValidation(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>
)

fun validateUrl(url: String): UrlValidation {
    if (url.isEmpty()) {
        return UrlValidation(false, emptyList(), emptyList())
    }

    val errors = mutableListOf<String>()
    val warnings = mutableListOf<String>()

    // Check protocol
    when {
        url.startsWith("ws://") -> {
            warnings.add("Using insecure WebSocket (ws://). Consider using wss:// for production.")
        }
        url.startsWith("wss://") -> {
            // Good, secure connection
        }
        url.startsWith("http://") || url.startsWith("https://") -> {
            errors.add("Use WebSocket protocol (ws:// or wss://) instead of HTTP.")
        }
        else -> {
            errors.add("URL must start with ws:// or wss://")
        }
    }

    // Extract the part after protocol
    val urlWithoutProtocol = url.substringAfter("://")

    if (urlWithoutProtocol.isEmpty()) {
        errors.add("URL is incomplete after protocol")
        return UrlValidation(false, errors, warnings)
    }

    // Split into host and path parts
    val parts = urlWithoutProtocol.split("/", limit = 2)
    val hostPart = parts[0]

    // Validate host part (IP or domain)
    if (hostPart.isEmpty()) {
        errors.add("Host/IP address is missing")
    } else {
        // Check if it looks like an IP address
        val ipPattern = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})(:\\d+)?$")
        val domainPattern = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]*\\..*$")
        val localhostPattern = Pattern.compile("^localhost(:\\d+)?$", Pattern.CASE_INSENSITIVE)

        when {
            ipPattern.matcher(hostPart).matches() -> {
                // Validate IP octets
                val ipParts = hostPart.split(":")[0].split(".")
                ipParts.forEach { octet ->
                    val num = octet.toIntOrNull()
                    if (num == null || num > 255) {
                        errors.add("Invalid IP address: $hostPart")
                    }
                }
            }
            localhostPattern.matcher(hostPart).matches() -> {
                // localhost is valid
            }
            domainPattern.matcher(hostPart).matches() || hostPart.contains(".") -> {
                // Looks like a domain name
                if (hostPart.count { it == '.' } < 1 && !hostPart.contains("localhost")) {
                    warnings.add("Domain name might be incomplete")
                }
            }
            hostPart.matches(Regex("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) -> {
                errors.add("Incomplete IP address (missing last octet)")
            }
            hostPart.matches(Regex("^\\d{1,3}\\.\\d{1,3}$")) -> {
                errors.add("Incomplete IP address (missing two octets)")
            }
            hostPart.matches(Regex("^\\d{1,3}$")) -> {
                errors.add("Incomplete IP address (only one octet provided)")
            }
            else -> {
                errors.add("Invalid host format. Use IP address, domain name, or localhost")
            }
        }
    }

    // Check if API path is present
    val pathPart = if (parts.size > 1) "/${parts[1]}" else ""
    if (!pathPart.contains("/api/current")) {
        warnings.add("URL will be automatically formatted to include /api/current")
    }

    return UrlValidation(
        isValid = errors.isEmpty(),
        errors = errors,
        warnings = warnings
    )
}