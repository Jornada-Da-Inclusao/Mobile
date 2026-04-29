package com.example.integra_kids_mobile.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.integra_kids_mobile.API.UsuarioService;
import com.example.integra_kids_mobile.BuildConfig;
import com.example.integra_kids_mobile.LoginCadastro;
import com.example.integra_kids_mobile.R;
import com.example.integra_kids_mobile.common.ReturnButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

public class PerfilEditCad extends AppCompatActivity {

    private TextInputEditText inputNome, inputEmail, inputSenha, inputSenhaConf;
    private Button btnAltUser, btnDeleteUser;

    private long userId;
    private String token;

    private static final String PREF_NAME = "AuthPrefs";
    private static final String KEY_ID    = "user_id";
    private static final String KEY_NOME  = "user_nome";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_TOKEN = "user_token";
    private static final String KEY_PASS  = "user_password"; // necessário para o auto-login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.perfil_edit_cad);

        ReturnButton.configurar(this);

        inputNome     = findViewById(R.id.inputEditCadUserName);
        inputEmail    = findViewById(R.id.inputEditCadUserEmail);
        inputSenha    = findViewById(R.id.inputEditCadUserSenha);
        inputSenhaConf = findViewById(R.id.inputEditCadUserSenhaConf);

        loadUserData();

        btnAltUser    = findViewById(R.id.btnAltUser);
        btnDeleteUser = findViewById(R.id.btnDeleteUser);

        // ALTERAR DADOS
        btnAltUser.setOnClickListener(v -> {
            String novoNome  = inputNome.getText().toString().trim();
            String novoEmail = inputEmail.getText().toString().trim();
            String novaSenha = inputSenha.getText().toString().trim();
            String confSenha = inputSenhaConf.getText().toString().trim();

            if (!novaSenha.equals(confSenha)) {
                Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Confirmação")
                    .setMessage("Deseja realmente alterar os dados?")
                    .setPositiveButton("Sim", (dialog, which) -> atualizarUsuario(novoNome, novoEmail, novaSenha))
                    .setNegativeButton("Não", null)
                    .show();
        });

        // DELETAR CONTA
        btnDeleteUser.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmação")
                    .setMessage("Deseja realmente deletar sua conta?")
                    .setPositiveButton("Sim", (dialog, which) -> deletarUsuario())
                    .setNegativeButton("Não", null)
                    .show();
        });
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        userId = prefs.getInt(KEY_ID, 0);
        token  = prefs.getString(KEY_TOKEN, "");

        inputNome.setText(prefs.getString(KEY_NOME, ""));
        inputEmail.setText(prefs.getString(KEY_EMAIL, ""));

        if (BuildConfig.DEBUG) { Log.d("DEBUG_USER", "ID: " + userId + " | Token: " + token); }
    }

    private void saveUserDataLocal(JSONObject json, String novaSenha) {
        try {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            if (json.has("id"))      editor.putInt(KEY_ID, json.getInt("id"));
            if (json.has("nome"))    editor.putString(KEY_NOME, json.getString("nome"));
            if (json.has("email")) editor.putString(KEY_EMAIL, json.getString("email"));
            if (json.has("token"))   editor.putString(KEY_TOKEN, json.getString("token"));

            // Atualiza a senha salva para o auto-login continuar funcionando
            if (novaSenha != null && !novaSenha.isEmpty())
                editor.putString(KEY_PASS, novaSenha);

            editor.apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------------
    //     PATCH /usuarios/{id}
    // ---------------------------
    private void atualizarUsuario(String nome, String email, String senha) {
        new Thread(() -> {
            try {
                JSONObject dados = new JSONObject();
                dados.put("id", userId);
                if (!nome.isEmpty())  dados.put("nome", nome);
                if (!email.isEmpty()) dados.put("email", email);
                if (!senha.isEmpty()) dados.put("senha", senha);

                JSONObject resp = UsuarioService.atualizarParcial(this, dados.toString(), userId);

                if (BuildConfig.DEBUG) { Log.d("DEBUG_PATCH_RESP", resp.toString()); }

                saveUserDataLocal(resp, senha); // passa a nova senha para salvar nas prefs

                runOnUiThread(() -> {
                    Toast.makeText(this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();
                    loadUserData();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao atualizar dados.", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    // ---------------------------
    //   DELETE /usuarios/{id}
    // ---------------------------
    private void deletarUsuario() {
        new Thread(() -> {
            try {
                boolean sucesso = UsuarioService.deletar(this, userId);

                runOnUiThread(() -> {
                    if (sucesso) {
                        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                        prefs.edit().clear().apply();

                        Toast.makeText(this, "Conta deletada com sucesso!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, LoginCadastro.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        finish();
                    } else {
                        Toast.makeText(this, "Usuário não encontrado ou erro ao deletar.", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao deletar conta.", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}