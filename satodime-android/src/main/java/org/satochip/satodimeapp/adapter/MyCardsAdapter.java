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
import org.satochip.satodimeapp.model.Card; // todo: remove
import org.satochip.satodimeapp.ui.activity.KeySlotDetailsActivity;
//import org.satochip.satodimeapp.SealFormDialogFragment;
import org.satochip.satodimeapp.MainActivity;

import java.util.Locale;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import static org.satochip.client.Constants.*;

/* Currently unused? */
public class MyCardsAdapter extends RecyclerView.Adapter<MyCardsAdapter.MyViewHolder> {
    
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = "SATODIME_CARD_ADAPTER";
    protected List<HashMap<String, Object>> keyInfoList= null;

    List<Card> notesList; // todo: remove
    Context context;

    //public MyCardsAdapter(List<Card> usersList, Context context) {
    public MyCardsAdapter(List<HashMap<String, Object>> keyInfoList, Context context) {
        //this.notesList = usersList;
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
            
            // nft or token
            boolean isToken= (boolean) keyInfo.get("isToken");
            boolean isNFT= (boolean) keyInfo.get("isNFT");
            boolean isTokenOrNFT= (isToken || isNFT);
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
                //todo: check if uninitialized
                Intent intent = new Intent(context, KeySlotDetailsActivity.class);
                //intent.putExtra("position", position);
                intent.putExtra("keyInfo", keyInfo);
                context.startActivity(intent);
            }
        });

        holder.cardStatusImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DEBUG) Log.d(TAG,"Clicked on cardStatusImg!");
                boolean isOwner= true; // todo
                if (isOwner) {
                    int keyslotNbr= position; // todo
                    if (keyState == STATE_UNINITIALIZED) { // => seal
                        if(DEBUG) Log.d(TAG,"Clicked on SEAL!");
                        ((MainActivity)context).showSealDialog(position);
                        // DialogFragment dialog = new SealFormDialogFragment();
                        // dialog.show(context.getSupportFragmentManager(), "SealFormDialogFragment");
                        
                    } else if (keyState == STATE_SEALED) { // => unseal
                        if(DEBUG) Log.d(TAG,"Clicked on UNSEAL!");
                        ((MainActivity)context).showUnsealDialog(position);
                        /* final AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
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
                                ((MainActivity)context).sendUnsealKeyslotApduToCard();
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

                        alertDialog.show(); */
                        
                    } else if (keyState == STATE_UNSEALED){ // => reset
                        if(DEBUG) Log.d(TAG,"Clicked on RESET!");
                        ((MainActivity)context).showResetDialog(position);
                        /* final AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this, R.style.CustomAlertDialog);
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
                                ((MainActivity)context).sendResetKeyslotApduToCard();
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
                        alertDialog.show(); */
                    }
                } else {
                    //todo: toast: not owner!
                }
            } // end onClick()
        });

        /*
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, KeySlotDetailsActivity.class);
                intent.putExtra("position", position);
                context.startActivity(intent);
            }
        }); */

        /*
        holder.cardStatusImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DEBUG) Log.d(TAG,"Clicked on cardStatusImg!");
                 if (position == 2) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
                    ViewGroup viewGroup = v.findViewById(android.R.id.content);
                    View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_uninitialized, viewGroup, false);

                    TextView transferBtn = dialogView.findViewById(R.id.transfer_btn);
                    TextView cancelBtn = dialogView.findViewById(R.id.cancel_btn);
                    Spinner blockChainSpinner = dialogView.findViewById(R.id.spinner_coin_type);
                    Spinner currecySpinner = dialogView.findViewById(R.id.spinner_asset_type);
                    CheckBox textNetCheckBox = dialogView.findViewById(R.id.checkbox_use_testnet);
                    EditText entropyEt = dialogView.findViewById(R.id.edittext_entropy_input);

                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, R.layout.spinner_item, context.getResources().getStringArray(R.array.blockchain));
                    dataAdapter.setDropDownViewResource(R.layout.spinner_item);
                    blockChainSpinner.setAdapter(dataAdapter);

                    ArrayAdapter<String> currencyAdapter = new ArrayAdapter<String>(context, R.layout.spinner_item, context.getResources().getStringArray(R.array.currency));
                    currencyAdapter.setDropDownViewResource(R.layout.spinner_item);
                    currecySpinner.setAdapter(currencyAdapter);

                    builder.setView(dialogView);
                    final AlertDialog alertDialog = builder.create();
                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });

                    blockChainSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            String item = adapterView.getItemAtPosition(i).toString();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    currecySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            String item = adapterView.getItemAtPosition(i).toString();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                    alertDialog.show();
                } 
            } // end onClick()
        });*/
        
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
        TextView keyNbr, cardAddress, assetType, balance, tokenBalance; // cardName, unit
        TextView cardStatus;
        ImageView cardStatusImg, cardImg, nextBtn;
        //LinearLayout initLayout;
        LinearLayout tokenLayout;
        RelativeLayout parentLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            keyNbr = itemView.findViewById(R.id.key_number);
            assetType = itemView.findViewById(R.id.asset_type);
            cardStatusImg = itemView.findViewById(R.id.card_status_image);
            cardStatus = itemView.findViewById(R.id.card_status);
            cardImg = itemView.findViewById(R.id.card_image);
            cardAddress = itemView.findViewById(R.id.card_address);
            //cardName = itemView.findViewById(R.id.card_name);
            balance = itemView.findViewById(R.id.balance);
            tokenBalance= itemView.findViewById(R.id.token_balance);
            //unit = itemView.findViewById(R.id.unit);
            nextBtn = itemView.findViewById(R.id.next_btn);

            //initLayout = itemView.findViewById(R.id.intialized_card_layout);
            parentLayout = itemView.findViewById(R.id.parent_layout);
            tokenLayout = itemView.findViewById(R.id.token_balance_layout);
        }
    }
}
