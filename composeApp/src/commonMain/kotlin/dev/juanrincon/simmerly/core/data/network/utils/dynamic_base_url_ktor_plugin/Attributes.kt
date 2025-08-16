package dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin

import io.ktor.http.Url
import io.ktor.util.AttributeKey

// Prefer typed Url override
val BaseUrlOverrideUrl = AttributeKey<Url>("BaseUrlOverrideUrl")
// Keep string override for convenience (parsed safely)
val BaseUrlOverride = AttributeKey<String>("BaseUrlOverride")
// Per-call opt-out
val SkipDynamicBaseUrl = AttributeKey<Boolean>("SkipDynamicBaseUrl")
