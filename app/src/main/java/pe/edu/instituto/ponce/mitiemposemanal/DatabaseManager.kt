package pe.edu.instituto.ponce.mitiemposemanal

import android.content.ContentValues
import android.content.Context

class DatabaseManager(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // ================= MÉTODOS PARA CATEGORÍAS =================

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

    // Verifica si ya existe una categoría con ese nombre (ignorando mayúsculas/minúsculas).
    // El idAignorar sirve para cuando estamos editando, no nos marque como repetido el nombre actual de esa misma categoría.
    fun existeCategoria(nombre: String, idAignorar: Int = -1): Boolean {
        val db = dbHelper.readableDatabase
        val query = "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_CATEGORIES} WHERE LOWER(${DatabaseHelper.COL_CAT_NAME}) = LOWER(?) AND ${DatabaseHelper.COL_CAT_ID} != ?"
        val cursor = db.rawQuery(query, arrayOf(nombre, idAignorar.toString()))
        var existe = false
        if (cursor.moveToFirst()) {
            existe = cursor.getInt(0) > 0
        }
        cursor.close()
        db.close()
        return existe
    }

    fun agregarCategoria(nombre: String): Boolean {
        if (existeCategoria(nombre)) return false
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_CAT_NAME, nombre)
        }
        val result = db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values)
        db.close()
        return result != -1L
    }

    fun actualizarCategoria(id: Int, nombre: String): Boolean {
        if (existeCategoria(nombre, id)) return false
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_CAT_NAME, nombre)
        }
        val result = db.update(DatabaseHelper.TABLE_CATEGORIES, values, "${DatabaseHelper.COL_CAT_ID}=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    // Valida la HU-01: No permitir eliminar categorías que tengan tareas
    fun categoriaTieneTareas(idCategoria: Int): Boolean {
        val db = dbHelper.readableDatabase
        val query = "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_TASKS} WHERE ${DatabaseHelper.COL_TASK_CAT_ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(idCategoria.toString()))
        var tieneTareas = false
        if (cursor.moveToFirst()) {
            tieneTareas = cursor.getInt(0) > 0
        }
        cursor.close()
        db.close()
        return tieneTareas
    }

    fun eliminarCategoria(id: Int): Boolean {
        if (categoriaTieneTareas(id)) return false
        val db = dbHelper.writableDatabase
        val result = db.delete(DatabaseHelper.TABLE_CATEGORIES, "${DatabaseHelper.COL_CAT_ID}=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    // ================= MÉTODOS PARA TAREAS =================

    fun obtenerTareas(): List<Tarea> {
        val lista = mutableListOf<Tarea>()
        val db = dbHelper.readableDatabase
        // Usamos INNER JOIN para traer el nombre de la categoría asociada (HU-02)
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
                val id = cursor.getInt(0)
                val nombre = cursor.getString(1)
                val catId = cursor.getInt(2)
                val catNombre = cursor.getString(3)
                lista.add(Tarea(id, nombre, catId, catNombre))
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
        if (cursor.moveToFirst()) {
            existe = cursor.getInt(0) > 0
        }
        cursor.close()
        db.close()
        return existe
    }

    fun agregarTarea(nombre: String, idCategoria: Int): Boolean {
        if (existeTarea(nombre)) return false
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_TASK_NAME, nombre)
            put(DatabaseHelper.COL_TASK_CAT_ID, idCategoria)
        }
        val result = db.insert(DatabaseHelper.TABLE_TASKS, null, values)
        db.close()
        return result != -1L
    }

    fun actualizarTarea(id: Int, nombre: String, idCategoria: Int): Boolean {
        if (existeTarea(nombre, id)) return false
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_TASK_NAME, nombre)
            put(DatabaseHelper.COL_TASK_CAT_ID, idCategoria)
        }
        val result = db.update(DatabaseHelper.TABLE_TASKS, values, "${DatabaseHelper.COL_TASK_ID}=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    fun eliminarTarea(id: Int): Boolean {
        val db = dbHelper.writableDatabase
        val result = db.delete(DatabaseHelper.TABLE_TASKS, "${DatabaseHelper.COL_TASK_ID}=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }
}