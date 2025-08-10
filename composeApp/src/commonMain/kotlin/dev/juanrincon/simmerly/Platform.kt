package dev.juanrincon.simmerly

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform