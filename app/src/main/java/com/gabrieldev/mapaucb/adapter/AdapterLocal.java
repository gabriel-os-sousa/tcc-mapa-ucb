package com.gabrieldev.mapaucb.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.model.Local;

import java.util.List;

public class AdapterLocal extends RecyclerView.Adapter<AdapterLocal.MyViewHolder> {

    List<Local> locais;

    public AdapterLocal(List<Local> locais) {
        this.locais = locais;
    }

    @NonNull
    @Override
    public AdapterLocal.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_lista_locais, parent, false);

        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterLocal.MyViewHolder holder, int position) {
        Local local = locais.get(position);

        holder.nome.setText(local.getNome());
        holder.descricao.setText(local.getDescricao());
//        holder.image.setImageDrawable(local.getDescricao());
        /*holder.latitude.setText(String.valueOf(local.getLatitude()));
        holder.longitude.setText(String.valueOf(local.getLongitude()));*/
    }

    @Override
    public int getItemCount() {
        return locais.size();
    }

    //Guarda cada dado antes de ser mostrado na tela
    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nome, descricao;
        ImageView image;
        //TextView latitude, longitude;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            //itemView é responsável por acessar os id's
            nome = itemView.findViewById(R.id.textEventoNome);
            descricao = itemView.findViewById(R.id.textEventoData);
            /*TODO: Adicionar miniatura de imagem*/
//            image = itemView.findViewById(R.id.textDescricao);
            /*latitude = itemView.findViewById(R.id.textLatitude);
            longitude = itemView.findViewById(R.id.textLongitude);*/
        }
    }
}
