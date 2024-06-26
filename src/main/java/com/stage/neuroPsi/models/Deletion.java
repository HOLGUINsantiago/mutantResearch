package com.stage.neuroPsi.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.*;

import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Deletion extends Sequence {
    private int debut_del;

    private int fin_del;

    public Deletion(int debut, int fin, String chromosome, String nom) {
        super(debut, fin, chromosome, nom);
    }

    public Deletion(int debut, int fin, String chromosome, String nom, int debut_del, int fin_del, Set<Gene> genes) {
        super(debut, fin, chromosome, nom, genes);
        this.debut_del = debut_del;
        this.fin_del = fin_del;
    }

    public String autourDel1(int longeur) {
        return sequenceEntiere.substring(Math.round(debut_del - debut - longeur / 2),
                Math.round(debut_del - debut + longeur / 2));
    }

    public String autourDel2(int longeur) {
        return sequenceEntiere.substring(Math.round(fin_del - debut - longeur / 2),
                Math.round(fin_del - debut + longeur / 2));
    }

    public String autourLiaison(int longeur) {
        return sequenceEntiere.substring(Math.round(debut_del - debut - longeur / 2),
                debut_del - debut)
                + sequenceEntiere.substring(fin_del - debut,
                        Math.round(fin_del - debut + longeur / 2));
    }

    public String autourCentre(int longeur) {
        int taille = fin_del - debut_del;
        return sequenceEntiere.substring(debut_del + taille / 2 - longeur / 2, debut_del + taille / 2 + longeur / 2);
    }

    @Override
    public void motifs(int longeurRead) {
        List<String> response = new ArrayList<>();
        Random random = new Random();

        String s1 = autourDel1(longeurRead);
        String s2 = autourDel2(longeurRead);
        String s3 = autourLiaison(longeurRead);

        for (int i = 0; i < 3; i++) {
            int rand;
            if (i == 0) {
                rand = random.nextInt((longeurRead / 2) - 5 + 1);
            } else if (i == 3) {
                rand = random.nextInt((longeurRead - 5 + 1));
            } else {
                rand = random.nextInt((longeurRead - 5) - (longeurRead / 2)) + longeurRead / 2;
            }
            response.add(s1.substring(rand, rand + 5));
        }
        for (int i = 0; i < 3; i++) {
            int rand;
            if (i == 0) {
                rand = random.nextInt((longeurRead / 2) - 5 + 1);
            } else if (i == 3) {
                rand = random.nextInt((longeurRead - 5 + 1));
            } else {
                rand = random.nextInt((longeurRead - 5) - (longeurRead / 2)) + longeurRead / 2;
            }
            response.add(s2.substring(rand, rand + 5));
        }
        for (int i = 0; i < 4; i++) {
            int rand;
            if (i == 0) {
                rand = random.nextInt((longeurRead / 2) - 6);
            } else {
                rand = random.nextInt((longeurRead - 5) - (longeurRead / 2)) + longeurRead / 2;
            }
            response.add(s3.substring(rand, rand + 5));
        }

        motifs = response;
        motifs.addAll(response
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
    public void listeRef(int length) {
        listeRef = fin_del - debut_del > 0 ? List.of(autourDel1(length), autourDel2(length), autourLiaison(length))
                : List.of(autourCentre(length), autourLiaison(length));

    }

    @Override
    public void mostifsStricted() {
        mostifsStricted = new ArrayList<>();
        List<String> motifs = fin_del - debut_del > 0 ? List.of(autourDel1(10), autourDel2(10), autourLiaison(10))
                : List.of(autourCentre(10), autourLiaison(10));

        mostifsStricted.addAll(motifs);
        mostifsStricted.addAll(motifs
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
    public void sequenceMutated(int longeur) {
        sequenceMutated = autourLiaison(longeur);
    }
}
