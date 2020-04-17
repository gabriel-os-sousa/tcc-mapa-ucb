package com.gabrieldev.mapaucb.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.config.ConfiguracaoFirebase;
import com.gabrieldev.mapaucb.helper.UsuarioFirebase;
import com.gabrieldev.mapaucb.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {
    private TextInputEditText campoNome, campoSobrenome, campoEmail, campoSenha;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        //Inicializando componentes
        campoNome = findViewById(R.id.editCadastroNome);
        campoSobrenome = findViewById(R.id.editCadastroSobrenome);
        campoEmail = findViewById(R.id.editCadastroEmail);
        campoSenha = findViewById(R.id.editCadastroSenha);
    }

    public void validarCadastroUsuario (View view) {
        //recuperar textos dos campos
        String textoNome = campoNome.getText().toString();
        String textoSobrenome = campoSobrenome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        String tipoUsuario = "usuario";

        //Verifica se os campos estão vazios
        if (!textoNome.isEmpty()) { //verifica nome
            if (!textoSobrenome.isEmpty()) { //verifica sobrenome
                if (!textoEmail.isEmpty()) { //verifica email
                    if (!textoSenha.isEmpty()) { //verifica senha
                        Usuario usuario = new Usuario();
                        usuario.setNome(textoNome);
                        usuario.setSobrenome(textoSobrenome);
                        usuario.setEmail(textoEmail);
                        usuario.setSenha(textoSenha);
                        usuario.setTipo(tipoUsuario);

                        cadastrarUsuario(usuario);

                    } else {
                        Toast.makeText(this, "Preencha a senha!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Preencha o E-mail!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Preencha o sobrenome!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Preencha o nome!", Toast.LENGTH_SHORT).show();
        }
    }

    public void cadastrarUsuario(final Usuario usuario) {
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    try {
                        //String identificadorUsuario = Base64Custom.codificarBase64( usuario.getEmail() );
                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setId( idUsuario );
                        usuario.salvar(); //salva usuário no firebase

                        //Atualizar nome no UserProfile
                        UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                        //Redirecionar usuario após cadastro finalizado
                        setResult(RESULT_OK, null);
                        //abrirTelaPrincipal();
                        finish();//para fechar tela de usuario

                        Toast.makeText(CadastroActivity.this, "Usuário cadastrado!", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //Tratamento de excessoes
                    String excecao = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte!";
                    } catch (FirebaseAuthInvalidCredentialsException e ){
                        excecao = "Digite um email válido!";
                    } catch (FirebaseAuthUserCollisionException e) {
                        excecao = "Esta conta já foi cadastrada";
                    } catch (Exception e) {
                        excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroActivity.this,
                            excecao,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
    }

    /*public void abrirTelaPrincipal() {
        Intent intent = new Intent(CadastroActivity.this, MapaActivity.class);
        Toast.makeText(CadastroActivity.this,"C>H on login",Toast.LENGTH_LONG).show();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Faz com que ao pressionar o botão de voltar do android, tire o bug de voltar pra mesma tela
        startActivity(intent);
        finish();
    }*/

}
