package com.example.transfer.protocol.domain


class SessionManagerState {
    enum class State(internal val flag: Int) {
        STARTED(1),        // 0b0001
        STOPPED(1 shl 1),  // 0b0010
        ERROR(1 shl 2),    // 0b0100
    }

    data class States internal constructor(internal val flags: Int) {
        constructor(vararg states: State) : this(
            states.map { it.flag }.reduce { acc, flag -> acc or flag }
        )

        fun hasState(state: State): Boolean = (flags and state.flag) == state.flag
    }
}