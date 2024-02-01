package org.satochip.satodimeapp.data

enum class SlotState {
    UNINITIALIZED, SEALED, UNSEALED;

    companion object {
        fun byteAsSlotState(byte: Byte) : SlotState {
            return when(byte.toInt()) {
                0 -> UNINITIALIZED
                1 -> SEALED
                2 -> UNSEALED
                else -> {
                    UNINITIALIZED
                }
            }
        }
    }
}