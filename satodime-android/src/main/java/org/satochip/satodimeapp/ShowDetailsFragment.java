package org.satochip.satodimeapp;

import android.os.Bundle;
import android.util.Log;
import android.net.Uri;
import android.support.v4.app.Fragment; 
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.graphics.Color;
import android.graphics.Bitmap;

import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import org.satochip.satodimeapp.R;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import static org.satochip.client.Constants.*;
import static org.satochip.javacryptotools.coins.Constants.*;


public class ShowDetailsFragment extends DialogFragment {
    
    private static final String TAG = "SATODIME_SHOW_DETAILS";
    // private static final int COLOR_AUTH_OK_BACKGROUND= 0xff90EE90; //0xff9cff57; //0xff64dd17; // 
    // private static final int COLOR_AUTH_KO_BACKGROUND=  0xffff867c; //0xffef5350; //
    // private static final int COLOR_AUTH_OK_TXT= 0xff1b5e20;
    // private static final int COLOR_AUTH_KO_TXT= 0xff7f0000;
    private static final int[] BACKGROUND_COLORS= {Color.LTGRAY, 0xff90EE90, 0xFFFFD580};
    
    private HashMap<String, Object> keyInfo;
    
    private boolean buttonShowhideShow= false;
    LinearLayout llPrivInfoShowhide;
    ImageView imviewPrivkey;
    ImageView imviewWif;
    
    private ClipboardManager myClipboard;
    private ClipData myClip;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view= inflater.inflate(R.layout.activity_show_details, null);
        
        keyInfo = (HashMap<String,Object>) getArguments().getSerializable("keyInfo");
        
        int keyNbr= (int) keyInfo.get("keyNbr");
        Log.d(TAG, "ShowDetailsActivity: keyNbr:" + keyNbr); 
        int keyState= (int) keyInfo.get("keyState");
        Log.d(TAG, "ShowDetailsActivity: keyState:" + keyState); 

        // set background
        LinearLayout llMain= (LinearLayout) view.findViewById(R.id.group_details_main);
        llMain.setBackgroundColor(BACKGROUND_COLORS[keyState]);
    
        // KeyInfo
        // keyStatus
        String[] ARRAY_KEYSLOT_STATES = getResources().getStringArray(R.array.array_keyslot_states); 
        TextView tvKeyslotStatus= (TextView) view.findViewById(R.id.value_details_keyslot_status);
        tvKeyslotStatus.setText(ARRAY_KEYSLOT_STATES[keyState]);
        // pubkeyHex
        String pubkeyHex= (String) keyInfo.get("pubkeyHex");
        if (pubkeyHex== null){pubkeyHex= "(unknown pubkey)"; }
        Log.d(TAG, "ShowDetailsActivity: pubkeyHex:" + pubkeyHex);
        TextView tvKeyslotPubkey= (TextView) view.findViewById(R.id.value_details_keyslot_pubkley);
        tvKeyslotPubkey.setText(pubkeyHex);
        // keyAsset
        //int keyAsset= (int) keyInfo.get("keyAsset");
        String keyAssetTxt= (String) keyInfo.get("keyAssetTxt");
        TextView tvKeyslotAsset= (TextView) view.findViewById(R.id.value_details_keyslot_asset_type);
        tvKeyslotAsset.setText(keyAssetTxt);
        //tvKeyslotAsset.setText(Integer.toString(keyAsset));
        
        // CoinInfo
        // blockchain
        String coinDisplayName= (String)keyInfo.get("coinDisplayName");
        TextView tvCoinBlockchain= (TextView) view.findViewById(R.id.value_details_coin_blockchain);
        tvCoinBlockchain.setText(coinDisplayName);
        // address
        String coinAddress= (String) keyInfo.get("coinAddress");
        TextView tvCoinAddress= (TextView) view.findViewById(R.id.value_details_coin_address);
        tvCoinAddress.setText(coinAddress);
        String coinAddressWeburl= (String)keyInfo.get("coinAddressWeburl");
        // balance  
        String coinBalanceTxt= (String)keyInfo.get("coinBalanceTxt");
        TextView tvCoinBalance= (TextView) view.findViewById(R.id.value_details_coin_balance);
        tvCoinBalance.setText(coinBalanceTxt);
        
