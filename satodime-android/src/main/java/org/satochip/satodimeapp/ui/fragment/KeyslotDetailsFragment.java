package org.satochip.satodimeapp.ui.fragment;

import android.app.Dialog;
import android.app.AlertDialog;
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

import static org.satochip.client.Constants.*;

import java.util.HashMap;

import androidx.fragment.app.DialogFragment;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class KeyslotDetailsFragment extends DialogFragment {

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "SATODIME_SHOW_DETAILS";
    private static final int[] BACKGROUND_COLORS = {Color.LTGRAY, 0xff90EE90, 0xFFFFD580};
    
    private CardView toolBar;
    private ImageView backBtn;
    private LinearLayout nftLayout, showPublicKey;
    private TextView showMoreBtn;
    private TextView headingTxt;

    private HashMap<String, Object> keyInfo;
    
    private boolean showNftDetails = false;
    private boolean showPrivkey = false;
    private boolean showPubkey = false;
    private LinearLayout llPrivInfoShowhide;
    private ImageView imviewPrivkey;
    private ImageView imviewPubkey;
    private ImageView imviewWif;
    private String pubkeyHex;

    private ClipboardManager myClipboard;
    private ClipData myClip;
    String[] ARRAY_KEYSLOT_STATES;
    int keyState;
        
    @Override	
    public Dialog onCreateDialog(Bundle savedInstanceState) {	
        // Get the layout inflater	
        LayoutInflater inflater = requireActivity().getLayoutInflater();	
        // Inflate and set the layout for the dialog	
        // Pass null as the parent view because its going in the dialog layout	
        View view= inflater.inflate(R.layout.activity_keyslot_details, null);	
        	
        keyInfo = (HashMap<String,Object>) getArguments().getSerializable("keyInfo");	
        int keyNbr= (int) keyInfo.get("keyNbr");	
        if(DEBUG) Log.d(TAG, "ShowDetailsActivity: keyNbr:" + keyNbr); 	
        int keyState= (int) keyInfo.get("keyState");	
        if(DEBUG) Log.d(TAG, "ShowDetailsActivity: keyState:" + keyState); 	
        
        // header
        headingTxt = view.findViewById(R.id.heading_text);
        String header= getResources().getString(R.string.keyslot_details_header) + keyNbr;
        headingTxt.setText(header);
        toolBar = view.findViewById(R.id.toolbar);
        toolBar.setBackgroundResource(R.drawable.bottom_corer_round);
        //Window window = getActivity().getWindow(); // todo: set color for system toolbar
        //window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (keyState==STATE_SEALED) {
            toolBar.setBackgroundTintList(ContextCompat.getColorStateList(getActivity().getApplicationContext(), R.color.green));
            //window.setStatusBarColor(getResources().getColor(R.color.green));
        }
        else if (keyState==STATE_UNSEALED) {
            toolBar.setBackgroundTintList(ContextCompat.getColorStateList(getActivity().getApplicationContext(), R.color.RED));
            //window.setStatusBarColor(getResources().getColor(R.color.RED));
        }
        else if (keyState==STATE_UNINITIALIZED) {
            toolBar.setBackgroundTintList(ContextCompat.getColorStateList(getActivity().getApplicationContext(), R.color.gold_color));
            //window.setStatusBarColor(getResources().getColor(R.color.gold_color));
        }
        
        // keyinfo
        ARRAY_KEYSLOT_STATES = getResources().getStringArray(R.array.array_keyslot_states);
        TextView tvKeyslotStatus = (TextView) view.findViewById(R.id.value_details_keyslot_status);
        tvKeyslotStatus.setText(ARRAY_KEYSLOT_STATES[keyState]);
        // keyAsset
        String keyAssetTxt = (String) keyInfo.getOrDefault("keyAssetTxt", "");
        TextView tvKeyslotAsset = (TextView) view.findViewById(R.id.value_details_keyslot_asset_type);
        tvKeyslotAsset.setText(keyAssetTxt);
        // pubkey
        pubkeyHex = (String) keyInfo.getOrDefault("pubkeyHex", "");
        TextView tvPubkeyHex = (TextView) view.findViewById(R.id.value_details_pubkey);
        tvPubkeyHex.setText(pubkeyHex);
        if (DEBUG) Log.d(TAG, "ShowDetailsActivity: pubkeyHex:" + pubkeyHex);
        
        // show/hide pubkey info depending on button state
        View layoutPubkeyInfo = view.findViewById(R.id.group_details_pubkey_info);
        TextView buttonShowhidePubkey = view.findViewById(R.id.button_showhide_pubkey);
        buttonShowhidePubkey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DEBUG) Log.d(TAG, "BUTTON SHOWHIDE CLICKED!");
                showPubkey = !showPubkey;
                if (showPubkey) {
                    layoutPubkeyInfo.setVisibility(View.VISIBLE);
                    buttonShowhidePubkey.setText(R.string.button_hide_pubkey);
                } else {
                    layoutPubkeyInfo.setVisibility(View.GONE);
                    buttonShowhidePubkey.setText(R.string.button_show_pubkey);
                }
            }
        }); // end setOnClickListener
        
        // buttons pubkey
        // COPY
        ImageView imButtonPubkeyCopy = (ImageView) view.findViewById(R.id.imbutton_pubkey_copy);
        imButtonPubkeyCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DEBUG) Log.d(TAG, "BUTTON pubkey COPY CLICKED!");
                myClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                myClip = ClipData.newPlainText("pubkey", pubkeyHex);
                myClipboard.setPrimaryClip(myClip);
                Toast.makeText(getActivity().getApplicationContext(), R.string.pubkey_copied_toast, Toast.LENGTH_SHORT).show();
            }
        });
        // QR
        imviewPubkey = (ImageView) view.findViewById(R.id.imview_pubkey_qr);
        ImageView imButtonPubkeyQr = (ImageView) view.findViewById(R.id.imbutton_pubkey_qr);
        imButtonPubkeyQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DEBUG) Log.d(TAG, "button pubkey QR clicked!");
                if (imviewPubkey.getVisibility() == View.GONE){
                    try {
                        QRCodeWriter writer = new QRCodeWriter();
                        BitMatrix bitMatrix = writer.encode(pubkeyHex.equals("")? "(null)": pubkeyHex, BarcodeFormat.QR_CODE, 512, 512);
                        int width = bitMatrix.getWidth();
                        int height = bitMatrix.getHeight();
                        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                        for (int x = 0; x < width; x++) {
                            for (int y = 0; y < height; y++) {
                                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                            }
                        }
                        imviewPubkey.setImageBitmap(bmp);
                        imviewPubkey.setVisibility(View.VISIBLE);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }  
                } else {
                    imviewPubkey.setVisibility(View.GONE);
                }
            }
        });
        
        // CoinInfo
        // blockchain
        String coinDisplayName = (String) keyInfo.getOrDefault("coinDisplayName", "");
        TextView tvCoinBlockchain = (TextView) view.findViewById(R.id.value_details_coin_blockchain);
        tvCoinBlockchain.setText(coinDisplayName);
        // address
        String coinAddress = (String) keyInfo.getOrDefault("coinAddress", "");
        TextView tvCoinAddress = (TextView) view.findViewById(R.id.value_details_coin_address);
        tvCoinAddress.setText(coinAddress);
        String coinAddressWeburl = (String) keyInfo.getOrDefault("coinAddressWeburl", "");
        // balance
        String coinBalanceTxt = (String) keyInfo.getOrDefault("coinBalanceTxt", "");
        TextView tvCoinBalance = (TextView) view.findViewById(R.id.value_details_coin_balance);
        tvCoinBalance.setText(coinBalanceTxt);

        // address buttons
        // COPY
        ImageView imButtonAddressCopy = view.findViewById(R.id.imbutton_address_copy);
        imButtonAddressCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DEBUG) Log.d(TAG, "BUTTON COPY CLICKED!");
                myClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                myClip = ClipData.newPlainText("address", coinAddress);
                myClipboard.setPrimaryClip(myClip);
                Toast.makeText(getActivity().getApplicationContext(), R.string.address_copied_toast, Toast.LENGTH_SHORT).show();
            }
        });
        // QR
        ImageView imviewAddress = (ImageView) view.findViewById(R.id.imview_address_qr);
        ImageView imButtonAddressQr = view.findViewById(R.id.imbutton_address_qr);
        imButtonAddressQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DEBUG) Log.d(TAG, "BUTTON QR CLICKED!");
                if (imviewAddress.getVisibility() == View.GONE){
                    try {
                        QRCodeWriter writer = new QRCodeWriter();
                        //TODO: change coinAddress?
                        BitMatrix bitMatrix = writer.encode(coinAddress.equals("")? "(null)" : coinAddress , BarcodeFormat.QR_CODE, 512, 512);
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
        ImageView imButtonAddressBrowse = view.findViewById(R.id.imbutton_address_browse);
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
        boolean isToken = (boolean) keyInfo.getOrDefault("isToken", false);
        boolean isNFT = (boolean) keyInfo.getOrDefault("isNFT", false);
        /* boolean isToken = false; 
        boolean isNFT = false; 
        try{ //temp fix
            isToken = (boolean) keyInfo.get("isToken");
            isNFT = (boolean) keyInfo.get("isNFT");
        } catch (Exception e){
            if(DEBUG) Log.e(TAG, "Exception while fetching isToken and isNFT: " + e);
        } */
        if (isToken) {
            String keyContractHex = (String) keyInfo.get("keyContractHex");
            TextView tvTokenContract = (TextView) view.findViewById(R.id.value_details_token_contract);
            tvTokenContract.setText(keyContractHex);

            String tokenBalanceTxt = (String) keyInfo.get("tokenBalanceTxt");
            TextView tvTokenBalance = (TextView) view.findViewById(R.id.value_details_token_balance);
            tvTokenBalance.setText(tokenBalanceTxt);

            LinearLayout llGroupToken = (LinearLayout) view.findViewById(R.id.group_details_token_info);
            llGroupToken.setVisibility(View.VISIBLE);
        } else if (isNFT) {
            String keyContractHex = (String) keyInfo.get("keyContractHex");
            TextView tvTokenContract = (TextView) view.findViewById(R.id.value_details_nft_contract);
            tvTokenContract.setText(keyContractHex);

            String tokenBalanceTxt = (String) keyInfo.get("tokenBalanceTxt");
            TextView tvTokenBalance = (TextView) view.findViewById(R.id.value_details_nft_balance);
            tvTokenBalance.setText(tokenBalanceTxt);

            String keyTokenIdDec = (String) keyInfo.get("keyTokenIdDec");
            TextView tvNftTokenId = (TextView) view.findViewById(R.id.value_details_nft_tokenid);
            tvNftTokenId.setText(keyTokenIdDec);

            LinearLayout llGroupNft = (LinearLayout) view.findViewById(R.id.group_details_nft_info);
            llGroupNft.setVisibility(View.VISIBLE);

            // nft data only shown if balance >0
            //double tokenBalance = (double) keyInfo.get("tokenBalance");
            //if (tokenBalance>0){
            //if (tokenBalance >= 0) { // DEBUG: only show if balance strictly >0
            if (true){ // temp fix
                String nftName = (String) keyInfo.get("nftName");
                LinearLayout llGroupNftName = (LinearLayout) view.findViewById(R.id.group_details_nft_name);
                if (nftName!=null && !nftName.equals("")) {
                    TextView tvNftName = (TextView) view.findViewById(R.id.value_details_nft_name);
                    tvNftName.setText(nftName);
                    llGroupNftName.setVisibility(View.VISIBLE);
                }

                String nftDescription = (String) keyInfo.get("nftDescription");
                LinearLayout llGroupNftDescription = (LinearLayout) view.findViewById(R.id.group_details_nft_description);
                if (nftDescription!=null && !nftDescription.equals("")) {
                    TextView tvNftDescription = (TextView) view.findViewById(R.id.value_details_nft_description);
                    tvNftDescription.setText(nftDescription);
                    llGroupNftDescription.setVisibility(View.VISIBLE);
                }

                Bitmap bmpNft = (Bitmap) keyInfo.get("nftBitmap");
                ImageView imviewNft = (ImageView) view.findViewById(R.id.imview_nft_image);
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
            View layoutPrivInfo = view.findViewById(R.id.group_details_private_info);
            layoutPrivInfo.setVisibility(View.VISIBLE);

            // show/hide privkey info depending on button state
            llPrivInfoShowhide = (LinearLayout) view.findViewById(R.id.group_details_private_showhide_info);
            TextView buttonShowhidePrivkey = view.findViewById(R.id.button_showhide_privkey);
            buttonShowhidePrivkey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (DEBUG) Log.d(TAG, "BUTTON SHOWHIDE CLICKED!");
                    showPrivkey = !showPrivkey;
                    if (showPrivkey) {
                        llPrivInfoShowhide.setVisibility(View.VISIBLE);
                        buttonShowhidePrivkey.setText(R.string.button_hide_privkey);
                    } else {
                        llPrivInfoShowhide.setVisibility(View.GONE);
                        buttonShowhidePrivkey.setText(R.string.button_show_privkey);
                    }
                }
            }); // end setOnClickListener

            //hex
            String privkeyHex = (String) keyInfo.get("privkeyHex");
            TextView tvPrivkeyHex = (TextView) view.findViewById(R.id.value_details_private_key);
            tvPrivkeyHex.setText(privkeyHex);
            // wif
            String privkeyWif = (String) keyInfo.get("privkeyWif");
            TextView tvPrivkeyWif = (TextView) view.findViewById(R.id.value_details_private_wif);
            tvPrivkeyWif.setText(privkeyWif);
            // entropy
            String entropyHex = (String) keyInfo.get("entropyHex");
            TextView tvEntropyHex = (TextView) view.findViewById(R.id.value_details_private_entropy);
            tvEntropyHex.setText(entropyHex);

            // buttons privkey
            // COPY
            ImageView imButtonPrivkeyCopy = (ImageView) view.findViewById(R.id.imbutton_privkey_copy);
            imButtonPrivkeyCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (DEBUG) Log.d(TAG, "BUTTON PRIVKEY COPY CLICKED!");
                    myClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    myClip = ClipData.newPlainText("privkey", privkeyHex);
                    myClipboard.setPrimaryClip(myClip);
                    Toast.makeText(getActivity().getApplicationContext(), R.string.privkey_copied_toast, Toast.LENGTH_SHORT).show();
                }
            });
            // QR
            imviewPrivkey = (ImageView) view.findViewById(R.id.imview_privkey_qr);
            ImageView imButtonPrivkeyQr = (ImageView) view.findViewById(R.id.imbutton_privkey_qr);
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
            ImageView imButtonWifCopy = (ImageView) view.findViewById(R.id.imbutton_wif_copy);
            imButtonWifCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (DEBUG) Log.d(TAG, "BUTTON WIF COPY CLICKED!");
                    myClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    myClip = ClipData.newPlainText("WIF", privkeyWif);
                    myClipboard.setPrimaryClip(myClip);
                    Toast.makeText(getActivity().getApplicationContext(), R.string.wif_copied_toast, Toast.LENGTH_SHORT).show();
                }
            });
            // QR
            imviewWif = (ImageView) view.findViewById(R.id.imview_wif_qr);
            ImageView imButtonWifQr = (ImageView) view.findViewById(R.id.imbutton_wif_qr);
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
       
        backBtn = view.findViewById(R.id.back_btn);
        showMoreBtn = view.findViewById(R.id.show_more_btn);
        nftLayout = view.findViewById(R.id.nft_info_layout);
        clickListeners();
       
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
    
    private void clickListeners() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //finish();
                getDialog().dismiss();
            }
        });
        
        showMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!showNftDetails) {
                    nftLayout.setVisibility(View.VISIBLE);
                    showNftDetails = true;
                    showMoreBtn.setText("Show less");
                } else {
                    nftLayout.setVisibility(View.GONE);
                    showNftDetails = false;
                    showMoreBtn.setText("Show more");
                }
            }
        });
    }

}