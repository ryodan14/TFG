package com.example.tfg.ui.slideshow

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tfg.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class FormularioFacturaActivity : AppCompatActivity() {

    private lateinit var etNumero: TextInputEditText
    private lateinit var etCliente: TextInputEditText
    private lateinit var etConcepto: TextInputEditText
    private lateinit var etTotal: TextInputEditText // Corregido según XML
    private lateinit var etIva: TextInputEditText   // Corregido según XML
    private lateinit var tvTitulo: TextView
    private var tipoDocumento: String = "FACTURA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_factura)

        // 1. Vincular vistas con los IDs EXACTOS del XML
        tvTitulo = findViewById(R.id.tvTituloFormulario)
        etNumero = findViewById(R.id.etNumero)
        etCliente = findViewById(R.id.etCliente)
        etConcepto = findViewById(R.id.etConcepto)
        etTotal = findViewById(R.id.etTotal) // No etBaseImponible
        etIva = findViewById(R.id.etIva)     // No etIvaPorcentaje

        val btnGuardar = findViewById<MaterialButton>(R.id.btnGuardar)
        val btnVolver = findViewById<MaterialButton>(R.id.btnVolver)

        // 2. Lógica de Título (Factura o Presupuesto)
        val tipoRecibido = intent.getStringExtra("TIPO_NUEVO") ?: "FACTURA"
        tipoDocumento = tipoRecibido

        if (tipoDocumento == "PRESUPUESTO") {
            tvTitulo.text = "Nuevo PRESUPUESTO"
            btnGuardar.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2196F3"))
        } else {
            tvTitulo.text = "Nueva FACTURA"
        }

        // 3. Botones
        btnGuardar.setOnClickListener { guardarEnServidor() }
        btnVolver.setOnClickListener { finish() }
    }

    private fun guardarEnServidor() {
        val num = etNumero.text.toString()
        val cli = etCliente.text.toString()
        val con = etConcepto.text.toString()
        val base = etTotal.text.toString()
        val iva = etIva.text.toString()

        if (num.isEmpty() || cli.isEmpty() || base.isEmpty()) {
            Toast.makeText(this, "Rellena los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val params = "numero_factura=${URLEncoder.encode(num, "UTF-8")}" +
                        "&cliente=${URLEncoder.encode(cli, "UTF-8")}" +
                        "&concepto=${URLEncoder.encode(con, "UTF-8")}" +
                        "&base_imponible=$base" +
                        "&iva_porcentaje=$iva" +
                        "&tipo=$tipoDocumento" +
                        "&estado=PENDIENTE"

                val url = URL("http://10.0.2.2/TFG/insertar_factura.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.outputStream.write(params.toByteArray())

                val response = conn.inputStream.bufferedReader().readText()

                withContext(Dispatchers.Main) {
                    if (response.contains("Éxito")) {
                        Toast.makeText(this@FormularioFacturaActivity, "$tipoDocumento guardado", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@FormularioFacturaActivity, "Respuesta: $response", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@FormularioFacturaActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}