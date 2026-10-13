package pe.edu.instituto.ponce.mitiemposemanal

data class Categoria(
    var id: Int = 0,
    var nombre: String
) {
    // Sobrescribimos toString para que cuando mostremos la categoría en un menú desplegable (Spinner),
    // se lea directamente el nombre de la categoría y no la referencia de memoria del objeto.
    override fun toString(): String {
        return nombre
    }
}