package org.satochip.satodimeapp.ui.fragment;

import android.app.Dialog;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.satochip.satodimeapp.BuildConfig;
import org.satochip.satodimeapp.MainActivity;
import org.satochip.satodimeapp.R;

import static org.satochip.client.Constants.*;

//import java.util.HashMap;

import androidx.fragment.app.DialogFragment;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class AuthenticityDetailsFragment extends DialogFragment {

    private String[] authResults;
        
    @Override	
    public Dialog onCreateDialog(Bundle savedInstanceState) {	
        // Get the layout inflater	
        LayoutInflater inflater = requireActivity().getLayoutInflater();	
        // Inflate and set the layout for the dialog	
        // Pass null as the parent view because its going in the dialog layout	
        View view= inflater.inflate(R.layout.dialog_certificate_details, null);	
        
        authResults = getArguments().getStringArray("authResults");    
        
        String authRes = "";
        String authCa = "";
        String authSubca = "";
        String authDevice = "";
        String authError = "";
        String authStatus = "";

        LinearLayout llGroupDetails;
        TextView tvAuthStatus;
        TextView tvCa;
        TextView tvSubca;
        TextView tvDevice;

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
            tvAuthStatus.setTextColor(getResources().getColor(R.color.green));
        } else {
            authStatus= getResources().getString(R.string.auth_fail);
            authStatus+="\n\n" + getResources().getString(R.string.reason) + authError + "\n\n";
            tvAuthStatus.setTextColor(getResources().getColor(R.color.RED));
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
    
}