package com.stage.neuroPsi.models;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MutationsToAlign {
    private List<Sequence> sequences = new ArrayList<>();

    private Map<Sequence, Integer> compteurs_mismatchs = new HashMap<>();
    private Map<Sequence, Integer> compteur_matchs = new HashMap<>();

    private String fichiersTempPath;

    private Integer tailleRefSeq;

    private File results;
    private File mutations;
    private File stricted;
    private File motifs;

    public MutationsToAlign(List<Sequence> sequences, String temp, int tailleRef) {
        fichiersTempPath = temp;
        tailleRefSeq = tailleRef;
        this.sequences = sequences;
        sequences.forEach(seq -> {
            compteur_matchs.put(seq, 0);
            compteurs_mismatchs.put(seq, 0);
        });
        initialize();
    }

    public MutationsToAlign(Sequence sequence, String temp, int tailleRef) {
        fichiersTempPath = temp;
        tailleRefSeq = tailleRef;
        this.sequences = List.of(sequence);
        compteur_matchs.put(sequence, 0);
        compteurs_mismatchs.put(sequence, 0);
        initialize();
    }

    public void addMatch(Sequence seq) {
        compteur_matchs.put(seq, compteur_matchs.get(seq) + 1);
    }

    public void addMismatch(Sequence seq) {
        compteurs_mismatchs.put(seq, compteurs_mismatchs.get(seq) + 1);
    }

    public double getMatchProportion(Sequence seq) {
        if (compteur_matchs.containsKey(seq) && compteurs_mismatchs.containsKey(seq)) {
            int matches = compteur_matchs.get(seq);
            int mismatches = compteurs_mismatchs.get(seq);
            return mismatches == 0 ? matches : (double) matches / (matches + mismatches);
        }
        return 0.0;
    }

    public Map<Sequence, Double> getAllMatchProportion() {
        return sequences.stream()
                .collect(Collectors.toMap(seq -> seq, this::getMatchProportion));
    }

    public List<String> getAllMotifs() {
        return sequences.stream()
                .flatMap(seq -> seq.getMotifs().stream())
                .collect(Collectors.toList());
    }

    public List<String> getAllMotifsStricted() {
        return sequences.stream()
                .flatMap(seq -> seq.getMostifsStricted().stream())
                .collect(Collectors.toList());
    }

    public void initialize() {
        sequences.forEach(seq -> seq.allInitialize(tailleRefSeq));

        results = new File(fichiersTempPath, "results.tsv");
        mutations = new File(fichiersTempPath, "mutations.fasta");
        stricted = new File(fichiersTempPath, "stricted.fasta");
        motifs = new File(fichiersTempPath, "motifs.fasta");

        try {
            if (!mutations.exists())
                mutations.createNewFile();
            if (!motifs.exists())
                motifs.createNewFile();

            if (!stricted.exists())
                stricted.createNewFile();

            if (!results.exists())
                results.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileWriter writer = new FileWriter(mutations);
                BufferedWriter bw = new BufferedWriter(writer);

                FileWriter writer2 = new FileWriter(motifs);
                BufferedWriter bwm = new BufferedWriter(writer2);

                FileWriter writer3 = new FileWriter(stricted);
                BufferedWriter bws = new BufferedWriter(writer3)) {

            int compt = 1;

            for (Sequence seq : sequences) {
                for (String ref : seq.getListeRef()) {
                    String ligne = ">";
                    if (seq instanceof Deletion) {
                        ligne += seq.nom + (compt == 3 ? "_liaison" : "_wt_" + compt) + "\n";
                    } else if (seq instanceof Substitution) {
                        ligne += seq.nom + (compt == 2 ? "_mutated" : "_wt") + "\n";
                    } else if (seq instanceof Insertion) {
                        ligne += seq.nom + (compt == 1 ? "_wt" : "_mutated") + "\n";
                    } else {
                        ligne += seq.nom + (compt == 1 ? "_wt" : "_mutated") + "\n";
                    }
                    ligne += ref + "\n \n";

                    bw.write(ligne);
                    compt++;
                }
                compt = 1;
                for (String motif : seq.getMotifs())
                    bwm.write(motif + "\n");

                for (String motif : seq.getMostifsStricted())
                    bws.write(motif + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetCounters() {
        compteur_matchs.replaceAll((seq, count) -> 0);
        compteurs_mismatchs.replaceAll((seq, count) -> 0);
    }

    public Sequence findByName(String nom) {
        return sequences.stream().filter(seq -> seq.getNom().equals(nom)).findFirst().get();
    }
}
