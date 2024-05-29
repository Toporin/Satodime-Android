package org.satochip.satodimeapp.data

import org.satochip.satodimeapp.R

enum class  NfcResultCode(val res : Int) {
    Ok(R.string.nfcResultCodeOk),

    RequireSetup(R.string.nfcResultCodeRequireSetup),
    CardMismatch(R.string.nfcResultCodeCardMismatch),
    OwnershipAlreadyClaimed(R.string.nfcResultCodeOwnershipAlreadyClaimed),
    NotOwner(R.string.nfcResultCodeNotOwner),
    UnknownError(R.string.nfcResultCodeUnknownError),

    FailedToTakeOwnership(R.string.nfcResultCodeFailedToTakeOwnership),
    FailedToReleaseOwnership(R.string.nfcResultCodeFailedToReleaseOwnership),
    FailedToSealVault(R.string.nfcResultCodeFailedToSealVault),
    FailedToUnsealVault(R.string.nfcResultCodeFailedToUnsealVault),
    FailedToResetVault(R.string.nfcResultCodeFailedToResetVault),
    FailedToRecoverPrivkey(R.string.nfcResultCodeFailedToRecoverPrivkey),

    None(R.string.nfcResultCodeNone),
    Busy(R.string.nfcResultCodeBusy),
    NfcError(R.string.nfcResultCodeNfcError),
}