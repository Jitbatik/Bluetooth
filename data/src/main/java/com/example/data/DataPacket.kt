package com.example.data

data class DataPacket(
    val state: DataPacketState,
    val data: ByteArray = byteArrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataPacket) return false

        if (state != other.state) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = state.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

