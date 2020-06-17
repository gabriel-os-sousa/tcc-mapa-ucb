package com.gabrieldev.mapaucb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.activity.EventosActivity;
import com.gabrieldev.mapaucb.config.ConfiguracaoFirebase;
import com.gabrieldev.mapaucb.model.Evento;
import com.gabrieldev.mapaucb.model.Local;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;

public class AdapterEventos extends RecyclerView.Adapter<AdapterEventos.MyViewHolder>{
    List<Evento> eventos;
    DatabaseReference localRef;

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
    public void onBindViewHolder(@NonNull final AdapterEventos.MyViewHolder holder, int position) {
        Evento evento = eventos.get(position);

        holder.nome.setText(evento.getNome());
        //holder.local.setText(evento.getLocal());

        //Recuperar o nome do local
        localRef = ConfiguracaoFirebase.getFirebaseDatabase().child("locais");
        localRef.child(evento.getLocal()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Local local = dataSnapshot.getValue(Local.class);
                holder.local.setText(local.getNome());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                holder.local.setText("Local não encontrado");
            }
        });

        Calendar cInicial = Calendar.getInstance(EventosActivity.TIMEZONE);
        cInicial.setTimeInMillis(evento.getData_inicio());

        Calendar cFinal = Calendar.getInstance(EventosActivity.TIMEZONE);
        cFinal.setTimeInMillis(evento.getData_fim());

        //MONTH = 0 pra janeiro / DAY_OF_MONTH = 1 para o primeiro dia do mês
        //Adiciona um mês aos calêndários pois os meses começam no 0
        cInicial.add(Calendar.MONTH,1);
        cFinal.add(Calendar.MONTH,1);

        //se o dia e o meses forem iguais, evento de só um dia
        if((cInicial.get(Calendar.MONTH) == cFinal.get(Calendar.MONTH)) && (cInicial.get(Calendar.DAY_OF_MONTH) == cFinal.get(Calendar.DAY_OF_MONTH))) {
            String dataini = cInicial.get(Calendar.DAY_OF_MONTH )+"/"+cInicial.get(Calendar.MONTH);
            holder.data.setText(dataini);

        //se o evento for mais de um dia
        } /*else if (cInicial.get(Calendar.MONTH) != cFinal.get(Calendar.MONTH)){
            String datafim = ((cInicial.get(Calendar.DAY_OF_MONTH))+1)+"/"+cInicial.get(Calendar.MONTH)+" a "+cFinal.get(Calendar.DAY_OF_MONTH)+"/"+cFinal.get(Calendar.MONTH);
            holder.data.setText(datafim);
        }*/ else {
            String datafim = cInicial.get(Calendar.DAY_OF_MONTH)+"/"+cInicial.get(Calendar.MONTH)+" a "+cFinal.get(Calendar.DAY_OF_MONTH)+"/"+cFinal.get(Calendar.MONTH);
            holder.data.setText(datafim);
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
