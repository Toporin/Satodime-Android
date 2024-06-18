package org.satochip.satodimeapp.data

import org.satochip.satodimeapp.R

enum class  NfcResultCode(val resTitle : Int, val resMsg : Int, val resImage : Int) {
    Ok(R.string.nfcTitleSuccess, R.string.nfcOk, R.drawable.icon_check_gif),

    RequireSetup(R.string.nfcTitleWarning, R.string.nfcSatodimeNeedsSetup, R.drawable.error_24px),
    CardMismatch(R.string.nfcTitleWarning, R.string.nfcCardMismatch, R.drawable.error_24px),
    OwnershipAlreadyClaimed(R.string.nfcTitleWarning, R.string.nfcTransferAlreadyDone, R.drawable.error_24px),
    NotOwner(R.string.nfcTitleWarning, R.string.nfcUnlockSecretNotFound, R.drawable.error_24px),
    UnknownError(R.string.nfcTitleWarning, R.string.nfcErrorOccured, R.drawable.error_24px),

    FailedToListVaults(R.string.nfcTitleWarning, R.string.nfcVaultsListFailed, R.drawable.error_24px),
    FailedToTakeOwnership(R.string.nfcTitleWarning, R.string.nfcOwnershipAcceptFailed, R.drawable.error_24px),
    FailedToReleaseOwnership(R.string.nfcTitleWarning, R.string.nfcOwnershiptTransferFailed, R.drawable.error_24px),
    FailedToSealVault(R.string.nfcTitleWarning, R.string.nfcVaultSealedFailed, R.drawable.error_24px),
    FailedToUnsealVault(R.string.nfcTitleWarning, R.string.nfcVaultUnsealFailed, R.drawable.error_24px),
    FailedToResetVault(R.string.nfcTitleWarning, R.string.nfcVaultResetFailed, R.drawable.error_24px),
    FailedToRecoverPrivkey(R.string.nfcTitleWarning, R.string.nfcPrivkeyRecoverFailed, R.drawable.error_24px),

    ListVaultsSuccess(R.string.nfcTitleSuccess, R.string.nfcVaultsListSuccess, R.drawable.icon_check_gif),
    TakeOwnershipSuccess(R.string.nfcTitleSuccess, R.string.nfcOwnershipAcceptSuccess, R.drawable.icon_check_gif),
    ReleaseOwnershipSuccess(R.string.nfcTitleSuccess, R.string.nfcOwnershipTransferSuccess, R.drawable.icon_check_gif),
    SealVaultSuccess(R.string.nfcTitleSuccess, R.string.nfcVaultSealedSuccess, R.drawable.icon_check_gif),
    UnsealVaultSuccess(R.string.nfcTitleSuccess, R.string.nfcVaultUnsealSuccess, R.drawable.icon_check_gif),
    ResetVaultSuccess(R.string.nfcTitleSuccess, R.string.nfcVaultResetSuccess, R.drawable.icon_check_gif),
    RecoverPrivkeySuccess(R.string.nfcTitleSuccess, R.string.nfcPrivkeyRecoverSuccess, R.drawable.icon_check_gif),

    None(R.string.scanning, R.string.nfcResultCodeNone, R.drawable.error_24px),
    Busy(R.string.scanning, R.string.nfcResultCodeBusy, R.drawable.contactless_24px),
    NfcError(R.string.nfcTitleWarning, R.string.nfcResultCodeNfcError, R.drawable.error_24px),

    //nfcNeedsFirstVaults
    //nfcUnlockSecretNotFound
}