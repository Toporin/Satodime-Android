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

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;

import java.util.Arrays;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import static org.satochip.client.Constants.*;
import static org.satochip.javacryptotools.coins.Constants.*;

import androidx.fragment.app.DialogFragment;


public class SealFormDialogFragment extends DialogFragment {
    
    private static final boolean DEBUG= BuildConfig.DEBUG;
    private static final String TAG = "SEAL_FORM_FRAGMENT";
    public static final int RESULT_OK=1;
    public static final int RESULT_CANCELLED=0;
    public static final int REQUEST_CODE=1;
        
    byte[] entropyUser;
    SHA256Digest sha256;
    
    private Spinner spinnerAsset;
    private Spinner spinnerCoin;
    private CheckBox cbTestnet;
    private LinearLayout llContract;
    private EditText etContract;
    private LinearLayout llTokenid;
    private EditText etTokenid;
    private EditText etEntropyIn;
    private TextView tvNotif; 
    private TextView tvEntropyOut; 
    
    // based on https://developer.android.com/guide/topics/ui/dialogs
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view= inflater.inflate(R.layout.dialog_uninitialized, null);
        
        //
        entropyUser= new byte[32];
        sha256 = new SHA256Digest();
        
        // widgets
        spinnerAsset= (Spinner) view.findViewById(R.id.spinner_asset_type);
        spinnerCoin= (Spinner) view.findViewById(R.id.spinner_coin_type);
        cbTestnet= (CheckBox) view.findViewById(R.id.checkbox_use_testnet);
        llContract= (LinearLayout) view.findViewById(R.id.group_contract);
        etContract= (EditText) view.findViewById(R.id.edittext_contract);
        llTokenid= (LinearLayout) view.findViewById(R.id.group_tokenid);
        etTokenid= (EditText) view.findViewById(R.id.edittext_tokenid);
        etEntropyIn= (EditText) view.findViewById(R.id.edittext_entropy_input);
        tvNotif= (TextView) view.findViewById(R.id.text_notification); 
        tvEntropyOut= (TextView) view.findViewById(R.id.value_entropy_output); 
               
