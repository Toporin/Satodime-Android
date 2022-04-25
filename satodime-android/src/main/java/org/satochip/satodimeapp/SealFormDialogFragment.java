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

import static org.satochip.client.Constants.*;
import static org.satochip.javacryptotools.coins.Constants.*;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import androidx.fragment.app.DialogFragment;

public class SealFormDialogFragment extends DialogFragment {
    
    private static final boolean DEBUG= BuildConfig.DEBUG;
    private static final String TAG = "SEAL_FORM_FRAGMENT";
    // public static final int RESULT_OK=1;
    // public static final int RESULT_CANCELLED=0;
    // public static final int REQUEST_CODE=1;
    
    // Use this instance of the interface to deliver action events
    private DialogListener listener;
    
    private int keyslotNbr;
    byte[] entropyUser;
    SHA256Digest sha256;
    
    private AlertDialog dialog;
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
        super.onCreate(savedInstanceState);
        keyslotNbr = getArguments().getInt("keyslotNbr");
        
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view= inflater.inflate(R.layout.activity_seal_keyslot, null);
        
        //entropy
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
        TextView transferBtn= (TextView) view.findViewById(R.id.transfer_btn);
        TextView cancelBtn= (TextView) view.findViewById(R.id.cancel_btn);
        
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
                    // TODO: set input type and allowed chars according to coin type
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
        
