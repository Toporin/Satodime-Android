package org.satochip.satodimeapp.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.satochip.satodimeapp.BuildConfig;
import org.satochip.satodimeapp.MainActivity;
import org.satochip.satodimeapp.R;
import org.satochip.satodimeapp.SettingsDialogFragment;
import org.satochip.satodimeapp.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class SettignsActivity extends AppCompatActivity {

    private static final boolean DEBUG= BuildConfig.DEBUG;
    private static final String TAG = "SATODIME_SETTINGS";
    public static final int REQUEST_CODE=2;
    private CardView toolBar;
    private ImageView backBtn;

    private SwitchCompat darkThemeSwitch;
    Spinner spinnerLanguage, spinnerFiat;
    private String darkTheme;
    private LinearLayout okBtn;
    private SharedPreferences prefs;
    String appFiatFull;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settigns);

        initAll();

        clickListners();

        // get current language from config and set language spinner to it
        if(DEBUG) Log.d(TAG, "SettingsDialogFragment: get current language");
        prefs = getSharedPreferences("satodime", Context.MODE_PRIVATE);
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


        // Do something on language or Fiat selection?
        // Do something on dark mode switch?
        // language
       final String[] array_language = getResources().getStringArray(R.array.array_language);
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(DEBUG) Log.d(TAG, "SettingsDialogFragment: setOnItemSelectedListener languages: " + position + " " + id);
                String language= array_language[position];
                if(DEBUG) Log.d(TAG, "SettingsDialogFragment: setOnItemSelectedListener languages: " + language);
                // update fields accordingl
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                if(DEBUG) Log.d(TAG, "SettingsDialogFragment: onNothingSelected: ");
            }
        });

        final String[] array_fiat = getResources().getStringArray(R.array.array_fiat);
        spinnerFiat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(DEBUG) Log.d(TAG, "SettingsDialogFragment: setOnItemSelectedListener languages: " + position + " " + id);
                String fait= array_fiat[position];
                if(DEBUG) Log.d(TAG, "SettingsDialogFragment: setOnItemSelectedListener languages: " + fait);
                // update fields accordingly
                if (fait.equals("(none)")) {
                    MainActivity.useFiat = false;
                } else {
                    MainActivity.useFiat = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                if(DEBUG) Log.d(TAG, "SettingsDialogFragment: onNothingSelected: ");
            }
        });

    }

    private void clickListners() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DEBUG) Log.d(TAG, "onCreateDialog - builder.setPositiveButton - onClick");

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

                    MainActivity.isLanguageChanged=true;
                    recreate();


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

    @Override
    public void recreate() {
        Log.d("testActivityREs", "happended");
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        startActivity(getIntent());
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void initAll() {
        toolBar = findViewById(R.id.toolbar);
        backBtn = findViewById(R.id.back_btn);
        toolBar.setBackgroundResource(R.drawable.bottom_corer_round);
        darkThemeSwitch = findViewById(R.id.dark_theme);
        spinnerFiat = findViewById(R.id.currecy_spinner);
        spinnerLanguage = findViewById(R.id.language_spinner);
        okBtn = findViewById(R.id.ok_btn);
        darkTheme = Paper.book().read("darkmode", "off");

        if (darkTheme.equals("on")) {
            darkThemeSwitch.setChecked(true);
        } else {
            darkThemeSwitch.setChecked(false);
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