package cl.duoc.ourarea.data

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Clase para el cifrado seguro de contraseñas usando PBKDF2
 */
object PasswordHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    /**
     * Hashea una contraseña con salt aleatorio
     * @param password Contraseña en texto plano
     * @return String con formato "salt:hash" para almacenar en BD
     */
    fun hashPassword(password: String): String {
        // Generar salt aleatorio
        val salt = generateSalt()

        // Generar hash
        val hash = pbkdf2(password, salt)

        // Retornar "salt:hash"
        return "${salt.toHex()}:${hash.toHex()}"
    }

    /**
     * Verifica si una contraseña coincide con el hash almacenado
     * @param password Contraseña en texto plano a verificar
     * @param storedHash Hash almacenado con formato "salt:hash"
     * @return true si la contraseña es correcta
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val parts = storedHash.split(":")
            if (parts.size != 2) return false

            val salt = parts[0].hexToByteArray()
            val originalHash = parts[1].hexToByteArray()

            // Generar hash con el mismo salt
            val testHash = pbkdf2(password, salt)

            // Comparación constante en tiempo para evitar timing attacks
            originalHash.contentEquals(testHash)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Genera un salt aleatorio de 16 bytes
     */
    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return salt
    }

    /**
     * Aplica PBKDF2 para generar el hash
     */
    private fun pbkdf2(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    /**
     * Convierte ByteArray a String hexadecimal
     */
    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }

    /**
     * Convierte String hexadecimal a ByteArray
     */
    private fun String.hexToByteArray(): ByteArray {
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}
