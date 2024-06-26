package com.stage.neuroPsi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.stage.neuroPsi.models.*;
import com.stage.neuroPsi.repository.*;

import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Service
public class MutationsService {
    @Autowired
    private SequenceRepository sequenceRepository;

    @Autowired
    private MultipleMutationRepository multipleMutationRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private GeneRepository geneRepository;

    public List<Sequence> findAllMutations() {
        return sequenceRepository.findAll();
    }

    public Sequence saveMutation(Sequence sequence) {
        if (!sequence.getChromosome().startsWith("chr"))
            sequence.setChromosome("chr" + sequence.getChromosome());
        return sequenceRepository.save(sequence);
    }

    public boolean exists(String nom) {
        return sequenceRepository.existsById(nom);
    }

    public List<Sequence> getMutations(List<String> noms) {
        return sequenceRepository.findAllById(noms);
    }

    public Sequence getMutation(String nom) {
        return sequenceRepository.findById(nom).orElse(null);
    }

    public List<Sequence> saveMutation(List<Sequence> sequence) {
        return sequenceRepository.saveAll(sequence);
    }

    public void deleteMutation(String nom) {
        sequenceRepository.deleteById(nom);
    }

    public Gene saveGene(Gene gene) {
        return geneRepository.save(gene);
    }

    @Transactional
    public List<Gene> saveGenes(List<Gene> genes) {
        return geneRepository.saveAll(genes);
    }

    @Transactional
    public MultipleMutation saveMultipleMutation(MultipleMutation multipleMutation) {
        return multipleMutationRepository.save(multipleMutation);
    }

    public List<AlignResults> addResults(List<AlignResults> results) {
        return resultRepository.saveAll(results);
    }

    public Map<Sequence, Map<Line, ResultsRegrouper>> getResultsFull(int scoreMin, int scoreMinInteressed,
            boolean fullStricted, int tailleRefSeq) {

        Map<Sequence, Map<Line, ResultsRegrouper>> result = new HashMap<>();

        Map<Sequence, List<AlignResults>> groups = this.getAllResults().stream()
                .map(res -> {
                    result.put(res.getMutation(), new HashMap<>());
                    res.getMutation().allInitialize(tailleRefSeq);
                    return res;
                })
                .collect(Collectors.groupingBy(res -> res.getMutation()));

        for (Map.Entry<Sequence, List<AlignResults>> entry : groups.entrySet()) {
            for (Entry<Line, List<AlignResults>> entryLine : entry.getValue().stream()
                    .collect(Collectors.groupingBy(align -> align.getLine()))
                    .entrySet()) {
                result.get(entry.getKey()).put(entryLine.getKey(), new ResultsRegrouper(entryLine.getValue()));
            }
        }
        return result;
    }

    public Entry<Sequence, Map<Line, ResultsRegrouper>> result(Line line, Sequence mutation, int scoreMin,
            int scoreMinInteressed,
            boolean fullStricted, int tailleRefSeq) {
        Map<Line, ResultsRegrouper> results = new HashMap<>();
        for (Entry<Sequence, Map<Line, ResultsRegrouper>> a : getResultsFull(scoreMin, scoreMinInteressed, fullStricted,
                tailleRefSeq).entrySet()) {
            for (Entry<Line, ResultsRegrouper> entry : a.getValue().entrySet()) {
                if (a.getKey().getNom()
                        .equals(mutation.getNom()) && line.getLineId().equals(entry.getKey().getLineId()))
                    results.put(entry.getKey(), entry.getValue());

            }
        }
        return Map.of(mutation, results).entrySet().stream().findFirst().orElse(null);
    }

    @Transactional
    public List<AlignResults> getAllResults() {
        return resultRepository.findAll();
    }
}
