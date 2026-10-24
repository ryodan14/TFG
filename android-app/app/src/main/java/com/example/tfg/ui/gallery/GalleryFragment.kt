package com.example.tfg.ui.gallery

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.R
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.net.HttpURLConnection
import java.io.OutputStreamWriter
import java.util.Calendar

class GalleryFragment : Fragment() {

    private lateinit var adapter: EconomiaAdapter
    private var mesActual = 3 // Marzo
    private var anioActual = 2025

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflamos tu XML de ScrollView
        val root = inflater.inflate(R.layout.fragment_economia, container, false)

        val rv = root.findViewById<RecyclerView>(R.id.rvDetalleEconomia)
        rv.layoutManager = LinearLayoutManager(requireContext())

        // Evitamos que el scroll del RV choque con el ScrollView
        rv.isNestedScrollingEnabled = false

        adapter = EconomiaAdapter(emptyList()) { mov -> mostrarMenuOpciones(mov, root) }
        rv.adapter = adapter

        setupSpinners(root)

        root.findViewById<Button>(R.id.btnRegistrarSalida)?.setOnClickListener {
            mostrarDialogoFormulario(null, root)
        }

        // Carga inicial
        root.post { cargarDatos(root) }

        return root
    }

    private fun cargarDatos(v: View) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val urlStr = "http://10.0.2.2/TFG/obtener_economia.php?mes=$mesActual&anio=$anioActual"
                val res = URL(urlStr).readText()
                val json = JSONObject(res)

                val mensual = json.getJSONObject("resumen")
                val anual = json.getJSONObject("anual")
                val detallesArray = json.getJSONArray("detalles")

                val listaFinal = mutableListOf<MovimientoDetalle>()
                for (i in 0 until detallesArray.length()) {
                    val item = detallesArray.getJSONObject(i)
                    listaFinal.add(MovimientoDetalle(
                        item.optString("id_actividad", "0"),
                        item.optString("fecha", ""),
                        item.optString("descripcion", "Sin nombre"),
                        item.optString("monto", "0.00"),
                        item.optString("tipo", "")
                    ))
                }

                withContext(Dispatchers.Main) {
                    actualizarInterfaz(mensual, anual, listaFinal, v)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun actualizarInterfaz(mensual: JSONObject, anual: JSONObject, lista: List<MovimientoDetalle>, v: View) {
        // Mapeo con los IDs de tu XML
        v.findViewById<TextView>(R.id.tvTotalSalidas)?.text = mensual.optString("salidas")
        v.findViewById<TextView>(R.id.tvTotalIngresos)?.text = "${mensual.optString("ingresos")}€"
        v.findViewById<TextView>(R.id.tvTotalGastos)?.text = "${mensual.optString("gastos")}€"
        v.findViewById<TextView>(R.id.tvBeneficio)?.text = "Beneficio: ${mensual.optString("beneficio")}€"

        v.findViewById<TextView>(R.id.tvAnualSalidas)?.text = anual.optString("salidas")
        v.findViewById<TextView>(R.id.tvAnualIngresos)?.text = "${anual.optString("ingresos")}€"
        v.findViewById<TextView>(R.id.tvAnualGastos)?.text = "${anual.optString("gastos")}€"
        v.findViewById<TextView>(R.id.tvAnualBeneficio)?.text = "${anual.optString("beneficio")}€"

        adapter.updateData(lista)
    }

    private fun setupSpinners(root: View) {
        val spMes = root.findViewById<Spinner>(R.id.spinnerMes)
        val spAnio = root.findViewById<Spinner>(R.id.spinnerAnio)

        spMes.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, (1..12).toList())
        spAnio.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listOf(2024, 2025, 2026))

        spMes.setSelection(mesActual - 1)
        spAnio.setSelection(1) // 2025

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val m = spMes.selectedItem.toString().toInt()
                val a = spAnio.selectedItem.toString().toInt()
                if (m != mesActual || a != anioActual) {
                    mesActual = m
                    anioActual = a
                    cargarDatos(root)
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        spMes.onItemSelectedListener = listener
        spAnio.onItemSelectedListener = listener
    }

    private fun mostrarMenuOpciones(mov: MovimientoDetalle, root: View) {
        AlertDialog.Builder(requireContext())
            .setTitle(mov.descripcion)
            .setItems(arrayOf("Editar", "Eliminar")) { _, which ->
                if (which == 0) mostrarDialogoFormulario(mov, root)
                else confirmarEliminacion(mov, root)
            }.show()
    }

    private fun confirmarEliminacion(mov: MovimientoDetalle, root: View) {
        AlertDialog.Builder(requireContext())
            .setMessage("¿Eliminar ${mov.descripcion}?")
            .setPositiveButton("Sí") { _, _ -> ejecutarOperacion("eliminar", mov, root) }
            .setNegativeButton("No", null).show()
    }

    private fun mostrarDialogoFormulario(mov: MovimientoDetalle?, root: View) {
        val builder = AlertDialog.Builder(requireContext())
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 40, 60, 10)
        }
        val etNom = EditText(requireContext()).apply { hint = "Nombre"; setText(mov?.descripcion ?: "") }
        val etIng = EditText(requireContext()).apply { hint = "Monto"; setText(mov?.monto ?: "") }
        container.addView(etNom); container.addView(etIng)

        builder.setView(container).setTitle("Actividad")
            .setPositiveButton("Guardar") { _, _ ->
                val nuevo = MovimientoDetalle(mov?.id ?: "0", "2025-03-15", etNom.text.toString(), etIng.text.toString(), "ingreso")
                ejecutarOperacion(if (mov == null) "insertar" else "editar", nuevo, root)
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun ejecutarOperacion(accion: String, mov: MovimientoDetalle, root: View) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://10.0.2.2/TFG/gestion_actividad.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                val p = "accion=$accion&id_actividad=${mov.id}&nombre=${mov.descripcion}&fecha=${mov.fecha}&ingresos=${mov.monto}&gastos=0"
                OutputStreamWriter(conn.outputStream).use { it.write(p) }
                if (conn.responseCode == 200) withContext(Dispatchers.Main) { cargarDatos(root) }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}