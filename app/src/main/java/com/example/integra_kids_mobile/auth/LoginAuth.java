package com.example.integra_kids_mobile.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.integra_kids_mobile.LoginCadastro;
import com.example.integra_kids_mobile.MenuPrincipal;
import com.example.integra_kids_mobile.API.UsuarioService;

import org.json.JSONObject;

public class LoginAuth {

    private static final String PREF_NAME = "AuthPrefs";
    private static final String KEY_ID    = "user_id";
    private static final String KEY_NOME  = "user_nome";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_PASS  = "user_password";
    private static final String KEY_TOKEN = "user_token";

    // ---------------------------------------------------------
    //                     LOGIN
    // ---------------------------------------------------------
    public static boolean login(Context context, String email, String senha) {
        try {
            JSONObject resp = UsuarioService.logar(context, email, senha);

            if (resp.has("token")) {
                saveUserData(context, resp, senha);
                return true;
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------------------------------------------------
    //                     CADASTRO
    // ---------------------------------------------------------
    public static boolean cadastrar(Context context, String nome, String email, String senha) {
        try {
            JSONObject resp = UsuarioService.cadastrar(context, nome, email, senha);

            if (resp.has("id")) {
                Log.d("DEBUG_CAD_RESP", "Cadastro bem-sucedido para usuário ID: " + resp.getLong("id"));
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------------------------------------------------
    //           SALVAR DADOS DO USUÁRIO LOGADO
    // ---------------------------------------------------------
    private static void saveUserData(Context context, JSONObject json, String senha) throws Exception {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (json.has("id"))    editor.putInt(KEY_ID, json.getInt("id"));
        if (json.has("nome"))  editor.putString(KEY_NOME, json.getString("nome"));
        if (json.has("email")) editor.putString(KEY_EMAIL, json.getString("email"));
        if (json.has("token")) editor.putString(KEY_TOKEN, "Bearer " + json.getString("token"));
        if (senha != null)     editor.putString(KEY_PASS, senha);

        editor.apply();
    }

    // ---------------------------------------------------------
    //                LOGOUT – LIMPA MEMÓRIA
    // ---------------------------------------------------------
    public static void logout(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent i = new Intent(context, LoginCadastro.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(i);
    }

    // ---------------------------------------------------------
    //                 VERIFICAR SE ESTÁ LOGADO
    // ---------------------------------------------------------
    public static boolean isLogged(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .contains(KEY_TOKEN);
    }

    // ---------------------------------------------------------
    //   REDIRECIONAR PARA MENU – renova token antes de ir
    //
    //   Uso: LoginAuth.checkLoginRedirect(this, success -> {
    //       // dialog já fechou, só trate o caso de falha se precisar
    //   });
    // ---------------------------------------------------------
    public interface AutoLoginCallback {
        void onResult(boolean success);
    }

    public static void checkLoginRedirect(Context context, AutoLoginCallback callback) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String email = prefs.getString(KEY_EMAIL, null);
        String senha = prefs.getString(KEY_PASS, null);

        // Sem credenciais salvas → avisa e fica na tela de login
        if (email == null || senha == null) {
            if (callback != null) callback.onResult(false);
            return;
        }

        // Renova o token em background e só então redireciona
        new Thread(() -> {
            boolean ok = login(context, email, senha);

            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                if (callback != null) callback.onResult(ok);

                if (ok) {
                    Intent i = new Intent(context, MenuPrincipal.class);
                    Toast.makeText(context, "Sessão renovada automaticamente 🔄", Toast.LENGTH_SHORT).show();
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(i);
                }
            });
        }).start();
    }

    // ---------------------------------------------------------
    //                  MÉTODOS DE ACESSO RÁPIDO
    // ---------------------------------------------------------
    public static int getUserId(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_ID, -1);
    }

    public static String getUserNome(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_NOME, null);
    }

    public static String getUserEmail(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_EMAIL, null);
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_TOKEN, null);
    }
}