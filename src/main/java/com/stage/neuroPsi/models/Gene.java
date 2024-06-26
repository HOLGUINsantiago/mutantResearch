package com.stage.neuroPsi.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Gene {

    @Id
    private String nom;

    private String flyBaseId;

    private String localisationRecombinaison;

    private String localisationCytogenetique;

    private String chromosome;

    private boolean forward;

    private int debut;

    private int fin;

    public Gene(String nom, String flyBaseId, String localisationRecombinaison, String localisationCytogenetique) {
        this.nom = nom;
        this.flyBaseId = flyBaseId;
        this.localisationRecombinaison = localisationRecombinaison;
        this.localisationCytogenetique = localisationCytogenetique;
    }

    public Gene(String nom) {
        this.nom = nom;
    }

}
