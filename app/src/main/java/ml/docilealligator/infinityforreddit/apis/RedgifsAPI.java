package ml.docilealligator.infinityforreddit.apis;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RedgifsAPI {
    @GET("/v2/gifs/{id}")
    Call<String> getRedgifsData(@HeaderMap Map<String, String> headers, @Path("id") String id, @Query("user-agent") String userAgent);

    @GET("/v2/auth/temporary")
    Call<String> getRedgifsTemporaryToken();
}
