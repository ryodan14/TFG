package com.example.tfg

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NinoAdapter : RecyclerView.Adapter<NinoAdapter.NinoViewHolder>() {

    private var listaMaestra = listOf<Nino>()
    private var ninos = mutableListOf<Nino>()

    class NinoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreNino)
        val tvCurso: TextView = view.findViewById(R.id.tvCursoNino)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NinoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nino, parent, false)
        return NinoViewHolder(view)
    }

    override fun onBindViewHolder(holder: NinoViewHolder, position: Int) {
        val nino = ninos[position]
        holder.tvNombre.text = "${nino.nombre} ${nino.apellido1} ${nino.apellido2}".trim()
        holder.tvCurso.text = "${nino.curso} - ${nino.colegio_instituto}"

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetalleNinoActivity::class.java).apply {
                putExtra("id", nino.id)
                putExtra("nombre", nino.nombre)
                putExtra("apellido1", nino.apellido1)
                putExtra("apellido2", nino.apellido2)
                putExtra("fecha_nacimiento", nino.fecha_nacimiento)
                putExtra("colegio", nino.colegio_instituto)
                putExtra("curso", nino.curso)
                putExtra("alergias", nino.alergias)
                putExtra("fotos", nino.fotos)
                putExtra("saleSolo", nino.sale_solo)
                putExtra("observaciones", nino.observaciones)
                putExtra("id_tutor", nino.id_tutor)
                putExtra("vinculo_tutor", nino.vinculo_tutor)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = ninos.size

    fun setDatos(nuevaLista: List<Nino>) {
        listaMaestra = nuevaLista
        ninos = nuevaLista.toMutableList()
        notifyDataSetChanged()
    }

    fun filtrar(texto: String, tipoChip: String, curso: String, centro: String) {
        val q = texto.lowercase().trim()

        ninos = listaMaestra.filter { nino ->
            val coincideTexto = q.isEmpty() || "${nino.nombre} ${nino.apellido1}".lowercase().contains(q)

            val coincideChip = when (tipoChip) {
                "ALERGIAS" -> nino.alergias == "Sí"
                "SIN_FOTOS" -> nino.fotos == "No" // Si no tiene permiso de fotos
                "SALE_SOLO" -> nino.sale_solo == "Sí"
                else -> true
            }

            val coincideCurso = curso == "Todos" || nino.curso == curso
            val coincideCentro = centro == "Todos" || nino.colegio_instituto == centro

            coincideTexto && coincideChip && coincideCurso && coincideCentro
        }.toMutableList()

        notifyDataSetChanged()
    }
}