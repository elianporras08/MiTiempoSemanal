package pe.edu.instituto.ponce.mitiemposemanal

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CategoriasActivity : AppCompatActivity() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var etNombreCategoria: EditText
    private lateinit var btnGuardarCategoria: Button
    private lateinit var lvCategorias: ListView
    private lateinit var btnVolver: Button

    private var listaCategorias = listOf<Categoria>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorias)

        dbManager = DatabaseManager(this)

        etNombreCategoria = findViewById(R.id.etNombreCategoria)
        btnGuardarCategoria = findViewById(R.id.btnGuardarCategoria)
        lvCategorias = findViewById(R.id.lvCategorias)
        btnVolver = findViewById(R.id.btnVolver)

        cargarCategorias()

        // El botón superior ahora solo sirve para CREAR
        btnGuardarCategoria.setOnClickListener {
            guardarNuevaCategoria()
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun cargarCategorias() {
        listaCategorias = dbManager.obtenerCategorias()
        val adapter = CategoriaAdapter(
            this,
            listaCategorias,
            onEditClick = { categoria -> mostrarDialogoEditar(categoria) },
            onDeleteClick = { categoria -> mostrarDialogoEliminar(categoria) }
        )
        lvCategorias.adapter = adapter
    }

    private fun guardarNuevaCategoria() {
        val nombre = etNombreCategoria.text.toString().trim()
        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        val exito = dbManager.agregarCategoria(nombre)
        if (exito) {
            Toast.makeText(this, "Categoría guardada", Toast.LENGTH_SHORT).show()
            etNombreCategoria.text.clear()
            cargarCategorias()
        } else {
            Toast.makeText(this, "Error: La categoría ya existe", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoEditar(categoria: Categoria) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_editar_categoria)

        val etEditarNombre = dialog.findViewById<EditText>(R.id.etEditarNombreCategoria)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarEditarCat)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarEditarCat)

        etEditarNombre.setText(categoria.nombre)

        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnConfirmar.setOnClickListener {
            val nuevoNombre = etEditarNombre.text.toString().trim()
            if (nuevoNombre.isEmpty()) {
                Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val exito = dbManager.actualizarCategoria(categoria.id, nuevoNombre)
            if (exito) {
                Toast.makeText(this, "Categoría actualizada", Toast.LENGTH_SHORT).show()
                cargarCategorias()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Error: Ya existe otra categoría con ese nombre", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun mostrarDialogoEliminar(categoria: Categoria) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_eliminar)

        val tvMensaje = dialog.findViewById<TextView>(R.id.tvMensajeEliminar)
        val btnCancelar = dialog.findViewById<Button>(R.id.btnCancelarEliminar)
        val btnConfirmar = dialog.findViewById<Button>(R.id.btnConfirmarEliminar)

        tvMensaje.text = "¿Eliminar la categoría '${categoria.nombre}'?"

        btnCancelar.setOnClickListener { dialog.dismiss() }
        btnConfirmar.setOnClickListener {
            val exito = dbManager.eliminarCategoria(categoria.id)
            if (exito) {
                Toast.makeText(this, "Categoría eliminada", Toast.LENGTH_SHORT).show()
                cargarCategorias()
            } else {
                Toast.makeText(this, "No se puede eliminar: tiene tareas asociadas", Toast.LENGTH_LONG).show()
            }
            dialog.dismiss()
        }
        dialog.show()
    }
}