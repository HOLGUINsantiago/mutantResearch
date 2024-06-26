package com.stage.neuroPsi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stage.neuroPsi.models.Gene;

public interface GeneRepository extends JpaRepository<Gene, String> {
}
