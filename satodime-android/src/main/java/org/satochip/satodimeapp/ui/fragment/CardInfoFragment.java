package org.satochip.satodimeapp.ui.fragment;

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
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;

import androidx.fragment.app.DialogFragment;
//import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import org.satochip.satodimeapp.MainActivity;
import org.satochip.satodimeapp.R;

import static org.satochip.satodimeapp.BuildConfig.DEBUG;

public class CardInfoFragment extends DialogFragment {
    
    private static final String TAG = "SATODIME_CARDINFO";
    //private static final boolean DEBUG= BuildConfig.DEBUG;
    
    private CardView toolBar;
    private ImageView backBtn;
    private LinearLayout showCertificateBtn;
    private LinearLayout cardConnectedLayout, cardNotConnectedLayout;

    private boolean isConnected = false;
    private boolean isOwner = false;
    private String[] authResults;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view= inflater.inflate(R.layout.activity_card_info, null);
        
        // get values passed in bundle
        isConnected = getArguments().getBoolean("isConnected");    
        isOwner = getArguments().getBoolean("isOwner");    
        authResults = getArguments().getStringArray("authResults");    
        
        // init
        toolBar = view.findViewById(R.id.toolbar);
        toolBar.setBackgroundResource(R.drawable.bottom_corer_round);
        cardConnectedLayout = view.findViewById(R.id.connected_card_layout);
        cardNotConnectedLayout = view.findViewById(R.id.not_connected_card_layout);
        showCertificateBtn = view.findViewById(R.id.show_certificate_btn);
        backBtn = view.findViewById(R.id.back_btn);
        
        
        // update card connected view
        TextView tvConnected = (TextView) view.findViewById(R.id.value_card_connected);
        if (authResults == null){
            cardConnectedLayout.setVisibility(View.GONE);
            cardNotConnectedLayout.setVisibility(View.VISIBLE);
            tvConnected.setText(R.string.card_connected_value_no);
        } else if (isConnected){
            cardConnectedLayout.setVisibility(View.VISIBLE);
            cardNotConnectedLayout.setVisibility(View.GONE);
            tvConnected.setText(R.string.card_connected_value_ok);
        } else {
            cardConnectedLayout.setVisibility(View.GONE);
            cardNotConnectedLayout.setVisibility(View.VISIBLE);
            tvConnected.setText(R.string.card_connected_value_ko);
        }
        
        // ownership
        Log.d(TAG, "isOwner: " + isOwner);
        TextView tvOwner = (TextView) view.findViewById(R.id.value_card_owner);
        TextView tvOwnerNot = (TextView) view.findViewById(R.id.card_ownership_not);
        if (authResults == null){
            tvOwner.setText(R.string.card_connected_value_no);
            tvOwnerNot.setText(R.string.card_connected_value_no);
        } else if (isOwner) {
            tvOwner.setText(R.string.card_ownership_value_ok);
            tvOwnerNot.setText(R.string.card_ownership_value_ok);
        } else {
            tvOwner.setText(R.string.card_ownership_value_ko);
            tvOwnerNot.setText(R.string.card_ownership_value_ko);
        }
        
        // update card authenticity status
        TextView tvStatus = (TextView) view.findViewById(R.id.value_card_status);
        TextView tvCardStatusNotConnected = (TextView) view.findViewById(R.id.card_status_when_not_connected);
        if (authResults == null){
            tvStatus.setText(R.string.card_connected_value_no);
            tvCardStatusNotConnected.setText(R.string.card_connected_value_no);
        } else if (authResults[0].equals("OK")) {
            tvStatus.setText(R.string.card_status_value_ok);
            tvCardStatusNotConnected.setText(R.string.card_status_value_ok);
        } else {
            tvStatus.setText(R.string.card_status_value_ko);
            tvCardStatusNotConnected.setText(R.string.card_status_value_ko);
        }
        
        clickListners(); //TODO
        
        // build dialog
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
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

    private void clickListners() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //finish();
                if (DEBUG) Log.d(TAG, "CardInfoFragment: clicked on back button"); 
                //https://stackoverflow.com/questions/20812922/how-to-close-the-current-fragment-by-using-button-like-the-back-button
                //getActivity().onBackPressed(); // close app?
                getActivity().getFragmentManager().popBackStack();
            }
        });
        
        showCertificateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //final AlertDialog.Builder builder = new AlertDialog.Builder(CardInfoFragment.this, R.style.CustomAlertDialog);
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);
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
                alertDialog.show();
            }
        });
    }

}