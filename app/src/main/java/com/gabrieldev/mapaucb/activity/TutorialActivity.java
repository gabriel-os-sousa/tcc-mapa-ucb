package com.gabrieldev.mapaucb.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.config.ConfiguracaoFirebase;
import com.gabrieldev.mapaucb.helper.Permissoes;
import com.google.firebase.auth.FirebaseAuth;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;

public class TutorialActivity extends IntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Configura os Slides
        setButtonBackVisible(false);//remove o botão de voltar
        setButtonNextVisible(false);//remove o botão de avançar

        addSlide(new FragmentSlide.Builder()
                .fragment(R.layout.intro_1)
                .background(android.R.color.holo_orange_light)//cor da barra de navegacao
                //.backgroundDark(android.R.color.black)//cor da barra de notificacao
                .canGoBackward(false)
                .canGoForward(true)
                .build()
        );

        addSlide(new FragmentSlide.Builder()
                .fragment(R.layout.intro_1)
                .background(android.R.color.holo_blue_bright)//cor da barra de navegacao
                //.backgroundDark(android.R.color.black)//cor da barra de notificacao
                .canGoBackward(true)
                .canGoForward(true)
                .build()
        );

        //TODO: Fazer Tutorial, adicinando outros fragments

    }
}
