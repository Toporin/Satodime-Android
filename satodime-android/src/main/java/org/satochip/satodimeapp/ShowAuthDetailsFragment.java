package org.satochip.satodimeapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.app.Dialog;
import android.content.DialogInterface;
import android.app.AlertDialog;

import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

// Deprecated 
public class ShowAuthDetailsFragment extends DialogFragment {
    
    private static final boolean DEBUG= BuildConfig.DEBUG;
    private static final String TAG = "SATODIME_AUTH_DETAILS";
    private static final int COLOR_AUTH_OK_BACKGROUND= 0xff90EE90; //0xff9cff57; //0xff64dd17; // 0xff90EE90 (lightgreen)
    private static final int COLOR_AUTH_KO_BACKGROUND=  0xffff867c; //0xffef5350; //
    private static final int COLOR_AUTH_OK_TXT= 0xff1b5e20;
    private static final int COLOR_AUTH_KO_TXT= 0xff7f0000;
    
    private String[] authResults=null;
    private String authRes= "";
    private String authCa= "";
    private String authSubca= "";
    private String authDevice= "";
    private String authError= "";
    private String authStatus="";
        
    private LinearLayout llGroupDetails;
    private TextView tvAuthStatus;
    private TextView tvCa;
    private TextView tvSubca;
    private TextView tvDevice;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view= inflater.inflate(R.layout.activity_show_auth_details, null);
        
        authResults = getArguments().getStringArray("authResults");    
        if (authResults != null){
            authRes= authResults[0];
            authCa= authResults[1];
            authSubca= authResults[2];
            authDevice= authResults[3];
            authError= authResults[4];
            authStatus="";
        }
        
        tvAuthStatus= (TextView) view.findViewById(R.id.text_auth_status);
        llGroupDetails= (LinearLayout) view.findViewById(R.id.group_auth_details);
        if (authRes.equals("OK")){
            authStatus= getResources().getString(R.string.auth_success);
            authStatus+="\n\n";
            //tvAuthStatus.setTextColor(COLOR_AUTH_OK_TXT); //Color.GREEN
            llGroupDetails.setBackgroundColor(COLOR_AUTH_OK_BACKGROUND);
        } else {
            authStatus= getResources().getString(R.string.auth_fail);
            authStatus+="\n\n" + getResources().getString(R.string.reason) + authError + "\n\n";
            //tvAuthStatus.setTextColor(COLOR_AUTH_KO_TXT); //tvAuthStatus.setTextColor(Color.RED);
            llGroupDetails.setBackgroundColor(COLOR_AUTH_KO_BACKGROUND);
        }
        tvAuthStatus.setText(authStatus);

        tvCa= (TextView) view.findViewById(R.id.text_ca);
        authCa= "======== Root CA certificate: ======== \r\n" + authCa;
        tvCa.setText(authCa);

        tvSubca= (TextView) view.findViewById(R.id.text_subca);
        authSubca= "======== Sub CA certificate: ======== \r\n" + authSubca;
        tvSubca.setText(authSubca);

        tvDevice= (TextView) view.findViewById(R.id.text_device);
        authDevice= "======== Device certificate: ======== \r\n" + authDevice;
        tvDevice.setText(authDevice);
               
        // build dialog
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.show_auth_details_title) 
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       if(DEBUG) Log.d(TAG, "ShowAuthDetailsFragment: builder.setPositiveButton - onClick");
                       // do something else?
                       dismiss();
                   }
                })
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
    
    
}
