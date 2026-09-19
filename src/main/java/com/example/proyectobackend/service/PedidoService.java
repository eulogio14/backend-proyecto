package com.example.proyectobackend.service;

import com.example.proyectobackend.model.Lavadora;
import com.example.proyectobackend.model.Pedido;
import com.example.proyectobackend.model.Usuario;
import com.example.proyectobackend.repository.LavadoraRepository;
import com.example.proyectobackend.repository.PedidoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private LavadoraRepository lavadoraRepository;

    @Transactional
    public Pedido crearReserva(Long usuarioId, Long lavadoraId, String modoLavado, String fragancia) {

        // 1. Bloqueo optimista o verificación rápida
        Lavadora lavadora = lavadoraRepository.findById(lavadoraId)
                .orElseThrow(() -> new RuntimeException("Lavadora no encontrada"));

        if (!lavadora.getEstado().equals("LIBRE")) {
            throw new RuntimeException("¡La lavadora ya está ocupada!");
        }

        // 2. Cambiar estado inmediatamente para que nadie más la tome
        lavadora.setEstado("OCUPADA");
        lavadoraRepository.save(lavadora);

        // 3. Crear el pedido
        Pedido nuevoPedido = new Pedido();
        // (Aquí asumo que buscas al usuario por ID)
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        nuevoPedido.setUsuario(usuario);
        nuevoPedido.setLavadora(lavadora);
        nuevoPedido.setModoLavado(modoLavado);
        nuevoPedido.setFragancia(fragancia);
        nuevoPedido.setEstado("PENDIENTE");
        nuevoPedido.setFechaReserva(LocalDateTime.now());

        return pedidoRepository.save(nuevoPedido);
    }
}
