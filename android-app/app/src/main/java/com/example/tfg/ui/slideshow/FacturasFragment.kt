package com.example.tfg.ui.slideshow

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.*
import com.example.tfg.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import org.json.JSONArray
import java.net.URL

class FacturasFragment : Fragment() {
    private lateinit var adapter: FacturaAdapter
    private lateinit var spTipo: Spinner
    private lateinit var spEstado: Spinner

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_facturas, container, false)

        spTipo = root.findViewById(R.id.spinnerTipos)
        spEstado = root.findViewById(R.id.spinnerEstados)
        val rv = root.findViewById<RecyclerView>(R.id.rvFacturas)
        val btnAdd = root.findViewById<FloatingActionButton>(R.id.fabAdd)

        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = FacturaAdapter(emptyList()) { factura ->
            val intent = Intent(requireContext(), DetalleFacturaActivity::class.java)
            intent.putExtra("ID", factura.id)
            intent.putExtra("NUMERO", factura.numero)
            intent.putExtra("CLIENTE", factura.cliente)
            intent.putExtra("TOTAL", factura.total)
            intent.putExtra("ESTADO", factura.estado)
            startActivity(intent)
        }
        rv.adapter = adapter

        // --- LÓGICA DEL BOTÓN "+" CORREGIDA ---
        btnAdd.setOnClickListener {
            val opciones = arrayOf("Nueva Factura", "Nuevo Presupuesto")
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("¿Qué deseas crear?")
            builder.setItems(opciones) { _, which ->
                val intent = Intent(requireContext(), FormularioFacturaActivity::class.java)
                if (which == 0) {
                    intent.putExtra("TIPO_NUEVO", "FACTURA")
                } else {
                    intent.putExtra("TIPO_NUEVO", "PRESUPUESTO")
                }
                startActivity(intent)
            }
            builder.show()
        }

        setupSpinners()
        return root
    }

    private fun setupSpinners() {
        val tipos = arrayOf("Todos", "Factura", "Presupuesto")
        val estados = arrayOf("Todos", "Pendiente", "Pagada")
        spTipo.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, tipos)
        spEstado.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, estados)

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) { cargarDatos() }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        spTipo.onItemSelectedListener = listener
        spEstado.onItemSelectedListener = listener
    }

    private fun cargarDatos() {
        val t = spTipo.selectedItem.toString()
        val e = spEstado.selectedItem.toString()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = URL("http://10.0.2.2/TFG/obtener_facturas.php?tipo=$t&estado=$e").readText()
                val json = JSONArray(res)
                val lista = mutableListOf<Factura>()
                for (i in 0 until json.length()) {
                    val o = json.getJSONObject(i)
                    lista.add(Factura(
                        o.getString("id"),
                        o.getString("numero"),
                        o.getString("tipo"),
                        o.getString("cliente"),
                        o.getString("fecha"),
                        o.getString("total"),
                        o.getString("estado")
                    ))
                }
                withContext(Dispatchers.Main) { adapter.updateData(lista) }
            } catch (ex: Exception) { ex.printStackTrace() }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }
}