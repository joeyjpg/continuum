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

    private static final String TAG = "ClientIDPrefFragment";

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;

    public ClientIDPreferenceFragment() {}

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        PreferenceManager preferenceManager = getPreferenceManager();
        // Use default shared preferences file for client ID
        preferenceManager.setSharedPreferencesName(SharedPreferencesUtils.DEFAULT_PREFERENCES_FILE);
        setPreferencesFromResource(R.xml.client_id_preferences, rootKey);
        ((Infinity) requireActivity().getApplication()).getAppComponent().inject(this);

        EditTextPreference clientIdPref = findPreference(SharedPreferencesUtils.CLIENT_ID_PREF_KEY);

        if (clientIdPref != null) {
            // Set input type to visible password to prevent suggestions, but allow any string
            clientIdPref.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                editText.setSingleLine(true);
            });

            clientIdPref.setOnPreferenceChangeListener(((preference, newValue) -> {
                String value = (String) newValue;

                // Validate length: must be exactly 22 characters
                if (value.length() != 22) {
                    Toast.makeText(getContext(), R.string.client_id_length_error, Toast.LENGTH_LONG).show();
                    return false;
                }

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