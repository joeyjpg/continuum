package ml.docilealligator.infinityforreddit.settings;

import android.content.Context;
import android.content.Intent;
import android.content.Context;
import android.content.Intent;
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
                // Get current and default values
                String currentValue = clientIdPref.getText();
                String defaultValue = editText.getContext().getString(R.string.default_client_id);

                // Clear the text field only if the current value is the default value
                if (currentValue == null || currentValue.isEmpty() || currentValue.equals(defaultValue)) {
                    editText.setText("");
                }
                // Otherwise, the EditText will automatically show the non-default current value

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
                                boolean isEnabled = s.length() == 22;
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

                // Ensure the button is initially disabled since the text is empty.
                // Post this to run after the dialog view is likely inflated.
                editText.post(() -> {
                    View rootView = editText.getRootView();
                    if (rootView != null) {
                        Button positiveButton = rootView.findViewById(android.R.id.button1);
                        if (positiveButton != null) {
                            // Set initial state based on the (cleared) text
                            boolean isEnabled = editText.getText().length() == 22;
                            positiveButton.setEnabled(isEnabled);
                            positiveButton.setAlpha(isEnabled ? 1.0f : 0.5f); // Adjust alpha for visual feedback
                        } else {
                            Log.w(TAG, "Could not find positive button (android.R.id.button1) in dialog root view (post).");
                        }
                    } else {
                        Log.w(TAG, "Could not get root view from EditText to find positive button (post).");
                    }
                });
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
                if (value == null || value.length() != 22) {
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
                    triggerAppRestart(requireContext()); // Use simpler restart method
                } else {
                    Log.e(TAG, "Failed to save Client ID manually.");
                    Toast.makeText(getContext(), "Error saving Client ID.", Toast.LENGTH_SHORT).show();
                    // Don't restart if save failed
                }

                // Return false because we handled the saving manually (or attempted to)
                return false;
            }));
        }
    }

    // Inject dependencies
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((Infinity) requireActivity().getApplication()).getAppComponent().inject(this);
    }

    // Simplified restart method using Intent flags
    private void triggerAppRestart(Context context) {
        try {
            Context appContext = context.getApplicationContext();
            Intent intent = appContext.getPackageManager().getLaunchIntentForPackage(appContext.getPackageName());
            if (intent != null) {
                // Clear the activity stack and start the launch activity as a new task.
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                appContext.startActivity(intent);
                Log.i(TAG, "Triggering app restart via Intent.");

                // Finish the current settings activity stack
                if (getActivity() != null) {
                    getActivity().finishAffinity();
                } else {
                    // Fallback if activity context is somehow lost, less clean exit
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            } else {
                Log.e(TAG, "Could not get launch intent for package to trigger restart.");
                Toast.makeText(context, "Client ID updated. Please restart the app manually.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error triggering app restart", e);
            Toast.makeText(context, "Client ID updated. Please restart the app manually.", Toast.LENGTH_LONG).show();
        }
    }
}
// Removed old scheduleAppRestart method
