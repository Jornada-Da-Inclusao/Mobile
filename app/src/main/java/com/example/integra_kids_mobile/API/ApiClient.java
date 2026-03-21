package com.example.integra_kids_mobile.API;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.integra_kids_mobile.BuildConfig;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class ApiClient {
    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String BASE_URL = Api.BASE_URL;

    private static String fullUrl(String endpoint) {
        if (endpoint.startsWith("http")) return endpoint;
        return BASE_URL + endpoint;
    }

    public static String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        return prefs.getString("user_token", null);
    }

    // =======================================================
    // GET
    // =======================================================
    public static Response get(Context context, String endpoint) throws Exception {

        String token = getToken(context);

        if (BuildConfig.DEBUG) {
            Log.d("API_DEBUG", "GET -> URL: " + fullUrl(endpoint));
            Log.d("API_DEBUG", "Headers: Authorization=" + token);
        }

        Request.Builder builder = new Request.Builder()
                .url(fullUrl(endpoint))
                .addHeader("Content-Type", "application/json");

        // Só adiciona Authorization se tiver token
        if (token != null && !token.isEmpty()) {
            builder.addHeader("Authorization", token);
        }

        Request request = builder.build();
        Response response = client.newCall(request).execute();

        if (BuildConfig.DEBUG) {
            Log.d("API_DEBUG", "Response Code: " + response.code());
            Log.d("API_DEBUG", "Response Body: " + response.peekBody(Long.MAX_VALUE).string());
        }

        return response;
    }


    // =======================================================
    // POST
    // =======================================================
    public static Response post(Context context, String endpoint, String jsonBody) throws Exception {

        String token = getToken(context);
        if (BuildConfig.DEBUG) {
            Log.d("API_DEBUG", "POST -> URL: " + fullUrl(endpoint));
            Log.d("API_DEBUG", "Body: " + jsonBody);
            Log.d("API_DEBUG", "Headers: Authorization=" + token);
        }
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request.Builder builder = new Request.Builder()
                .url(fullUrl(endpoint))
                .post(body)
                .addHeader("Content-Type", "application/json");

        // Só adiciona Authorization se existir
        if (token != null && !token.isEmpty()) {
            builder.addHeader("Authorization", token);
        }

        Request request = builder.build();
        Response response = client.newCall(request).execute();

        if (BuildConfig.DEBUG) {
            Log.d("API_DEBUG", "Response Code: " + response.code());
            Log.d("API_DEBUG", "Response Body: " + response.peekBody(Long.MAX_VALUE).string());
        }

        return response;
    }


    public static Response postNoAuth(String url, String jsonBody) throws Exception {

        if (BuildConfig.DEBUG) {
            Log.d("API_DEBUG", "POST (NO AUTH) -> URL: " + url);
            Log.d("API_DEBUG", "Body: " + jsonBody);
        }

        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = client.newCall(request).execute();

        if (BuildConfig.DEBUG) {
            Log.d("API_DEBUG", "Response Code: " + response.code());
            Log.d("API_DEBUG", "Response Body: " + response.peekBody(Long.MAX_VALUE).string());
        }

        return response;
    }

    // =======================================================
    // PUT
    // =======================================================
    public static Response put(Context context, String endpoint, String jsonBody) throws Exception {

        String token = getToken(context);

        if (BuildConfig.DEBUG) {
            Log.d("API_DEBUG", "PUT -> URL: " + fullUrl(endpoint));
            Log.d("API_DEBUG", "Body: " + jsonBody);
            Log.d("API_DEBUG", "Headers: Authorization=" + token);
        }

        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(fullUrl(endpoint))
                .put(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", token)
                .build();

        Response response = client.newCall(request).execute();

        if (BuildConfig.DEBUG) {
            Log.d("API_DEBUG", "Response Code: " + response.code());
            Log.d("API_DEBUG", "Response Body: " + response.peekBody(Long.MAX_VALUE).string());
        }

        return response;
    }

    // =======================================================
    // PATCH
    // =======================================================
    public static Response patch(Context context, String endpoint, String jsonBody) throws Exception {

        String token = getToken(context);

        if (BuildConfig.DEBUG) {
            Log.d("API_DEBUG", "PATCH -> URL: " + fullUrl(endpoint));
            Log.d("API_DEBUG", "Body: " + jsonBody);
            Log.d("API_DEBUG", "Headers: Authorization=" + token);
        }

        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(fullUrl(endpoint))
                .patch(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", token)
                .build();

        Response response = client.newCall(request).execute();

        if (BuildConfig.DEBUG) {
            Log.d("API_DEBUG", "Response Code: " + response.code());
            Log.d("API_DEBUG", "Response Body: " + response.peekBody(Long.MAX_VALUE).string());
        }

        return response;
    }

    // =======================================================
    // DELETE
    // =======================================================
    public static Response delete(Context context, String endpoint) throws Exception {

        String token = getToken(context);

        if (BuildConfig.DEBUG) {
            Log.d("API_DEBUG", "DELETE -> URL: " + fullUrl(endpoint));
            Log.d("API_DEBUG", "Headers: Authorization=" + token);
        }

        Request request = new Request.Builder()
                .url(fullUrl(endpoint))
                .delete()
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", token)
                .build();

        Response response = client.newCall(request).execute();

        if (BuildConfig.DEBUG) {
            Log.d("API_DEBUG", "Response Code: " + response.code());
            Log.d("API_DEBUG", "Response Body: " + response.peekBody(Long.MAX_VALUE).string());
        }

        return response;
    }
}
