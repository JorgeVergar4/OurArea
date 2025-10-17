package cl.duoc.ourarea.ui.theme

fun getAddressFromEvent(title: String): String {
    return when {
        title.contains("Santa Lucía") -> "Centro • Alameda 499"
        title.contains("Noche de Museos") -> "Providencia • Parque Forestal s/n"
        title.contains("Festival de Cine UC") -> "Centro • Alameda 390"
        title.contains("Bellas Artes") -> "Centro • Parque Forestal s/n"
        title.contains("Vega Central") -> "Recoleta • Artesanos 1001"
        title.contains("Patrimonios") -> "Centro • Plaza de la Ciudadanía"
        title.contains("Hoyts") -> "Las Condes • Av. Kennedy 5413"
        title.contains("Fútbol") -> "La Florida • Vicuña Mackenna 7351"
        title.contains("Vegana") -> "Providencia • Av. Providencia 1234"
        title.contains("Arte Contemporáneo") -> "Centro • Merced 349"
        else -> "Santiago Centro"
    }
}

fun getScheduleFromEvent(title: String): String {
    return when {
        title.contains("Santa Lucía") -> "Lunes a Domingo, 10:00 - 19:00"
        title.contains("Noche de Museos") -> "Sábado, 19:00 - 01:00"
        title.contains("Festival de Cine") -> "Del 16 al 20 de Oct, 14:00 - 23:00"
        title.contains("Bellas Artes") -> "Martes a Domingo, 10:00 - 18:45"
        title.contains("Vega Central") -> "Lunes a Domingo, 06:00 - 17:00"
        title.contains("Patrimonios") -> "Sábado y Domingo, 10:00 - 20:00"
        title.contains("Hoyts") -> "Hoy, desde las 11:00"
        title.contains("Fútbol") -> "Este fin de semana, 09:00 - 18:00"
        title.contains("Vegana") -> "Hoy, 11:00 - 20:00"
        title.contains("Arte Contemporáneo") -> "Este fin, 12:00 - 21:00"
        else -> "Hoy, 10:00 - 22:00"
    }
}

fun getOrganizerFromEvent(title: String): String {
    return when {
        title.contains("Santa Lucía") -> "Artesanos de Chile"
        title.contains("Noche de Museos") -> "Ministerio de las Culturas"
        title.contains("Festival de Cine") -> "Pontificia Universidad Católica"
        title.contains("Bellas Artes") -> "MNBA Chile"
        title.contains("Vega Central") -> "Asociación de Locatarios"
        title.contains("Patrimonios") -> "Consejo de Monumentos"
        title.contains("Hoyts") -> "Cinemark Chile"
        title.contains("Fútbol") -> "Club Deportivo Local"
        title.contains("Vegana") -> "Vegan Fest Chile"
        title.contains("Arte Contemporáneo") -> "Galería MAC"
        else -> "Gourmet City"
    }
}
