package com.stage.neuroPsi.models;

import java.util.*;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.data.util.Pair;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class MultipleMutation extends Sequence {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "multiple_mutation_id")
    private List<Sequence> mutations = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "multiple_mutation_relative_id")
    private List<Sequence> mutations_relative = new ArrayList<>();

    @JsonIgnore
    @Transient
    Map<Pair<Sequence, Sequence>, Integer> distanceEntreMutations = new HashMap<>();

    @JsonIgnore
    private int mutIndiceStart = 0;

    @JsonIgnore
    private int mutIndiceEnd = 0;

    @JsonIgnore
    private int wtIndiceStart = 0;

    @JsonIgnore
    private int wtIndiceEnd = 0;

    public MultipleMutation(String nom) {
        super(nom);
    }

    public MultipleMutation(int debut, int fin, String chromosome, String nom) {
        super(debut, fin, chromosome, nom);
    }

    public MultipleMutation(int debut, int fin, String chromosome, String nom, List<Sequence> mutations,
            Set<Gene> genes) {
        super(debut, fin, chromosome, nom, genes);
        this.mutations = mutations;
    }

    public List<Sequence> triageMutations() {
        List<Sequence> res = mutations;
        Collections.sort(res, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence m1, Sequence m2) {
                if (m1 instanceof Substitution && !(m2 instanceof Substitution)) {
                    return -1;
                } else if (!(m1 instanceof Substitution) && m2 instanceof Substitution) {
                    return 1;
                } else if (m1 instanceof Inversion && !(m2 instanceof Inversion)) {
                    return -1;
                } else if (!(m1 instanceof Inversion) && m2 instanceof Inversion) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        return res;
    }

    // renvoyer la sequence entre deux mutations
    public void motifsChangingPosition() {
        List<Sequence> changingPosition = mutations;

        distanceEntreMutations = new HashMap<>();

        for (int i = 0; i < changingPosition.size(); i++) {
            for (int j = i + 1; j < changingPosition.size(); j++) {
                Sequence seq1 = changingPosition.get(i);
                Sequence seq2 = changingPosition.get(j);

                if (seq1 instanceof Deletion) {
                    if (seq2 instanceof Deletion)
                        if (((Deletion) seq1).getFin_del() < ((Deletion) seq2).getDebut_del())
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Deletion) seq2).getDebut_del() - ((Deletion) seq1).getFin_del());
                        else
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Deletion) seq1).getDebut_del() - ((Deletion) seq2).getFin_del());

                    else if (seq2 instanceof Insertion)
                        if (((Deletion) seq1).getFin_del() < ((Insertion) seq2).getPos_insertion())
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Insertion) seq2).getPos_insertion() - ((Deletion) seq1).getFin_del());
                        else
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Deletion) seq1).getDebut_del() - ((Insertion) seq2).getPos_insertion());

                    else if (seq2 instanceof Substitution)
                        if (((Deletion) seq1).getFin_del() < ((Substitution) seq2).getPos_substitution())
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Substitution) seq2).getPos_substitution() - ((Deletion) seq1).getFin_del());
                        else
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Deletion) seq1).getDebut_del() - ((Substitution) seq2).getPos_substitution());

                    else if (seq2 instanceof Inversion)
                        if (((Deletion) seq1).getFin_del() < ((Inversion) seq2).getDebut_inversion())
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Inversion) seq2).getDebut_inversion() - ((Deletion) seq1).getFin_del());
                        else
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Deletion) seq1).getDebut_del() - ((Inversion) seq2).getFin_inversion());

                } else if (seq1 instanceof Insertion) {
                    if (seq2 instanceof Insertion)
                        if (((Insertion) seq1).getPos_insertion() < ((Insertion) seq2).getPos_insertion())
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Insertion) seq2).getPos_insertion() - ((Insertion) seq1).getPos_insertion());
                        else
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Insertion) seq1).getPos_insertion() - ((Insertion) seq2).getPos_insertion());

                    else if (seq2 instanceof Deletion)
                        if (((Deletion) seq2).getFin_del() > ((Insertion) seq1).getPos_insertion())
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Deletion) seq2).getDebut_del() - ((Insertion) seq1).getPos_insertion());
                        else
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Insertion) seq1).getPos_insertion() - ((Deletion) seq2).getFin_del());

                    else if (seq2 instanceof Substitution)
                        if (((Substitution) seq2).getPos_substitution() > ((Insertion) seq1).getPos_insertion())
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Substitution) seq2).getPos_substitution()
                                            - ((Insertion) seq1).getPos_insertion());
                        else
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Insertion) seq1).getPos_insertion()
                                            - ((Substitution) seq2).getPos_substitution());

                    else if (seq2 instanceof Inversion)
                        if (((Inversion) seq2).getDebut_inversion() > ((Insertion) seq1).getPos_insertion())
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Inversion) seq2).getDebut_inversion()
                                            - ((Insertion) seq1).getPos_insertion());
                        else
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Insertion) seq1).getPos_insertion()
                                            - ((Inversion) seq2).getFin_inversion());

                } else if (seq1 instanceof Inversion) {
                    if (seq2 instanceof Inversion)
                        if (((Inversion) seq1).getFin_inversion() < ((Inversion) seq2).getDebut_inversion())
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Inversion) seq2).getDebut_inversion() - ((Inversion) seq1).getFin_inversion());
                        else
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Inversion) seq1).getDebut_inversion() - ((Inversion) seq2).getFin_inversion());

                    else if (seq2 instanceof Deletion)
                        if (((Inversion) seq1).getFin_inversion() < ((Deletion) seq2).getDebut_del())
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Deletion) seq2).getDebut_del() - ((Inversion) seq1).getFin_inversion());
                        else
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Inversion) seq1).getDebut_inversion() - ((Deletion) seq2).getFin_del());

                    else if (seq2 instanceof Substitution)
                        if (((Inversion) seq1).getFin_inversion() < ((Substitution) seq2).getPos_substitution())
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Substitution) seq2).getPos_substitution()
                                            - ((Inversion) seq1).getFin_inversion());
                        else
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Inversion) seq1).getDebut_inversion()
                                            - ((Substitution) seq2).getPos_substitution());

                    else if (seq2 instanceof Insertion)
                        if (((Inversion) seq1).getFin_inversion() < ((Insertion) seq2).getPos_insertion())
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Insertion) seq2).getPos_insertion()
                                            - ((Inversion) seq1).getFin_inversion());
                        else
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Inversion) seq1).getDebut_inversion()
                                            - ((Insertion) seq2).getPos_insertion());

                } else if (seq1 instanceof Substitution) {
                    if (seq2 instanceof Insertion)
                        if (((Substitution) seq1).getPos_substitution() < ((Insertion) seq2).getPos_insertion())
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Insertion) seq2).getPos_insertion()
                                            - ((Substitution) seq1).getPos_substitution());
                        else
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Substitution) seq1).getPos_substitution()
                                            - ((Insertion) seq2).getPos_insertion());

                    else if (seq2 instanceof Deletion)
                        if (((Deletion) seq2).getDebut_del() > ((Substitution) seq1).getPos_substitution())
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Deletion) seq2).getDebut_del() - ((Substitution) seq1).getPos_substitution());
                        else
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Insertion) seq1).getPos_insertion() - ((Deletion) seq2).getFin_del());

                    else if (seq2 instanceof Substitution)
                        if (((Substitution) seq2).getPos_substitution() > ((Substitution) seq1)
                                .getPos_substitution())
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Substitution) seq2).getPos_substitution()
                                            - ((Substitution) seq1).getPos_substitution());
                        else
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Insertion) seq1).getPos_insertion()
                                            - ((Substitution) seq2).getPos_substitution());

                    else if (seq2 instanceof Inversion)
                        if (((Inversion) seq2).getDebut_inversion() > ((Substitution) seq1).getPos_substitution())
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Inversion) seq2).getDebut_inversion()
                                            - ((Substitution) seq1).getPos_substitution());
                        else
                            distanceEntreMutations.put(Pair.of(seq1, seq2),
                                    ((Substitution) seq1).getPos_substitution()
                                            - ((Inversion) seq2).getFin_inversion());

                }
            }
        }

        for (Entry<Pair<Sequence, Sequence>, Integer> entry : distanceEntreMutations.entrySet()) {
            entry.setValue(entry.getValue() - 1);
        }
    }

    @Override
    public void listeRef(int length) {
        int start = 0;
        int end = 0;

        Sequence debut = mutations.get(0);

        if (debut instanceof Deletion)
            start = ((Deletion) debut).getDebut_del() - debut.getDebut();
        else if (debut instanceof Insertion)
            start = ((Insertion) debut).getPos_insertion() - debut.getDebut();
        else if (debut instanceof Substitution)
            start = ((Substitution) debut).getPos_substitution() - debut.getDebut();
        else if (debut instanceof Inversion)
            start = ((Inversion) debut).getDebut_inversion() - debut.getDebut();

        Sequence fin = mutations.get(mutations.size() - 1);

        if (fin instanceof Deletion)
            end = ((Deletion) fin).getFin_del() - fin.getDebut();
        else if (fin instanceof Insertion)
            end = ((Insertion) fin).getPos_insertion() + ((Insertion) fin).getInserted().length() - fin.getDebut();
        else if (fin instanceof Substitution)
            end = ((Substitution) fin).getPos_substitution() - fin.getDebut();
        else if (fin instanceof Inversion)
            end = ((Inversion) fin).getFin_inversion() - fin.getDebut();

        int diff = length > end - start + 20 ? (length - end - start) / 2 : 10;

        listeRef.add(sequenceEntiere.substring(start - diff, end + diff));

        wtIndiceEnd = length - diff;
        wtIndiceStart = diff;

        debut = mutations_relative.get(0);

        if (debut instanceof Deletion)
            start = ((Deletion) debut).getDebut_del() - debut.getDebut();
        else if (debut instanceof Insertion)
            start = ((Insertion) debut).getPos_insertion() - debut.getDebut();
        else if (debut instanceof Substitution)
            start = ((Substitution) debut).getPos_substitution() - debut.getDebut();
        else if (debut instanceof Inversion)
            start = ((Inversion) debut).getDebut_inversion() - debut.getDebut();

        fin = mutations_relative.get(mutations_relative.size() - 1);

        if (fin instanceof Deletion)
            end = ((Deletion) fin).getFin_del() - fin.getDebut();
        else if (fin instanceof Insertion)
            end = ((Insertion) fin).getPos_insertion() + ((Insertion) fin).getInserted().length() - fin.getDebut();
        else if (fin instanceof Substitution)
            end = ((Substitution) fin).getPos_substitution() - fin.getDebut();
        else if (fin instanceof Inversion)
            end = ((Inversion) fin).getFin_inversion() - fin.getDebut();

        diff = length > end - start + 20 ? (length - end + start) / 2 : 10;
        if (diff * 2 + end - start < length)
            start--;

        mutIndiceEnd = length - diff;
        mutIndiceStart = diff;

        listeRef.add(sequenceMutated.substring(start - diff, end + diff));

    }

    @Override
    public void motifs(int longeur) {
        initialize();
        List<String> response = new ArrayList<>();
        Random random = new Random();

        String s1 = sequenceEntiere;
        String s2 = sequenceMutated;
        int longeur1 = sequenceEntiere.length();
        int longeur2 = sequenceMutated.length();

        for (int i = 0; i < 4; i++) {
            int rand;
            if (i == 0 || i == 1) {
                rand = random.nextInt((longeur1 / 2) - 5 + 1);
            } else {
                rand = random.nextInt((longeur1 - 5) - (longeur1 / 2)) + longeur1 / 2;
            }
            response.add(s1.substring(rand, rand + 5));
        }
        for (int i = 0; i < 4; i++) {
            int rand;
            if (i == 0 || i == 1) {
                rand = random.nextInt((longeur2 / 2) - 6);
            } else {
                rand = random.nextInt((longeur2 - 5) - (longeur2 / 2)) + longeur2 / 2;
            }
            response.add(s2.substring(rand, rand + 5));
        }
        motifs = response;
    }

    @Override
    public void mostifsStricted() {
        List<String> s = new ArrayList<>();

        for (Sequence mutation : mutations) {
            if (mutation instanceof Deletion) {
                Deletion seq = (Deletion) mutation;
                s.add(seq.autourDel1(10));
                s.add(seq.autourDel2(10));
            } else if (mutation instanceof Insertion) {
                Insertion seq = (Insertion) mutation;
                s.add(seq.autourLiaison(10));
            } else if (mutation instanceof Inversion) {
                Inversion seq = (Inversion) mutation;
                s.add(seq.autourWt(10));
            } else if (mutation instanceof Substitution) {
                Substitution seq = (Substitution) mutation;
                s.add(seq.autourSubst(10));
            }

        }

        for (Sequence mutation : mutations_relative) {
            mutation.allInitialize(100);
            if (mutation instanceof Deletion) {
                Deletion seq = (Deletion) mutation;
                s.add(sequenceMutated.substring(seq.getDebut_del() - seq.getDebut() - 6,
                        seq.getDebut_del() - seq.getDebut() + 4));
            } else if (mutation instanceof Insertion) {
                Insertion seq = (Insertion) mutation;
                s.add(sequenceMutated.substring(seq.getPos_insertion() - seq.getDebut() - 6,
                        seq.getPos_insertion() + seq.getInserted().length() - seq.getDebut() + 4));
            } else if (mutation instanceof Inversion) {
                Inversion seq = (Inversion) mutation;
                s.add(sequenceMutated.substring(seq.getDebut_inversion() - seq.getDebut() - 6,
                        seq.getFin_inversion() - seq.getDebut() + 4));
            } else if (mutation instanceof Substitution) {
                Substitution seq = (Substitution) mutation;
                s.add(sequenceMutated.substring(seq.getPos_substitution() - seq.getDebut() - 6,
                        seq.getPos_substitution() - seq.getDebut() + 4));
            }
        }
        mostifsStricted = s;

        mostifsStricted.addAll(s
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
        StringBuilder mutatedSequence = new StringBuilder(sequenceEntiere);
        mutations_relative = new ArrayList<>(mutations);
        for (Sequence mutation : mutations_relative) {
            if (mutation instanceof Substitution) {
                Substitution substitution = (Substitution) mutation;
                mutatedSequence.setCharAt(substitution.getPos_substitution() - substitution.getDebut(),
                        substitution.getMutate());
            } else if (mutation instanceof Inversion) {
                Inversion inversion = (Inversion) mutation;
                int start = inversion.getDebut_inversion() - inversion.getDebut();
                int end = inversion.getFin_inversion() - inversion.getDebut();

                while (start < end) {
                    char temp = mutatedSequence.charAt(start);
                    mutatedSequence.setCharAt(start, mutatedSequence.charAt(end));
                    mutatedSequence.setCharAt(end, temp);
                    start++;
                    end--;
                }
            } else if (mutation instanceof Deletion) {
                Deletion deletion = (Deletion) mutation;
                mutatedSequence.replace(deletion.getDebut_del() - deletion.getDebut() - 1,
                        deletion.getFin_del() - deletion.getDebut(), "");
                updateMutationPositions(mutations_relative.indexOf(deletion),
                        (deletion.getDebut_del() - deletion.getFin_del() - 1) * -1);
            } else if (mutation instanceof Insertion) {
                Insertion insertion = (Insertion) mutation;
                mutatedSequence.insert(insertion.getPos_insertion() - insertion.getDebut(), insertion.getInserted());
                updateMutationPositions(mutations_relative.indexOf(insertion), insertion.getInserted().length());
            }
        }

        sequenceMutated = mutatedSequence.toString();
    }

    private void updateMutationPositions(int indice, int change) {
        for (int i = indice + 1; i < mutations_relative.size(); i++) {
            Sequence mutation = mutations_relative.get(i);
            if (mutation instanceof Deletion) {
                Deletion instance = (Deletion) mutation;
                Deletion copie = new Deletion(instance.getDebut(), instance.getFin(), instance.getChromosome(),
                        instance.getNom(), instance.getDebut_del(), instance.getFin_del(), instance.getAffectedGenes());
                copie.setDebut(copie.getDebut() + change);
                mutations_relative.set(i, copie);
            } else if (mutation instanceof Insertion) {
                Insertion instance = (Insertion) mutation;
                Insertion copie = new Insertion(instance.getDebut(), instance.getFin(), instance.getChromosome(),
                        instance.getNom(), instance.getPos_insertion(), instance.getInserted());
                copie.setDebut(copie.getDebut() + change);
                mutations_relative.set(i, copie);
            } else if (mutation instanceof Inversion) {
                Inversion instance = (Inversion) mutation;
                Inversion copie = new Inversion(instance.getDebut(), instance.getFin(), instance.getChromosome(),
                        instance.getNom(), instance.getDebut_inversion(), instance.getFin_inversion(),
                        instance.getInverted());
                copie.setDebut(copie.getDebut() + change);
                mutations_relative.set(i, copie);
            } else if (mutation instanceof Substitution) {
                Substitution instance = (Substitution) mutation;
                Substitution copie = new Substitution(instance.getDebut(), instance.getFin(), instance.getChromosome(),
                        instance.getNom(), instance.getPos_substitution(), instance.getMutate(),
                        instance.getAffectedGenes());
                copie.setDebut(copie.getDebut() + change);
                mutations_relative.set(i, copie);
            }

        }
    }

    public void initialize() {
        String seq = "";
        String url = "https://api.genome.ucsc.edu/getData/sequence?genome=dm3;chrom="
                + getChromosome() +
                ";start=" + getDebut() +
                ";end=" + getFin();

        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        try {
            while (seq.equals("")) {
                HttpResponse httpResponse = httpClient.execute(httpGet);

                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    String responseBody = EntityUtils.toString(httpResponse.getEntity());
                    DocumentContext body = JsonPath.parse(responseBody);
                    seq = (String) body.read("$['dna']").toString().toUpperCase();
                    if (!seq.equals(""))
                        setSequenceEntiere(seq);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        sequenceMutated(10);
        motifsChangingPosition();
        getMutations().forEach(mut -> mut.setSequenceEntiere(getSequenceEntiere()));
    }
}
