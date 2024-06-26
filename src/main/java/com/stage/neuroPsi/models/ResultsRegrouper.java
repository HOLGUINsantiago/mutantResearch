package com.stage.neuroPsi.models;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.core.io.ByteArrayResource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stage.neuroPsi.service.alignement.SmithWaterman;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ResultsRegrouper {
    @JsonIgnore
    private List<AlignResults> resultats = new ArrayList<>();

    @JsonIgnore
    public Map<String, List<AlignResults>> filtrage() {
        Map<String, List<AlignResults>> reponse = new HashMap<>();

        reponse.put("matching", new ArrayList<>());
        reponse.put("not matching", new ArrayList<>());

        List<AlignResults> premierFiltreTrue = premierFiltre(resultats, 75, 70, true).get(true);
        List<AlignResults> premierFiltreFalse = premierFiltre(resultats, 75, 70, true).get(false);

        if (premierFiltreTrue != null && !premierFiltreTrue.isEmpty())
            for (AlignResults resultat : premierFiltreTrue) {
                Optional<AlignResults> existingResult = reponse.get("matching").stream()
                        .filter(res -> res.getReadId().equals(resultat.getReadId()))
                        .findFirst();

                if (existingResult.isPresent()) {
                    if (resultat.compareTo(existingResult.get()) > 0) {
                        reponse.get("matching").add(resultat);
                        reponse.get("matching").remove(existingResult.get());
                        reponse.get("not matching").add(existingResult.get());
                    } else {
                        reponse.get("not matching").add(resultat);
                    }
                } else
                    reponse.get("matching").add(resultat);

            }
        if (premierFiltreFalse != null && !premierFiltreFalse.isEmpty())
            reponse.get("not matching").addAll(premierFiltreFalse);
        return reponse;
    }

    @JsonIgnore
    public Map<String, List<AlignResults>> filtrage(int scoreMin, int scoreMinInteressed, boolean fullStricted) {
        Map<String, List<AlignResults>> reponse = new HashMap<>();

        reponse.put("matching", new ArrayList<>());
        reponse.put("not matching", new ArrayList<>());

        List<AlignResults> premierFiltreTrue = premierFiltre(resultats, scoreMin, scoreMinInteressed, fullStricted)
                .get(true);
        List<AlignResults> premierFiltreFalse = premierFiltre(resultats, scoreMin, scoreMinInteressed, fullStricted)
                .get(false);

        if (premierFiltreTrue != null && !premierFiltreTrue.isEmpty())
            for (AlignResults resultat : premierFiltreTrue) {
                Optional<AlignResults> existingResult = reponse.get("matching").stream()
                        .filter(res -> res.getReadId().equals(resultat.getReadId()))
                        .findFirst();

                if (existingResult.isPresent()) {
                    if (resultat.compareTo(existingResult.get()) > 0) {
                        reponse.get("matching").add(resultat);
                        reponse.get("matching").remove(existingResult.get());
                        reponse.get("not matching").add(existingResult.get());
                    } else {
                        reponse.get("not matching").add(resultat);
                    }
                } else
                    reponse.get("matching").add(resultat);

            }
        if (premierFiltreFalse != null && !premierFiltreFalse.isEmpty())
            reponse.get("not matching").addAll(premierFiltreFalse);
        return reponse;
    }

    @SuppressWarnings("null")
    @JsonIgnore
    public ByteArrayResource getFichier() throws IOException {
        List<String> content = new ArrayList<>();
        content.add("SUMMARY : Matchs of mutations = " + mutatedMatchs() + " , matchs of wt sequences = " + wtMatchs()
                + " ; Proportion/ratio = " + ratioMutated());
        content.add("\n");

        Map<String, List<AlignResults>> filtered = filtrage();
        for (String type : new String[] { "matching", "not matching" }) {
            content.add(type + " results :");
            for (Entry<Line, List<AlignResults>> line : filtered.get(type).stream()
                    .collect(Collectors.groupingBy(AlignResults::getLine)).entrySet()) {
                try (FileReader reader = new FileReader(line.getKey().getPath());
                        BufferedReader br = new BufferedReader(reader)) {
                    String ligne;
                    boolean seq = false;
                    AlignResults result = null;
                    while ((ligne = br.readLine()) != null) {
                        String cop = ligne;
                        if (seq) {
                            content.add(ligne);
                            seq = false;

                            content.add("Alignement : ");
                            if (result.getMutation().getListeRef().size() == 3) {
                                content.add(result.isWt() ? "Wt match : " : "Mutated Match : ");
                                if (!result.isWt()) {
                                    String[] sm = new SmithWaterman(
                                            result.isForward() ? result.getMutation().getListeRef().get(2)
                                                    : result.getMutation().reversedRef().get(2),
                                            ligne, 1,
                                            -1, -2)
                                            .getAlignment();
                                    content.add(sm[0]);
                                    content.add(sm[1]);
                                    content.add("\n");
                                } else {

                                    SmithWaterman sm1 = new SmithWaterman(
                                            result.isForward() ? result.getMutation().getListeRef().get(0)
                                                    : result.getMutation().reversedRef().get(0),
                                            ligne, 1, -1, -2);
                                    SmithWaterman sm2 = new SmithWaterman(
                                            result.isForward() ? result.getMutation().getListeRef().get(1)
                                                    : result.getMutation().reversedRef().get(1),
                                            ligne, 1, -1, -2);
                                    String[] good = (sm1.getAlignmentScore() > sm2.getAlignmentScore() ? sm1 : sm2)
                                            .getAlignment();

                                    content.add(good[0]);
                                    content.add(good[1]);
                                    content.add("\n");

                                }
                                content.add("\n");
                            } else {
                                content.add(result.isWt() ? "Wt match : " : "Mutated Match : ");
                                String[] sm = new SmithWaterman(
                                        result.getMutation().getListeRef().get(result.isWt() ? 0 : 1), ligne, 1, -1, -2)
                                        .getAlignment();
                                content.add(sm[0]);
                                content.add(sm[1]);
                                content.add("\n");
                            }
                        } else if ((result = filtered.get("matching").stream()
                                .filter(id -> id.getReadId().equals(cop.split(" ")[0].substring(1))).findFirst()
                                .orElse(null)) != null) {
                            content.add(ligne);
                            seq = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return generateFileFromContent(content);

    }

    public long mutatedMatchs() {
        return filtrage().get("matching").stream().filter(res -> !res.isWt()).count();
    }

    public long wtMatchs() {
        return filtrage().get("matching").stream().filter(res -> res.isWt()).count();
    }

    public long ratioMutated() {
        return (long) (wtMatchs() != 0.0 && mutatedMatchs() != 0.0 ? mutatedMatchs() / (mutatedMatchs() + wtMatchs())
                : 0.0);
    }

    public long mutatedMatchs(int scoreMin, int scoreMinInteressed, boolean fullStricted) {
        return filtrage(scoreMin, scoreMinInteressed, fullStricted).get("matching").stream().filter(res -> !res.isWt())
                .count();
    }

    public long wtMatchs(int scoreMin, int scoreMinInteressed, boolean fullStricted) {
        return filtrage(scoreMin, scoreMinInteressed, fullStricted).get("matching").stream().filter(res -> res.isWt())
                .count();
    }

    public long ratioMutated(int scoreMin, int scoreMinInteressed, boolean fullStricted) {
        return (long) (wtMatchs(scoreMin, scoreMinInteressed, fullStricted) != 0.0 && mutatedMatchs() != 0.0
                ? mutatedMatchs() / (mutatedMatchs() + wtMatchs())
                : 0.0);
    }

    @JsonIgnore
    public static ByteArrayResource generateFileFromContent(List<String> lines) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            for (String line : lines) {
                baos.write(line.getBytes(StandardCharsets.UTF_8));
                baos.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
            }
            return new ByteArrayResource(baos.toByteArray());
        }
    }

    public Map<String, Long> results() {
        return Map.of("matchs", mutatedMatchs(), "mismatchs", wtMatchs(), "ratio", ratioMutated());
    }

    public Map<String, Long> results(int scoreMin, int scoreMinInteressed, boolean fullStricted) {
        return Map.of("matchs", mutatedMatchs(scoreMin, scoreMinInteressed, fullStricted), "mismatchs",
                wtMatchs(scoreMin, scoreMinInteressed, fullStricted), "ratio",
                ratioMutated(scoreMin, scoreMinInteressed, fullStricted));
    }

    private Map<Boolean, List<AlignResults>> premierFiltre(List<AlignResults> valores, double scoreMin,
            double scoreMinInteressed, boolean fullStricted) {
        if (valores == null || valores.isEmpty()) {
            return new HashMap<>();
        }

        double media = valores.stream().map(AlignResults::score).mapToDouble(Double::doubleValue).average()
                .orElse(Double.NaN);

        double sumatoriaCuadrados = valores.stream()
                .mapToDouble(valor -> Math.pow(valor.score() - media, 2))
                .sum();
        double desviacionEstandar = Math.sqrt(sumatoriaCuadrados / Math.pow(valores.size(), 2));

        double umbral = media - desviacionEstandar;

        // TDO : comparar con ||
        return valores.stream()
                .collect(Collectors.partitioningBy(valor -> fullStricted ? valor.score() > umbral
                        && (valor.score() > scoreMin
                                || (valor.isInInterestPosition() && valor.score() > scoreMinInteressed))
                        : valor.score() > umbral
                                && (valor.score() > scoreMin
                                        && (valor.isInInterestPosition() && valor.score() > scoreMinInteressed))));
    }

    public String texteResults(int scoreMin, int scoreMinInteressed, boolean fullStricted) {
        String texte = "";

        for (AlignResults res : filtrage(scoreMin, scoreMinInteressed, fullStricted).get("matching"))
            texte += res.isWt() ? ""
                    : res.getReadId() + " , score : " + res.score()
                            + " - ";

        texte += "Wt : " + wtMatchs(scoreMin, scoreMinInteressed, fullStricted) + " , et mutated : "
                + mutatedMatchs(scoreMin, scoreMinInteressed, fullStricted);
        return texte;
    }

}
