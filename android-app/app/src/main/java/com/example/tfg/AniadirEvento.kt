package com.example.tfg.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.tfg.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

class AniadirEvento : AppCompatActivity() {

    // Declaramos las variables que coinciden con tu XML
    private lateinit var spTipo: Spinner
    private lateinit var etNombre: EditText
    private lateinit var etFecha: EditText
    private lateinit var etHoraInicio: EditText
    private lateinit var etHoraFin: EditText
    private lateinit var etMonitores: EditText
    private lateinit var etObservaciones: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button
    private lateinit var btnEliminar: Button
    private lateinit var tvTituloPagina: TextView

    private var idActividadEditar: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aniadir_evento2) // Asegúrate de que tu XML se llame así

        // 1. Vinculamos con los IDs exactos de tu XML
        spTipo = findViewById(R.id.spTipo)
        etNombre = findViewById(R.id.etNombre)
        etFecha = findViewById(R.id.etFecha)
        etHoraInicio = findViewById(R.id.etHoraInicio)
        etHoraFin = findViewById(R.id.etHoraFin)
        etMonitores = findViewById(R.id.etMonitores) // En tu XML se llama etMonitores
        etObservaciones = findViewById(R.id.etObservaciones)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)
        btnEliminar = findViewById(R.id.btnEliminar)
        tvTituloPagina = findViewById(R.id.tvTituloPagina)

        // 2. Configurar el Spinner (El desplegable de Tipo)
        val tipos = arrayOf("LUDOTEKA", "GAZTE_LOKALA", "HINCHABLES", "OTROS")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spTipo.adapter = adapter

        // 3. Configurar los selectores de Fecha y Hora (porque en el XML son focusable=false)
        configurarSelectoresFechaHora()

        // 4. Comprobar si venimos a EDITAR (recibimos datos)
        idActividadEditar = intent.getStringExtra("id_actividad")

        if (idActividadEditar != null) {
            // MODO EDICIÓN
            tvTituloPagina.text = "Editar Actividad"
            btnEliminar.visibility = View.VISIBLE // Mostramos el botón rojo
            btnGuardar.text = "Actualizar"

            // Rellenar los campos con los datos que vienen del Adapter
            etNombre.setText(intent.getStringExtra("nombre"))
            etFecha.setText(intent.getStringExtra("fecha"))
            etHoraInicio.setText(intent.getStringExtra("hora_inicio"))
            etHoraFin.setText(intent.getStringExtra("hora_fin"))
            etMonitores.setText(intent.getStringExtra("monitores"))
            etObservaciones.setText(intent.getStringExtra("observaciones"))

            // Seleccionar el valor correcto en el Spinner
            val tipoRecibido = intent.getStringExtra("tipo")
            val spinnerPosition = adapter.getPosition(tipoRecibido)
            if (spinnerPosition >= 0) {
                spTipo.setSelection(spinnerPosition)
            }
        }

        // 5. Botón GUARDAR logic
        btnGuardar.setOnClickListener {
            guardarDatos()
        }

        // 6. Botón CANCELAR logic
        btnCancelar.setOnClickListener {
            finish() // Cierra la pantalla y vuelve atrás
        }

        // 7. Botón ELIMINAR logic
        btnEliminar.setOnClickListener {
            confirmarBorrado()
        }
    }

    private fun configurarSelectoresFechaHora() {
        val calendar = Calendar.getInstance()

        // Click en Fecha
        etFecha.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                // Formato para MySQL: YYYY-MM-DD
                val mesAjustado = month + 1
                val fechaFormateada = String.format("%04d-%02d-%02d", year, mesAjustado, day)
                etFecha.setText(fechaFormateada)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Click en Hora Inicio
        etHoraInicio.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                etHoraInicio.setText(String.format("%02d:%02d", hour, minute))
            }, 12, 0, true).show()
        }

        // Click en Hora Fin
        etHoraFin.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                etHoraFin.setText(String.format("%02d:%02d", hour, minute))
            }, 14, 0, true).show()
        }
    }

// ... (resto del código igual hasta llegar a guardarDatos)

    private fun guardarDatos() {
        val nombre = etNombre.text.toString().trim()
        val tipo = spTipo.selectedItem.toString()
        val fecha = etFecha.text.toString().trim()
        val horaInicio = etHoraInicio.text.toString().trim()
        val horaFin = etHoraFin.text.toString().trim()
        val monitores = etMonitores.text.toString().trim()
        val observaciones = etObservaciones.text.toString().trim()

        if (nombre.isEmpty() || fecha.isEmpty() || horaInicio.isEmpty()) {
            Toast.makeText(this, "Rellena los campos obligatorios (*)", Toast.LENGTH_SHORT).show()
            return
        }

        val urlString = if (idActividadEditar != null) {
            "http://10.0.2.2/tfg/actualizar_actividad.php"
        } else {
            "http://10.0.2.2/tfg/insert_actividad.php"
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL(urlString)
                val con = url.openConnection() as HttpURLConnection
                con.requestMethod = "POST"
                con.doOutput = true
                con.connectTimeout = 5000 // Tiempo de espera

                // CORRECCIÓN: Usar URLEncoder para cada parámetro por separado
                val params = StringBuilder()
                params.append("nombre=").append(URLEncoder.encode(nombre, "UTF-8"))
                params.append("&tipo=").append(URLEncoder.encode(tipo, "UTF-8"))
                params.append("&fecha=").append(URLEncoder.encode(fecha, "UTF-8"))
                params.append("&hora_inicio=").append(URLEncoder.encode(horaInicio, "UTF-8"))
                params.append("&hora_fin=").append(URLEncoder.encode(horaFin, "UTF-8"))
                params.append("&monitores=").append(URLEncoder.encode(monitores, "UTF-8"))
                params.append("&observaciones=").append(URLEncoder.encode(observaciones, "UTF-8"))

                if (idActividadEditar != null) {
                    params.append("&id_actividad=").append(URLEncoder.encode(idActividadEditar!!, "UTF-8"))
                }

                val writer = OutputStreamWriter(con.outputStream)
                writer.write(params.toString())
                writer.flush()
                writer.close()

                val responseCode = con.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Actividad guardada", Toast.LENGTH_SHORT).show()
                        finish() // Vuelve a la pantalla principal
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Error del servidor: $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun confirmarBorrado() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Actividad")
            .setMessage("¿Estás seguro de que quieres borrar esta actividad?")
            .setPositiveButton("Sí") { _, _ ->
                borrarEnBaseDeDatos()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun borrarEnBaseDeDatos() {
        if (idActividadEditar == null) return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://10.0.2.2/tfg/borrar_actividad.php")
                val con = url.openConnection() as HttpURLConnection
                con.requestMethod = "POST"
                con.doOutput = true

                val params = "id_actividad=$idActividadEditar"
                val writer = OutputStreamWriter(con.outputStream)
                writer.write(params)
                writer.flush()
                writer.close()

                if (con.responseCode == HttpURLConnection.HTTP_OK) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Eliminado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}