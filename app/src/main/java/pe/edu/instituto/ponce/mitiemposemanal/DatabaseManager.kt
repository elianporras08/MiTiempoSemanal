package pe.edu.instituto.ponce.mitiemposemanal

import android.content.ContentValues
import android.content.Context

class DatabaseManager(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // ======= CATEGORÍAS =======
    fun obtenerCategorias(): List<Categoria> {
        val lista = mutableListOf<Categoria>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_CATEGORIES} ORDER BY ${DatabaseHelper.COL_CAT_NAME}", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_ID))
                val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CAT_NAME))
                lista.add(Categoria(id, nombre))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun existeCategoria(nombre: String, idAignorar: Int = -1): Boolean {
        val db = dbHelper.readableDatabase
        val query = "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_CATEGORIES} WHERE LOWER(${DatabaseHelper.COL_CAT_NAME}) = LOWER(?) AND ${DatabaseHelper.COL_CAT_ID} != ?"
        val cursor = db.rawQuery(query, arrayOf(nombre, idAignorar.toString()))
        var existe = false
        if (cursor.moveToFirst()) existe = cursor.getInt(0) > 0
        cursor.close()
        db.close()
        return existe
    }

    fun agregarCategoria(nombre: String): Boolean {
        if (existeCategoria(nombre)) return false
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply { put(DatabaseHelper.COL_CAT_NAME, nombre) }
        val result = db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values)
        db.close()
        return result != -1L
    }

    fun actualizarCategoria(id: Int, nombre: String): Boolean {
        if (existeCategoria(nombre, id)) return false
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply { put(DatabaseHelper.COL_CAT_NAME, nombre) }
        val result = db.update(DatabaseHelper.TABLE_CATEGORIES, values, "${DatabaseHelper.COL_CAT_ID}=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    fun categoriaTieneTareas(idCategoria: Int): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_TASKS} WHERE ${DatabaseHelper.COL_TASK_CAT_ID} = ?", arrayOf(idCategoria.toString()))
        var tiene = false
        if (cursor.moveToFirst()) tiene = cursor.getInt(0) > 0
        cursor.close()
        db.close()
        return tiene
    }

    fun eliminarCategoria(id: Int): Boolean {
        if (categoriaTieneTareas(id)) return false
        val db = dbHelper.writableDatabase
        val result = db.delete(DatabaseHelper.TABLE_CATEGORIES, "${DatabaseHelper.COL_CAT_ID}=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    // ======= TAREAS =======
    fun obtenerTareas(): List<Tarea> {
        val lista = mutableListOf<Tarea>()
        val db = dbHelper.readableDatabase
        val query = """
            SELECT t.${DatabaseHelper.COL_TASK_ID}, t.${DatabaseHelper.COL_TASK_NAME}, 
                   t.${DatabaseHelper.COL_TASK_CAT_ID}, c.${DatabaseHelper.COL_CAT_NAME}
            FROM ${DatabaseHelper.TABLE_TASKS} t
            INNER JOIN ${DatabaseHelper.TABLE_CATEGORIES} c ON t.${DatabaseHelper.COL_TASK_CAT_ID} = c.${DatabaseHelper.COL_CAT_ID}
            ORDER BY t.${DatabaseHelper.COL_TASK_NAME}
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                lista.add(Tarea(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // NUEVO HU-03: Búsqueda real en DB con cada letra
    fun buscarTareasPorTexto(texto: String): List<Tarea> {
        val lista = mutableListOf<Tarea>()
        if (texto.length < 2) return lista
        val db = dbHelper.readableDatabase
        val query = """
            SELECT t.${DatabaseHelper.COL_TASK_ID}, t.${DatabaseHelper.COL_TASK_NAME}, 
                   t.${DatabaseHelper.COL_TASK_CAT_ID}, c.${DatabaseHelper.COL_CAT_NAME}
            FROM ${DatabaseHelper.TABLE_TASKS} t
            INNER JOIN ${DatabaseHelper.TABLE_CATEGORIES} c ON t.${DatabaseHelper.COL_TASK_CAT_ID} = c.${DatabaseHelper.COL_CAT_ID}
            WHERE LOWER(t.${DatabaseHelper.COL_TASK_NAME}) LIKE LOWER(?)
            ORDER BY t.${DatabaseHelper.COL_TASK_NAME}
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf("%${texto.trim()}%"))
        if (cursor.moveToFirst()) {
            do {
                lista.add(Tarea(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    fun existeTarea(nombre: String, idAignorar: Int = -1): Boolean {
        val db = dbHelper.readableDatabase
        val query = "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_TASKS} WHERE LOWER(${DatabaseHelper.COL_TASK_NAME}) = LOWER(?) AND ${DatabaseHelper.COL_TASK_ID} != ?"
        val cursor = db.rawQuery(query, arrayOf(nombre, idAignorar.toString()))
        var existe = false
        if (cursor.moveToFirst()) existe = cursor.getInt(0) > 0
        cursor.close()
        db.close()
        return existe
    }

    fun agregarTarea(nombre: String, idCategoria: Int): Boolean {
        if (existeTarea(nombre)) return false
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_TASK_NAME, nombre.trim())
            put(DatabaseHelper.COL_TASK_CAT_ID, idCategoria)
        }
        val result = db.insert(DatabaseHelper.TABLE_TASKS, null, values)
        db.close()
        return result != -1L
    }

    fun agregarTareaRetornandoId(nombre: String, idCategoria: Int): Int {
        if (existeTarea(nombre)) return -1
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_TASK_NAME, nombre.trim())
            put(DatabaseHelper.COL_TASK_CAT_ID, idCategoria)
        }
        val id = db.insert(DatabaseHelper.TABLE_TASKS, null, values).toInt()
        db.close()
        return id
    }

    fun actualizarTarea(id: Int, nombre: String, idCategoria: Int): Boolean {
        if (existeTarea(nombre, id)) return false
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_TASK_NAME, nombre.trim())
            put(DatabaseHelper.COL_TASK_CAT_ID, idCategoria)
        }
        val result = db.update(DatabaseHelper.TABLE_TASKS, values, "${DatabaseHelper.COL_TASK_ID}=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    fun tareaTieneEventos(idTarea: Int): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_EVENTS} WHERE ${DatabaseHelper.COL_EV_TASK_ID} = ?", arrayOf(idTarea.toString()))
        var tiene = false
        if (cursor.moveToFirst()) tiene = cursor.getInt(0) > 0
        cursor.close()
        db.close()
        return tiene
    }

    fun eliminarTarea(id: Int): Boolean {
        if (tareaTieneEventos(id)) return false
        val db = dbHelper.writableDatabase
        val result = db.delete(DatabaseHelper.TABLE_TASKS, "${DatabaseHelper.COL_TASK_ID}=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    fun buscarTareaPorNombre(nombre: String): Tarea? {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT t.${DatabaseHelper.COL_TASK_ID}, t.${DatabaseHelper.COL_TASK_NAME}, 
                   t.${DatabaseHelper.COL_TASK_CAT_ID}, c.${DatabaseHelper.COL_CAT_NAME}
            FROM ${DatabaseHelper.TABLE_TASKS} t
            INNER JOIN ${DatabaseHelper.TABLE_CATEGORIES} c ON t.${DatabaseHelper.COL_TASK_CAT_ID} = c.${DatabaseHelper.COL_CAT_ID}
            WHERE LOWER(t.${DatabaseHelper.COL_TASK_NAME}) = LOWER(?)
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(nombre.trim()))
        var tarea: Tarea? = null
        if (cursor.moveToFirst()) {
            tarea = Tarea(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3))
        }
        cursor.close()
        db.close()
        return tarea
    }

    // ======= EVENTOS SPRINT 2 =======
    fun iniciarEvento(tareaId: Int, inicioIso: String): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_EV_TASK_ID, tareaId)
            put(DatabaseHelper.COL_EV_START, inicioIso)
        }
        val id = db.insert(DatabaseHelper.TABLE_EVENTS, null, values)
        db.close()
        return id
    }

    fun finalizarEvento(eventoId: Long, finIso: String, duracionStr: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_EV_END, finIso)
            put(DatabaseHelper.COL_EV_DURATION, duracionStr)
        }
        val result = db.update(DatabaseHelper.TABLE_EVENTS, values, "${DatabaseHelper.COL_EV_ID}=?", arrayOf(eventoId.toString()))
        db.close()
        return result > 0
    }

    fun obtenerEventos(): List<Evento> {
        val lista = mutableListOf<Evento>()
        val db = dbHelper.readableDatabase
        val query = """
            SELECT e.${DatabaseHelper.COL_EV_ID}, e.${DatabaseHelper.COL_EV_TASK_ID}, 
                   t.${DatabaseHelper.COL_TASK_NAME}, c.${DatabaseHelper.COL_CAT_NAME},
                   e.${DatabaseHelper.COL_EV_START}, e.${DatabaseHelper.COL_EV_END}, e.${DatabaseHelper.COL_EV_DURATION}
            FROM ${DatabaseHelper.TABLE_EVENTS} e
            INNER JOIN ${DatabaseHelper.TABLE_TASKS} t ON e.${DatabaseHelper.COL_EV_TASK_ID} = t.${DatabaseHelper.COL_TASK_ID}
            INNER JOIN ${DatabaseHelper.TABLE_CATEGORIES} c ON t.${DatabaseHelper.COL_TASK_CAT_ID} = c.${DatabaseHelper.COL_CAT_ID}
            ORDER BY e.${DatabaseHelper.COL_EV_ID} DESC
        """.trimIndent()
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                lista.add(
                    Evento(
                        id = cursor.getInt(0),
                        tareaId = cursor.getInt(1),
                        tareaNombre = cursor.getString(2),
                        categoriaNombre = cursor.getString(3),
                        inicio = cursor.getString(4),
                        fin = cursor.getString(5),
                        duracion = cursor.getString(6)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }
}