        // button cancel
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dialog.dismiss();
                listener.onDialogNegativeClick(SealFormDialogFragment.this, REQUEST_CODE_SEAL, RESULT_CANCELLED);
                getDialog().dismiss();
            }
        });
        
        // button seal
        transferBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DEBUG) Log.d(TAG, "onCreateDialog - builder.setPositiveButton - onClick");

                try {
                    // check that all required data is provided & sanitize inputs
                    String asset= spinnerAsset.getSelectedItem().toString();
                    int assetInt= (int) MAP_CODE_BY_ASSET.get(asset);
                    if(DEBUG) Log.d(TAG, "SEAL asset: " + asset);
                    String coin= spinnerCoin.getSelectedItem().toString();
                    if(DEBUG) Log.d(TAG, "SEAL coin: " + coin);
                    boolean isTestnet= cbTestnet.isChecked();
                    if(DEBUG) Log.d(TAG, "SEAL isTestnet: " + isTestnet);
                    int slip44Int= (int) MAP_SLIP44_BY_SYMBOL.get(coin);
                    if (isTestnet) {
                        slip44Int= slip44Int & 0x7FFFFFFF; // set msb to 0
                    }
                    ByteBuffer bb= ByteBuffer.allocate(4);
                    bb.putInt(slip44Int); // big endian
                    byte[] slip44= bb.array();
                    // isToken or isNFT?
                    boolean isToken= TOKENSET.contains(asset);
                    boolean isNFT= NFTSET.contains(asset);
                    // check contract: contract byte array should be [size(2b) | contract | 0-padding to 34b]
                    String contract= etContract.getText().toString();
                    byte[] contractBytes;
                    if (isToken || isNFT) {
                        contractBytes= checkContractFieldToBytes(contract, coin);
                    } else {
                        contractBytes= new byte[0]; // ignore contract value
                    }
                    byte[] contractByteTLV= new byte[34];
                    contractByteTLV[0]= (byte) 0;
                    contractByteTLV[1]= (byte) (contractBytes.length);
                    System.arraycopy(contractBytes, 0, contractByteTLV, 2, contractBytes.length);
                    if(DEBUG) Log.d(TAG, "SEAL contract (after)   : " + Hex.toHexString(contractBytes));
                    // check tokenid: tokenid byte array should be [size(2b) | tokenid | 0-padding to 34b]
                    String tokenid= etTokenid.getText().toString();
                    byte[] tokenidBytes;
                    if (isNFT) {
                       tokenidBytes= checkTokenidFieldToBytes(tokenid);
                    } else {
                        tokenidBytes= new byte[0]; // ignore tokenID
                    }
                    byte[] tokenidByteTLV= new byte[34];
                    tokenidByteTLV[0]= (byte) 0;
                    tokenidByteTLV[1]= (byte) (tokenidBytes.length);
                    System.arraycopy(tokenidBytes, 0, tokenidByteTLV, 2, tokenidBytes.length);
                    if(DEBUG) Log.d(TAG, "SEAL tokenid (before): " + tokenid);
                    if(DEBUG) Log.d(TAG, "SEAL tokenid (after)   : " + Hex.toHexString(tokenidBytes));

                    // return data to activity
                    Intent resultIntent= new Intent();
                    resultIntent.putExtra("keyslotNbr", keyslotNbr);
                    resultIntent.putExtra("entropyUser", entropyUser);
                    resultIntent.putExtra("asset", asset);
                    resultIntent.putExtra("assetInt", assetInt);
                    resultIntent.putExtra("slip44", slip44);
                    resultIntent.putExtra("contractByteTLV", contractByteTLV);
                    resultIntent.putExtra("tokenidByteTLV", tokenidByteTLV);
                    listener.onDialogPositiveClick(SealFormDialogFragment.this, REQUEST_CODE_SEAL, RESULT_OK, resultIntent);
                    getDialog().dismiss();
                    
                } catch (Exception e) {
                    if(DEBUG) Log.e(TAG, e.getMessage());
                    tvNotif.setText(getResources().getString(R.string.error) + e.getMessage());
                    tvNotif.setVisibility(View.VISIBLE);
                }
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

    // Override the Fragment.onAttach() method to instantiate the DialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        if(DEBUG) Log.d(TAG, "onAttach");
        try {
            // Instantiate the SealFormDialogListener so we can send events to the host
            listener = (DialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            //throw new ClassCastException(activity.toString() + " must implement DialogListener");
            throw new ClassCastException("ClassCastException: must implement DialogListener");
        }
    }

    public byte[] checkContractFieldToBytes(String contract, String coin) throws Exception{
        
        if (DEBUG) Log.d(TAG, "SEAL contract (before): " + contract);
        
        if (contract.equals("")) return new byte[0];
        
        byte[] contractBytes= null;
        List<String> hexCoinList = Arrays.asList("ETH", "ETC", "BSC");
        if (hexCoinList.contains(coin)){
            // hex value
            if (!contract.matches("^(0x)?[0-9a-fA-F]{40}$")) {
                throw new Exception(getResources().getString(R.string.exception_contract_format));
            }
            contract= contract.replaceFirst("^0x", ""); // remove "0x" if present
            try {
                contractBytes= Hex.decode(contract);
            } catch (Exception e) {
                throw new Exception(getResources().getString(R.string.exception_contract_format_hex));
            }
            if (contractBytes.length > 20) {
                throw new Exception(getResources().getString(R.string.exception_contract_too_long));
            }
            return contractBytes;
        } 
        else if (coin.equals("XCP")){
            // https://counterparty.io/docs/protocol_specification/
            String asset= contract;
            String subasset="";
            // contract cannot start, end with '.' or contains consecutive dots 
            //https://stackoverflow.com/questions/40718851/regex-that-does-not-allow-consecutive-dots
            // TODO
            // if (!asset.matches("(?!\\.)(?!.*\\.$)(?!.*?\\.\\.)")) {
                // throw new Exception(getResources().getString(R.string.exception_contract_format)); // Wrong asset format: {asset} (asset cannot start or end with dot or contain consecutive dots)"
            // }
            if (asset.contains(".")){
                // contains subasset
                String[] parts= asset.split(".", 2);
                asset= parts[0];
                subasset= parts[1];
                String minlength=String.valueOf(1);
                String maxlength= String.valueOf(250- asset.length() -1);
                String pattern= "^[a-zA-Z0-9.-_@!]{" + minlength + "," + maxlength + "}$";
                if (!subasset.matches(pattern)) {
                    throw new Exception(getResources().getString(R.string.exception_contract_format)); // "Wrong subasset format: {subasset} (check for length and unauthorized characters)"
                }
            }
            if (asset.startsWith("A") || asset.startsWith("a")){
                // numeric asset
                asset= asset.toUpperCase(Locale.ENGLISH); // a=>A
                String nbrString= asset.substring(1); // remove the "A"
                BigInteger nbrBig;
                try{
                    nbrBig= new BigInteger(nbrString);
                } catch (Exception e) {
                    throw new Exception("Wrong numeric asset format: {asset} is not an integer");
                }
                BigInteger minBound= (new BigInteger("26")).pow(12).add(BigInteger.ONE);
                BigInteger maxBound= (new BigInteger("256")).pow(8);
                if ( (nbrBig.compareTo(minBound)<0) || (nbrBig.compareTo(maxBound)>0) ){
                     throw new Exception("Wrong numeric asset format: {asset} (numeric value outside of bounds)");
                }
            } else {
                // named asset
                asset= asset.toUpperCase(Locale.ENGLISH);
                if (!asset.matches("^[A-Z]{4,12}$")) {
                    throw new Exception("Wrong named asset format: {asset} (should be 4-12 uppercase latin characters)");
                }
            }
            // encode 
            if (subasset.equals("")){
                contract= asset;
            }else {
                contract= asset+ "." + subasset;
            }
            contractBytes = contract.getBytes(StandardCharsets.UTF_8);
            if (contractBytes.length>32){
                throw new Exception("Unfortunately, Satodime supports only asset name smaller than 32 char");
            }
            return contractBytes;
        }
        else {
            throw new Exception("Unsupported blockchain" + coin); 
        }
    }
    
    public byte[] checkTokenidFieldToBytes(String tokenid) throws Exception{
        
        if (tokenid.equals("")) tokenid="0"; // default 
        
        BigInteger tokenidBig= new BigInteger(tokenid); // tokenid is decimal-formated
        String tokenidHexString= tokenidBig.toString(16); // convert to hex string
        if (tokenidHexString.length() % 2== 1) {
            tokenidHexString= "0" + tokenidHexString; // must have an even number of chars
        }
        byte[] tokenidBytes= Hex.decode(tokenidHexString); // convert to bytes
        if (tokenidBytes.length > 32) {
            throw new Exception(getResources().getString(R.string.exception_tokenid_too_long));
        }
        return tokenidBytes;
    }
    
}
