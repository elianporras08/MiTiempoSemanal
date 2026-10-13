package pe.edu.instituto.ponce.mitiemposemanal

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CalendarioActivity : AppCompatActivity() {

    private lateinit var dbManager: DatabaseManager
    private lateinit var etBuscarTarea: AutoCompleteTextView
    private lateinit var tvCategoriaSeleccionada: TextView
    private lateinit var btnIniciar: Button
    private lateinit var btnFinalizar: Button
    private lateinit var rvEventos: RecyclerView
    private lateinit var eventoAdapter: EventoAdapter

    private var eventoEnCursoId: Long = -1L
    private val prefsName = "MiTiempoPrefs"
    private val keyEventoId = "evento_en_curso_id"

    // Para HU-03
    private var tareasFiltradas: List<Tarea> = emptyList()
    private var tareaSeleccionada: Tarea? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario)

        dbManager = DatabaseManager(this)

        etBuscarTarea = findViewById(R.id.etBuscarTarea)
        tvCategoriaSeleccionada = findViewById(R.id.tvCategoriaSeleccionada)
        btnIniciar = findViewById(R.id.btnIniciar)
        btnFinalizar = findViewById(R.id.btnFinalizar)
        rvEventos = findViewById(R.id.rvEventos)
        val btnVolver: TextView = findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener { finish() }

        // RecyclerView
        eventoAdapter = EventoAdapter(dbManager.obtenerEventos())
        rvEventos.layoutManager = LinearLayoutManager(this)
        rvEventos.adapter = eventoAdapter

        // HU-03: Configurar búsqueda con cada letra y fix del dropdown blanco
        configurarBusquedaEnTiempoReal()

        // HU-05: Restaurar evento en curso
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        eventoEnCursoId = prefs.getLong(keyEventoId, -1L)
        if (eventoEnCursoId != -1L) {
            // Validar que realmente siga abierto (fin = null)
            val evento = dbManager.obtenerEventos().find { it.id.toLong() == eventoEnCursoId && it.fin == null }
            if (evento != null) {
                tareaSeleccionada = Tarea(evento.tareaId, evento.tareaNombre, 0, evento.categoriaNombre)
                etBuscarTarea.setText(evento.tareaNombre)
                tvCategoriaSeleccionada.text = "Categoría: ${evento.categoriaNombre}"
                activarModoEnCurso(true)
            } else {
                eventoEnCursoId = -1L
                prefs.edit().remove(keyEventoId).apply()
            }
        }

        btnIniciar.setOnClickListener {
            val nombreTarea = etBuscarTarea.text.toString().trim()
            if (nombreTarea.length < 2) {
                Toast.makeText(this, "Escribe al menos 2 letras", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Si ya seleccionó de la lista, usar esa
            val tareaExacta = tareaSeleccionada?.takeIf { it.nombre.equals(nombreTarea, ignoreCase = true) }
                ?: dbManager.buscarTareaPorNombre(nombreTarea)

            if (tareaExacta == null) {
                mostrarDialogoCrearTarea(nombreTarea)
            } else {
                iniciarCronometroConTarea(tareaExacta.id)
            }
        }

        btnFinalizar.setOnClickListener {
            finalizarCronometroActual()
        }
    }

    private fun configurarBusquedaEnTiempoReal() {
        // Fix visual: fondo oscuro para el dropdown
        etBuscarTarea.setDropDownBackgroundDrawable(
            ContextCompat.getDrawable(this, android.R.color.background_dark) ?:
            ContextCompat.getDrawable(this, android.R.drawable.editbox_background)
        )
        etBuscarTarea.dropDownHeight = ViewGroup.LayoutParams.WRAP_CONTENT
        etBuscarTarea.threshold = 2

        etBuscarTarea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString()
                if (texto.length >= 2 && tareaSeleccionada == null) {
                    actualizarListaFiltrada(texto)
                } else if (texto.length < 2) {
                    tvCategoriaSeleccionada.text = ""
                    tareaSeleccionada = null
                }
            }
        })

        etBuscarTarea.setOnItemClickListener { _, _, position, _ ->
            if (position < tareasFiltradas.size) {
                val tarea = tareasFiltradas[position]
                tareaSeleccionada = tarea
                tvCategoriaSeleccionada.text = "Categoría: ${tarea.categoriaNombre}"
            }
        }

        // Si borra o cambia texto manualmente, invalidar selección
        etBuscarTarea.setOnDismissListener {
            // No hacer nada, mantenemos selección si coincide
        }
    }

    private fun actualizarListaFiltrada(texto: String) {
        tareasFiltradas = dbManager.buscarTareasPorTexto(texto) // HU-03: búsqueda en DB con LIKE
        val nombres = tareasFiltradas.map { it.nombre }

        // Adapter custom para que el texto se vea en tema oscuro
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, nombres) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(ContextCompat.getColor(context, R.color.blanco_pixel))
                return view
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.fondo_oscuro))
                (view as TextView).setTextColor(ContextCompat.getColor(context, R.color.blanco_pixel))
                view.setPadding(24, 24, 24, 24)
                return view
            }
        }
        etBuscarTarea.setAdapter(adapter)
        if (nombres.isNotEmpty()) {
            etBuscarTarea.showDropDown()
        }
    }

    private fun mostrarDialogoCrearTarea(nombreTarea: String) {
        val categorias = dbManager.obtenerCategorias()
        if (categorias.isEmpty()) {
            Toast.makeText(this, "No hay categorías registradas", Toast.LENGTH_SHORT).show()
            return
        }
        val nombresCategorias = categorias.map { it.nombre }.toTypedArray()
        var index = 0
        AlertDialog.Builder(this)
            .setTitle("[ NUEVA TAREA ]")
            .setMessage("La tarea \"$nombreTarea\" no existe. Elige categoría:")
            .setSingleChoiceItems(nombresCategorias, 0) { _, which -> index = which }
            .setPositiveButton("Crear e Iniciar") { _, _ ->
                val cat = categorias[index]
                val nuevaId = dbManager.agregarTareaRetornandoId(nombreTarea, cat.id)
                if (nuevaId != -1) {
                    tareaSeleccionada = Tarea(nuevaId, nombreTarea, cat.id, cat.nombre)
                    tvCategoriaSeleccionada.text = "Categoría: ${cat.nombre}"
                    iniciarCronometroConTarea(nuevaId)
                } else {
                    Toast.makeText(this, "Error al crear tarea", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun iniciarCronometroConTarea(tareaId: Int) {
        val inicioIso = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val idGenerado = dbManager.iniciarEvento(tareaId, inicioIso)
        if (idGenerado != -1L) {
            eventoEnCursoId = idGenerado
            getSharedPreferences(prefsName, MODE_PRIVATE).edit().putLong(keyEventoId, eventoEnCursoId).apply()
            activarModoEnCurso(true)
            Toast.makeText(this, "Evento iniciado", Toast.LENGTH_SHORT).show()
            eventoAdapter.updateData(dbManager.obtenerEventos())
        } else {
            Toast.makeText(this, "Error al iniciar evento", Toast.LENGTH_SHORT).show()
        }
    }

    private fun finalizarCronometroActual() {
        val finIso = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val eventos = dbManager.obtenerEventos()
        val eventoActual = eventos.find { it.id.toLong() == eventoEnCursoId }

        var duracionTexto = "0m"
        if (eventoActual != null) {
            try {
                val inicioDt = LocalDateTime.parse(eventoActual.inicio, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val finDt = LocalDateTime.parse(finIso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val duracion = Duration.between(inicioDt, finDt)
                val horas = duracion.toHours()
                val minutos = duracion.toMinutes() % 60
                duracionTexto = if (horas > 0) "${horas}h ${minutos}m" else "${minutos}m"
                if (duracionTexto == "0m") duracionTexto = "<1m"
            } catch (e: Exception) {
                duracionTexto = "Completado"
            }
        }

        if (dbManager.finalizarEvento(eventoEnCursoId, finIso, duracionTexto)) {
            eventoEnCursoId = -1L
            getSharedPreferences(prefsName, MODE_PRIVATE).edit().remove(keyEventoId).apply()
            activarModoEnCurso(false)
            etBuscarTarea.setText("")
            tvCategoriaSeleccionada.text = ""
            tareaSeleccionada = null
            Toast.makeText(this, "Evento finalizado: $duracionTexto", Toast.LENGTH_SHORT).show()
            eventoAdapter.updateData(dbManager.obtenerEventos())
        } else {
            Toast.makeText(this, "Error al finalizar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun activarModoEnCurso(enCurso: Boolean) {
        btnIniciar.isEnabled = !enCurso
        btnFinalizar.isEnabled = enCurso
        etBuscarTarea.isEnabled = !enCurso
        if (enCurso) {
            btnIniciar.alpha = 0.4f
            btnFinalizar.alpha = 1f
        } else {
            btnIniciar.alpha = 1f
            btnFinalizar.alpha = 0.4f
        }
    }
}