package org.satochip.satodimeapp;

import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.content.Context;
import android.view.View;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;

// fragment
import android.support.v4.app.Fragment; 
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.satochip.satodimeapp.R;
import org.satochip.satodimeapp.BuildConfig;
import org.satochip.io.*;
import org.satochip.io.CardChannel;
import org.satochip.io.CardListener;
import org.satochip.android.NFCCardManager;

import org.bouncycastle.util.encoders.Hex;

import static org.satochip.client.Constants.*;
import org.satochip.client.SatochipCommandSet;
import org.satochip.client.ApplicationStatus;
import org.satochip.client.SatodimeStatus;
import org.satochip.client.SatodimeKeyslotStatus;
import org.satochip.client.SatochipParser;
import org.satochip.client.*;
//import org.satochip.client.Util;

import org.satochip.javacryptotools.*;
import static org.satochip.javacryptotools.coins.Constants.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL; 
 
public class MainActivity extends AppCompatActivity 
                                                implements SealFormDialogFragment.SealFormDialogListener, SettingsDialogFragment.SettingsDialogListener {
    
    private static final boolean DEBUG= BuildConfig.DEBUG;
    private static final String TAG = "SATODIME";
    private static final int REQUEST_CODE_SEAL_KEYSLOT_FORM= 666; 
    private static final int COLOR_GREEN= 0xff90EE90; //0xffb2ff59; // #0xff90EE90
    private static final int COLOR_ORANGE= 0xFFFFD580; 
    private static final int COLOR_RED= 0xFFFF0000;
    private static final int[] BACKGROUND_COLORS= {Color.LTGRAY, COLOR_GREEN, COLOR_ORANGE};
    private static final int PENDING_ACTION_NONE=-1;
    private static final int PENDING_ACTION_RESET=0;
    private static final int PENDING_ACTION_SEAL=1;
    private static final int PENDING_ACTION_UNSEAL=2;
    private static final int PENDING_ACTION_TRANSFER=3;
    // private static String[] PENDING_ACTION_TXT; //= getResources().getStringArray(R.array.array_pending_actions); //{"Reset keyslot", "Seal Keyslot", "Unseal keyslot", "Transfer card ownership"};
    // private static String[] ARRAY_KEYSLOT_STATES; //= getResources().getStringArray(R.array.array_keyslot_states); 
    // private static String[] ACTIONS_FROM_STATE; //= getResources().getStringArray(R.array.array_actions_from_state); //{"Seal key!", "Unseal key!", "Reset key!"};
        
    // green: 81c784, b2fab4, b2ff59
    // orange: fbc02d, FFD580
    
    private NfcAdapter nfcAdapter;
    private NFCCardManager cardManager;
    private boolean isConnected=false;
    private SatochipCommandSet cmdSet= null;
    private SatochipParser parser=null;
    private SatodimeStatus satodimeStatus=null;
    private byte[] keysState=null;
    private int nbKeyslot=0;
    private List<HashMap<String,Object>> keyInfoList = null;
    
    // flags & vars used when action is pending
    private String keyslotAuthentikeyHex=""; 
    private int keyslotNbr=-1; 
    private int pendingAction= PENDING_ACTION_NONE;
    // used when sealing a slot...
    private int sealKeyslotAsset=-1;
    private byte[] sealKeyslotSlip44= null;
    private byte[] sealKeyslotContractByteTLV= null;
    private byte[] sealKeyslotTokenidByteTLV= null;
    private byte[] sealKeyslotEntropyUser= null;
    
    // detect ownership & issues
    byte[] authentikey= null;
    String authentikeyHex= null;
    private boolean isOwner= false;
    private static final int MAX_OWNERSHIP_ERROR= 5;
    private int ownership_error_counter=0;
    
    // settings button 
    private Button buttonSettings=null;
    
    // keyslot layout root
    private LinearLayout keyslotsLayout=null;
    private boolean isLayoutReady= false;
    
    // API KEYS
    private HashMap<String, String> APIKEYS= null;
    
    // preferences & settings
    private SharedPreferences prefs =null;
    private String appLanguage;
    private String appFiat="(none)";
    private boolean useFiat= false;
    private boolean appDarkModeEnabled;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(DEBUG) Log.d(TAG, "LIFECYCLE ONCREATE");
        
        Activity myactivity= this;
        Context mycontext= this;
        boolean[] isReconnectionFlag= new boolean[1];
        isReconnectionFlag[0]=false; // detect reconnection
        
        // load API keys from resources
        APIKEYS= new HashMap<String, String>();
        APIKEYS.put("API_KEY_ETHERSCAN", getResources().getString(R.string.API_KEY_ETHERSCAN));
        APIKEYS.put("API_KEY_ETHPLORER", getResources().getString(R.string.API_KEY_ETHPLORER));
        APIKEYS.put("API_KEY_BSCSCAN", getResources().getString(R.string.API_KEY_BSCSCAN));
        
        // persistent memory & locale & fiat
        prefs = getSharedPreferences("satodime", MODE_PRIVATE);
        appLanguage = prefs.getString("appLanguage", Locale.getDefault().getLanguage());
        Locale myLocale = new Locale(appLanguage);
        Locale.setDefault(myLocale);
        Resources resource = getResources();
        Configuration config = resource.getConfiguration();
        config.locale = myLocale;
        resource.updateConfiguration(config, resource.getDisplayMetrics());
        appFiat = prefs.getString("appFiat", "(none)");
        if (!appFiat.equals("(none)")){
            useFiat= true;
        }
        
        // load some string arrays 
        // PENDING_ACTION_TXT= getResources().getStringArray(R.array.array_pending_actions); //{"Reset keyslot", "Seal Keyslot", "Unseal keyslot", "Transfer card ownership"};
        // ARRAY_KEYSLOT_STATES= getResources().getStringArray(R.array.array_keyslot_states); 
        // ACTIONS_FROM_STATE= getResources().getStringArray(R.array.array_actions_from_state); //{"Seal key!", "Unseal key!", "Reset key!"};
        
        if(DEBUG) Log.d(TAG, "Starting SatoDime Application: OnCreate");
        setContentView(R.layout.activity_main);
        // settings button
        buttonSettings= (Button) findViewById(R.id.button_settings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DEBUG) Log.d(TAG, "BUTTON SETTINGS CLICKED!");
                DialogFragment fragment = new SettingsDialogFragment();
                fragment.show(getSupportFragmentManager(), "SettingsDialogFragment");
            }
        });

        // NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        cardManager = new NFCCardManager();
        if(DEBUG) Log.d(TAG, "Created cardManager object");
        cardManager.setCardListener(new CardListener() {
            @Override
            public void onConnected(CardChannel cardChannel) {
                try {
                    // Applet-specific code
                    if(DEBUG) Log.d(TAG, "onConnected!");
                    isConnected= true;
                    isLayoutReady= false; 
                  
                    // detect reconnection and reset view...
                    if (isReconnectionFlag[0]){
                        runOnUiThread(new Runnable() {
                            public void run() {
                                setContentView(R.layout.activity_main);
                            }
                        });
                    }
                  
                    // satodime object
                    cmdSet = new SatochipCommandSet(cardChannel);
                    if (DEBUG) {
                        cmdSet.setLoggerLevel("info"); 
                        Log.d(TAG, "Created a SatochipCommandSet object");} 
                    else{
                        cmdSet.setLoggerLevel("warning");}
                    parser= cmdSet.getParser();

                    // First thing to do is selecting the applet on the card.
                    APDUResponse rapdu= cmdSet.cardSelect("satodime");
                    if(DEBUG) Log.d(TAG, "Applet selected:" + rapdu.toHexString());
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast toast = Toast.makeText(getApplicationContext(), R.string.card_connected, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                  
                    // get card status
                    APDUResponse rapdu2= cmdSet.cardGetStatus();
                    ApplicationStatus cardStatus = cmdSet.getApplicationStatus();
                    if(DEBUG) Log.d(TAG, "Card status:" + cardStatus.toString());
                  
                    // check if setup done
                    if (!cardStatus.isSetupDone()){
                    
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                
                                new AlertDialog.Builder(MainActivity.this)
                                            .setTitle(R.string.accept_transfer_question)
                                            .setMessage(R.string.accept_transfer_warning)
                                            .setPositiveButton(R.string.accept_transfer_confirmation, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) { 
                                                    if(DEBUG) Log.d(TAG, "OWNERSHIP APPROVAL DIALOG " + "YES  has been clicked!");
                                                    SecureRandom random = new SecureRandom();
                                                    byte pin_tries0= (byte)5;
                                                    byte[] pin0= new byte[8];
                                                    random.nextBytes(pin0);
                                                    APDUResponse rapduSetup= cmdSet.cardSetup(pin_tries0,  pin0);
                                                    if (rapduSetup.isOK()){
                                                        // save unlockSecret in SharedPreferences
                                                        authentikey= cmdSet.getAuthentikey();
                                                        authentikeyHex= parser.toHexString(authentikey);
                                                        if(DEBUG) Log.d(TAG, "Satodime authentikey: " + authentikeyHex);
                                                        byte[] unlockSecret= cmdSet.getSatodimeUnlockSecret();
                                                        String unlockSecretHex= parser.toHexString(unlockSecret);
                                                        prefs.edit().putString(authentikeyHex, unlockSecretHex).apply();
                                                        Toast toast = Toast.makeText(getApplicationContext(), R.string.transfer_success, Toast.LENGTH_SHORT);
                                                        toast.show();
                                                    }else{
                                                        if(DEBUG) Log.e(TAG, "Error: setupDone: " + rapduSetup.toHexString());
                                                        Toast toast = Toast.makeText(getApplicationContext(), R.string.transfer_fail, Toast.LENGTH_SHORT);
                                                        toast.show();
                                                    }
                                                }
                                            })
                                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) { 
                                                    if(DEBUG) Log.d(TAG, "OWNERSHIP APPROVAL DIALOG " + "NO  has been clicked!");
                                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.transfer_rejected, Toast.LENGTH_SHORT);
                                                    toast.show();
                                                }
                                            })
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show();
                            } // run
                        });
                    } 

                    // get authentikey
                    authentikey= cmdSet.getAuthentikey();
                    authentikeyHex= parser.toHexString(authentikey);
                    if(DEBUG) Log.d(TAG, "Satodime authentikey: " + authentikeyHex);
                  
                    // check if unlock_secret is available for the authentikey
                    if (prefs.contains(authentikeyHex)){
                        if(DEBUG) Log.d(TAG, "DEBUGUNLOCK recovered: START");
                        String unlockSecretHex= prefs.getString(authentikeyHex, null);
                        byte[] unlockSecret= parser.fromHexString(unlockSecretHex);
                        cmdSet.setSatodimeUnlockSecret(unlockSecret);
                    }
                  
                    // get satodime status
                    satodimeStatus= cmdSet.getSatodimeStatus();
                    if(DEBUG) Log.d(TAG, "Satodime status:" + satodimeStatus.toString());
                    keysState= satodimeStatus.getKeysState();
                    nbKeyslot= satodimeStatus.getMaxNumKeys();
                  
                    // check if a keyslot request has been done (seal-unseal-reset-transfer)
                    if (pendingAction != PENDING_ACTION_NONE){
                        
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String[] array_pending_actions= getResources().getStringArray(R.array.array_pending_actions); 
                                String msg= getResources().getString(R.string.request_pending_msg) +" " + array_pending_actions[pendingAction] + "\n";
                                if (pendingAction != PENDING_ACTION_TRANSFER){
                                    msg+=getResources().getString(R.string.keyslot_nbr) + keyslotNbr + "\n";
                                }
                                msg+=getResources().getString(R.string.proceed_with_request);
                                
                                new AlertDialog.Builder(MainActivity.this)
                                            .setTitle(R.string.request_pending_title)
                                            .setMessage(msg)
                                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) { 
                                                    if(DEBUG) Log.d(TAG, "PENDING REQUEST DIALOG " + "YES  has been clicked!");
                                                    switch (pendingAction) {
                                                        case PENDING_ACTION_RESET:  
                                                                sendResetKeyslotApduToCard();
                                                                break;
                                                        case PENDING_ACTION_SEAL:  
                                                                sendSealKeyslotApduToCard();
                                                                break;
                                                        case PENDING_ACTION_UNSEAL:  
                                                                sendUnsealKeyslotApduToCard();
                                                                break;
                                                        case PENDING_ACTION_TRANSFER:  
                                                                sendTransferApduToCard();
                                                                break;
                                                    }
                                                }
                                             })
                                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) { 
                                                    if(DEBUG) Log.d(TAG, "PENDING REQUEST DIALOG " + "NO  has been clicked!");
                                                    pendingAction= PENDING_ACTION_NONE;
                                                    Toast toast = Toast.makeText(getApplicationContext(), R.string.request_rejected, Toast.LENGTH_SHORT);
                                                    toast.show();
                                                }
                                            })
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show();
                            
                            } //run
                        }); // thread
                    }
                  
                    // update card connected view
                    TextView tvConnected= (TextView) findViewById(R.id.value_card_connected);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvConnected.setText(R.string.card_connected_value_ok); 
                            tvConnected.setTextColor(COLOR_GREEN);
                        }
                    });
                    
                    // settings button
                    buttonSettings= (Button) findViewById(R.id.button_settings);
                    buttonSettings.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(DEBUG) Log.d(TAG, "BUTTON SETTINGS CLICKED!");
                            DialogFragment fragment = new SettingsDialogFragment();
                            fragment.show(getSupportFragmentManager(), "SettingsDialogFragment");
                        }
                    });
                    
                    // check card authenticity
                    String[] authResults= cmdSet.cardVerifyAuthenticity();
                    for (int index=0; index<authResults.length; index++){
                        if(DEBUG) Log.d(TAG, "DEBUGAUTH : " + authResults[index]);
                    }
                    // update status
                    TextView tvStatus= (TextView) findViewById(R.id.value_card_status);
                    Button buttonAuth= (Button) findViewById(R.id.button_auth);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (authResults[0].equals("OK")){
                                tvStatus.setText(R.string.card_status_value_ok); 
                                tvStatus.setTextColor(COLOR_GREEN);
                            } else{
                                tvStatus.setText(R.string.card_status_value_ko); 
                                tvStatus.setTextColor(COLOR_RED);
                            }
                            buttonAuth.setEnabled(true); 
                        }
                    });
                    // update buttonAuth
                    buttonAuth.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(DEBUG) Log.d(TAG, "BUTTON DETAILS CLICKED!");
                            //using fragment
                            Bundle bundle = new Bundle();
                            bundle.putStringArray("authResults",authResults);
                            DialogFragment fragment = new ShowAuthDetailsFragment();
                            fragment.setArguments(bundle);
                            fragment.show(getSupportFragmentManager(), "ShowAuthDetailsFragment");
                        }
                    });
                  
                    // update layout header info (details & transfert card button)
                    isOwner= satodimeStatus.isOwner();
                    Button buttonTransfer= (Button) findViewById(R.id.button_transfer);
                    TextView tvOwner= (TextView) findViewById(R.id.value_card_owner);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isOwner){
                                buttonTransfer.setEnabled(true); 
                                tvOwner.setText(R.string.card_ownership_value_ok);
                                tvOwner.setTextColor(COLOR_GREEN);
                            }else{
                                buttonTransfer.setEnabled(false); 
                                tvOwner.setText(R.string.card_ownership_value_ko);
                                tvOwner.setTextColor(COLOR_ORANGE);
                            }
                        }
                    });
                    buttonTransfer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(DEBUG) Log.d(TAG, "BUTTON TRANSFER CLICKED!");
                            keyslotAuthentikeyHex= authentikeyHex;
                            
                            new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.transfer_card_ownership)
                                .setMessage(R.string.warning_initiate_transfer)
                                .setPositiveButton(R.string.transfer, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) { 
                                        if(DEBUG) Log.d(TAG, "TRANSFER DIALOG " + "YES  has been clicked!");
                                        sendTransferApduToCard();
                                    }
                                 })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) { 
                                        if(DEBUG) Log.d(TAG, "TRANSFER DIALOG " + "NO  has been clicked!");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast toast = Toast.makeText(getApplicationContext(), R.string.transfer_cancelled, Toast.LENGTH_SHORT);
                                                toast.show();
                                            }
                                        });
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                        }
                    }); // setOnClickListener
                  
                    keyInfoList = new ArrayList<HashMap<String,Object>>();
                    // iterate for each key
                    for (int k=0; k<nbKeyslot; k++){
                    
                        HashMap<String, Object> keyInfo= new HashMap<String, Object>();
                        keyInfoList.add(keyInfo);
                        keyInfo.put("keyNbr", k);
                        keyInfo.put("keyState", (int)keysState[k]);
                        
                        // call update functions depending on state
                        int keyState= (int) keysState[k];
                        if (keyState== STATE_UNINITIALIZED){
                            updateKeyslotInfoAfterReset(k);
                        } else if(keyState== STATE_SEALED) {
                            updateKeyslotInfoAfterSeal(k);
                        } else if (keyState== STATE_UNSEALED) {
                            updateKeyslotInfoAfterSeal(k);
                            updateKeyslotInfoAfterUnseal(k);
                        }
                    
                    }// enfdfor
                    
                    /******************************************************************
                    *       modify layout according to number of keyslots 
                    *******************************************************************/

                    keyslotsLayout= (LinearLayout) findViewById(R.id.group_keyslots);
                    LayoutParams params;
                    if(DEBUG) Log.d(TAG, "LAYOUT START!");
                    for (int k=0; k<nbKeyslot; k++){
                        final int kfinal= k;
                        
                        // recover keyInfo
                        HashMap<String, Object> keyInfo= keyInfoList.get(k);
                        int keyState= (int) keyInfo.get("keyState");
                        
                        if(DEBUG) Log.d(TAG, "LAYOUT Keyslot " + k + " START!");
                        // Keyslot container
                        LinearLayout keyslotLayout= new LinearLayout(mycontext);
                        keyslotLayout.setOrientation(LinearLayout.VERTICAL);
                        keyslotLayout.setBackgroundColor(BACKGROUND_COLORS[keyState]);
                        params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                        keyslotLayout.setLayoutParams(params);
                        keyslotLayout.setTag("keyslotLayout_"+k);
                        // add title text
                        TextView tvTitle= new TextView(mycontext);
                        String[] array_keyslot_states= getResources().getStringArray(R.array.array_keyslot_states);
                        String keyStateTxt= getResources().getString(R.string.key_nbr) + String.valueOf(k) + " - " + array_keyslot_states[keyState];
                        tvTitle.setText(keyStateTxt);
                        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        tvTitle.setTypeface(null, Typeface.BOLD);
                        tvTitle.setTag("tvTitle_"+k);
                        keyslotLayout.addView(tvTitle);
                        
                        // Asset type
                        LinearLayout assetTypeLayout= new LinearLayout(mycontext);
                        assetTypeLayout.setOrientation(LinearLayout.HORIZONTAL);
                        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        assetTypeLayout.setLayoutParams(params);
                        //
                        TextView tvAssetType= new TextView(mycontext);
                        tvAssetType.setText(R.string.asset_type);
                        assetTypeLayout.addView(tvAssetType);
                        //
                        TextView tvAssetTypeValue= new TextView(mycontext);
                        if (keyState == STATE_UNINITIALIZED){
                            tvAssetTypeValue.setText(R.string.uninitialized);
                        } else {
                            tvAssetTypeValue.setText((String) keyInfo.get("keyAssetTxt"));
                        }
                        tvAssetTypeValue.setTag("tvAssetTypeValue_"+k);
                        assetTypeLayout.addView(tvAssetTypeValue);
                        //
                        keyslotLayout.addView(assetTypeLayout);
                        
                        // add address layout
                        LinearLayout addressLayout= new LinearLayout(mycontext);
                        addressLayout.setOrientation(LinearLayout.HORIZONTAL);
                        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        addressLayout.setLayoutParams(params);
                        //
                        TextView tvAddress= new TextView(mycontext);
                        tvAddress.setText(R.string.address);
                        addressLayout.addView(tvAddress);
                        //
                        TextView tvAddressValue= new TextView(mycontext);
                        if (keyState == STATE_UNINITIALIZED){
                            tvAddressValue.setText(R.string.uninitialized);
                        } else {
                            tvAddressValue.setText((String) keyInfo.get("coinAddress"));
                        }
                        tvAddressValue.setTag("tvAddressValue_"+k);
                        addressLayout.addView(tvAddressValue);
                        //
                        keyslotLayout.addView(addressLayout);

                        // add balance layout
                        LinearLayout balanceLayout= new LinearLayout(mycontext);
                        balanceLayout.setOrientation(LinearLayout.HORIZONTAL);
                        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        balanceLayout.setLayoutParams(params);
                        //
                        TextView tvBalance= new TextView(mycontext);
                        tvBalance.setText(R.string.balance);
                        balanceLayout.addView(tvBalance);
                        //
                        TextView tvBalanceValue= new TextView(mycontext);
                        if (keyState == STATE_UNINITIALIZED){
                            tvBalanceValue.setText(R.string.uninitialized);
                        } else {
                            // balance info are only available after network thread has executed
                            tvBalanceValue.setText(R.string.wait);
                        }
                        tvBalanceValue.setTag("tvBalanceValue_"+k);
                        balanceLayout.addView(tvBalanceValue);
                        //
                        keyslotLayout.addView(balanceLayout);
                        
                        // token/NFT balance 
                        LinearLayout tokenBalanceLayout= new LinearLayout(mycontext);
                        tokenBalanceLayout.setOrientation(LinearLayout.HORIZONTAL);
                        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        tokenBalanceLayout.setLayoutParams(params);
                        tokenBalanceLayout.setTag("tokenBalanceLayout_"+k);
                        tokenBalanceLayout.setVisibility(View.GONE); // not visible by default
                        //
                        TextView tvTokenBalance= new TextView(mycontext);
                        tvTokenBalance.setText(R.string.token_balance);
                        tokenBalanceLayout.addView(tvTokenBalance);
                        //
                        TextView tvTokenBalanceValue= new TextView(mycontext);
                        if (keyState == STATE_UNINITIALIZED){
                            tvTokenBalanceValue.setText(R.string.uninitialized);
                        } else if (keyState != STATE_UNINITIALIZED){
                            // balance info are only available after network thread has executed
                            tvTokenBalanceValue.setText(R.string.wait);
                        }
                        tvTokenBalanceValue.setTag("tvTokenBalanceValue_"+k);
                        tokenBalanceLayout.addView(tvTokenBalanceValue);
                        // set visibility and add to layout
                        if (keyState != STATE_UNINITIALIZED){
                            boolean isToken= (boolean) keyInfo.get("isToken");
                            boolean isNFT= (boolean) keyInfo.get("isNFT");
                            if (isToken || isNFT){
                                tokenBalanceLayout.setVisibility(View.VISIBLE);
                            } 
                        }
                        keyslotLayout.addView(tokenBalanceLayout);
                        
                        // add 2 buttons
                        LinearLayout buttonsLayout= new LinearLayout(mycontext);
                        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
                        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        buttonsLayout.setLayoutParams(params);
                        // action button
                        String[] array_actions_from_state= getResources().getStringArray(R.array.array_actions_from_state);
                        String actionMsg= array_actions_from_state[keyState];
                        Button buttonAction= new Button(mycontext);
                        buttonAction.setText(actionMsg);
                        buttonAction.setTag(k);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isOwner){
                                    buttonAction.setEnabled(true); 
                                }else{
                                    buttonAction.setEnabled(false); 
                                }
                            }
                        });
                        buttonAction.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View view) {
                            if(DEBUG) Log.d(TAG, "BUTTON ACTION CLICKED!");                
                            keyslotAuthentikeyHex= authentikeyHex;
                            keyslotNbr= ((Integer) view.getTag()).intValue();
                            int keyslotState= (int) keyInfoList.get(keyslotNbr).get("keyState");
                            
                            if (keyslotState==0){ // uninitialized => seal
                                if(DEBUG) Log.d(TAG, "BUTTON ACTION CLICKED => SEALING KEYSLOT!");
                                // open as fragment
                                showSealFormDialog();
                            } else if (keyslotState==1){ // sealed => unseal
                                new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(R.string.unseal_question)
                                    .setMessage(R.string.unseal_warning)
                                    // Specifying a listener allows you to take an action before dismissing the dialog.
                                    // The dialog is automatically dismissed when a dialog button is clicked.
                                    .setPositiveButton(R.string.unseal_confirm, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) { 
                                            if(DEBUG) Log.d(TAG, "UNSEAL DIALOG " + "YES  has been clicked!");
                                            sendUnsealKeyslotApduToCard();
                                        }
                                     })
                                    // A null listener allows the button to dismiss the dialog and take no further action.
                                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) { 
                                            if(DEBUG) Log.d(TAG, "UNSEAL DIALOG " + "NO  has been clicked!");
                                            Toast toast = Toast.makeText(getApplicationContext(), R.string.unseal_cancel, Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                                
                            } else { // unsealed => reset
                                new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(R.string.reset_question)
                                    .setMessage(R.string.reset_warning)
                                    .setPositiveButton(R.string.reset_confirm, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) { 
                                            if(DEBUG) Log.d(TAG, "RESET DIALOG " + "YES  has been clicked!");
                                            sendResetKeyslotApduToCard();
                                        }
                                     })
                                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) { 
                                            if(DEBUG) Log.d(TAG, "RESET DIALOG " + "NO  has been clicked!");
                                            Toast toast = Toast.makeText(getApplicationContext(), R.string.reset_cancel, Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();                    
                            }
                            
                            
                          } // end onClick()
                        });
                        buttonsLayout.addView(buttonAction);
                        
                        // details button
                        if(DEBUG) Log.d(TAG, "LAYOUT Keyslot " + k + "add details button!");
                        Button buttonDetails= new Button(mycontext);
                        buttonDetails.setText(R.string.button_more_details);
                        buttonDetails.setTag("buttonDetails_"+k);
                        if (keyState == STATE_UNINITIALIZED){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    buttonDetails.setEnabled(false); 
                                }
                            });
                        }
                        buttonDetails.setOnClickListener(new View.OnClickListener() {
                          @Override
                          public void onClick(View view) {
                            if(DEBUG) Log.d(TAG, "BUTTON SHOWDETAILS CLICKED!");
                            // recover keyslotNbr
                            String tag= (String) view.getTag();
                            int keyslotNbr=Integer.parseInt(tag.replaceAll("[\\D]", "")); // only keep digits then parse
                            //using fragment
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("keyInfo", keyInfoList.get(keyslotNbr));
                            DialogFragment fragment = new ShowDetailsFragment();
                            fragment.setArguments(bundle);
                            fragment.show(getSupportFragmentManager(), "ShowDetailsFragment");
                          }
                        });
                        buttonsLayout.addView(buttonDetails);
                        
                        // add buttons to layout            
                        keyslotLayout.addView(buttonsLayout);


                        // add keyslotLayout to keyslotsLayout
                        //keyslotsLayout.addView(keyslotLayout);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                keyslotsLayout.addView(keyslotLayout);
                            }
                        });

                        if(DEBUG) Log.d(TAG, "LAYOUT Keyslot " + k + " FINISHED!");
                    }// end for loop
                    isLayoutReady= true;  
                    
                } catch (Exception e) {
                    if(DEBUG) Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }

            } // onConnected
            
            @Override
            public void onDisconnected() {
                if(DEBUG) Log.d(TAG, "Card disconnected");
                isConnected= false;
                isReconnectionFlag[0]= true; // detect future reconnection
                //Button buttonTransfer= (Button) findViewById(R.id.button_transfer);
                //Button buttonDetails= (Button) findViewById(R.id.button_auth);
                //TextView tvStatus= (TextView) findViewById(R.id.value_card_status);
                //TextView tvOwner= (TextView) findViewById(R.id.value_card_owner);
                
                // update card detected
                TextView tvConnected= (TextView) findViewById(R.id.value_card_connected);
            
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.card_disconnected, Toast.LENGTH_SHORT);
                        toast.show();
                        // update card connected view
                        tvConnected.setText(R.string.card_disconnected); 
                        tvConnected.setTextColor(COLOR_ORANGE);
                        // disable card-specific buttons
                        //buttonTransfer.setEnabled(false);
                        //buttonDetails.setEnabled(false); 
                        //tvStatus.setText("(no card detected)");
                        //tvOwner.setText("(no card detected)");
                    }
                });
            } // onDisconnected
        }); // cardManager
        cardManager.start();
    } // onCreate

    @Override
    public void onResume() {
        super.onResume();
        if(DEBUG) Log.d(TAG, "LIFECYCLE ONRESUME");
        if (nfcAdapter != null) {
          nfcAdapter.enableReaderMode(this, this.cardManager, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(DEBUG) Log.d(TAG, "LIFECYCLE ONPAUSE");
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }
    
    // DEBUG
    @Override
    public void onStop() {
        super.onStop();
        if(DEBUG) Log.d(TAG, "LIFECYCLE ONSTOP ");
    }
    
    //DEBUG
    @Override
    public void onStart() {
        super.onStart();
        if(DEBUG) Log.d(TAG, "LIFECYCLE ONSTART ");
    }
    
    //DEBUG
    @Override
    public void onRestart() {
        super.onRestart();
        if(DEBUG) Log.d(TAG, "LIFECYCLE ONRESTART ");
    }
    
    // DEBUG
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(DEBUG) Log.d(TAG, "LIFECYCLE ONDESTROY ");
    }
    
    // FRAGMENTS
    public void showSealFormDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new SealFormDialogFragment();
        dialog.show(getSupportFragmentManager(), "SealFormDialogFragment");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, int requestCode, int resultCode, Intent intent) {
        // TODO check resultCode
        // User touched the dialog's positive button
        if(DEBUG) Log.d(TAG, "showSealFormDialog onDialogPositiveClick");
        
        switch (requestCode){
            case 1: // sealDialog
                //String asset= intent.getStringExtra("asset");
                sealKeyslotAsset= intent.getIntExtra("assetInt", 0);
                sealKeyslotSlip44= intent.getByteArrayExtra("slip44");
                sealKeyslotContractByteTLV= intent.getByteArrayExtra("contractByteTLV");
                sealKeyslotTokenidByteTLV= intent.getByteArrayExtra("tokenidByteTLV");
                sealKeyslotEntropyUser= intent.getByteArrayExtra("entropyUser");
                // send to card
                // in case of failure, seal is postposed to next card connected
                sendSealKeyslotApduToCard();
                break;
            case 2: // settingDialog
                appFiat= intent.getStringExtra("appFiat");
                if (appFiat.equals("(none)")){
                    useFiat= false;
                } else {
                    useFiat= true;
                }
                appLanguage= intent.getStringExtra("appLanguage");
                appDarkModeEnabled= intent.getBooleanExtra("appDarkModeEnabled", false);
                // TODO: move logic from settingDialog to here (prefs edit...)?
            default:
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog, int requestCode, int resultCode) {
        // User touched the dialog's negative button
        if(DEBUG) Log.d(TAG, "onDialogNegativeClick: ABCD");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.seal_cancel, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
    
    // Actions from fragments
    // send seal keyslot APDU to card
    public boolean sendSealKeyslotApduToCard(){
        if(DEBUG) Log.d(TAG, "DEBUGSEAL: A SEAL ACTION HAS BEEN REQUESTED!");
        if(DEBUG) Log.d(TAG, "DEBUGSEAL: sealKeyslotSlip44= " + parser.toHexString(sealKeyslotSlip44));
        
        // send seal command to card
        if(DEBUG) Log.d(TAG, "SEAL SENDING APDU TO CARD");
        try{
            
            if (!isConnected){
                throw new Exception("No card connected!");
            }
            
            // check that authentikey match actual card authentikey
            if (! keyslotAuthentikeyHex.equals(cmdSet.getAuthentikeyHex())){
                throw new Exception("Authentikeys do not match!");
            }
            // send apdu commands
            APDUResponse rapduSeal= cmdSet.satodimeSealKey(keyslotNbr, sealKeyslotEntropyUser);
            if (rapduSeal.isOK()){
                int RFU1= 0;
                int RFU2= 0;
                APDUResponse rapduSetInfo= cmdSet.satodimeSetKeyslotStatusPart0(keyslotNbr, RFU1, RFU2, sealKeyslotAsset, sealKeyslotSlip44, sealKeyslotContractByteTLV, sealKeyslotTokenidByteTLV);
                if (!rapduSetInfo.isOK()){
                    throw new Exception("RAPDU rapduSetInfo error: "+ rapduSetInfo.toHexString());
                }
            } else {
                if (isOwner && rapduSeal.getSw()==0x9C51){ // 0x9C51 is SW_INCORRECT_UNLOCK_CODE
                    // This could be caused if card ownership has changed outside of the app
                    updateLayoutOwnershipWarning();
                }                
                throw new Exception("RAPDU rapduSeal error: "+ Integer.toHexString(rapduSeal.getSw()));
            }    
            pendingAction= PENDING_ACTION_NONE;
            // update keyInfo and layout
            updateKeyslotInfoAfterSeal(keyslotNbr);
            updateLayoutAfterKeyslotChange(keyslotNbr);
            
            // toast?
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.seal_success, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
            return true;
        } catch (Exception e) {
            if(DEBUG) Log.e(TAG, "Failed to seal keyslot"+ e);
            e.printStackTrace();
            // if seal failed, will attempt again at next card connection
            pendingAction= PENDING_ACTION_SEAL;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.seal_fail, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
            return false;
        }
    }
    
    //send unseal keyslot APDU command to card
    public boolean sendUnsealKeyslotApduToCard(){
        
        // send seal command to card
        if(DEBUG) Log.d(TAG, "UNSEAL SENDING APDU TO CARD");
        try{
            
            if (!isConnected){
                throw new Exception("No card connected!");
            }
            
             // check that authentikey match actual card authentikey
            if (! keyslotAuthentikeyHex.equals(cmdSet.getAuthentikeyHex())){
                throw new Exception("Authentikeys do not match!");
            }
            // send apdu command
            APDUResponse rapduUnseal= cmdSet.satodimeUnsealKey(keyslotNbr);
            if (rapduUnseal.isOK()){
                pendingAction= PENDING_ACTION_NONE;
                // update keyInfo & layout
                updateKeyslotInfoAfterUnseal(keyslotNbr);
                updateLayoutAfterKeyslotChange(keyslotNbr);

                // toast?
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(DEBUG) Log.d(TAG, "Update BUTTON SEALED => UNSEALED");
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.unseal_success, Toast.LENGTH_SHORT);
                        toast.show();
                        // update action button
                        String[] array_actions_from_state= getResources().getStringArray(R.array.array_actions_from_state);
                        String actionMsgUpdated= array_actions_from_state[2];
                        Button buttonAction= (Button) keyslotsLayout.findViewWithTag(keyslotNbr);
                        buttonAction.setText(actionMsgUpdated);
                    }
                });
                return true;
            } else {
                if (isOwner && rapduUnseal.getSw()==0x9C51){ // 0x9C51 is SW_INCORRECT_UNLOCK_CODE
                    // This could be caused if card ownership has changed outside of the app
                    updateLayoutOwnershipWarning();
                } 
                throw new Exception("RAPDU error: "+ Integer.toHexString(rapduUnseal.getSw()));
            }
        } catch (Exception e) {
            if(DEBUG) Log.e(TAG, "Failed to unseal keyslot: " + e);
            e.printStackTrace();
            // if seal failed, will attempt again at next card connection
            pendingAction= PENDING_ACTION_UNSEAL;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.unseal_fail, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
            return false;
        }
        
    }
    
    //send unseal keyslot APDU command to card
    public boolean sendResetKeyslotApduToCard(){
        
        // send seal command to card
        if(DEBUG) Log.d(TAG, "RESET SENDING APDU TO CARD");
        try{
            
            if (!isConnected){
                throw new Exception("No card connected!");
            }
            
            // check that authentikey match actual card authentikey
            if (! keyslotAuthentikeyHex.equals(cmdSet.getAuthentikeyHex())){
                throw new Exception("Authentikeys do not match!");
            }
            APDUResponse rapduReset= cmdSet.satodimeResetKey(keyslotNbr);
            if (rapduReset.isOK()){
                pendingAction= PENDING_ACTION_NONE;
                // update keyInfo & layout
                updateKeyslotInfoAfterReset(keyslotNbr);
                updateLayoutAfterKeyslotChange(keyslotNbr);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.reset_success, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                return true;
            } else {
                if (isOwner && rapduReset.getSw()==0x9C51){ // 0x9C51 is SW_INCORRECT_UNLOCK_CODE
                    // This could be caused if card ownership has changed outside of the app
                    updateLayoutOwnershipWarning();
                } 
                throw new Exception("RAPDU error: "+ Integer.toHexString(rapduReset.getSw()));
            }
        } catch (Exception e) {
            if(DEBUG) Log.e(TAG, "Failed to reset keyslot! " + e);
            e.printStackTrace();
            pendingAction= PENDING_ACTION_RESET;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.reset_fail, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
            return false;
        }
        
    }
    
    //send transfer card APDU command to card
    public boolean sendTransferApduToCard(){
        // send seal command to card
        if(DEBUG) Log.d(TAG, "TRANSFER SENDING APDU TO CARD");
        try{
            
            if (!isConnected){
                throw new Exception("No card connected!");
            }
            
            // check that authentikey match actual card authentikey
            if (! keyslotAuthentikeyHex.equals(cmdSet.getAuthentikeyHex())){
                throw new Exception("Authentikeys do not match!");
            }
        
            APDUResponse rapduTransfer= cmdSet.satodimeInitiateOwnershipTransfer();
            if (rapduTransfer.isOK()){
                pendingAction= PENDING_ACTION_NONE;  
                // remove unlockSecretHex from SharedPreferences
                prefs.edit().remove(keyslotAuthentikeyHex).apply();
                runOnUiThread(new Runnable() {
                    public void run() {
                        Button buttonTransfer= (Button) findViewById(R.id.button_transfer);
                        TextView tvOwner= (TextView) findViewById(R.id.value_card_owner);
                        buttonTransfer.setEnabled(false); 
                        tvOwner.setText(R.string.card_ownership_value_ko);
                        tvOwner.setTextColor(COLOR_ORANGE);  
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.transfer_init_success, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                return true;
            } else {
                if (isOwner && rapduTransfer.getSw()==0x9C51){ // 0x9C51 is SW_INCORRECT_UNLOCK_CODE
                    // This could be caused if card ownership has changed outside of the app
                    updateLayoutOwnershipWarning();
                } 
                throw new Exception("RAPDU error: "+ Integer.toHexString(rapduTransfer.getSw()));
            }
        } catch (Exception e) {
            if(DEBUG) Log.e(TAG, "Failed to transfer card ownership! " + e);
            e.printStackTrace();
            pendingAction= PENDING_ACTION_TRANSFER;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(getApplicationContext(), R.string.transfer_init_fail, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
            return false;
        }
        
    }
    
    // send satodimeGetKeyslotStatus() command and gather keyInfo  
    public void updateKeyslotInfoAfterSeal(int keyslotNbr){
        if(DEBUG) Log.d(TAG, "updateKeyslotInfoAfterSeal: keyslotNbr= " + keyslotNbr);
        
        if (!isConnected){
            return;
        }
        
        satodimeStatus= cmdSet.getSatodimeStatus();
        keysState= satodimeStatus.getKeysState();
        int keyState= keysState[keyslotNbr];
        if (keyState == STATE_UNINITIALIZED ){
          return; 
        }
        
        HashMap<String, Object> keyInfo= new HashMap<String, Object>();
        keyInfo.put("keyState", keyState);
        keyInfo.put("keyNbr", keyslotNbr);
        
        // get keyslots status
        if(DEBUG) Log.d(TAG, "Keyslot " +  keyslotNbr + " get status...");
        APDUResponse rapduKeyslotStatus= cmdSet.satodimeGetKeyslotStatus(keyslotNbr);
        if(DEBUG) Log.d(TAG, "Keyslot " + keyslotNbr + " status received!");
        SatodimeKeyslotStatus keyslotStatus= new SatodimeKeyslotStatus(rapduKeyslotStatus);
        if(DEBUG) Log.d(TAG, "Keyslot " + keyslotNbr + " status:" + keyslotStatus.toString());
        keyInfo.put("keyType", (int)keyslotStatus.getKeyType());
        // asset type (coin/token/nft)
        int keyAsset=  (int)keyslotStatus.getKeyAsset();
        String keyAssetTxt= (String)MAP_ASSET_BY_CODE.get(keyAsset);
        keyInfo.put("keyAsset", keyAsset); // convert byte to int, otherwise it is boxed to Byte value (instead of Integer)
        keyInfo.put("keyAssetTxt", keyAssetTxt);
        boolean isToken= TOKENSET.contains(keyAssetTxt);
        boolean isNFT= NFTSET.contains(keyAssetTxt);
        keyInfo.put("isToken", isToken); // convert byte to int, otherwise it is boxed to Byte value (instead of Integer)
        keyInfo.put("isNFT", isNFT);
        // slip44
        byte[] keySlip44= keyslotStatus.getKeySlip44();
        ByteBuffer wrapped = ByteBuffer.wrap(keySlip44); // big-endian by default
        int keySlip44Int= wrapped.getInt();
        boolean isTestnet= ((keySlip44[0] & 0x80) == 0x00); // testnet if first bit is 0
        keyInfo.put("keySlip44", keySlip44);
        keyInfo.put("keySlip44Int", keySlip44Int);
        keyInfo.put("keySlip44Hex", parser.toHexString(keySlip44) );
        keyInfo.put("isTestnet", isTestnet);
        // token/nft info
        final String keyContractHex= "0x"+parser.toHexString(keyslotStatus.getKeyContract());
        final String keyTokenIdHex= parser.toHexString(keyslotStatus.getKeyTokenId());
        final String keyTokenIdDec;
        if (keyTokenIdHex.length()>=2){
            keyTokenIdDec= (new BigInteger(keyTokenIdHex, 16)).toString();
        } else{
            keyTokenIdDec="";
        }
        keyInfo.put("keyContract", keyslotStatus.getKeyContract());
        keyInfo.put("keyContractHex", keyContractHex);
        keyInfo.put("keyTokenId", keyslotStatus.getKeyTokenId());
        keyInfo.put("keyTokenIdHex", keyTokenIdHex);
        keyInfo.put("keyTokenIdDec", keyTokenIdDec);
        keyInfo.put("keyData", keyslotStatus.getKeyData());
        keyInfo.put("keyDataHex", parser.toHexString(keyslotStatus.getKeyData()) );

        // get pubkey
        if(DEBUG) Log.d(TAG, "Keyslot " + keyslotNbr + " get pubkey...");
        APDUResponse rapduPubkey= cmdSet.satodimeGetPubkey(keyslotNbr);
        byte[] pubkeyBytes= parser.parseSatodimeGetPubkey(rapduPubkey);
        String pubkeyHex= parser.toHexString(pubkeyBytes);
        if(DEBUG) Log.d(TAG, "Keyslot " + keyslotNbr + " pubkey: " + pubkeyHex);
        keyInfo.put("pubkeyBytes", pubkeyBytes);
        keyInfo.put("pubkeyHex", pubkeyHex);

        // build coin object
        BaseCoin coin= getCoin(keySlip44Int, isTestnet, APIKEYS);
        String coinSymbol= coin.coin_symbol;
        String coinDisplayName= coin.display_name;
        keyInfo.put("coinSymbol", coinSymbol);
        keyInfo.put("coinDisplayName", coinDisplayName);
        if(DEBUG) Log.d(TAG, "coinSymbol: " + coinSymbol); 
        if(DEBUG) Log.d(TAG, "coinDisplayName: " + coinDisplayName); 
        
        // get address
        String coinAddress= coin.pubToAddress(pubkeyBytes);
        keyInfo.put("coinAddress", coinAddress);
        if(DEBUG) Log.d(TAG, "coinAddress: " + coinAddress); 
        String coinAddressWeburl= coin.getAddressWeburl(coinAddress);
        keyInfo.put("coinAddressWeburl", coinAddressWeburl);
        
        // add keyInfo to keyInfoList
        // TODO: thread safe?
        keyInfoList.get(keyslotNbr).putAll(keyInfo); 
        
        /* network requests in thread */
        if(DEBUG) Log.d(TAG, "BEFORE THREAD"); 
        
        // get balance
        // using threading for network operations...
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, Object> keyInfo2= new HashMap<String, Object>();
                try {
                    double coinBalance;
                    String coinBalanceTxt;
                    try{
                        coinBalance= coin.getBalance(coinAddress);
                        coinBalanceTxt= coinBalance + " " + coinDisplayName + " (" + coinSymbol + ")";
                        keyInfo2.put("coinBalance", coinBalance);
                        //keyInfo2.put("coinBalanceTxt", coinBalanceTxt);
                        
                        // get exchange rate with preferred fiat
                        if (useFiat){
                            double coinRate= coin.get_exchange_rate_between(appFiat);
                            if (coinRate>=0){
                                double coinValue= coinBalance * coinRate;
                                coinBalanceTxt+="\t (~ " + coinValue + " " + appFiat + ")";
                            } else {
                                coinBalanceTxt+="\t (~ ? " + appFiat + ")";
                            }
                        }
                        keyInfo2.put("coinBalanceTxt", coinBalanceTxt);
                        
                    } catch (Exception e){
                        coinBalance= -1;
                        coinBalanceTxt= getResources().getString(R.string.coin_balance_fail); //"(failed to fetch coin balance)";
                        keyInfo2.put("coinBalance", coinBalance);
                        keyInfo2.put("coinBalanceTxt", coinBalanceTxt);
                    } 
                    
                    // token/nft
                    double tokenBalance;
                    String tokenBalanceTxt;
                    if (isToken || isNFT){
                        try{
                            long balanceLong= coin.getTokenBalance(coinAddress, keyContractHex);
                            HashMap<String, String> tokenInfo= coin.getTokenInfo(keyContractHex);
                            String decimalsTxt= tokenInfo.get("decimals");
                            int decimals= Integer.parseInt(decimalsTxt);
                            tokenBalance= (double)(balanceLong)/(Math.pow(10, decimals));
                            keyInfo2.put("tokenBalance", tokenBalance);
                            // txt
                            String tokenDisplayName= tokenInfo.get("name");
                            String tokenSymbol= tokenInfo.get("symbol");
                            tokenBalanceTxt= tokenBalance + " " + tokenDisplayName + " (" + tokenSymbol + ")";
                            // get exchange rate with preferred fiat
                            if (useFiat && !isNFT){
                                double tokenRate= coin.get_token_exchange_rate_between(keyContractHex, appFiat);
                                if (tokenRate>=0){
                                    double tokenValue= tokenBalance * tokenRate;
                                    tokenBalanceTxt+="\t (~ " + tokenValue + " " + appFiat + ")";
                                } else {
                                    tokenBalanceTxt+="\t (~ ? " + appFiat + ")";
                                }
                            }
                            keyInfo2.put("tokenBalanceTxt", tokenBalanceTxt);
                            // TODO: update ui 
                        } catch (Exception e){
                            tokenBalance= -1;
                            tokenBalanceTxt= getResources().getString(R.string.token_balance_fail);
                            keyInfo2.put("tokenBalance", tokenBalance);
                            keyInfo2.put("tokenBalanceTxt", tokenBalanceTxt);
                        }
                    }else{
                        tokenBalanceTxt="(N/A)";
                    }
                    
                    // add keyInfo to keyInfoList
                    // TODO: thread safety?
                    keyInfoList.get(keyslotNbr).putAll(keyInfo2); 
                    
                    // update ui?
                    final String coinBalanceTxtFinal= coinBalanceTxt;
                    final String tokenBalanceTxtFinal= tokenBalanceTxt;
                    try{
                        
                        // The layout is not always ready 
                        while (!isLayoutReady){
                            if(DEBUG) Log.d(TAG, "WAITING LAYOUT for keyslot " + keyslotNbr); 
                            Thread.sleep(500);
                        }
                        TextView tvBalanceValue= (TextView)  keyslotsLayout.findViewWithTag("tvBalanceValue_"+keyslotNbr);
                        TextView tvTokenBalanceValue= (TextView)  keyslotsLayout.findViewWithTag("tvTokenBalanceValue_"+keyslotNbr);
                        tvBalanceValue.post(new Runnable() {
                            @Override
                            public void run() {
                                if(DEBUG) Log.d(TAG, "UPDATING LAYOUT for keyslot " + keyslotNbr); 
                                tvBalanceValue.setText(coinBalanceTxtFinal);
                                tvTokenBalanceValue.setText(tokenBalanceTxtFinal);
                                if(DEBUG) Log.d(TAG, "UPDATED LAYOUT for keyslot " + keyslotNbr); 
                            }
                        });
                    } catch (Exception e) {
                        if(DEBUG) Log.e(TAG, "Failed to update UI: " + e);
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if(DEBUG) Log.e(TAG, "Failed to update keyInfo in thread: " + e);
                    e.printStackTrace();
                }
            }
        }).start();
        
        if(DEBUG) Log.d(TAG, "AFTER THREAD"); 
        
        // NFT thread 
        if (isNFT){
            if(DEBUG) Log.d(TAG, "BEFORE NFT THREAD"); 
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // JSONObject nftInfo;
                        HashMap<String, Object> keyInfoNft= new HashMap<String, Object>();
                        keyInfoNft.put("nftName", "");
                        keyInfoNft.put("nftDescription", "");
                        keyInfoNft.put("nftImageUrl", "");
                        keyInfoNft.put("nftBitmap", null);
                        
                        try{
                            HashMap<String, String> nftInfo= coin.getNftInfo(keyContractHex, keyTokenIdDec);
                            keyInfoNft.put("nftName", nftInfo.get("nftName"));
                            keyInfoNft.put("nftDescription", nftInfo.get("nftDescription"));
                            keyInfoNft.put("nftImageUrl", nftInfo.get("nftImageUrl"));
                        } catch (Exception e){
                            if(DEBUG) Log.e(TAG, "Failed to fetch nftInfoMap in thread: " + e);
                        } 
                        
                        // get data from network
                        try{
                            String nftImageUrl= (String) keyInfoNft.get("nftImageUrl");
                            URL url = new URL(nftImageUrl);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setDoInput(true);
                            connection.connect();
                            InputStream input = connection.getInputStream();
                            Bitmap nftBitmap = BitmapFactory.decodeStream(input);
                            keyInfoNft.put("nftBitmap", nftBitmap);
                        } catch (Exception e){
                            if(DEBUG) Log.e(TAG, "Failed to fetch nft bitmap in thread: " + e);
                        } 
                        
                        // add keyInfo to keyInfoList
                        // TODO: thread safety?
                        keyInfoList.get(keyslotNbr).putAll(keyInfoNft); 
                        
                    } catch (Exception e) {
                        if(DEBUG) Log.e(TAG, "Failed to update keyInfo in thread: " + e);
                        e.printStackTrace();
                    }
                }
            }).start();
            if(DEBUG) Log.d(TAG, "AFTER NFT THREAD"); 
        }
        
        return;
    } // end 
    
    // send satodimeGetKeyslotStatus() command and gather keyInfo  
    // After 'unseal' command, private key info becomes available
    public void updateKeyslotInfoAfterUnseal(int keyslotNbr){
        if(DEBUG) Log.d(TAG, "updateKeyslotInfoAfterUnseal: keyslotNbr= " + keyslotNbr);
        
        if (!isConnected){
            return;
        }
        
        satodimeStatus= cmdSet.getSatodimeStatus();
        keysState= satodimeStatus.getKeysState();
        int keyState= keysState[keyslotNbr];
        
        if (keyState != STATE_UNSEALED){
            // TODO: should not happen, but nevertheless, do something?
            return;
        }
        
        HashMap<String, Object> keyInfo= new HashMap<String, Object>();
        keyInfo.put("keyState", keyState);
        keyInfo.put("keyNbr", keyslotNbr);

        // recover slip44 from memory
        byte[] keySlip44= (byte[]) keyInfoList.get(keyslotNbr).get("keySlip44");
        ByteBuffer wrapped = ByteBuffer.wrap(keySlip44); // big-endian by default
        int keySlip44Int= wrapped.getInt();
        boolean isTestnet= ((keySlip44[0] & 0x80) == 0x00); // testnet if first bit is 0

        // get coin object from javacryptotools
        BaseCoin coin= getCoin(keySlip44Int, isTestnet, APIKEYS);
        
        // get private key
        if(DEBUG) Log.d(TAG, "Privkey recovery: START");
        try{
            APDUResponse rapduPrivkey= cmdSet.satodimeGetPrivkey(keyslotNbr);
            if (rapduPrivkey.isOK()){
                HashMap<String, byte[]> privkeyInfo=  parser.parseSatodimeGetPrivkey(rapduPrivkey);
                // privkey
                byte[] privkeyBytes= (byte[]) privkeyInfo.get("privkey");
                String privkeyHex= "0x" + parser.toHexString(privkeyBytes);
                keyInfo.put("privkeyHex", privkeyHex);
                // wif
                String privkeyWif= coin.encodePrivkey(privkeyBytes);
                keyInfo.put("privkeyWif", privkeyWif);
                // entropy
                byte[] entropyBytes= (byte[]) privkeyInfo.get("entropy");
                String entropyHex= parser.toHexString(entropyBytes);
                keyInfo.put("entropyHex", entropyHex);
            } else{
                if (isOwner && rapduPrivkey.getSw()==0x9C51){ // 0x9C51 is SW_INCORRECT_UNLOCK_CODE
                    // This could be caused if card ownership has changed outside of the app
                    updateLayoutOwnershipWarning();
                } 
                throw new Exception("RAPDU error: "+ Integer.toHexString(rapduPrivkey.getSw()));
            }
        } catch (Exception e) {
            if(DEBUG) Log.e(TAG, "Exception: unable to recover privkey: " + e);
            String privkey_fail= getResources().getString(R.string.privkey_fail);
            keyInfo.put("privkeyHex",privkey_fail);
            keyInfo.put("privkeyWif", privkey_fail);
            keyInfo.put("entropyHex", privkey_fail);
        }
        // add keyInfo to keyInfoList
        // TODO: thread safe?
        keyInfoList.get(keyslotNbr).putAll(keyInfo);
        
        return;
    } // end 
    
    public void updateKeyslotInfoAfterReset(int keyslotNbr){
        if(DEBUG) Log.d(TAG, "updateKeyslotInfoAfterReset: keyslotNbr= " + keyslotNbr);
        
        if (!isConnected){
            return;
        }
        
        satodimeStatus= cmdSet.getSatodimeStatus();
        if(DEBUG) Log.d(TAG, "Satodime status:" + satodimeStatus.toString());
        keysState= satodimeStatus.getKeysState();
        int keyState= keysState[keyslotNbr];
        
        if (keyState != STATE_UNINITIALIZED){
            // TODO: should not happen, but nevertheless, do something?
            return;
        }
        
        HashMap<String, Object> keyInfo= new HashMap<String, Object>();
        keyInfo.put("keyState", keyState);
        keyInfo.put("keyNbr", keyslotNbr);
        
        // add keyInfo to keyInfoList
        // TODO: thread safe?
        keyInfoList.get(keyslotNbr).putAll(keyInfo);
        
        return;
    }
    
    // update layout  after keyslot status change
    public void updateLayoutAfterKeyslotChange(int keyslotNbr){
        
        // update data from keyInfo
        HashMap<String, Object> keyInfo= keyInfoList.get(keyslotNbr);
        int keyState= (int) keyInfo.get("keyState");
        String[] array_keyslot_states= getResources().getStringArray(R.array.array_keyslot_states);
        String keyStateTxt= getResources().getString(R.string.key_nbr) + String.valueOf(keyslotNbr) + " - " + array_keyslot_states[keyState];
        // address
        final String address;
        if (keyState == STATE_UNINITIALIZED){
             address= getResources().getString(R.string.uninitialized);           
        } else {
            address= (String) keyInfo.get("coinAddress"); // keyInfo.getOrDefault("pubkeyHex", "(unknown address)");
        }
        // balance 
        final String keyAssetTxt;
        final String coinBalanceTxt;
        final boolean isTokenOrNFT;
        final String tokenBalanceTxt;
        if (keyState == STATE_UNINITIALIZED){
            keyAssetTxt= getResources().getString(R.string.uninitialized);  
            coinBalanceTxt= getResources().getString(R.string.uninitialized);
            tokenBalanceTxt= getResources().getString(R.string.uninitialized);  
            isTokenOrNFT= false;
        }else{
            keyAssetTxt= (String) keyInfo.get("keyAssetTxt");
            coinBalanceTxt= (String) keyInfo.get("coinBalanceTxt");

            // token balance
            boolean isToken= (boolean) keyInfo.get("isToken");
            boolean isNFT= (boolean) keyInfo.get("isNFT");
            isTokenOrNFT= (isToken || isNFT);
            if (isTokenOrNFT){
                tokenBalanceTxt= (String) keyInfo.get("tokenBalanceTxt");
            } else {
                tokenBalanceTxt= getResources().getString(R.string.not_applicable);
            }
        }
        // button
        String[] array_actions_from_state= getResources().getStringArray(R.array.array_actions_from_state);
        String actionMsgUpdated= array_actions_from_state[keyState];
        
        // get widgets
        LinearLayout keyslotLayout= (LinearLayout) keyslotsLayout.findViewWithTag("keyslotLayout_"+keyslotNbr);
        LinearLayout tokenBalanceLayout= (LinearLayout)  keyslotsLayout.findViewWithTag("tokenBalanceLayout_"+keyslotNbr);
        TextView tvTitle= (TextView)  keyslotsLayout.findViewWithTag("tvTitle_"+keyslotNbr);
        TextView tvAssetType= (TextView)  keyslotsLayout.findViewWithTag("tvAssetTypeValue_"+keyslotNbr);
        TextView tvAddressValue= (TextView)  keyslotsLayout.findViewWithTag("tvAddressValue_"+keyslotNbr);
        TextView tvBalanceValue= (TextView)  keyslotsLayout.findViewWithTag("tvBalanceValue_"+keyslotNbr);
        TextView tvTokenBalanceValue= (TextView)  keyslotsLayout.findViewWithTag("tvTokenBalanceValue_"+keyslotNbr);
        Button buttonAction= (Button) keyslotsLayout.findViewWithTag(keyslotNbr);
        Button buttonDetails= (Button) keyslotsLayout.findViewWithTag("buttonDetails_"+keyslotNbr); 
        
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                keyslotLayout.setBackgroundColor(BACKGROUND_COLORS[keyState]);
                tvTitle.setText(keyStateTxt);
                tvAssetType.setText(keyAssetTxt);
                tvAddressValue.setText(address);
                tvBalanceValue.setText(coinBalanceTxt);
                buttonAction.setText(actionMsgUpdated); 
                // if uninitialized, disable show details
                if (keyState == STATE_UNINITIALIZED){
                    buttonDetails.setEnabled(false);
                } else{
                    buttonDetails.setEnabled(true);
                }
                // if token/NFT, show layout & balance
                if (isTokenOrNFT){
                    tvTokenBalanceValue.setText(tokenBalanceTxt);
                    tokenBalanceLayout.setVisibility(View.VISIBLE);
                } else{
                    tokenBalanceLayout.setVisibility(View.GONE);
                }
            }
        });
        
    }
    
    /**
    *  If ownership is force-changed outside of  the app, the app may wrongfully thinks it has ownership of the card.
    *  Forcing ownership is possible using using a usb card reader that allows to bypass NFC check (unlock_code is only enforced on the NFC interface).
    * If so, sending sensitive APDU (seal-unseal-reset-transfer...) will return 0x9C51 code.
    * A message is shown to the user to let him know that he may not be the owner anymore
    * Prompt the user to unpair card (remove old unlock_secret) once we are sure that ownership has been transfered outside of the app.
    */
    public void updateLayoutOwnershipWarning(){
        
        // show error message
        TextView tvOwner= (TextView) findViewById(R.id.value_card_owner);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvOwner.setText(R.string.card_ownership_value_unknown);
                tvOwner.setTextColor(COLOR_ORANGE);
            }
        });
        
        // after limit is reached, propose to remove ownership
        ownership_error_counter++;
        if (ownership_error_counter>=MAX_OWNERSHIP_ERROR){
            ownership_error_counter=0;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    
                    new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.reset_ownership_question)
                                .setMessage(R.string.reset_ownership_warning)
                                .setPositiveButton(R.string.reset_ownership_confirmation, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) { 
                                        if(DEBUG) Log.d(TAG, "RESET OWNERSHIP DIALOG " + "YES  has been clicked!");
                                        if(DEBUG) Log.d(TAG, "DEBUGUNLOCK setupDone: START");
                                        
                                        // remove unlockSecretHex from SharedPreferences
                                        prefs.edit().remove(authentikeyHex).apply();
                                        Button buttonTransfer= (Button) findViewById(R.id.button_transfer);
                                        //TextView tvOwner= (TextView) findViewById(R.id.value_card_owner);
                                        buttonTransfer.setEnabled(false); 
                                        tvOwner.setText(R.string.card_ownership_value_ko);
                                        tvOwner.setTextColor(COLOR_ORANGE);                                       
                                        isOwner= false;
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) { 
                                        if(DEBUG) Log.d(TAG, "OWNERSHIP APPROVAL DIALOG " + "NO  has been clicked!");
                                        Toast toast = Toast.makeText(getApplicationContext(), R.string.reset_ownership_rejected, Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                } // run
            });
        }
    }
    
    // build a coin object from javacryptotools library
    public BaseCoin getCoin(int keySlip44Int, boolean isTestnet, HashMap<String, String> apikeys){
        BaseCoin coin;
        int keySlip44IntAbs= keySlip44Int | 0x80000000; // switch first bit (ignore testnet or mainnet)
        switch(keySlip44IntAbs){
            case BTC:
                coin= new Bitcoin(isTestnet, apikeys);
                break;
            case LTC:
                coin= new Litecoin(isTestnet, apikeys);
                break;
            case BCH:
                coin= new BitcoinCash(isTestnet, apikeys);
                break;
            case ETH:
                coin= new org.satochip.javacryptotools.Ethereum(isTestnet, apikeys);
                break;
            default:
                coin= new UnsupportedCoin(isTestnet, apikeys);
                break;
        }
        if (DEBUG) {
            coin.setLoggerLevel("info"); } 
        else{
            coin.setLoggerLevel("warning"); }
        return coin;
    }
    
}
