package com.stage.neuroPsi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stage.neuroPsi.models.Sequence;

public interface SequenceRepository extends JpaRepository<Sequence, String> {
}
