package com.gabrieldev.mapaucb.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.config.ConfiguracaoFirebase;
import com.gabrieldev.mapaucb.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText campoEmail, campoSenha;
    private TextView editTextBtnCadastrar, editTextBtnRecuperarSenha;
    private FirebaseAuth autenticacao;

    public static final int REQUEST_EXIT = 01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        campoEmail = findViewById(R.id.editLoginEmail);
        campoSenha = findViewById(R.id.editLoginSenha);
        editTextBtnCadastrar = findViewById(R.id.textLoginBtnCadastrar);
        editTextBtnRecuperarSenha = findViewById(R.id.textLoginBtnRecuperarSenha);

        editTextBtnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(LoginActivity.this, CadastroActivity.class));
                startActivityForResult(new Intent(LoginActivity.this, CadastroActivity.class), REQUEST_EXIT);
                //finish();
            }
        });

        editTextBtnRecuperarSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RedefinirSenhaActivity.class);
                startActivity(i);
                //finish();
            }
        });
    }

    public void validarLoginUsuario(View view) {
        //Recuperar textos dos campos
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if (!textoEmail.isEmpty()) {
            if (!textoSenha.isEmpty()) {
                Usuario usuario = new Usuario();
                usuario.setEmail(textoEmail);
                usuario.setSenha(textoSenha);

                logarUsuario(usuario);
            } else {
                Toast.makeText(this, "Digite a Senha!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Digite o Email!", Toast.LENGTH_SHORT).show();
        }

    }

    public void logarUsuario (Usuario usuario) {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //abrirTelaPrincipal();
                    Log.d("loginZZ", "logar usuario");
                    /*TODO: Verificar se vai para a tela principal ou não. Fazer método de ir para a tela princpal na tela que chamar essa Intent*/
                    finish();
                } else {
                    //Tratamento de excessoes
                    String excecao = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        excecao = "Usuário não está cadastrado!";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        excecao = "Email e senha não conrrespondem a um usuário cadastrado!";
                    } catch (Exception e) {
                        excecao = "Erro ao logar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }

                    Toast.makeText(LoginActivity.this,
                            excecao,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
    }

    /*public void abrirTelaPrincipal() {
        Intent intent = new Intent(LoginActivity.this, TutorialActivity.class);
        Toast.makeText(LoginActivity.this,"L>H on login",Toast.LENGTH_LONG).show();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Faz com que ao pressionar o botão de voltar do android, tire o bug de voltar pra mesma tela
        startActivity(intent);
        finish();
    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("dBug ", "pressback login");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_EXIT == requestCode) {
            if (resultCode == RESULT_OK) {
                this.finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("lifecycle ", "destroy Login");
        super.onDestroy();
    }
}
