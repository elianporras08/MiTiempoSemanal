package pe.edu.instituto.ponce.mitiemposemanal

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TareasActivity : AppCompatActivity() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var etNombreTarea: EditText
    private lateinit var spCategorias: Spinner
    private lateinit var btnGuardarTarea: Button
    private lateinit var lvTareas: ListView
    private lateinit var btnVolverTareas: Button

    private var listaCategorias = listOf<Categoria>()
    private var listaTareas = listOf<Tarea>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tareas)

        dbManager = DatabaseManager(this)

        etNombreTarea = findViewById(R.id.etNombreTarea)
        spCategorias = findViewById(R.id.spCategorias)
        btnGuardarTarea = findViewById(R.id.btnGuardarTarea)
        lvTareas = findViewById(R.id.lvTareas)
        btnVolverTareas = findViewById(R.id.btnVolverTareas)

        cargarCategorias()
        cargarTareas()

        // El botón superior ahora solo sirve para CREAR
        btnGuardarTarea.setOnClickListener {
            guardarNuevaTarea()
        }

        btnVolverTareas.setOnClickListener {
            finish()
        }
    }

    private fun cargarCategorias() {
        listaCategorias = dbManager.obtenerCategorias()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaCategorias)
        spCategorias.adapter = adapter
    }

    private fun cargarTareas() {
        listaTareas = dbManager.obtenerTareas()
        val adapter = TareaAdapter(
            this,
            listaTareas,
            onEditClick = { tarea -> mostrarDialogoEditar(tarea) },
            onDeleteClick = { tarea -> mostrarDialogoEliminar(tarea) }
        )
        lvTareas.adapter = adapter
    }

    private fun guardarNuevaTarea() {
        val nombre = etNombreTarea.text.toString().trim()
        val categoriaSeleccionada = spCategorias.selectedItem as? Categoria

        if (nombre.isEmpty()) {
            Toast.makeText(this, "La tarea debe tener un nombre", Toast.LENGTH_SHORT).show()
            return
        }

        if (categoriaSeleccionada == null) {
            Toast.makeText(this, "Debes crear y seleccionar una categoría primero", Toast.LENGTH_SHORT).show()
            return
        }

        val exito = dbManager.agregarTarea(nombre, categoriaSeleccionada.id)
        if (exito) {
            Toast.makeText(this, "Tarea guardada", Toast.LENGTH_SHORT).show()
            etNombreTarea.text.clear()
            if (listaCategorias.isNotEmpty()) spCategorias.setSelection(0)
            cargarTareas()
        } else {
            Toast.makeText(this, "Error: Ya existe una tarea con ese nombre", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoEditar(tarea: Tarea) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_editar_tarea)

        val etEditarNombre = dialog.findViewById<EditText>(R.id.etEditarNombreTarea)
        val spEditarCategoria = dialog.findViewById<Spinner>(R.id.spEditarCategoriaTarea)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarEditarTarea)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarEditarTarea)

        etEditarNombre.setText(tarea.nombre)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaCategorias)
        spEditarCategoria.adapter = adapter

        val indexCategoria = listaCategorias.indexOfFirst { it.id == tarea.categoriaId }
        if (indexCategoria != -1) {
            spEditarCategoria.setSelection(indexCategoria)
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnConfirmar.setOnClickListener {
            val nuevoNombre = etEditarNombre.text.toString().trim()
            val categoriaSeleccionada = spEditarCategoria.selectedItem as? Categoria

            if (nuevoNombre.isEmpty()) {
                Toast.makeText(this, "La tarea debe tener un nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (categoriaSeleccionada == null) return@setOnClickListener

            val exito = dbManager.actualizarTarea(tarea.id, nuevoNombre, categoriaSeleccionada.id)
            if (exito) {
                Toast.makeText(this, "Tarea actualizada", Toast.LENGTH_SHORT).show()
                cargarTareas()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Error: Ya existe otra tarea con ese nombre", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun mostrarDialogoEliminar(tarea: Tarea) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_eliminar)

        val tvMensaje = dialog.findViewById<TextView>(R.id.tvMensajeEliminar)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarEliminar)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarEliminar)

        tvMensaje.text = "¿Eliminar la tarea '${tarea.nombre}'?"

        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnConfirmar.setOnClickListener {
            val exito = dbManager.eliminarTarea(tarea.id)
            if (exito) {
                Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                cargarTareas()
            } else {
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        dialog.show()
    }
}