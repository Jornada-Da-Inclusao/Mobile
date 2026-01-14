package com.example.integra_kids_mobile.profile;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.integra_kids_mobile.API.DependenteService;
import com.example.integra_kids_mobile.R;
import com.example.integra_kids_mobile.adapter.HistoricoAdapter;
import com.example.integra_kids_mobile.common.ReturnButton;
import com.example.integra_kids_mobile.model.DependenteCallback;
import com.example.integra_kids_mobile.model.Partida;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfilResultados extends AppCompatActivity {

    private LinearLayout layoutResult;
    private Button btnChooseChild, btnChooseGraph, btnPDF, btnExcel;
    private TextView textGraph;
    private HistoricoAdapter adapter;

    private int currentChartIndex = 0; // 0 = Bar, 1 = Pie, 2 = Radar

    private BarChart barChart;
    private PieChart pieChart;
    private RadarChart radarChart;
    private RecyclerView recyclerHistorico;

    private int idDependenteSelecionado = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.perfil_resultados);

        ReturnButton.configurar(this);

        layoutResult = findViewById(R.id.layoutResult);
        btnChooseChild = findViewById(R.id.btnChooseChild);
        btnChooseGraph = findViewById(R.id.btnChooseGraph);
        textGraph = findViewById(R.id.textGraph);

        barChart = findViewById(R.id.barChart);
        pieChart = findViewById(R.id.pieChart);
        radarChart = findViewById(R.id.radarChart);
        recyclerHistorico = findViewById(R.id.recyclerHistorico);

        btnPDF = findViewById(R.id.btnPDF);
        btnExcel = findViewById(R.id.btnExcel);

        layoutResult.setVisibility(GONE);
        btnChooseGraph.setEnabled(false);

        btnPDF.setOnClickListener(v -> {
            if (idDependenteSelecionado != -1) {
                // 🔹 Agora o ID está correto e disponível!
                DependenteService.baixarRelatorioPdf(this, idDependenteSelecionado);
            } else {
                Toast.makeText(this, "Por favor, selecione uma criança primeiro", Toast.LENGTH_SHORT).show();
            }
        });

        btnExcel.setOnClickListener(v -> {
            if (idDependenteSelecionado != -1) {
                DependenteService.baixarRelatorioExcel(this, idDependenteSelecionado);
            } else {
                Toast.makeText(this, "Por favor, selecione uma criança primeiro", Toast.LENGTH_SHORT).show();
            }
        });

        // Inicializa RecyclerView com lista vazia
        recyclerHistorico.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoricoAdapter(new ArrayList<>(), partida ->
                Toast.makeText(this, "Partida deletada: " + partida.getNomeJogo(), Toast.LENGTH_SHORT).show());
        recyclerHistorico.setAdapter(adapter);

        // Configura botões
        btnChooseChild.setOnClickListener(v -> abrirDialogDependentes(obj -> {
            idDependenteSelecionado = obj.optInt("id", -1);
            carregarResultados(idDependenteSelecionado);
            layoutResult.setVisibility(VISIBLE);
            btnChooseGraph.setEnabled(true);
            btnChooseGraph.setBackgroundColor(getResources().getColor(R.color.tint));
            btnChooseChild.setText("Trocar criança");
        }));

        btnChooseGraph.setOnClickListener(v -> {
            currentChartIndex = (currentChartIndex + 1) % 3;
            atualizarVisibilidadeGraficos();
        });

        // Inicialmente mostra BarChart
        atualizarVisibilidadeGraficos();
    }

    private void atualizarVisibilidadeGraficos() {
        switch (currentChartIndex) {
            case 0:
                barChart.setVisibility(VISIBLE);
                pieChart.setVisibility(GONE);
                radarChart.setVisibility(GONE);
                btnChooseGraph.setText("Acertos e erros");
                textGraph.setText("Gráfico de Tentativas:");
                break;
            case 1:
                barChart.setVisibility(GONE);
                pieChart.setVisibility(VISIBLE);
                radarChart.setVisibility(GONE);
                btnChooseGraph.setText("Tempo médio");
                textGraph.setText("Gráfico de Tempo:");
                break;
            case 2:
                barChart.setVisibility(GONE);
                pieChart.setVisibility(GONE);
                radarChart.setVisibility(VISIBLE);
                btnChooseGraph.setText("Desempenho geral");
                textGraph.setText("Gráfico de Desempenho:");
                break;
        }
    }

    private void abrirDialogDependentes(DependenteCallback callback) {
        new Thread(() -> {
            try {
                int userId = getSharedPreferences("AuthPrefs", MODE_PRIVATE).getInt("user_id", -1);
                List<JSONObject> dependentes = DependenteService.getDependentesByUsuario(this, userId);

                runOnUiThread(() -> {
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_dependente, null);
                    ListView listView = dialogView.findViewById(R.id.listDependentes);

                    List<String> nomes = new ArrayList<>();
                    for (JSONObject d : dependentes) nomes.add(d.optString("nome"));

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, nomes);
                    listView.setAdapter(adapter);

                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("Selecione a criança")
                            .setView(dialogView)
                            .create();

                    listView.setOnItemClickListener((parent, view, position, id) -> {
                        callback.onDependenteSelecionado(dependentes.get(position));
                        dialog.dismiss();
                    });

                    dialog.show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao carregar dependentes", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void carregarResultados(int dependenteId) {
        new Thread(() -> {
            try {
                List<Partida> lista = DependenteService.getPartidasByDependente(this, dependenteId);

                // Agrupar dados
                Map<String, Integer> totalAcertos = new HashMap<>();
                Map<String, Integer> totalErros = new HashMap<>();
                Map<String, Float> totalTempo = new HashMap<>();

                for (Partida p : lista) {
                    Log.d("PerfilResultados", "Partida recebida -> ID: " + p.getId() +
                            ", Nome: " + p.getNomeJogo() +
                            ", Acertos: " + p.getAcertos() +
                            ", Erros: " + p.getErros() +
                            ", Tempo: " + p.getTempoTotal());
                    String nome = p.getNomeJogo(); // já corrigido acima
                    totalAcertos.put(nome, totalAcertos.getOrDefault(nome, 0) + p.getAcertos());
                    totalErros.put(nome, totalErros.getOrDefault(nome, 0) + p.getErros());
                    totalTempo.put(nome, totalTempo.getOrDefault(nome, 0f) + (float)p.getTempoTotal());

                    Log.d("PerfilResultados", "Mapa atualizado -> Nome: " + nome +
                            ", Acertos: " + totalAcertos.get(nome) +
                            ", Erros: " + totalErros.get(nome) +
                            ", Tempo: " + totalTempo.get(nome));
                }


                runOnUiThread(() -> {
                    configurarBarChart(barChart, totalAcertos, totalErros);
                    configurarPieChart(pieChart, totalTempo);
                    configurarRadarChart(radarChart, totalAcertos);

                    adapter.atualizarLista(lista); // atualiza dados do RecyclerView
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao carregar resultados!", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    // ---------------------- MÉTODOS GRÁFICOS ----------------------

    private void configurarBarChart(BarChart chart, Map<String, Integer> acertos, Map<String, Integer> erros) {
        List<BarEntry> tentativas = new ArrayList<>();
        List<BarEntry> entradasAcertos = new ArrayList<>();
        List<BarEntry> entradasErros = new ArrayList<>();
        List<String> nomes = new ArrayList<>();
        List<String> nomesOriginais = new ArrayList<>(); // para manter correspondência com o mapa

        for (String nomeOriginal : acertos.keySet()) {
            String nomeLimpo = nomeOriginal.replaceFirst("(?i)^jogo\\s+(de|da|do|das|dos)\\s+", "").trim();
            nomes.add(nomeLimpo);
            nomesOriginais.add(nomeOriginal);
        }

        int i = 0;
        for (int j = 0; j < nomes.size(); j++) {
            String nomeOriginal = nomesOriginais.get(j);
            int a = acertos.getOrDefault(nomeOriginal, 0);
            int e = erros.getOrDefault(nomeOriginal, 0);
            entradasAcertos.add(new BarEntry(i, a));
            entradasErros.add(new BarEntry(i, e));
            tentativas.add(new BarEntry(i, a + e));
            i++;
        }

        int corTexto = getResources().getColor(R.color.text);

        BarDataSet dsTentativas = new BarDataSet(tentativas, "Tentativas");
        dsTentativas.setColor(Color.rgb(66, 135, 245));
        dsTentativas.setValueTextColor(corTexto);

        BarDataSet dsAcertos = new BarDataSet(entradasAcertos, "Acertos");
        dsAcertos.setColor(Color.rgb(76, 175, 80));
        dsAcertos.setValueTextColor(corTexto);

        BarDataSet dsErros = new BarDataSet(entradasErros, "Erros");
        dsErros.setColor(Color.rgb(244, 67, 54));
        dsErros.setValueTextColor(corTexto);

        BarData data = new BarData(dsTentativas, dsAcertos, dsErros);
        chart.setData(data);
        chart.getDescription().setEnabled(false);

        // legenda
        Legend legend = chart.getLegend();
        legend.setTextColor(corTexto);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(9f);
        xAxis.setTextColor(corTexto);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value;
                return (index >= 0 && index < nomes.size()) ? nomes.get(index) : "";
            }
        });

        float groupSpace = 0.3f, barSpace = 0.05f, barWidth = 0.2f;
        data.setBarWidth(barWidth);
        float groupWidth = data.getGroupWidth(groupSpace, barSpace);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(0 + groupWidth * nomes.size());
        chart.groupBars(0, groupSpace, barSpace);
        chart.invalidate();
    }

    private void configurarPieChart(PieChart chart, Map<String, Float> tempo) {
        List<PieEntry> entries = new ArrayList<>();

        for (String nomeOriginal : tempo.keySet()) {
            String nomeLimpo = nomeOriginal
                    .replaceFirst("(?i)^jogo\\s+(de|da|do|das|dos)\\s+", "")
                    .trim();

            float segundosReais = tempo.get(nomeOriginal);

            // 🔥 Aqui dividimos por 2 antes de enviar
            float segundosCorrigidos = segundosReais / 2f;

            entries.add(new PieEntry(segundosCorrigidos, nomeLimpo, segundosCorrigidos));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Tempo");
        dataSet.setColors(
                Color.rgb(66, 135, 245),
                Color.rgb(76, 175, 80),
                Color.rgb(255, 193, 7),
                Color.rgb(244, 67, 54)
        );

        int corTexto = getResources().getColor(R.color.text);
        dataSet.setValueTextColor(corTexto);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int segundos = Math.round(value);

                int min = segundos / 60;
                int seg = segundos % 60;

                return String.format("%02dm:%02ds", min, seg);
            }
        });

        chart.setData(data);
        chart.getDescription().setEnabled(false);
        chart.setUsePercentValues(false);

        Legend legend = chart.getLegend();
        legend.setTextColor(corTexto);

        chart.invalidate();
    }



    private void configurarRadarChart(RadarChart chart, Map<String, Integer> acertos) {
        List<RadarEntry> entries = new ArrayList<>();
        List<String> nomes = new ArrayList<>();
        for (String nomeOriginal : acertos.keySet()) {
            String nomeLimpo = nomeOriginal.replaceFirst("(?i)^jogo\\s+(de|da|do|das|dos)\\s+", "").trim();
            nomes.add(nomeLimpo);
            entries.add(new RadarEntry(acertos.get(nomeOriginal)));
        }

        RadarDataSet dataSet = new RadarDataSet(entries, "Acertos por jogo");
        dataSet.setColor(Color.rgb(33, 150, 243));
        dataSet.setFillColor(Color.rgb(33, 150, 243));
        dataSet.setDrawFilled(true);
        dataSet.setLineWidth(2f);
        int corTexto = getResources().getColor(R.color.text);
        dataSet.setValueTextColor(corTexto);

        RadarData data = new RadarData(dataSet);
        chart.setData(data);
        chart.getDescription().setEnabled(false);
        chart.getYAxis().setAxisMinimum(0);
        chart.getYAxis().setTextColor(corTexto);
        chart.getXAxis().setTextColor(corTexto);
        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if (nomes.isEmpty()) return "";
                int index = (int) value % nomes.size();
                return (index >= 0 && index < nomes.size()) ? nomes.get(index) : "";
            }
        });

        Legend legend = chart.getLegend();
        legend.setTextColor(corTexto);
        chart.getLegend().setEnabled(false);

        chart.invalidate();
    }

}
