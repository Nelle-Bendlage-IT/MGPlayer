package com.mgtvapi

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform