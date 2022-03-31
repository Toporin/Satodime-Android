package org.satochip.satodimeapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.app.AlertDialog;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch ;
import android.widget.ArrayAdapter;

import androidx.fragment.app.DialogFragment;

import java.util.Locale;

public class SettingsDialogFragment extends DialogFragment {
    
    private static final boolean DEBUG= BuildConfig.DEBUG;
    private static final String TAG = "SATODIME_SETTINGS";
    public static final int RESULT_OK=1;
    public static final int RESULT_CANCELLED=0;
    public static final int REQUEST_CODE=2;
        
    private Spinner spinnerLanguage;
    private Spinner spinnerFiat;
    
    // based on https://developer.android.com/guide/topics/ui/dialogs
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(DEBUG) Log.d(TAG, "SettingsDialogFragment: onCreateDialog"); 
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view= inflater.inflate(R.layout.activity_settings, null);
        
        // widgets
        spinnerLanguage= (Spinner) view.findViewById(R.id.spinner_language);
        spinnerFiat= (Spinner) view.findViewById(R.id.spinner_fiat);
        
        // get current language from config and set language spinner to it
        if(DEBUG) Log.d(TAG, "SettingsDialogFragment: get current language"); 
        SharedPreferences prefs = getActivity().getSharedPreferences("satodime", Context.MODE_PRIVATE);
        String appLanguage = prefs.getString("appLanguage", Locale.getDefault().getLanguage());
        String language= convertLocaleToLanguageString(appLanguage);
        ArrayAdapter adapter= (ArrayAdapter) spinnerLanguage.getAdapter();
        spinnerLanguage.setSelection(adapter.getPosition(language));
        if(DEBUG) Log.d(TAG, "SettingsDialogFragment: get current language: " + language); 
        
        // get current fiat from config and set language spinner to it
        String appFiatFull = prefs.getString("appFiatFull", "(none)");
        ArrayAdapter adapterFiat= (ArrayAdapter) spinnerFiat.getAdapter();
        spinnerFiat.setSelection(adapterFiat.getPosition(appFiatFull));

        
        // Do something on language or Fiat selection?
        // language 
        final String[] array_language = getResources().getStringArray(R.array.array_language);
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(DEBUG) Log.d(TAG, "SettingsDialogFragment: setOnItemSelectedListener languages: " + position + " " + id); 
                String language= array_language[position];
                if(DEBUG) Log.d(TAG, "SettingsDialogFragment: setOnItemSelectedListener languages: " + language); 
                // update fields accordingly
                // TODO
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                if(DEBUG) Log.d(TAG, "SettingsDialogFragment: onNothingSelected: "); 
            }
        });
        
        // build dialog
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.settings_title) 
                // Add action buttons
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       SettingsDialogFragment.this.getDialog().cancel();
                       if(DEBUG) Log.d(TAG, "onCreateDialog - builder.setNegativeButton - onClick");
                       listener.onDialogNegativeClick(SettingsDialogFragment.this, REQUEST_CODE, RESULT_CANCELLED);
                   }
                })
                .setPositiveButton(R.string.settings_confirm, null)
                .create();
                
                // https://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            
                @Override
                public void onShow(DialogInterface dialogInterface) {

                    Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        
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

                                
                                Intent resultIntent= new Intent();
                                resultIntent.putExtra("appFiat", appFiat);
                                resultIntent.putExtra("appFiatFull", appFiatFull);
                                resultIntent.putExtra("appLanguage", appLanguage);
                                listener.onDialogPositiveClick(SettingsDialogFragment.this, REQUEST_CODE, RESULT_OK, resultIntent);
                                dialog.dismiss();
                            } catch (Exception e) {
                                if(DEBUG) Log.e(TAG, e.getMessage());
                                // TODO remove
                            } 
                            
                        }
                    });
                }
            });
        
        //return builder.create();
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
    
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface SettingsDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, int requestCode, int resultCode, Intent intent);
        public void onDialogNegativeClick(DialogFragment dialog, int requestCode, int resultCode);
    }

    // Use this instance of the interface to deliver action events
    SettingsDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the SettingsDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        if(DEBUG) Log.d(TAG, "onAttach");
        try {
            // Instantiate the SettingsDialogListener so we can send events to the host
            listener = (SettingsDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            //throw new ClassCastException(activity.toString() + " must implement SettingsDialogListener");
            throw new ClassCastException("ClassCastException: must implement SettingsDialogListener");
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
