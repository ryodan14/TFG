package com.example.tfg.ui.slideshow

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg.R

class FacturaAdapter(
    private var lista: List<Factura>,
    private val onClick: (Factura) -> Unit
) : RecyclerView.Adapter<FacturaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumero: TextView = view.findViewById(R.id.tvNumeroFactura)
        val tvCliente: TextView = view.findViewById(R.id.tvClienteFactura)
        val tvTotal: TextView = view.findViewById(R.id.tvTotalFactura)
        val tvEstado: TextView = view.findViewById(R.id.tvEstadoFactura)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_factura, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNumero.text = item.numero
        holder.tvCliente.text = "Cliente: ${item.cliente}"
        holder.tvTotal.text = "${item.total} €"
        holder.tvEstado.text = item.estado

        when (item.estado.uppercase()) {
            "PAGADA" -> holder.tvEstado.setTextColor(Color.parseColor("#4CAF50"))
            "PENDIENTE" -> holder.tvEstado.setTextColor(Color.parseColor("#FF9800"))
            else -> holder.tvEstado.setTextColor(Color.RED)
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = lista.size

    fun updateData(newList: List<Factura>) {
        lista = newList
        notifyDataSetChanged()
    }
}