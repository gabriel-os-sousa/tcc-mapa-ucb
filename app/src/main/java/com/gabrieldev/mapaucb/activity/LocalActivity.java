package com.gabrieldev.mapaucb.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.config.ConfiguracaoFirebase;
import com.gabrieldev.mapaucb.model.Local;
import com.gabrieldev.mapaucb.model.TipoLocal;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class LocalActivity extends AppCompatActivity implements OnMapReadyCallback {
    private TextView textViewNome, textViewDescricao, textViewTipo, textViewAndar;
    private Local local;
    private ImageView imagemDestaque;
    private GoogleMap mapa;
    private Marker marcador;

    private StorageReference storageRef;

    /*Ui Setting do Mapa*/
    private UiSettings uiSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local);

        //Configura a toolbar específica
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("");
        setSupportActionBar(toolbar); //Seta a toolbar configurada
        //Configura o item voltar na toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        /* Fecha a activity, pois ela pode ser iniciada de dois lugares.
            Ao fechar a activity, automaticamente vai para activity que a chamou
        */
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Storage Ref Firebase
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();


        //Configurações iniciais
        textViewNome = findViewById(R.id.textNomeLocalSelecionado);
        textViewDescricao = findViewById(R.id.textDescricaoLocalSelecionado);
        textViewTipo = findViewById(R.id.textTipoLocalSelecionado);
        textViewAndar = findViewById(R.id.textAndarLocalSelecionado);
        imagemDestaque = findViewById(R.id.imageLocalSelecionado);

        //Recuperar os dados do Local Selecionado
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            local = (Local) bundle.getSerializable("localSelecionado");
            toolbar.setTitle(local.getNome());//Muda o nome na toolbar

            textViewNome.setText(local.getNome());
            textViewDescricao.setText(local.getDescricao());

            if (local.getTipo().contains("Bloco")) {
                textViewTipo.setText("Bloco");
            } else {
                textViewTipo.setText(local.getTipo());
            }

            //Formatação do campo Andar
            String andar;
            if (local.getAndar() == 0) andar = "Térreo";
            else andar = local.getAndar()+"º Andar";

            textViewAndar.setText(andar);

            //Recupera imagem no storage
            String nomeImg = local.getId() + ".png";
            storageRef.child("locais").child(nomeImg)
                    .getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri.toString()).into(imagemDestaque);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LocalActivity.this, "Imagem não encontrada!", Toast.LENGTH_SHORT).show();
                }
            });

            //Inicializar mapa - Obtem o SupportMapFragment e notifica quando o mapa está pronto para uso
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_local);
            mapFragment.getMapAsync(this);

        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(local.getLatitude(), local.getLongitude()), 16));

        styleMap();
        configuraUi();
        adicionarMarcador();

        mapa.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                abrirMapaLocalSelecionado();
            }
        });

        mapa.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                abrirMapaLocalSelecionado();
                return true;
            }
        });
    }

    private void abrirMapaLocalSelecionado() {
        Intent i = new Intent(LocalActivity.this, MapaLocalActivity.class);
        i.putExtra("localSelecionado", local); //passar o local
        startActivity(i);
    }

    private void styleMap() {
        try {
            // Customiza o mapa baseado em um JSON definido
            // No Raw que está nos Resources.
            boolean success = mapa.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e("log", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("log", "Can't find style. Error: ", e);
        }
    }

    private void configuraUi() {
        uiSettings = mapa.getUiSettings();

        uiSettings.setMapToolbarEnabled(false);//botões de navegação e de abrir googlemaps
        uiSettings.setZoomControlsEnabled(false);//botão de zoom
        uiSettings.setCompassEnabled(false);//Compasso da bússula
        uiSettings.setRotateGesturesEnabled(false);//rotação
        uiSettings.setTiltGesturesEnabled(false);//inclinação
        uiSettings.setScrollGesturesEnabled(false);//dragging
        uiSettings.setZoomGesturesEnabled(false);//gesto de zoom no mapa
    }


    private void adicionarMarcador() {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(local.getLatitude(), local.getLongitude()));

        marcador = mapa.addMarker(markerOptions);
        switch (local.getTipo()) {
            case "Bloco":
                marcador.setTag(TipoLocal.MARKER_BLOCO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this,R.drawable.marker_blocos));
                break;
            case "Bloco A":
                marcador.setTag(TipoLocal.MARKER_BLOCO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this,R.drawable.marker_bloco_a));
                break;
            case "Bloco B":
                marcador.setTag(TipoLocal.MARKER_BLOCO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this,R.drawable.marker_bloco_b));
                break;
            case "Bloco C":
                marcador.setTag(TipoLocal.MARKER_BLOCO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this,R.drawable.marker_bloco_c));
                break;
            case "Bloco D":
                marcador.setTag(TipoLocal.MARKER_BLOCO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this,R.drawable.marker_bloco_d));
                break;
            case "Bloco E":
                marcador.setTag(TipoLocal.MARKER_BLOCO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this,R.drawable.marker_bloco_e));
                break;
            case "Bloco F":
                marcador.setTag(TipoLocal.MARKER_BLOCO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this,R.drawable.marker_bloco_f));
                break;
            case "Bloco G":
                marcador.setTag(TipoLocal.MARKER_BLOCO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this,R.drawable.marker_bloco_g));
                break;
            case "Bloco K":
                marcador.setTag(TipoLocal.MARKER_BLOCO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this,R.drawable.marker_bloco_k));
                break;
            case "Bloco L":
                marcador.setTag(TipoLocal.MARKER_BLOCO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this,R.drawable.marker_bloco_l));
                break;
            case "Bloco M":
                marcador.setTag(TipoLocal.MARKER_BLOCO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this,R.drawable.marker_bloco_m));
                break;
            case "Bloco R":
                marcador.setTag(TipoLocal.MARKER_BLOCO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this,R.drawable.marker_bloco_r));
                break;
            case "Bloco S":
                marcador.setTag(TipoLocal.MARKER_BLOCO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this,R.drawable.marker_bloco_s));
                break;
            case "Sala":
                marcador.setTag(TipoLocal.MARKER_SALA.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this, R.drawable.marker_sala));
                break;
            case "Esporte":
                marcador.setTag(TipoLocal.MARKER_ESPORTE.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this, R.drawable.marker_esporte));
                break;
            case "Refeição":
                marcador.setTag(TipoLocal.MARKER_REFEICOES.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this, R.drawable.marker_refeicao));
                break;
            case "Estacionamento":
                marcador.setTag(TipoLocal.MARKER_ESTACIONAMENTO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this, R.drawable.marker_estacionamento));
                break;
            case "Token Estacionamento":
                marcador.setTag(TipoLocal.MARKER_ESTACIONAMENTO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this, R.drawable.marker_token_estacionamento));
                break;
            case "Atendimento":
                marcador.setTag(TipoLocal.MARKER_ATENDIMENTO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this, R.drawable.marker_atendimento));
                break;
            case "Banheiro":
                marcador.setTag(TipoLocal.MARKER_BANHEIRO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this, R.drawable.marker_banheiro));
                break;
            case "Biblioteca":
                marcador.setTag(TipoLocal.MARKER_BIBLIOTECA.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this, R.drawable.marker_biblioteca));
                break;
            case "Laboratório":
                marcador.setTag(TipoLocal.MARKER_LABORATORIO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this, R.drawable.marker_laboratorio));
                break;
            case "Auditório":
                marcador.setTag(TipoLocal.MARKER_AUDITORIO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this, R.drawable.marker_auditorio));
                break;
            default:
                marcador.setTag(TipoLocal.MARKER_OUTRO.getTipo());
                marcador.setIcon(bitmapDescriptorFromVector(this, R.drawable.marker_outro));
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
