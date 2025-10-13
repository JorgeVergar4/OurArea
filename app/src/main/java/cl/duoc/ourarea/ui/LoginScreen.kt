package cl.duoc.ourarea.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.ourarea.viewmodel.AuthViewModel
import cl.duoc.ourarea.R

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginSuccess by authViewModel.loginSuccess.collectAsState()
    var showError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4FBF8)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "OurAREA",
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color(0xFF19B6B6),
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo OurAREA",
                modifier = Modifier.size(180.dp).padding(bottom = 16.dp)
            )
            Text(
                text = "Accede para ver eventos cercanos.",
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                textAlign = TextAlign.Center
            )
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it; showError = false
                },
                label = { Text("Correo electrónico", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF19B6B6),
                    unfocusedBorderColor = Color(0xFFE0E5EA),
                    focusedLabelColor = Color(0xFF19B6B6)
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it; showError = false
                },
                label = { Text("Contraseña", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF19B6B6),
                    unfocusedBorderColor = Color(0xFFE0E5EA),
                    focusedLabelColor = Color(0xFF19B6B6)
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )
            Button(
                onClick = {
                    authViewModel.login(email, password)
                    showError = true
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF19B6B6))
            ) {
                Text("Continuar", color = Color.White, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = { onGoToRegister() }) {
                Text("¿No tienes cuenta? Regístrate", color = Color(0xFF19B6B6))
            }
            if (loginSuccess == false && showError) {
                Text(
                    "Correo o contraseña incorrectos",
                    color = Color.Red,
                    modifier = Modifier.padding(top = 20.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            if (loginSuccess == true) {
                LaunchedEffect(Unit) {
                    onLoginSuccess()
                }
                Text(
                    "¡Inicio de sesión exitoso!",
                    color = Color(0xFF19B6B6),
                    modifier = Modifier.padding(top = 20.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
