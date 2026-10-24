package com.example.tfg

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class FormularioNinoActivity : AppCompatActivity() {

    private var idNinoActual: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_nino)

        val tvTitulo = findViewById<TextView>(R.id.tvTituloFormulario)
        val etNombre = findViewById<EditText>(R.id.etFormNombre)
        val etApe1 = findViewById<EditText>(R.id.etFormApe1)
        val etApe2 = findViewById<EditText>(R.id.etFormApe2)
        val etFecha = findViewById<EditText>(R.id.etFormFecha)
        val etColegio = findViewById<EditText>(R.id.etFormColegio)
        val etCurso = findViewById<EditText>(R.id.etFormCurso)
        val etIdTutor = findViewById<EditText>(R.id.etFormIdTutor)
        val etVinculo = findViewById<EditText>(R.id.etFormVinculo)
        val etObs = findViewById<EditText>(R.id.etFormObs)
        val swAlergias = findViewById<Switch>(R.id.swFormAlergias)
        val swSaleSolo = findViewById<Switch>(R.id.swFormSaleSolo)
        val swFotos = findViewById<Switch>(R.id.swFormFotos)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarNino)


        if (intent.hasExtra("id")) {
            idNinoActual = intent.getStringExtra("id")
            tvTitulo.text = "Editar Niño"

            // Rellenar datos existentes
            etNombre.setText(intent.getStringExtra("nombre"))
            etApe1.setText(intent.getStringExtra("apellido1"))
            etApe2.setText(intent.getStringExtra("apellido2"))
            etFecha.setText(intent.getStringExtra("fecha_nacimiento"))
            etColegio.setText(intent.getStringExtra("colegio"))
            etCurso.setText(intent.getStringExtra("curso"))
            etIdTutor.setText(intent.getStringExtra("id_tutor"))
            etVinculo.setText(intent.getStringExtra("vinculo_tutor"))
            etObs.setText(intent.getStringExtra("observaciones"))
            swAlergias.isChecked = intent.getStringExtra("alergias") == "Sí"
            swSaleSolo.isChecked = intent.getStringExtra("saleSolo") == "Sí"
            swFotos.isChecked = intent.getStringExtra("fotos") == "Sí"
        }

        btnGuardar.setOnClickListener {
            // Recoger todo y crear el JSON
            val jsonDatos = JSONObject().apply {
                if (idNinoActual != null) put("id", idNinoActual)
                put("nombre", etNombre.text.toString())
                put("apellido1", etApe1.text.toString())
                put("apellido2", etApe2.text.toString())
                put("fecha_nacimiento", etFecha.text.toString())
                put("colegio", etColegio.text.toString())
                put("curso", etCurso.text.toString())
                put("id_tutor", if (etIdTutor.text.isEmpty()) "0" else etIdTutor.text.toString())
                put("vinculo_tutor", etVinculo.text.toString())
                put("observaciones", etObs.text.toString())
                put("alergias", if (swAlergias.isChecked) "1" else "0")
                put("sale_solo", if (swSaleSolo.isChecked) "1" else "0")
                put("fotos", if (swFotos.isChecked) "1" else "0")
            }

            guardarEnBaseDeDatos(jsonDatos)
        }
    }

    private fun guardarEnBaseDeDatos(json: JSONObject) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Si hay ID es actualizar, si no es insertar
                val urlPhp = if (idNinoActual != null) "actualizar_nino.php" else "insertar_nino.php"
                val url = URL("http://10.0.2.2/TFG/$urlPhp")

                val conexion = url.openConnection() as HttpURLConnection
                conexion.requestMethod = "POST"
                conexion.setRequestProperty("Content-Type", "application/json")
                conexion.doOutput = true

                val escritor = OutputStreamWriter(conexion.outputStream)
                escritor.write(json.toString())
                escritor.flush()
                escritor.close()

                val respuestaCode = conexion.responseCode
                withContext(Dispatchers.Main) {
                    if (respuestaCode == 200) {
                        Toast.makeText(this@FormularioNinoActivity, "¡Guardado con éxito!", Toast.LENGTH_SHORT).show()
                        finish() // Cierra y vuelve a la lista
                    } else {
                        Toast.makeText(this@FormularioNinoActivity, "Error al guardar", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}