package models;

import java.util.Objects;

public record ServicioAfectado(String nombre, int usuariosAfectados) {
    public ServicioAfectado {
        Objects.requireNonNull(nombre, "nombre");
        if (nombre.isBlank() || usuariosAfectados < 0) {
            throw new IllegalArgumentException("Servicio inválido");
        }
    }
}
