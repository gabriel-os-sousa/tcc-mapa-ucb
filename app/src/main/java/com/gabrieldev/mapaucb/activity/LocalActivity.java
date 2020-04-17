package com.gabrieldev.mapaucb.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.model.Local;

public class LocalActivity extends AppCompatActivity {
    private TextView textViewNome, textViewDescricao, textViewLatitude, textViewLongitude;
    private Local local;

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


        //Configurações iniciais
        textViewNome = findViewById(R.id.textNomeLocal);
        textViewDescricao = findViewById(R.id.textDescricaoLocal);
        textViewLatitude = findViewById(R.id.textLatitudeLocal);
        textViewLongitude = findViewById(R.id.textLongitudeLocal);

        //Recuperar os dados do Local Selecionado
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            local = (Local) bundle.getSerializable("localSelecionado");
            textViewNome.setText(local.getNome());
            textViewDescricao.setText(local.getDescricao());
            textViewLatitude.setText(String.valueOf(local.getLatitude()));
            textViewLongitude.setText(String.valueOf(local.getLongitude()));
            toolbar.setTitle(local.getNome());//Muda o nome na toolbar
        }
    }
}
