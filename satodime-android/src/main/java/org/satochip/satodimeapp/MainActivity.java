package org.satochip.satodimeapp;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;
import org.satochip.io.*;
import org.satochip.io.CardChannel;
import org.satochip.io.CardListener;
import org.satochip.android.NFCCardManager;

import static org.satochip.client.Constants.*;

import org.satochip.client.SatochipCommandSet;
import org.satochip.client.ApplicationStatus;
import org.satochip.client.SatodimeStatus;
import org.satochip.client.SatodimeKeyslotStatus;
import org.satochip.client.SatochipParser;
//import org.satochip.client.Util;

import org.satochip.javacryptotools.*;
import org.satochip.satodimeapp.adapter.MyCardsAdapter;
import org.satochip.satodimeapp.model.Card;
import org.satochip.satodimeapp.ui.activity.CardInfoActivity;
import org.satochip.satodimeapp.ui.activity.KeySlotDetailsActivity;
import org.satochip.satodimeapp.ui.activity.SettignsActivity;

import static org.satochip.javacryptotools.coins.Constants.*;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ybq.android.spinkit.SpinKitView;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements SealFormDialogFragment.SealFormDialogListener {

    private static final boolean DEBUG= BuildConfig.DEBUG;
    private static final String TAG= "SATODIME";
    private static final int REQUEST_CODE_SEAL_KEYSLOT_FORM= 666;
    private static final int COLOR_GREEN= 0xff90EE90; //0xffb2ff59; // #0xff90EE90
    private static final int COLOR_ORANGE= 0xFFFFD580;
    private static final int COLOR_RED= 0xFFFF0000;
    private static final int[] BACKGROUND_COLORS= {R.color.green, R.color.RED, R.color.gold_light};
    private static final int PENDING_ACTION_NONE= -1;
    private static final int PENDING_ACTION_RESET= 0;
    private static final int PENDING_ACTION_SEAL= 1;
    private static final int PENDING_ACTION_UNSEAL= 2;
    private static final int PENDING_ACTION_TRANSFER= 3;
    // private static String[] PENDING_ACTION_TXT; //= getResources().getStringArray(R.array.array_pending_actions); //{"Reset keyslot", "Seal Keyslot", "Unseal keyslot", "Transfer card ownership"};
    // private static String[] ARRAY_KEYSLOT_STATES; //= getResources().getStringArray(R.array.array_keyslot_states); 
    // private static String[] ACTIONS_FROM_STATE; //= getResources().getStringArray(R.array.array_actions_from_state); //{"Seal key!", "Unseal key!", "Reset key!"};

    // green: 81c784, b2fab4, b2ff59
    // orange: fbc02d, FFD580

    private NfcAdapter nfcAdapter;
    private NFCCardManager cardManager;
    private boolean isConnected= false;
    private SatochipCommandSet cmdSet= null;
    private SatochipParser parser= null;
    private SatodimeStatus satodimeStatus= null;
    private byte[] keysState= null;
    private int nbKeyslot= 0;
    private List<HashMap<String, Object>> keyInfoList= null;

    // flags & vars used when action is pending
    private String keyslotAuthentikeyHex= "";
    private int keyslotNbr= -1;
    private int pendingAction= PENDING_ACTION_NONE;
    // used when sealing a slot...
    private int sealKeyslotAsset= -1;
    private byte[] sealKeyslotSlip44= null;
    private byte[] sealKeyslotContractByteTLV= null;
    private byte[] sealKeyslotTokenidByteTLV= null;
    private byte[] sealKeyslotEntropyUser= null;

    // detect ownership & issues
    byte[] authentikey= null;
    String authentikeyHex= null;
    private boolean isOwner= false;
    private static final int MAX_OWNERSHIP_ERROR= 5;
    private int ownership_error_counter= 0;

    // settings button 
    private Button buttonSettings= null;

    // keyslot layout root
    private LinearLayout keyslotsLayout= null;
    private boolean isLayoutReady= false;

    // API KEYS
    private HashMap<String, String> APIKEYS= null;

    // preferences & settings
    private SharedPreferences prefs= null;
    private String appLanguage;
    private String appFiat= "(none)";
    public static boolean useFiat= false;
    private boolean appDarkModeEnabled;

    private DrawerLayout drawer;
    private ImageView menuBtn, headerImg;

    private RecyclerView lst_menu;
    private CardView toolBar;
    private RecyclerView recyclerView;
    private ArrayList<Card> cardArrayList;
    private MyCardsAdapter myCardsAdapter;


    TextView key0, key1, key2;
    RelativeLayout card0, card1, card2;
    TextView tokenBalance0, tokenBalance1, tokenBalance2;
    LinearLayout tokenBalanceLayout0, tokenBalanceLayout1, tokenBalanceLayout2;
    TextView assestType0, assestType1, assestType2;
    TextView cardName0, cardName1, cardName2;
    TextView balance0, balance1, balance2;
    TextView unit0, unit1, unit2;
    ImageView cardStatusImg0, cardStatusImg1, cardStatusImg2;
    TextView cardStatus0, cardStatus1, cardStatus2;
    ImageView cardImg0, cardImg1, cardImg2;
    ImageView nextBtn0, nextBtn1, nextBtn2;
    TextView cardAddress0, cardAddress1, cardAddress2;

    LinearLayout cardConnectedLayout;
    CardView cardNotConnectedLayout;

    CardView firstCard, secondCard, thirdCard;

    SpinKitView progressBar;
    ImageView connLogo, cardAuthticityImg;
    TextView connText,noCardMainText;
    Activity myactivity= this;
    Context mycontext= this;
    boolean[] isReconnectionFlag= new boolean[1];


    public static boolean isLanguageChanged=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(DEBUG) Log.d(TAG, "LIFECYCLE ONCREATE");

        isReconnectionFlag[0]= false; // detect reconnection

        // load API keys from resources
        APIKEYS= new HashMap<String, String>();
        APIKEYS.put("API_KEY_ETHERSCAN", getString(R.string.API_KEY_ETHERSCAN));
        APIKEYS.put("API_KEY_ETHPLORER", getString(R.string.API_KEY_ETHPLORER));
        APIKEYS.put("API_KEY_BSCSCAN", getString(R.string.API_KEY_BSCSCAN));

        // persistent memory & locale & fiat
        prefs= getSharedPreferences("satodime", MODE_PRIVATE);
        appLanguage= prefs.getString("appLanguage", Locale.getDefault().getLanguage());
        Log.d("applang",appLanguage);
        Locale myLocale= new Locale(appLanguage);
        Locale.setDefault(myLocale);
        Resources resource= getResources();
        Configuration config= resource.getConfiguration();
        config.locale= myLocale;
        resource.updateConfiguration(config, resource.getDisplayMetrics());
        appFiat= prefs.getString("appFiat", "(none)");
        if (!appFiat.equals("(none)")) {
            useFiat= true;
        }

        // load some string arrays 
        // PENDING_ACTION_TXT= getResources().getStringArray(R.array.array_pending_actions); //{"Reset keyslot", "Seal Keyslot", "Unseal keyslot", "Transfer card ownership"};
        // ARRAY_KEYSLOT_STATES= getResources().getStringArray(R.array.array_keyslot_states); 
        // ACTIONS_FROM_STATE= getResources().getStringArray(R.array.array_actions_from_state); //{"Seal key!", "Unseal key!", "Reset key!"};

        if(DEBUG) Log.d(TAG, "Starting SatoDime Application: OnCreate");
        setContentView(R.layout.activity_main);

        initAllViews();

        clickListners();

//        myCardsAdapter= new MyCardsAdapter(cardArrayList, MainActivity.this);
//        RecyclerView.LayoutManager mLayoutManager1= new LinearLayoutManager(MainActivity.this);
//        recyclerView.setLayoutManager(mLayoutManager1);
//        recyclerView.setAdapter(myCardsAdapter);
//        myCardsAdapter.notifyDataSetChanged();
        cardListners();

    } // onCreate

    public void cardListners(){
        // NFC
        nfcAdapter= NfcAdapter.getDefaultAdapter(this);
        cardManager= new NFCCardManager();
        if(DEBUG) Log.d(TAG, "Created cardManager object");
        cardManager.setCardListener(new CardListener() {
            @Override
            public void onConnected(CardChannel cardChannel) {
                try {
//                 Applet-specific code
                    if(DEBUG) Log.d(TAG, "onConnected!");
                    isConnected= true;
                    isLayoutReady= false;


                    // satodime object
                    cmdSet= new SatochipCommandSet(cardChannel);
                    if(DEBUG) {
                        cmdSet.setLoggerLevel("info");
                        Log.d(TAG, "Created a SatochipCommandSet object");
                    } else {
                        cmdSet.setLoggerLevel("warning");
                    }
                    parser= cmdSet.getParser();

                    // First thing to do is selecting the applet on the card.
                    APDUResponse rapdu= cmdSet.cardSelect("satodime");
                    if(DEBUG) Log.d(TAG, "Applet selected:" + rapdu.toHexString());

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast toast= Toast.makeText(getApplicationContext(), R.string.card_connected, Toast.LENGTH_SHORT);
                            toast.show();
                            cardConnectedLayout.setVisibility(View.VISIBLE);
                            cardNotConnectedLayout.setVisibility(View.GONE);
                            progressBar.setVisibility(View.VISIBLE);
                            connLogo.setImageResource(R.drawable.ic_card_connected);


                            if (appLanguage.equals("fr")) {
                                connText.setText("Carte connectée");
                            } else {
                                connText.setText("Card connected");
                            }
                        }
                    });

                    // get card status
                    APDUResponse rapdu2= cmdSet.cardGetStatus();
                    Log.d("cardAuthneticiy", rapdu2.isOK() + "");
                    ApplicationStatus cardStatus= cmdSet.getApplicationStatus();
                    if(DEBUG) Log.d(TAG, "Card status:" + cardStatus.toString());
                    // check if setup done
                    if (!cardStatus.isSetupDone()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
                                ViewGroup viewGroup= findViewById(android.R.id.content);
                                View dialogView= LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_transfer_card, viewGroup, false);

                                TextView transferBtn= dialogView.findViewById(R.id.transfer_btn);
                                TextView cancelBtn= dialogView.findViewById(R.id.cancel_btn);
                                TextView warningText= dialogView.findViewById(R.id.card_warning_text);


                                builder.setView(dialogView);
                                final AlertDialog alertDialog= builder.create();

                                transferBtn.setText(getString(R.string.accept_transfer_confirmation));
                                warningText.setText(getString(R.string.accept_transfer_warning));
                                transferBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if(DEBUG)
                                            Log.d(TAG, "OWNERSHIP APPROVAL DIALOG " + "YES  has been clicked!");
                                        SecureRandom random= new SecureRandom();
                                        byte pin_tries0= (byte) 5;
                                        byte[] pin0= new byte[8];
                                        random.nextBytes(pin0);
                                        APDUResponse rapduSetup= cmdSet.cardSetup(pin_tries0, pin0);
                                        if (rapduSetup.isOK()) {
                                            // save unlockSecret in SharedPreferences
                                            authentikey= cmdSet.getAuthentikey();
                                            authentikeyHex= parser.toHexString(authentikey);
                                            if(DEBUG)
                                                Log.d(TAG, "Satodime authentikey: " + authentikeyHex);
                                            byte[] unlockSecret= cmdSet.getSatodimeUnlockSecret();
                                            String unlockSecretHex= parser.toHexString(unlockSecret);
                                            prefs.edit().putString(authentikeyHex, unlockSecretHex).apply();
                                            Toast toast= Toast.makeText(getApplicationContext(), R.string.transfer_success, Toast.LENGTH_SHORT);
                                            toast.show();
                                            alertDialog.dismiss();
                                        } else {
                                            if(DEBUG)
                                                Log.e(TAG, "Error: setupDone: " + rapduSetup.toHexString());
                                            Toast toast= Toast.makeText(getApplicationContext(), R.string.transfer_fail, Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    }
                                });
                                cancelBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        alertDialog.dismiss();
                                        if(DEBUG)
                                            Log.d(TAG, "OWNERSHIP APPROVAL DIALOG " + "NO  has been clicked!");
                                        Toast toast= Toast.makeText(getApplicationContext(), R.string.transfer_rejected, Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                });
                                alertDialog.show();

                            }
                        });


                    }

                    // get authentikey
                    authentikey= cmdSet.getAuthentikey();
                    authentikeyHex= parser.toHexString(authentikey);
                    if(DEBUG) Log.d(TAG, "Satodime authentikey: " + authentikeyHex);

                    // check if unlock_secret is available for the authentikey
                    if (prefs.contains(authentikeyHex)) {
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
                    if (pendingAction != PENDING_ACTION_NONE) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {


                                final AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
                                ViewGroup viewGroup= findViewById(android.R.id.content);
                                View dialogView= LayoutInflater.from(MainActivity.this).inflate(R.layout.dialoge_pending_request, viewGroup, false);

                                TextView okBtn= dialogView.findViewById(R.id.ok_btn);
                                TextView cancelBtn= dialogView.findViewById(R.id.cancel_btn);
                                TextView message= dialogView.findViewById(R.id.message);

                                String[] array_pending_actions= getResources().getStringArray(R.array.array_pending_actions);
                                String msg= getResources().getString(R.string.request_pending_msg) + " " + array_pending_actions[pendingAction] + "\n";
                                if (pendingAction != PENDING_ACTION_TRANSFER) {
                                    msg += getResources().getString(R.string.keyslot_nbr) + keyslotNbr + "\n";
                                }
                                msg += getResources().getString(R.string.proceed_with_request);

                                message.setText(msg);

                                builder.setView(dialogView);
                                final AlertDialog alertDialog= builder.create();

                                okBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        alertDialog.dismiss();

                                        if(DEBUG)
                                            Log.d(TAG, "PENDING REQUEST DIALOG " + "YES  has been clicked!");
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
                                });
                                cancelBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        alertDialog.dismiss();
                                        if(DEBUG)
                                            Log.d(TAG, "PENDING REQUEST DIALOG " + "NO  has been clicked!");
                                        pendingAction= PENDING_ACTION_NONE;
                                        Toast toast= Toast.makeText(getApplicationContext(), R.string.request_rejected, Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                });

                                alertDialog.show();
                            } //run
                        }); // thread
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cardConnectedLayout.setVisibility(View.VISIBLE);
                            cardNotConnectedLayout.setVisibility(View.GONE);
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    });

                    // check card authenticity
                    String[] authResults= cmdSet.cardVerifyAuthenticity();
                    for (int index= 0; index < authResults.length; index++) {
                        if(DEBUG) Log.d(TAG, "DEBUGAUTH : " + authResults[index]);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (authResults[0].equals("OK")) {
                                cardAuthticityImg.setImageResource(R.drawable.ic_auth_batch_ok);
                            } else {
                                cardAuthticityImg.setImageResource(R.drawable.ic_auth_batch_no);
                            }
                        }
                    });

                    // update layout header info (details & transfert card button)
                    isOwner= satodimeStatus.isOwner();
                    Log.d("cardStatus", isOwner + "");
                    keyInfoList= new ArrayList<HashMap<String, Object>>();
                    // iterate for each key
                    for (int k= 0; k < nbKeyslot; k++) {

                        HashMap<String, Object> keyInfo= new HashMap<String, Object>();
                        keyInfoList.add(keyInfo);
                        keyInfo.put("keyNbr", k);
                        keyInfo.put("keyState", (int) keysState[k]);

                        // call update functions depending on state
                        int keyState= (int) keysState[k];
                        if (keyState== STATE_UNINITIALIZED) {
                            updateKeyslotInfoAfterReset(k);
                        } else if (keyState== STATE_SEALED) {
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
                    if(DEBUG) Log.d(TAG, "LAYOUT START!");
                    for (int k= 0; k < nbKeyslot; k++) {
                        Log.d("testSAto", k + "");
                        // recover keyInfo
                        HashMap<String, Object> keyInfo= keyInfoList.get(k);
                        int keyState= (int) keyInfo.get("keyState");

                        Log.d("testKeyInfo", keyInfo.toString());

                        // Keyslot container
                        String[] array_keyslot_states= getResources().getStringArray(R.array.array_keyslot_states);
                        int finalK= k;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (finalK== 0) {
                                    String blockChain= (String) keyInfoList.get(0).get("coinDisplayName");
                                    if (blockChain != null) {
                                        switch (blockChain) {
                                            case "Bitcoin":
                                            case "Bitcoin Testnet":{
                                                cardImg0.setImageResource(R.drawable.ic_bitcoin);
                                                break;
                                            }
                                            case "Ethereum":
                                            case "Ethereum Testnet":{
                                                cardImg0.setImageResource(R.drawable.ic_etherium);
                                                break;
                                            }
                                            case "Litecoin":
                                            case "Litecoin Testnet":{
                                                cardImg0.setImageResource(R.drawable.ic_ltc);
                                                break;
                                            }
                                            case "Bitcoin Cash":
                                            case "Bitcoin Cash Testnet":{
                                                cardImg0.setImageResource(R.drawable.ic_bch);
                                                break;
                                            }
                                        }
                                    }
                                    firstCard.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                    if (array_keyslot_states[keyState].equals(getResources().getString(R.string.sealed))) {
                                        key0.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.green));
                                        card0.setBackground(MainActivity.this.getDrawable(R.drawable.card_outline_green));
                                        cardStatus0.setText(getString(R.string.sealed));
                                        cardStatus0.setTextColor(getResources().getColor(R.color.green));
                                        cardStatusImg0.setImageResource(R.drawable.ic_lock);
                                    }
                                    if (array_keyslot_states[keyState].equals(getResources().getString(R.string.unsealed))) {
                                        key0.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.RED));
                                        card0.setBackground(MainActivity.this.getDrawable(R.drawable.card_ouline_red));
                                        cardStatus0.setText(getString(R.string.unsealed));
                                        cardStatus0.setTextColor(getResources().getColor(R.color.RED));
                                        cardStatusImg0.setImageResource(R.drawable.ic_unlock);
                                    }
                                    if (array_keyslot_states[keyState].equals(getResources().getString(R.string.lbl_uninitalized))) {
                                        key0.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.gold_light));
                                        card0.setBackground(MainActivity.this.getDrawable(R.drawable.card_outline_gold));
                                        cardStatus0.setText(getString(R.string.lbl_uninitalized));
                                        cardStatus0.setTextColor(getResources().getColor(R.color.grey));
                                        cardStatusImg0.setImageResource(R.drawable.ic_hourglass);
                                        cardImg0.setImageResource(R.drawable.ic_hourglass);
                                    }
                                    key0.setText(getResources().getString(R.string.key_nbr) + " " + String.valueOf(finalK));

                                    //asset type
                                    if (keyState== STATE_UNINITIALIZED) {
                                        assestType0.setText(R.string.uninitialized);
                                    } else {
                                        assestType0.setText((String) keyInfo.get("keyAssetTxt"));
                                    }

                                    //address
                                    if (keyState== STATE_UNINITIALIZED) {
                                        cardAddress0.setText(R.string.uninitialized);
                                    } else {
                                        cardAddress0.setText((String) keyInfo.get("coinAddress"));
                                    }

                                    //balance
                                    if (keyState== STATE_UNINITIALIZED) {
                                        balance0.setText(R.string.uninitialized);
                                    } else {
                                        // balance info are only available after network thread has executed
                                        balance0.setText(R.string.wait);
                                    }

                                    //token balance
                                    if (keyState== STATE_UNINITIALIZED) {
                                        tokenBalance0.setText(R.string.uninitialized);
                                    } else if (keyState != STATE_UNINITIALIZED) {
                                        // balance info are only available after network thread has executed
                                        tokenBalance0.setText(R.string.wait);
                                    }
                                    // set visibility and add to layout
                                    if (keyState != STATE_UNINITIALIZED) {
                                        boolean isToken= (boolean) keyInfo.get("isToken");
                                        boolean isNFT= (boolean) keyInfo.get("isNFT");
                                        if (isToken || isNFT) {
                                            tokenBalanceLayout0.setVisibility(View.VISIBLE);
                                        }
                                    }

                                    cardStatusImg0.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (isOwner) {
                                                if(DEBUG) Log.d(TAG, "BUTTON ACTION CLICKED!");
                                                keyslotAuthentikeyHex= authentikeyHex;
                                                keyslotNbr= 0;
                                                int keyslotState= (int) keyInfoList.get(keyslotNbr).get("keyState");

                                                if (keyslotState== 0) { // uninitialized=> seal
                                                    if(DEBUG)
                                                        Log.d(TAG, "BUTTON ACTION CLICKED=> SEALING KEYSLOT!");
                                                    // open as fragment
                                                    showSealFormDialog();
                                                } else if (keyslotState== 1) { // sealed=> unseal

                                                    final AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
                                                    ViewGroup viewGroup= findViewById(android.R.id.content);
                                                    View dialogView= LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_unseal, viewGroup, false);

                                                    TextView unSealBtn= dialogView.findViewById(R.id.transfer_btn);
                                                    TextView cancelBtn= dialogView.findViewById(R.id.cancel_btn);


                                                    builder.setView(dialogView);
                                                    final AlertDialog alertDialog= builder.create();

                                                    unSealBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            alertDialog.dismiss();
                                                            sendUnsealKeyslotApduToCard();
                                                        }
                                                    });
                                                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            alertDialog.dismiss();
                                                        }
                                                    });

                                                    alertDialog.show();
                                                } else { // unsealed=> reset
                                                    Log.d("dialgoge", "ok");
                                                    final AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
                                                    ViewGroup viewGroup= findViewById(android.R.id.content);
                                                    View dialogView= LayoutInflater.from(MainActivity.this).inflate(R.layout.dialoge_reset, viewGroup, false);

                                                    TextView unSealBtn= dialogView.findViewById(R.id.transfer_btn);
                                                    TextView cancelBtn= dialogView.findViewById(R.id.cancel_btn);

                                                    builder.setView(dialogView);
                                                    final AlertDialog alertDialog= builder.create();
                                                    unSealBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            alertDialog.dismiss();
                                                            sendResetKeyslotApduToCard();
                                                        }
                                                    });
                                                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            alertDialog.dismiss();
                                                            Toast toast= Toast.makeText(getApplicationContext(), R.string.reset_cancel, Toast.LENGTH_SHORT);
                                                            toast.show();
                                                        }
                                                    });
                                                    alertDialog.show();
                                                }

                                            }

                                        } // end onClick()
                                    });

                                    //details button
                                    nextBtn0.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (keyState != STATE_UNINITIALIZED) {
                                                if(DEBUG)
                                                    Log.d(TAG, "BUTTON SHOWDETAILS CLICKED!");
                                                //using fragment
                                                Bundle bundle= new Bundle();
                                                bundle.putSerializable("keyInfo", keyInfoList.get(0));
                                                bundle.putInt("position", 0);
                                                Intent intent= new Intent(MainActivity.this, KeySlotDetailsActivity.class);
                                                intent.putExtras(bundle);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                                }
                                else if (finalK== 1) {
                                    String blockChain= (String) keyInfoList.get(1).get("coinDisplayName");
                                    if (blockChain != null) {
                                        switch (blockChain) {
                                            case "Bitcoin":
                                            case "Bitcoin Testnet":{
                                                cardImg1.setImageResource(R.drawable.ic_bitcoin);
                                                break;
                                            }
                                            case "Ethereum":
                                            case "Ethereum Testnet":{
                                                cardImg1.setImageResource(R.drawable.ic_etherium);
                                                break;
                                            }
                                            case "Litecoin":
                                            case "Litecoin Testnet":{
                                                cardImg1.setImageResource(R.drawable.ic_ltc);
                                                break;
                                            }
                                            case "Bitcoin Cash":
                                            case "Bitcoin Cash Testnet":{
                                                cardImg1.setImageResource(R.drawable.ic_bch);
                                                break;
                                            }
                                        }
                                    }
                                    secondCard.setVisibility(View.VISIBLE);

                                    if (array_keyslot_states[keyState].equals(getResources().getString(R.string.sealed))) {
                                        key1.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.green));
                                        card1.setBackground(MainActivity.this.getDrawable(R.drawable.card_outline_green));
                                        cardStatus1.setText(getString(R.string.sealed));
                                        cardStatus1.setTextColor(getResources().getColor(R.color.green));
                                        cardStatusImg1.setImageResource(R.drawable.ic_lock);
                                    }
                                    if (array_keyslot_states[keyState].equals(getResources().getString(R.string.unsealed))) {
                                        key1.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.RED));
                                        card1.setBackground(MainActivity.this.getDrawable(R.drawable.card_ouline_red));
                                        cardStatus1.setText(getString(R.string.unsealed));
                                        cardStatus1.setTextColor(getResources().getColor(R.color.RED));
                                        cardStatusImg1.setImageResource(R.drawable.ic_unlock);

                                    }
                                    if (array_keyslot_states[keyState].equals(getResources().getString(R.string.lbl_uninitalized))) {
                                        key1.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.gold_light));
                                        card1.setBackground(MainActivity.this.getDrawable(R.drawable.card_outline_gold));
                                        cardStatus1.setText(getString(R.string.lbl_uninitalized));
                                        cardStatus1.setTextColor(getResources().getColor(R.color.grey));
                                        cardStatusImg1.setImageResource(R.drawable.ic_hourglass);
                                        cardImg1.setImageResource(R.drawable.ic_hourglass);
                                    }

                                    key1.setText(getResources().getString(R.string.key_nbr) + " " + String.valueOf(finalK));

                                    //asset type
                                    if (keyState== STATE_UNINITIALIZED) {
                                        assestType1.setText(R.string.uninitialized);
                                    } else {
                                        assestType1.setText((String) keyInfo.get("keyAssetTxt"));
                                    }

                                    //address
                                    if (keyState== STATE_UNINITIALIZED) {
                                        cardAddress1.setText(R.string.uninitialized);
                                    } else {
                                        cardAddress1.setText((String) keyInfo.get("coinAddress"));
                                    }

                                    //balance
                                    if (keyState== STATE_UNINITIALIZED) {
                                        balance1.setText(R.string.uninitialized);
                                    } else {
                                        // balance info are only available after network thread has executed
                                        balance1.setText(R.string.wait);
                                    }

                                    //token balance
                                    if (keyState== STATE_UNINITIALIZED) {
                                        tokenBalance1.setText(R.string.uninitialized);
                                    } else if (keyState != STATE_UNINITIALIZED) {
                                        // balance info are only available after network thread has executed
                                        tokenBalance1.setText(R.string.wait);
                                    }
                                    // set visibility and add to layout
                                    if (keyState != STATE_UNINITIALIZED) {
                                        boolean isToken= (boolean) keyInfo.get("isToken");
                                        boolean isNFT= (boolean) keyInfo.get("isNFT");
                                        if (isToken || isNFT) {
                                            tokenBalanceLayout1.setVisibility(View.VISIBLE);
                                        }
                                    }


                                    cardStatusImg1.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (isOwner) {
                                                if(DEBUG) Log.d(TAG, "BUTTON ACTION CLICKED!");
                                                keyslotAuthentikeyHex= authentikeyHex;
                                                keyslotNbr= 1;
                                                int keyslotState= (int) keyInfoList.get(keyslotNbr).get("keyState");

                                                if (keyslotState== 0) { // uninitialized=> seal
                                                    if(DEBUG)
                                                        Log.d(TAG, "BUTTON ACTION CLICKED=> SEALING KEYSLOT!");
                                                    // open as fragment
                                                    showSealFormDialog();
                                                } else if (keyslotState== 1) { // sealed=> unseal

                                                    final AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
                                                    ViewGroup viewGroup= findViewById(android.R.id.content);
                                                    View dialogView= LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_unseal, viewGroup, false);

                                                    TextView unSealBtn= dialogView.findViewById(R.id.transfer_btn);
                                                    TextView cancelBtn= dialogView.findViewById(R.id.cancel_btn);


                                                    builder.setView(dialogView);
                                                    final AlertDialog alertDialog= builder.create();

                                                    unSealBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            alertDialog.dismiss();
                                                            sendUnsealKeyslotApduToCard();
                                                        }
                                                    });
                                                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            alertDialog.dismiss();
                                                        }
                                                    });

                                                    alertDialog.show();
                                                } else { // unsealed=> reset
                                                    final AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
                                                    ViewGroup viewGroup= findViewById(android.R.id.content);
                                                    View dialogView= LayoutInflater.from(MainActivity.this).inflate(R.layout.dialoge_reset, viewGroup, false);

                                                    TextView unSealBtn= dialogView.findViewById(R.id.transfer_btn);
                                                    TextView cancelBtn= dialogView.findViewById(R.id.cancel_btn);


                                                    builder.setView(dialogView);
                                                    final AlertDialog alertDialog= builder.create();

                                                    unSealBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            alertDialog.dismiss();
                                                            sendResetKeyslotApduToCard();
                                                        }
                                                    });
                                                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            alertDialog.dismiss();
                                                            Toast toast= Toast.makeText(getApplicationContext(), R.string.reset_cancel, Toast.LENGTH_SHORT);
                                                            toast.show();
                                                        }
                                                    });
                                                    alertDialog.show();
                                                }

                                            }

                                        } // end onClick()
                                    });

                                    //details button
                                    nextBtn1.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (keyState != STATE_UNINITIALIZED) {
                                                if(DEBUG)
                                                    Log.d(TAG, "BUTTON SHOWDETAILS CLICKED!");
                                                //using fragment
                                                Bundle bundle= new Bundle();
                                                bundle.putSerializable("keyInfo", keyInfoList.get(1));
                                                bundle.putInt("position", 1);
                                                Intent intent= new Intent(MainActivity.this, KeySlotDetailsActivity.class);
                                                intent.putExtras(bundle);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                                }
                                else {
                                    String blockChain= (String) keyInfoList.get(2).get("coinDisplayName");
                                    if (blockChain != null) {
                                        Log.d("testNetICon", blockChain);
                                        switch (blockChain) {
                                            case "Bitcoin":
                                            case "Bitcoin Testnet":{
                                                cardImg2.setImageResource(R.drawable.ic_bitcoin);
                                                break;
                                            }
                                            case "Ethereum":
                                            case "Ethereum Testnet":{
                                                cardImg2.setImageResource(R.drawable.ic_etherium);
                                                break;
                                            }
                                            case "Litecoin":
                                            case "Litecoin Testnet":{
                                                cardImg2.setImageResource(R.drawable.ic_ltc);
                                                break;
                                            }
                                            case "Bitcoin Cash":
                                            case "Bitcoin Cash Testnet":{
                                                cardImg2.setImageResource(R.drawable.ic_bch);
                                                break;
                                            }
                                        }
                                    }
                                    thirdCard.setVisibility(View.VISIBLE);
                                    if (array_keyslot_states[keyState].equals(getResources().getString(R.string.sealed))) {
                                        key2.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.green));
                                        card2.setBackground(MainActivity.this.getDrawable(R.drawable.card_outline_green));
                                        cardStatus2.setText(getString(R.string.sealed));
                                        cardStatus2.setTextColor(getResources().getColor(R.color.green));
                                        cardStatusImg2.setImageResource(R.drawable.ic_lock);
                                    }
                                    if (array_keyslot_states[keyState].equals(getResources().getString(R.string.unsealed))) {
                                        key2.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.RED));
                                        card2.setBackground(MainActivity.this.getDrawable(R.drawable.card_ouline_red));
                                        cardStatus2.setText(getString(R.string.unsealed));
                                        cardStatus2.setTextColor(getResources().getColor(R.color.RED));
                                        cardStatusImg2.setImageResource(R.drawable.ic_unlock);
                                    }
                                    if (array_keyslot_states[keyState].equals(getResources().getString(R.string.lbl_uninitalized))) {
                                        key2.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.gold_light));
                                        card2.setBackground(MainActivity.this.getDrawable(R.drawable.card_outline_gold));
                                        cardStatus2.setText(getString(R.string.lbl_uninitalized));
                                        cardStatus2.setTextColor(getResources().getColor(R.color.grey));
                                        cardStatusImg2.setImageResource(R.drawable.ic_hourglass);
                                        cardImg2.setImageResource(R.drawable.ic_hourglass);
                                    }

                                    key2.setText(getResources().getString(R.string.key_nbr) + " " + String.valueOf(finalK));


                                    //asset type
                                    if (keyState== STATE_UNINITIALIZED) {
                                        assestType2.setText(R.string.uninitialized);
                                    } else {
                                        assestType2.setText((String) keyInfo.get("keyAssetTxt"));
                                    }

                                    //address
                                    if (keyState== STATE_UNINITIALIZED) {
                                        cardAddress2.setText(R.string.uninitialized);
                                    } else {
                                        cardAddress2.setText((String) keyInfo.get("coinAddress"));
                                    }

                                    //balance
                                    if (keyState== STATE_UNINITIALIZED) {
                                        balance2.setText(R.string.uninitialized);
                                    } else {
                                        // balance info are only available after network thread has executed
                                        balance2.setText(R.string.wait);
                                    }

                                    //token balance
                                    if (keyState== STATE_UNINITIALIZED) {
                                        tokenBalance2.setText(R.string.uninitialized);
                                    } else if (keyState != STATE_UNINITIALIZED) {
                                        // balance info are only available after network thread has executed
                                        tokenBalance2.setText(R.string.wait);
                                    }
                                    // set visibility and add to layout
                                    if (keyState != STATE_UNINITIALIZED) {
                                        boolean isToken= (boolean) keyInfo.get("isToken");
                                        boolean isNFT= (boolean) keyInfo.get("isNFT");
                                        if (isToken || isNFT) {
                                            tokenBalanceLayout2.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    cardStatusImg2.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (isOwner) {
                                                if(DEBUG) Log.d(TAG, "BUTTON ACTION CLICKED!");
                                                keyslotAuthentikeyHex= authentikeyHex;
                                                keyslotNbr= 2;
                                                int keyslotState= (int) keyInfoList.get(keyslotNbr).get("keyState");

                                                if (keyslotState== 0) { // uninitialized=> seal
                                                    if(DEBUG)
                                                        Log.d(TAG, "BUTTON ACTION CLICKED=> SEALING KEYSLOT!");
                                                    // open as fragment
                                                    showSealFormDialog();
                                                } else if (keyslotState== 1) { // sealed=> unseal

                                                    final AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
                                                    ViewGroup viewGroup= findViewById(android.R.id.content);
                                                    View dialogView= LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_unseal, viewGroup, false);

                                                    TextView unSealBtn= dialogView.findViewById(R.id.transfer_btn);
                                                    TextView cancelBtn= dialogView.findViewById(R.id.cancel_btn);


                                                    builder.setView(dialogView);
                                                    final AlertDialog alertDialog= builder.create();

                                                    unSealBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            alertDialog.dismiss();
                                                            sendUnsealKeyslotApduToCard();
                                                        }
                                                    });
                                                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            alertDialog.dismiss();
                                                        }
                                                    });

                                                    alertDialog.show();
                                                } else { // unsealed=> reset
                                                    final AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
                                                    ViewGroup viewGroup= findViewById(android.R.id.content);
                                                    View dialogView= LayoutInflater.from(MainActivity.this).inflate(R.layout.dialoge_reset, viewGroup, false);

                                                    TextView unSealBtn= dialogView.findViewById(R.id.transfer_btn);
                                                    TextView cancelBtn= dialogView.findViewById(R.id.cancel_btn);


                                                    builder.setView(dialogView);
                                                    final AlertDialog alertDialog= builder.create();

                                                    unSealBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            alertDialog.dismiss();
                                                            sendResetKeyslotApduToCard();
                                                        }
                                                    });
                                                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            alertDialog.dismiss();
                                                            Toast toast= Toast.makeText(getApplicationContext(), R.string.reset_cancel, Toast.LENGTH_SHORT);
                                                            toast.show();
                                                        }
                                                    });
                                                    alertDialog.show();
                                                }

                                            }

                                        } // end onClick()
                                    });
                                    //details button
                                    nextBtn2.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (keyState != STATE_UNINITIALIZED) {
                                                if(DEBUG)
                                                    Log.d(TAG, "BUTTON SHOWDETAILS CLICKED!");
                                                //using fragment
                                                Bundle bundle= new Bundle();
                                                bundle.putSerializable("keyInfo", keyInfoList.get(2));
                                                bundle.putInt("position", 2);
                                                Intent intent= new Intent(MainActivity.this, KeySlotDetailsActivity.class);
                                                intent.putExtras(bundle);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                                }

                                if(DEBUG) Log.d(TAG, "LAYOUT Keyslot " + finalK + " FINISHED!");
                            }
                        });
                    }// end for loop
                    isLayoutReady= true;
                } catch (Exception e) {
                    if(DEBUG) Log.e(TAG, e.getMessage());
                    Log.d("errorinCode", e.toString());
                }
            }
            // onConnected

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

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast= Toast.makeText(getApplicationContext(), R.string.card_disconnected, Toast.LENGTH_SHORT);
                        toast.show();
                        connLogo.setImageResource(R.drawable.ic_card_not_connected);

                        if (appLanguage.equals("fr")) {
                            connText.setText("Carte non connectée");
                        } else {
                            connText.setText("Card Not Connected");
                        }

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
    }

    private void clickListners() {
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!drawer.isDrawerOpen(Gravity.LEFT))
                    drawer.openDrawer(Gravity.LEFT);
                else
                    drawer.closeDrawer(Gravity.RIGHT);
            }
        });
    }

    private void initAllViews() {
        drawer= findViewById(R.id.drawer_layout);
        noCardMainText= findViewById(R.id.card_not_connected_tv_main);
        cardAuthticityImg= findViewById(R.id.card_authenticity_img);
        connLogo= findViewById(R.id.card_connected_view1_logo);
        connText= findViewById(R.id.card_connected_view1);
        progressBar= findViewById(R.id.spin_progress_bar);
        menuBtn= findViewById(R.id.menu_btn);
        lst_menu= findViewById(R.id.lst_menu);
        headerImg= findViewById(R.id.menu_back);
        recyclerView= findViewById(R.id.cards_list);
        toolBar= findViewById(R.id.toolbar);
        toolBar.setBackgroundResource(R.drawable.bottom_corer_round);
        cardArrayList= new ArrayList<>();

        cardConnectedLayout= findViewById(R.id.card_connected_layout);
        cardNotConnectedLayout= findViewById(R.id.car_not_connected_layout);
        key0= findViewById(R.id.key_number_0);
        key1= findViewById(R.id.key_number_1);
        key2= findViewById(R.id.key_number_2);
        card0= findViewById(R.id.card0);
        card1= findViewById(R.id.card1);
        card2= findViewById(R.id.card2);
        tokenBalance0= findViewById(R.id.token_balance_0);
        tokenBalance1= findViewById(R.id.token_balance_1);
        tokenBalance2= findViewById(R.id.token_balance_2);
        tokenBalanceLayout0= findViewById(R.id.token_balance_layout0);
        tokenBalanceLayout1= findViewById(R.id.token_balance_layout1);
        tokenBalanceLayout2= findViewById(R.id.token_balance_layout2);
        assestType0= findViewById(R.id.asset_type_0);
        assestType1= findViewById(R.id.asset_type_1);
        assestType2= findViewById(R.id.asset_type_2);
        cardName0= findViewById(R.id.card_name_0);
        cardName1= findViewById(R.id.card_name_1);
        cardName2= findViewById(R.id.card_name_2);
        balance0= findViewById(R.id.balance_0);
        balance1= findViewById(R.id.balance_1);
        balance2= findViewById(R.id.balance_2);
        unit0= findViewById(R.id.unit_0);
        unit1= findViewById(R.id.unit_1);
        unit2= findViewById(R.id.unit_2);
        cardStatusImg0= findViewById(R.id.card_status_image_0);
        cardStatusImg1= findViewById(R.id.card_status_image_1);
        cardStatusImg2= findViewById(R.id.card_status_image_2);
        cardStatus0= findViewById(R.id.card_status_0);
        cardStatus1= findViewById(R.id.card_status_1);
        cardStatus2= findViewById(R.id.card_status_2);
        cardImg0= findViewById(R.id.card_image_0);
        cardImg1= findViewById(R.id.card_image_1);
        cardImg2= findViewById(R.id.card_image_2);
        nextBtn0= findViewById(R.id.next_btn_0);
        nextBtn1= findViewById(R.id.next_btn_1);
        nextBtn2= findViewById(R.id.next_btn_2);
        cardAddress0= findViewById(R.id.card_address_0);
        cardAddress1= findViewById(R.id.card_address_1);
        cardAddress2= findViewById(R.id.card_address_2);
        firstCard= findViewById(R.id.first_card);
        secondCard= findViewById(R.id.second_card);
        thirdCard= findViewById(R.id.third_card);


    }

    @Override
    public void onResume() {
        super.onResume();

        lst_menu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        NavigationAdapter adapter= new NavigationAdapter(this);
        lst_menu.setHasFixedSize(true);
        lst_menu.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (Utils.isDark) {
            headerImg.setImageResource(R.drawable.splash_screen_white_logo);
        } else {
            headerImg.setImageResource(R.drawable.splash_screen_golden_logo);
        }
        if(DEBUG) Log.d(TAG, "LIFECYCLE ONRESUME");
        if (nfcAdapter != null) {
            nfcAdapter.enableReaderMode(this, this.cardManager, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
        }

        if(isLanguageChanged){
            isLanguageChanged=false;
            recreate();
        }

        appLanguage= prefs.getString("appLanguage", Locale.getDefault().getLanguage());
        if (appLanguage.equals("fr")) {
            noCardMainText.setText("Carte non connectée");
        } else {
            noCardMainText.setText("Card Not Connected");
        }

        appFiat= prefs.getString("appFiat", "(none)");
        if (!appFiat.equals("(none)")) {
            useFiat= true;
        }
    }
    @Override
    public void recreate() {
        Log.d("testActivityREs", "happended");
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        startActivity(getIntent());
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void closeDrawer() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
        final AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
        ViewGroup viewGroup= findViewById(android.R.id.content);
        View dialogView= LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_uninitialized, viewGroup, false);

        final boolean DEBUG= BuildConfig.DEBUG;
        final String TAG= "SEAL_FORM_FRAGMENT";
        final int RESULT_OK= 1;
        final int RESULT_CANCELLED= 0;
        final int REQUEST_CODE= 1;
        byte[] entropyUser;
        SHA256Digest sha256;


        entropyUser= new byte[32];
        sha256= new SHA256Digest();

        TextView transferBtn= dialogView.findViewById(R.id.transfer_btn);
        TextView cancelBtn= dialogView.findViewById(R.id.cancel_btn);
        Spinner spinnerAsset= dialogView.findViewById(R.id.spinner_coin_type);
        Spinner spinnerCoin= dialogView.findViewById(R.id.spinner_asset_type);
        CheckBox cbTestnet= dialogView.findViewById(R.id.checkbox_use_testnet);
        EditText entropyEt= dialogView.findViewById(R.id.edittext_entropy_input);
        LinearLayout llContract= (LinearLayout) dialogView.findViewById(R.id.group_contract);
        EditText etContract= (EditText) dialogView.findViewById(R.id.edittext_contract);
        LinearLayout llTokenid= (LinearLayout) dialogView.findViewById(R.id.group_tokenid);
        EditText etTokenid= (EditText) dialogView.findViewById(R.id.edittext_tokenid);
        TextView tvEntropyOut= (TextView) dialogView.findViewById(R.id.value_entropy_output);
        TextView tvNotif= (TextView) dialogView.findViewById(R.id.text_notification);


        final String[] array_coin= getResources().getStringArray(R.array.array_coin);
        final String[] array_asset_full= getResources().getStringArray(R.array.array_asset);
        final String[] array_asset_limited= getResources().getStringArray(R.array.array_asset_limited);

        ArrayAdapter<String> dataAdapter= new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_item, array_asset_full);
        dataAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerAsset.setAdapter(dataAdapter);

        ArrayAdapter<String> currencyAdapter= new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_item, array_coin);
        currencyAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerCoin.setAdapter(currencyAdapter);

        builder.setView(dialogView);
        final AlertDialog alertDialog= builder.create();
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

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
                    byte[] contractByte;
                    if (isToken || isNFT) {
                        if(DEBUG) Log.d(TAG, "SEAL contract (before): " + contract);
                        String regex= "^(0x)?[0-9a-fA-F]{40}$";
                        if (!contract.matches(regex)) {
                            throw new Exception(getResources().getString(R.string.exception_contract_format));
                        }
                        contract= contract.replaceFirst("^0x", ""); // remove "0x" if present
                        try {
                            contractByte= Hex.decode(contract);
                        } catch (Exception e) {
                            throw new Exception(getResources().getString(R.string.exception_contract_format_hex));
                        }
                        if (contractByte.length > 32) {
                            throw new Exception(getResources().getString(R.string.exception_contract_too_long));
                        }
                    } else {
                        contractByte= new byte[0]; // ignore contract value
                    }
                    byte[] contractByteTLV= new byte[34];
                    contractByteTLV[0]= (byte) 0;
                    contractByteTLV[1]= (byte) (contractByte.length);
                    System.arraycopy(contractByte, 0, contractByteTLV, 2, contractByte.length);
                    if(DEBUG)
                        Log.d(TAG, "SEAL contract (after)   : " + Hex.toHexString(contractByte));
                    // check tokenid: tokenid byte array should be [size(2b) | tokenid | 0-padding to 34b]
                    String tokenid= etTokenid.getText().toString();
                    byte[] tokenidByte;
                    if (isNFT) {
                        BigInteger tokenidBig= new BigInteger(tokenid); // tokenid is decimal-formated
                        String tokenidHexString= tokenidBig.toString(16); // convert to hex string
                        if (tokenidHexString.length() % 2== 1) {
                            tokenidHexString= "0" + tokenidHexString; // must have an even number of chars
                        }
                        tokenidByte= Hex.decode(tokenidHexString); // convert to bytes
                        if (tokenidByte.length > 32) {
                            throw new Exception(getResources().getString(R.string.exception_tokenid_too_long));
                        }
                    } else {
                        tokenidByte= new byte[0]; // ignore tokenID
                    }
                    byte[] tokenidByteTLV= new byte[34];
                    tokenidByteTLV[0]= (byte) 0;
                    tokenidByteTLV[1]= (byte) (tokenidByte.length);
                    System.arraycopy(tokenidByte, 0, tokenidByteTLV, 2, tokenidByte.length);
                    if(DEBUG) Log.d(TAG, "SEAL tokenid (before): " + tokenid);
                    if(DEBUG)
                        Log.d(TAG, "SEAL tokenid (after)   : " + Hex.toHexString(tokenidByte));


                    sealKeyslotAsset= assetInt;
                    sealKeyslotSlip44= slip44;
                    sealKeyslotContractByteTLV= contractByteTLV;
                    sealKeyslotTokenidByteTLV= tokenidByteTLV;
                    sealKeyslotEntropyUser= entropyUser;
                    // send to card
                    // in case of failure, seal is postposed to next card connected
                    sendSealKeyslotApduToCard();
                    alertDialog.dismiss();
                } catch (Exception e) {
                    if(DEBUG) Log.e(TAG, e.getMessage());
                    tvNotif.setText(getResources().getString(R.string.error) + e.getMessage());
                    tvNotif.setVisibility(View.VISIBLE);
                }
            }
        });

        entropyEt.addTextChangedListener(new TextWatcher() {
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
                byte[] entropyRawByte= s.toString().getBytes();
                if (entropyRawByte.length > 32) {
                    sha256.reset();
                    sha256.update(entropyRawByte, 0, entropyRawByte.length);
                    sha256.doFinal(entropyUser, 0);
                } else {
                    Arrays.fill(entropyUser, (byte) 0);
                    System.arraycopy(entropyRawByte, 0, entropyUser, 0, entropyRawByte.length);
                }
                String entropyHex= Hex.toHexString(entropyUser);
                // update layout
                tvEntropyOut.setText(entropyHex);
            }
        });

        spinnerCoin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                ArrayAdapter<String> adp;
                String coin= array_coin[position];
                if (SUPPORTS_TOKEN_SET.contains(coin)) {
                    adp= new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_item, array_asset_full);
                } else {
                    adp= new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_item, array_asset_limited);
                }
                adp.setDropDownViewResource(R.layout.spinner_item);
                spinnerAsset.setAdapter(adp);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinnerAsset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String asset= array_asset_full[position];
                if(DEBUG)
                    Log.d(TAG, "SealKeyslotActivity: setOnItemSelectedListener: " + asset);
                // update fields accordingly
                // show/hide contract field
                if (TOKENSET.contains(asset) || NFTSET.contains(asset)) {
                    if(DEBUG) Log.d(TAG, "SealKeyslotActivity: SHOW CONTRACT in thread: ");
                    llContract.setVisibility(View.VISIBLE);
                } else {
                    llContract.setVisibility(View.GONE);
                }
                // show/hide tokenid field
                if (NFTSET.contains(asset)) {
                    if(DEBUG) Log.d(TAG, "SealKeyslotActivity: SHOW TOKENID in thread: ");
                    llTokenid.setVisibility(View.VISIBLE);
                } else {
                    llTokenid.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        alertDialog.show();
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, int requestCode,
                                      int resultCode, Intent intent) {
        // TODO check resultCode
        // User touched the dialog's positive button
        if(DEBUG) Log.d(TAG, "showSealFormDialog onDialogPositiveClick");

        switch (requestCode) {
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
                if (appFiat.equals("(none)")) {
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
                Toast toast= Toast.makeText(getApplicationContext(), R.string.seal_cancel, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    // Actions from fragments
    // send seal keyslot APDU to card
    public boolean sendSealKeyslotApduToCard() {
        if(DEBUG) Log.d(TAG, "DEBUGSEAL: A SEAL ACTION HAS BEEN REQUESTED!");
        if(DEBUG)
            Log.d(TAG, "DEBUGSEAL: sealKeyslotSlip44= " + parser.toHexString(sealKeyslotSlip44));

        // send seal command to card
        if(DEBUG) Log.d(TAG, "SEAL SENDING APDU TO CARD");
        try {

            if (!isConnected) {
                throw new Exception("No card connected!");
            }

            // check that authentikey match actual card authentikey
            if (!keyslotAuthentikeyHex.equals(cmdSet.getAuthentikeyHex())) {
                throw new Exception("Authentikeys do not match!");
            }
            // send apdu commands
            APDUResponse rapduSeal= cmdSet.satodimeSealKey(keyslotNbr, sealKeyslotEntropyUser);
            if (rapduSeal.isOK()) {
                int RFU1= 0;
                int RFU2= 0;
                APDUResponse rapduSetInfo= cmdSet.satodimeSetKeyslotStatusPart0(keyslotNbr, RFU1, RFU2, sealKeyslotAsset, sealKeyslotSlip44, sealKeyslotContractByteTLV, sealKeyslotTokenidByteTLV);
                if (!rapduSetInfo.isOK()) {
                    throw new Exception("RAPDU rapduSetInfo error: " + rapduSetInfo.toHexString());
                }
            } else {
                if (isOwner && rapduSeal.getSw()== 0x9C51) { // 0x9C51 is SW_INCORRECT_UNLOCK_CODE
                    // This could be caused if card ownership has changed outside of the app
                    updateLayoutOwnershipWarning();
                }
                throw new Exception("RAPDU rapduSeal error: " + Integer.toHexString(rapduSeal.getSw()));
            }
            pendingAction= PENDING_ACTION_NONE;
            // update keyInfo and layout
            updateKeyslotInfoAfterSeal(keyslotNbr);
            updateLayoutAfterKeyslotChange(keyslotNbr);

            // toast?
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast= Toast.makeText(getApplicationContext(), R.string.seal_success, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
            return true;
        } catch (Exception e) {
            if(DEBUG) Log.e(TAG, "Failed to seal keyslot" + e);
            e.printStackTrace();
            // if seal failed, will attempt again at next card connection
            pendingAction= PENDING_ACTION_SEAL;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast= Toast.makeText(getApplicationContext(), R.string.seal_fail, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
            return false;
        }
    }

    //send unseal keyslot APDU command to card
    public boolean sendUnsealKeyslotApduToCard() {

        // send seal command to card
        if(DEBUG) Log.d(TAG, "UNSEAL SENDING APDU TO CARD");
        try {

            if (!isConnected) {
                throw new Exception("No card connected!");
            }

            // check that authentikey match actual card authentikey
            if (!keyslotAuthentikeyHex.equals(cmdSet.getAuthentikeyHex())) {
                throw new Exception("Authentikeys do not match!");
            }
            // send apdu command
            APDUResponse rapduUnseal= cmdSet.satodimeUnsealKey(keyslotNbr);
            if (rapduUnseal.isOK()) {
                pendingAction= PENDING_ACTION_NONE;
                // update keyInfo & layout
                updateKeyslotInfoAfterUnseal(keyslotNbr);
                updateLayoutAfterKeyslotChange(keyslotNbr);

                // toast?
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(DEBUG) Log.d(TAG, "Update BUTTON SEALED=> UNSEALED");
                        Toast toast= Toast.makeText(getApplicationContext(), R.string.unseal_success, Toast.LENGTH_SHORT);
                        toast.show();
                        // update action button
                        String[] array_actions_from_state= getResources().getStringArray(R.array.array_actions_from_state);
                        String actionMsgUpdated= array_actions_from_state[2];
                        Log.d("actionMsg", "ActionMessageUpdate");
//                        Button buttonAction= (Button) keyslotsLayout.findViewWithTag(keyslotNbr);
//                        buttonAction.setText(actionMsgUpdated);
                    }
                });
                return true;
            } else {
                if (isOwner && rapduUnseal.getSw()== 0x9C51) { // 0x9C51 is SW_INCORRECT_UNLOCK_CODE
                    // This could be caused if card ownership has changed outside of the app
                    updateLayoutOwnershipWarning();
                }
                throw new Exception("RAPDU error: " + Integer.toHexString(rapduUnseal.getSw()));
            }
        } catch (Exception e) {
            if(DEBUG) Log.e(TAG, "Failed to unseal keyslot: " + e);
            e.printStackTrace();
            // if seal failed, will attempt again at next card connection
            pendingAction= PENDING_ACTION_UNSEAL;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast= Toast.makeText(getApplicationContext(), R.string.unseal_fail, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
            return false;
        }

    }

    //send unseal keyslot APDU command to card
    public boolean sendResetKeyslotApduToCard() {

        // send seal command to card
        if(DEBUG) Log.d(TAG, "RESET SENDING APDU TO CARD");
        try {

            if (!isConnected) {
                throw new Exception("No card connected!");
            }

            // check that authentikey match actual card authentikey
            if (!keyslotAuthentikeyHex.equals(cmdSet.getAuthentikeyHex())) {
                throw new Exception("Authentikeys do not match!");
            }
            APDUResponse rapduReset= cmdSet.satodimeResetKey(keyslotNbr);
            if (rapduReset.isOK()) {
                pendingAction= PENDING_ACTION_NONE;
                // update keyInfo & layout
                updateKeyslotInfoAfterReset(keyslotNbr);
                updateLayoutAfterKeyslotChange(keyslotNbr);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast= Toast.makeText(getApplicationContext(), R.string.reset_success, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                return true;
            } else {
                if (isOwner && rapduReset.getSw()== 0x9C51) { // 0x9C51 is SW_INCORRECT_UNLOCK_CODE
                    // This could be caused if card ownership has changed outside of the app
                    updateLayoutOwnershipWarning();
                }
                throw new Exception("RAPDU error: " + Integer.toHexString(rapduReset.getSw()));
            }
        } catch (Exception e) {
            if(DEBUG) Log.e(TAG, "Failed to reset keyslot! " + e);
            e.printStackTrace();
            pendingAction= PENDING_ACTION_RESET;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast= Toast.makeText(getApplicationContext(), R.string.reset_fail, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
            return false;
        }

    }

    //send transfer card APDU command to card
    public boolean sendTransferApduToCard() {
        // send seal command to card
        if(DEBUG) Log.d(TAG, "TRANSFER SENDING APDU TO CARD");
        try {

            if (!isConnected) {
                throw new Exception("No card connected!");
            }

            // check that authentikey match actual card authentikey
            if (!keyslotAuthentikeyHex.equals(cmdSet.getAuthentikeyHex())) {
                throw new Exception("Authentikeys do not match!");
            }

            APDUResponse rapduTransfer= cmdSet.satodimeInitiateOwnershipTransfer();
            if (rapduTransfer.isOK()) {
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
                        Toast toast= Toast.makeText(getApplicationContext(), R.string.transfer_init_success, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                return true;
            } else {
                if (isOwner && rapduTransfer.getSw()== 0x9C51) { // 0x9C51 is SW_INCORRECT_UNLOCK_CODE
                    // This could be caused if card ownership has changed outside of the app
                    updateLayoutOwnershipWarning();
                }
                throw new Exception("RAPDU error: " + Integer.toHexString(rapduTransfer.getSw()));
            }
        } catch (Exception e) {
            if(DEBUG) Log.e(TAG, "Failed to transfer card ownership! " + e);
            e.printStackTrace();
            pendingAction= PENDING_ACTION_TRANSFER;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast= Toast.makeText(getApplicationContext(), R.string.transfer_init_fail, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
            return false;
        }

    }

    // send satodimeGetKeyslotStatus() command and gather keyInfo
    public void updateKeyslotInfoAfterSeal(int keyslotNbr) {
        if(DEBUG) Log.d(TAG, "updateKeyslotInfoAfterSeal: keyslotNbr= " + keyslotNbr);

        if (!isConnected) {
            return;
        }

        satodimeStatus= cmdSet.getSatodimeStatus();
        keysState= satodimeStatus.getKeysState();
        int keyState= keysState[keyslotNbr];
        if (keyState== STATE_UNINITIALIZED) {
            return;
        }

        HashMap<String, Object> keyInfo= new HashMap<String, Object>();
        keyInfo.put("keyState", keyState);
        keyInfo.put("keyNbr", keyslotNbr);

        // get keyslots status
        if(DEBUG) Log.d(TAG, "Keyslot " + keyslotNbr + " get status...");
        APDUResponse rapduKeyslotStatus= cmdSet.satodimeGetKeyslotStatus(keyslotNbr);
        if(DEBUG) Log.d(TAG, "Keyslot " + keyslotNbr + " status received!");
        SatodimeKeyslotStatus keyslotStatus= new SatodimeKeyslotStatus(rapduKeyslotStatus);
        if(DEBUG) Log.d(TAG, "Keyslot " + keyslotNbr + " status:" + keyslotStatus.toString());
        keyInfo.put("keyType", (int) keyslotStatus.getKeyType());
        // asset type (coin/token/nft)
        int keyAsset= (int) keyslotStatus.getKeyAsset();
        String keyAssetTxt= (String) MAP_ASSET_BY_CODE.get(keyAsset);
        keyInfo.put("keyAsset", keyAsset); // convert byte to int, otherwise it is boxed to Byte value (instead of Integer)
        keyInfo.put("keyAssetTxt", keyAssetTxt);
        boolean isToken= TOKENSET.contains(keyAssetTxt);
        boolean isNFT= NFTSET.contains(keyAssetTxt);
        keyInfo.put("isToken", isToken); // convert byte to int, otherwise it is boxed to Byte value (instead of Integer)
        keyInfo.put("isNFT", isNFT);
        // slip44
        byte[] keySlip44= keyslotStatus.getKeySlip44();
        ByteBuffer wrapped= ByteBuffer.wrap(keySlip44); // big-endian by default
        int keySlip44Int= wrapped.getInt();
        boolean isTestnet= ((keySlip44[0] & 0x80)== 0x00); // testnet if first bit is 0
        keyInfo.put("keySlip44", keySlip44);
        keyInfo.put("keySlip44Int", keySlip44Int);
        keyInfo.put("keySlip44Hex", parser.toHexString(keySlip44));
        keyInfo.put("isTestnet", isTestnet);
        // token/nft info
        final String keyContractHex= "0x" + parser.toHexString(keyslotStatus.getKeyContract());
        final String keyTokenIdHex= parser.toHexString(keyslotStatus.getKeyTokenId());
        final String keyTokenIdDec;
        if (keyTokenIdHex.length() >= 2) {
            keyTokenIdDec= (new BigInteger(keyTokenIdHex, 16)).toString();
        } else {
            keyTokenIdDec= "";
        }
        keyInfo.put("keyContract", keyslotStatus.getKeyContract());
        keyInfo.put("keyContractHex", keyContractHex);
        keyInfo.put("keyTokenId", keyslotStatus.getKeyTokenId());
        keyInfo.put("keyTokenIdHex", keyTokenIdHex);
        keyInfo.put("keyTokenIdDec", keyTokenIdDec);
        keyInfo.put("keyData", keyslotStatus.getKeyData());
        keyInfo.put("keyDataHex", parser.toHexString(keyslotStatus.getKeyData()));

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
                    try {
                        coinBalance= coin.getBalance(coinAddress);
                        coinBalanceTxt= coinBalance + " " + coinDisplayName + " (" + coinSymbol + ")";
                        keyInfo2.put("coinBalance", coinBalance);
                        //keyInfo2.put("coinBalanceTxt", coinBalanceTxt);

                        // get exchange rate with preferred fiat
                        if (useFiat) {
                            double coinRate= coin.get_exchange_rate_between(appFiat);
                            if (coinRate >= 0) {
                                double coinValue= coinBalance * coinRate;
                                coinBalanceTxt += "\t (~ " + coinValue + " " + appFiat + ")";
                            } else {
                                coinBalanceTxt += "\t (~ ? " + appFiat + ")";
                            }
                        }
                        keyInfo2.put("coinBalanceTxt", coinBalanceTxt);

                    } catch (Exception e) {
                        coinBalance= -1;
                        coinBalanceTxt= getResources().getString(R.string.coin_balance_fail); //"(failed to fetch coin balance)";
                        keyInfo2.put("coinBalance", coinBalance);
                        keyInfo2.put("coinBalanceTxt", coinBalanceTxt);
                    }

                    // token/nft
                    double tokenBalance=-1;
                    String tokenBalanceTxt= getResources().getString(R.string.token_balance_wait); // temporary value
                    keyInfo2.put("tokenBalance", tokenBalance);
                    keyInfo2.put("tokenBalanceTxt", tokenBalanceTxt);
                    if (isToken || isNFT) {
                        try {
                            long balanceLong= coin.getTokenBalance(coinAddress, keyContractHex);
                            HashMap<String, String> tokenInfo= coin.getTokenInfo(keyContractHex);
                            String decimalsTxt= tokenInfo.get("decimals");
                            int decimals= Integer.parseInt(decimalsTxt);
                            tokenBalance= (double) (balanceLong) / (Math.pow(10, decimals));
                            keyInfo2.put("tokenBalance", tokenBalance);
                            // txt
                            String tokenDisplayName= tokenInfo.get("name");
                            String tokenSymbol= tokenInfo.get("symbol");
                            tokenBalanceTxt= tokenBalance + " " + tokenDisplayName + " (" + tokenSymbol + ")";
                            // get exchange rate with preferred fiat
                            if (useFiat && !isNFT) {
                                double tokenRate= coin.get_token_exchange_rate_between(keyContractHex, appFiat);
                                if (tokenRate >= 0) {
                                    double tokenValue= tokenBalance * tokenRate;
                                    tokenBalanceTxt += "\t (~ " + tokenValue + " " + appFiat + ")";
                                } else {
                                    tokenBalanceTxt += "\t (~ ? " + appFiat + ")";
                                }
                            }
                            keyInfo2.put("tokenBalanceTxt", tokenBalanceTxt);
                            // TODO: update ui
                        } catch (Exception e) {
                            tokenBalance= -1;
                            tokenBalanceTxt= getResources().getString(R.string.token_balance_fail);
                            keyInfo2.put("tokenBalance", tokenBalance);
                            keyInfo2.put("tokenBalanceTxt", tokenBalanceTxt);
                        }
                    } else {
                        tokenBalanceTxt= "(N/A)";
                    }

                    // add keyInfo to keyInfoList
                    // TODO: thread safety?
                    keyInfoList.get(keyslotNbr).putAll(keyInfo2);

                    // update ui?
                    final String coinBalanceTxtFinal= coinBalanceTxt;
                    final String tokenBalanceTxtFinal= tokenBalanceTxt;
                    try {

                        // The layout is not always ready
                        while (!isLayoutReady) {
                            if(DEBUG) Log.d(TAG, "WAITING LAYOUT for keyslot " + keyslotNbr);
                            Thread.sleep(500);
                        }
                        balance0.post(new Runnable() {
                            @Override
                            public void run() {
                                switch (keyslotNbr) {
                                    case 0: {
                                        if(DEBUG)
                                            Log.d(TAG, "UPDATING LAYOUT for keyslot " + keyslotNbr);
                                        balance0.setText(coinBalanceTxtFinal);
                                        tokenBalance0.setText(tokenBalanceTxtFinal);
                                        if(DEBUG)
                                            Log.d(TAG, "UPDATED LAYOUT for keyslot " + keyslotNbr);
                                        break;
                                    }
                                    case 1: {
                                        if(DEBUG)
                                            Log.d(TAG, "UPDATING LAYOUT for keyslot " + keyslotNbr);
                                        balance1.setText(coinBalanceTxtFinal);
                                        tokenBalance1.setText(tokenBalanceTxtFinal);
                                        if(DEBUG)
                                            Log.d(TAG, "UPDATED LAYOUT for keyslot " + keyslotNbr);
                                        break;
                                    }
                                    case 2: {
                                        if(DEBUG)
                                            Log.d(TAG, "UPDATING LAYOUT for keyslot " + keyslotNbr);
                                        balance2.setText(coinBalanceTxtFinal);
                                        tokenBalance2.setText(tokenBalanceTxtFinal);
                                        if(DEBUG)
                                            Log.d(TAG, "UPDATED LAYOUT for keyslot " + keyslotNbr);
                                        break;
                                    }
                                }
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
        if (isNFT) {
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

                        try {
                            HashMap<String, String> nftInfo= coin.getNftInfo(keyContractHex, keyTokenIdDec);
                            keyInfoNft.put("nftName", nftInfo.get("nftName"));
                            keyInfoNft.put("nftDescription", nftInfo.get("nftDescription"));
                            keyInfoNft.put("nftImageUrl", nftInfo.get("nftImageUrl"));
                        } catch (Exception e) {
                            if(DEBUG) Log.e(TAG, "Failed to fetch nftInfoMap in thread: " + e);
                        }

                        // get data from network
                        try {
                            String nftImageUrl= (String) keyInfoNft.get("nftImageUrl");
                            URL url= new URL(nftImageUrl);
                            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
                            connection.setDoInput(true);
                            connection.connect();
                            InputStream input= connection.getInputStream();
                            Bitmap nftBitmap= BitmapFactory.decodeStream(input);
                            keyInfoNft.put("nftBitmap", nftBitmap);
                        } catch (Exception e) {
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
    public void updateKeyslotInfoAfterUnseal(int keyslotNbr) {
        if(DEBUG) Log.d(TAG, "updateKeyslotInfoAfterUnseal: keyslotNbr= " + keyslotNbr);

        if (!isConnected) {
            return;
        }

        satodimeStatus= cmdSet.getSatodimeStatus();
        keysState= satodimeStatus.getKeysState();
        int keyState= keysState[keyslotNbr];

        if (keyState != STATE_UNSEALED) {
            // TODO: should not happen, but nevertheless, do something?
            return;
        }

        HashMap<String, Object> keyInfo= new HashMap<String, Object>();
        keyInfo.put("keyState", keyState);
        keyInfo.put("keyNbr", keyslotNbr);

        // recover slip44 from memory
        byte[] keySlip44= (byte[]) keyInfoList.get(keyslotNbr).get("keySlip44");
        ByteBuffer wrapped= ByteBuffer.wrap(keySlip44); // big-endian by default
        int keySlip44Int= wrapped.getInt();
        boolean isTestnet= ((keySlip44[0] & 0x80)== 0x00); // testnet if first bit is 0

        // get coin object from javacryptotools
        BaseCoin coin= getCoin(keySlip44Int, isTestnet, APIKEYS);

        // get private key
        if(DEBUG) Log.d(TAG, "Privkey recovery: START");
        try {
            APDUResponse rapduPrivkey= cmdSet.satodimeGetPrivkey(keyslotNbr);
            if (rapduPrivkey.isOK()) {
                HashMap<String, byte[]> privkeyInfo= parser.parseSatodimeGetPrivkey(rapduPrivkey);
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
            } else {
                if (isOwner && rapduPrivkey.getSw()== 0x9C51) { // 0x9C51 is SW_INCORRECT_UNLOCK_CODE
                    // This could be caused if card ownership has changed outside of the app
                    updateLayoutOwnershipWarning();
                }
                throw new Exception("RAPDU error: " + Integer.toHexString(rapduPrivkey.getSw()));
            }
        } catch (Exception e) {
            if(DEBUG) Log.e(TAG, "Exception: unable to recover privkey: " + e);
            String privkey_fail= getResources().getString(R.string.privkey_fail);
            keyInfo.put("privkeyHex", privkey_fail);
            keyInfo.put("privkeyWif", privkey_fail);
            keyInfo.put("entropyHex", privkey_fail);
        }
        // add keyInfo to keyInfoList
        // TODO: thread safe?
        keyInfoList.get(keyslotNbr).putAll(keyInfo);

        return;
    } // end

    public void updateKeyslotInfoAfterReset(int keyslotNbr) {
        if(DEBUG) Log.d(TAG, "updateKeyslotInfoAfterReset: keyslotNbr= " + keyslotNbr);

        if (!isConnected) {
            return;
        }

        satodimeStatus= cmdSet.getSatodimeStatus();
        if(DEBUG) Log.d(TAG, "Satodime status:" + satodimeStatus.toString());
        keysState= satodimeStatus.getKeysState();
        int keyState= keysState[keyslotNbr];

        if (keyState != STATE_UNINITIALIZED) {
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
    public void updateLayoutAfterKeyslotChange(int keyslotNbr) {

        // update data from keyInfo
        HashMap<String, Object> keyInfo= keyInfoList.get(keyslotNbr);
        int keyState= (int) keyInfo.get("keyState");
        String[] array_keyslot_states= getResources().getStringArray(R.array.array_keyslot_states);
        String keyStateTxt= getResources().getString(R.string.key_nbr) + String.valueOf(keyslotNbr) + " - " + array_keyslot_states[keyState];
        // address
        final String address;
        if (keyState== STATE_UNINITIALIZED) {
            address= getResources().getString(R.string.uninitialized);
        } else {
            address= (String) keyInfo.get("coinAddress"); // keyInfo.getOrDefault("pubkeyHex", "(unknown address)");
        }
        // balance
        final String keyAssetTxt;
        final String coinBalanceTxt;
        final boolean isTokenOrNFT;
        final String tokenBalanceTxt;
        if (keyState== STATE_UNINITIALIZED) {
            keyAssetTxt= getResources().getString(R.string.uninitialized);
            coinBalanceTxt= getResources().getString(R.string.uninitialized);
            tokenBalanceTxt= getResources().getString(R.string.uninitialized);
            isTokenOrNFT= false;
        } else {
            keyAssetTxt= (String) keyInfo.get("keyAssetTxt");
            coinBalanceTxt= (String) keyInfo.get("coinBalanceTxt");

            // token balance
            boolean isToken= (boolean) keyInfo.get("isToken");
            boolean isNFT= (boolean) keyInfo.get("isNFT");
            isTokenOrNFT= (isToken || isNFT);
            if (isTokenOrNFT) {
                tokenBalanceTxt= (String) keyInfo.get("tokenBalanceTxt");
            } else {
                tokenBalanceTxt= getResources().getString(R.string.not_applicable);
            }
        }
        // button
        String[] array_actions_from_state= getResources().getStringArray(R.array.array_actions_from_state);
        String actionMsgUpdated= array_actions_from_state[keyState];


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (keyslotNbr) {
                    case 0: {
                        String blockChain= (String) keyInfoList.get(0).get("coinDisplayName");
                        if (blockChain != null) {
                            switch (blockChain) {
                                case "Bitcoin":
                                case "Bitcoin Testnet":{
                                    cardImg0.setImageResource(R.drawable.ic_bitcoin);
                                    break;
                                }
                                case "Ethereum":
                                case "Ethereum Testnet":{
                                    cardImg0.setImageResource(R.drawable.ic_etherium);
                                    break;
                                }
                                case "Litecoin":
                                case "Litecoin Testnet":{
                                    cardImg0.setImageResource(R.drawable.ic_ltc);
                                    break;
                                }
                                case "Bitcoin Cash":
                                case "Bitcoin Cash Testnet":{
                                    cardImg0.setImageResource(R.drawable.ic_bch);
                                    break;
                                }
                            }
                        }
                        if (array_keyslot_states[keyState].equals(getString(R.string.sealed))) {
                            key0.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.green));
                            card0.setBackground(MainActivity.this.getDrawable(R.drawable.card_outline_green));
                            cardStatus0.setText(actionMsgUpdated);
                            cardStatus0.setTextColor(getResources().getColor(R.color.green));
                            cardStatusImg0.setImageResource(R.drawable.ic_lock);
                        }
                        if (array_keyslot_states[keyState].equals(getString(R.string.unsealed))) {
                            key0.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.RED));
                            card0.setBackground(MainActivity.this.getDrawable(R.drawable.card_ouline_red));
                            cardStatus0.setText(actionMsgUpdated);
                            cardStatus0.setTextColor(getResources().getColor(R.color.RED));
                            cardStatusImg0.setImageResource(R.drawable.ic_unlock);
                            break;
                        }
                        if (array_keyslot_states[keyState].equals(getString(R.string.lbl_uninitalized))) {
                            key0.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.gold_light));
                            card0.setBackground(MainActivity.this.getDrawable(R.drawable.card_outline_gold));
                            cardStatus0.setText(actionMsgUpdated);
                            cardStatus0.setTextColor(getResources().getColor(R.color.grey));
                            cardStatusImg0.setImageResource(R.drawable.ic_hourglass);
                            cardImg0.setImageResource(R.drawable.ic_hourglass);
                            break;
                        }


//                        cardName0.setText(keyStateTxt);
                        assestType0.setText(keyAssetTxt);
                        cardAddress0.setText(address);
                        balance0.setText(coinBalanceTxt);

                        // if token/NFT, show layout & balance
                        if (isTokenOrNFT) {
                            tokenBalance0.setText(tokenBalanceTxt);
                            tokenBalanceLayout0.setVisibility(View.VISIBLE);
                        } else {
                            tokenBalanceLayout0.setVisibility(View.GONE);
                        }
                        break;
                    }
                    case 1: {
                        String blockChain= (String) keyInfoList.get(1).get("coinDisplayName");
                        if (blockChain != null) {
                            switch (blockChain) {
                                case "Bitcoin":
                                case "Bitcoin Testnet":{
                                    cardImg1.setImageResource(R.drawable.ic_bitcoin);
                                    break;
                                }
                                case "Ethereum":
                                case "Ethereum Testnet":{
                                    cardImg1.setImageResource(R.drawable.ic_etherium);
                                    break;
                                }
                                case "Litecoin":
                                case "Litecoin Testnet":{
                                    cardImg1.setImageResource(R.drawable.ic_ltc);
                                    break;
                                }
                                case "Bitcoin Cash":
                                case "Bitcoin Cash Testnet":{
                                    cardImg1.setImageResource(R.drawable.ic_bch);
                                    break;
                                }
                            }
                        }
                        if (array_keyslot_states[keyState].equals(getString(R.string.sealed))) {
                            key1.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.green));
                            card1.setBackground(MainActivity.this.getDrawable(R.drawable.card_outline_green));
                            cardStatus1.setText(actionMsgUpdated);
                            cardStatus1.setTextColor(getResources().getColor(R.color.green));
                            cardStatusImg1.setImageResource(R.drawable.ic_lock);
                        }
                        if (array_keyslot_states[keyState].equals(getString(R.string.unsealed))) {
                            key1.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.RED));
                            card1.setBackground(MainActivity.this.getDrawable(R.drawable.card_ouline_red));
                            cardStatus1.setText(actionMsgUpdated);
                            cardStatus1.setTextColor(getResources().getColor(R.color.RED));
                            cardStatusImg1.setImageResource(R.drawable.ic_unlock);
                        }
                        if (array_keyslot_states[keyState].equals(getString(R.string.lbl_uninitalized))) {
                            key1.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.gold_light));
                            card1.setBackground(MainActivity.this.getDrawable(R.drawable.card_outline_gold));
                            cardStatus1.setText(actionMsgUpdated);
                            cardStatus1.setTextColor(getResources().getColor(R.color.grey));
                            cardStatusImg1.setImageResource(R.drawable.ic_hourglass);
                            cardImg1.setImageResource(R.drawable.ic_hourglass);
                        }

//                        cardName1.setText(keyStateTxt);
                        assestType1.setText(keyAssetTxt);
                        cardAddress1.setText(address);
                        balance1.setText(coinBalanceTxt);
                        // if token/NFT, show layout & balance
                        if (isTokenOrNFT) {
                            tokenBalance1.setText(tokenBalanceTxt);
                            tokenBalanceLayout1.setVisibility(View.VISIBLE);
                        } else {
                            tokenBalanceLayout1.setVisibility(View.GONE);
                        }
                        break;
                    }
                    case 2: {

                        String blockChain= (String) keyInfoList.get(2).get("coinDisplayName");
                        if (blockChain != null) {
                            switch (blockChain) {
                                case "Bitcoin":
                                case "Bitcoin Testnet":{
                                    cardImg2.setImageResource(R.drawable.ic_bitcoin);
                                    break;
                                }
                                case "Ethereum":
                                case "Ethereum Testnet":{
                                    cardImg2.setImageResource(R.drawable.ic_etherium);
                                    break;
                                }
                                case "Litecoin":
                                case "Litecoin Testnet":{
                                    cardImg2.setImageResource(R.drawable.ic_ltc);
                                    break;
                                }
                                case "Bitcoin Cash":
                                case "Bitcoin Cash Testnet":{
                                    cardImg2.setImageResource(R.drawable.ic_bch);
                                    break;
                                }
                            }
                        }
                        if (array_keyslot_states[keyState].equals(getString(R.string.sealed))) {
                            key2.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.green));
                            card2.setBackground(MainActivity.this.getDrawable(R.drawable.card_outline_green));
                            cardStatus2.setText(actionMsgUpdated);
                            cardStatus2.setTextColor(getResources().getColor(R.color.green));
                            cardStatusImg2.setImageResource(R.drawable.ic_lock);
                        }
                        if (array_keyslot_states[keyState].equals(getString(R.string.unsealed))) {
                            key2.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.RED));
                            card2.setBackground(MainActivity.this.getDrawable(R.drawable.card_ouline_red));
                            cardStatus2.setText(actionMsgUpdated);
                            cardStatus2.setTextColor(getResources().getColor(R.color.RED));
                            cardStatusImg2.setImageResource(R.drawable.ic_unlock);
                            break;
                        }
                        if (array_keyslot_states[keyState].equals(getString(R.string.lbl_uninitalized))) {
                            key2.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.gold_light));
                            card2.setBackground(MainActivity.this.getDrawable(R.drawable.card_outline_gold));
                            cardStatus2.setText(actionMsgUpdated);
                            cardStatus2.setTextColor(getResources().getColor(R.color.grey));
                            cardStatusImg2.setImageResource(R.drawable.ic_hourglass);
                            cardImg2.setImageResource(R.drawable.ic_hourglass);
                        }


//                        cardName2.setText(keyStateTxt);
                        assestType2.setText(keyAssetTxt);
                        cardAddress2.setText(address);
                        balance2.setText(coinBalanceTxt);
                        // if token/NFT, show layout & balance
                        if (isTokenOrNFT) {
                            tokenBalance2.setText(tokenBalanceTxt);
                            tokenBalanceLayout2.setVisibility(View.VISIBLE);
                        } else {
                            tokenBalanceLayout2.setVisibility(View.GONE);
                        }
                        break;
                    }
                }
            }
        });

    }

    /**
     * If ownership is force-changed outside of  the app, the app may wrongfully thinks it has ownership of the card.
     * Forcing ownership is possible using using a usb card reader that allows to bypass NFC check (unlock_code is only enforced on the NFC interface).
     * If so, sending sensitive APDU (seal-unseal-reset-transfer...) will return 0x9C51 code.
     * A message is shown to the user to let him know that he may not be the owner anymore
     * Prompt the user to unpair card (remove old unlock_secret) once we are sure that ownership has been transfered outside of the app.
     */
    public void updateLayoutOwnershipWarning() {
//
//        // show error message
//        TextView tvOwner= (TextView) findViewById(R.id.value_card_owner);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                tvOwner.setText(R.string.card_ownership_value_unknown);
//                tvOwner.setTextColor(COLOR_ORANGE);
//            }
//        });

        // after limit is reached, propose to remove ownership
        ownership_error_counter++;
        if (ownership_error_counter >= MAX_OWNERSHIP_ERROR) {
            ownership_error_counter= 0;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    final AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
                    ViewGroup viewGroup= findViewById(android.R.id.content);
                    View dialogView= LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_reset_ownership, viewGroup, false);

                    TextView resetBtn= dialogView.findViewById(R.id.reset_btn);
                    TextView cancelBtn= dialogView.findViewById(R.id.cancel_btn);


                    builder.setView(dialogView);
                    final AlertDialog alertDialog= builder.create();

                    resetBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(DEBUG)
                                Log.d(TAG, "RESET OWNERSHIP DIALOG " + "YES  has been clicked!");
                            if(DEBUG) Log.d(TAG, "DEBUGUNLOCK setupDone: START");
                            // remove unlockSecretHex from SharedPreferences
                            prefs.edit().remove(authentikeyHex).apply();
                            isOwner= false;
                            alertDialog.dismiss();
                        }
                    });
                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            if(DEBUG)
                                Log.d(TAG, "OWNERSHIP APPROVAL DIALOG " + "NO  has been clicked!");
                            Toast toast= Toast.makeText(getApplicationContext(), R.string.reset_ownership_rejected, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                    alertDialog.show();
                } // run
            });
        }
    }

    // build a coin object from javacryptotools library
    public BaseCoin getCoin(int keySlip44Int, boolean isTestnet, HashMap<
            String, String> apikeys) {
        BaseCoin coin;
        int keySlip44IntAbs= keySlip44Int | 0x80000000; // switch first bit (ignore testnet or mainnet)
        switch (keySlip44IntAbs) {
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
        if(DEBUG) {
            coin.setLoggerLevel("info");
        } else {
            coin.setLoggerLevel("warning");
        }
        return coin;
    }


    public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.NavigationHolder> {

        private final MainActivity homeActivity;
        private String[] menu_title;
        Boolean isanyselected= false;
        String selectedItem= "";
        private int[] menu_icon= new int[]{

                R.drawable.ic_home,
                R.drawable.ic_card_info,
                R.drawable.ic_credit_cards_payment,
                R.drawable.ic_settings,
                R.drawable.ic_network,
                R.drawable.ic_ask,
        };

        public NavigationAdapter(MainActivity homeActivity) {
            this.homeActivity= homeActivity;
            menu_title= homeActivity.getResources().getStringArray(R.array.array_menu);
            selectedItem= menu_title[0];
        }

        @NonNull
        @Override
        public NavigationAdapter.NavigationHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

            View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_side_nav, viewGroup, false);
            NavigationAdapter.NavigationHolder navigationHolder= new NavigationAdapter.NavigationHolder(view);

            return navigationHolder;
        }

        @RequiresApi(api= Build.VERSION_CODES.M)
        @Override
        public void onBindViewHolder(@NonNull NavigationAdapter.NavigationHolder navigationHolder, @SuppressLint("RecyclerView") final int i) {
            navigationHolder.item_icon.setImageResource(menu_icon[i]);
            navigationHolder.item_icon.setColorFilter(homeActivity.getColor(R.color.black));
            navigationHolder.item_text.setText(menu_title[i]);

            if (!selectedItem.equals("")) {
                for (int j= 0; j < menu_icon.length; j++) {
                    if (navigationHolder.item_text.getText().equals(selectedItem)) {
                        Log.d("txt_menu", navigationHolder.item_text.getText() + "");
                        Log.d("selectedItem", selectedItem + "");
                        navigationHolder.item_background.setBackgroundResource(R.drawable.item_selected_background);
                        navigationHolder.item_icon.setColorFilter(homeActivity.getColor(R.color.white));
                        navigationHolder.item_text.setTextColor(homeActivity.getColor(R.color.white));

                    } else {
                        navigationHolder.item_background.setBackgroundResource(0);
                        navigationHolder.item_icon.setColorFilter(homeActivity.getColor(R.color.black));
                        navigationHolder.item_text.setTextColor(homeActivity.getColor(R.color.black));

                    }
                }
            }
            navigationHolder.item_background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String menuTittle= menu_title[i];
                    Log.d("menuNameaa", menuTittle);
                    if (menuTittle.equalsIgnoreCase(getString(R.string.item_home))) {
                        selectedItem= menu_title[i];
                        setFilter(navigationHolder, i);
                    } else if (menuTittle.equalsIgnoreCase(getString(R.string.item_card_info))) {
                        selectedItem= menu_title[i];
                        homeActivity.startActivity(new Intent(homeActivity, CardInfoActivity.class));
                        setFilter(navigationHolder, i);
                    } else if (menuTittle.equalsIgnoreCase(getString(R.string.item_transfer_card))) {
                        selectedItem= menu_title[i];
                        setFilter(navigationHolder, i);

                        final AlertDialog.Builder builder= new AlertDialog.Builder(homeActivity, R.style.CustomAlertDialog);
                        ViewGroup viewGroup= v.findViewById(android.R.id.content);
                        View dialogView= LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_transfer_card, viewGroup, false);

                        TextView transferBtn= (TextView) dialogView.findViewById(R.id.transfer_btn);
                        TextView cancelBtn= (TextView) dialogView.findViewById(R.id.cancel_btn);

                        builder.setView(dialogView);
                        final AlertDialog alertDialog= builder.create();
                        transferBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                keyslotAuthentikeyHex= authentikeyHex;
                                sendTransferApduToCard();
                            }
                        });
                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                                selectedItem= "Home";
                                setFilter(navigationHolder, 0);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast= Toast.makeText(getApplicationContext(), R.string.transfer_cancelled, Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                });
                            }
                        });
                        alertDialog.show();

                    } else if (menuTittle.equalsIgnoreCase(getString(R.string.item_settings))) {
                        selectedItem= menu_title[i];
                        setFilter(navigationHolder, i);
                        homeActivity.startActivity(new Intent(homeActivity, SettignsActivity.class));
                    } else if (menuTittle.equalsIgnoreCase(getString(R.string.item_tell_a_friend))) {
                        Log.d("menuNameaa", menu_title[i]);
                        selectedItem= menu_title[i];
                        setFilter(navigationHolder, i);
                        Log.d("testLangu", appLanguage);
                        Intent sendIntent= new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        if (appLanguage.equals("fr")) {
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Partagez avec vos amis");
                            sendIntent.putExtra(Intent.EXTRA_TEXT,
                                    "Parce que vos amis méritent de savoir à quel point le monde de la crypto-monnaie est génial avec une carte Satodime ! https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                        } else {
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Share with friends");
                            sendIntent.putExtra(Intent.EXTRA_TEXT,
                                    "Because your friends deserve to know how great the crypto-currency world is with a Satodime card! https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                        }
                        sendIntent.setType("text/plain");
                        homeActivity.startActivity(sendIntent);
                    } else if (menuTittle.equalsIgnoreCase("FAQ")) {
                        selectedItem= menu_title[i];
                        setFilter(navigationHolder, i);
                        Intent browserIntent= null;
                        if (appLanguage.equals("fr")) {
                            browserIntent= new Intent(Intent.ACTION_VIEW, Uri.parse("https://satochip.io/faq/?lang=fr"));
                        } else {
                            browserIntent= new Intent(Intent.ACTION_VIEW, Uri.parse("https://satochip.io/faq/"));
                        }
                        homeActivity.startActivity(browserIntent);
                    }

                    homeActivity.closeDrawer();
                }
            });
        }


        public void shareApp() {
            try {
                Intent i= new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
                i.putExtra(Intent.EXTRA_TEXT, homeActivity.getResources().getString(R.string.share_txt));
                homeActivity.startActivityForResult(Intent.createChooser(i, homeActivity.getResources().getString(R.string.share_app_cptn)), 100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @RequiresApi(api= Build.VERSION_CODES.M)
        void setFilter(NavigationAdapter.NavigationHolder navigationHolder, int index) {
            for (int i= 0; i < menu_icon.length; i++) {
                if (navigationHolder.item_text.getText().equals(selectedItem)) {
                    Log.d("txt_menu", navigationHolder.item_text.getText() + "");
                    Log.d("selectedItem", selectedItem + "");
                    navigationHolder.item_background.setBackgroundResource(R.drawable.item_selected_background);
                    navigationHolder.item_icon.setColorFilter(homeActivity.getColor(R.color.white));
                    navigationHolder.item_text.setTextColor(homeActivity.getColor(R.color.white));

                } else {
                    navigationHolder.item_background.setBackgroundResource(0);
                    navigationHolder.item_icon.setColorFilter(homeActivity.getColor(R.color.black));
                    navigationHolder.item_text.setTextColor(homeActivity.getColor(R.color.black));
                }
                notifyItemChanged(i);
            }
        }


        @Override
        public int getItemCount() {
            return menu_icon.length;
        }


        public class NavigationHolder extends RecyclerView.ViewHolder {

            private ImageView item_icon;
            private LinearLayout item_background;
            private TextView item_text;

            public NavigationHolder(@NonNull View itemView) {
                super(itemView);

                item_icon= itemView.findViewById(R.id.item_icon);
                item_background= itemView.findViewById(R.id.parent_layout);
                item_text= itemView.findViewById(R.id.item_text);

            }
        }
    }


}
