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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.satochip.satodimeapp.R;
import org.satochip.satodimeapp.model.Card;
import org.satochip.satodimeapp.ui.activity.KeySlotDetailsActivity;

import java.util.List;

public class MyCardsAdapter extends RecyclerView.Adapter<MyCardsAdapter.MyViewHolder> {

    List<Card> notesList;
    Context context;


    public MyCardsAdapter(List<Card> usersList, Context context) {
        this.notesList = usersList;
        this.context = context;
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

        if (position == 0) {
            holder.assestType.setText("NFT");
            holder.key.setText("Kay # 0");
            holder.key.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.green));
            holder.parentLayout.setBackground(context.getDrawable(R.drawable.card_outline_green));
            holder.cardImg.setImageResource(R.drawable.ic_etherium);
            holder.cardStatusImg.setImageResource(R.drawable.ic_lock);
            holder.cardName.setText("Ethereum");
            holder.cardStatus.setText("Sealed");
            holder.cardStatus.setTextColor(context.getResources().getColor(R.color.green));
        } else if (position == 1) {
            holder.assestType.setText("Coin");
            holder.key.setText("Kay # 1");
            holder.key.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.RED));
            holder.parentLayout.setBackground(context.getDrawable(R.drawable.card_ouline_red));
            holder.cardImg.setImageResource(R.drawable.ic_bitcoin);
            holder.cardStatusImg.setImageResource(R.drawable.ic_unlock);
            holder.cardName.setText("Bitcoin Cash");
            holder.cardStatus.setText("Unsealed");
            holder.cardStatus.setTextColor(context.getResources().getColor(R.color.RED));
        } else {
            holder.assestType.setText("Uninitialized");
            holder.key.setText("Kay # 2");
            holder.key.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.gold_light));
            holder.parentLayout.setBackground(context.getDrawable(R.drawable.card_outline_gold));
            holder.cardImg.setImageResource(R.drawable.ic_hourglass);
            holder.cardStatusImg.setImageResource(R.drawable.ic_hourglass);
            holder.cardStatus.setText("Uninitialized");
            holder.cardStatus.setTextColor(context.getResources().getColor(R.color.grey));
            holder.initLayout.setVisibility(View.GONE);
            holder.unInitLayout.setVisibility(View.VISIBLE);
        }

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, KeySlotDetailsActivity.class);
                intent.putExtra("position", position);
                context.startActivity(intent);
            }
        });

        holder.cardStatusImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("clickHers","ok");
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
            }
        });

    }


    @Override
    public int getItemCount() {
        return 3;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView key, assestType, cardName, balance, unit;
        ImageView cardStatusImg;
        TextView cardStatus;
        ImageView cardImg, nextBtn;
        LinearLayout initLayout, unInitLayout;
        TextView cardAddress;
        RelativeLayout parentLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            key = itemView.findViewById(R.id.key_number);
            assestType = itemView.findViewById(R.id.asset_type);
            cardStatusImg = itemView.findViewById(R.id.card_status_image);
            cardStatus = itemView.findViewById(R.id.card_status);
            cardImg = itemView.findViewById(R.id.card_image);
            cardAddress = itemView.findViewById(R.id.card_address);
            cardName = itemView.findViewById(R.id.card_name);
            balance = itemView.findViewById(R.id.balance);
            unit = itemView.findViewById(R.id.unit);
            nextBtn = itemView.findViewById(R.id.next_btn);

            initLayout = itemView.findViewById(R.id.intialized_card_layout);
            unInitLayout = itemView.findViewById(R.id.unintialized_card_layout);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}
