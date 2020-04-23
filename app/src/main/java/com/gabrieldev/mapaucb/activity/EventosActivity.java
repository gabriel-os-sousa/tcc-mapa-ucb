package com.gabrieldev.mapaucb.activity;

import android.graphics.Color;
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
import com.gabrieldev.mapaucb.helper.EventoDecorator;
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
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class EventosActivity extends AppCompatActivity {
    private MaterialCalendarView calendarView;
    private RecyclerView recyclerEventos;
    private AdapterEventos adapterEventos;

    private DatabaseReference eventosRef;
    private ValueEventListener valueEventListenerEventos;

    private List<Evento> listaEventos = new ArrayList<>();
    private List<Evento> listaEventosDia = new ArrayList<>();
    private List<Evento> listaEventosMes = new ArrayList<>();

    /*Collection de Decorators*/
    private Collection<CalendarDay> datasDecoratorMes = new ArrayList<>();

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

    private void configurarCalendarView() {
        //Seta a data inicial
        calendarView.state().edit()
                .setMinimumDate(CalendarDay.from(2020,0,1))
                .commit();

        final CharSequence meses[] = {"Janeiro","Fevereiro","Março", "Abril", "Maio", "Junho", "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"};
        calendarView.setTitleMonths(meses);

        CharSequence semanas[] = {"Seg","Ter","Qua", "Qui", "Sex", "Sab", "Dom"};
        calendarView.setWeekDayLabels(semanas);

        //Listener de Troca de Mês
        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                Log.d("gabi", "mes mudou");
                calendarView.clearSelection();//Limpa a data selecionada, se tiver, quando mudar o mês
                /*String mesSelecionado = String.format("%02d", (date.getMonth()+1)); //Preenche com 0 à frente se tiver um digito
                mesAnoSelecionado = String.valueOf(mesSelecionado +""+ date.getYear());*/

                listaEventosMes.clear();//limpa a lista para não duplicarrr
                datasDecoratorMes.clear(); //limpa a lista de decoradores para não duplicar e ocupar memória

                for (Evento evento : listaEventos){
                    Calendar cInicial = Calendar.getInstance();
                    cInicial.setTimeInMillis(evento.getData_inicio());

                    Calendar cFinal = Calendar.getInstance();
                    cFinal.setTimeInMillis(evento.getData_fim());

                    if((cInicial.get(Calendar.MONTH) == date.getMonth()) || (cFinal.get(Calendar.MONTH) == date.getMonth())) {
                        listaEventosMes.add(evento);

                        /*Adiciona o evento ao Collection de decoradores*/
                        datasDecoratorMes.add(CalendarDay.from(evento.getData_inicio()));
                        datasDecoratorMes.add(CalendarDay.from(evento.getData_fim()));
                    }

                }

                /*Adiciona Decoradores no calendário*/
                calendarView.removeDecorators();
                calendarView.invalidateDecorators();
                calendarView.addDecorators(new EventoDecorator(Color.BLUE, datasDecoratorMes));/*Adiciona pontos nos eventos do mês*/

                /*Configura o adapter novamente com os novos dados*/
                adapterEventos = new AdapterEventos(listaEventosMes);
                recyclerEventos.setAdapter(adapterEventos);
                adapterEventos.notifyDataSetChanged(); //Diz para o adapter que os dados mudaram. Recria o recyclerView

            }
        });

        //Listener de Troca de Data (Dia)
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                listaEventosDia.clear();//limpa a lista para não duplicarrr

                for (Evento evento : listaEventos){
                    Calendar cInicial = Calendar.getInstance();
                    cInicial.setTimeInMillis(evento.getData_inicio());

                    Calendar cFinal = Calendar.getInstance();
                    cFinal.setTimeInMillis(evento.getData_fim());

                    Calendar cSelecionado = Calendar.getInstance();
                    cSelecionado.set(date.getYear(),date.getMonth(),date.getDay(), 0,0,0);

                    if ((cSelecionado.getTimeInMillis() >= cInicial.getTimeInMillis() && cSelecionado.getTimeInMillis() <= cFinal.getTimeInMillis()) //Verificação de eventos em varios dias
                            || (cInicial.get(Calendar.DAY_OF_MONTH) == cFinal.get(Calendar.DAY_OF_MONTH) && cInicial.get(Calendar.MONTH) == cFinal.get(Calendar.MONTH)) //Verificacao de eventos diarios
                            && cInicial.get(Calendar.DAY_OF_MONTH) == cSelecionado.get(Calendar.DAY_OF_MONTH) && cInicial.get(Calendar.MONTH) == cSelecionado.get(Calendar.MONTH)

                    ) {
                        listaEventosDia.add(evento);
                    }

                    Log.d("gabi", "onDateSelected: "+ cSelecionado.getTimeInMillis()+"");
                }

                /*Configura o adapter novamente com os novos dados*/
                adapterEventos = new AdapterEventos(listaEventosDia);
                recyclerEventos.setAdapter(adapterEventos);
                adapterEventos.notifyDataSetChanged(); //Diz para o adapter que os dados mudaram. Recria o recyclerView
            }
        });
    }



    public void recuperarEventos () {
        eventosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("eventos");

        valueEventListenerEventos = eventosRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaEventos.clear();//limpa a lista para não duplicar
                listaEventosMes.clear();

                for (DataSnapshot dados: dataSnapshot.getChildren()){
                    Evento evento = dados.getValue(Evento.class);
                    listaEventos.add(evento);
                }

                Calendar cAtual = Calendar.getInstance();
                cAtual.setTimeInMillis(System.currentTimeMillis());

                for (Evento evento : listaEventos){
                    Calendar cInicial = Calendar.getInstance();
                    cInicial.setTimeInMillis(evento.getData_inicio());

                    Calendar cFinal = Calendar.getInstance();
                    cFinal.setTimeInMillis(evento.getData_fim());

                    /* Se o evento mês iniciao do evento ou o mês final do evento for igual ao mês atual, adiciona o evento na lista*/
                    if((cInicial.get(Calendar.MONTH) == cAtual.get(Calendar.MONTH))
                       || (cFinal.get(Calendar.MONTH) == cAtual.get(Calendar.MONTH))) {

                        /*Adiciona o evento na lista de eventos do Mês*/
                        listaEventosMes.add(evento);

                        /*Adiciona o evento ao Collection de decoradores*/
                        datasDecoratorMes.add(CalendarDay.from(evento.getData_inicio()));
                        datasDecoratorMes.add(CalendarDay.from(evento.getData_fim()));
                    }
                }

                /*Adiciona Decoradores no calendário*/
                //decoratorDiaAtual.add(CalendarDay.today());
                calendarView.removeDecorators();
                calendarView.invalidateDecorators();
                calendarView.addDecorators(new EventoDecorator(Color.BLUE, datasDecoratorMes));/*Adiciona decoradores no dia atual*/

                /*Configura o adapter novamente com os novos dados*/
                adapterEventos = new AdapterEventos(listaEventosMes);
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
