package cl.duoc.ourarea.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.ourarea.model.Event
import cl.duoc.ourarea.R

@Composable
fun HomeScreen() {
    // Eventos DEMO por ahora
    val events = remember {
        listOf(
            Event(
                1, "Feria gastronómica", "Disfruta platos típicos", R.drawable.feria_gastronomica, "1.2 km", "Hoy 19:00"
            ),
            Event(
                2, "Concierto al aire libre", "Música para todos", R.drawable.concierto_aire_libre, "2.0 km", "Sáb 21:00"
            ),
            Event(
                3, "Mercado de barrio", "Productos locales", R.drawable.mercado_barrio, "900 m", "Mañana"
            )
        )
    }

    val mapImage = R.drawable.map_demo // Algo estatico por mientras

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Text(
            "Mapa de eventos",
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp),
            textAlign = TextAlign.Center
        )
        // simulador de mapa fake jojo
        Box(
            Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = painterResource(mapImage),
                contentDescription = "Mapa de eventos",
                modifier = Modifier.fillMaxSize()
            )
            //Eventos en el mapa (mockup)
            Row(
                Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EventMapTag("Mercado de barrio")
                Spacer(Modifier.width(16.dp))
                EventMapTag("Feria gastronómica")
            }
        }
        // Lista de eventos
        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
            ) {
                events.forEach { event ->
                    EventCard(event)
                }
                Spacer(Modifier.height(44.dp)) // para la barra inferior
            }
        }
        // Botones de navegación inferior
        Row(
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(Color.Transparent),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationButton(
                "Mapa", true, Modifier.weight(1f)
            )
            NavigationButton(
                "Cuadrícula", false, Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun EventMapTag(label: String) {
    Surface(
        color = Color(0xFFEFFCFB),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp
    ) {
        Text(
            label,
            color = Color(0xFF19B6B6),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 15.sp
        )
    }
}

@Composable
fun EventCard(event: Event) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { /* Ir a detalles */ },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFEFFCFB),
            modifier = Modifier.size(56.dp)
        ) {
            Image(
                painter = painterResource(event.imageRes),
                contentDescription = event.title,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(event.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(event.distanceText, color = Color.Gray, fontSize = 13.sp)
                Spacer(Modifier.width(8.dp))
                Surface(
                    color = Color(0xFFB9F7F2), shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        event.infoText,
                        color = Color(0xFF19B6B6), fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationButton(label: String, selected: Boolean, modifier: Modifier = Modifier) {
    Surface(
        color = if (selected) Color(0xFF19B6B6) else Color(0xFFEFFCFB),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.padding(8.dp)
    ) {
        Text(
            label,
            modifier = Modifier.padding(vertical = 12.dp),
            color = if (selected) Color.White else Color(0xFF19B6B6),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp
        )
    }
}
