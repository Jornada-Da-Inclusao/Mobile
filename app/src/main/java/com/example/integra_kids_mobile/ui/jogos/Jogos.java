package com.example.integra_kids_mobile.ui.jogos;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.integra_kids_mobile.LoginCadastro;
import com.example.integra_kids_mobile.R;
import com.example.integra_kids_mobile.databinding.JogosBinding;
import com.example.integra_kids_mobile.games.views.jogo_cores.JogoCores;
import com.example.integra_kids_mobile.games.views.jogo_cores.JogoCores2;
import com.example.integra_kids_mobile.games.views.jogo_cores.JogoCores3;
import com.example.integra_kids_mobile.games.views.jogo_memoria.JogoMemoria;
import com.example.integra_kids_mobile.games.views.jogo_memoria.JogoMemoria2;
import com.example.integra_kids_mobile.games.views.jogo_memoria.JogoMemoria3;
import com.example.integra_kids_mobile.games.views.jogo_numeros.JogoNumeros;
import com.example.integra_kids_mobile.games.views.jogo_numeros.JogoNumeros2;
import com.example.integra_kids_mobile.games.views.jogo_numeros.JogoNumeros3;
import com.example.integra_kids_mobile.games.views.jogo_vogais.JogoVogais;
import com.example.integra_kids_mobile.games.views.jogo_vogais.JogoVogais2;
import com.example.integra_kids_mobile.games.views.jogo_vogais.JogoVogais3;
import com.example.integra_kids_mobile.profile.PerfilTrocarPlayer;

public class Jogos extends Fragment {

    int[] gameDescribe = {
            R.string.game_describe_mem,
            R.string.game_describe_vog,
            R.string.game_describe_num,
            R.string.game_describe_cor,
    };
    View[] gameRoute = {}; // colocar as rotas dps aqui
    int[] viewImg = {
            R.drawable.memoria,
            R.drawable.vogais,
            R.drawable.numeros,
            R.drawable.cores};
    int[] colors = {
            R.color.red,
            R.color.blue,
            R.color.yellow,
            R.color.green
    };
    private Class<?>[] gameRoutes = {
            JogoMemoria.class,
            JogoVogais.class,
            JogoNumeros.class,
            JogoCores.class
    };
    int localGame;
    String viewGame;
    Button btnGame1, btnGame2, btnGame3, btnGame4, btnGameScreenReturn, btnPlay;
    LinearLayout layoutGameList, layoutGameFocus;
    ImageView imgGame;
    TextView textGameName, textGameDescribe;

    private JogosBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        JogosViewModel jogosViewModel =
                new ViewModelProvider(this).get(JogosViewModel.class);
    binding = JogosBinding.inflate(inflater, container, false);
    View root = binding.getRoot();

        btnGame1 = root.findViewById(R.id.btnGame1);
        btnGame2 = root.findViewById(R.id.btnGame2);
        btnGame3 = root.findViewById(R.id.btnGame3);
        btnGame4 = root.findViewById(R.id.btnGame4);

        btnGame1.setOnClickListener(v -> {openGameView(0,btnGame1.getText().toString());});
        btnGame2.setOnClickListener(v -> {openGameView(1,btnGame2.getText().toString());});
        btnGame3.setOnClickListener(v -> {openGameView(2,btnGame3.getText().toString());});
        btnGame4.setOnClickListener(v -> {openGameView(3,btnGame4.getText().toString());});

        btnPlay = root.findViewById(R.id.btnPlay); // sem uso até q os jogos existam

        btnGameScreenReturn = root.findViewById(R.id.btnGameScreenReturn);
        layoutGameFocus = root.findViewById(R.id.layoutGameFocus);
        layoutGameList = root.findViewById(R.id.layoutGameList);

        // inicialização
        layoutGameFocus.setVisibility(GONE);
        layoutGameList.setVisibility(VISIBLE);

        textGameName = root.findViewById(R.id.textGameName);
        textGameDescribe = root.findViewById(R.id.textGameDescribe);
        imgGame = root.findViewById(R.id.imgGame);

        // btn de retorno
        btnGameScreenReturn.setOnClickListener(v -> {
            layoutGameFocus.setVisibility(GONE);
            layoutGameList.setVisibility(VISIBLE);
        });

        btnPlay.setOnClickListener(v -> {
            int selectedId = getSelectedPlayerId();
            String selectedName = getSelectedPlayerName();

            // Chama o mesmo modal para ambos os casos
            mostrarModalPlayer(selectedName, selectedId);
        });


