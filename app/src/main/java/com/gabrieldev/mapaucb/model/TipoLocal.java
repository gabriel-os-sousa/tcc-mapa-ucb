package com.gabrieldev.mapaucb.model;

public enum TipoLocal {
    MARKER_USER(10),
    MARKER_LOCAL(20),
    MARKER_EVENTO(2000),
    MARKER_BLOCO(30),
    MARKER_SALA(40),
    MARKER_ESPORTE(50),
    MARKER_REFEICOES(60),
    MARKER_ESTACIONAMENTO(70),
    MARKER_ATENDIMENTO(80),
    MARKER_BANHEIRO(90),
    MARKER_BIBLIOTECA(100),
    MARKER_AUDITORIO(110),
    MARKER_LABORATORIO(120),
    MARKER_TOKEN_ESTACIONAMENTO(120),
    MARKER_OUTRO(200);

    private Integer tipo;

    TipoLocal(Integer tipo) {
        this.tipo = tipo;
    };

    public Integer getTipo() {
        return tipo;
    }
}