        // address buttons
        // COPY
        ImageButton imButtonAddressCopy= (ImageButton) view.findViewById(R.id.imbutton_address_copy);
        imButtonAddressCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "BUTTON COPY CLICKED!");
                myClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                myClip = ClipData.newPlainText("address", coinAddress);
                myClipboard.setPrimaryClip(myClip);
                Toast.makeText(getActivity().getApplicationContext(), R.string.address_copied_toast,Toast.LENGTH_SHORT).show();
            }
        });
        // QR
        ImageView imviewAddress= (ImageView) view.findViewById(R.id.imview_address_qr);
        ImageButton imButtonAddressQr= (ImageButton) view.findViewById(R.id.imbutton_address_qr);
        imButtonAddressQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "BUTTON QR CLICKED!");
                //
                QRCodeWriter writer = new QRCodeWriter();
                try {
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
            }
        });
        // BROWSE
        ImageButton imButtonAddressBrowse= (ImageButton) view.findViewById(R.id.imbutton_address_browse);
        imButtonAddressBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "BUTTON BROWSE CLICKED!");
                try{
                    String url= coinAddressWeburl;
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://" + url;
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Exception while starting action_view intent: " + e);
                    e.printStackTrace();
                }
            }
        });
        
        // token/NFT
        boolean isToken= (boolean)keyInfo.get("isToken");
        boolean isNFT= (boolean)keyInfo.get("isNFT");
        if (isToken){
            String keyContractHex= (String) keyInfo.get("keyContractHex");
            TextView tvTokenContract= (TextView) view.findViewById(R.id.value_details_token_contract);
            tvTokenContract.setText(keyContractHex);
            
            String tokenBalanceTxt= (String) keyInfo.get("tokenBalanceTxt");
            TextView tvTokenBalance= (TextView) view.findViewById(R.id.value_details_token_balance);
            tvTokenBalance.setText(tokenBalanceTxt);
            
            LinearLayout llGroupToken= (LinearLayout) view.findViewById(R.id.group_details_token_info); 
            llGroupToken.setVisibility(View.VISIBLE);
        } else if (isNFT){
            String keyContractHex= (String) keyInfo.get("keyContractHex");
            TextView tvTokenContract= (TextView) view.findViewById(R.id.value_details_nft_contract);
            tvTokenContract.setText(keyContractHex);
            
            String tokenBalanceTxt= (String) keyInfo.get("tokenBalanceTxt");
            TextView tvTokenBalance= (TextView) view.findViewById(R.id.value_details_nft_balance);
            tvTokenBalance.setText(tokenBalanceTxt);
            
            String keyTokenIdDec= (String) keyInfo.get("keyTokenIdDec");
            TextView tvNftTokenId= (TextView) view.findViewById(R.id.value_details_nft_tokenid);
            tvNftTokenId.setText(keyTokenIdDec);
            
            LinearLayout llGroupNft= (LinearLayout) view.findViewById(R.id.group_details_nft_info); 
            llGroupNft.setVisibility(View.VISIBLE);
            
            // nft data only shown if balance >0
            double tokenBalance= (double) keyInfo.get("tokenBalance");
            //if (tokenBalance>0){
            if (tokenBalance>=0){ // DEBUG: only show if balance strictly >0
                String nftName= (String) keyInfo.get("nftName");
                LinearLayout llGroupNftName= (LinearLayout) view.findViewById(R.id.group_details_nft_name); 
                if (!nftName.equals("")){
                    TextView tvNftName= (TextView) view.findViewById(R.id.value_details_nft_name);
                    tvNftName.setText(nftName);
                    llGroupNftName.setVisibility(View.VISIBLE);
                }
                
                String nftDescription= (String) keyInfo.get("nftDescription"); 
                LinearLayout llGroupNftDescription= (LinearLayout) view.findViewById(R.id.group_details_nft_description); 
                if (!nftDescription.equals("")){
                    TextView tvNftDescription= (TextView) view.findViewById(R.id.value_details_nft_description);
                    tvNftDescription.setText(nftDescription);
                    llGroupNftDescription.setVisibility(View.VISIBLE);
                }
                
                Bitmap bmpNft= (Bitmap) keyInfo.get("nftBitmap");
                ImageView imviewNft = (ImageView) view.findViewById(R.id.imview_nft_image); 
                if (bmpNft != null){
                    imviewNft.setImageBitmap(bmpNft);
                } else {
                    imviewNft.setImageDrawable(null); // https://stackoverflow.com/questions/2859212/how-to-clear-an-imageview-in-android
                }
                imviewNft.setVisibility(View.VISIBLE);
            }
        }
        
        // privkey (only if redeemed)
        if (keyState==STATE_UNSEALED){
            View layoutPrivInfo = view.findViewById(R.id.group_details_private_info);
            layoutPrivInfo.setVisibility(View.VISIBLE);
            
            // show/hide privkey info depending on button state
            llPrivInfoShowhide= (LinearLayout) view.findViewById(R.id.group_details_private_showhide_info);
            Button buttonShowhide= (Button) view.findViewById(R.id.button_showhide_info);
            buttonShowhide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "BUTTON SHOWHIDE CLICKED!");
                    buttonShowhideShow= !buttonShowhideShow;
                    if (buttonShowhideShow){
                        llPrivInfoShowhide.setVisibility(View.VISIBLE);
                        buttonShowhide.setText(R.string.button_hide);
                    } else{
                        llPrivInfoShowhide.setVisibility(View.GONE);
                        buttonShowhide.setText(R.string.button_show);
                    }
                }
            }); // end setOnClickListener
            
            //hex
            String privkeyHex= (String) keyInfo.get("privkeyHex");
            TextView tvPrivkeyHex= (TextView) view.findViewById(R.id.value_details_private_key);
            tvPrivkeyHex.setText(privkeyHex);
            // wif
            String privkeyWif= (String) keyInfo.get("privkeyWif");
            TextView tvPrivkeyWif= (TextView) view.findViewById(R.id.value_details_private_wif);
            tvPrivkeyWif.setText(privkeyWif);
            // entropy
            String entropyHex= (String) keyInfo.get("entropyHex");
            TextView tvEntropyHex= (TextView) view.findViewById(R.id.value_details_private_entropy);
            tvEntropyHex.setText(entropyHex);
            
            // buttons privkey
            // COPY
            ImageButton imButtonPrivkeyCopy= (ImageButton) view.findViewById(R.id.imbutton_privkey_copy);
            imButtonPrivkeyCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "BUTTON PRIVKEY COPY CLICKED!");
                    myClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    myClip = ClipData.newPlainText("privkey", privkeyHex);
                    myClipboard.setPrimaryClip(myClip);
                    Toast.makeText(getActivity().getApplicationContext(), R.string.privkey_copied_toast,Toast.LENGTH_SHORT).show();
                }
            });
            // QR
            imviewPrivkey= (ImageView) view.findViewById(R.id.imview_privkey_qr);
            ImageButton imButtonPrivkeyQr= (ImageButton) view.findViewById(R.id.imbutton_privkey_qr);
            imButtonPrivkeyQr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "BUTTON PRIVKEY QR CLICKED!");
                    QRCodeWriter writer = new QRCodeWriter();
                    try {
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
                }
            });
            
            // buttons WIF
            // COPY
            ImageButton imButtonWifCopy= (ImageButton) view.findViewById(R.id.imbutton_wif_copy);
            imButtonWifCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "BUTTON WIF COPY CLICKED!");
                    myClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    myClip = ClipData.newPlainText("WIF", privkeyWif);
                    myClipboard.setPrimaryClip(myClip);
                    Toast.makeText(getActivity().getApplicationContext(), R.string.wif_copied_toast,Toast.LENGTH_SHORT).show();
                }
            });
            // QR
            imviewWif= (ImageView) view.findViewById(R.id.imview_wif_qr);
            ImageButton imButtonWifQr= (ImageButton) view.findViewById(R.id.imbutton_wif_qr);
            imButtonWifQr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "BUTTON WIF QR CLICKED!");
                    QRCodeWriter writer = new QRCodeWriter();
                    try {
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
                }
            });
            
        } // end STATE_UNSEALED
               
        // build dialog
        String title= getResources().getString(R.string.show_details_title) + keyNbr;
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(title) 
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       Log.d(TAG, "ShowAuthDetailsFragment: builder.setPositiveButton - onClick");
                       // do something else?
                       dismiss();
                   }
                })
                .create();
                
        return dialog;
    }

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
