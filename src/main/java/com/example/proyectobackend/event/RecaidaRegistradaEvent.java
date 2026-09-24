package com.example.proyectobackend.event;

import com.example.proyectobackend.model.ContactoCeroRecaida;
import com.example.proyectobackend.model.Usuario;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RecaidaRegistradaEvent extends ApplicationEvent {

    private final Usuario usuario;
    private final ContactoCeroRecaida recaida;

    public RecaidaRegistradaEvent(Object source, Usuario usuario, ContactoCeroRecaida recaida) {
        super(source);
        this.usuario = usuario;
        this.recaida = recaida;
    }
}
