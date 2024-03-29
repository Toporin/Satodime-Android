package org.satochip.satodimeapp.data

enum class  NfcResultCode {
    Ok,

    RequireSetup,
    CardMismatch,
    OwnershipAlreadyClaimed,
    NotOwner,
    UnknownError,

    FailedToTakeOwnership,
    FailedToReleaseOwnership,
    FailedToSealVault,
    FailedToUnsealVault,
    FailedToResetVault,
    FailedToRecoverPrivkey,

    None,
    Busy,
    NfcError,
}