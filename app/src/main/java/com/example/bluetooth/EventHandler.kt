package com.example.bluetooth

interface EventHandler<E : Event, R> {
    fun handle(event: E): R
}
