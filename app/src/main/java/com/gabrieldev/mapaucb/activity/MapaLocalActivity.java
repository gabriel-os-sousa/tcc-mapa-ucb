package com.gabrieldev.mapaucb.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.adapter.AdapterInfoWindow;
import com.gabrieldev.mapaucb.config.ConfiguracaoFirebase;
import com.gabrieldev.mapaucb.model.Local;
import com.gabrieldev.mapaucb.model.TipoLocal;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.StorageReference;

public class MapaLocalActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {
    private static final String TAG = "meulog";
    private FloatingActionButton fabLocalizacao;
    private Toolbar toolbar;

    /* Local */
    private Local local;

    /* Mapa */
    private GoogleMap mapa;
    private Marker marcador;

    /*Ui Setting do Mapa*/
    private UiSettings uiSettings;

    /* Storage firebase*/
    private StorageReference imgRef = ConfiguracaoFirebase.getFirebaseStorage();

    /*Localização do usuário*/
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Marker markerLocalizacaoUsuario;
    private LatLng locUsuario;

    /*Sensores*/
    SensorManager sensorManager;
    private Sensor sensor;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;

    private float[] valuesAccelerometer;
    private float[] valuesMagneticField;

    private float[] matrixR;
    private float[] matrixI;
    private float[] matrixValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_local);

        toolbar = findViewById(R.id.toolbarMapaLocal);
        setSupportActionBar(toolbar); //Seta a toolbar configurada

        //Configura o item voltar na toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        fabLocalizacao = findViewById(R.id.fab_localizacao_local);

        //Inicializar mapa - Obtem o SupportMapFragment e notifica quando o mapa está pronto para uso
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_local_selecionado);
        mapFragment.getMapAsync(this);

        sensor();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "start Mapa Local");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "resume Mapa Local Register sensor");
        getLocationManager();
        sensorManager.registerListener(this, sensorAccelerometer, 100000000);
        sensorManager.registerListener(this, sensorMagneticField, 100000000);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "pause Mapa Local Unregister sensor");
        sensorManager.unregisterListener(this, sensorAccelerometer);
        sensorManager.unregisterListener(this, sensorMagneticField);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "stop Mapa Local");

        // Remove atualizações de GPS quando a activity estiver em segundo plano
        removeUpdatesLocation();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "restart Mapa Local");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "destroy Mapa");

        // Remove atualizações de GPS quando a activity estiver em segundo plano
        removeUpdatesLocation();
        super.onDestroy();
    }

    /****************************************** Mapa **********************************************/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;

        styleMap();
        configuraUi();

        //Seta a infowindow customizada
        mapa.setInfoWindowAdapter(new AdapterInfoWindow(MapaLocalActivity.this));

        //Recuperar os dados do Local Selecionado
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            local = (Local) bundle.getSerializable("localSelecionado");
            toolbar.setTitle(local.getNome());//Muda o nome na toolbar

            adicionarMarcador();
            markerClickListener();

            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(local.getLatitude(),local.getLongitude()), 16));

            //Configura o botão de localização somente quando o mapa estiver pronto
            configuraFabLocalizacao();

            infoWindowClickListener();
        }
    }

    /********************************** Métodos Localização usuário *********************************/
    private void configuraFabLocalizacao() {
        fabLocalizacao.setVisibility(View.VISIBLE);
        fabLocalizacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) )
                    alertaGPS();
                if(locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ))
                    recuperarLocalizacaoUsuario();

                if(locUsuario != null){
                    mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(locUsuario, getZoomAtual()), 500, null);
                }
                locUsuario = null;
            }
        });
    }

    private void alertaGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Seu GPS parece estar desativado, deseja ativar para visualizar sua localização?")
                .setCancelable(false)
                .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void recuperarLocalizacaoUsuario() {
        getLocationManager();
        //locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //recuperar latitude e longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng meuLocal = new LatLng(latitude, longitude);

                if(markerLocalizacaoUsuario != null)
                    markerLocalizacaoUsuario.remove();
                markerLocalizacaoUsuario = mapa.addMarker(new MarkerOptions()
                        .position(meuLocal)
                        .title("Você está aqui!")
                        .flat(true) //alinha ao norte o marcador, mesmo que gire o mapa
                        .icon(bitmapDescriptorFromVector(MapaLocalActivity.this,R.drawable.marker_usuario_nav))
                        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
                        .anchor(0.5f, 0.5f));
                markerLocalizacaoUsuario.setTag(TipoLocal.MARKER_USER.getTipo());
                markerLocalizacaoUsuario.setRotation(location.getBearing());


                if (locUsuario == null) {
                    getZoomAtual();
                    mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(meuLocal, getZoomAtual()), 500, null);
                }
                locUsuario = meuLocal;
                //Log.d(TAG, "Changed: "+ latitude +" / "+longitude + "  --> "+location.getBearing());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                //Toast.makeText(MapaActivity.this, "GPS ativado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                if(markerLocalizacaoUsuario != null)
                    markerLocalizacaoUsuario.remove();
                locUsuario = null;
                //Toast.makeText(MapaActivity.this, "GPS desativado", Toast.LENGTH_SHORT).show();
            }
        };

        //Solicitar atualizações de localização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    10,
                    locationListener
            );
        }
    }

    private void getLocationManager() {
        if(locationManager == null)
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    private void removeUpdatesLocation(){
        if(locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }

    private float getZoomAtual() {
        return mapa.getCameraPosition().zoom;
    }

    /************************************** Métodos do Sensor *************************************/
    private void sensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        valuesAccelerometer = new float[3];
        valuesMagneticField = new float[3];

        matrixR = new float[9];
        matrixI = new float[9];
        matrixValues = new float[3];

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                for(int i =0; i < 3; i++){
                    valuesAccelerometer[i] = event.values[i];
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                for(int i =0; i < 3; i++){
                    valuesMagneticField[i] = event.values[i];
                }
                break;
        }

        boolean success = SensorManager.getRotationMatrix(matrixR, matrixI, valuesAccelerometer, valuesMagneticField);

        if(success){
            SensorManager.getOrientation(matrixR, matrixValues);

            double azimuth = Math.toDegrees(matrixValues[0]);
           /* double pitch = Math.toDegrees(matrixValues[1]);
            double roll = Math.toDegrees(matrixValues[2]);*/

            float f = (float)azimuth;
            if (markerLocalizacaoUsuario != null)
                markerLocalizacaoUsuario.setRotation(f);

            //Log.d(TAG, "onSensorChanged: azimuth: "+ String.valueOf(azimuth)+ " / Pitch: "+ String.valueOf(pitch) + " / Roll"+ String.valueOf(roll));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /********************************** Métodos Configuração Mapa *********************************/
    private void infoWindowClickListener() {
        mapa.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                finish();
            }
        });
    }

    private void markerClickListener() {
        mapa.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {

                if((Integer) marker.getTag() == TipoLocal.MARKER_USER.getTipo()) {
                    // false ele abre a infowindow
                    // true não abre a infowindow
                    return false;
                } else {
                    // Calcular o deslocamento horizontal necessário para a densidade atual da tela
                    final int dX = getResources().getDimensionPixelSize(R.dimen.map_dx);
                    // Calcular o deslocamento vertical necessário para a densidade atual da tela
                    final int dY = getResources().getDimensionPixelSize(R.dimen.map_dy);
                    final Projection projection = mapa.getProjection();
                    final Point markerPoint = projection.toScreenLocation(
                            marker.getPosition()
                    );
                    // Mudar o ponto que usaremos para centralizar o mapa
                    markerPoint.offset(dX, dY);
                    final LatLng novoCentro = projection.fromScreenLocation(markerPoint);

                    // Movimento de câmera suavizado
                    mapa.animateCamera(CameraUpdateFactory.newLatLng(novoCentro), 500, null);

                    /* A idéia aqui é passar no snippet do Marker:
                        - nome da imagem (nome da image = id do local; que será usado para pegar a imagem do storage no firebase) e
                        - url que será recuperado a partir do nome da imagem (que será usado para fazer download da imagem)
                     */
                    String snippet = marker.getSnippet();
                    /* Aqui verifica o nome da imagem (id do local), que sempre é salvo no campo snippet do marker entre "(" ")"*/

                    String nomeImg = snippet.substring(snippet.indexOf("(") + 1, snippet.indexOf(")"));

                    /* Aqui recupera a Url do local, a partir do nome da imagem recuperado acima*/

                    imgRef.child("locais").child(nomeImg + ".png").getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String snippet = marker.getSnippet();
                                    /* Aqui verifica se já contém a url concatenada no campo snippet do marker entre "[" "]"
                                     *  Se não tiver a url salva no snippet, realiza o processo de concatenação
                                     * */
                                    if (!snippet.contains("[") && !snippet.contains("]")) {
                                        String img = snippet + "[" + uri + "]";
                                        marker.setSnippet(img);
                                    }
                                    marker.showInfoWindow();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            marker.showInfoWindow();
                        }
                    });

                    return true;
                }
            }
        });
    }

    private void adicionarMarcador() {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(local.getLatitude(), local.getLongitude()))
                .title(local.getNome())
                .snippet("(" + local.getId() + ")");// Colocar o Id do local que será usado para recuperar a imagem do firebase

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
    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
