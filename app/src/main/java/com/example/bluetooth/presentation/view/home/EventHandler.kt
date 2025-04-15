package com.example.bluetooth.presentation.view.home

interface EventHandler {
    fun handleEvent(event: HomeEvent): ByteArray
}