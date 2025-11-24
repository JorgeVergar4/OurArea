package cl.duoc.ourarea.data

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitarios para PasswordHasher
 * Verifica el correcto funcionamiento del cifrado de contraseñas con PBKDF2
 */
class PasswordHasherTest {

    @Test
    fun `hashPassword genera hash no vacío`() {
        // Given
        val password = "miContraseña123"

        // When
        val hashedPassword = PasswordHasher.hashPassword(password)

        // Then
        assertNotNull(hashedPassword)
        assertTrue(hashedPassword.isNotEmpty())
        assertTrue(hashedPassword.contains(":")) // Formato salt:hash
    }

    @Test
    fun `hashPassword genera hashes diferentes para misma contraseña`() {
        // Given
        val password = "miContraseña123"

        // When
        val hash1 = PasswordHasher.hashPassword(password)
        val hash2 = PasswordHasher.hashPassword(password)

        // Then
        assertNotEquals(hash1, hash2) // Los salts son aleatorios
    }

    @Test
    fun `verifyPassword retorna true para contraseña correcta`() {
        // Given
        val password = "miContraseña123"
        val hashedPassword = PasswordHasher.hashPassword(password)

        // When
        val isValid = PasswordHasher.verifyPassword(password, hashedPassword)

        // Then
        assertTrue(isValid)
    }

    @Test
    fun `verifyPassword retorna false para contraseña incorrecta`() {
        // Given
        val correctPassword = "miContraseña123"
        val wrongPassword = "otraContraseña456"
        val hashedPassword = PasswordHasher.hashPassword(correctPassword)

        // When
        val isValid = PasswordHasher.verifyPassword(wrongPassword, hashedPassword)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `verifyPassword retorna false para hash inválido`() {
        // Given
        val password = "miContraseña123"
        val invalidHash = "hash_invalido_sin_formato"

        // When
        val isValid = PasswordHasher.verifyPassword(password, invalidHash)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `verifyPassword retorna false para hash vacío`() {
        // Given
        val password = "miContraseña123"
        val emptyHash = ""

        // When
        val isValid = PasswordHasher.verifyPassword(password, emptyHash)

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `hashPassword funciona con contraseñas vacías`() {
        // Given
        val password = ""

        // When
        val hashedPassword = PasswordHasher.hashPassword(password)

        // Then
        assertNotNull(hashedPassword)
        assertTrue(hashedPassword.contains(":"))
        assertTrue(PasswordHasher.verifyPassword(password, hashedPassword))
    }

    @Test
    fun `hashPassword funciona con contraseñas largas`() {
        // Given
        val password = "a".repeat(1000)

        // When
        val hashedPassword = PasswordHasher.hashPassword(password)

        // Then
        assertNotNull(hashedPassword)
        assertTrue(PasswordHasher.verifyPassword(password, hashedPassword))
    }

    @Test
    fun `hashPassword funciona con caracteres especiales`() {
        // Given
        val password = "¡@#$%^&*()_+-=[]{}|;':,.<>?/~`"

        // When
        val hashedPassword = PasswordHasher.hashPassword(password)

        // Then
        assertNotNull(hashedPassword)
        assertTrue(PasswordHasher.verifyPassword(password, hashedPassword))
    }

    @Test
    fun `hashPassword funciona con caracteres unicode`() {
        // Given
        val password = "contraseña🔒中文日本語"

        // When
        val hashedPassword = PasswordHasher.hashPassword(password)

        // Then
        assertNotNull(hashedPassword)
        assertTrue(PasswordHasher.verifyPassword(password, hashedPassword))
    }

    @Test
    fun `hash tiene formato correcto salt colon hash`() {
        // Given
        val password = "testPassword"

        // When
        val hashedPassword = PasswordHasher.hashPassword(password)
        val parts = hashedPassword.split(":")

        // Then
        assertEquals(2, parts.size)
        assertTrue(parts[0].isNotEmpty()) // Salt
        assertTrue(parts[1].isNotEmpty()) // Hash
        // Salt y hash deben ser hexadecimales
        assertTrue(parts[0].matches(Regex("[0-9a-f]+")))
        assertTrue(parts[1].matches(Regex("[0-9a-f]+")))
    }

    @Test
    fun `verifyPassword es sensible a mayúsculas`() {
        // Given
        val password = "MiContraseña"
        val hashedPassword = PasswordHasher.hashPassword(password)

        // When
        val isValidLower = PasswordHasher.verifyPassword("micontraseña", hashedPassword)
        val isValidUpper = PasswordHasher.verifyPassword("MICONTRASEÑA", hashedPassword)
        val isValidCorrect = PasswordHasher.verifyPassword(password, hashedPassword)

        // Then
        assertFalse(isValidLower)
        assertFalse(isValidUpper)
        assertTrue(isValidCorrect)
    }
}
