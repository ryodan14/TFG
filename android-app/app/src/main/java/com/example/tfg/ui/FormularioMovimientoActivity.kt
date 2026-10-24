package com.example.tfg.ui.slideshow

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tfg.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class FormularioMovimientoActivity : AppCompatActivity() {

    private lateinit var etConcepto: TextInputEditText
    private lateinit var etCantidad: TextInputEditText
    private lateinit var etObservaciones: TextInputEditText
    private lateinit var tvTitulo: TextView
    private var modo: String = "INSERTAR"
    private var idMov: String? = null
    private var tipoMov: String = "INGRESO"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_movimiento)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvTitulo = findViewById(R.id.tvTituloMovimiento)
        etConcepto = findViewById(R.id.etConceptoMov)
        etCantidad = findViewById(R.id.etCantidadMov)
        etObservaciones = findViewById(R.id.etObsMov)

        val btnGuardar = findViewById<MaterialButton>(R.id.btnGuardarMov)
        val btnEliminar = findViewById<MaterialButton>(R.id.btnEliminarMov)
        val btnVolver = findViewById<MaterialButton>(R.id.btnVolverMov)

        // Recuperar datos del Intent
        modo = intent.getStringExtra("MODO") ?: "INSERTAR"
        tipoMov = intent.getStringExtra("TIPO") ?: "INGRESO"

        if (modo == "EDITAR") {
            idMov = intent.getStringExtra("ID")
            tvTitulo.text = "Editar $tipoMov"
            etConcepto.setText(intent.getStringExtra("CONCEPTO"))
            etCantidad.setText(intent.getStringExtra("CANTIDAD"))
            etObservaciones.setText(intent.getStringExtra("OBS"))
            btnGuardar.text = "ACTUALIZAR"
            btnEliminar.visibility = View.VISIBLE
        } else {
            tvTitulo.text = "Registrar $tipoMov"
            btnEliminar.visibility = View.GONE
        }

        btnVolver.setOnClickListener { finish() }

        btnGuardar.setOnClickListener {
            val accion = if (modo == "EDITAR") "actualizar" else "insertar"
            ejecutarAccion(accion)
        }

        btnEliminar.setOnClickListener {
            ejecutarAccion("eliminar")
        }
    }

    private fun ejecutarAccion(accion: String) {
        val concepto = etConcepto.text.toString()
        val cantidad = etCantidad.text.toString()
        val obs = etObservaciones.text.toString()

        if (accion != "eliminar" && (concepto.isEmpty() || cantidad.isEmpty())) {
            Toast.makeText(this, "Rellena los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val params = "accion=$accion" +
                        "&id=$idMov" +
                        "&tipo=$tipoMov" +
                        "&concepto=${URLEncoder.encode(concepto, "UTF-8")}" +
                        "&cantidad=$cantidad" +
                        "&observaciones=${URLEncoder.encode(obs, "UTF-8")}"

                val url = URL("http://10.0.2.2/TFG/gestion_contabilidad.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.outputStream.write(params.toByteArray())

                val res = conn.inputStream.bufferedReader().readText()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FormularioMovimientoActivity, "Operación con éxito", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}