package pt.isel.daw.gomoku.http.hypermedia

interface Entity<T> {
    val clazz: List<String>?
    val properties: T?
    val entities: List<SubEntity>?
    val actions: List<Action>?
    val links: List<Link>?
}
