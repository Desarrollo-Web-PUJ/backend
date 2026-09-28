package com.example.backend.entity;

public enum TipoEventoMensaje {
    THROW,              // HU-25: lanza mensaje hacia otro pool
    CATCH_INICIO,       // HU-27: arranca el proceso al recibir
    CATCH_INTERMEDIO    // HU-27: deja el proceso en espera
}