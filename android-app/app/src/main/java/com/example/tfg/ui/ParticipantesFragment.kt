package com.example.tfg

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

class ParticipantesFragment : Fragment() {

    private lateinit var adapter: NinoAdapter
    private lateinit var searchView: SearchView
    private var busquedaActual = ""
    private var chipActual = "TODOS"
    private var cursoActual = "Todos"
    private var centroActual = "Todos"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_participantes, container, false)
        val rv = view.findViewById<RecyclerView>(R.id.rvParticipantes)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = NinoAdapter()
        rv.adapter = adapter
        searchView = view.findViewById(R.id.svBuscarNino)

        setupListeners(view)
        cargarDatos()
        return view
    }

    private fun setupListeners(view: View) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = true
            override fun onQueryTextChange(txt: String?): Boolean {
                busquedaActual = txt ?: ""
                aplicarTodo()
                return true
            }
        })

        view.findViewById<Chip>(R.id.chipTodos).setOnClickListener { chipActual = "TODOS"; aplicarTodo() }
        view.findViewById<Chip>(R.id.chipAlergias).setOnClickListener { chipActual = "ALERGIAS"; aplicarTodo() }
        view.findViewById<Chip>(R.id.chipSinFotos).setOnClickListener { chipActual = "SIN_FOTOS"; aplicarTodo() }
        view.findViewById<Chip>(R.id.chipSalenSolos).setOnClickListener { chipActual = "SALE_SOLO"; aplicarTodo() }

        view.findViewById<ImageButton>(R.id.btnFiltrosAvanzados).setOnClickListener { mostrarDialogoFiltros() }

        // Aquí añadimos la acción del botón flotante para que abra el formulario
        // NOTA: Si al compilar te da error aquí, revisa abajo el Paso Extra sobre el ID
        view.findViewById<FloatingActionButton>(R.id.fabAddNino)?.setOnClickListener {
            val intent = Intent(requireContext(), FormularioNinoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun aplicarTodo() {
        adapter.filtrar(busquedaActual, chipActual, cursoActual, centroActual)
    }

    private fun cargarDatos() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = URL("http://10.0.2.2/TFG/obtener_participantes.php").readText()
                val array = JSONArray(res)
                val lista = mutableListOf<Nino>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    lista.add(Nino(
                        id = obj.optString("id"),
                        nombre = obj.optString("nombre"),
                        apellido1 = obj.optString("apellido1"),
                        apellido2 = obj.optString("apellido2"),
                        fecha_nacimiento = obj.optString("fecha_nacimiento"),
                        colegio_instituto = obj.optString("colegio_instituto"),
                        curso = obj.optString("curso"),
                        alergias = if (obj.optInt("alergias") == 1) "Sí" else "No",
                        sale_solo = if (obj.optInt("sale_solo") == 1) "Sí" else "No",
                        fotos = if (obj.optInt("fotos") == 1) "Sí" else "No",
                        id_tutor = obj.optString("id_tutor"),
                        vinculo_tutor = obj.optString("vinculo_tutor"),
                        observaciones = obj.optString("observaciones")
                    ))
                }
                withContext(Dispatchers.Main) {
                    adapter.setDatos(lista)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun mostrarDialogoFiltros() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_filtros, null)
        val spCurso = dialogView.findViewById<Spinner>(R.id.spinnerCurso)
        val spCentro = dialogView.findViewById<Spinner>(R.id.spinnerColegio)

        val cursos = arrayOf("Todos", "1º Primaria", "2º Primaria", "3º Primaria", "4º Primaria", "5º Primaria", "6º Primaria", "1º ESO", "2º ESO")
        val centros = arrayOf("Todos", "CEIP Munoa", "CEIP Rontegi", "CEIP Bagatza", "IES Trueba")

        spCurso.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, cursos)
        spCentro.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, centros)

        spCurso.setSelection(cursos.indexOf(cursoActual))
        spCentro.setSelection(centros.indexOf(centroActual))

        builder.setTitle("Filtrar Alumnos")
            .setView(dialogView)
            .setPositiveButton("Aplicar") { _, _ ->
                cursoActual = spCurso.selectedItem.toString()
                centroActual = spCentro.selectedItem.toString()
                aplicarTodo()
            }
            .setNeutralButton("Limpiar") { _, _ ->
                cursoActual = "Todos"; centroActual = "Todos"
                aplicarTodo()
            }
            .show()
    }
}