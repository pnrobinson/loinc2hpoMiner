package org.monarchinitiative.loinc2hpo.model;

import org.monarchinitiative.phenol.ontology.data.Term;

/**
 * This is a class that contains information about the current Annotation as the user enters it
 */
public class CurrentAnnotationModel {


    enum LoincType { QN, ORD, NOM, UNKNOWN }
    /** Normal -- for QN or ORD */
    private Term normal = null;

    private Term qnLow = null;
    private Term qnHigh = null;

    private Term ordAbnormal = null;

    private Term nominal = null;


    public CurrentAnnotationModel() {

    }

    public void reset() {
        normal = null;
        qnLow = null;
        qnHigh = null;
        ordAbnormal = null;
        nominal = null;
    }

    /**
     * For Qn, the only required annotation is normal, i.e., low and high can be missing. In any case it is
     * not correct to have any ordinal or nominal annotation.
     * @return True if the annotations are valid.
     */
    public boolean validQnAnnotation() {
        return normal != null && ordAbnormal == null && nominal == null;
    }

    public boolean validOrdAnnotation(){
        return qnLow == null && normal != null && qnHigh == null && ordAbnormal != null && nominal == null;
    }

    public boolean validNomAnnotation(){
        return qnLow == null && normal == null && qnHigh == null && ordAbnormal == null && nominal != null;
    }

    public Term getNormal() {
        return normal;
    }

    public void setNormal(Term normal) {
        this.normal = normal;
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

    public Term getOrdAbnormal() {
        return ordAbnormal;
    }

    public void setOrdAbnormal(Term ordAbnormal) {
        this.ordAbnormal = ordAbnormal;
    }

    public Term getNominal() {
        return nominal;
    }

    public void setNominal(Term nominal) {
        this.nominal = nominal;
    }
}
