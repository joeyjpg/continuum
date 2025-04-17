package ml.docilealligator.infinityforreddit.settings;

import static android.content.Intent.ACTION_OPEN_DOCUMENT_TREE;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class DownloadLocationPreferenceFragment extends CustomFontPreferenceFragmentCompat {
    private static final int IMAGE_DOWNLOAD_LOCATION_REQUEST_CODE = 10;
    private static final int GIF_DOWNLOAD_LOCATION_REQUEST_CODE = 11;
    private static final int VIDEO_DOWNLOAD_LOCATION_REQUEST_CODE = 12;
    private static final int NSFW_DOWNLOAD_LOCATION_REQUEST_CODE = 13;

    Preference imageDownloadLocationPreference;
    Preference gifDownloadLocationPreference;
    Preference videoDownloadLocationPreference;
    Preference nsfwDownloadLocationPreference;
    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        ((Infinity) activity.getApplication()).getAppComponent().inject(this);
        setPreferencesFromResource(R.xml.download_location_preferences, rootKey);

        imageDownloadLocationPreference = findPreference(SharedPreferencesUtils.IMAGE_DOWNLOAD_LOCATION);
        gifDownloadLocationPreference = findPreference(SharedPreferencesUtils.GIF_DOWNLOAD_LOCATION);
        videoDownloadLocationPreference = findPreference(SharedPreferencesUtils.VIDEO_DOWNLOAD_LOCATION);
        nsfwDownloadLocationPreference = findPreference(SharedPreferencesUtils.NSFW_DOWNLOAD_LOCATION);

        if (nsfwDownloadLocationPreference != null) {
            String downloadLocation = sharedPreferences.getString(SharedPreferencesUtils.NSFW_DOWNLOAD_LOCATION, "");
            if (!downloadLocation.equals("")) {
                nsfwDownloadLocationPreference.setSummary(formatDownloadPath(downloadLocation));
            }

            nsfwDownloadLocationPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, NSFW_DOWNLOAD_LOCATION_REQUEST_CODE);
                return true;
            });
        }
        if (imageDownloadLocationPreference != null) {
            String downloadLocation = sharedPreferences.getString(SharedPreferencesUtils.IMAGE_DOWNLOAD_LOCATION, "");
            if (!downloadLocation.equals("")) {
                imageDownloadLocationPreference.setSummary(formatDownloadPath(downloadLocation));
            }

            imageDownloadLocationPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, IMAGE_DOWNLOAD_LOCATION_REQUEST_CODE);
                return true;
            });
        }

        if (gifDownloadLocationPreference != null) {
            String downloadLocation = sharedPreferences.getString(SharedPreferencesUtils.GIF_DOWNLOAD_LOCATION, "");
            if (!downloadLocation.equals("")) {
                gifDownloadLocationPreference.setSummary(formatDownloadPath(downloadLocation));
            }

            gifDownloadLocationPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, GIF_DOWNLOAD_LOCATION_REQUEST_CODE);
                return true;
            });
        }

        if (videoDownloadLocationPreference != null) {
            String downloadLocation = sharedPreferences.getString(SharedPreferencesUtils.VIDEO_DOWNLOAD_LOCATION, "");
            if (!downloadLocation.equals("")) {
                videoDownloadLocationPreference.setSummary(formatDownloadPath(downloadLocation));
            }

            videoDownloadLocationPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, VIDEO_DOWNLOAD_LOCATION_REQUEST_CODE);
                return true;
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == IMAGE_DOWNLOAD_LOCATION_REQUEST_CODE) {
                activity.getContentResolver().takePersistableUriPermission(data.getData(),
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sharedPreferences.edit().putString(SharedPreferencesUtils.IMAGE_DOWNLOAD_LOCATION, data.getDataString()).apply();
                if (imageDownloadLocationPreference != null) {
                    imageDownloadLocationPreference.setSummary(formatDownloadPath(data.getDataString()));
                }
            } else if (requestCode == GIF_DOWNLOAD_LOCATION_REQUEST_CODE) {
                activity.getContentResolver().takePersistableUriPermission(data.getData(),
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sharedPreferences.edit().putString(SharedPreferencesUtils.GIF_DOWNLOAD_LOCATION, data.getDataString()).apply();
                if (gifDownloadLocationPreference != null) {
                    gifDownloadLocationPreference.setSummary(formatDownloadPath(data.getDataString()));
                }
            } else if (requestCode == VIDEO_DOWNLOAD_LOCATION_REQUEST_CODE) {
                activity.getContentResolver().takePersistableUriPermission(data.getData(),
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sharedPreferences.edit().putString(SharedPreferencesUtils.VIDEO_DOWNLOAD_LOCATION, data.getDataString()).apply();
                if (videoDownloadLocationPreference != null) {
                    videoDownloadLocationPreference.setSummary(formatDownloadPath(data.getDataString()));
                }
            } else if (requestCode == NSFW_DOWNLOAD_LOCATION_REQUEST_CODE) {
                activity.getContentResolver().takePersistableUriPermission(data.getData(),
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sharedPreferences.edit().putString(SharedPreferencesUtils.NSFW_DOWNLOAD_LOCATION, data.getDataString()).apply();
                if (nsfwDownloadLocationPreference != null) {
                    nsfwDownloadLocationPreference.setSummary(formatDownloadPath(data.getDataString()));
                }
            }
        }
    }

    private String formatDownloadPath(String uriString) {
        if (uriString == null || uriString.isEmpty()) {
            return "";
        }
        String prefix = "content://com.android.externalstorage.documents/tree/primary%3A";
        if (uriString.startsWith(prefix)) {
            String encodedPath = uriString.substring(prefix.length());
            try {
                // Decode URL encoding (e.g., %2F -> /, %20 -> space)
                return "/" + URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                // Fallback to original string if decoding fails
                return uriString;
            }
        }
        // Return original string if prefix doesn't match
        return uriString;
    }
}