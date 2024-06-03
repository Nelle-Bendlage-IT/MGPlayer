package com.mgtvapi.api.model

abstract class ListableClip<T> {
    abstract val magazineName: String
    abstract fun getSelf(): T
}