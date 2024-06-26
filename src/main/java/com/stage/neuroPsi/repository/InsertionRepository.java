package com.stage.neuroPsi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stage.neuroPsi.models.Insertion;

public interface InsertionRepository extends JpaRepository<Insertion, String> {
}
