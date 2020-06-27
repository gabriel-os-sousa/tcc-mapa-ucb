package com.gabrieldev.mapaucb.model;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Calendar;

public class Local implements Serializable {
    private String id;
    private String nome;
    private String tipo;
    private String descricao;
    private double latitude;
    private double longitude;
    private int zIndex;
    private Long dataCadastro;
    private Integer andar;

    public Local() {
    }

    @NonNull
    @Override
    public String toString() {
        return " Nome: "+this.getNome()+" - Desc: "+this.getDescricao();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getzIndex() {
        return zIndex;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Integer getAndar() {
        return andar;
    }

    public void setAndar(Integer andar) {
        this.andar = andar;
    }

    public Long getDataCadastro() {
        return dataCadastro;
    }

    @Exclude
    public Calendar getDataCadastroCalendar() {
        if (this.dataCadastro == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(this.dataCadastro);
        return c;
    }

    public void setDataCadastro(Long dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}
