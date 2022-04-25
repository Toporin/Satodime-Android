package org.satochip.satodimeapp.ui.fragment;

import android.app.Dialog;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import org.satochip.satodimeapp.BuildConfig;
import org.satochip.satodimeapp.DialogListener;	
import org.satochip.satodimeapp.MainActivity;
import org.satochip.satodimeapp.R;
import org.satochip.satodimeapp.Utils;
import static org.satochip.satodimeapp.Constants.*;	

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class SettingsFragment extends DialogFragment {

    private static final boolean DEBUG= BuildConfig.DEBUG;
    private static final String TAG = "SATODIME_SETTINGS";
    
    // Use this instance of the interface to deliver action events
    private DialogListener listener;
    
    private CardView toolBar;
    private ImageView backBtn;
    private SwitchCompat darkThemeSwitch;
    private Spinner spinnerLanguage, spinnerFiat;
    private String darkTheme;
    private LinearLayout okBtn;
    private SharedPreferences prefs;
    private String appFiatFull;

    @Override	
    public Dialog onCreateDialog(Bundle savedInstanceState) {	
        // Get the layout inflater	
        LayoutInflater inflater = requireActivity().getLayoutInflater();	
        // Inflate and set the layout for the dialog	
        // Pass null as the parent view because its going in the dialog layout	
        View view= inflater.inflate(R.layout.activity_settings, null);	
        
        toolBar = view.findViewById(R.id.toolbar);
        backBtn = view.findViewById(R.id.back_btn);
        toolBar.setBackgroundResource(R.drawable.bottom_corer_round);
        darkThemeSwitch = view.findViewById(R.id.dark_theme);
        spinnerFiat = view.findViewById(R.id.currecy_spinner);
        spinnerLanguage = view.findViewById(R.id.language_spinner);
        okBtn = view.findViewById(R.id.ok_btn);
        darkTheme = Paper.book().read("darkmode", "off"); // todo: use paper or prefs?

        if (darkTheme.equals("on")) {
            darkThemeSwitch.setChecked(true);
        } else {
            darkThemeSwitch.setChecked(false);
        }

        // get current language from config and set language spinner to it
        if(DEBUG) Log.d(TAG, "SettingsDialogFragment: get current language");
        prefs = getActivity().getSharedPreferences("satodime", Context.MODE_PRIVATE);
        String appLanguage = prefs.getString("appLanguage", Locale.getDefault().getLanguage());
        String language= convertLocaleToLanguageString(appLanguage);
        ArrayAdapter adapter= (ArrayAdapter) spinnerLanguage.getAdapter();
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerLanguage.setSelection(adapter.getPosition(language));
        if(DEBUG) Log.d(TAG, "SettingsDialogFragment: get current language: " + language);

        // get current fiat from config and set language spinner to it
        appFiatFull = prefs.getString("appFiatFull", "(none)");
        ArrayAdapter adapterFiat= (ArrayAdapter) spinnerFiat.getAdapter();
        adapterFiat.setDropDownViewResource(R.layout.spinner_item);
        spinnerFiat.setSelection(adapterFiat.getPosition(appFiatFull));
        
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
                getDialog().dismiss();
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DEBUG) Log.d(TAG, "onCreateDialog - apply - onClick");

                try{
                    // check that all required data is provided
                    String selected_language= spinnerLanguage.getSelectedItem().toString();
                    String selected_fiat= spinnerFiat.getSelectedItem().toString();
                    if(DEBUG) Log.d(TAG, "onCreateDialog - selected_language: " + selected_language);
                    if(DEBUG) Log.d(TAG, "onCreateDialog - selected_fiat: " + selected_fiat);

                    // update locale in configuration
                    String appLanguage= convertLangageToLocaleString(selected_language);
                    Locale locale = new Locale(appLanguage);
                    Locale.setDefault(locale);
                    Configuration config = getResources().getConfiguration();
                    config.locale = locale;
                    getResources().updateConfiguration(config, getResources().getDisplayMetrics());
                    if(DEBUG) Log.d(TAG, "onCreateDialog - updated in config: " + appLanguage);
                    // update preferences
                    //prefs = getActivity().getSharedPreferences("satodime", Context.MODE_PRIVATE);
                    prefs.edit().putString("appLanguage", appLanguage).apply();
                    if(DEBUG) Log.d(TAG, "onCreateDialog - saved in preferences: " + appLanguage);

                    // update fiat in preferences
                    String appFiat= selected_fiat.split(" ")[0];
                    prefs.edit().putString("appFiat", appFiat).apply();
                    prefs.edit().putString("appFiatFull", selected_fiat).apply();
                    if(DEBUG) Log.d(TAG, "onCreateDialog - saved in preferences: " + appFiat);

                    // return results
                    Intent resultIntent= new Intent();
                    resultIntent.putExtra("appFiat", appFiat);
                    resultIntent.putExtra("appFiatFull", appFiatFull);
                    resultIntent.putExtra("appLanguage", appLanguage);
                    listener.onDialogPositiveClick(SettingsFragment.this, REQUEST_CODE_SETTINGS, RESULT_OK, resultIntent);
                    getDialog().dismiss();

                } catch (Exception e) {
                    if(DEBUG) Log.e(TAG, e.getMessage());
                    // TODO remove
                }
            }
        });

        darkThemeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AppCompatDelegate
                            .setDefaultNightMode(
                                    AppCompatDelegate
                                            .MODE_NIGHT_YES);
                    Paper.book().write("darkmode", "on");
                    Utils.isDark = true;
                } else {
                    AppCompatDelegate
                            .setDefaultNightMode(
                                    AppCompatDelegate
                                            .MODE_NIGHT_NO);
                    Paper.book().write("darkmode", "off");
                    Utils.isDark = false;
                }
            }
        });
    }
    
    // Override the Fragment.onAttach() method to instantiate the DialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        if(DEBUG) Log.d(TAG, "onAttach");
        try {
            // Instantiate the SettingsDialogListener so we can send events to the host
            listener = (DialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            //throw new ClassCastException(activity.toString() + " must implement DialogListener");
            throw new ClassCastException("ClassCastException: must implement DialogListener");
        }
    }

    public static String  convertLangageToLocaleString(String language){
        switch (language) {
            case "English":
                return "en";
            case "Français":
                return "fr";
            default:
                return "en";
        }
    }

    public static String  convertLocaleToLanguageString(String locale){
        switch (locale) {
            case "en":
                return "English";
            case "fr":
                return "Français";
            default:
                return "English";
        }
    }

}