package com.example.tfg.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.R
import com.example.tfg.ui.slideshow.MovimientoEconomico

class MovimientosAdapter(
    private var lista: List<MovimientoEconomico>,
    private val onItemClick: (MovimientoEconomico) -> Unit // Función para el clic
) : RecyclerView.Adapter<MovimientosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView = view.findViewById(R.id.tvColFecha)
        val tvTipo: TextView = view.findViewById(R.id.tvColTipo)
        val tvConcepto: TextView = view.findViewById(R.id.tvColConcepto)
        val tvCantidad: TextView = view.findViewById(R.id.tvColCantidad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movimiento, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mov = lista[position]
        holder.tvFecha.text = mov.fecha
        holder.tvTipo.text = mov.tipo
        holder.tvConcepto.text = mov.concepto

        val color = if (mov.tipo == "INGRESO") "#4CAF50" else "#F44336"
        holder.tvTipo.setTextColor(Color.parseColor(color))
        holder.tvCantidad.text = "${mov.cantidad} €"
        holder.tvCantidad.setTextColor(Color.parseColor(color))

        // Detectar el clic en toda la fila
        holder.itemView.setOnClickListener { onItemClick(mov) }
    }

    override fun getItemCount() = lista.size

    fun updateData(nuevaLista: List<MovimientoEconomico>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}