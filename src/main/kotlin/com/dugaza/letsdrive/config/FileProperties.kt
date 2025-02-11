package com.dugaza.letsdrive.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
@ConfigurationProperties(prefix = "file")
class FileProperties {
    var maxSize: Long = 10 * 1024 * 1024L
    var imageExtensions: String = "jpg,jpeg,png,gif,bmp"
    var uncompressedExtensions: String = "bmp"
    var allowedExtensions: String = "mp4"
    var uploadRoot: String = "uploads"
    var defaultImageDetailId: UUID = UUID.fromString("2a20d335-09fd-4ec3-900a-938bcb5c267b")

    fun imageExtensionSet(): Set<String> = imageExtensions.split(",").map { it.trim().lowercase() }.toSet()

    fun uncompressedExtensionSet(): Set<String> = uncompressedExtensions.split(",").map { it.trim().lowercase() }.toSet()

    fun allowedExtensionSet(): Set<String> = allowedExtensions.split(",").map { it.trim().lowercase() }.toSet()
}
