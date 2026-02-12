package com.example.CJLInvestimentos.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Deserializa JSON para LocalDateTime aceitando:
 * - "yyyy-MM-dd" (apenas data) → converte para início do dia
 * - "yyyy-MM-dd'T'HH:mm:ss" e ISO-8601 com timezone (ex: 2026-02-12T00:00:00.000Z)
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText().trim();
        if (value == null || value.isEmpty()) {
            return null;
        }
        // Apenas data (10 caracteres): 2026-02-12
        if (value.length() == 10 && value.charAt(4) == '-' && value.charAt(7) == '-') {
            try {
                LocalDate date = LocalDate.parse(value, DATE_ONLY);
                return date.atStartOfDay();
            } catch (DateTimeParseException e) {
                throw new IOException("Data inválida (use yyyy-MM-dd ou ISO date-time): " + value, e);
            }
        }
        // ISO date-time: pode vir com Z (UTC) ou sem
        try {
            if (value.endsWith("Z")) {
                return java.time.Instant.parse(value).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            }
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e2) {
                throw new IOException("Data/hora inválida: " + value, e2);
            }
        }
    }
}
