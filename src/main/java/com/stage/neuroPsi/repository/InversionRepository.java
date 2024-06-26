package com.stage.neuroPsi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stage.neuroPsi.models.Inversion;

public interface InversionRepository extends JpaRepository<Inversion, String> {
}
