package com.mpaz.panol.img.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mpaz.panol.img.entities.ImagenEquipo;

public interface ImagenRepository extends JpaRepository<ImagenEquipo, Long> {
}
