package com.cibertec.mangahub.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cibertec.mangahub.dto.*;
import com.cibertec.mangahub.model.*;
import com.cibertec.mangahub.repository.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VentaService {
    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MangaRepository mangaRepository;

    @Transactional(readOnly = true)
    public List<Venta> listar() {
        return ventaRepository.findAll(org.springframework.data.domain.Sort.by("idVenta").descending());
    }
    @Transactional(readOnly = true)
    public Venta buscar(Integer id) {
        return ventaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Boleta no encontrada"));
    }
    @Transactional
    public void eliminar(Integer id) {
        Venta venta = ventaRepository.buscarParaEliminar(id)
            .orElseThrow(() -> new IllegalArgumentException("La boleta ya no existe"));
        // Mismo orden de bloqueo que el registro para evitar interbloqueos.
        Map<Integer, Integer> cantidades = new TreeMap<>();
        for (DetalleVenta d : venta.getDetalles())
            cantidades.merge(d.getManga().getIdManga(), d.getCantidad(), Math::addExact);
        for (var item : cantidades.entrySet()) {
            Manga manga = mangaRepository.buscarParaVenta(item.getKey())
                .orElseThrow(() -> new IllegalArgumentException("Manga no encontrado"));
            try { manga.setStock(Math.addExact(manga.getStock(), item.getValue())); }
            catch (ArithmeticException e) { throw new IllegalArgumentException("El stock supera el límite permitido"); }
        }
        // CascadeType.ALL elimina los detalles junto a su cabecera.
        ventaRepository.delete(venta);
    }

    @Transactional
    public Venta registrar(VentaDTO dto, Integer idUsuario) {
        if (idUsuario == null) throw new IllegalArgumentException("Inicie sesión nuevamente");
        Usuario usuario = usuarioRepository.findById(idUsuario)
            .filter(u -> Boolean.TRUE.equals(u.getActivo()))
            .orElseThrow(() -> new IllegalArgumentException("Usuario no válido"));
        if (dto.getIdCliente() == null) throw new IllegalArgumentException("Seleccione un cliente");
        Cliente cliente = clienteRepository.findById(dto.getIdCliente())
            .filter(c -> Boolean.TRUE.equals(c.getActivo()))
            .orElseThrow(() -> new IllegalArgumentException("Seleccione un cliente activo"));
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty() || dto.getDetalles().size() > 100)
            throw new IllegalArgumentException("Agregue entre 1 y 100 líneas de mangas");
        // Orden estable de bloqueo; las líneas repetidas se acumulan antes de validar stock.
        Map<Integer, Integer> cantidades = new TreeMap<>();
        for (DetalleVentaDTO d : dto.getDetalles()) {
            if (d == null || d.getIdManga() == null || d.getCantidad() == null || d.getCantidad() <= 0)
                throw new IllegalArgumentException("Seleccione un manga y una cantidad positiva en cada línea");
            try { cantidades.merge(d.getIdManga(), d.getCantidad(), Math::addExact); }
            catch (ArithmeticException e) { throw new IllegalArgumentException("Cantidad demasiado grande"); }
        }
        Venta venta = new Venta();
        venta.setCliente(cliente); venta.setUsuario(usuario);
        venta.setFechaEmision(LocalDateTime.now()); venta.setActivo(true);
        List<DetalleVenta> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (var item : cantidades.entrySet()) {
            Manga manga = mangaRepository.buscarParaVenta(item.getKey())
                .orElseThrow(() -> new IllegalArgumentException("Manga no encontrado"));
            if (!Boolean.TRUE.equals(manga.getActivo())) throw new IllegalArgumentException("Manga inactivo: " + manga.getTitulo());
            if (manga.getStock() == null || manga.getStock() < item.getValue())
                throw new IllegalArgumentException("Stock insuficiente: " + manga.getTitulo());
            if (manga.getPrecio() == null || manga.getPrecio().signum() < 0)
                throw new IllegalArgumentException("Precio no válido: " + manga.getTitulo());
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta); detalle.setManga(manga); detalle.setCantidad(item.getValue());
            detalle.setPrecioUnitario(manga.getPrecio());
            detalle.setSubtotal(manga.getPrecio().multiply(BigDecimal.valueOf(item.getValue())));
            total = total.add(detalle.getSubtotal()); detalles.add(detalle);
            manga.setStock(manga.getStock() - item.getValue());
        }
        if (total.compareTo(new BigDecimal("99999999.99")) > 0)
            throw new IllegalArgumentException("El total supera el límite permitido");
        venta.setDetalles(detalles); venta.setMontoTotal(total);
        return ventaRepository.save(venta);
    }
}
