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
public class Insertion extends Sequence {
    private int pos_insertion;

    private String inserted;

    public Insertion(int debut, int fin, String chromosome, String nom) {
        super(debut, fin, chromosome, nom);
    }

    public Insertion(int debut, int fin, String chromosome, String nom, int pos_insertion,
            String inserted) {
        super(debut, fin, chromosome, nom);
        this.pos_insertion = pos_insertion;
        this.inserted = inserted;
    }

    public String autourInser(int longeur) {
        return sequenceEntiere.substring(Math.round(pos_insertion - debut - (longeur - inserted.length()) / 2),
                pos_insertion - debut)
                + inserted + sequenceEntiere.substring(pos_insertion - debut,
                        Math.round(pos_insertion - debut + (longeur - inserted.length()) / 2));
    }

    public String autourLiaison(int longeur) {
        return sequenceEntiere.substring(Math.round(pos_insertion - debut - longeur / 2),
                Math.round(pos_insertion - debut + longeur / 2));
    }

    @Override
    public void motifs(int longeurRead) {
        List<String> response = new ArrayList<>();
        Random random = new Random();

        String s1 = autourInser(longeurRead);
        String s2 = autourLiaison(longeurRead);

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
        listeRef = List.of(autourInser(length), autourLiaison(length));
    }

    @Override
    public void mostifsStricted() {
        mostifsStricted = List.of(autourInser(10), autourLiaison(10));
        mostifsStricted.addAll(List.of(autourInser(10), autourLiaison(10))
                .stream()
                .map(str -> new StringBuilder(str).reverse().toString()).toList());
    }

    @Override
    public void sequenceMutated(int longeur) {
        sequenceMutated = autourInser(longeur);
    }
}
