package com.cibertec.mangahub.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cibertec.mangahub.model.Manga;

public interface MangaRepository extends JpaRepository<Manga, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Manga m WHERE m.idManga = :id")
    java.util.Optional<Manga> buscarParaVenta(@Param("id") Integer id);


	List<Manga> findAllByOrderByIdMangaDesc();

	@Query("""
			SELECT m FROM Manga m
			WHERE (:idEditorial IS NULL OR m.editorial.idEditorial = :idEditorial)
			  AND (:idGenero IS NULL OR m.genero.idGenero = :idGenero)
			  AND (:idProveedor IS NULL OR m.proveedor.idProveedor = :idProveedor)
			""")
	Page<Manga> findByFilters(@Param("idEditorial") Integer idEditorial, @Param("idGenero") Integer idGenero,
			@Param("idProveedor") Integer idProveedor, Pageable pageable);
}
