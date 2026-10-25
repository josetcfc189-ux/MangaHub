package com.cibertec.mangahub.dto;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class VentaDTO {
    private Integer idCliente;
    private List<DetalleVentaDTO> detalles = new ArrayList<>();
}
