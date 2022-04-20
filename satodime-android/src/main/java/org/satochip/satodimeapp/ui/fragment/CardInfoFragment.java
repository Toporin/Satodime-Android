package org.satochip.satodimeapp.ui.fragment;

import android.os.Bundle;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.app.Dialog;
import android.content.Context;
// import android.content.DialogInterface;
// import android.content.Intent;
import android.app.AlertDialog;
// import android.text.TextWatcher;
// import android.text.Editable;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.EditText;
//import android.widget.Button;

import androidx.fragment.app.DialogFragment;
import androidx.cardview.widget.CardView;

import org.satochip.satodimeapp.MainActivity;
import org.satochip.satodimeapp.R;

import static org.satochip.satodimeapp.BuildConfig.DEBUG;

public class CardInfoFragment extends DialogFragment {
    
    private static final String TAG = "SATODIME_CARDINFO";
    //private static final boolean DEBUG= BuildConfig.DEBUG;
    
    private boolean isConnected = false;
    private boolean isOwner = false;
    private String[] authResults;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view= inflater.inflate(R.layout.dialog_card_info, null);
        
        // get values passed in bundle
        isConnected = getArguments().getBoolean("isConnected");    
        isOwner = getArguments().getBoolean("isOwner");    
        authResults = getArguments().getStringArray("authResults");    
        
        LinearLayout llConnected= (LinearLayout) view.findViewById(R.id.ll_card_connected);
        TextView tvConnected = (TextView) view.findViewById(R.id.value_card_connected);
        ImageView ivConnected= (ImageView) view.findViewById(R.id.iv_card_connected);
        LinearLayout llOwner= (LinearLayout) view.findViewById(R.id.ll_card_owner);
        TextView tvOwner = (TextView) view.findViewById(R.id.value_card_owner);
        ImageView ivOwner= (ImageView) view.findViewById(R.id.iv_card_owner);
        LinearLayout llAuth= (LinearLayout) view.findViewById(R.id.ll_card_auth);
        TextView tvAuth = (TextView) view.findViewById(R.id.value_card_auth);
        ImageView ivAuth= (ImageView) view.findViewById(R.id.iv_card_auth);
        if (authResults == null){
            tvConnected.setText(R.string.card_connected_value_no);
            tvOwner.setText(R.string.card_connected_value_no);
            tvAuth.setText(R.string.card_connected_value_no);
        } else {
            // connection status
            if (isConnected){
                llConnected.setBackgroundResource(R.drawable.card_info_item_background);
                tvConnected.setText(R.string.card_connected_value_ok);
                ivConnected.setImageResource(R.drawable.ic_success);
            } else {
                tvConnected.setText(R.string.card_connected_value_ko);
            }
            // ownership status
            if (isOwner){
                llOwner.setBackgroundResource(R.drawable.card_info_item_background);
                tvOwner.setText(R.string.card_ownership_value_ok);
                ivOwner.setImageResource(R.drawable.ic_success);
            } else {
                tvOwner.setText(R.string.card_ownership_value_ko);
            }
            // authenticity status
            if (authResults[0].equals("OK")) {
                llAuth.setBackgroundResource(R.drawable.card_info_item_background);
                tvAuth.setText(R.string.card_status_value_ok);
                ivAuth.setImageResource(R.drawable.ic_success);
            } else {
                tvAuth.setText(R.string.card_status_value_ko);
            }
        }
        
        LinearLayout showCertificateBtn= (LinearLayout) view.findViewById(R.id.show_certificate_btn);
        showCertificateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).showAuthenticityDetailsDialog();
                
                /* final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);
                ViewGroup viewGroup = v.findViewById(android.R.id.content);
                View view = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_certificate_details, viewGroup, false);
                builder.setView(view);

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
                } else {
                    authStatus= getResources().getString(R.string.auth_fail);
                    authStatus+="\n\n" + getResources().getString(R.string.reason) + authError + "\n\n";
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

                final AlertDialog alertDialog = builder.create();
                alertDialog.show(); */
            }
        });
        
        // build dialog
        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog)
                .setView(view)
                .create();
        
        return dialog;
    }

/*     // increase size of dialog
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
    }     */

}