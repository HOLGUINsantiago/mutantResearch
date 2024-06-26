package com.stage.neuroPsi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stage.neuroPsi.models.Line;

public interface LineRepository extends JpaRepository<Line, String> {
}
