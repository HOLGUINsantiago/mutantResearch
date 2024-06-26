package com.stage.neuroPsi.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stage.neuroPsi.models.Line;
import com.stage.neuroPsi.models.MultipleMutation;
import com.stage.neuroPsi.models.MutationsToAlign;
import com.stage.neuroPsi.models.ResultsRegrouper;
import com.stage.neuroPsi.models.Sequence;
import com.stage.neuroPsi.service.ImportationService;
import com.stage.neuroPsi.service.LineService;
import com.stage.neuroPsi.service.MutationsService;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.HttpStatus;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("api/neuroPsi/mutantResearch")
@CrossOrigin(origins = "*")
// Reception des requetes HTTP
public class Controller {

    @Value("${fichiers.temporaires}")
    private String fichiersTempPath;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Importation des composants qui seront utilisés
    @Autowired
    private ImportationService importationService;

    @Autowired
    private LineService lineService;

    @Autowired
    private MutationsService mutationsService;

    @PostMapping("{lignName}")
    // Recherche dans un ligné spécifique : renvoie fichier avec alignements
    public ByteArrayResource getSequenceUnicMulti(@RequestBody List<String> seq,
            @PathVariable String lignName,
            @RequestParam(name = "margeMotifs", defaultValue = "2") int margeMotifs,
            @RequestParam(name = "margeStricted", defaultValue = "3") int margeStricted,
            @RequestParam(name = "applyFilter", defaultValue = "true") boolean applyFilter,
            @RequestParam(name = "tailleRefSeq", defaultValue = "40") int tailleRefSeq)
            throws IOException, InterruptedException, ExecutionException {
        return importationService.getSequenceUnicMulti(
                new MutationsToAlign(mutationsService.getMutations(seq), fichiersTempPath, tailleRefSeq),
                getLine(lignName), margeMotifs, margeStricted, applyFilter).getFichier();
    }

    // Recherche plusieurs mmutations dans tous les lignés disponibles
    @PostMapping
    public Map<String, Map<String, Map<String, Long>>> getSequenceMulti(@RequestBody List<String> seq,
            @RequestParam(name = "margeMotifs", defaultValue = "2") int margeMotifs,
            @RequestParam(name = "margeStricted", defaultValue = "3") int margeStricted,
            @RequestParam(name = "applyFilter", defaultValue = "true") boolean applyFilter,
            @RequestParam(name = "tailleRefSeq", defaultValue = "40") int tailleRefSeq,
            @RequestParam(name = "saveInDb", defaultValue = "true") boolean saveInDb)
            throws IOException, InterruptedException {
        List<Line> allLines = getAllLines();
        Map<String, Map<String, Map<String, Long>>> results = new HashMap<>();

        for (String mutation : seq) {
            // Obtention de la mutation à partir du nom
            List<Line> lines = allLines.stream().map(x -> new Line(x)).toList();
            Sequence sequence = mutationsService.getMutation(mutation);

            Entry<Sequence, Map<Line, ResultsRegrouper>> existing;
            List<List<String>> done = new ArrayList<>();
            results.put(mutation, new HashMap<>()); // Ajouter chaque mutation comme clé

            if (saveInDb) // En cas de vouloir sauvegarder dans la base de données
                for (Line line : lines) {
                    // En cas de resultats dèja existant
                    if ((existing = mutationsService.result(line, sequence, margeMotifs, margeStricted, applyFilter,
                            tailleRefSeq)).getValue().entrySet().stream().allMatch(entry -> entry.getValue() != null)) {
                        existing.getValue().entrySet().forEach(// Obtenir le result regrouper pour chaque lignée
                                exist -> {
                                    // Ajouter le resultat existant aux resultats finaux et aux alignements faits
                                    results.get(mutation).put(exist.getKey().getLineId(), exist.getValue().results());
                                    done.add(List.of(sequence.getNom(), exist.getKey().getLineId()));
                                });
                    }
                }
            if (sequence instanceof MultipleMutation)
                saveInDb = false;

            // Lancer le pipeline
            results.put(mutation, importationService.getSequencesMutliMulti(
                    new MutationsToAlign(sequence, fichiersTempPath,
                            tailleRefSeq),
                    lines, margeMotifs,
                    margeStricted, applyFilter, done, saveInDb));

        }

        return results;
    }

