package com.example.integra_kids_mobile.API;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.integra_kids_mobile.BuildConfig;
import com.example.integra_kids_mobile.model.Partida;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

public class DependenteService {

    private static final String BASE = "/dependente";

    // ==========================================================
    //                         GET
    // ==========================================================

    // 🔹 Buscar todos os dependentes
    public static JSONArray getTodos(Context context) throws Exception {
        Response response = ApiClient.get(context, BASE);
        String resp = response.body().string();
        return new JSONArray(resp);
    }

    // 🔹 Buscar dependente por ID
    public static JSONObject getById(Context context, long id) throws Exception {
        Response response = ApiClient.get(context, BASE + "/" + id);
        String resp = response.body().string();
        return new JSONObject(resp);
    }

    // 🔹 Histórico de jogos do dependente
    public static JSONArray getInfoJogos(Context context, long id) throws Exception {
        Response response = ApiClient.get(context, BASE + "/infoJogosByDependente/" + id);
        String resp = response.body().string();
        return new JSONArray(resp);
    }

    // 🔹 Buscar dependentes de um usuário
    public static List<JSONObject> getDependentesByUsuario(Context context, int userId) throws Exception {

        if (BuildConfig.DEBUG) {
            Log.d("DEPENDENTE_DEBUG", "Iniciando getDependentesByUsuario()");
            Log.d("DEPENDENTE_DEBUG", "UserID recebido: " + userId);
        }

        Response response = ApiClient.get(context, "/dependente/getDependenteByIdUsuario/" + userId);

        if (BuildConfig.DEBUG) {Log.d("DEPENDENTE_DEBUG", "Response recebido. Status: " + response.code());}

        String body = null;
        try {
            body = response.body().string();
            if (BuildConfig.DEBUG) {Log.d("DEPENDENTE_DEBUG", "Body bruto: " + body);}
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {Log.e("DEPENDENTE_DEBUG", "Erro ao ler body da resposta", e);}
            throw e;
        }

        // Verificação extra — evita crash caso o backend retorne null / vazio / HTML de erro
        if (body == null || body.trim().isEmpty()) {
            if (BuildConfig.DEBUG) {Log.e("DEPENDENTE_DEBUG", "Body vazio ou null! Retornando lista vazia.");}
            return new ArrayList<>();
        }

        JSONArray array;
        try {
            array = new JSONArray(body);
            if (BuildConfig.DEBUG) {Log.d("DEPENDENTE_DEBUG", "JSONArray criado. Tamanho: " + array.length());}
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {Log.e("DEPENDENTE_DEBUG", "ERRO ao converter resposta para JSONArray!", e);}
            throw e;
        }

        List<JSONObject> result = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject obj = array.getJSONObject(i);
                if (BuildConfig.DEBUG) {Log.d("DEPENDENTE_DEBUG", "Dependente carregado: " + obj.toString());}
                result.add(obj);
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {Log.e("DEPENDENTE_DEBUG", "Erro ao ler item [" + i + "] do JSONArray", e);}
            }
        }

        if (BuildConfig.DEBUG) {Log.d("DEPENDENTE_DEBUG", "Finalizado. Total de dependentes: " + result.size());}

        return result;
    }

    // ==========================================================
    //                         POST
    // ==========================================================

    // 🔹 Cadastrar dependente
    public static JSONObject cadastrar(Context context, String nome, int idade, String sexo, String avatar, int usuarioId) throws Exception {

        JSONObject dep = new JSONObject();
        dep.put("nome", nome);
        dep.put("idade", idade);
        dep.put("sexo", sexo);
        dep.put("foto", avatar);

        JSONObject usuario = new JSONObject();
        usuario.put("id", usuarioId);
        dep.put("usuario_id_fk", usuario);

        if (BuildConfig.DEBUG) {Log.d("API_DEBUG", "Enviando JSON: " + dep.toString());}

        Response response = ApiClient.post(context, BASE, dep.toString());
        String resp = response.body().string();
        return new JSONObject(resp);
    }

    // ==========================================================
    //                         PUT
    // ==========================================================

    // 🔹 Atualização completa
    public static JSONObject atualizar(Context context, int id, String nome, int idade, String sexo, String avatar, int usuarioId) throws Exception {

        JSONObject dep = new JSONObject();
        dep.put("id", id);
        dep.put("nome", nome);
        dep.put("idade", idade);
        dep.put("sexo", sexo);
        dep.put("foto", avatar);

        JSONObject usuario = new JSONObject();
        usuario.put("id", usuarioId);
        dep.put("usuario_id_fk", usuario);

        Response response = ApiClient.put(context, BASE, dep.toString());
        String resp = response.body().string();
        return new JSONObject(resp);
    }

    // ==========================================================
    //                         PATCH
    // ==========================================================

    // 🔹 Atualização parcial
    public static JSONObject atualizarParcial(Context context, int id, JSONObject dto) throws Exception {

        Response response = ApiClient.patch(context, BASE + "/" + id, dto.toString());
        String resp = response.body().string();
        return new JSONObject(resp);
    }

    // ==========================================================
    //                         DELETE
    // ==========================================================

    // 🔹 Deletar dependente
    public static boolean deletar(Context context, long id) throws Exception {
        Response response = ApiClient.delete(context, BASE + "/" + id);
        return response.isSuccessful();
    }

    public static List<Partida> getPartidasByDependente(Context context, int dependenteId) throws Exception {

        String url = BASE + "/infoJogosByDependente/" + dependenteId;

        Response response = ApiClient.get(context, url);
        String resp = response.body().string();

        JSONArray arr = new JSONArray(resp);

        List<Partida> lista = new ArrayList<>();

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);

            Gson gson = new Gson();
            Partida p = gson.fromJson(obj.toString(), Partida.class);
            lista.add(p);
        }

        return lista;
    }

    // ==========================================================
    //                         DOWNLOAD
    // ==========================================================

    public static void baixarRelatorioPdf(Context context, int dependenteId) {
        try {
            // Se o seu ApiClient tiver uma constante BASE_URL, use ela aqui.
            // Exemplo: "https://seu-backend.onrender.com/dependente/relatorio/123"
            String urlCompleta = Api.BASE_URL + BASE + "/exportPdf/" + dependenteId;
            Log.d("DOWNLOAD_URL", "URL: " + urlCompleta);
            String nomeArquivo = "Relatorio_Dependente_" + dependenteId + ".pdf";

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlCompleta));

            // Adicionando o Token de Autenticação exigido pelo Render
            request.addRequestHeader("Authorization", ApiClient.getToken(context));

            request.setTitle("Relatório Integra Kids");
            request.setDescription("Baixando PDF do dependente...");

            // Configura para salvar na pasta pública de Downloads
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nomeArquivo);

            // Faz aparecer na barra de notificações
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);

            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm != null) {
                dm.enqueue(request);
                Toast.makeText(context, "Download do PDF iniciado...", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("DOWNLOAD_DEBUG", "Erro ao iniciar download", e);
        }
    }

    // ==========================================================
