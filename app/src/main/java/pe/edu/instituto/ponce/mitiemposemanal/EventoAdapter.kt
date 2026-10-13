package pe.edu.instituto.ponce.mitiemposemanal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EventoAdapter(private var eventos: List<Evento>) : RecyclerView.Adapter<EventoAdapter.EventoViewHolder>() {

    private val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val outputTimeFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")

    class EventoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTarea: TextView = view.findViewById(R.id.tvItemTarea)
        val tvCategoria: TextView = view.findViewById(R.id.tvItemCategoria)
        val tvHorarios: TextView = view.findViewById(R.id.tvItemHorarios)
        val tvDuracion: TextView = view.findViewById(R.id.tvItemDuracion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.tvTarea.text = evento.tareaNombre
        holder.tvCategoria.text = "Categoría: ${evento.categoriaNombre}"

        try {
            val inicioParsed = LocalDateTime.parse(evento.inicio, inputFormatter)
            val inicioStr = inicioParsed.format(outputTimeFormatter)

            if (evento.fin != null) {
                val finParsed = LocalDateTime.parse(evento.fin, inputFormatter)
                val finStr = finParsed.format(outputTimeFormatter)
                holder.tvHorarios.text = "Inicio: $inicioStr  •  Fin: $finStr"
                holder.tvDuracion.text = evento.duracion ?: ""
            } else {
                holder.tvHorarios.text = "Inicio: $inicioStr  •  [EN CURSO]"
                holder.tvDuracion.text = "Activo"
            }
        } catch (e: Exception) {
            holder.tvHorarios.text = "Inicio: ${evento.inicio}"
            holder.tvDuracion.text = evento.duracion ?: ""
        }
    }

    override fun getItemCount(): Int = eventos.size

    fun updateData(nuevosEventos: List<Evento>) {
        eventos = nuevosEventos
        notifyDataSetChanged()
    }
}