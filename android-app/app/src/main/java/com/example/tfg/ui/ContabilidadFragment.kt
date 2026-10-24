package com.example.tfg.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.R
import com.example.tfg.ui.slideshow.MovimientoEconomico
import com.example.tfg.ui.slideshow.FormularioMovimientoActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class ContabilidadFragment : Fragment() {

    private lateinit var adapterMov: MovimientosAdapter
    private lateinit var tvIngresos: TextView
    private lateinit var tvGastos: TextView
    private lateinit var tvBeneficio: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_contabilidad, container, false)

        tvIngresos = root.findViewById(R.id.tvIngresosTotal)
        tvGastos = root.findViewById(R.id.tvGastosTotal)
        tvBeneficio = root.findViewById(R.id.tvBeneficioTotal)
        val rv = root.findViewById<RecyclerView>(R.id.rvMovimientos)

        rv.layoutManager = LinearLayoutManager(requireContext())

        // Configuramos el clic de la lista aquí
        adapterMov = MovimientosAdapter(emptyList()) { mov ->
            val intent = Intent(context, FormularioMovimientoActivity::class.java)
            intent.putExtra("MODO", "EDITAR")
            intent.putExtra("ID", mov.id_movimiento)
            intent.putExtra("TIPO", mov.tipo)
            intent.putExtra("CONCEPTO", mov.concepto)
            intent.putExtra("CANTIDAD", mov.cantidad)
            intent.putExtra("OBS", mov.observaciones)
            startActivity(intent)
        }
        rv.adapter = adapterMov

        root.findViewById<Button>(R.id.btnRegIngreso).setOnClickListener { abrirFormulario("INGRESO") }
        root.findViewById<Button>(R.id.btnRegGasto).setOnClickListener { abrirFormulario("GASTO") }

        return root
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun abrirFormulario(tipo: String) {
        val intent = Intent(context, FormularioMovimientoActivity::class.java)
        intent.putExtra("MODO", "INSERTAR")
        intent.putExtra("TIPO", tipo)
        startActivity(intent)
    }

    private fun cargarDatos() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = URL("http://10.0.2.2/TFG/obtener_contabilidad.php").readText()
                val json = JSONObject(res)
                val arrayMovs = json.getJSONArray("lista")
                val listaMovs = mutableListOf<MovimientoEconomico>()

                for (i in 0 until arrayMovs.length()) {
                    val o = arrayMovs.getJSONObject(i)
                    listaMovs.add(MovimientoEconomico(
                        o.getString("id_movimiento"), o.getString("fecha"), o.getString("tipo"),
                        o.getString("concepto"), o.getString("cantidad"), o.getString("observaciones")
                    ))
                }

                withContext(Dispatchers.Main) {
                    tvIngresos.text = "${json.getString("ingresos")} €"
                    tvGastos.text = "${json.getString("gastos")} €"
                    tvBeneficio.text = "${json.getString("beneficio")} €"
                    adapterMov.updateData(listaMovs)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}