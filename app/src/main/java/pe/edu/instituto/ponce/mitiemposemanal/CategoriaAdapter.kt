package pe.edu.instituto.ponce.mitiemposemanal

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView

class CategoriaAdapter(
    context: Context,
    private val categorias: List<Categoria>,
    private val onEditClick: (Categoria) -> Unit,
    private val onDeleteClick: (Categoria) -> Unit
) : ArrayAdapter<Categoria>(context, 0, categorias) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_categoria, parent, false)
        }

        val categoria = categorias[position]

        val tvNombre = view!!.findViewById<TextView>(R.id.tvNombreCategoriaItem)
        val btnEditar = view.findViewById<Button>(R.id.btnEditarCategoria)
        val btnBorrar = view.findViewById<Button>(R.id.btnBorrarCategoria)

        tvNombre.text = categoria.nombre

        // Configuramos la acción de los botones para que envíen la categoría seleccionada a la Actividad
        btnEditar.setOnClickListener {
            onEditClick(categoria)
        }

        btnBorrar.setOnClickListener {
            onDeleteClick(categoria)
        }

        return view
    }
}