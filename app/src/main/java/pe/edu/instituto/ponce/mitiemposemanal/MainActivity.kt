package pe.edu.instituto.ponce.mitiemposemanal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enlazamos las variables con los botones del diseño XML
        val btnCategorias = findViewById<Button>(R.id.btnCategorias)
        val btnTareas = findViewById<Button>(R.id.btnTareas)
        val btnCalendario = findViewById<Button>(R.id.btnCalendario) // Nuevo botón

        // Acción para ir a la pantalla de Categorías
        btnCategorias.setOnClickListener {
            val intent = Intent(this, CategoriasActivity::class.java)
            startActivity(intent)
        }

        // Acción para ir a la pantalla de Tareas
        btnTareas.setOnClickListener {
            val intent = Intent(this, TareasActivity::class.java)
            startActivity(intent)
        }

        // Acción para ir a la pantalla del Calendario
        btnCalendario.setOnClickListener {
            val intent = Intent(this, CalendarioActivity::class.java)
            startActivity(intent)
        }
    }
}