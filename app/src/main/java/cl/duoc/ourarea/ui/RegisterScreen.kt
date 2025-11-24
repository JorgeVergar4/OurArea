package cl.duoc.ourarea.ui

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.ourarea.R
import cl.duoc.ourarea.viewmodel.AuthViewModel
import cl.duoc.ourarea.ui.theme.AppColors
import kotlinx.coroutines.delay


fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordMatchError by remember { mutableStateOf<String?>(null) }

    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val emailExists by authViewModel.emailExists.collectAsState()
    val emailCheckError by authViewModel.emailCheckError.collectAsState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val isLandscape = screenWidth > screenHeight

    // Verificar coincidencia de contraseñas
    LaunchedEffect(password, confirmPassword) {
        passwordMatchError = when {
            confirmPassword.isBlank() -> null
            password != confirmPassword -> "Las contraseñas no coinciden"
            else -> null
        }
    }

    // Verificar email en tiempo real con debounce
    LaunchedEffect(email) {
        delay(500)
        if (email.isNotBlank()) {
            if (isValidEmail(email)) {
                authViewModel.checkEmailExists(email)
                emailError = null
            } else {
                emailError = "Formato de email inválido"
                authViewModel.clearEmailCheck()
            }
        } else {
            emailError = null
            authViewModel.clearEmailCheck()
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.Center
    ) {
        // Contenedor con scroll para pantallas pequeñas
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (isLandscape) 64.dp else 24.dp,
                        vertical = 16.dp
                    )
                    .widthIn(max = 600.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = if (isLandscape) 48.dp else 32.dp,
                        vertical = if (isLandscape) 24.dp else 32.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo OurArea
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo OurArea",
                        modifier = Modifier
                            .size(if (isLandscape) 60.dp else 80.dp)
                            .padding(bottom = if (isLandscape) 4.dp else 8.dp)
                    )

                    Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 12.dp))

                    Text(
                        text = "Crear Cuenta",
                        fontSize = if (isLandscape) 20.sp else 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )

                    Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 8.dp))

                    Text(
                        text = "Únete a la comunidad",
                        fontSize = if (isLandscape) 12.sp else 14.sp,
                        color = androidx.compose.ui.graphics.Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 20.dp))

                    // Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = AppColors.Primary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(if (isLandscape) 10.dp else 12.dp))

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            authViewModel.clearEmailCheck()
                            emailError = when {
                                it.isBlank() -> null
                                !it.contains("@") -> "El email debe contener @"
                                !isValidEmail(it) -> "Formato de email inválido"
                                else -> null
                            }
                        },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = AppColors.Primary)
                        },
                        trailingIcon = {
                            if (email.isNotBlank()) {
                                when {
                                    emailError != null -> Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Email inválido",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    emailExists -> Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Email ya existe",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    emailCheckError == null && email.length > 3 && isValidEmail(email) -> Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Email disponible",
                                        tint = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                    )
                                }
                            }
                        },
                        isError = emailError != null || emailExists,
                        supportingText = {
                            when {
                                emailError != null -> Text(
                                    text = emailError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = if (isLandscape) 10.sp else 12.sp
                                )
                                emailCheckError != null -> Text(
                                    text = emailCheckError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = if (isLandscape) 10.sp else 12.sp
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(if (isLandscape) 10.dp else 12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = AppColors.Primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = AppColors.Primary
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(if (isLandscape) 10.dp else 12.dp))

                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar Contraseña") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = AppColors.Primary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = AppColors.Primary
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordMatchError != null,
                        supportingText = {
                            if (passwordMatchError != null) {
                                Text(
                                    text = passwordMatchError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = if (isLandscape) 10.sp else 12.sp
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // General Error Message
                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = if (isLandscape) 10.sp else 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 24.dp))

                    // Register Button
                    Button(
                        onClick = {
                            authViewModel.clearError()
                            if (password == confirmPassword && isValidEmail(email)) {
                                authViewModel.register(name.trim(), email.trim(), password)
                            } else if (!isValidEmail(email)) {
                                emailError = "Ingresa un email válido"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isLandscape) 45.dp else 50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isLoading &&
                                !emailExists &&
                                emailError == null &&
                                passwordMatchError == null &&
                                name.isNotBlank() &&
                                email.isNotBlank() &&
                                password.isNotBlank() &&
                                confirmPassword.isNotBlank() &&
                                password == confirmPassword &&
                                isValidEmail(email)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = AppColors.TextOnPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                "Crear Cuenta",
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextOnPrimary,
                                fontSize = if (isLandscape) 14.sp else 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(if (isLandscape) 12.dp else 16.dp))

                    // Login Link
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "¿Ya tienes cuenta? ",
                            color = AppColors.TextSecondary,
                            fontSize = if (isLandscape) 12.sp else 14.sp
                        )
                        TextButton(onClick = onNavigateToLogin) {
                            Text(
                                text = "Inicia sesión",
                                color = AppColors.Primary,
                                fontSize = if (isLandscape) 12.sp else 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
