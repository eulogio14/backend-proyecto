package com.example.proyectobackend.mapper;

import com.example.proyectobackend.dto.*;
import com.example.proyectobackend.model.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoMapper {

    public UsuarioResponseDto toUsuarioResponseDto(Usuario u) {
        if (u == null) return null;
        return UsuarioResponseDto.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .email(u.getEmail())
                .rol(u.getRol())
                .modoActual(u.getModoActual())
                .nombreEx(u.getNombreEx())
                .apodoEx(u.getApodoEx())
                .fotoPerfilEx(u.getFotoPerfilEx())
                .estadoWhatsAppEx(u.getEstadoWhatsAppEx())
                .personalidadEx(u.getPersonalidadEx())
                .fechaRuptura(u.getFechaRuptura())
                .fechaInicioContactoCero(u.getFechaInicioContactoCero())
                .diasRachaContactoCero(u.getDiasRachaContactoCero())
                .rachaMaximaContactoCero(u.getRachaMaximaContactoCero())
                .fechaRegistro(u.getFechaRegistro())
                .build();
    }

    public MensajeResponseDto toMensajeResponseDto(Mensaje m) {
        if (m == null) return null;
        return MensajeResponseDto.builder()
                .id(m.getId())
                .contenido(m.getContenido())
                .remitente(m.getRemitente())
                .estado(m.getEstado())
                .fechaEnvio(m.getTimestamp())
                .build();
    }

    public ChatResponseDto toChatResponseDto(Chat c, List<Mensaje> mensajes) {
        if (c == null) return null;
        List<MensajeResponseDto> mensajesDto = (mensajes == null)
                ? Collections.emptyList()
                : mensajes.stream().map(this::toMensajeResponseDto).collect(Collectors.toList());

        return ChatResponseDto.builder()
                .id(c.getId())
                .tipo(c.getTipoChat())
                .fechaCreacion(c.getFechaCreacion())
                .mensajes(mensajesDto)
                .build();
    }

    public DiarioResponseDto toDiarioResponseDto(DiarioEntrada d) {
        if (d == null) return null;
        return DiarioResponseDto.builder()
                .id(d.getId())
                .titulo(d.getTitulo())
                .contenido(d.getContenido())
                .estadoAnimo(d.getEstadoAnimo())
                .fechaCreacion(d.getFecha())
                .build();
    }

    public RecaidaResponseDto toRecaidaResponseDto(ContactoCeroRecaida r) {
        if (r == null) return null;
        return RecaidaResponseDto.builder()
                .id(r.getId())
                .motivo(r.getMotivo())
                .fechaRecaida(r.getFechaRecaida())
                .build();
    }

    public RedFlagResponseDto toRedFlagResponseDto(RedFlag rf) {
        if (rf == null) return null;
        return RedFlagResponseDto.builder()
                .id(rf.getId())
                .titulo(rf.getTitulo())
                .descripcion(rf.getDescripcion())
                .gravedad(rf.getGravedad())
                .fechaCreacion(rf.getFechaRegistro())
                .build();
    }

    public RecuerdoResponseDto toRecuerdoResponseDto(Recuerdo r) {
        if (r == null) return null;
        return RecuerdoResponseDto.builder()
                .id(r.getId())
                .titulo(r.getTitulo())
                .descripcion(r.getDescripcion())
                .urlFoto(r.getUrlFoto())
                .fechaRecuerdo(r.getFechaRecuerdo())
                .nivelNostalgia(r.getNivelNostalgia())
                .tipo(r.getTipo())
                .fechaCreacion(r.getFechaCreacion())
                .build();
    }
}