//                     DOWNLOAD EXCEL
// ==========================================================

    public static void baixarRelatorioExcel(Context context, long dependenteId) {
        try {
            // Ajuste o endpoint conforme definido no seu backend (ex: /exportExcel)
            String endpoint = BASE + "/exportExcel/" + dependenteId;
            String urlCompleta = Api.BASE_URL + endpoint;
            String token = ApiClient.getToken(context);

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlCompleta));

            // Autenticação
            if (token != null && !token.isEmpty()) {
                String tokenFormatado = token.startsWith("Bearer ") ? token : "Bearer " + token;
                request.addRequestHeader("Authorization", tokenFormatado);
            }

            // Define que aceitamos um arquivo Excel
            request.addRequestHeader("Accept", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            // Nome do arquivo com extensão .xlsx
            String nomeArquivo = "Relatorio_Kids_" + dependenteId + "_" + System.currentTimeMillis() + ".xlsx";

            request.setTitle("Relatório Excel Integra Kids");
            request.setDescription("Baixando planilha de dados...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nomeArquivo);

            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm != null) {
                dm.enqueue(request);
                Toast.makeText(context, "Download do Excel iniciado...", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("DOWNLOAD_ERROR", "Erro ao baixar Excel: " + e.getMessage());
        }
    }

}
