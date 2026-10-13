package pe.edu.instituto.ponce.mitiemposemanal

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MiTiempoSemanal.db"
        private const val DATABASE_VERSION = 3 // Subimos a 3 para arreglar el crash

        const val TABLE_CATEGORIES = "categorias"
        const val COL_CAT_ID = "id"
        const val COL_CAT_NAME = "nombre"

        const val TABLE_TASKS = "tareas"
        const val COL_TASK_ID = "id"
        const val COL_TASK_NAME = "nombre"
        const val COL_TASK_CAT_ID = "categoria_id"

        const val TABLE_EVENTS = "eventos"
        const val COL_EV_ID = "id"
        const val COL_EV_TASK_ID = "tarea_id"
        const val COL_EV_START = "inicio"
        const val COL_EV_END = "fin"
        const val COL_EV_DURATION = "duracion"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createCategoriesTable = ("CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIES ("
                + "$COL_CAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COL_CAT_NAME TEXT UNIQUE NOT NULL)")
        db.execSQL(createCategoriesTable)

        val createTasksTable = ("CREATE TABLE IF NOT EXISTS $TABLE_TASKS ("
                + "$COL_TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COL_TASK_NAME TEXT NOT NULL,"
                + "$COL_TASK_CAT_ID INTEGER NOT NULL,"
                + "FOREIGN KEY($COL_TASK_CAT_ID) REFERENCES $TABLE_CATEGORIES($COL_CAT_ID) ON DELETE CASCADE)")
        db.execSQL(createTasksTable)

        val createEventsTable = ("CREATE TABLE IF NOT EXISTS $TABLE_EVENTS ("
                + "$COL_EV_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COL_EV_TASK_ID INTEGER NOT NULL,"
                + "$COL_EV_START TEXT NOT NULL,"
                + "$COL_EV_END TEXT,"
                + "$COL_EV_DURATION TEXT,"
                + "FOREIGN KEY($COL_EV_TASK_ID) REFERENCES $TABLE_TASKS($COL_TASK_ID) ON DELETE CASCADE)")
        db.execSQL(createEventsTable)

        insertarCategoriaDefecto(db, "Estudio")
        insertarCategoriaDefecto(db, "Trabajo")
        insertarCategoriaDefecto(db, "Descanso")
        insertarCategoriaDefecto(db, "Ocio")
    }

    private fun insertarCategoriaDefecto(db: SQLiteDatabase, nombre: String) {
        val values = ContentValues()
        values.put(COL_CAT_NAME, nombre)
        db.insertWithOnConflict(TABLE_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Si viene del Sprint 1 (v1) -> crear eventos
        if (oldVersion < 2) {
            val createEventsTable = ("CREATE TABLE IF NOT EXISTS $TABLE_EVENTS ("
                    + "$COL_EV_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "$COL_EV_TASK_ID INTEGER NOT NULL,"
                    + "$COL_EV_START TEXT NOT NULL,"
                    + "$COL_EV_END TEXT,"
                    + "$COL_EV_DURATION TEXT,"
                    + "FOREIGN KEY($COL_EV_TASK_ID) REFERENCES $TABLE_TASKS($COL_TASK_ID) ON DELETE CASCADE)")
            db.execSQL(createEventsTable)
        }
        // Si viene de la v2 con columnas rotas -> borrar y recrear solo eventos
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTS")
            val createEventsTable = ("CREATE TABLE IF NOT EXISTS $TABLE_EVENTS ("
                    + "$COL_EV_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "$COL_EV_TASK_ID INTEGER NOT NULL,"
                    + "$COL_EV_START TEXT NOT NULL,"
                    + "$COL_EV_END TEXT,"
                    + "$COL_EV_DURATION TEXT,"
                    + "FOREIGN KEY($COL_EV_TASK_ID) REFERENCES $TABLE_TASKS($COL_TASK_ID) ON DELETE CASCADE)")
            db.execSQL(createEventsTable)
        }
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (!db.isReadOnly) {
            db.execSQL("PRAGMA foreign_keys=ON;")
        }
    }
}