package org.satochip.satodimeapp.ui.activity;

import static org.satochip.client.Constants.STATE_UNSEALED;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.content.ClipData;
import android.content.ClipboardManager;
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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.satochip.satodimeapp.BuildConfig;
import org.satochip.satodimeapp.MainActivity;
import org.satochip.satodimeapp.R;

import java.util.HashMap;

public class KeySlotDetailsActivity extends AppCompatActivity {


    private CardView toolBar;
    private ImageView backBtn;
    LinearLayout nftLayout, showPublicKey;
    TextView showMoreBtn;
    TextView headingTxt;
    private boolean isShowMore = false;


    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "SATODIME_SHOW_DETAILS";
    // private static final int COLOR_AUTH_OK_BACKGROUND= 0xff90EE90; //0xff9cff57; //0xff64dd17; //
    // private static final int COLOR_AUTH_KO_BACKGROUND=  0xffff867c; //0xffef5350; //
    // private static final int COLOR_AUTH_OK_TXT= 0xff1b5e20;
    // private static final int COLOR_AUTH_KO_TXT= 0xff7f0000;
    private static final int[] BACKGROUND_COLORS = {Color.LTGRAY, 0xff90EE90, 0xFFFFD580};

    private HashMap<String, Object> keyInfo;

    private boolean buttonShowhideShow = false;
    LinearLayout llPrivInfoShowhide;
    ImageView imviewPrivkey;
    ImageView imviewWif;
    String pubkeyHex;

