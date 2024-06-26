package com.stage.neuroPsi.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Line {
    @Id
    private String lineId;

    private String path;

    private String project;

    private String season;

    private Double latitud;

    private Double longitud;

    private String localité;

    private String origineLab;

    private String pays;

    private String webLink;

    public Line(String lineId) {
        this.lineId = lineId;
    }

    public Line(String path, Double latitud, Double longitud, String localité, String origineLab, String pays,
            String webLink) {
        this.path = path;
        this.latitud = latitud;
        this.longitud = longitud;
        this.localité = localité;
        this.origineLab = origineLab;
        this.pays = pays;
        this.webLink = webLink;
    }

    public Line(Line l) {
        this.lineId = l.lineId;
        this.path = l.path;
        this.latitud = l.latitud;
        this.longitud = l.longitud;
        this.localité = l.localité;
        this.origineLab = l.origineLab;
        this.pays = l.pays;
        this.webLink = l.webLink;
    }

}
