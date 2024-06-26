package com.stage.neuroPsi.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.*;
import com.stage.neuroPsi.models.*;
import com.stage.neuroPsi.service.*;
import java.io.*;

// Initiate de application
@Configuration
public class LoadDatabase {

	@Value("${file.upload-dir}")
	private String lines_path;

	@Bean
	CommandLineRunner initDatabase(MutationsService mutationsService, LineService lineService) {
		return args -> {
			// Chargement et lecture des genes existants chez la drosoph
			List<Gene> genes = new ArrayList<>();
			File geneMap = new File("/app/src/main/resources/static/genes/gene_map_table_fb_2024_02.tsv");

			try (BufferedReader br = new BufferedReader(
					new FileReader(geneMap))) {
				String line;
				while ((line = br.readLine()) != null) {
					if (!line.startsWith("#") && !line.equals("")) {
						String[] fields = line.split("\t");
						Gene gene = createGeneFromFields(fields);
						if (gene != null)
							genes.add(gene); // Ajouter le gene dans la liste
					}

				}
			}
			// Ajouter les genes a la base de données
			mutationsService.saveGenes(genes);

			// Création des mutations
			Deletion melted = new Deletion(7131000, 7131600, "chr3L", "melted", 7131328, 7131542,
					Set.of(new Gene("melt")));

			Deletion delta26 = new Deletion(12008500, 12008700, "chr2L", "delta26", 12008598, 12008624,
					Set.of(new Gene("Rh5")));

			Substitution q365 = new Substitution(12009500, 12009800, "chr2L", "q365", 12009649, 'T',
					Set.of(new Gene("Rh5")));

			Deletion ral551 = new Deletion(11309050, 11309280, "chr3R", "ral551", 11309156, 11309179,
					Set.of(new Gene("Rh6")));

			Deletion edn = new Deletion(11309250, 11309400, "chr3R", "Edn", 11309305, 11309314,
					Set.of(new Gene("Rh6")));

			Deletion nyx1 = new Deletion(6068150, 6068300, "chr2L", "nyx1", 6068232, 6068236,
					Set.of(new Gene("CG9150")));

			MultipleMutation nyx2 = new MultipleMutation(13835100, 13835350, "chr3L", "nyx2",
					List.of(new Deletion(13835100, 13835350, "chr3L", "nyx2_d1", 13835221, 13835257,
							Set.of(new Gene("CG8757"))),

							new Deletion(13835100, 13835350, "chr3L", "nyx2_d2", 13835264,
									13835285,
									Set.of(new Gene("CG8757")))),
					Set.of(new Gene("CG8757")));

			// ajout des premières mutations
			if (mutationsService.getMutation(melted.getNom()) == null)
				mutationsService.saveMutation(melted);
			if (mutationsService.getMutation(delta26.getNom()) == null)
				mutationsService.saveMutation(delta26);
			if (mutationsService.getMutation(q365.getNom()) == null)
				mutationsService.saveMutation(q365);
			if (mutationsService.getMutation(ral551.getNom()) == null)
				mutationsService.saveMutation(ral551);
			if (mutationsService.getMutation(edn.getNom()) == null)
				mutationsService.saveMutation(edn);
			if (mutationsService.getMutation(nyx1.getNom()) == null)
				mutationsService.saveMutation(nyx1);
			if (mutationsService.getMutation(nyx2.getNom()) == null)
				mutationsService.saveMultipleMutation(nyx2);

		};
	}

	private Gene createGeneFromFields(String[] fields) {
		if (fields.length > 5) {
			// extraction de l'information d'un gène
			String nom = fields[1];
			String flyBaseId = fields[2];
			String localisationRecombinaison = fields[3];
			String localisationCytogenetique = fields[4];

			String chromosome = fields[5].split(":")[0];

			String rangeString = fields[5].split(":")[1];
			int debut = Integer.parseInt(rangeString.split("\\.\\.")[0]);
			int fin = Integer.parseInt(rangeString.split("\\.\\.")[1].replaceAll("\\(.*\\)", ""));

			boolean forward = !rangeString.contains("-");

			return new Gene(nom, flyBaseId, localisationRecombinaison, localisationCytogenetique,
					chromosome, forward,
					debut, fin);
		}
		return null;

	}
}
