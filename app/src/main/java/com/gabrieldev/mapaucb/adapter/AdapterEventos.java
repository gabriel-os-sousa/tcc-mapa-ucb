package com.gabrieldev.mapaucb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.model.Evento;

import java.util.Calendar;
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

        Calendar cInicial = Calendar.getInstance();
        cInicial.setTimeInMillis(evento.getData_inicio());
        Calendar cFinal = Calendar.getInstance();
        cFinal.setTimeInMillis(evento.getData_fim());

        //MONTH = 0 pra janeiro / DAY_OF_MONTH = 1 para o primeiro dia do mês
        //Adiciona um mês aos calêndários pois os meses começam no 0
        cInicial.add(Calendar.MONTH,1);
        cFinal.add(Calendar.MONTH,1);

        if((cInicial.get(Calendar.MONTH) == cFinal.get(Calendar.MONTH)) && (cInicial.get(Calendar.DAY_OF_MONTH) == cFinal.get(Calendar.DAY_OF_MONTH))) {
            String data = cInicial.get(Calendar.DAY_OF_MONTH )+"/"+cInicial.get(Calendar.MONTH);
            holder.data.setText(data);
        } else {
            String data = cInicial.get(Calendar.DAY_OF_MONTH)+"/"+cInicial.get(Calendar.MONTH)+" a "+cFinal.get(Calendar.DAY_OF_MONTH)+"/"+cFinal.get(Calendar.MONTH);
            holder.data.setText(data);
        }

    }

    @Override
    public int getItemCount() {
        return eventos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nome, local, data;

        public MyViewHolder(View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.textEventoNome);
            local = itemView.findViewById(R.id.textEventoLocal);
            data = itemView.findViewById(R.id.textEventoData);
        }
    }
}
