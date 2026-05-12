package com.example.integra_kids_mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.integra_kids_mobile.helper.AccessibilityHelper;

public class UsuarioConfig extends Fragment {

    // Tema claro/escuro
    private RadioGroup radioGroup;
    private RadioButton radioSystem, radioLight, radioDark;

    // Daltonismo
    private RadioGroup radioColorGroup;
    private RadioButton radioNormal, radioProtan, radioDeuteran, radioTritan;

    // Acessibilidade
    private SeekBar seekBarText;

    // Redefinir
    private Button btnReset;

    private SharedPreferences prefs;

    public UsuarioConfig() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.usuario_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AccessibilityHelper.applyColorblindFilter(view);
        AccessibilityHelper.applyFontScale(view);

        prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        // --- Referências ---
        radioGroup      = view.findViewById(R.id.radioTheme);
        radioSystem     = view.findViewById(R.id.radioButton);
        radioLight      = view.findViewById(R.id.radioButton2);
        radioDark       = view.findViewById(R.id.radioButton3);
        radioColorGroup = view.findViewById(R.id.radioColorblind);
        radioNormal     = view.findViewById(R.id.radioButton6);
        radioProtan     = view.findViewById(R.id.radioButton7);
        radioDeuteran   = view.findViewById(R.id.radioButton8);
        radioTritan     = view.findViewById(R.id.radioButton9);
        seekBarText     = view.findViewById(R.id.seekBar);
        btnReset        = view.findViewById(R.id.btnResetConfig); // adicionar id no XML

        setupTheme();
        setupColorblind();
        setupTextSize();
        setupReset();
    }

    private void setupTheme() {
        int themeMode = prefs.getInt("themeMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        switch (themeMode) {
            case AppCompatDelegate.MODE_NIGHT_YES:  radioDark.setChecked(true);   break;
            case AppCompatDelegate.MODE_NIGHT_NO:   radioLight.setChecked(true);  break;
            default:                                radioSystem.setChecked(true); break;
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            if      (checkedId == R.id.radioButton2) mode = AppCompatDelegate.MODE_NIGHT_NO;
            else if (checkedId == R.id.radioButton3) mode = AppCompatDelegate.MODE_NIGHT_YES;

            AppCompatDelegate.setDefaultNightMode(mode);
            prefs.edit().putInt("themeMode", mode).apply();
        });
    }

    private void setupColorblind() {
        String saved = prefs.getString("colorblindMode", "NONE");
        switch (saved) {
            case "PROTANOMALIA":   radioProtan.setChecked(true);   break;
            case "DEUTERANOMALIA": radioDeuteran.setChecked(true); break;
            case "TRITANOMALIA":   radioTritan.setChecked(true);   break;
            default:               radioNormal.setChecked(true);   break;
        }

        radioColorGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String mode = "NONE";
            if      (checkedId == R.id.radioButton7) mode = "PROTANOMALIA";
            else if (checkedId == R.id.radioButton8) mode = "DEUTERANOMALIA";
            else if (checkedId == R.id.radioButton9) mode = "TRITANOMALIA";

            prefs.edit().putString("colorblindMode", mode).apply();
            requireActivity().recreate();
        });
    }

    private void setupTextSize() {
        int progress = prefs.getInt("textSizeProgress", 50);
        seekBarText.setProgress(progress);

        seekBarText.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                prefs.edit().putInt("textSizeProgress", seekBar.getProgress()).apply();
                requireActivity().recreate();
            }
        });
    }

    private void setupReset() {
        btnReset.setOnClickListener(v -> {
            // Limpa todas as preferências de acessibilidade
            prefs.edit()
                    .putInt("themeMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    .putString("colorblindMode", "NONE")
                    .putInt("textSizeProgress", 50)
                    .apply();

            // Restaura os elementos visuais
            radioSystem.setChecked(true);
            radioNormal.setChecked(true);
            seekBarText.setProgress(50);

            // Aplica o tema padrão
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            requireActivity().recreate();
        });
    }
}