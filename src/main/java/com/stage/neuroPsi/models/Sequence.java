package com.stage.neuroPsi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import jakarta.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Substitution.class, name = "substitution"),
        @JsonSubTypes.Type(value = Deletion.class, name = "deletion"),
        @JsonSubTypes.Type(value = Insertion.class, name = "insertion"),
        @JsonSubTypes.Type(value = Inversion.class, name = "inversion"),
        @JsonSubTypes.Type(value = MultipleMutation.class, name = "multiple")
})
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Sequence {
    @Id
    protected String nom;

    protected int debut;

    protected int fin;

    protected String chromosome;

    @ManyToMany
    @JoinTable(name = "sequence_gene", joinColumns = @JoinColumn(name = "sequence_id"), inverseJoinColumns = @JoinColumn(name = "nom"))
    private Set<Gene> affectedGenes = new HashSet<>();

    @Transient
    protected String sequenceEntiere;

    @Transient
    protected String sequenceMutated;

    @Transient
    public List<String> listeRef = new ArrayList<>();

    @Transient
    public List<String> motifs = new ArrayList<>();

    @Transient
    public List<String> mostifsStricted = new ArrayList<>();

    @Transient
    public List<String[]> alignements_match = new ArrayList<>();

    @Transient
    public List<String[]> alignements_mismatch = new ArrayList<>();

    @Transient
    public File fichierResultats;

    public Sequence(String nom) {
        this.nom = nom;
    }

    public Sequence(int debut, int fin, String chromosome, String nom) {

        this.debut = debut;
        this.fin = fin;
        this.chromosome = chromosome;
        this.nom = nom;
    }

    public Sequence(int debut, int fin, String chromosome, String nom, Set<Gene> genes) {

        this.debut = debut;
        this.fin = fin;
        this.chromosome = chromosome;
        this.nom = nom;
        this.affectedGenes = genes;
    }

    public Sequence(int debut, int fin, String chromosome, String nom, String sequenceEntiere) {
        this.debut = debut;
        this.fin = fin;
        this.chromosome = chromosome;
        this.nom = nom;
        this.sequenceEntiere = sequenceEntiere;
    }

    public Sequence(int debut, int fin, String chromosome, String nom, Boolean isForward) {
        this.debut = debut;
        this.fin = fin;
        this.chromosome = chromosome;
        this.nom = nom;
    }

    public abstract void listeRef(int length);

    public abstract void motifs(int longeurRead);

    public abstract void mostifsStricted();

    public abstract void sequenceMutated(int length);

    public void allInitialize(int length) {
        if (this.sequenceEntiere == null || this.sequenceEntiere == "")
            findFullSequence();
        if (this instanceof Deletion) {
            Deletion delet = ((Deletion) this);
            length = Math.min(length, delet.getFin_del() - delet.getDebut_del() + 30);
        }

        sequenceMutated(length);
        motifs(length);
        mostifsStricted();
        listeRef(length);
    }

    public void findFullSequence() {
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
        setSequenceEntiere(getSequenceEntiere());
    }

    public List<String> reversedRef() {
        return listeRef.stream()
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
                }).toList();
    }
}
