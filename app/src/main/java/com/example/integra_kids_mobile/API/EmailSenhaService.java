package com.example.integra_kids_mobile.API;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EmailSenhaService {

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // ---------------------------------------------------------
    //                  SENHA CONTROLLER
    // ---------------------------------------------------------

    public static boolean alterarSenha(String token, String novaSenha) throws Exception {

        JSONObject body = new JSONObject();
        body.put("token", token);
        body.put("senha", novaSenha);

        return put("/senha/atualizar", body).isSuccessful();
    }


    // ---------------------------------------------------------
    //                  EMAIL CONTROLLER
    // ---------------------------------------------------------

    // 🔹 POST /email/enviar
    public static boolean enviarEmail(String email) throws Exception {

        JSONObject body = new JSONObject();
        body.put("email", email);

        RequestBody reqBody = RequestBody.create(body.toString(), JSON);

        Request request = new Request.Builder()
                .url(Api.BASE_URL + "/emailApi/token/" + email)
                .post(reqBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }


    // ---------------------------------------------------------
    //              MÉTODOS PRIVADOS (PUT)
    // ---------------------------------------------------------

    private static Response put(String endpoint, JSONObject bodyJson) throws Exception {
        String url = Api.BASE_URL + endpoint;

        RequestBody body = RequestBody.create(bodyJson.toString(), JSON);
        Request request = new Request.Builder().url(url).put(body).build();

        return client.newCall(request).execute();
    }

}
