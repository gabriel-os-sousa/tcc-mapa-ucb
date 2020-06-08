package com.gabrieldev.mapaucb.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gabrieldev.mapaucb.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class AdapterInfoWindow implements GoogleMap.InfoWindowAdapter {
    private final View mWindow;
    private Context mContext;

    public AdapterInfoWindow(Context context) {
        this.mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.info_window, null);
    }

       @Override
    public View getInfoWindow(Marker marker) {
       return null;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        //recupera as referencias dos objetos da view
        TextView tvTitulo = (TextView) mWindow.findViewById(R.id.adapterInfoWindowTitulo);
        ImageView imgInfo = (ImageView) mWindow.findViewById(R.id.adapterInfoWindowImagem);

        //Seta os valores
        tvTitulo.setText(marker.getTitle());

        /*Recupera o Url que está salvo no campo snippet do marker entre "[" "]"*/
        String snippet = marker.getSnippet();


        /*Se não tiver imagem no Firebase Storage, colocar imagem padrão*/
        if(snippet.contains("[") && snippet.contains("]")) {
            String url;
            url = snippet.substring(snippet.indexOf("[") + 1, snippet.indexOf("]"));
            Picasso.get()
                    .load(url)
                    //.fit() //redimensionar causa bug e a imagem só é renderizada no sengundo clicle no infowindow
                    .error(R.drawable.default_img)
                    .into(imgInfo, new MarkerCallback(marker, url,imgInfo));
        } else {
            Drawable url = mContext.getResources().getDrawable(R.drawable.default_img);
            imgInfo.setImageDrawable(url);
        }

        return mWindow;
    }

    /*Classe de Callback para a biblioteca Picasso*/
    public class MarkerCallback implements Callback {
        Marker marker = null;
        String URL;
        ImageView userPhoto;

        MarkerCallback(Marker marker, String URL, ImageView userPhoto) {
            this.marker = marker;
            this.URL = URL;
            this.userPhoto = userPhoto;
        }
        @Override
        public void onSuccess() {

            if (marker != null && marker.isInfoWindowShown()) {
                marker.hideInfoWindow();
                Picasso.get().load(URL).into(userPhoto);
                marker.showInfoWindow();
            }
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }
    }
}
