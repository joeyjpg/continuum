package ml.docilealligator.infinityforreddit.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.utils.AppRestartHelper;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class APIKeysPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    private static final String TAG = "APIKeysPrefFragment";
    private static final int CLIENT_ID_LENGTH = 22;
    private static final int GIPHY_API_KEY_LENGTH = 32;

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;

    public APIKeysPreferenceFragment() {}

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        PreferenceManager preferenceManager = getPreferenceManager();
        // Use default shared preferences file for client ID
        preferenceManager.setSharedPreferencesName(SharedPreferencesUtils.DEFAULT_PREFERENCES_FILE);
        setPreferencesFromResource(R.xml.api_keys_preferences, rootKey);
        ((Infinity) requireActivity().getApplication()).getAppComponent().inject(this);

        setupClientIdPreference();
        setupGiphyApiKeyPreference();
    }

    private void setupClientIdPreference() {
        EditTextPreference clientIdPref = findPreference(SharedPreferencesUtils.CLIENT_ID_PREF_KEY);
        if (clientIdPref != null) {
            // Set input type to visible password to prevent suggestions, but allow any string
            clientIdPref.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                editText.setSingleLine(true);
                // Get current and default values
                String currentValue = clientIdPref.getText();
                String defaultValue = editText.getContext().getString(R.string.default_client_id);

                // Clear the text field only if the current value is the default value
                if (currentValue == null || currentValue.isEmpty() || currentValue.equals(defaultValue)) {
                    editText.setText("");
                }
                // Otherwise, the EditText will automatically show the non-default current value

                // Setup validation for the specific length
                setupLengthValidation(editText, CLIENT_ID_LENGTH);
            });

            // Set a summary provider that hides the default value but shows custom ones
            clientIdPref.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                String currentValue = preference.getText();
                String defaultValue = preference.getContext().getString(R.string.default_client_id);
                if (currentValue == null || currentValue.isEmpty() || currentValue.equals(defaultValue)) {
                    // Show generic message if value is null, empty, or the default
                    return preference.getContext().getString(R.string.tap_to_set_client_id);
                } else {
                    // Show the actual custom client ID
                    return currentValue;
                }
            });

            clientIdPref.setOnPreferenceChangeListener(((preference, newValue) -> {
                String value = (String) newValue;

                // Final validation check (redundant due to button state, but safe)
                if (value == null || value.length() != CLIENT_ID_LENGTH) {
                    return false; // Should not happen if button logic is correct
                }

                // Manually save the preference value *before* restarting
                // Get the specific SharedPreferences instance used by the PreferenceManager
                SharedPreferences prefs = preference.getContext().getSharedPreferences(SharedPreferencesUtils.DEFAULT_PREFERENCES_FILE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(SharedPreferencesUtils.CLIENT_ID_PREF_KEY, value);
                boolean success = editor.commit(); // Use commit() for synchronous saving

                if (success) {
                    Log.i(TAG, "Client ID manually saved successfully.");
                    // Update the summary provider manually since we return false
                    preference.setSummaryProvider(clientIdPref.getSummaryProvider()); // Re-set to trigger update
                    AppRestartHelper.triggerAppRestart(requireContext()); // Use the helper
                } else {
                    Log.e(TAG, "Failed to save Client ID manually.");
                    Toast.makeText(getContext(), "Error saving Client ID.", Toast.LENGTH_SHORT).show();
                    // Don't restart if save failed
                }

                // Return false because we handled the saving manually (or attempted to)
                return false;
            }));
        } else {
            Log.e(TAG, "Could not find Client ID preference: " + SharedPreferencesUtils.CLIENT_ID_PREF_KEY);
        }
    }

    private void setupGiphyApiKeyPreference() {
        EditTextPreference giphyApiKeyPref = findPreference(SharedPreferencesUtils.GIPHY_API_KEY_PREF_KEY);
        if (giphyApiKeyPref != null) {
            // Set input type to visible password to prevent suggestions
            giphyApiKeyPref.setOnBindEditTextListener(editText -> {
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                editText.setSingleLine(true);
                // No need to clear the text field like for client ID, as there's no "default" shown

                // Setup validation for the specific length
                setupLengthValidation(editText, GIPHY_API_KEY_LENGTH);
            });

            // Set a summary provider that hides the default value but shows custom ones
            giphyApiKeyPref.setSummaryProvider((Preference.SummaryProvider<EditTextPreference>) preference -> {
                String currentValue = preference.getText();
                // Use the default Giphy key from resources as the "default" for comparison, even though we don't display it.
                String defaultValue = preference.getContext().getString(R.string.default_giphy_api_key); // Need to add this string resource

                if (currentValue == null || currentValue.isEmpty() || currentValue.equals(defaultValue)) {
                    // Show generic message if value is null, empty, or the (unseen) default
                    return preference.getContext().getString(R.string.tap_to_set_giphy_api_key);
                } else {
                    // Show the actual custom Giphy API Key
                    return currentValue;
                }
            });

            giphyApiKeyPref.setOnPreferenceChangeListener(((preference, newValue) -> {
                String value = (String) newValue;

                // Final validation check (redundant due to button state, but safe)
                if (value == null || value.length() != GIPHY_API_KEY_LENGTH) {
                    // Also allow empty string to revert to default
                    if (value != null && value.isEmpty()) {
                         // Handled below
                    } else {
                        Toast.makeText(getContext(), R.string.giphy_api_key_length_error, Toast.LENGTH_SHORT).show();
                        return false; // Prevent saving if invalid length (and not empty)
                    }
                }

                // Allow empty string to revert to default
                if (value == null) {
                    value = ""; // Treat null as empty
                }

                // Get the specific SharedPreferences instance used by the PreferenceManager
                SharedPreferences prefs = preference.getContext().getSharedPreferences(SharedPreferencesUtils.DEFAULT_PREFERENCES_FILE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(SharedPreferencesUtils.GIPHY_API_KEY_PREF_KEY, value);
                boolean success = editor.commit(); // Use commit() for synchronous saving

                if (success) {
                    Log.i(TAG, "Giphy API Key saved successfully.");
                    // Re-set the SummaryProvider to trigger a summary update
                    preference.setSummaryProvider(giphyApiKeyPref.getSummaryProvider());
                    Toast.makeText(getContext(), "Giphy API Key saved.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to save Giphy API Key.");
                    Toast.makeText(getContext(), "Error saving Giphy API Key.", Toast.LENGTH_SHORT).show();
                }

                // Return false because we handle the saving and summary update manually
                return true;
            }));

        } else {
            Log.e(TAG, "Could not find Giphy API Key preference: " + SharedPreferencesUtils.GIPHY_API_KEY_PREF_KEY);
        }
    }

    // Reusable helper method for setting up length validation on an EditTextPreference dialog
    private void setupLengthValidation(android.widget.EditText editText, final int requiredLength) {
        // Add TextWatcher to enable/disable OK button based on length
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                View rootView = editText.getRootView();
                if (rootView != null) {
                    Button positiveButton = rootView.findViewById(android.R.id.button1);
                    if (positiveButton != null) {
                        boolean isEnabled = s.length() == requiredLength || (requiredLength == GIPHY_API_KEY_LENGTH && s.length() == 0); // Allow empty for Giphy
                        positiveButton.setEnabled(isEnabled);
                        positiveButton.setAlpha(isEnabled ? 1.0f : 0.5f); // Adjust alpha for visual feedback
                    } else {
                        Log.w(TAG, "Could not find positive button (android.R.id.button1) in dialog root view (afterTextChanged).");
                    }
                } else {
                    Log.w(TAG, "Could not get root view from EditText to find positive button (afterTextChanged).");
                }
            }
        });

        // Ensure the button state is correct initially based on the current value
        editText.post(() -> {
            View rootView = editText.getRootView();
            if (rootView != null) {
                Button positiveButton = rootView.findViewById(android.R.id.button1);
                if (positiveButton != null) {
                    boolean isEnabled = editText.getText().length() == requiredLength || (requiredLength == GIPHY_API_KEY_LENGTH && editText.getText().length() == 0); // Allow empty for Giphy
                    positiveButton.setEnabled(isEnabled);
                    positiveButton.setAlpha(isEnabled ? 1.0f : 0.5f);
                } else {
                    Log.w(TAG, "Could not find positive button (android.R.id.button1) in dialog root view (post).");
                }
            } else {
                Log.w(TAG, "Could not get root view from EditText to find positive button (post).");
            }
        });
    }

    // Inject dependencies
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((Infinity) requireActivity().getApplication()).getAppComponent().inject(this);
    }
}
