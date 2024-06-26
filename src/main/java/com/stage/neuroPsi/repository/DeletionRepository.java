package com.stage.neuroPsi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stage.neuroPsi.models.Deletion;

public interface DeletionRepository extends JpaRepository<Deletion, String> {
}