        // update entropyOut when entropyIn changes
        etEntropyIn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //if(DEBUG) Log.d(TAG, "SealKeyslotActivity: beforeTextChanged: " + s); 
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //if(DEBUG) Log.d(TAG, "SealKeyslotActivity: onTextChanged: " + s); 
            }
            @Override
            public void afterTextChanged(Editable s) {
                // if text is >32 byte, compute sha256, otherwise, padd to 32 byte with 0;
                byte[] entropyRawByte=s.toString().getBytes();
                if (entropyRawByte.length>32){
                    sha256.reset();
                    sha256.update(entropyRawByte, 0, entropyRawByte.length);
                    sha256.doFinal(entropyUser, 0);
                } else {
                    Arrays.fill(entropyUser,(byte)0);
                    System.arraycopy(entropyRawByte, 0, entropyUser, 0, entropyRawByte.length);
                }
                String entropyHex= Hex.toHexString(entropyUser);
                // update layout
                tvEntropyOut.setText(entropyHex);
            }
        });
        
        // show asset types according to coin selectect
        final String[] array_coin = getResources().getStringArray(R.array.array_coin);
        final String[] array_asset_full = getResources().getStringArray(R.array.array_asset);
        final String[] array_asset_limited = getResources().getStringArray(R.array.array_asset_limited);
        spinnerCoin.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                ArrayAdapter<String> adp;
                String coin= array_coin[position];
                if (SUPPORTS_TOKEN_SET.contains(coin)){
                    adp = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, array_asset_full);
                } else{
                    adp = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, array_asset_limited);
                }
                adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerAsset.setAdapter(adp);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                if(DEBUG) Log.d(TAG, "SealKeyslotActivity: spinnerCoin onNothingSelected: "); 
            }
        });
        
        // show contract/tokenid for TOKEN/NFT assets (only)
        spinnerAsset.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) { 
                String asset= array_asset_full[position];
                if(DEBUG) Log.d(TAG, "SealKeyslotActivity: setOnItemSelectedListener: " + asset); 
                // update fields accordingly
                // show/hide contract field
                if (TOKENSET.contains(asset) || NFTSET.contains(asset)){
                    if(DEBUG) Log.d(TAG, "SealKeyslotActivity: SHOW CONTRACT in thread: "); 
                    llContract.setVisibility(View.VISIBLE);
                } else {
                    llContract.setVisibility(View.GONE);
                }
                // show/hide tokenid field
                if (NFTSET.contains(asset)){
                    if(DEBUG) Log.d(TAG, "SealKeyslotActivity: SHOW TOKENID in thread: "); 
                    llTokenid.setVisibility(View.VISIBLE);
                } else {
                    llTokenid.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                if(DEBUG) Log.d(TAG, "SealKeyslotActivity: onNothingSelected: "); 
            }
        });
        
        // build dialog
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.seal_keyslot_title) 
                // Add action buttons
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       SealFormDialogFragment.this.getDialog().cancel();
                       if(DEBUG) Log.d(TAG, "onCreateDialog - builder.setNegativeButton - onClick");
                       listener.onDialogNegativeClick(SealFormDialogFragment.this, REQUEST_CODE, RESULT_CANCELLED);
                   }
                })
                .setPositiveButton(R.string.seal_keyslot_confirm, null)
                .create();
                
                // https://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            
                @Override
                public void onShow(DialogInterface dialogInterface) {

                    Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        
                        @Override
                        public void onClick(View view) {
                
                            if(DEBUG) Log.d(TAG, "onCreateDialog - builder.setPositiveButton - onClick");
                            
                            try{
                                // check that all required data is provided & sanitize inputs
                                String asset= spinnerAsset.getSelectedItem().toString();
                                int assetInt= (int) MAP_CODE_BY_ASSET.get(asset);
                                if(DEBUG) Log.d(TAG, "SEAL asset: "+ asset);
                                String coin= spinnerCoin.getSelectedItem().toString();
                                if(DEBUG) Log.d(TAG, "SEAL coin: "+ coin);
                                boolean isTestnet= cbTestnet.isChecked();
                                if(DEBUG) Log.d(TAG, "SEAL isTestnet: "+ isTestnet);
                                int slip44Int= (int) MAP_SLIP44_BY_SYMBOL.get(coin);
                                if (isTestnet){
                                    slip44Int= slip44Int & 0x7FFFFFFF; // set msb to 0
                                }
                                ByteBuffer bb = ByteBuffer.allocate(4);
                                bb.putInt(slip44Int); // big endian
                                byte[] slip44= bb.array();  
                                // isToken or isNFT?
                                boolean isToken= TOKENSET.contains(asset);
                                boolean isNFT= NFTSET.contains(asset); 
                                // check contract: contract byte array should be [size(2b) | contract | 0-padding to 34b]
                                String contract= etContract.getText().toString();
                                byte[] contractByte;
                                if (isToken || isNFT){
                                    if(DEBUG) Log.d(TAG, "SEAL contract (before): "+ contract);
                                    String regex = "^(0x)?[0-9a-fA-F]{40}$";
                                    if(!contract.matches(regex)){
                                        throw new Exception(getResources().getString(R.string.exception_contract_format));
                                    }
                                    contract= contract.replaceFirst("^0x", ""); // remove "0x" if present
                                    try{
                                        contractByte= Hex.decode(contract); 
                                    } catch (Exception e) {
                                        throw new Exception(getResources().getString(R.string.exception_contract_format_hex));
                                    }   
                                    if (contractByte.length>32){
                                        throw new Exception(getResources().getString(R.string.exception_contract_too_long));
                                    }
                                } else {
                                    contractByte= new byte[0]; // ignore contract value
                                }
                                byte[] contractByteTLV= new byte[34];
                                contractByteTLV[0]=(byte)0;
                                contractByteTLV[1]=(byte)(contractByte.length);
                                System.arraycopy(contractByte, 0, contractByteTLV, 2, contractByte.length);
                                if(DEBUG) Log.d(TAG, "SEAL contract (after)   : "+ Hex.toHexString(contractByte));
                                // check tokenid: tokenid byte array should be [size(2b) | tokenid | 0-padding to 34b]
                                String tokenid= etTokenid.getText().toString();
                                byte[] tokenidByte;
                                if (isNFT){
                                    BigInteger tokenidBig= new BigInteger(tokenid); // tokenid is decimal-formated 
                                    String tokenidHexString=tokenidBig.toString(16); // convert to hex string
                                    if (tokenidHexString.length()%2==1){
                                        tokenidHexString= "0" + tokenidHexString; // must have an even number of chars
                                    }
                                    tokenidByte= Hex.decode(tokenidHexString); // convert to bytes
                                    if (tokenidByte.length>32){
                                        throw new Exception(getResources().getString(R.string.exception_tokenid_too_long));
                                    }
                                } else{
                                    tokenidByte= new byte[0]; // ignore tokenID
                                }
                                byte[] tokenidByteTLV= new byte[34];
                                tokenidByteTLV[0]=(byte)0;
                                tokenidByteTLV[1]=(byte)(tokenidByte.length);
                                System.arraycopy(tokenidByte, 0, tokenidByteTLV, 2, tokenidByte.length);
                                if(DEBUG) Log.d(TAG, "SEAL tokenid (before): "+ tokenid);
                                if(DEBUG) Log.d(TAG, "SEAL tokenid (after)   : "+ Hex.toHexString(tokenidByte));
                                // return data to activity
                                Intent resultIntent= new Intent();
                                //resultIntent.putExtra("keyNbr", keyNbr); //TODO: remove?
                                resultIntent.putExtra("entropyUser", entropyUser);
                                resultIntent.putExtra("asset", asset);
                                resultIntent.putExtra("assetInt", assetInt);
                                resultIntent.putExtra("slip44", slip44);
                                resultIntent.putExtra("contractByteTLV", contractByteTLV);
                                resultIntent.putExtra("tokenidByteTLV", tokenidByteTLV);
                                listener.onDialogPositiveClick(SealFormDialogFragment.this, REQUEST_CODE, RESULT_OK, resultIntent);
                                dialog.dismiss();
                            } catch (Exception e) {
                                if(DEBUG) Log.e(TAG, e.getMessage());
                                tvNotif.setText(getResources().getString(R.string.error) + e.getMessage());
                                tvNotif.setVisibility(View.VISIBLE);
                            } 
                            
                        }
                    });
                }
            });
        
        //return builder.create();
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
    
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface SealFormDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, int requestCode, int resultCode, Intent intent);
        public void onDialogNegativeClick(DialogFragment dialog, int requestCode, int resultCode);
    }

    // Use this instance of the interface to deliver action events
    SealFormDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the SealFormDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        if(DEBUG) Log.d(TAG, "onAttach");
        try {
            // Instantiate the SealFormDialogListener so we can send events to the host
            listener = (SealFormDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            //throw new ClassCastException(activity.toString() + " must implement SealFormDialogListener");
            throw new ClassCastException("ClassCastException: must implement SealFormDialogListener");
        }
    }

    
    
}
