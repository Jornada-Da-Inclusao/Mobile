package com.example.integra_kids_mobile.model;

public class Partida {
    private int id;
    private int jogoId;
    private int dependenteId;
    private String nomeJogo;
    private double tempoTotal;
    private int totalTentativas;
    private int totalAcertos;
    private int totalErros;
    private String createDate;
    private String updateDate;

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getJogoId() { return jogoId; }
    public void setJogoId(int jogoId) { this.jogoId = jogoId; }

    public int getDependenteId() { return dependenteId; }
    public void setDependenteId(int dependenteId) { this.dependenteId = dependenteId; }

    public String getNomeJogo() { return nomeJogo != null ? nomeJogo : "Jogo desconhecido"; }
    public void setNomeJogo(String nomeJogo) { this.nomeJogo = nomeJogo; }

    public double getTempoTotal() { return tempoTotal; }
    public void setTempoTotal(double tempoTotal) { this.tempoTotal = tempoTotal; }

    public int getTentativas() { return totalTentativas; }
    public void setTentativas(int totalTentativas) { this.totalTentativas = totalTentativas; }

    public int getAcertos() { return totalAcertos; }
    public void setAcertos(int totalAcertos) { this.totalAcertos = totalAcertos; }

    public int getErros() { return totalErros; }
    public void setErros(int totalErros) { this.totalErros = totalErros; }

    public String getCreateDate() { return createDate; }
    public void setCreateDate(String createDate) { this.createDate = createDate; }

    public String getUpdateDate() { return updateDate; }
    public void setUpdateDate(String updateDate) { this.updateDate = updateDate; }
}