    // Verification sequence mutiple
    @GetMapping("multiple")
    public String getSequenceMultitrop(@RequestBody MultipleMutation seq) {
        seq.allInitialize(54);
        return seq.getSequenceMutated();
    }

    @PostMapping("mutations")
    // Ajouter une mutation
    public Sequence addMutatioSequence(@RequestBody Sequence mutation) {
        return mutationsService.saveMutation(mutation);
    }

    @GetMapping("mutations")
    // Obtenir toutes les mutations
    public List<Sequence> getMutations() {
        return mutationsService.findAllMutations().stream().map(seq -> {
            seq.allInitialize(40);
            return seq;
        }).toList();
    }

    @GetMapping("results")
    // Obtention de tous les resultats
    public Map<String, Map<String, String>> getResults(
            @RequestParam(name = "scoreMin", defaultValue = "75") int scoreMin,
            @RequestParam(name = "scoreMinInteressed", defaultValue = "70") int scoreMinInteressed,
            @RequestParam(name = "fullStricted", defaultValue = "true") boolean fullStricted,
            @RequestParam(name = "tailleRefSeq", defaultValue = "40") int tailleRefSeq) {
        Map<String, Map<String, String>> result = new HashMap<>();
        // Obtenir les resultats détailles
        for (Entry<Sequence, Map<Line, ResultsRegrouper>> entry : getResultsFull(scoreMin, scoreMinInteressed,
                fullStricted, tailleRefSeq).entrySet()) {
            // Ajout des resultats pour chaque ligné
            result.put(entry.getKey().getNom(), new HashMap<>());
            for (Entry<Line, ResultsRegrouper> entryLine : entry.getValue().entrySet()) {
                result.get(entry.getKey().getNom()).put(entryLine.getKey().getLineId(),
                        entryLine.getValue().texteResults(scoreMin, scoreMinInteressed, fullStricted));
            }
        }
        return result;
    }

    @GetMapping("results/full")
    // Obtention detaillé des résultats
    public Map<Sequence, Map<Line, ResultsRegrouper>> getResultsFull(
            @RequestParam(name = "scoreMin", defaultValue = "80") int scoreMin,
            @RequestParam(name = "scoreMinInteressed", defaultValue = "75") int scoreMinInteressed,
            @RequestParam(name = "fullStricted", defaultValue = "true") boolean fullStricted,
            @RequestParam(name = "tailleRefSeq", defaultValue = "40") int tailleRefSeq) {
        return mutationsService.getResultsFull(scoreMin, scoreMinInteressed, fullStricted, tailleRefSeq);
    }

    // Obtenir mutation par nom
    @GetMapping("mutations/{nom}")
    public Sequence getMutation(@PathVariable String nom) {
        Sequence res = mutationsService.getMutation(nom);
        res.allInitialize(54);
        return res;
    }

    // Obtenir chemins de tous les lignés
    @GetMapping("lines/paths")
    public List<String> getAllLinesPaths() {
        return lineService.getAllLines().stream().map(line -> line.getPath()).toList();
    }

    // Obtenir toutes les lignées
    @GetMapping("lines")
    public List<Line> getAllLines() {
        return lineService.getAllLines();
    }

    // Obtenir une lignée
    @GetMapping("lines/{id}")
    public Line getLine(@PathVariable String id) {
        return lineService.getById(id);
    }

    // AJouter une lignée
    @PostMapping("/line")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam("metadata") String metadata) {
        ObjectMapper objectMapper = new ObjectMapper();
        Line line;

        try {
            line = objectMapper.readValue(metadata, Line.class);
            if (new File(line.getPath()).exists()) {
                lineService.addLine(line);
                return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
            }
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Invalid metadata");
        }

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        try {
            Path path = Paths.get(uploadDir, line.getLineId());
            File dest = path.toFile();
            FileUtils.copyInputStreamToFile(file.getInputStream(), dest);

            line.setPath(uploadDir + "/" + line.getPath());
            lineService.addLine(line);
            return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save file", e);
        }
    }
}