    private ClipboardManager myClipboard;
    private ClipData myClip;
    String[] ARRAY_KEYSLOT_STATES;
    int keyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_slot_details);
        initAll();

        clickListners();


        // KeyInfo
        // keyStatus

        TextView tvKeyslotStatus = (TextView) findViewById(R.id.value_details_keyslot_status);
        tvKeyslotStatus.setText(ARRAY_KEYSLOT_STATES[keyState]);
        // pubkeyHex
        pubkeyHex = (String) keyInfo.get("pubkeyHex");
        if (pubkeyHex == null) {
            pubkeyHex = "(unknown pubkey)";
        }
        if (DEBUG) Log.d(TAG, "ShowDetailsActivity: pubkeyHex:" + pubkeyHex);
        TextView tvKeyslotPubkey = (TextView) findViewById(R.id.value_details_keyslot_pubkley);
        tvKeyslotPubkey.setText(pubkeyHex);
        // keyAsset
        //int keyAsset= (int) keyInfo.get("keyAsset");
        String keyAssetTxt = (String) keyInfo.get("keyAssetTxt");
        TextView tvKeyslotAsset = (TextView) findViewById(R.id.value_details_keyslot_asset_type);
        tvKeyslotAsset.setText(keyAssetTxt);
        //tvKeyslotAsset.setText(Integer.toString(keyAsset));

        // CoinInfo
        // blockchain
        String coinDisplayName = (String) keyInfo.get("coinDisplayName");
        TextView tvCoinBlockchain = (TextView) findViewById(R.id.value_details_coin_blockchain);
        tvCoinBlockchain.setText(coinDisplayName);
        // address
        String coinAddress = (String) keyInfo.get("coinAddress");
        TextView tvCoinAddress = (TextView) findViewById(R.id.value_details_coin_address);
        tvCoinAddress.setText(coinAddress);
        String coinAddressWeburl = (String) keyInfo.get("coinAddressWeburl");
        // balance
        String coinBalanceTxt = (String) keyInfo.get("coinBalanceTxt");
        TextView tvCoinBalance = (TextView) findViewById(R.id.value_details_coin_balance);
        tvCoinBalance.setText(coinBalanceTxt);

        // address buttons
        // COPY
        ImageView imButtonAddressCopy = findViewById(R.id.imbutton_address_copy);
        imButtonAddressCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DEBUG) Log.d(TAG, "BUTTON COPY CLICKED!");
                myClipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                myClip = ClipData.newPlainText("address", coinAddress);
                myClipboard.setPrimaryClip(myClip);
                Toast.makeText(KeySlotDetailsActivity.this, R.string.address_copied_toast, Toast.LENGTH_SHORT).show();
            }
        });
        // QR
        ImageView imviewAddress = (ImageView) findViewById(R.id.imview_address_qr);
        ImageView imButtonAddressQr = findViewById(R.id.imbutton_address_qr);
        imButtonAddressQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DEBUG) Log.d(TAG, "BUTTON QR CLICKED!");
                if (imviewAddress.getVisibility() == View.GONE){
                    try {
                        QRCodeWriter writer = new QRCodeWriter();
                        //TODO: change coinAddress?
                        BitMatrix bitMatrix = writer.encode(coinAddress, BarcodeFormat.QR_CODE, 512, 512);
                        int width = bitMatrix.getWidth();
                        int height = bitMatrix.getHeight();
                        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                            }
                        }
                        imviewAddress.setImageBitmap(bmp);
                        imviewAddress.setVisibility(View.VISIBLE);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                } else {
                    imviewAddress.setVisibility(View.GONE);
                }
            }
        });
        // BROWSE
        ImageView imButtonAddressBrowse = findViewById(R.id.imbutton_address_browse);
        imButtonAddressBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DEBUG) Log.d(TAG, "BUTTON BROWSE CLICKED!");
                try {
                    String url = coinAddressWeburl;
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://" + url;
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    if (DEBUG) Log.e(TAG, "Exception while starting action_view intent: " + e);
                    e.printStackTrace();
                }
            }
        });

        // token/NFT
        boolean isToken = (boolean) keyInfo.get("isToken");
        boolean isNFT = (boolean) keyInfo.get("isNFT");
        if (isToken) {
            String keyContractHex = (String) keyInfo.get("keyContractHex");
            TextView tvTokenContract = (TextView) findViewById(R.id.value_details_token_contract);
            tvTokenContract.setText(keyContractHex);

            String tokenBalanceTxt = (String) keyInfo.get("tokenBalanceTxt");
            TextView tvTokenBalance = (TextView) findViewById(R.id.value_details_token_balance);
            tvTokenBalance.setText(tokenBalanceTxt);

            LinearLayout llGroupToken = (LinearLayout) findViewById(R.id.group_details_token_info);
            llGroupToken.setVisibility(View.VISIBLE);
        } else if (isNFT) {
            String keyContractHex = (String) keyInfo.get("keyContractHex");
            TextView tvTokenContract = (TextView) findViewById(R.id.value_details_nft_contract);
            tvTokenContract.setText(keyContractHex);

            String tokenBalanceTxt = (String) keyInfo.get("tokenBalanceTxt");
            TextView tvTokenBalance = (TextView) findViewById(R.id.value_details_nft_balance);
            tvTokenBalance.setText(tokenBalanceTxt);

            String keyTokenIdDec = (String) keyInfo.get("keyTokenIdDec");
            TextView tvNftTokenId = (TextView) findViewById(R.id.value_details_nft_tokenid);
            tvNftTokenId.setText(keyTokenIdDec);

            LinearLayout llGroupNft = (LinearLayout) findViewById(R.id.group_details_nft_info);
            llGroupNft.setVisibility(View.VISIBLE);

            // nft data only shown if balance >0
            double tokenBalance = (double) keyInfo.get("tokenBalance");
            //if (tokenBalance>0){
            if (tokenBalance >= 0) { // DEBUG: only show if balance strictly >0
                String nftName = (String) keyInfo.get("nftName");
                LinearLayout llGroupNftName = (LinearLayout) findViewById(R.id.group_details_nft_name);
                if (!nftName.equals("")) {
                    TextView tvNftName = (TextView) findViewById(R.id.value_details_nft_name);
                    tvNftName.setText(nftName);
                    llGroupNftName.setVisibility(View.VISIBLE);
                }

                String nftDescription = (String) keyInfo.get("nftDescription");
                LinearLayout llGroupNftDescription = (LinearLayout) findViewById(R.id.group_details_nft_description);
                if (!nftDescription.equals("")) {
                    TextView tvNftDescription = (TextView) findViewById(R.id.value_details_nft_description);
                    tvNftDescription.setText(nftDescription);
                    llGroupNftDescription.setVisibility(View.VISIBLE);
                }

                Bitmap bmpNft = (Bitmap) keyInfo.get("nftBitmap");
                ImageView imviewNft = (ImageView) findViewById(R.id.imview_nft_image);
                if (bmpNft != null) {
                    imviewNft.setImageBitmap(bmpNft);
                } else {
                    imviewNft.setImageDrawable(null); // https://stackoverflow.com/questions/2859212/how-to-clear-an-imageview-in-android
                }
                imviewNft.setVisibility(View.VISIBLE);
            }
        }

        // privkey (only if redeemed)
        if (keyState == STATE_UNSEALED) {
            View layoutPrivInfo = findViewById(R.id.group_details_private_info);
            layoutPrivInfo.setVisibility(View.VISIBLE);

            // show/hide privkey info depending on button state
            llPrivInfoShowhide = (LinearLayout) findViewById(R.id.group_details_private_showhide_info);
            TextView buttonShowhide = findViewById(R.id.button_showhide_info);
            buttonShowhide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (DEBUG) Log.d(TAG, "BUTTON SHOWHIDE CLICKED!");
                    buttonShowhideShow = !buttonShowhideShow;
                    if (buttonShowhideShow) {
                        llPrivInfoShowhide.setVisibility(View.VISIBLE);
                        buttonShowhide.setText(R.string.button_hide);
                    } else {
                        llPrivInfoShowhide.setVisibility(View.GONE);
                        buttonShowhide.setText(R.string.button_show);
                    }
                }
            }); // end setOnClickListener

            //hex
            String privkeyHex = (String) keyInfo.get("privkeyHex");
            TextView tvPrivkeyHex = (TextView) findViewById(R.id.value_details_private_key);
            tvPrivkeyHex.setText(privkeyHex);
            // wif
            String privkeyWif = (String) keyInfo.get("privkeyWif");
            TextView tvPrivkeyWif = (TextView) findViewById(R.id.value_details_private_wif);
            tvPrivkeyWif.setText(privkeyWif);
            // entropy
            String entropyHex = (String) keyInfo.get("entropyHex");
            TextView tvEntropyHex = (TextView) findViewById(R.id.value_details_private_entropy);
            tvEntropyHex.setText(entropyHex);

            // buttons privkey
            // COPY
            ImageView imButtonPrivkeyCopy = (ImageView) findViewById(R.id.imbutton_privkey_copy);
            imButtonPrivkeyCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (DEBUG) Log.d(TAG, "BUTTON PRIVKEY COPY CLICKED!");
                    myClipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    myClip = ClipData.newPlainText("privkey", privkeyHex);
                    myClipboard.setPrimaryClip(myClip);
                    Toast.makeText(getApplicationContext(), R.string.privkey_copied_toast, Toast.LENGTH_SHORT).show();
                }
            });
            // QR
            imviewPrivkey = (ImageView) findViewById(R.id.imview_privkey_qr);
            ImageView imButtonPrivkeyQr = (ImageView) findViewById(R.id.imbutton_privkey_qr);
            imButtonPrivkeyQr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (DEBUG) Log.d(TAG, "BUTTON PRIVKEY QR CLICKED!");
                    if (imviewPrivkey.getVisibility() == View.GONE){
                        try {
                            QRCodeWriter writer = new QRCodeWriter();
                            BitMatrix bitMatrix = writer.encode(privkeyHex, BarcodeFormat.QR_CODE, 512, 512);
                            int width = bitMatrix.getWidth();
                            int height = bitMatrix.getHeight();
                            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                            for (int x = 0; x < width; x++) {
                                for (int y = 0; y < height; y++) {
                                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                                }
                            }
                            imviewPrivkey.setImageBitmap(bmp);
                            imviewPrivkey.setVisibility(View.VISIBLE);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }  
                    } else {
                        imviewPrivkey.setVisibility(View.GONE);
                    }
                }
            });

            // buttons WIF
            // COPY
            ImageView imButtonWifCopy = (ImageView) findViewById(R.id.imbutton_wif_copy);
            imButtonWifCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (DEBUG) Log.d(TAG, "BUTTON WIF COPY CLICKED!");
                    myClipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    myClip = ClipData.newPlainText("WIF", privkeyWif);
                    myClipboard.setPrimaryClip(myClip);
                    Toast.makeText(getApplicationContext(), R.string.wif_copied_toast, Toast.LENGTH_SHORT).show();
                }
            });
            // QR
            imviewWif = (ImageView) findViewById(R.id.imview_wif_qr);
            ImageView imButtonWifQr = (ImageView) findViewById(R.id.imbutton_wif_qr);
            imButtonWifQr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (DEBUG) Log.d(TAG, "BUTTON WIF QR CLICKED!");
                    if (imviewWif.getVisibility() == View.GONE){
                        try {
                            QRCodeWriter writer = new QRCodeWriter();
                            BitMatrix bitMatrix = writer.encode(privkeyWif, BarcodeFormat.QR_CODE, 512, 512);
                            int width = bitMatrix.getWidth();
                            int height = bitMatrix.getHeight();
                            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                            for (int x = 0; x < width; x++) {
                                for (int y = 0; y < height; y++) {
                                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                                }
                            }
                            imviewWif.setImageBitmap(bmp);
                            imviewWif.setVisibility(View.VISIBLE);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                    } else {
                        imviewWif.setVisibility(View.GONE);
                    }
                }
            });

        } // end STATE_UNSEALED

