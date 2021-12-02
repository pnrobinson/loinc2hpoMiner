package org.monarchinitiative.loinc2hpo.model;

import org.monarchinitiative.loinc2hpocore.annotation.Loinc2HpoAnnotation;
import org.monarchinitiative.loinc2hpocore.codesystems.Outcome;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class HpoAnnotationRow {
    private static final Logger LOGGER = LoggerFactory.getLogger(HpoAnnotationRow.class);
    private final Outcome outcome;
    private final TermId hpoTermId;
    private final String hpoTermLabel;


    public HpoAnnotationRow(Outcome outcome, Term term) {
        this.outcome = outcome;
        this.hpoTermId = term.getId();
        this.hpoTermLabel = term.getName();
    }

    public static HpoAnnotationRow qnLow(Term term) {
        return new HpoAnnotationRow(Outcome.LOW(), term);
    }

    public static HpoAnnotationRow qnHigh(Term term) {
        return new HpoAnnotationRow(Outcome.HIGH(), term);
    }

    public static HpoAnnotationRow qnNormal(Term term) {
        return new HpoAnnotationRow(Outcome.NORMAL(), term);
    }

    public static HpoAnnotationRow ordPositive(Term term) {
        return new HpoAnnotationRow(Outcome.POSITIVE(), term);
    }

    public static HpoAnnotationRow ordNegative(Term term) {
        return new HpoAnnotationRow(Outcome.NEGATIVE(), term);
    }

    /**
     * @return row for a nominal annotation
     */
    public static HpoAnnotationRow nominal(Outcome nominalOutcome, Term term) {
        return new HpoAnnotationRow(nominalOutcome, term);
    }


    public Outcome getOutcome() {
        return outcome;
    }


    public TermId getHpoTermId() {
        return hpoTermId;
    }

    public String getHpoTermLabel() {
        return hpoTermLabel;
    }

    public static Optional<HpoAnnotationRow> fromLoinc2HpoAnnotation(Loinc2HpoAnnotation annot, Ontology ontology) {
        Outcome outcome = annot.getOutcome();
        TermId tid = annot.getHpoTermId();
        if (ontology.containsTerm(tid)) {
            Term term = ontology.getTermMap().get(tid);
            return Optional.of(new HpoAnnotationRow(outcome, term));
        } else {
            // should never happen
            LOGGER.error("Could not find HPO term for {}", tid.getValue());
            return Optional.empty();
        }
    }


    @Override
    public String toString() {
        return outcome.getOutcome() + " - " + hpoTermId.getValue() + " - " + hpoTermLabel;
    }
}
