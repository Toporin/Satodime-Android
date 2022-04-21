package org.satochip.satodimeapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.satochip.satodimeapp.R;
import org.satochip.satodimeapp.BuildConfig;
import org.satochip.satodimeapp.MainActivity;

import java.util.Locale;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import static org.satochip.client.Constants.*;

public class MyCardsAdapter extends RecyclerView.Adapter<MyCardsAdapter.MyViewHolder> {
    
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "SATODIME_CARD_ADAPTER";
    protected List<HashMap<String, Object>> keyInfoList= null;

    Context context;

    public MyCardsAdapter(List<HashMap<String, Object>> keyInfoList, Context context) {
        this.context = context;
        this.keyInfoList= keyInfoList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (DEBUG) Log.d(TAG, "in onBindViewHolder START position: " + position);
        long startTime = System.currentTimeMillis();
        
        String keyNbr= "Key #" + position;
            
        // get keyslot map from list
        HashMap<String, Object> keyInfo= this.keyInfoList.get(position);
        if (DEBUG) Log.d(TAG, "in onBindViewHolder keyInfo map: " + keyInfo);
        
        int keyState= (int) keyInfo.get("keyState");

        // state info
        if (keyState== STATE_UNINITIALIZED){
            holder.keyNbr.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gold_light));
            holder.parentLayout.setBackground(context.getDrawable(R.drawable.card_outline_gold));
            holder.cardImg.setImageResource(R.drawable.ic_coin_empty);
            holder.cardStatusImg.setImageResource(R.drawable.ic_coin_empty);
            holder.cardStatus.setText(R.string.uninitialized);
            holder.cardStatus.setTextColor(context.getResources().getColor(R.color.grey));
        } else{
            // get info
            String assetType= (String) keyInfo.get("keyAssetTxt");
            String coinName= (String) keyInfo.get("coinDisplayName");
            String coinSymbol= (String) keyInfo.get("coinSymbol");
            String balance= (String) keyInfo.get("coinBalanceTxt");
            String tokenBalance= (String) keyInfo.get("tokenBalanceTxt");
            String address= (String) keyInfo.get("coinAddress");
            // set info
            holder.assetType.setText(assetType);
            holder.keyNbr.setText(keyNbr);
            holder.balance.setText(balance);
            holder.cardAddress.setText(address);
            // set static txt (needed when language changes)
            holder.assetTypeTxt.setText(R.string.asset_type);
            holder.cardAddressTxt.setText(R.string.address);
            holder.tokenBalanceTxt.setText(R.string.token_balance);
            
            // nft or token
            boolean isTokenOrNFT= (boolean) keyInfo.getOrDefault("isTokenOrNFT", false);
            if (isTokenOrNFT){
                holder.tokenLayout.setVisibility(View.VISIBLE);
                holder.tokenBalance.setText(tokenBalance);
            }
            
            // icon
            if (coinSymbol != null) {
                int id;
                if (coinSymbol.equals("?")){
                    id= this.context.getResources().getIdentifier("ic_coin_unknown", "drawable", this.context.getPackageName());
                } else {
                    id= this.context.getResources().getIdentifier("ic_coin_"+coinSymbol.toLowerCase(Locale.ENGLISH), "drawable", this.context.getPackageName());
                }
                holder.cardImg.setImageResource(id);
            }
            
            // state
            if(keyState== STATE_SEALED) {
                holder.keyNbr.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.green));
                holder.parentLayout.setBackground(context.getDrawable(R.drawable.card_outline_green));
                holder.cardStatusImg.setImageResource(R.drawable.ic_lock);
                holder.cardStatus.setText(R.string.sealed);
                holder.cardStatus.setTextColor(context.getResources().getColor(R.color.green));
            } else if (keyState== STATE_UNSEALED) {
                holder.keyNbr.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.RED));
                holder.parentLayout.setBackground(context.getDrawable(R.drawable.card_ouline_red));
                holder.cardStatusImg.setImageResource(R.drawable.ic_unlock);
                holder.cardStatus.setText(R.string.unsealed);
                holder.cardStatus.setTextColor(context.getResources().getColor(R.color.RED));
            }
        }

        long middleTime = System.currentTimeMillis();
        
        holder.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DEBUG) Log.d(TAG,"Clicked on more details button!");
                ((MainActivity)context).showKeyslotDetailsDialog(position);
            }
        });

        holder.cardStatusImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DEBUG) Log.d(TAG,"Clicked on cardStatusImg!");
                boolean isOwner= true; // todo
                if (isOwner) {
                    if (keyState == STATE_UNINITIALIZED) { // => seal
                        if(DEBUG) Log.d(TAG,"Clicked on SEAL!");
                        ((MainActivity)context).showSealDialog(position);
                        
                    } else if (keyState == STATE_SEALED) { // => unseal
                        if(DEBUG) Log.d(TAG,"Clicked on UNSEAL!");
                        ((MainActivity)context).showUnsealDialog(position);
                        
                    } else if (keyState == STATE_UNSEALED){ // => reset
                        if(DEBUG) Log.d(TAG,"Clicked on RESET!");
                        ((MainActivity)context).showResetDialog(position);
                        
                    }
                } else {
                    //todo: toast: not owner!
                }
            } // end onClick()
        });
        
        if (DEBUG) Log.d(TAG, "Full time: " + (System.currentTimeMillis() - startTime));
        if (DEBUG) Log.d(TAG, "Last middle time: " + (System.currentTimeMillis() - middleTime));
        if (DEBUG) Log.d(TAG, "in onBindViewHolder END");
    }

    @Override
    public int getItemCount() {
        if (keyInfoList == null){ 
            if (DEBUG) Log.d(TAG, "keyInfoList is null!");
            return 0;
        }
        if (DEBUG) Log.d(TAG, "keyInfoList size: " + this.keyInfoList.size());
        return this.keyInfoList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView keyNbr, cardAddressTxt, cardAddress, assetTypeTxt, assetType, balance, tokenBalanceTxt, tokenBalance;
        TextView cardStatus;
        ImageView cardStatusImg, cardImg, nextBtn;
        LinearLayout tokenLayout;
        RelativeLayout parentLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            keyNbr = itemView.findViewById(R.id.key_number);
            assetTypeTxt = itemView.findViewById(R.id.asset_type_txt);
            assetType = itemView.findViewById(R.id.asset_type);
            cardStatusImg = itemView.findViewById(R.id.card_status_image);
            cardStatus = itemView.findViewById(R.id.card_status);
            cardImg = itemView.findViewById(R.id.card_image);
            cardAddressTxt = itemView.findViewById(R.id.card_address_txt);
            cardAddress = itemView.findViewById(R.id.card_address);
            balance = itemView.findViewById(R.id.balance);
            tokenBalanceTxt= itemView.findViewById(R.id.token_balance_txt);
            tokenBalance= itemView.findViewById(R.id.token_balance);
            nextBtn = itemView.findViewById(R.id.next_btn);

            parentLayout = itemView.findViewById(R.id.parent_layout);
            tokenLayout = itemView.findViewById(R.id.token_balance_layout);
        }
    }
}
