package com.gabrieldev.mapaucb.activity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.config.ConfiguracaoFirebase;
import com.gabrieldev.mapaucb.model.Evento;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class EventoSelecionadoActivity extends AppCompatActivity {
    private TextView textViewNome, textViewDescricao, textViewLocal, textTipoEventoSelecionado, textHorario;
    private Evento evento;
    private StorageReference storageRef;
    private DatabaseReference localRef;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evento_selecionado);

        //Configura a toolbar específica
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("");
        setSupportActionBar(toolbar); //Seta a toolbar configurada
        //Configura o item voltar na toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Configurações os campos da tela
        textViewNome = findViewById(R.id.textNomeEventoSelecionado);
        textViewDescricao = findViewById(R.id.textDescricaoEventoSelecionado);
        textViewLocal = findViewById(R.id.textLocalEventoSelecionado);
        textTipoEventoSelecionado = findViewById(R.id.textTipoEventoSelecionado);
        textHorario = findViewById(R.id.textHorarioEventoSelecionado);
        imageView = findViewById(R.id.imageViewEventoSelecionado);

        //Storage Ref Firebase
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        localRef = ConfiguracaoFirebase.getFirebaseDatabase();


        //Recuperar os dados do Local Selecionado
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            evento = (Evento) bundle.getSerializable("eventoSelecionado");
            toolbar.setTitle(evento.getNome());//Muda o nome na toolbar

            textViewNome.setText(evento.getNome());
            textViewDescricao.setText(evento.getDescricao());
            textTipoEventoSelecionado.setText(evento.getTipo());
            textHorario.setText(evento.getHorario());

            /*TODO: Recuperar url do campo no firebase*/
            String nomeImg = evento.getId() + ".png";
            storageRef.child("eventos").child(nomeImg)
                    .getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri.toString()).into(imageView);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EventoSelecionadoActivity.this, "Imagem não encontrada!", Toast.LENGTH_SHORT).show();
                }
            });

            //Recuperar o nome do local do evento
            localRef = ConfiguracaoFirebase.getFirebaseDatabase().child("locais");
            localRef.child(evento.getLocal()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Evento evento = dataSnapshot.getValue(Evento.class);
                    textViewLocal.setText(evento.getNome());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    textViewLocal.setText("Local não encontrado");
                }
            });
        }
    }
}
