package com.stage.neuroPsi.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import com.stage.neuroPsi.models.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Aligner {

    private MutationsService mutationsService;

    private MutationsToAlign sequences;

    private String fichiersTempPath;

    public Aligner(MutationsToAlign sequences, String temp) {
        this.sequences = sequences;
        this.fichiersTempPath = temp;
    }

    public Aligner(MutationsToAlign sequences, String temp, MutationsService mutationsService) {
        this.sequences = sequences;
        this.fichiersTempPath = temp;
        this.mutationsService = mutationsService;
    }

    public ResultsRegrouper getSimpleResults(Line path) throws IOException {
        alignement(path);
        FileReader reader = new FileReader(sequences.getResults());
        BufferedReader br = new BufferedReader(reader);

        String line;
        List<AlignResults> resultats = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            String[] colonnes = line.split("\t");
            AlignResults resultat = new AlignResults(mutationsService.getMutation(colonnes[0].split("_")[0]),
                    path,
                    colonnes[1],
                    Double.parseDouble(colonnes[2]),
                    Integer.parseInt(colonnes[3]), Integer.parseInt(colonnes[6]),
                    Integer.parseInt(colonnes[8]),
                    Integer.parseInt(colonnes[7]), Integer.parseInt(colonnes[9]),
                    colonnes[7],
                    colonnes[0].split("_")[1].equals("wt"),
                    Integer.parseInt(colonnes[4]), Integer.parseInt(colonnes[5]));

            resultats.add(resultat);
        }

        // if (!resultats.stream().anyMatch(res -> res.getMutation() instanceof
        // MultipleMutation))
        // mutationsService.addResults(resultats);

        br.close();

        return new ResultsRegrouper(resultats);

    }

    private synchronized void alignement(Line path) throws IOException {
        double startTime = System.nanoTime();
        String command = "src/main/resources/static/alignement/alignment.sh";
        System.out.println("START : " + path.getLineId());

        Path sourcePath = Paths.get(path.getPath());
        Path destinationPath = Paths.get(fichiersTempPath + "/output_file.fasta");

        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder processBuilder;

        if (os.contains("win")) {
            processBuilder = new ProcessBuilder("ubuntu", "run", "-e",
                    "bash", "-c", command);
        } else {
            processBuilder = new ProcessBuilder("bash", command);
        }

        try {
            Process process = processBuilder.start();

            process.waitFor();

            System.out.println("SUCCESS");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(
                "fin alignement " + path + " temps : " + ((double) System.nanoTime() - startTime) / 1_000_000_000.0);
    }
}
