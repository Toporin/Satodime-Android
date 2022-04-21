package org.satochip.satodimeapp.adapter;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
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

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.satochip.satodimeapp.R;
import org.satochip.satodimeapp.BuildConfig;
import org.satochip.satodimeapp.MainActivity;
import org.satochip.satodimeapp.ui.fragment.SettingsFragment;

public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.NavigationHolder> {
    
    private static final boolean DEBUG= BuildConfig.DEBUG;
    private static final String TAG = "SATODIME_NAVIGATION";
    
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
    
    public void updateMenuTitle(){
        menu_title= homeActivity.getResources().getStringArray(R.array.array_menu);
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
                    Log.d(TAG, navigationHolder.item_text.getText() + "");
                    Log.d(TAG, selectedItem + "");
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
                selectedItem= menu_title[i];
                setFilter(navigationHolder, i);
                
                // watch out: this switch depends implicitly on the order of items in menu_title!
                switch(i){
                    case 0: // home
                        break;
                    case 1: // card info
                        homeActivity.showCardInfoDialog();
                        break;
                    case 2: // transfer card
                        homeActivity.showTransferDialog();
                        break;
                    case 3: //settings
                        DialogFragment settingsFragment = new SettingsFragment();
                        settingsFragment.show(homeActivity.getSupportFragmentManager(), "SettingsFragment");
                        break;
                    case 4: // share
                        Intent sendIntent= new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, homeActivity.getResources().getString(R.string.share_with_friends)); 
                        sendIntent.putExtra(Intent.EXTRA_TEXT, homeActivity.getResources().getString(R.string.share_details) + BuildConfig.APPLICATION_ID);       
                        sendIntent.setType("text/plain");
                        homeActivity.startActivity(sendIntent);
                        break;
                    case 5: // FAQ
                        Intent browserIntent= new Intent(Intent.ACTION_VIEW, Uri.parse(homeActivity.getResources().getString(R.string.faq_link)));
                        homeActivity.startActivity(browserIntent);
                        break;
                    default:
                        break;
                }

                homeActivity.closeDrawer();
            }
        });
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