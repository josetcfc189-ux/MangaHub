package com.cibertec.mangahub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.cibertec.mangahub.model.Venta;

public interface VentaRepository extends JpaRepository<Venta, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT v FROM Venta v WHERE v.idVenta = :id")
    java.util.Optional<Venta> buscarParaEliminar(@org.springframework.data.repository.query.Param("id") Integer id);

}