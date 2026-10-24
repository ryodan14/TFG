package com.example.tfg

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

class CalendarioActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ActividadAdapter
    private var listaEventosDia = mutableListOf<Actividad>()
    private var todasLasActividades = mutableListOf<Actividad>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario)

        val calendarView = findViewById<MaterialCalendarView>(R.id.calendarView)
        val tvFecha = findViewById<TextView>(R.id.tvFechaSeleccionada)

        // Configurar la lista de abajo (RecyclerView)
        recyclerView = findViewById(R.id.rvEventosCalendario)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ActividadAdapter(listaEventosDia)
        recyclerView.adapter = adapter

        // 1. Nada más abrir, cargamos los datos y pintamos los puntos
        cargarDatosYDecorar(calendarView)

        // 2. ¿Qué pasa cuando tocas un día?
        calendarView.setOnDateChangedListener { _, date, selected ->
            if (selected) {
                // Preparamos la fecha para que coincida con tu Base de Datos (YYYY-MM-DD)
                val mesFormateado = String.format("%02d", date.month)
                val diaFormateado = String.format("%02d", date.day)
                val fechaSeleccionada = "${date.year}-$mesFormateado-$diaFormateado"

                tvFecha.text = "Eventos para el día: $diaFormateado/$mesFormateado/${date.year}"

                // Filtramos la lista de abajo para mostrar solo los de ese día
                filtrarEventos(fechaSeleccionada)
            }
        }
    }

    private fun cargarDatosYDecorar(calendarView: MaterialCalendarView) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Llamamos a tu PHP
                val respuesta = URL("http://10.0.2.2/tfg/obtener_actividades_calendario.php").readText()
                val jsonArray = JSONArray(respuesta)

                val diasConEvento = mutableSetOf<CalendarDay>()
                todasLasActividades.clear()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val act = Actividad(
                        id = obj.getString("id_actividad"),
                        nombre = obj.getString("nombre"),
                        tipo = obj.getString("tipo"),
                        fecha = obj.getString("fecha"), // Formato YYYY-MM-DD
                        horaInicio = obj.getString("hora_inicio"),
                        horaFin = obj.getString("hora_fin"),
                        monitores = obj.optString("descripcion", "N/A"),
                        observaciones = obj.optString("observaciones", "")
                    )
                    todasLasActividades.add(act)

                    // Cortamos la fecha por los guiones para saber Año, Mes y Día
                    val partes = act.fecha.split("-")
                    if (partes.size == 3) {
                        val anio = partes[0].toInt()
                        val mes = partes[1].toInt()
                        val dia = partes[2].toInt()

                        // Guardamos ese día en la lista de "Días con Punto"
                        diasConEvento.add(CalendarDay.from(anio, mes, dia))
                    }
                }

                // ¡LA MAGIA DEL PUNTO! Se lo aplicamos al calendario visual
                withContext(Dispatchers.Main) {
                    calendarView.addDecorator(EventDecorator(Color.parseColor("#2196F3"), diasConEvento))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun filtrarEventos(fecha: String) {
        val filtrados = todasLasActividades.filter { it.fecha == fecha }
        listaEventosDia.clear()
        listaEventosDia.addAll(filtrados)
        adapter.notifyDataSetChanged()
    }
}