package com.gabrieldev.mapaucb.model;

import java.io.Serializable;

public class Evento implements Serializable {
    private String id;
    private Long data_fim;
    private Long data_inicio;
    private String descricao;
    private String local;
    private String nome;
    private String tipo;
    private String horario;
    private Integer zIndex;

    public Evento() {
    }

    public Long getData_fim() {
        return data_fim;
    }

    public void setData_fim(Long data_fim) {
        this.data_fim = data_fim;
    }

    public Long getData_inicio() {
        return data_inicio;
    }

    public void setData_inicio(Long data_inicio) {
        this.data_inicio = data_inicio;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public Integer getzIndex() {
        return zIndex;
    }

    public void setzIndex(Integer zIndex) {
        this.zIndex = zIndex;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
