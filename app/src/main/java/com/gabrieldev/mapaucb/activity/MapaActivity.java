package com.gabrieldev.mapaucb.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.adapter.AdapterInfoWindow;
import com.gabrieldev.mapaucb.config.ConfiguracaoFirebase;
import com.gabrieldev.mapaucb.dijkstra.Dijkstra;
import com.gabrieldev.mapaucb.dijkstra.Graph;
import com.gabrieldev.mapaucb.dijkstra.Node;
import com.gabrieldev.mapaucb.helper.Permissoes;
import com.gabrieldev.mapaucb.model.Evento;
import com.gabrieldev.mapaucb.model.Local;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback {
    /*Google Maps*/
    private GoogleMap mapa;
    private List<Marker> marcadores = new ArrayList<>() ;
    private Button buttonToogleMarcadores;

    /*Para carregar a lista de locais*/
    private List<Local> listaLocais = new ArrayList<>();

    /*Para carregar a lista de eventos. */
    private List<Evento> listaEventos = new ArrayList<>();

    /*Firebase*/
    private DatabaseReference locaisRef;
    private ValueEventListener valueEventListenerLocais;
    private DatabaseReference eventosRef;
    private ValueEventListener valueEventListenerEventos;
    private StorageReference imgRef = ConfiguracaoFirebase.getFirebaseStorage();

    /*Verificar usuário*/
    private FirebaseAuth autenticacao;
    private FirebaseUser firebaseUser;

    /*Verificar permissões*/
    private String[] permissoes = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    /*Menu Flutuante*/
    private FloatingActionButton fabMapa;
    private FloatingActionButton fabLocais;
    private FloatingActionButton fabEventos;
    private FloatingActionMenu fabMenu;
    private FloatingActionButton fabLocalizacao;

    /*Ui Setting do Mapa*/
    private UiSettings uiSettings;

    /*Tag log*/
    private final String TAG = "meulog";

    /*Location*/
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        //Configura a toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Mapa");
        setSupportActionBar(toolbar);

        //Validar permissões
        Permissoes.validarPermissoes(permissoes, this, 1);

        //Configura a referência do banco de dados / autenticacao
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        locaisRef = ConfiguracaoFirebase.getFirebaseDatabase().child("locais");
        eventosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("eventos");
        buttonToogleMarcadores = findViewById(R.id.toogleMarcadores);

        //Configura os Fab's do menu flutuante
        fabLocais = findViewById(R.id.floating_menu_locais);
        fabEventos = findViewById(R.id.floating_menu_eventos);
        fabMapa = findViewById(R.id.floating_menu_mapa);
        fabMenu = findViewById(R.id.fab_menu_principal);
        fabLocalizacao = findViewById(R.id.fab_localizacao);


        //Inicializar mapa - Obtem o SupportMapFragment e notifica quando o mapa está pronto para uso
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Menu flutuante
        configuraMenuFlutuante();
        configuraFabLocalizacao();
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarLocais();
        recuperarEventos();
        invalidateOptionsMenu(); // O onCreateOptionsMenu( ) é chamado novamente
        Log.d("lifecycle ", "start Mapa");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("lifecycle ", "stop Mapa");
        locaisRef.removeEventListener(valueEventListenerLocais);
        eventosRef.removeEventListener(valueEventListenerEventos);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        invalidateOptionsMenu();
        Log.d("lifecycle ", "restart Mapa");
    }

    @Override
    protected void onDestroy() {
        Log.d("lifecycle ", "destroy Mapa");
        super.onDestroy();
    }

    /************************************** Métodos do Mapa****************************************/
    // Manipula o Mapa quando ele estiver disponível(Terminou de ser carregado)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng centro_ucb = new LatLng(-15.8660717, -48.0305822);
        mapa = googleMap;

        moverCamera(centro_ucb,16);
        styleMap();
        configuraUi(); //Configuração de botões e Ui do Mapa
        infoWindowCustomizada(); //Configura a infowindo
        infoWindowClickListener();
        markerClickListener();

        //rotas();
        //pontosRotas();

    }

    /************************************ TesteCalcularRota ***************************************/
    public void testeCalcularDistancia(View view) {

        /*//Adicionando os nós (No meu caso vai ser os marcadores)
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");
        Node nodeD = new Node("D");
        Node nodeE = new Node("E");
        Node nodeF = new Node("F");


        //Adicioando os destinos possíveis a partir do nó (No meu caso vai ser o outro marcador e a distancia entre eles)
        nodeA.addDestination(nodeB, 10);
        nodeA.addDestination(nodeC, 15);

        nodeB.addDestination(nodeD, 12);
        nodeB.addDestination(nodeF, 15);

        nodeC.addDestination(nodeE, 10);

        nodeD.addDestination(nodeE, 2);
        nodeD.addDestination(nodeF, 1);

        nodeF.addDestination(nodeE, 5);



        Graph graph = new Graph();

        graph.addNode(nodeA);
        graph.addNode(nodeB);
        graph.addNode(nodeC);
        graph.addNode(nodeD);
        graph.addNode(nodeE);
        graph.addNode(nodeF);

        graph = Dijkstra.calculateShortestPathFromSource(graph, nodeA);

        List<Node> shortestPathForNodeB = Arrays.asList(nodeA);
        List<Node> shortestPathForNodeC = Arrays.asList(nodeA);
        List<Node> shortestPathForNodeD = Arrays.asList(nodeA, nodeB);
        List<Node> shortestPathForNodeE = Arrays.asList(nodeA, nodeB, nodeD);
        List<Node> shortestPathForNodeF = Arrays.asList(nodeA, nodeB, nodeD);

        for (Node node : graph.getNodes()) {
            switch (node.getName()) {
                case "B":
                    Log.d("TesteD", "B :"+ node.getShortestPath().equals(shortestPathForNodeB));
                    for (Node nodet :shortestPathForNodeB) {
                        Log.d("TesteD", "B -->: "+ nodet.getName());
                    }
                    break;
                case "C":
                    Log.d("TesteD", "C :"+ node.getShortestPath().equals(shortestPathForNodeC));
                    for (Node nodet :shortestPathForNodeC) {
                        Log.d("TesteD", "C -->: "+ nodet.getName());
                    }
                    break;
                case "D":
                    Log.d("TesteD", "D :"+ node.getShortestPath().equals(shortestPathForNodeD));
                    for (Node nodet :shortestPathForNodeD) {
                        Log.d("TesteD", "D -->: "+ nodet.getName());
                    }
                    break;
                case "E":
                    Log.d("TesteD", "E :"+ node.getShortestPath().equals(shortestPathForNodeE));
                    for (Node nodet :shortestPathForNodeE) {
                        Log.d("TesteD", "E -->: "+ nodet.getName());
                    }
                    break;
                case "F":
                    Log.d("TesteD", "F :"+ node.getShortestPath().equals(shortestPathForNodeF));
                    for (Node nodet :shortestPathForNodeF) {
                        Log.d("TesteD", "F -->: "+ nodet.getName());
                    }
                    break;
            }
        }*/
    }

    public void testeCalcularRota(View view) {

        //Adicionando os nós (No meu caso vai ser os marcadores)
        /*Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");
        Node nodeD = new Node("D");
        Node nodeE = new Node("E");
        Node nodeF = new Node("F");*/

        /*
         *  -15.866191, -48.031270 , divisa biblioteca e estacionamento dos professores
         *  -15.865556, -48.031541 , frente da biblioteca
         *  -15.865200, -48.031146 , norte da biblioteca
         *  -15.865500, -48.030127 , biblioteca com bloco m
         *  -15.865644, -48.030197 , frente do bloco M
         *  -15.864643, -48.029840 , frente do bloco K
         * */


        Node nodeMinhaLoc = new Node("Minha Loc", new LatLng(-15.864794, -48.030199));
        Node nodeA = new Node("frente do bloco K", new LatLng(-15.864643, -48.029840));
        Node nodeB = new Node("biblioteca com bloco m", new LatLng(-15.865500, -48.030127));
        Node nodeC = new Node("Norte da Biblioteca", new LatLng(-15.865200, -48.031146));
        Node nodeD = new Node("Frente do Bloco M", new LatLng(-15.865644, -48.030197));
        Node nodeF = new Node("frente biblioteca", new LatLng(-15.865556, -48.031541));
        Node nodeG = new Node("Divisa biblioteca, estacionamento professores", new LatLng(-15.866191, -48.031270));


        float[] distancia = new float[1];


        // frente do bloco K
        Location.distanceBetween(-15.864643, -48.029840 , -15.865500, -48.030127, distancia);
        nodeA.addDestination(nodeB, distancia[0]);

        //biblioteca com bloco M
        Location.distanceBetween(-15.865500, -48.030127 ,  -15.865200, -48.031146, distancia);
        nodeB.addDestination(nodeC, distancia[0]);

        Location.distanceBetween(-15.865500, -48.030127 , -15.865644, -48.030197, distancia);
        nodeB.addDestination(nodeD, distancia[0]);

        Location.distanceBetween(-15.865500, -48.030127 , -15.864643, -48.029840, distancia);
        nodeB.addDestination(nodeA, distancia[0]);


        // Norte da Biblioteca
        Location.distanceBetween(-15.865200, -48.031146, -15.865556, -48.031541, distancia);
        nodeC.addDestination(nodeF, distancia[0]);

        Location.distanceBetween(-15.865200, -48.031146, -15.865500, -48.030127, distancia);
        nodeC.addDestination(nodeB, distancia[0]);

        // Frente do Bloco M
        Location.distanceBetween(-15.865644, -48.030197, -15.866191, -48.031270, distancia);
        nodeD.addDestination(nodeG, distancia[0]);

        Location.distanceBetween(-15.865644, -48.030197, -15.865500, -48.030127, distancia);
        nodeD.addDestination(nodeB, distancia[0]);


        //frente biblioteca
        Location.distanceBetween(-15.865556, -48.031541 , -15.865200, -48.031146, distancia);
        nodeF.addDestination(nodeC, distancia[0]);

        Location.distanceBetween(-15.865556, -48.031541 , -15.866191, -48.031270, distancia);
        nodeF.addDestination(nodeG, distancia[0]);

        //Divisa biblioteca, estacionamento professores
        Location.distanceBetween(-15.866191, -48.031270, -15.865556, -48.031541, distancia);
        nodeG.addDestination(nodeF, distancia[0]);

        Location.distanceBetween(-15.866191, -48.031270, -15.865644, -48.030197, distancia);
        nodeG.addDestination(nodeD, distancia[0]);

        Graph graph = new Graph();
        graph.addNode(nodeA);
        graph.addNode(nodeB);
        graph.addNode(nodeC);
        graph.addNode(nodeD);
        graph.addNode(nodeF);
        graph.addNode(nodeG);


        graph = Dijkstra.calculateShortestPathFromSource(graph, nodeA);


        for (Node node : graph.getNodes()){
            Log.d("TesteD", "-----> "+ node.getName());
            if (node.getName().equals("Divisa biblioteca, estacionamento professores")) {

                List<LatLng> coordList = new ArrayList<>();

                for (Node nodeLatLng: node.getShortestPath()) {
                    coordList.add(nodeLatLng.getLatLng());
                    Log.d("TesteD", "LatLng"+nodeLatLng.getLatLng());
                }

                coordList.add(node.getLatLng()); //adiciona o ultimo

                PolylineOptions polylineOptions = new PolylineOptions();

                // Cria uma polyline options com todos os LatLng em coordlist
                polylineOptions.addAll(coordList);
                polylineOptions.width(10).color(Color.RED);

                // Adding multiple points in map using polyline and arraylist
                mapa.addPolyline(polylineOptions);
            }

            for (Node node2: node.getShortestPath()) {
                Log.d("TesteD", "-> "+ node2.getName());
            }
        }



    }

    private void pontosRotas () {
        ArrayList<LatLng> markers = new ArrayList<>();

        /*markers.add(new LatLng(-15.864650, -48.029828));
        markers.add(new LatLng(-15.865501, -48.030114));
        markers.add(new LatLng(-15.865709, -48.030177));
        markers.add(new LatLng(-15.865474, -48.030324));
        markers.add(new LatLng(-15.865823, -48.030408));
        markers.add(new LatLng(-15.865503, -48.031526));
        markers.add(new LatLng(-15.866228, -48.031236));
        markers.add(new LatLng(-15.866345, -48.030405));
        markers.add(new LatLng(-15.866688, -48.031079));
        markers.add(new LatLng(-15.865743, -48.030076));*/

       /* Marker m1 = mapa.addMarker(new MarkerOptions().position(new LatLng(-15.867621, -48.030888))
                .title("m1: "+-15.867621+", "+-48.030888));
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(-15.867621, -48.030888))
                .title("m1: "+-15.867621+", "+-48.030888);

        Marker m2;
        Marker m3;
        Marker m4;
        Marker m5;



            marcador = mapa.addMarker(markerOptions);*/

        float[] results = new float[1];
        Location.distanceBetween(-15.865743, -48.030076, -15.864650, -48.029828, results);

        Log.d(TAG, "distancia: "+ results[0]);

    }

    private void rotas () {
        Polyline p1 = mapa.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(
                        new LatLng(-15.864692, -48.029663),
                        new LatLng(-15.864650, -48.029830),
                        new LatLng(-15.865500, -48.030111) //fim intersecção
                ));
        p1.setTag("A");

        Polyline p2 = mapa.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(
                        new LatLng(-15.865500, -48.030111),
                        new LatLng(-15.865715, -48.030190)
                ));
        p2.setTag("B");

        Polyline p3 = mapa.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(
                        new LatLng(-15.865715, -48.030190),
                        new LatLng(-15.865844, -48.030470),
                        new LatLng(-15.866248, -48.031250)
                ));
        p3.setTag("C");

        stylePolyline(p1);
        stylePolyline(p2);
        stylePolyline(p3);

    }

    private void stylePolyline(Polyline polyline) {
        String type = "";
        // Get the data object stored with the polyline.
        if (polyline.getTag() != null) {
            type = polyline.getTag().toString();
        }

        switch (type) {

            case "A":
                polyline.setColor(0xffF9A825);
                break;
            case "B":
                polyline.setColor(0xff81C784);
                break;
            case "C":
                polyline.setColor(0xffF57F17);
                break;
            case "D":
                polyline.setColor(0xff388E3C);
                break;
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
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    public void moverCamera(LatLng local, int zoom) {
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(local,zoom));
    }

    public void moverCamera(LatLng local) {
        mapa.moveCamera(CameraUpdateFactory.newLatLng(local));
    }

    private void infoWindowClickListener() {
        mapa.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Local localSelecionadoInfoWindow = null;

                for (Local local : listaLocais){
                    if (marker.getTitle().contains(local.getNome())) {
                        localSelecionadoInfoWindow = local;
                    }
                }

                Intent i = new Intent(MapaActivity.this, LocalActivity.class);
                i.putExtra("localSelecionado", localSelecionadoInfoWindow);
                startActivity(i);
            }
        });

    }

    private void markerClickListener() {
        mapa.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
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

                    imgRef.child("locais").child(nomeImg+ ".png").getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String snippet = marker.getSnippet();
                                    /* Aqui verifica se já contém a url concatenada no campo snippet do marker entre "[" "]"
                                     *  Se não tiver a url salva no snippet, realiza o processo de concatenação
                                     * */
                                    if(!snippet.contains("[") && !snippet.contains("]")) {
                                        String img = snippet+"["+uri+"]";
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
        });
    }

    private void configuraUi() {
        uiSettings = mapa.getUiSettings();

        uiSettings.setMapToolbarEnabled(false);//botões de navegação e de abrir googlemaps
        uiSettings.setZoomControlsEnabled(false);//botão de zoom
        uiSettings.setCompassEnabled(false);//Compasso da bússula
        uiSettings.setRotateGesturesEnabled(false);//rotação
        uiSettings.setTiltGesturesEnabled(false);//inclinação

    }

    private void infoWindowCustomizada() {
        /* Seta a InfoWindow Customizada*/
        mapa.setInfoWindowAdapter(new AdapterInfoWindow(MapaActivity.this));
    }

    public void recuperarLocais() {

        valueEventListenerLocais = locaisRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaLocais.clear();//limpa a lista para não duplicar

                for (DataSnapshot dados: dataSnapshot.getChildren()){
                    Local local = dados.getValue(Local.class);
                    Log.d("junior", "data: "+local.getDataCadastroCalendar());
                    listaLocais.add(local);
                }
                adicionarMarcadores();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("forLista", databaseError.getMessage());
            }
        });

    }

    public void removerMarcadoresDoMapa() { //Remove os marcadores do Mapa em si
        for (Marker marker : marcadores) {
            marker.remove();
        }
    }

    public void adicionarMarcadores( ) {
        removerMarcadoresDoMapa(); //Remove os marcadores do Mapa em si
        marcadores.clear(); //Remove os marcadores da Memória

        for (Local local : listaLocais ) {
            Marker marcador;
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(local.getLatitude(), local.getLongitude()))
                    .title(local.getNome())
                    .snippet("("+local.getId()+")");

            marcador = mapa.addMarker(markerOptions);
            marcadores.add(marcador);
        }

    }

    public void toggleMarcadores(View view){
        for (Marker marker: marcadores) {
            //Log.d("forLista", "--> "+ marker.getTitle());
            if(marker.isVisible())
                marker.setVisible(false);
            else
                marker.setVisible(true);
        }
    }

    public void recuperarEventos () {
        /*TODO: Adicionar marcadores para eventos*/
        valueEventListenerEventos = eventosRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaEventos.clear();//limpa a lista para não duplicar

                for (DataSnapshot dados: dataSnapshot.getChildren()){
                    Evento evento = dados.getValue(Evento.class);
                    listaEventos.add(evento);
                    /*TODO: Verificar a data para mostrar apenas os eventos do dia no mapa*/
                    //Log.d("forEventos", "->"+evento.getNome());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("forEventos", databaseError.getMessage());
            }
        });
    }

    /************************************** Verificar Permissão de localização ********************/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /********************************** Métodos do Menu Flutuante *********************************/
    private void configuraFabLocalizacao() {
        fabLocalizacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapaActivity.this, "Loc", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /********************************** Métodos do Menu Flutuante *********************************/
    private void configuraMenuFlutuante () {
        //remove submenus que não fazem parte da activity
        fabMapa.setVisibility(View.GONE);

        /*Configura listeners dos submenus*/
        fabLocais.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MapaActivity.this, ListaLocalActivity.class);
                startActivity(i);
                if (fabMenu.isOpened()) { //fecha o menu para quando voltar de outras activitys mostrar menu fechado
                    fabMenu.close(true);
                }
            }
        });

        fabEventos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MapaActivity.this, EventosActivity.class);
                startActivity(i);
                if (fabMenu.isOpened()) { //fecha o menu para quando voltar de outras activitys mostrar menu fechado
                    fabMenu.close(true);
                }
            }
        });
    }

    /************************************** Métodos do Menu********************************************/
    //Sobrescreve o Menu da Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuTutorial:
                startActivity(new Intent(MapaActivity.this, TutorialActivity.class));//Recarrefa a activity
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
