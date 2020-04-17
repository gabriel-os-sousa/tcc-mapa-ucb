package com.gabrieldev.mapaucb.model;

public class Evento {
    private String nome;
    private String descricao;
    private String dia_inicio;
    private String dia_fim;
    private String tipo;
    private String local;

    public Evento() {
    }

    public void salvar () {

    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDia_inicio() {
        return dia_inicio;
    }

    public void setDia_inicio(String dia_inicio) {
        this.dia_inicio = dia_inicio;
    }

    public String getDia_fim() {
        return dia_fim;
    }

    public void setDia_fim(String dia_fim) {
        this.dia_fim = dia_fim;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }
}
