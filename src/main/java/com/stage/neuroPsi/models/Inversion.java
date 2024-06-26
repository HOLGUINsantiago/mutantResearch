package com.stage.neuroPsi.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.*;

import jakarta.persistence.Entity;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Inversion extends Sequence {
    private int debut_inversion;

    private int fin_inversion;

    private String inverted;

    public Inversion(int debut, int fin, String chromosome, String nom) {
        super(debut, fin, chromosome, nom);
    }

    public Inversion(int debut, int fin, String chromosome, String nom, int debut_inversion, int fin_inversion,
            String inverted) {
        super(debut, fin, chromosome, nom);
        this.debut_inversion = debut_inversion;
        this.fin_inversion = fin_inversion;
        this.inverted = inverted;
    }

    public String autourInversion(int longeur) {
        return sequenceEntiere.substring(Math.round(debut_inversion - debut - longeur / 2),
                debut_inversion - debut)
                + inverted + sequenceEntiere.substring(debut_inversion - debut,
                        Math.round(debut_inversion - debut + (longeur - inverted.length()) / 2));
    }

    public String autourWt(int longeur) {
        return sequenceEntiere.substring(debut_inversion - debut - longeur / 2 + inverted.length() / 2,
                debut_inversion - debut + longeur / 2 - inverted.length() / 2);
    }

    @Override
    public void motifs(int longeurRead) {
        List<String> response = new ArrayList<>();
        Random random = new Random();

        String s1 = autourInversion(longeurRead);
        String s2 = autourWt(longeurRead);

        for (int i = 0; i < 1; i++) {
            int rand;
            if (i == 0) {
                rand = random.nextInt((longeurRead / 2) - 5 + 1);
            } else {
                rand = random.nextInt((longeurRead - 5) - (longeurRead / 2) + 1) + longeurRead / 2;
            }
            response.add(s1.substring(rand, rand + 5));
        }
        for (int i = 0; i < 1; i++) {
            int rand;
            if (i == 0) {
                rand = random.nextInt((longeurRead / 2) - 5 + 1);
            } else {
                rand = random.nextInt((longeurRead - 5) - (longeurRead / 2) + 1) + longeurRead / 2;
            }
            response.add(s2.substring(rand, rand + 5));
        }
        for (int i = 0; i < 2; i++) {
            int rand;
            if (i == 0) {
                rand = random.nextInt((longeurRead / 2) - 5 + 1);
            } else {
                rand = random.nextInt((longeurRead - 5) - (longeurRead / 2) + 1) + longeurRead / 2;
            }
            response.add(s2.substring(rand, rand + 5));
        }
        motifs = response;
        response = response.stream().map(str -> new StringBuilder(str).reverse().toString()).toList();
        motifs.addAll(response);
    }

    @Override
    public void listeRef(int length) {
        listeRef = List.of(autourInversion(length), autourWt(length));
    }

    @Override
    public void mostifsStricted() {
        mostifsStricted = List.of(autourInversion(10), autourWt(10));
        mostifsStricted.addAll(List.of(autourInversion(10), autourWt(10))
                .stream()
                .map(str -> new StringBuilder(str).reverse().toString()).toList());
    }

    @Override
    public void sequenceMutated(int longeur) {
        sequenceMutated = autourInversion(longeur);
    }

}
