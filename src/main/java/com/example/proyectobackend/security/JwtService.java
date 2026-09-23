package com.example.proyectobackend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.proyectobackend.exception.UnauthorizedException;
import com.example.proyectobackend.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret:SuperSecretRickPrimeDimensionalKeyOmegaDevice999999}")
    private String secretKey;

    @Value("${jwt.access-token-expiration-ms:86400000}") // 24 horas por defecto
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms:604800000}") // 7 días por defecto
    private long refreshTokenExpirationMs;

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secretKey);
    }

    public String generarAccessToken(Usuario usuario) {
        return JWT.create()
                .withSubject(usuario.getEmail())
                .withClaim("userId", usuario.getId())
                .withClaim("email", usuario.getEmail())
                .withClaim("nombre", usuario.getNombre())
                .withClaim("role", usuario.getRol() != null ? usuario.getRol().name() : "ROLE_USER")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .sign(getAlgorithm());
    }

    public String generarRefreshToken(Usuario usuario) {
        return JWT.create()
                .withSubject(usuario.getEmail())
                .withClaim("userId", usuario.getId())
                .withClaim("type", "REFRESH")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .sign(getAlgorithm());
    }

    public DecodedJWT validarYDecodificarToken(String token) {
        try {
            return JWT.require(getAlgorithm())
                    .build()
                    .verify(token);
        } catch (TokenExpiredException e) {
            throw new com.example.proyectobackend.exception.TokenExpiredException("El token JWT ha expirado");
        } catch (JWTVerificationException e) {
            throw new UnauthorizedException("Firma o token JWT inválido");
        }
    }

    public String extraerEmail(String token) {
        return validarYDecodificarToken(token).getSubject();
    }

    public Long extraerUserId(String token) {
        return validarYDecodificarToken(token).getClaim("userId").asLong();
    }

    public String extraerRol(String token) {
        return validarYDecodificarToken(token).getClaim("role").asString();
    }
}
