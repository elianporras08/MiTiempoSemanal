package pe.edu.instituto.ponce.mitiemposemanal

data class Tarea(
    var id: Int = 0,
    var nombre: String,
    var categoriaId: Int,
    var categoriaNombre: String = ""
)