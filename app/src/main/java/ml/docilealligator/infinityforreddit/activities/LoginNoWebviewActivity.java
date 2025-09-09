package ml.docilealligator.infinityforreddit.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.FetchMyInfo;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.asynctasks.ParseAndInsertNewAccount;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivityLoginNoWebviewBinding;
import ml.docilealligator.infinityforreddit.events.NewUserLoggedInEvent;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginNoWebviewActivity extends BaseActivity {

    @Inject @Named("no_oauth") Retrofit mRetrofit;
    @Inject @Named("oauth") Retrofit mOauthRetrofit;
    @Inject RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject @Named("default") SharedPreferences mSharedPreferences;
    @Inject @Named("current_account") SharedPreferences mCurrentAccountSharedPreferences;
    @Inject CustomThemeWrapper mCustomThemeWrapper;
    @Inject Executor mExecutor;

    private ActivityLoginNoWebviewBinding binding;
    private EditText editTextRedirect;
    private Button buttonSubmitRedirect;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        binding = ActivityLoginNoWebviewBinding.inflate(getLayoutInflater());

        try {
            setContentView(binding.getRoot());
        } catch (InflateException ie) {
            Log.e("LoginNoWebviewActivity", "Failed to inflate: " + ie.getMessage());
            Toast.makeText(this, R.string.no_system_webview_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        applyCustomTheme();

        // Inflate extra views for paste URL input
        editTextRedirect = binding.editTextRedirect;
        buttonSubmitRedirect = binding.buttonSubmitRedirect;

        // 1. Build OAuth URL
        Uri baseUri = Uri.parse(APIUtils.OAUTH_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter(APIUtils.CLIENT_ID_KEY, APIUtils.getClientId(getApplicationContext()));
        uriBuilder.appendQueryParameter(APIUtils.RESPONSE_TYPE_KEY, APIUtils.RESPONSE_TYPE);
        uriBuilder.appendQueryParameter(APIUtils.STATE_KEY, APIUtils.STATE);
        uriBuilder.appendQueryParameter(APIUtils.REDIRECT_URI_KEY, APIUtils.REDIRECT_URI);
        uriBuilder.appendQueryParameter(APIUtils.DURATION_KEY, APIUtils.DURATION);
        uriBuilder.appendQueryParameter(APIUtils.SCOPE_KEY, APIUtils.SCOPE);
        String oauthUrl = uriBuilder.toString();

        // 2. Copy OAuth URL to clipboard for user to open in browser
        binding.fabLoginActivity.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Reddit OAuth URL", oauthUrl);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, "OAuth URL copied. Open in browser, log in, then copy redirected URL.", Toast.LENGTH_LONG).show();
        });

        // 3. Handle user pasting the redirected URL
        buttonSubmitRedirect.setOnClickListener(v -> {
            String redirectUrl = editTextRedirect.getText().toString().trim();
            if (redirectUrl.isEmpty()) {
                Toast.makeText(this, "Paste the redirected URL here.", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri uri = Uri.parse(redirectUrl);
            String code = uri.getQueryParameter("code");
            String state = uri.getQueryParameter("state");

            if (code != null && APIUtils.STATE.equals(state)) {
                exchangeCodeForToken(code);
            } else {
                Toast.makeText(this, "Invalid URL or login cancelled.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void exchangeCodeForToken(String authCode) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.GRANT_TYPE_KEY, "authorization_code");
        params.put("code", authCode);
        params.put(APIUtils.REDIRECT_URI_KEY, APIUtils.REDIRECT_URI);

        RedditAPI api = mRetrofit.create(RedditAPI.class);
        Call<String> accessTokenCall = api.getAccessToken(APIUtils.getHttpBasicAuthHeader(getApplicationContext()), params);
        accessTokenCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body());
                        String accessToken = json.getString(APIUtils.ACCESS_TOKEN_KEY);
                        String refreshToken = json.getString(APIUtils.REFRESH_TOKEN_KEY);

                        FetchMyInfo.fetchAccountInfo(mExecutor, new android.os.Handler(), mOauthRetrofit,
                                mRedditDataRoomDatabase, accessToken,
                                new FetchMyInfo.FetchMyInfoListener() {
                                    @Override
                                    public void onFetchMyInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma, boolean isMod) {
                                        mCurrentAccountSharedPreferences.edit()
                                                .putString(SharedPreferencesUtils.ACCESS_TOKEN, accessToken)
                                                .putString(SharedPreferencesUtils.ACCOUNT_NAME, name)
                                                .putString(SharedPreferencesUtils.ACCOUNT_IMAGE_URL, profileImageUrl)
                                                .apply();

                                        ParseAndInsertNewAccount.parseAndInsertNewAccount(
                                                mExecutor, new android.os.Handler(), name, accessToken, refreshToken,
                                                profileImageUrl, bannerImageUrl, karma, isMod, authCode,
                                                mRedditDataRoomDatabase.accountDao(),
                                                () -> {
                                                    EventBus.getDefault().post(new NewUserLoggedInEvent());
                                                    finish();
                                                });
                                    }

                                    @Override
                                    public void onFetchMyInfoFailed(boolean parseFailed) {
                                        Toast.makeText(LoginNoWebviewActivity.this,
                                                parseFailed ? R.string.parse_user_info_error : R.string.cannot_fetch_user_info,
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginNoWebviewActivity.this, R.string.parse_json_response_error, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(LoginNoWebviewActivity.this, R.string.retrieve_token_error, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(LoginNoWebviewActivity.this, R.string.retrieve_token_error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    public SharedPreferences getCurrentAccountSharedPreferences() {
        return mCurrentAccountSharedPreferences;
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        if (binding == null) return;
        binding.getRoot().setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
    }
}
