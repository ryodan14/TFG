package com.example.tfg.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tfg.R

class DetalleActividad : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_actividad)

        val tvTitulo = findViewById<TextView>(R.id.tvDetalleTitulo)
        val tvTipo = findViewById<TextView>(R.id.tvDetalleTipo)
        val tvFechaHora = findViewById<TextView>(R.id.tvDetalleFechaHora)
        val tvMonitores = findViewById<TextView>(R.id.tvDetalleMonitores)
        val tvObs = findViewById<TextView>(R.id.tvDetalleObs)
        val btnVolver = findViewById<Button>(R.id.btnVolver)

        tvTitulo.text = intent.getStringExtra("nombre")
        tvTipo.text = intent.getStringExtra("tipo")

        val fecha = intent.getStringExtra("fecha")
        val inicio = intent.getStringExtra("hora_inicio")
        val fin = intent.getStringExtra("hora_fin")
        tvFechaHora.text = "$fecha | $inicio - $fin"

        tvMonitores.text = intent.getStringExtra("monitores")
        tvObs.text = intent.getStringExtra("observaciones")

        btnVolver.setOnClickListener { finish() }
    }
}