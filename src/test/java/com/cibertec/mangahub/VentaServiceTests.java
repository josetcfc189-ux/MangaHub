package com.cibertec.mangahub;

import com.cibertec.mangahub.dto.*;
import com.cibertec.mangahub.model.*;
import com.cibertec.mangahub.repository.*;
import com.cibertec.mangahub.service.VentaService;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VentaServiceTests {
    VentaRepository ventas = mock(VentaRepository.class);
    ClienteRepository clientes = mock(ClienteRepository.class);
    UsuarioRepository usuarios = mock(UsuarioRepository.class);
    MangaRepository mangas = mock(MangaRepository.class);
    VentaService service = new VentaService(ventas, clientes, usuarios, mangas);
    Manga manga;
    @BeforeEach void preparar() {
        Usuario usuario = new Usuario(); usuario.setActivo(true);
        Cliente cliente = new Cliente(); cliente.setActivo(true);
        manga = new Manga(); manga.setActivo(true); manga.setStock(5);
        manga.setPrecio(new BigDecimal("12.50")); manga.setTitulo("Manga de prueba");
        when(usuarios.findById(1)).thenReturn(Optional.of(usuario));
        when(clientes.findById(1)).thenReturn(Optional.of(cliente));
        when(mangas.buscarParaVenta(1)).thenReturn(Optional.of(manga));
        when(ventas.save(any(Venta.class))).thenAnswer(i -> i.getArgument(0));
    }
    VentaDTO solicitud(int... cantidades) {
        VentaDTO dto = new VentaDTO(); dto.setIdCliente(1);
        for (int n : cantidades) {
            DetalleVentaDTO d = new DetalleVentaDTO(); d.setIdManga(1); d.setCantidad(n);
            dto.getDetalles().add(d);
        }
        return dto;
    }
    @Test void acumulaDuplicadosYCalculaConPrecioDelCatalogo() {
        Venta venta = service.registrar(solicitud(1, 2), 1);
        assertEquals(new BigDecimal("37.50"), venta.getMontoTotal());
        assertEquals(2, manga.getStock());
        assertEquals(1, venta.getDetalles().size());
        assertSame(venta, venta.getDetalles().get(0).getVenta());
        assertEquals(3, venta.getDetalles().get(0).getCantidad());
    }
    @Test void rechazaDuplicadosQueSuperanStock() {
        assertThrows(IllegalArgumentException.class, () -> service.registrar(solicitud(3, 3), 1));
        verify(ventas, never()).save(any()); assertEquals(5, manga.getStock());
    }
    @Test void rechazaCantidadCero() {
        assertThrows(IllegalArgumentException.class, () -> service.registrar(solicitud(0), 1));
        verify(ventas, never()).save(any());
    }
    @Test void rechazaClienteInactivo() {
        Cliente inactivo = new Cliente(); inactivo.setActivo(false);
        when(clientes.findById(1)).thenReturn(Optional.of(inactivo));
        assertThrows(IllegalArgumentException.class, () -> service.registrar(solicitud(1), 1));
        verify(ventas, never()).save(any());
    }
    @Test void rechazaSinUsuario() {
        assertThrows(IllegalArgumentException.class, () -> service.registrar(solicitud(1), null));
        verify(ventas, never()).save(any());
    }
    @Test void eliminarDevuelveStockYBorraVenta() {
        manga.setIdManga(1);
        Venta venta = new Venta();
        DetalleVenta detalle = new DetalleVenta();
        detalle.setManga(manga); detalle.setCantidad(2);
        venta.setDetalles(List.of(detalle));
        when(ventas.buscarParaEliminar(7)).thenReturn(Optional.of(venta));
        service.eliminar(7);
        assertEquals(7, manga.getStock());
        verify(ventas).delete(venta);
    }
    @Test void eliminarInexistenteNoModificaStock() {
        when(ventas.buscarParaEliminar(7)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.eliminar(7));
        assertEquals(5, manga.getStock());
        verify(ventas, never()).delete(any());
    }
}
