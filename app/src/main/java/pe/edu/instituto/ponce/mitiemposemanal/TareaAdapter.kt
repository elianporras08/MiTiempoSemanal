package pe.edu.instituto.ponce.mitiemposemanal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView

class TareaAdapter(
    context: Context,
    private val tareas: List<Tarea>,
    private val onEditClick: (Tarea) -> Unit,
    private val onDeleteClick: (Tarea) -> Unit
) : ArrayAdapter<Tarea>(context, 0, tareas) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_tarea, parent, false)
        }

        val tarea = tareas[position]

        val tvNombre = view!!.findViewById<TextView>(R.id.tvNombreTareaItem)
        val tvCategoria = view.findViewById<TextView>(R.id.tvCategoriaTareaItem)
        val btnEditar = view.findViewById<Button>(R.id.btnEditarTarea)
        val btnBorrar = view.findViewById<Button>(R.id.btnBorrarTarea)

        // Asignamos los textos al diseño
        tvNombre.text = tarea.nombre
        tvCategoria.text = tarea.categoriaNombre

        // Configuramos la acción de los botones para que envíen la tarea seleccionada a la Actividad
        btnEditar.setOnClickListener {
            onEditClick(tarea)
        }

        btnBorrar.setOnClickListener {
            onDeleteClick(tarea)
        }

        return view
    }
}