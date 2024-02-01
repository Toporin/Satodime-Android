package org.satochip.satodimeapp.data

enum class NfcActionType {
    ScanCard,
    TakeOwnership,
    ReleaseOwnership,
    SealSlot,
    UnsealSlot,
    ResetSlot,
    GetPrivkey,
    DoNothing,
}