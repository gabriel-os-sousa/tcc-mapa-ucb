package com.gabrieldev.mapaucb.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.config.ConfiguracaoFirebase;
import com.gabrieldev.mapaucb.model.Local;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class LocalActivity extends AppCompatActivity {
    private TextView textViewNome, textViewDescricao, textViewTipo, textViewAndar;
    private Local local;
    private ImageView imagemDestaque;

    private StorageReference storageRef;

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
            textViewTipo.setText(local.getTipo());

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

            /*
            rules_version = '2';
            service firebase.storage {
              match /b/{bucket}/o {
                match /{allPaths=**} {
                  allow read, write: if request.auth != null;
                }
              }
            }
            */

        }
    }
}