//        // build dialog
//        String title= getResources().getString(R.string.show_details_title) + keyNbr;
//        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(KeySlotDetailsActivity.this)
//                .setView(view)
//                .setTitle(title)
//                // Add action buttons
//                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        if(DEBUG) Log.d(TAG, "ShowAuthDetailsFragment: builder.setPositiveButton - onClick");
//                        // do something else?
//                        dismiss();
//                    }
//                })
//                .create();
//
//        return dialog;

    }

    private void clickListners() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        showPublicKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog here

                final AlertDialog.Builder builder = new AlertDialog.Builder(KeySlotDetailsActivity.this, R.style.CustomAlertDialog);
                ViewGroup viewGroup = v.findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_copy_public_key, viewGroup, false);

                TextView key_info = dialogView.findViewById(R.id.key_info);
                LinearLayout copyBtn = dialogView.findViewById(R.id.copyBtn);

                key_info.setText(pubkeyHex);

                builder.setView(dialogView);
                final AlertDialog alertDialog = builder.create();
                copyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //copy code here
                        String text = key_info.getText().toString().trim();
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("public_key", text);
                        clipboard.setPrimaryClip(clip);
                        alertDialog.dismiss();
                        Toast.makeText(KeySlotDetailsActivity.this, "Copied to clipboard..!", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.show();
            }
        });
        showMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isShowMore) {
                    nftLayout.setVisibility(View.VISIBLE);
                    isShowMore = true;
                    showMoreBtn.setText("Show Less");
                } else {
                    nftLayout.setVisibility(View.GONE);
                    isShowMore = false;
                    showMoreBtn.setText("Show More");
                }
            }
        });
    }

    private void initAll() {
        toolBar = findViewById(R.id.toolbar);
        backBtn = findViewById(R.id.back_btn);
        showMoreBtn = findViewById(R.id.show_more_btn);
        nftLayout = findViewById(R.id.nft_info_layout);
        headingTxt = findViewById(R.id.heading_text);
        showPublicKey = findViewById(R.id.show_public_key);
        ARRAY_KEYSLOT_STATES = getResources().getStringArray(R.array.array_keyslot_states);
        toolBar.setBackgroundResource(R.drawable.bottom_corer_round);
        Bundle bundle = getIntent().getExtras();
        keyInfo = (HashMap<String, Object>) bundle.getSerializable("keyInfo");

        int keyNbr = (int) keyInfo.get("keyNbr");
        if (DEBUG) Log.d(TAG, "ShowDetailsActivity: keyNbr:" + keyNbr);
        headingTxt.setText("Details of Keyslot # " + keyNbr);
        keyState = (int) keyInfo.get("keyState");
        if (DEBUG) Log.d(TAG, "ShowDetailsActivity: keyState:" + keyState);

        Log.d("statusDeug", ARRAY_KEYSLOT_STATES[keyState]);
        if (ARRAY_KEYSLOT_STATES[keyState].equals(getString(R.string.sealed))) {
            toolBar.setBackgroundTintList(ContextCompat.getColorStateList(KeySlotDetailsActivity.this, R.color.green));
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.green));
        }
        if (ARRAY_KEYSLOT_STATES[keyState].equals(getString(R.string.unsealed))) {
            Log.d("statusDeug", "Unsealed");
            toolBar.setBackgroundTintList(ContextCompat.getColorStateList(KeySlotDetailsActivity.this, R.color.RED));
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.RED));

        }
        if (ARRAY_KEYSLOT_STATES[keyState].equals(getString(R.string.lbl_uninitalized))) {
            toolBar.setBackgroundTintList(ContextCompat.getColorStateList(KeySlotDetailsActivity.this, R.color.gold_color));
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.gold_color));
        }
    }
}