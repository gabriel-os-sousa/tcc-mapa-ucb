package com.gabrieldev.mapaucb.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.config.ConfiguracaoFirebase;
import com.gabrieldev.mapaucb.model.Evento;
import com.gabrieldev.mapaucb.model.Local;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

public class EventoSelecionadoActivity extends AppCompatActivity {
    private TextView textViewNome, textViewDescricao, textViewLocal, textTipoEventoSelecionado, textHorario, textData;
    private Evento evento;
    private StorageReference storageRef;
    private DatabaseReference localRef;
    private ImageView imageView;
    private Local localEvento;

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
        textData = findViewById(R.id.textDataEventoSelecionado);
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

            //Tratamento da data
            Calendar cInicial = Calendar.getInstance(EventosActivity.TIMEZONE);
            cInicial.setTimeInMillis(evento.getData_inicio());

            Calendar cFinal = Calendar.getInstance(EventosActivity.TIMEZONE);
            cFinal.setTimeInMillis(evento.getData_fim());

            //MONTH = 0 pra janeiro / DAY_OF_MONTH = 1 para o primeiro dia do mês
            //Adiciona um mês aos calêndários pois os meses começam no 0
            cInicial.add(Calendar.MONTH,1);
            cFinal.add(Calendar.MONTH,1);

            //se o dia e o meses forem iguais, evento de só um dia
            if((cInicial.get(Calendar.MONTH) == cFinal.get(Calendar.MONTH)) && (cInicial.get(Calendar.DAY_OF_MONTH) == cFinal.get(Calendar.DAY_OF_MONTH))) {
                String dataini = cInicial.get(Calendar.DAY_OF_MONTH )+"/"+cInicial.get(Calendar.MONTH);
                textData.setText(dataini);

                //se o evento for mais de um dia
            } else {
                String datafim = cInicial.get(Calendar.DAY_OF_MONTH)+"/"+cInicial.get(Calendar.MONTH)+" a "+cFinal.get(Calendar.DAY_OF_MONTH)+"/"+cFinal.get(Calendar.MONTH);
                textData.setText(datafim);
            }

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
                    localEvento = dataSnapshot.getValue(Local.class);
                    textViewLocal.setText(localEvento.getNome());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    textViewLocal.setText("Local não encontrado");
                }
            });

            textViewLocal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (localEvento != null) {
                        Intent i = new Intent(EventoSelecionadoActivity.this, LocalActivity.class);
                        i.putExtra("localSelecionado", localEvento);
                        startActivity(i);
                    } else {
                        Toast.makeText(EventoSelecionadoActivity.this, "Local vazio", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
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