        return root;
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void openGameView(int id, String name){
        layoutGameList.setVisibility(GONE);
        layoutGameFocus.setVisibility(VISIBLE);

        layoutGameFocus.setBackgroundResource(R.drawable.border);
        layoutGameFocus.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), colors[id]));

        textGameName.setText(name);
        textGameDescribe.setText(gameDescribe[id]);
        imgGame.setImageResource(viewImg[id]);
        // tem q adicionar as rotas dps aqui
        setGame(id);
    }

    public void setGame(int id){
        localGame = id;
    }

    private int getSelectedPlayerId() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);
        return prefs.getInt("selected_player_id", -1);
    }

    private String getSelectedPlayerName() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);
        return prefs.getString("selected_player_name", null);
    }

    private int getSelectedPlayerAvatar() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);

        // 1. Pega a URL que salvamos
        String url = prefs.getString("selected_player_photo_url", null);

        // 2. Usa o seu AvatarMapper para transformar o link no R.drawable correspondente
        return com.example.integra_kids_mobile.utils.AvatarMapper.getAvatarResource(url);
    }

    private void mostrarModalNivel() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_niveis, null);

        // Use o construtor direto para ter mais controle sobre a Window
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).create();
        dialog.setView(dialogView);

        Button btnFacil = dialogView.findViewById(R.id.btnFacil);
        Button btnMedio = dialogView.findViewById(R.id.btnMedio);
        Button btnDificil = dialogView.findViewById(R.id.btnDificil);

        // Lógica de cliques...
        btnFacil.setOnClickListener(v -> { iniciarJogo(0); dialog.dismiss(); });
        btnMedio.setOnClickListener(v -> { iniciarJogo(1); dialog.dismiss(); });
        btnDificil.setOnClickListener(v -> { iniciarJogo(2); dialog.dismiss(); });

        // 1. Primeiro mostramos o modal
        dialog.show();

        // 2. DEPOIS do show, forçamos a transparência da janela que envolve o XML
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            // Opcional: remover preenchimentos do sistema que podem causar bugs visuais
            dialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);
        }
    }

    private void iniciarJogo(int nivel) {
        Intent intent = null;
        Context context = requireContext();

        // localGame: 0=Memória, 1=Vogais, 2=Números, 3=Cores
        // nivel: 0=Fácil, 1=Médio, 2=Difícil

        switch (localGame) {
            case 0: // JOGO DA MEMÓRIA
                if (nivel == 0) intent = new Intent(context, JogoMemoria.class);
                else if (nivel == 1) intent = new Intent(context, JogoMemoria2.class);
                else intent = new Intent(context, JogoMemoria3.class);
                break;

            case 1: // JOGO DAS VOGAIS
                if (nivel == 0) intent = new Intent(context, JogoVogais.class);
                else if (nivel == 1) intent = new Intent(context, JogoVogais2.class);
                else intent = new Intent(context, JogoVogais3.class);
                break;

            case 2: // JOGO DOS NÚMEROS
                if (nivel == 0) intent = new Intent(context, JogoNumeros.class);
                else if (nivel == 1) intent = new Intent(context, JogoNumeros2.class);
                else intent = new Intent(context, JogoNumeros3.class);
                break;

            case 3: // JOGO DAS CORES
                if (nivel == 0) intent = new Intent(context, JogoCores.class);
                else if (nivel == 1) intent = new Intent(context, JogoCores2.class);
                else intent = new Intent(context, JogoCores3.class);
                break;
        }

        if (intent != null) {
            // Passa o ID do jogador para salvar o progresso depois no DB
            intent.putExtra("PLAYER_ID", getSelectedPlayerId());
            startActivity(intent);
        }
    }

    private void mostrarModalPlayer(String nome, int id) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirm_player, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).create();
        dialog.setView(dialogView);

        ImageView imgPlayer = dialogView.findViewById(R.id.imgPlayerModal);
        TextView tvNome = dialogView.findViewById(R.id.tvNomePlayer);
        Button btnConfirmar = dialogView.findViewById(R.id.btnConfirmarPlayer);
        Button btnTrocar = dialogView.findViewById(R.id.btnTrocarPlayer);

        int corTema = ContextCompat.getColor(requireContext(), colors[localGame]);
        btnConfirmar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(corTema));

        if (id == -1) {
            // CASO SEM JOGADOR
            tvNome.setText("Nenhum jogador selecionado!");
            imgPlayer.setImageResource(R.drawable.player_icon_null);
            btnConfirmar.setText("ESCOLHER JOGADOR");
            btnTrocar.setVisibility(View.GONE);

            btnConfirmar.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(requireContext(), PerfilTrocarPlayer.class));
            });
        } else {
            // CASO JOGADOR EXISTE
            tvNome.setText(nome);

            // 🔹 AQUI: Define a imagem que foi salva pelo PerfilTrocarPlayer
            imgPlayer.setImageResource(getSelectedPlayerAvatar());

            btnConfirmar.setText("USAR ESSE JOGADOR");
            btnTrocar.setVisibility(View.VISIBLE);

            btnConfirmar.setOnClickListener(v -> {
                dialog.dismiss();
                mostrarModalNivel();
            });

            btnTrocar.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(requireContext(), PerfilTrocarPlayer.class));
            });
        }

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

}