package com.example.tfg

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class DetalleNinoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_nino)

        // Vincular los textos del XML
        val tvNombreCompleto = findViewById<TextView>(R.id.tvDetalleNombreCompleto)
        val tvFechaNac = findViewById<TextView>(R.id.tvDetalleFechaNac)
        val tvColegio = findViewById<TextView>(R.id.tvDetalleColegio)
        val tvCurso = findViewById<TextView>(R.id.tvDetalleCurso)
        val tvTutor = findViewById<TextView>(R.id.tvDetalleTutor)
        val tvAlergias = findViewById<TextView>(R.id.tvDetalleAlergias)
        val tvFotos = findViewById<TextView>(R.id.tvDetalleFotos)
        val tvSaleSolo = findViewById<TextView>(R.id.tvDetalleSaleSolo)
        val tvObservaciones = findViewById<TextView>(R.id.tvDetalleObservaciones)

        // Vincular los 3 botones
        val btnVolver = findViewById<Button>(R.id.btnVolverDetalle)
        val btnEditar = findViewById<Button>(R.id.btnEditarNino)
        val btnBorrar = findViewById<Button>(R.id.btnBorrarNino)

        // Recoger todos los datos enviados (¡Incluido el ID nuevo!)
        val id = intent.getStringExtra("id") ?: ""
        val nombre = intent.getStringExtra("nombre") ?: ""
        val apellido1 = intent.getStringExtra("apellido1") ?: ""
        val apellido2 = intent.getStringExtra("apellido2") ?: ""
        val fechaNac = intent.getStringExtra("fecha_nacimiento") ?: "Sin datos"
        val colegio = intent.getStringExtra("colegio") ?: "Sin datos"
        val curso = intent.getStringExtra("curso") ?: "Sin datos"
        val idTutor = intent.getStringExtra("id_tutor") ?: "?"
        val vinculoTutor = intent.getStringExtra("vinculo_tutor") ?: "Sin datos"

        val alergias = intent.getStringExtra("alergias") ?: "No"
        val fotos = intent.getStringExtra("fotos") ?: "No"
        val saleSolo = intent.getStringExtra("saleSolo") ?: "No"
        val observaciones = intent.getStringExtra("observaciones") ?: ""

        // Colocar los datos en la pantalla
        tvNombreCompleto.text = "$nombre $apellido1 $apellido2".trim()
        tvFechaNac.text = fechaNac
        tvColegio.text = colegio
        tvCurso.text = curso
        tvTutor.text = "ID: $idTutor - $vinculoTutor"
        tvFotos.text = fotos
        tvSaleSolo.text = saleSolo

        if (observaciones.isEmpty() || observaciones == "null") {
            tvObservaciones.text = "Ninguna observación destacable."
        } else {
            tvObservaciones.text = observaciones
        }

        // Toque visual: Alergias en rojo si las hay
        if (alergias.lowercase() == "sí" || alergias.lowercase() == "si") {
            tvAlergias.setTextColor(Color.RED)
            tvAlergias.text = "SÍ (Atención a las observaciones)"
        } else {
            tvAlergias.setTextColor(Color.parseColor("#4CAF50"))
            tvAlergias.text = "No"
        }

        // --- ACCIONES DE LOS BOTONES ---

        // 1. Botón Volver
        btnVolver.setOnClickListener { finish() }

        // 2. Botón Editar
        btnEditar.setOnClickListener {
            // Abre el formulario y le pasa TODOS los datos que ya tenemos (usando putExtras)
            val intentEdit = android.content.Intent(this, FormularioNinoActivity::class.java)
            intentEdit.putExtras(intent)
            startActivity(intentEdit)
            finish() // Cerramos esta ficha para que al guardar vuelva a la lista directamente
        }

        // 3. Botón Borrar
        btnBorrar.setOnClickListener {
            // Mostrar un cuadro de diálogo (pop-up) para confirmar si de verdad quiere borrarlo
            AlertDialog.Builder(this)
                .setTitle("Eliminar Niño")
                .setMessage("¿Estás seguro de que quieres borrar a $nombre de la base de datos? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, Borrar") { _, _ ->

                    // Si dice que sí, nos conectamos a la base de datos para borrarlo
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val url = URL("http://10.0.2.2/TFG/borrar_nino.php")
                            val conexion = url.openConnection() as HttpURLConnection
                            conexion.requestMethod = "POST"
                            conexion.setRequestProperty("Content-Type", "application/json")
                            conexion.doOutput = true

                            val json = JSONObject().apply { put("id", id) }
                            val escritor = OutputStreamWriter(conexion.outputStream)
                            escritor.write(json.toString())
                            escritor.flush()
                            conexion.responseCode

                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@DetalleNinoActivity, "Niño borrado correctamente", Toast.LENGTH_SHORT).show()
                                finish() // Cierra la ficha y vuelve a la lista
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}