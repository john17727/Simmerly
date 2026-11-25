package dev.juanrincon.simmerly.di

import dev.juanrincon.simmerly.timeline.domain.PickedImage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.posix.memcpy

/** Builds a [PickedImage] from photo bytes chosen on iOS, since Swift can't cheaply fill a Kotlin ByteArray. */
object IosPickedImage {
    @OptIn(ExperimentalForeignApi::class)
    fun from(data: NSData, extension: String): PickedImage {
        val size = data.length.toInt()
        val bytes = ByteArray(size)
        if (size > 0) {
            bytes.usePinned { memcpy(it.addressOf(0), data.bytes, size.convert()) }
        }
        return PickedImage(bytes, extension)
    }
}
