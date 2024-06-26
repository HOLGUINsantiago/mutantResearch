package com.stage.neuroPsi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stage.neuroPsi.models.Line;
import com.stage.neuroPsi.repository.LineRepository;

@Service
public class LineService {
    @Autowired
    private LineRepository lineRepository;

    public Line addLine(Line line) {
        return lineRepository.save(line);
    }

    public Line setLine(Line line) {
        return lineRepository.save(line);
    }

    public List<Line> getAllLines() {
        return lineRepository.findAll();
    }

    public void delete(Line line) {
        lineRepository.delete(line);
    }

    public Line getById(String id) {
        return lineRepository.findById(id).orElse(null);
    }

}
