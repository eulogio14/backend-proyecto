package com.example.proyectobackend.service;

import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.repository.UsuarioRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository; // El repositorio de tu entidad Usuario

    private static final String GOOGLE_CLIENT_ID = "962526435206-8gnivbn68h8ntc10e9k6epaoe68h0o42.apps.googleusercontent.com";

    @Transactional
    public Usuario verificarYLoguearConGoogle(String tokenRecibido) throws Exception {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                .build();

        GoogleIdToken idToken = verifier.verify(tokenRecibido);

        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String nombre = (String) payload.get("name");

            Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(email);

            if (usuarioExistente.isPresent()) {
                return usuarioExistente.get();
            } else {
                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setEmail(email);
                nuevoUsuario.setNombre(nombre);
                nuevoUsuario.setPassword("GOOGLE_AUTH");
                return usuarioRepository.save(nuevoUsuario);
            }
        } else {
            throw new RuntimeException("El token de Google es inválido o ha expirado.");
        }
    }
}
