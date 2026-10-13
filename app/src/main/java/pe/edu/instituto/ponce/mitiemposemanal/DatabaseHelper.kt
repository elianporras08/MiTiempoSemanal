package pe.edu.instituto.ponce.mitiemposemanal

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MiTiempoSemanal.db"
        private const val DATABASE_VERSION = 1

        // Tabla Categorías
        const val TABLE_CATEGORIES = "categorias"
        const val COL_CAT_ID = "id"
        const val COL_CAT_NAME = "nombre"

        // Tabla Tareas
        const val TABLE_TASKS = "tareas"
        const val COL_TASK_ID = "id"
        const val COL_TASK_NAME = "nombre"
        const val COL_TASK_CAT_ID = "categoria_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla de categorías
        val createCategoriesTable = ("CREATE TABLE $TABLE_CATEGORIES ("
                + "$COL_CAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COL_CAT_NAME TEXT UNIQUE NOT NULL)")
        db.execSQL(createCategoriesTable)

        // Crear tabla de tareas con llave foránea apuntando a categorías
        val createTasksTable = ("CREATE TABLE $TABLE_TASKS ("
                + "$COL_TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COL_TASK_NAME TEXT NOT NULL,"
                + "$COL_TASK_CAT_ID INTEGER NOT NULL,"
                + "FOREIGN KEY($COL_TASK_CAT_ID) REFERENCES $TABLE_CATEGORIES($COL_CAT_ID))")
        db.execSQL(createTasksTable)

        // Insertar categorías por defecto (HU-01)
        insertarCategoriaDefecto(db, "Estudio")
        insertarCategoriaDefecto(db, "Trabajo")
        insertarCategoriaDefecto(db, "Descanso")
        insertarCategoriaDefecto(db, "Ocio")
    }

    private fun insertarCategoriaDefecto(db: SQLiteDatabase, nombre: String) {
        val values = ContentValues()
        values.put(COL_CAT_NAME, nombre)
        db.insert(TABLE_CATEGORIES, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        onCreate(db)
    }
}