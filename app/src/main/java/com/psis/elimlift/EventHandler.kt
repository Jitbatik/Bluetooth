package com.psis.elimlift

interface EventHandler<E : Event, R> {
    fun handle(event: E): R
}
