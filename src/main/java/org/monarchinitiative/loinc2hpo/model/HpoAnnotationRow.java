package org.monarchinitiative.loinc2hpo.model;

import org.monarchinitiative.phenol.ontology.data.TermId;

public class HpoAnnotationRow {
    private final String loincType;
    private final TermId hpoTermId;
    private final String hpoTermLabel;


    public HpoAnnotationRow(String loincType, TermId tid, String termLabel) {
        this.loincType = loincType;
        this.hpoTermId = tid;
        this.hpoTermLabel = termLabel;
    }

    public static HpoAnnotationRow qnLow(TermId tid, String name) {
        return new HpoAnnotationRow("QN-LOW", tid, name);
    }

    public static HpoAnnotationRow qnHigh(TermId tid, String name) {
        return new HpoAnnotationRow("QN-HIGH", tid, name);
    }

    public static HpoAnnotationRow normal(TermId tid, String name) {
        return new HpoAnnotationRow("NORMAL", tid, name);
    }

    public static HpoAnnotationRow ordAbnormal(TermId tid, String name) {
        return new HpoAnnotationRow("ORD-ABNORMAL", tid, name);
    }

    public static HpoAnnotationRow nominal(TermId tid, String name) {
        return new HpoAnnotationRow("NOMINAL", tid, name);
    }

    public String getLoincType() {
        return loincType;
    }

    public TermId getHpoTermId() {
        return hpoTermId;
    }

    public String getHpoTermLabel() {
        return hpoTermLabel;
    }


    @Override
    public String toString() {
        return loincType + " - " + hpoTermId.getValue() + " - " + hpoTermLabel;
    }
}
