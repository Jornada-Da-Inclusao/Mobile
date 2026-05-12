package com.example.integra_kids_mobile.profile;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.integra_kids_mobile.API.DependenteService;
import com.example.integra_kids_mobile.BuildConfig;
import com.example.integra_kids_mobile.R;
import com.example.integra_kids_mobile.common.ReturnButton;
import com.example.integra_kids_mobile.helper.AccessibilityHelper;
import com.example.integra_kids_mobile.utils.AvatarMapper;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PerfilEditKid extends AppCompatActivity {

    Button btnSelectedPlayer;
    LinearLayout layoutEditKid;

    private final int[] avatarDrawables = {
            R.drawable.player_icon1,
            R.drawable.player_icon2,
            R.drawable.player_icon3,
            R.drawable.player_icon4,
            R.drawable.player_icon5,
            R.drawable.player_icon6,
            R.drawable.player_icon7,
            R.drawable.player_icon8,
            R.drawable.player_icon9,
            R.drawable.player_icon10,
            R.drawable.player_icon11,
            R.drawable.player_icon12
    };
    private int selectedAvatarDrawable = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.perfil_edit_kid);

        AccessibilityHelper.applyColorblindFilter(this);
        AccessibilityHelper.applyFontScale(this);

        ReturnButton.configurar(this);

        Button btnUpdateKid = findViewById(R.id.btnUpdateKid);
        Button btnDeleteKid = findViewById(R.id.btnDeleteKid);

        GridLayout gridAvatares = findViewById(R.id.gridAvatares);

        for (int i = 0; i < gridAvatares.getChildCount(); i++) {
            View child = gridAvatares.getChildAt(i);
            if (child instanceof ImageButton) {
                ImageButton avatarButton = (ImageButton) child;
                avatarButton.setTag(avatarDrawables[i]);

                avatarButton.setOnClickListener(v -> {
                    for (int j = 0; j < gridAvatares.getChildCount(); j++) {
                        View other = gridAvatares.getChildAt(j);
                        if (other instanceof ImageButton) {
                            other.setBackground(null);
                        }
                    }
                    avatarButton.setBackgroundResource(R.drawable.avatar_border);
                    selectedAvatarDrawable = (int) avatarButton.getTag();
                });
            }
        }

        btnSelectedPlayer = findViewById(R.id.btnSelectedPlayer);
        layoutEditKid = findViewById(R.id.layoutEditKid);
        layoutEditKid.setVisibility(GONE);

        btnSelectedPlayer.setOnClickListener(v -> abrirDialogDependentes());

        btnUpdateKid.setOnClickListener(v -> {
            try {
                int dependenteId = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
                        .getInt("dependente_id", -1);
                int userId = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
                        .getInt("user_id", -1);

                if (dependenteId == -1 || userId == -1) {
                    Toast.makeText(this, "Erro: dependente ou usuário não selecionado", Toast.LENGTH_SHORT).show();
                    return;
                }

                TextInputEditText nomeField = findViewById(R.id.inputNomeDependente);
                TextInputEditText dataField = findViewById(R.id.textInputLayoutDate);

                String nome = nomeField.getText().toString().trim();
                String dataExibida = dataField.getText().toString().trim(); // "dd/MM/yyyy"
                String dataISO = formatarDataISO(dataExibida);             // "yyyy-MM-ddT00:00:00Z"

                RadioButton masc = findViewById(R.id.radioButton4);
                String sexo = masc.isChecked() ? "M" : "F";

                String fotoUrl = AvatarMapper.getAvatarUrlFromResource(selectedAvatarDrawable);

                JSONObject payload = new JSONObject();
                payload.put("nome", nome);
                payload.put("dataNascimento", dataISO);
                payload.put("sexo", sexo);
                payload.put("foto", fotoUrl);

                JSONObject usuarioFk = new JSONObject();
                usuarioFk.put("id", userId);
                payload.put("usuario", usuarioFk);

                new Thread(() -> {
                    try {
                        DependenteService.atualizarParcial(this, dependenteId, payload);
                        runOnUiThread(() ->
                                Toast.makeText(this, "Cadastro atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(this, "Erro ao atualizar cadastro", Toast.LENGTH_SHORT).show()
                        );
                    }
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnDeleteKid.setOnClickListener(v -> {
            int dependenteId = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
                    .getInt("dependente_id", -1);

            if (dependenteId == -1) {
                Toast.makeText(this, "Erro: dependente não selecionado", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    DependenteService.deletar(this, dependenteId);
                    runOnUiThread(() ->
                            Toast.makeText(this, "Dependente deletado com sucesso!", Toast.LENGTH_SHORT).show()
                    );
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(this, "Erro ao deletar dependente", Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        });
    }

    // ===============================================
    //  "2000-05-10T00:00:00Z" → "10/05/2000"
    //  (para exibir no campo de texto)
    // ===============================================
    private String isoParaExibicao(String dataISO) {
        try {
            // formato: yyyy-MM-ddT00:00:00Z
            String dataParte = dataISO.split("T")[0]; // "2000-05-10"
            String[] parts = dataParte.split("-");
            return String.format("%s/%s/%s", parts[2], parts[1], parts[0]); // "10/05/2000"
        } catch (Exception e) {
            return "";
        }
    }

    // ===============================================
    //  "10/05/2000" → "2000-05-10T00:00:00Z"
    //  (para enviar no payload)
    // ===============================================
    private String formatarDataISO(String dataExibida) {
        try {
            String[] parts = dataExibida.split("/");
            return String.format("%s-%s-%sT00:00:00Z", parts[2], parts[1], parts[0]);
        } catch (Exception e) {
            return "";
        }
    }

    private void abrirDialogDependentes() {
        if (BuildConfig.DEBUG) { Log.d("KID_DEBUG", "========== abrirDialogDependentes() =========="); }

        new Thread(() -> {
            try {
                int userId = PerfilEditKid.this
                        .getSharedPreferences("AuthPrefs", MODE_PRIVATE)
                        .getInt("user_id", -1);

                if (BuildConfig.DEBUG) { Log.d("KID_DEBUG", "user_id lido: " + userId); }

                List<JSONObject> dependentes = DependenteService.getDependentesByUsuario(PerfilEditKid.this, userId);

                if (BuildConfig.DEBUG) { Log.d("KID_DEBUG", "Quantidade: " + dependentes.size()); }

                runOnUiThread(() -> {
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_dependente, null);
                    ListView listView = dialogView.findViewById(R.id.listDependentes);

                    List<String> nomes = new ArrayList<>();
                    for (JSONObject d : dependentes) {
                        nomes.add(d.optString("nome"));
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            PerfilEditKid.this,
                            android.R.layout.simple_list_item_1,
                            nomes
                    );
                    listView.setAdapter(adapter);

                    AlertDialog dialog = new AlertDialog.Builder(PerfilEditKid.this)
                            .setTitle("Selecione a criança")
                            .setView(dialogView)
                            .create();

                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        try {
                            JSONObject escolhido = dependentes.get(position);
                            if (BuildConfig.DEBUG) { Log.d("KID_DEBUG", "Dependente escolhido: " + escolhido.toString()); }

                            preencherCamposDependente(escolhido);

                            layoutEditKid.setVisibility(View.VISIBLE);
                            btnSelectedPlayer.setText("Trocar criança");
                        } catch (Exception e) {
                            if (BuildConfig.DEBUG) { Log.e("KID_DEBUG", "Erro ao preencher dependente", e); }
                        }
                        dialog.dismiss();
                    });

                    dialog.show();
                });

            } catch (Exception e) {
                if (BuildConfig.DEBUG) { Log.e("KID_DEBUG", "ERRO NO PROCESSO:", e); }
                runOnUiThread(() ->
                        Toast.makeText(PerfilEditKid.this, "Erro ao carregar dependentes", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void preencherCamposDependente(JSONObject dep) throws Exception {
        layoutEditKid.setVisibility(VISIBLE);
        btnSelectedPlayer.setText("Mudar criança");

        String nome     = dep.getString("nome");
        String sexo     = dep.getString("sexo");
        String foto     = dep.optString("foto", "");
        String dataISO  = dep.optString("dataNascimento", ""); // "2000-05-10T00:00:00Z"
        int idDep       = dep.getInt("id");

        getSharedPreferences("AuthPrefs", MODE_PRIVATE)
                .edit()
                .putInt("dependente_id", idDep)
                .apply();

        // Preenche nome
        TextInputEditText nomeField = findViewById(R.id.inputNomeDependente);
        nomeField.setText(nome);

        // Converte ISO → dd/MM/yyyy e exibe no campo
        TextInputEditText dataField = findViewById(R.id.editTextDate);
        dataField.setText(isoParaExibicao(dataISO));

        // Marca o sexo
        RadioButton masc = findViewById(R.id.radioButton4);
        RadioButton fem  = findViewById(R.id.radioButton5);
        if (sexo.equalsIgnoreCase("M")) masc.setChecked(true); else fem.setChecked(true);

        // Seleciona o avatar correspondente no grid
        GridLayout gridAvatares = findViewById(R.id.gridAvatares);
        for (int i = 0; i < gridAvatares.getChildCount(); i++) {
            View child = gridAvatares.getChildAt(i);
            if (child instanceof ImageButton) {
                ImageButton avatarButton = (ImageButton) child;
                int drawableRes = (int) avatarButton.getTag();
                String url = AvatarMapper.getAvatarUrlFromResource(drawableRes);

                if (foto.equals(url)) {
                    avatarButton.setBackgroundResource(R.drawable.avatar_border);
                    selectedAvatarDrawable = drawableRes;
                } else {
                    avatarButton.setBackground(null);
                }
            }
        }
    }
}