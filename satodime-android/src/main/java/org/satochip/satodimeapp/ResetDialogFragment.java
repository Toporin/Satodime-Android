package org.satochip.satodimeapp;

import android.os.Bundle;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.AlertDialog;
import android.text.TextWatcher;
import android.text.Editable;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;

import org.satochip.satodimeapp.R;	
import org.satochip.satodimeapp.BuildConfig;	
import org.satochip.satodimeapp.DialogListener;	
import static org.satochip.satodimeapp.Constants.*;	

import java.util.Arrays;
import java.util.List;


import androidx.fragment.app.DialogFragment;


public class ResetDialogFragment extends DialogFragment {
    
    private static final boolean DEBUG= BuildConfig.DEBUG;
    private static final String TAG = "RESET_FRAGMENT";
    
    // Use this instance of the interface to deliver action events
    private DialogListener listener;
    
    private int keyslotNbr;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keyslotNbr = getArguments().getInt("keyslotNbr");
        
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view= inflater.inflate(R.layout.dialog_reset, null);
        
        TextView resetBtn= (TextView) view.findViewById(R.id.reset_btn);
        TextView cancelBtn= (TextView) view.findViewById(R.id.cancel_btn);
        
        // button cancel
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dialog.dismiss();
                listener.onDialogNegativeClick(ResetDialogFragment.this, REQUEST_CODE_RESET, RESULT_CANCELLED);
                getDialog().dismiss();
            }
        });
        
        // button reset
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DEBUG) Log.d(TAG, "onCreateDialog - builder.setPositiveButton - onClick");
                Intent resultIntent= new Intent();
                resultIntent.putExtra("keyslotNbr", keyslotNbr);
                listener.onDialogPositiveClick(ResetDialogFragment.this, REQUEST_CODE_RESET, RESULT_OK, resultIntent);
                getDialog().dismiss();
            }
        });
        
        // build dialog
        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog)
                .setView(view)
                .create();
                
        return dialog;
    }

    // increase size of dialog
    @Override
    public void onStart(){
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null){
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            //int height = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }    
    
    // Override the Fragment.onAttach() method to instantiate the ResetDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        if(DEBUG) Log.d(TAG, "onAttach");
        try {
            // Instantiate the ResetDialogListener so we can send events to the host
            listener = (DialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            //throw new ClassCastException(activity.toString() + " must implement ResetDialogListener");
            throw new ClassCastException("ClassCastException: must implement ResetDialogListener");
        }
    }

}
