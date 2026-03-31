package edu.pe.vallegrande.AuthenticationService.infrastructure.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** Utilidad para manejo de fechas y zonas horarias */
public class DateTimeUtil {

    // Zona horaria de Perú
    public static final ZoneId PERU_ZONE = ZoneId.of("America/Lima");
    
    // Formateadores comunes
    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /** Obtiene la fecha/hora actual en zona horaria de Perú */
    public static LocalDateTime nowInPeru() {
        return LocalDateTime.now(PERU_ZONE);
    }

    /** Convierte LocalDateTime a ZonedDateTime en zona horaria de Perú */
    public static ZonedDateTime toZonedDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(PERU_ZONE);
    }

    /** Convierte ZonedDateTime a LocalDateTime */
    public static LocalDateTime toLocalDateTime(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return zonedDateTime.withZoneSameInstant(PERU_ZONE).toLocalDateTime();
    }

    /** Formatea LocalDateTime para mostrar al usuario */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DISPLAY_FORMATTER);
    }

    /** Formatea LocalDateTime en formato ISO */
    public static String formatISO(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(ISO_FORMATTER);
    }

    /** Parsea una fecha en formato ISO a LocalDateTime */
    public static LocalDateTime parseISO(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, ISO_FORMATTER);
    }

    /** Verifica si una fecha está en el pasado */
    public static boolean isPast(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isBefore(nowInPeru());
    }

    /** Verifica si una fecha está en el futuro */
    public static boolean isFuture(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isAfter(nowInPeru());
    }

    /** Calcula la diferencia en minutos entre dos fechas */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return java.time.Duration.between(start, end).toMinutes();
    }

    /** Calcula la diferencia en horas entre dos fechas */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return java.time.Duration.between(start, end).toHours();
    }

    /** Añade minutos a una fecha */
    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusMinutes(minutes);
    }

    /** Añade horas a una fecha */
    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(hours);
    }

    /** Añade días a una fecha */
    public static LocalDateTime addDays(LocalDateTime dateTime, long days) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusDays(days);
    }
}
