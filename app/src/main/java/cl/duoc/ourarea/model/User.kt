package cl.duoc.ourarea.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User entity - Representa un usuario del sistema
 * @property id ID único del usuario (viene de Xano, no se autogenera)
 * @property email Email único del usuario
 * @property password Contraseña hasheada (bcrypt en Xano, placeholder local)
 * @property name Nombre completo del usuario
 * @property role Rol del usuario: admin, user (default)
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val email: String,
    val password: String,
    val name: String,
    val role: String = "user"
) {
    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_USER = "user"
    }
    
    fun isAdmin() = role == ROLE_ADMIN
    fun isUser() = role == ROLE_USER

    // TODOS los usuarios pueden crear eventos
    fun canCreateEvents() = true
    // Solo admins o el creador pueden editar
    fun canEditEvent(eventCreatorId: Int) = role == ROLE_ADMIN || this.id == eventCreatorId
    // Solo admins o el creador pueden eliminar
    fun canDeleteEvent(eventCreatorId: Int) = role == ROLE_ADMIN || this.id == eventCreatorId
}
