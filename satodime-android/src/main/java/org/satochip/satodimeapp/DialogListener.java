package org.satochip.satodimeapp;

import android.content.Intent;
import androidx.fragment.app.DialogFragment;

/* The activity that creates an instance of dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. 
 * Based on https://developer.android.com/guide/topics/ui/dialogs */
public interface DialogListener {
    public void onDialogPositiveClick(DialogFragment dialog, int requestCode, int resultCode, Intent intent);
    public void onDialogNegativeClick(DialogFragment dialog, int requestCode, int resultCode);
}