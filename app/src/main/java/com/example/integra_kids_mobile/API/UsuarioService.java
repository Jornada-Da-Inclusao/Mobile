package com.example.integra_kids_mobile.API;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import okhttp3.Response;

public class UsuarioService {

    private static final String BASE_URL = Api.BASE_URL;;

    // -----------------------
    // LOGIN → envia usuario + senha
    // -----------------------
    public static JSONObject logar(Context context, String email, String senha) throws Exception {
        String url = BASE_URL + "/usuarios/logar";

        JSONObject json = new JSONObject();
        json.put("email", email); // ⚠️ o backend espera "usuario"
        json.put("senha", senha);

        // 🔹 LOG do JSON que será enviado
        android.util.Log.d("LOGIN_DEBUG", "Enviando login: " + json.toString());

        Response resp = ApiClient.postNoAuth(url, json.toString());
        String respBody = resp.body().string();

        // 🔹 LOG da resposta recebida
        android.util.Log.d("LOGIN_DEBUG", "Resposta do backend: " + respBody);

        JSONObject jsonResp = new JSONObject(respBody);

        // ✅ Se login for bem-sucedido, limpar SharedPreferences antigos
        if (jsonResp.has("success") && jsonResp.getBoolean("success")) {
            SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("KEY_SELECTED_PLAYER_ID");
            editor.remove("KEY_SELECTED_PLAYER_NAME");
            editor.apply();
        }

        return jsonResp;
    }



    // -----------------------
    // CADASTRO
    // -----------------------
    public static JSONObject cadastrar(Context context, String nome, String email, String senha) throws Exception {
        String url = BASE_URL + "/usuarios";

        JSONObject json = new JSONObject();
        json.put("nome", nome);
        json.put("email", email);
        json.put("senha", senha);

        Response resp = ApiClient.postNoAuth(url, json.toString());

        String bodyString = resp.body() != null ? resp.body().string() : "";

        Log.d("DEBUG_CAD_RESP", "Código: " + resp.code());
        Log.d("DEBUG_CAD_RESP", "Body: " + bodyString);

        if (resp.isSuccessful()) {
            return new JSONObject(bodyString);
        } else {
            throw new Exception("Erro ao cadastrar: código " + resp.code() + " | Body: " + bodyString);
        }
    }

    // -----------------------
    // PATCH /usuarios/{id}
    // -----------------------
// PATCH /usuarios/atualizar-parcial
    public static JSONObject atualizarParcial(Context context, String jsonBody, long id) throws Exception {
        String endpoint = "/usuarios/" + id;
        Response resp = ApiClient.patch(context, endpoint, jsonBody);
        return new JSONObject(resp.body().string());
    }


    // -----------------------
    // DELETE /usuarios/{id}
    // -----------------------
    public static boolean deletar(Context context, long id) throws Exception {
        String url = BASE_URL + "/usuarios/" + id;
        String token = ApiClient.getToken(context);

        Log.d("DEBUG_DELETE_REQ", "URL: " + url);
        Log.d("DEBUG_DELETE_REQ", "Token: " + token);

        Response resp = ApiClient.delete(context, url);

        // Log da resposta
        Log.d("DEBUG_DELETE_RESP", "Código: " + resp.code());
        if (resp.body() != null) {
            Log.d("DEBUG_DELETE_RESP", "Body: " + resp.body().string());
        } else {
            Log.d("DEBUG_DELETE_RESP", "Body: null (sem conteúdo)");
        }

        // Aceita qualquer 2xx como sucesso
        return resp.code() >= 200 && resp.code() < 300;
    }



}
