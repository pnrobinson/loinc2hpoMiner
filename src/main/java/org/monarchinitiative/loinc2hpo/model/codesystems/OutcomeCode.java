package org.monarchinitiative.loinc2hpo.model.codesystems;


import java.io.Serializable;
import java.util.Objects;

/**
 * This is an class for coded values. This correspond to the Coding class in hapi-fhir with some modification (equal method)
 */
public class OutcomeCode {
    private InternalCode code;
    private String display;



    public OutcomeCode(InternalCode code, String display){
        this.code = code;
        this.display = display;
    }

    public OutcomeCode(OutcomeCode otherCode) {
        this.code = otherCode.code;
        this.display = otherCode.display;
    }

    public InternalCode getCode() {
        return code;
    }


    public String getDisplay() {
        return display;
    }




    @Override
    public boolean equals(Object obj){
        if (obj instanceof OutcomeCode other) {
            return other.getCode().equals(this.code);
        }
        return false;
    }
    @Override
    public int hashCode(){
        return Objects.hash(this.code);
    }

    @Override
    public String toString(){
        return String.format("Code: %s, Display: %s", code, display);
    }
}
