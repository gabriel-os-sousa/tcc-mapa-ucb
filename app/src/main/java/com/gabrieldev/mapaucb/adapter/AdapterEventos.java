package com.gabrieldev.mapaucb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.model.Evento;

import java.util.List;

public class AdapterEventos extends RecyclerView.Adapter<AdapterEventos.MyViewHolder>{
    List<Evento> eventos;

    public AdapterEventos(List<Evento> eventos) {
        this.eventos = eventos;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_eventos, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterEventos.MyViewHolder holder, int position) {
        Evento evento = eventos.get(position);

        holder.nome.setText(evento.getNome());
        holder.local.setText(evento.getLocal());
        if (evento.getDia_inicio().equals(evento.getDia_fim())) { //se o evento for de apenas um dia, muda o texto
            holder.diaInicio.setText(evento.getDia_inicio());
        } else {
            holder.diaInicio.setText(evento.getDia_inicio() +" a "+ evento.getDia_fim());
        }
    }

    @Override
    public int getItemCount() {
        return eventos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nome, local, diaInicio;

        public MyViewHolder(View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.textEventoNome);
            local = itemView.findViewById(R.id.textEventoLocal);
            diaInicio = itemView.findViewById(R.id.textEventoDiaInicio);
        }
    }
}
