package com.gabrieldev.mapaucb.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gabrieldev.mapaucb.R;
import com.gabrieldev.mapaucb.adapter.AdapterEventos;
import com.gabrieldev.mapaucb.config.ConfiguracaoFirebase;
import com.gabrieldev.mapaucb.model.Evento;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.util.ArrayList;
import java.util.List;

public class EventosActivity extends AppCompatActivity {
    private MaterialCalendarView calendarView;
    private RecyclerView recyclerEventos;
    private AdapterEventos adapterEventos;

    private DatabaseReference eventosRef;
    private ValueEventListener valueEventListenerEventos;

    private List<Evento> listaEventos = new ArrayList<>();
    private List<Evento> listaEventosDia = new ArrayList<>();

    private String mesAnoSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventos);

        //Configura a toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Eventos");
        setSupportActionBar(toolbar);

        //Inicializa os componentes
        calendarView = findViewById(R.id.calendarView);
        recyclerEventos = findViewById(R.id.recyclerEventos);

        configurarCalendarView();

        //Configurar RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerEventos.setLayoutManager(layoutManager);
        recyclerEventos.setHasFixedSize(true);
        recyclerEventos.addItemDecoration(new DividerItemDecoration(this, LinearLayout.VERTICAL));

    }

    private void configurarCalendarView() {
        //Seta a data inicial
        calendarView.state().edit()
                .setMinimumDate(CalendarDay.from(2020,0,1))
                .commit();

        CharSequence meses[] = {"Janeiro","Fevereiro","Março", "Abril", "Maio", "Junho", "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"};
        calendarView.setTitleMonths(meses);

        CharSequence semanas[] = {"Seg","Ter","Qua", "Qui", "Sex", "Sab", "Dom"};
        calendarView.setWeekDayLabels(semanas);

        CalendarDay dataAtual = calendarView.getCurrentDate();
        String mesSelecionado = String.format("%02d", (dataAtual.getMonth()+1));
        mesAnoSelecionado = String.valueOf(mesSelecionado +""+ dataAtual.getYear());


        //Listener de Troca de Mês
        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                calendarView.clearSelection();//Limpa a data selecionada, se tiver, quando mudar o mês

                String mesSelecionado = String.format("%02d", (date.getMonth()+1)); //Preenche com 0 à frente se tiver um digito
                mesAnoSelecionado = String.valueOf(mesSelecionado +""+ date.getYear());

                eventosRef.removeEventListener(valueEventListenerEventos);
                recuperarEventos();
                Log.d("forEventos", "Mes: "+(date.getMonth()+1));
                Log.d("forEventos", "Mes Ano Selecionado: "+ mesAnoSelecionado);

            }
        });

        //Listener de Troca de Data (Dia)
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                String diaSelecionado = String.valueOf(date.getDay());
                listaEventosDia.clear();//limpa a lista para não duplicar

                for (Evento evento : listaEventos){
                    if (Double.parseDouble(diaSelecionado) >= Double.parseDouble(evento.getDia_inicio())
                            && Double.parseDouble(diaSelecionado) <= Double.parseDouble(evento.getDia_fim())) {
                        listaEventosDia.add(evento);
                    }
                }

                adapterEventos = new AdapterEventos(listaEventosDia);
                recyclerEventos.setAdapter(adapterEventos);
                adapterEventos.notifyDataSetChanged(); //Diz para o adapter que os dados mudaram. Recria o recyclerView
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarEventos();
    }

    @Override
    protected void onStop() {
        super.onStop();
       eventosRef.removeEventListener(valueEventListenerEventos);
    }

    public void recuperarEventos () {
        //Referência banco de dados
        eventosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("eventos").child(mesAnoSelecionado);

        valueEventListenerEventos = eventosRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaEventos.clear();//limpa a lista para não duplicar

                for (DataSnapshot dados: dataSnapshot.getChildren()){
                    Evento evento = dados.getValue(Evento.class);
                    listaEventos.add(evento);
                    Log.d("forEventos", "->"+evento.getNome());
                }

                adapterEventos = new AdapterEventos(listaEventos);
                recyclerEventos.setAdapter(adapterEventos);
                adapterEventos.notifyDataSetChanged(); //Diz para o adapter que os dados mudaram. Recria o recyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("forEventos", databaseError.getMessage());
            }
        });
    }
}
