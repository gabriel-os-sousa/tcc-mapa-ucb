package com.gabrieldev.mapaucb.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.model.Local;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;

public class AddLocalActivity extends AppCompatActivity implements OnMapReadyCallback{
    private EditText textViewNome, textViewDescricao, textViewLatitude, textViewLongitude, textViewTipo;
    private DatabaseReference localRef;

    private GoogleMap mapa2;

    public static final LatLng UCB = new LatLng(-15.867310, -48.0305822);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_local);

        //Configurações iniciais
        textViewNome = findViewById(R.id.editNomeAdd);
        textViewDescricao = findViewById(R.id.editDescricaoAdd);
        textViewLatitude = findViewById(R.id.editLatitudeAdd);
        textViewLongitude = findViewById(R.id.editLongitudeAdd);
        textViewTipo = findViewById(R.id.editTipoAdd);

        //Inicializar mapa
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapAddLocal);
        mapFragment.getMapAsync(this);

    }

    public void validarCamposLocal (View view) {

        //recuperar textos dos campos
        String textoNome = textViewNome.getText().toString();
        String textoDescricao = textViewDescricao.getText().toString();
        String textoLatitude = textViewLatitude.getText().toString();
        String textoLongitude = textViewLongitude.getText().toString();
        String textoTipo = textViewTipo.getText().toString();

        //Verifica se os campos estão vazios
        if (textoNome.isEmpty()) {
            Toast.makeText(this, "Preencha o nome!", Toast.LENGTH_SHORT).show();
        } else if (textoDescricao.isEmpty()) {
            Toast.makeText(this, "Preencha a descrição!", Toast.LENGTH_SHORT).show();
        } else if (textoLatitude.isEmpty()) {
            Toast.makeText(this, "Preencha a latitude!", Toast.LENGTH_SHORT).show();
        } else if (textoLongitude.isEmpty()) {
            Toast.makeText(this, "Preencha a longitude!", Toast.LENGTH_SHORT).show();
        } else if (textoTipo.isEmpty()) {
            Toast.makeText(this, "Preencha o Tipo!", Toast.LENGTH_SHORT).show();
        } else {
            Local local = new Local();

            local.setNome(textoNome);
            local.setDescricao(textoDescricao);
            local.setLatitude(Double.parseDouble(textoLatitude));
            local.setLongitude(Double.parseDouble(textoLongitude));
            local.setTipo(textoTipo);
            local.setzIndex(1);
            local.salvar();
            finish();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa2 = googleMap;
        mapa2.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        mapa2.getUiSettings().setMapToolbarEnabled(false);
        mapa2.getUiSettings().setZoomControlsEnabled(true);

        mapa2.moveCamera(CameraUpdateFactory.newLatLngZoom(UCB,16));

        mapa2.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapa2.clear();
                mapa2.addMarker(new MarkerOptions().position(latLng));
                textViewLatitude.setText(String.valueOf(latLng.latitude));
                textViewLongitude.setText(String.valueOf(latLng.longitude));
            }
        });

    }
}
