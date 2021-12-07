package org.monarchinitiative.loinc2hpo.model;

import org.monarchinitiative.loinc2hpocore.annotation.*;
import org.monarchinitiative.loinc2hpocore.codesystems.Outcome;
import org.monarchinitiative.loinc2hpocore.loinc.LoincId;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is a class that contains information about the current Annotation as the user enters it.
 * It is intended to be used by the main controller.
 */
@Component()
public class CurrentAnnotationModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrentAnnotationModel.class);

    enum LoincType { QN, ORD, NOM, UNKNOWN }
    /** Normal -- for QN */
    private Term qnNormal = null;

    private Term qnLow = null;
    private Term qnHigh = null;

    private Term ordPositive = null;
    /** Normal -- for ORD */
    private Term ordNegative = null;

    private Term nominal = null;


    public CurrentAnnotationModel() {

    }

    public void reset() {
        qnNormal = null;
        qnLow = null;
        qnHigh = null;
        ordPositive = null;
        ordNegative = null;
        nominal = null;
        LOGGER.info("Reset the CurrentAnnotationModel");
    }

    /**
     * This method is activated when the user deletes a row from the HPO annotation table.
     * Since we do not know from that which term is affected, we check all of them and set terms
     * to null of the TermId matches.
     * @param hpoId and HPO term to be deleted from the annotations.
     */
    public void deleteSelectedTermId(TermId hpoId) {
        if (qnLow != null && qnLow.getId().equals(hpoId)) qnLow = null;
        if (qnNormal != null && qnNormal.getId().equals(hpoId)) qnNormal = null;
        if (qnHigh != null && qnHigh.getId().equals(hpoId)) qnHigh = null;
        if (ordPositive != null && ordPositive.getId().equals(hpoId)) ordPositive = null;
        if (ordNegative != null && ordNegative.getId().equals(hpoId)) ordNegative = null;
        if (nominal != null && nominal.getId().equals(hpoId)) nominal = null;
        LOGGER.info("Deleting {} from current model (if exists)", hpoId.getValue());
    }

    /**
     * For Qn, the only required annotation is normal, i.e., low and high can be missing. In any case it is
     * not correct to have any ordinal or nominal annotation.
     * @return True if the annotations are valid.
     */
    public boolean validQnAnnotation() {
        return qnNormal != null &&
                ordPositive == null &&
                ordNegative == null &&
                nominal == null;
    }

    public boolean validOrdAnnotation(){
        boolean b =  qnLow == null && qnNormal == null && qnHigh == null &&  nominal == null &&
                ordNegative != null &&
                ordPositive != null ;
        return b;

    }

    public boolean validNomAnnotation(){
        return qnLow == null && qnNormal == null && qnHigh == null && ordPositive == null && ordNegative == null &&
                nominal != null;
    }

    public Term getOrdinalNegative() {
        return ordNegative;
    }

    public void setOrdinalNegative(Term normal) {
        this.ordNegative = normal;
    }

    public Term getQnLow() {
        return qnLow;
    }

    public void setQnLow(Term qnLow) {
        this.qnLow = qnLow;
    }

    public Term getQnHigh() {
        return qnHigh;
    }

    public void setQnHigh(Term qnHigh) {
        this.qnHigh = qnHigh;
    }

    public Term getQnNormal() {
        return qnNormal;
    }

    public void setQnNormal(Term qnNormal) {
        this.qnNormal = qnNormal;
    }

    public Term getOrdPositive() {
        return ordPositive;
    }

    public void setOrdPositive(Term ordPositive) {
        this.ordPositive = ordPositive;
    }

    public Term getOrdNegative() {
        return ordNegative;
    }

    public void setOrdNegative(Term ordNegative) {
        this.ordNegative = ordNegative;
    }

    public Term getOrdinalPositive() {
        return ordPositive;
    }

    public void setOrdinalPositive(Term ordPositive) {
        this.ordPositive = ordPositive;
    }

    public Term getNominal() {
        return nominal;
    }

    public void setNominal(Term nominal) {
        this.nominal = nominal;
    }

    public Optional<LoincAnnotation> quantitative(LoincId loincCode, String biocuration, String comment) {
        if (! validQnAnnotation()) {
            LOGGER.info("Unable to create quantitative LoincAnnotation - currentModel is not valid Qn");
            return Optional.empty();
        }
        Loinc2HpoAnnotation normalAnnot = new Loinc2HpoAnnotation(loincCode,
                LoincScale.QUANTITATIVE,
                Outcome.NORMAL(),
                qnNormal.getId(),
                biocuration,
                comment);

        Map<Outcome, Loinc2HpoAnnotation> outcomeMap = new HashMap<>();
        outcomeMap.put(Outcome.NORMAL(), normalAnnot);
        if (qnLow != null) {
            Loinc2HpoAnnotation lowAnnot = new Loinc2HpoAnnotation(loincCode,
                    LoincScale.QUANTITATIVE,
                    Outcome.LOW(),
                    qnLow.getId(),
                    biocuration,
                    comment);
            outcomeMap.put(Outcome.LOW(), lowAnnot);
        }
        if (qnHigh != null) {
            Loinc2HpoAnnotation highAnnot = new Loinc2HpoAnnotation(loincCode,
                    LoincScale.QUANTITATIVE,
                    Outcome.HIGH(),
                    qnHigh.getId(),
                    biocuration,
                    comment);
            outcomeMap.put(Outcome.HIGH(), highAnnot);
        }
        LOGGER.info("Returning optional of Quantitative LoincAnnotation {}", outcomeMap);
        return Optional.of(QuantitativeLoincAnnotation.fromOutcomeMap(outcomeMap));
    }

    public Optional<LoincAnnotation> ordinal(LoincId loincCode, String biocuration, String comment) {
        if (! validOrdAnnotation()) return Optional.empty();
        Loinc2HpoAnnotation abnormalAnnot = new Loinc2HpoAnnotation(loincCode,
                LoincScale.ORDINAL,
                Outcome.POSITIVE(),
                ordPositive.getId(),
                biocuration,
                comment);
        Loinc2HpoAnnotation normalAnnot = new Loinc2HpoAnnotation(loincCode,
                LoincScale.ORDINAL,
                Outcome.NEGATIVE(),
                ordNegative.getId(),
                biocuration,
                comment);
        LoincAnnotation ordinalAnnot = new OrdinalHpoAnnotation(normalAnnot, abnormalAnnot);
        return Optional.of(ordinalAnnot);
    }


}
