package com.psis.elimlift

interface EventHandler<E : Event, R> {
    suspend fun handle(event: E): R
}
