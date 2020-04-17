package com.gabrieldev.mapaucb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gabrieldev.mapaucb.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class AdapterInfoWindow implements GoogleMap.InfoWindowAdapter {
    private final View mWindow;
    private Context mContext;

    public AdapterInfoWindow(Context context) {
        this.mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.info_window, null);
    }

    private void renderWindowText(Marker marker, View v) {
        //recupera as referencias dos objetos da view
        TextView tvTitulo = (TextView) v.findViewById(R.id.adapterInfoWindowTitulo);
        TextView tvDescricao = (TextView) v.findViewById(R.id.adapterInfoWindowDescricao);
        ImageView ivImagem = (ImageView) v.findViewById(R.id.adapterInfoWindowImagem);

        //Seta os valores
        tvTitulo.setText(marker.getTitle());
        tvDescricao.setText(marker.getSnippet());
        ivImagem.setImageResource(R.drawable.default_img);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        //renderWindowText(marker, mWindow);
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderWindowText(marker, mWindow);
        return mWindow;
    }
}
