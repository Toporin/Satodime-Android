package org.satochip.satodimeapp.ui.activity;

import static org.satochip.client.Constants.STATE_SEALED;
import static org.satochip.client.Constants.STATE_UNINITIALIZED;
import static org.satochip.client.Constants.STATE_UNSEALED;
import static org.satochip.satodimeapp.BuildConfig.DEBUG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.satochip.android.NFCCardManager;
import org.satochip.client.ApplicationStatus;
import org.satochip.client.SatochipCommandSet;
import org.satochip.client.SatochipParser;
import org.satochip.client.SatodimeStatus;
import org.satochip.io.APDUResponse;
import org.satochip.io.CardChannel;
import org.satochip.io.CardListener;
import org.satochip.satodimeapp.MainActivity;
import org.satochip.satodimeapp.R;
import org.satochip.satodimeapp.SettingsDialogFragment;
import org.satochip.satodimeapp.ShowAuthDetailsFragment;
import org.satochip.satodimeapp.ShowDetailsFragment;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

public class CardInfoActivity extends AppCompatActivity {

    private CardView toolBar;
    private ImageView backBtn;
    private LinearLayout showCertificateBtn;
    private LinearLayout cardConnectedLayout, cardNotConnectedLayout;

    private NfcAdapter nfcAdapter;
    private static final String TAG = "Card_Info";
    private NFCCardManager cardManager;

    private boolean isConnected = false;
    private SatochipCommandSet cmdSet = null;
    private SatodimeStatus satodimeStatus = null;
    private boolean isOwner = false;
    String[] authResults;
    byte[] authentikey = null;
    String authentikeyHex = null;
    private SatochipParser parser = null;
    private SharedPreferences prefs = null;

    // API KEYS
    private HashMap<String, String> APIKEYS = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_info);

        boolean[] isReconnectionFlag = new boolean[1];
        isReconnectionFlag[0] = false; // detect reconnection

        // load API keys from resources
        APIKEYS = new HashMap<String, String>();
        APIKEYS.put("API_KEY_ETHERSCAN", getString(R.string.API_KEY_ETHERSCAN));
        APIKEYS.put("API_KEY_ETHPLORER", getString(R.string.API_KEY_ETHPLORER));
        APIKEYS.put("API_KEY_BSCSCAN", getString(R.string.API_KEY_BSCSCAN));

        prefs = getSharedPreferences("satodime", MODE_PRIVATE);

        initAll();

        clickListners();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        cardManager = new NFCCardManager();

        cardManager.setCardListener(new CardListener() {
            @Override
            public void onConnected(CardChannel cardChannel) {
                try {
                    // Applet-specific code
                    isConnected = true;
                    // satodime object
                    cmdSet = new SatochipCommandSet(cardChannel);
                    if (DEBUG) {
                        cmdSet.setLoggerLevel("info");
                        Log.d(TAG, "Created a SatochipCommandSet object");
                    } else {
                        cmdSet.setLoggerLevel("warning");
                    }
                    parser = cmdSet.getParser();
                    // First thing to do is selecting the applet on the card.
                    APDUResponse rapdu = cmdSet.cardSelect("satodime");
                    if (DEBUG) Log.d(TAG, "Applet selected:" + rapdu.toHexString());
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast toast = Toast.makeText(getApplicationContext(), R.string.card_connected, Toast.LENGTH_SHORT);
                            toast.show();
                            cardConnectedLayout.setVisibility(View.VISIBLE);
                            cardNotConnectedLayout.setVisibility(View.GONE);
                        }
                    });

                    // get card status
                    APDUResponse rapdu2 = cmdSet.cardGetStatus();
                    ApplicationStatus cardStatus = cmdSet.getApplicationStatus();

                    // get authentikey
                    authentikey = cmdSet.getAuthentikey();
                    authentikeyHex = parser.toHexString(authentikey);
                    if (DEBUG) Log.d(TAG, "Satodime authentikey: " + authentikeyHex);

                    // check if unlock_secret is available for the authentikey
                    if (prefs.contains(authentikeyHex)) {
                        if (DEBUG) Log.d(TAG, "DEBUGUNLOCK recovered: START");
                        String unlockSecretHex = prefs.getString(authentikeyHex, null);
                        byte[] unlockSecret = parser.fromHexString(unlockSecretHex);
                        cmdSet.setSatodimeUnlockSecret(unlockSecret);
                    }


                    // get satodime status
                    satodimeStatus = cmdSet.getSatodimeStatus();

                    // update card connected view
                    TextView tvConnected = (TextView) findViewById(R.id.value_card_connected);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvConnected.setText(R.string.card_connected_value_ok);
                        }
                    });
                    // check card authenticity
                    authResults = cmdSet.cardVerifyAuthenticity();
                    // update status
                    TextView tvStatus = (TextView) findViewById(R.id.value_card_status);
                    TextView tvCardStatusNotConnected = (TextView) findViewById(R.id.card_status_when_not_connected);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (authResults[0].equals("OK")) {
                                tvStatus.setText(R.string.card_status_value_ok);
                                tvCardStatusNotConnected.setText(R.string.card_status_value_ok);
                            } else {
                                tvStatus.setText(R.string.card_status_value_ko);
                                tvCardStatusNotConnected.setText(R.string.card_status_value_ko);
                            }
                        }
                    });
                    // update buttonAuth
                    // update layout header info (details & transfert card button)
                    isOwner = satodimeStatus.isOwner();
                    Log.d("cardStatus",isOwner+"");
                    TextView tvOwner = (TextView) findViewById(R.id.value_card_owner);
                    TextView tvOwnerNot = (TextView) findViewById(R.id.card_ownership_not);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isOwner) {
                                tvOwner.setText(R.string.card_ownership_value_ok);
                                tvOwnerNot.setText(R.string.card_ownership_value_ok);
                            } else {
                                tvOwner.setText(R.string.card_ownership_value_ko);
                                tvOwnerNot.setText(R.string.card_ownership_value_ko);
                            }
                        }
                    });
                } catch (Exception e) {
                    if (DEBUG) Log.e(TAG, e.getMessage());
                    e.printStackTrace();
                }

            } // onConnected

            @Override
            public void onDisconnected() {
                if (DEBUG) Log.d(TAG, "Card disconnected");
                isConnected = false;
                // update card detected
                TextView tvConnected = (TextView) findViewById(R.id.card_not_connected_tv);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), R.string.card_disconnected, Toast.LENGTH_SHORT);
                        toast.show();
                        tvConnected.setText(R.string.card_disconnected);
                        cardConnectedLayout.setVisibility(View.GONE);
                        cardNotConnectedLayout.setVisibility(View.VISIBLE);
                    }
                });
            } // onDisconnected
        }); // cardManager
        cardManager.start();
    }

    private void clickListners() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        showCertificateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(CardInfoActivity.this, R.style.CustomAlertDialog);
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


    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableReaderMode(this, this.cardManager, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableReaderMode(this);
        }
    }

    private void initAll() {
        toolBar = findViewById(R.id.toolbar);
        cardConnectedLayout = findViewById(R.id.connected_card_layout);
        cardNotConnectedLayout = findViewById(R.id.not_connected_card_layout);
        showCertificateBtn = findViewById(R.id.show_certificate_btn);
        backBtn = findViewById(R.id.back_btn);
        toolBar.setBackgroundResource(R.drawable.bottom_corer_round);
    }
}