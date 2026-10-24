package com.example.tfg.ui

import android.content.Intent // 👈 Asegúrate de que se importe esto
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.Actividad
import com.example.tfg.ActividadAdapter
import com.example.tfg.R
import com.google.android.material.floatingactionbutton.FloatingActionButton // 👈 Y esto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var listaActividades = mutableListOf<Actividad>()
    private lateinit var adapter: ActividadAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Vincular RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = ActividadAdapter(listaActividades)
        recyclerView.adapter = adapter

        // Lógica del botón flotante
        val fabAniadir = view.findViewById<FloatingActionButton>(R.id.fab_aniadir)
        fabAniadir.setOnClickListener {
            val intent = Intent(requireContext(), AniadirEvento::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        obtenerDatos()
    }

    private fun obtenerDatos() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // He corregido la URL para que use "tfg" en minúsculas si así lo tienes en XAMPP
                val textoRespuesta = URL("http://10.0.2.2/tfg/obtener_actividades.php").readText()

                if (textoRespuesta.isNotEmpty()) {
                    val jsonArray = JSONArray(textoRespuesta)
                    val nuevaLista = mutableListOf<Actividad>()

                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        nuevaLista.add(
                            Actividad(
                                id = item.getString("id_actividad"),
                                nombre = item.getString("nombre"),
                                tipo = item.getString("tipo"),
                                fecha = item.getString("fecha"),
                                horaInicio = item.getString("hora_inicio"),
                                horaFin = item.getString("hora_fin"),
                                monitores = item.optString("descripcion", "N/A"),
                                observaciones = item.optString("observaciones", "")
                            )
                        )
                    }

                    withContext(Dispatchers.Main) {
                        listaActividades.clear()
                        listaActividades.addAll(nuevaLista)
                        adapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}