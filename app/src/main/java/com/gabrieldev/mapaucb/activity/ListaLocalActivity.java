package com.gabrieldev.mapaucb.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.adapter.AdapterLocal;
import com.gabrieldev.mapaucb.config.ConfiguracaoFirebase;
import com.gabrieldev.mapaucb.helper.RecyclerItemClickListener;
import com.gabrieldev.mapaucb.model.Local;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

public class ListaLocalActivity extends AppCompatActivity {
    private RecyclerView recyclerLocais;
    private List<Local> listaLocais = new ArrayList<>();
    private List<Local> listaLocalBusca = new ArrayList<>();
    private boolean submit = false; //Flag para o submit searchView
    private boolean inicio = false; //Flag para o início searchView
    private MaterialSearchView searchView;
    private AdapterLocal adapterLocal;

    /*Firebase*/
    private DatabaseReference locaisRef;
    private ValueEventListener valueEventListenerLocais;

    /*Menu Flutuante*/
    private FloatingActionButton fabMapa;
    private FloatingActionButton fabLocais;
    private FloatingActionButton fabEventos;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_local);

        //Configura a referência do banco de dados
        locaisRef = ConfiguracaoFirebase.getFirebaseDatabase().child("locais");

        //Inicializa recyclerview
        recyclerLocais = findViewById(R.id.recyclerLocais);

        //Configura os Fab's do menu flutuante
        fabLocais = findViewById(R.id.floating_menu_locais);
        fabEventos = findViewById(R.id.floating_menu_eventos);
        fabMapa = findViewById(R.id.floating_menu_mapa);
        configuraMenuFlutuante();


        //Configura a toolbar específica
        final Toolbar toolbar = findViewById(R.id.toolbarListaLocais);
        toolbar.setTitle("Locais");
        setSupportActionBar(toolbar); //Seta a toolbar configurada
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        //Configura Adapter
        //adapterLocal = new AdapterLocal(listaLocais);

        //Configurar Recycleview
        RecyclerView.LayoutManager layouteManager = new LinearLayoutManager(getApplicationContext());
        recyclerLocais.setLayoutManager(layouteManager);
        recyclerLocais.setHasFixedSize(true);//otimiza o recycler view
        recyclerLocais.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));
        //recyclerLocais.setAdapter(adapterLocal);

        //Configurando Evento de clique do recyclerView
        recyclerLocais.addOnItemTouchListener(new RecyclerItemClickListener(
                ListaLocalActivity.this,
                recyclerLocais,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Local localSelecionado = listaLocalBusca.get(position);
                        Intent i = new Intent(ListaLocalActivity.this, LocalActivity.class);
                        i.putExtra("localSelecionado", localSelecionado);
                        startActivity(i);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));


        //Configuracao de SearchView
        searchView = findViewById(R.id.materialSearchListaLocais);


        /*********************************** SearchView *******************************************/
        //Listner para o search view
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                Log.d("searchV: ", "show");
            }

            @Override
            public void onSearchViewClosed() {
                //finish();
                //listaLocalBusca.clear();
                Log.d("searchV: ", "closed");
            }
        });


        //Listner para a caixa de texto
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) { //Modifica a lista a ser apresentada quando o usuario terminar de digitar

                submit = true;//1 - passou pelo onQueryTextSubmit / 0 - não passou onQueryTextSubmit

                if(query != null && !query.isEmpty()) {
                    pesquisarLocais(query);
                    Log.d("searchV: ", "submit");
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { //Modifica a lista a ser apresentada enquanto o usuario estiver digitando

                if(!newText.isEmpty() || newText != null){ //Se newText não está vazio ou Não está nulo
                    Log.d("searchV: ", "changeQR "+ newText);

                    if (submit == false) {
                        pesquisarLocais(newText.toLowerCase());
                        //Log.d("searchV: ", "textchange: NÃO VEIO DO SUBMIT");
                    }
                    if (submit == true) { //Se passou pelo submit, apenas seta a flag para 0
                        //Log.d("searchV: ", "textchange: VEIO DO SUBMIT");
                        submit = false;
                    }
                }

                /*if(inicio == false){
                    Log.d("searchV: ", "Inicio");
                    inicio = true;
                } else {
                    if(!newText.isEmpty() || newText != null){ //Se newText não está vazio ou Não está nulo

                        if (submit == false) {
                            pesquisarLocais(newText.toLowerCase());
                            //Log.d("searchV: ", "textchange: NÃO VEIO DO SUBMIT");
                        }
                        if (submit == true) { //Se passou pelo submit, apenas seta a flag para 0
                            //Log.d("searchV: ", "textchange: VEIO DO SUBMIT");
                            submit = false;
                        }

                    }
                }*/



                return false;
            }
        });
        /*********************************** Fim SearchView ***************************************/

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarListaLocais();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locaisRef.removeEventListener(valueEventListenerLocais);
    }

    @Override
    protected void onDestroy() {
        Log.d("meulog", "onDestroy lista locais: ");
        super.onDestroy();
    }

    public void pesquisarLocais(String texto) {

        listaLocalBusca.clear();
        for (Local local : listaLocais ) {
            //Campos parâmetros para a busca
            String nome = local.getNome().toLowerCase();
            String descricao = local.getDescricao().toLowerCase();

            if (nome.contains(texto.toLowerCase()) || descricao.contains(texto.toLowerCase())){
                listaLocalBusca.add(local);
            }
        }

        Log.d("searchV: ", "changeQR "+ listaLocalBusca);

        //Recriar o recyclerview com os dados
        adapterLocal = new AdapterLocal(listaLocalBusca);
        recyclerLocais.setAdapter(adapterLocal);
        adapterLocal.notifyDataSetChanged(); //Diz para o adapter que os dados mudaram. Recria o recyclerView
    }

    public void recuperarListaLocais() {
        valueEventListenerLocais = locaisRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaLocais.clear();

                for (DataSnapshot dados: dataSnapshot.getChildren()){
                    Local local = dados.getValue(Local.class);
                    listaLocais.add(local);

                    //Log.d("searchV", "Listner "+ local.getNome());
                }
                Log.d("searchV", "dataChange");
                searchView.closeSearch();//fecha o searchview
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /********************************** Métodos do Menu Flutuante *********************************/
    private void configuraMenuFlutuante () {
        //remove submenus que não fazem parte da activity
        fabLocais.setVisibility(View.GONE);

        //Configura listeners dos submenus
        fabMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fabEventos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ListaLocalActivity.this, EventosActivity.class);
                startActivity(i);
            }
        });
    }


    /************************************** Métodos do Menu********************************************/
    //Sobrescreve o Menu da Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_lista_locais, menu);

        //Configurar o  SearchView
        MenuItem item = menu.findItem(R.id.menuPesquisarLocais);
        searchView.setMenuItem(item);
        searchView.showSearch();//Quando abrir a ListaLocalActivity, já deixa o campo de pesquisa aberto
        return true;
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
