package com.gabrieldev.mapaucb.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RedefinirSenhaActivity extends AppCompatActivity {
    private EditText campoEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redefinir_senha);

        //Configura a toolbar específica
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Redefinir senha");
        setSupportActionBar(toolbar); //Seta a toolbar configurada
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Inicializa os componentes
        campoEmail = findViewById(R.id.editTextRecuperarSenhaEmail);

    }

    public void validarCampoEmail (View view) {
        //Recuperar textos dos campos
        String textoEmail = campoEmail.getText().toString();

        if (!textoEmail.isEmpty()) {
            enviarLinkRedefinicao(textoEmail);
        } else {
            Toast.makeText(this, "Digite o Email!", Toast.LENGTH_SHORT).show();
        }
    }

    public void enviarLinkRedefinicao(final String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RedefinirSenhaActivity.this,
                                    "Foi enviado um link para redefinição de senha para o email: "+ email,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
