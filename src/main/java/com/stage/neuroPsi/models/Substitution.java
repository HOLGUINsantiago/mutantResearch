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
public class Substitution extends Sequence {
    private int pos_substitution;

    private char mutate;

    public Substitution(int debut, int fin, String chromosome, String nom) {
        super(debut, fin, chromosome, nom);
    }

    public Substitution(int debut, int fin, String chromosome, String nom, int pos_substitution, char mutate,
            Set<Gene> genes) {
        super(debut, fin, chromosome, nom, genes);
        this.pos_substitution = pos_substitution;
        this.mutate = mutate;
    }

    public String autourSubst(int longeur) {
        return sequenceEntiere.substring(Math.round(pos_substitution - debut - longeur / 2),
                Math.round(pos_substitution - debut + longeur / 2));
    }

    @Override
    public void motifs(int longeurRead) {
        List<String> response = new ArrayList<>();
        Random random = new Random();

        String s1 = autourSubst(longeurRead * 2 - 10);
        for (int i = 0; i < 6; i++) {
            int rand;
            rand = random.nextInt(longeurRead - 5 + 1);

            response.add(s1.substring(rand, rand + 5));
        }
        motifs = response;
        response = response.stream().map(str -> new StringBuilder(str).reverse().toString()).toList();
        motifs.addAll(response);
    }

    @Override
    public void listeRef(int length) {
        sequenceMutated(length);
        listeRef = List.of(autourSubst(length), sequenceMutated);
    }

    @Override
    public void mostifsStricted() {
        String seq = autourSubst(10);
        char[] tab = seq.toCharArray();
        tab[5 - 1] = mutate;
        mostifsStricted = new ArrayList<>();
        mostifsStricted.add(autourSubst(10));
        mostifsStricted.add(new String(tab));
        mostifsStricted.add(autourSubst(10));
        mostifsStricted.addAll(mostifsStricted
                .stream()
                .map(str -> new StringBuilder(str).reverse().toString())
                .map(str -> {
                    StringBuilder complementary = new StringBuilder();
                    for (char base : str.toCharArray()) {
                        switch (base) {
                            case 'A':
                                complementary.append('T');
                                break;
                            case 'T':
                                complementary.append('A');
                                break;
                            case 'C':
                                complementary.append('G');
                                break;
                            case 'G':
                                complementary.append('C');
                                break;
                            default:
                                complementary.append('N');
                        }
                    }
                    return complementary.toString();
                })
                .toList());
    }

    @Override
    public void sequenceMutated(int length) {
        String seq = autourSubst(length);
        char[] tab = seq.toCharArray();
        tab[length / 2 - 1] = mutate;
        sequenceMutated = new String(tab);
    }
}
