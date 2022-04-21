package org.satochip.satodimeapp;

import android.content.Intent;
import androidx.fragment.app.DialogFragment;

public interface DialogListener {
    public void onDialogPositiveClick(DialogFragment dialog, int requestCode, int resultCode, Intent intent);
    public void onDialogNegativeClick(DialogFragment dialog, int requestCode, int resultCode);
}