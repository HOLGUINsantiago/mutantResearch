package com.stage.neuroPsi.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlignResults implements Comparable<AlignResults> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "nom")
    private Sequence mutation;

    @ManyToOne
    @JoinColumn(name = "line_id")
    private Line line;

    private double identity;

    private int alignmentLength;

    private int refStart;

    private int lineStart;

    private int refEnd;

    private int lineEnd;

    private String eValue;

    private String readId;

    private boolean wt;

    private int mismatchs;

    private int gaps;

    public AlignResults(Sequence mutation, Line line, String readId, double identity, int alignmentLength,
            int refStart,
            int lineStart,
            int refEnd, int lineEnd, String eValue, boolean wt,
            int mismatchs,
            int gaps) {
        this.identity = identity;
        this.mutation = mutation;
        this.readId = readId;
        this.line = line;
        this.alignmentLength = alignmentLength;
        this.refStart = refStart;
        this.lineStart = lineStart;
        this.refEnd = refEnd;
        this.lineEnd = lineEnd;
        this.eValue = eValue;
        this.wt = wt;
        this.mismatchs = mismatchs;
        this.gaps = gaps;
    }

    // retourne un score pondéré
    public double score() {
        int mutantMaxLength = mutation.getListeRef().get(mutation.getListeRef().size() - 1).length();
        int wtMaxLength = mutation.getListeRef().get(0).length();
        double min = wt ? (double) wtMaxLength : (double) mutantMaxLength;
        return identity * ((double) alignmentLength / min) - gaps;
    }

    // retourne le meilleure alignement
    @Override
    public int compareTo(AlignResults o) {
        int res = Double.compare(this.score(), o.score());
        if (res != 0)
            return res;

        res = Integer.compare(this.gaps, o.getGaps());

        if (res != 0)
            return -res;

        res = Integer.compare(this.mismatchs, o.getMismatchs());

        if (res != 0)
            return -res;

        return this.isWt() && !o.isWt() ? 1 : -1;
    }

    // Retourne la taille mnimmale des séquences referentielles
    public int minRef() {
        int min = 0;
        for (String seq : mutation.getListeRef()) {
            if (seq.length() < min || min == 0)
                min = seq.length();
        }
        return min;
    }

    // Renvoie true en cas d'alignement en zone de mutation
    public boolean isInInterestPosition() {
        if (!(mutation instanceof MultipleMutation))
            return isForward() ? getRefStart() < 18 && getRefEnd() > 36 : getRefEnd() < 18 && getRefStart() > 36;

        MultipleMutation mm = (MultipleMutation) mutation;

        return isWt() ? getRefStart() < mm.getMutIndiceStart() - 5 && getRefEnd() > mm.getMutIndiceEnd() + 5
                : getRefStart() < mm.getMutIndiceStart() - 5 && getRefEnd() > mm.getMutIndiceEnd() + 5;

    }

    // Renvoie true si l'alignement est orienté forward
    public boolean isForward() {
        return (refEnd - refStart) * (lineEnd - lineStart) >= 0;
    }
}
