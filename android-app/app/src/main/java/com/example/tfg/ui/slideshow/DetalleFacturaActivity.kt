package com.example.tfg.ui.slideshow

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tfg.R
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class DetalleFacturaActivity : AppCompatActivity() {
    private var idFactura: String? = null
    private lateinit var spEstado: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_factura)

        // Vincular vistas
        idFactura = intent.getStringExtra("ID")
        val etNum = findViewById<TextInputEditText>(R.id.etNumeroDetalle)
        val etCli = findViewById<TextInputEditText>(R.id.etClienteDetalle)
        val etTot = findViewById<TextInputEditText>(R.id.etTotalDetalle)
        spEstado = findViewById(R.id.spEstadoDetalle)

        // Configurar Spinner de Estados
        val opciones = arrayOf("PENDIENTE", "PAGADA")
        spEstado.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opciones)

        // Rellenar con los datos actuales
        etNum.setText(intent.getStringExtra("NUMERO"))
        etCli.setText(intent.getStringExtra("CLIENTE"))
        etTot.setText(intent.getStringExtra("TOTAL"))

        val estadoActual = intent.getStringExtra("ESTADO")?.uppercase() ?: "PENDIENTE"
        spEstado.setSelection(opciones.indexOf(estadoActual))

        // Botón Actualizar
        findViewById<Button>(R.id.btnActualizar).setOnClickListener {
            actualizar(etNum.text.toString(), etCli.text.toString(), etTot.text.toString(), spEstado.selectedItem.toString())
        }

        findViewById<Button>(R.id.btnVolverDetalle).setOnClickListener { finish() }
    }

    private fun actualizar(n: String, c: String, t: String, est: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://10.0.2.2/TFG/editar_factura.php")
                val params = "id_factura=$idFactura&numero_factura=$n&cliente=$c&base_imponible=$t&estado=$est"
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.outputStream.write(params.toByteArray())

                if (conn.inputStream.bufferedReader().readText().contains("Éxito")) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DetalleFacturaActivity, "Actualizado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}