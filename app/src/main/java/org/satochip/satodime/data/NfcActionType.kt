package org.satochip.satodime.data

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