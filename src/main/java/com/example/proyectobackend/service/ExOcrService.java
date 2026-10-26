package com.example.proyectobackend.service;

import com.example.proyectobackend.dto.OcrDatasetResponseDto;
import com.example.proyectobackend.exception.BadRequestException;
import com.example.proyectobackend.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * Cliente HTTP del microservicio Python que hace OCR sobre las capturas de
 * WhatsApp. El backend nunca deja que el navegador hable directo con Python:
 * recibe las imágenes, las reenvía junto al id del usuario y guarda el texto
 * que devuelve el OCR.
 */
@Slf4j
@Service
public class ExOcrService {

    private static final int MAX_IMAGENES = 25;
    private static final long MAX_BYTES = 15L * 1024 * 1024;

    @Value("${ocr.service.url:http://localhost:8001}")
    private String ocrServiceUrl;

    private final RestClient restClient;

    public ExOcrService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofMinutes(3));
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    public OcrDatasetResponseDto procesar(Long usuarioId, String nombreContacto,
                                          String datasetActual,
                                          List<MultipartFile> imagenes) {
        validarImagenes(imagenes);

        // Usamos un MultiValueMap normal en vez de MultipartBodyBuilder para no
        // arrastrar la dependencia reactiva (reactive-streams) al proyecto MVC.
        MultiValueMap<String, Object> cuerpo = new LinkedMultiValueMap<>();
        cuerpo.add("usuario_id", String.valueOf(usuarioId));
        cuerpo.add("nombre_contacto",
                (nombreContacto == null || nombreContacto.isBlank()) ? "Ex" : nombreContacto);
        cuerpo.add("dataset_actual", datasetActual == null ? "" : datasetActual);

        for (MultipartFile imagen : imagenes) {
            String nombre = (imagen.getOriginalFilename() == null
                    || imagen.getOriginalFilename().isBlank())
                    ? "captura.jpg"
                    : imagen.getOriginalFilename();

            byte[] contenido;
            try {
                contenido = imagen.getBytes();
            } catch (IOException e) {
                throw new BadRequestException("No se pudo leer la imagen " + nombre);
            }

            ByteArrayResource recurso = new ByteArrayResource(contenido) {
                @Override
                public String getFilename() {
                    return nombre;
                }
            };

            cuerpo.add("imagenes", recurso);
        }

        String url = ocrServiceUrl.replaceAll("/+$", "") + "/ocr";

        try {
            OcrDatasetResponseDto respuesta = restClient.post()
                    .uri(url)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(cuerpo)
                    .retrieve()
                    .body(OcrDatasetResponseDto.class);

            if (respuesta == null) {
                throw new ExternalServiceException(
                        "El servicio OCR no devolvió una respuesta válida");
            }
            if (respuesta.getTexto() == null || respuesta.getTexto().isBlank()) {
                throw new ExternalServiceException(
                        "El servicio OCR no detectó texto en las capturas enviadas");
            }

            log.info("OCR completado para usuario {}: {} imágenes, {} mensajes nuevos, {} duplicados",
                    usuarioId, respuesta.getImagenesProcesadas(),
                    respuesta.getNuevos(), respuesta.getDuplicados());
            return respuesta;

        } catch (RestClientResponseException e) {
            log.warn("El servicio OCR respondió {}: {}", e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new ExternalServiceException(
                    "El servicio OCR respondió con error " + e.getStatusCode().value());
        } catch (RestClientException e) {
            log.warn("No se pudo conectar al servicio OCR en {}: {}", url, e.getMessage());
            throw new ExternalServiceException(
                    "No se pudo conectar con el servicio OCR. "
                            + "Verifica que proyect-script/server.py esté corriendo en "
                            + ocrServiceUrl);
        }
    }

    private void validarImagenes(List<MultipartFile> imagenes) {
        if (imagenes == null || imagenes.isEmpty()) {
            throw new BadRequestException("Debes subir al menos una imagen");
        }
        if (imagenes.size() > MAX_IMAGENES) {
            throw new BadRequestException(
                    "Puedes subir como máximo " + MAX_IMAGENES + " imágenes por vez");
        }
        for (MultipartFile imagen : imagenes) {
            if (imagen.isEmpty()) {
                throw new BadRequestException("Una de las imágenes está vacía");
            }
            if (imagen.getSize() > MAX_BYTES) {
                throw new BadRequestException(
                        "La imagen " + imagen.getOriginalFilename()
                                + " supera el límite de 15 MB");
            }
        }
    }

}
