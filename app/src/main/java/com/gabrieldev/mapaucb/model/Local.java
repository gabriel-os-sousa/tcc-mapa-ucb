package com.gabrieldev.mapaucb.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.gabrieldev.mapaucb.config.ConfiguracaoFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.List;

public class Local implements Serializable {
    private String id;
    private String nome;
    private String tipo;
    private String descricao;
    private double latitude;
    private double longitude;
    private int zIndex;
    private List<Local> localVizinho;

    public Local() {
    }

    @NonNull
    @Override
    public String toString() {
        return " Nome: "+this.getNome()+" - Desc: "+this.getDescricao();
    }


    public void salvar() {
        DatabaseReference firebase = ConfiguracaoFirebase.getFirebaseDatabase().child("locais");
        //.child(this.id) //salva o id único codificado em Base64
        this.setId(firebase.push().getKey());
        firebase.child(this.id).setValue(this);
        Log.d("salvarL", ""+ this.id);
        Log.d("salvarL", ""+ this.nome);
        Log.d("salvarL", ""+ this.descricao);
        Log.d("salvarL", ""+ this.latitude);
        Log.d("salvarL", ""+ this.longitude);
        Log.d("salvarL", ""+ this.tipo);
        Log.d("salvarL", ""+ this.zIndex);
    }

    @Exclude
    public List<Local> getLocalVizinho() {
        return localVizinho;
    }

    public void setLocalVizinho(List<Local> localVizinho) {
        this.localVizinho = localVizinho;
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
}
