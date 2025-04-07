package ml.docilealligator.infinityforreddit.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log; // Added import for Log
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceManager;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class ClientIDPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    private static final String TAG = "ClientIDPrefFragment"; // Added TAG for logging

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;

    public ClientIDPreferenceFragment() {}

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        PreferenceManager preferenceManager = getPreferenceManager();
        // Use default shared preferences file for client ID
        preferenceManager.setSharedPreferencesName(SharedPreferencesUtils.DEFAULT_PREFERENCES_FILE); // Corrected constant
        setPreferencesFromResource(R.xml.client_id_preferences, rootKey); // We'll create this XML next
        ((Infinity) requireActivity().getApplication()).getAppComponent().inject(this);

        EditTextPreference clientIdPref = findPreference(SharedPreferencesUtils.CLIENT_ID_PREF_KEY); // We'll add this constant later

        if (clientIdPref != null) {
            // Set input type to visible password to prevent suggestions, but allow any string
            clientIdPref.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                editText.setSingleLine(true);
            });

            // Basic validation: ensure it's not empty (optional, can be removed if empty is allowed)
            clientIdPref.setOnPreferenceChangeListener(((preference, newValue) -> {
                String value = (String) newValue;
                Log.d(TAG, "Client ID preference changing. New value: '" + value + "'"); // Added log
                if (value == null || value.trim().isEmpty()) {
                    Log.w(TAG, "Client ID value is empty or null."); // Added log for empty case
                    // Optionally show a toast or prevent saving if empty is not allowed
                    // Toast.makeText(activity, R.string.client_id_cannot_be_empty, Toast.LENGTH_SHORT).show();
                    // return false; // Uncomment this line if empty Client ID should not be saved
                }
                // No complex validation needed like hostname/IP for Client ID
                Log.d(TAG, "Client ID validation passed, allowing change."); // Added log
                return true;
            }));
        }
    }

    // Inject dependencies
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((Infinity) requireActivity().getApplication()).getAppComponent().inject(this);
    }
}