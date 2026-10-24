package com.example.tfg.ui.gallery

import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.R

class EconomiaAdapter(
    private var lista: List<MovimientoDetalle>,
    private val onLongClick: (MovimientoDetalle) -> Unit
) : RecyclerView.Adapter<EconomiaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView = view.findViewById(R.id.tvColFecha)
        val tvDesc: TextView = view.findViewById(R.id.tvColConcepto)
        val tvMonto: TextView = view.findViewById(R.id.tvColCantidad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_movimiento, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.tvFecha.text = item.fecha
        holder.tvDesc.text = item.descripcion
        holder.tvMonto.text = "${item.monto}€"
        holder.itemView.setOnLongClickListener { onLongClick(item); true }
    }

    override fun getItemCount() = lista.size

    fun updateData(newList: List<MovimientoDetalle>) {
        lista = newList
        notifyDataSetChanged()
    }
}