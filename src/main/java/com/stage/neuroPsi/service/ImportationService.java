package com.stage.neuroPsi.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.concurrent.*;

import com.stage.neuroPsi.models.AlignResults;
import com.stage.neuroPsi.models.Line;
import com.stage.neuroPsi.models.MutationsToAlign;
import com.stage.neuroPsi.models.ResultsRegrouper;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.*;

@Service
public class ImportationService {

    @Value("${filtrage.path}")
    private String filtragePath;

    @Value("${fichiers.temporaires}")
    private String tempPath;

    @Value("${seuilLongeurVerif}")
    private int seuil;

    @Value("${file.upload-dir}")
    private String linesPath;

    @Autowired
    private MutationsService mutationsService;

    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final ConcurrentLinkedQueue<Line> ready = new ConcurrentLinkedQueue<>();
    private final AtomicInteger done = new AtomicInteger(0);

    public ResultsRegrouper getSequenceUnicMulti(MutationsToAlign info, Line line, int margeMotifs, int margeStricted,
            boolean applyFilter)
            throws IOException, InterruptedException, ExecutionException {
        Aligner manager = new Aligner(info, tempPath, mutationsService);
        if (applyFilter)
            filtrageC(line, margeMotifs, margeStricted).run();
        return manager.getSimpleResults(line);
    }

    public Map<String, Map<String, Long>> getSequencesMutliMulti(MutationsToAlign info, List<Line> lines,
            int margeMotifs,
            int margeStricted, boolean addFilter, List<List<String>> forbidden, boolean save)
            throws IOException, InterruptedException {
        long startTime = System.nanoTime();
        Map<String, Map<String, Long>> response = new HashMap<>();
        Aligner manager = new Aligner(info, tempPath, mutationsService);
        int cores = Runtime.getRuntime().availableProcessors();
        lines = lines.stream()
                .filter(l -> !forbidden.stream()
                        .anyMatch(pair -> pair.get(0).equals(info.getSequences().get(0).getNom())
                                && pair.get(1).equals(l.getLineId())))
                .toList();

        if (addFilter) {
            for (int i = 0; i < lines.size(); i += cores) {
                int end = Math.min(i + cores, i == 0 ? lines.size() : cores + lines.size() - i);
                List<Line> sublist = lines.subList(i, end);
                for (Line line : sublist) {
                    executor.submit(filtrageC(line, margeMotifs, margeStricted));
                }
                if (cores / 2 < sublist.size())
                    waitForTasksToComplete(Math.abs(sublist.size() - cores / 2), sublist.size());

                while (!ready.isEmpty() || done.get() < sublist.size()) {
                    Line line = ready.poll();
                    if (line != null) {
                        ResultsRegrouper res = manager.getSimpleResults(line);
                        if (res.getResultats().isEmpty())
                            res.getResultats()
                                    .add(new AlignResults(info.getSequences().get(0), line, "", 1, 10, 0, 0, 0, 0,
                                            "", true, 0, 0));
                        if (save)
                            mutationsService.addResults(res.getResultats());
                        response.put(line.getLineId(), res.results());
                    }
                    Thread.sleep(100);
                }
                done.set(0);
                ready.clear();
            }
        } else {
            for (Line line : lines) {
                ResultsRegrouper res = manager.getSimpleResults(line);
                response.put(line.getLineId(), res.results());
            }
        }

        double seconds = ((double) System.nanoTime() - startTime) / 1_000_000_000.0;
        System.out.println("Tiempo transcurrido en segundos: " + seconds);

        executor.shutdown();
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
        ready.clear();
        done.set(0);

        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        FileUtils.cleanDirectory(new File(tempPath));

        return response;
    }

    private void waitForTasksToComplete(int expectedCount, int max) throws InterruptedException {
        while (done.get() < expectedCount && done.get() < max) {
            Thread.sleep(100);
        }
    }

    private Runnable filtrageC(Line line, int margeMotifs, int margeStricted) {
        return () -> {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                Process filtrage;

                if (os.contains("win")) {
                    filtrage = new ProcessBuilder(tempPath + "/Filtrage2.0.exe", line.getPath(),
                            tempPath + "/" + line.getLineId() + ".filtered",
                            Integer.toString(margeMotifs), Integer.toString(margeStricted))
                            .redirectErrorStream(true)
                            .start();
                } else {
                    filtrage = new ProcessBuilder(tempPath + "/Filtrage", line.getPath(),
                            tempPath + "/" + line.getLineId() + ".filtered",
                            Integer.toString(margeMotifs), Integer.toString(margeStricted))
                            .redirectErrorStream(true)
                            .start();
                }

                final Process finalFiltrage = filtrage;
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (finalFiltrage.isAlive())
                        finalFiltrage.destroy();
                }));

                filtrage.waitFor();

                // Capture both stdout and stderr
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(filtrage.getInputStream()))) {
                    String lineOutput;
                    while ((lineOutput = reader.readLine()) != null) {
                        System.out.println(lineOutput);
                    }
                }

                System.out.println("FIN FILTER " + done.get());
                line.setPath(tempPath + "/" + line.getLineId() + ".filtered");
                ready.add(line);
                done.incrementAndGet();
                System.out.println("Task completed for line: " + line.getLineId() + ". Total done: " + done.get()
                        + " . Queu state : " + ready.size());
                Thread.sleep(100);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        };
    }
}
