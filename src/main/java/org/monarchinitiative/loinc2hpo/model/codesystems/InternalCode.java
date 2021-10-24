package org.monarchinitiative.loinc2hpo.model.codesystems;


import org.monarchinitiative.loinc2hpo.except.Loinc2HpoRunTimeException;

public enum InternalCode {

    A,
    L,
    N,
    H,
    NP,
    P,
    U,
    NEG,
    POS;

    public static InternalCode fromCode(String codeString) throws Loinc2HpoRunTimeException  {
        if (codeString == null || codeString.isEmpty()) {
            return null;
        }
        if (codeString.equals("A")) {
            return A;
        }
        if (codeString.equals("L")) {
            return L;
        }
        if (codeString.equals("N")) {
            return N;
        }
        if (codeString.equals("H")) {
            return H;
        }
        if (codeString.equals("NP")) {
            return NEG;
        }
        if (codeString.equals("P")) {
            return POS;
        }
        if (codeString.equals("U")) {
            return U;
        }
        if (codeString.equals("POS")) {
            return POS;
        }
        if (codeString.equals("NEG")) {
            return NEG;
        }
        throw new Loinc2HpoRunTimeException("Cannot recognize the code: " + codeString);
    }

    public String toCodeString(){
        switch(this) {
            case A: return "A";
            case L: return "L";
            case N: return "N";
            case H: return "H";
            case NP:
            case NEG: return "NEG";
            case P:
            case POS: return "POS";
            case U: return "U";
            default: return "?";
        }
    }

    public String getDisplay(){
        switch(this){
            case A: return "abnormal";
            case L: return "below normal range";
            case N: return "within normal range";
            case H: return "above normal range";
            case NP: return "not present";
            case P: return "present";
            case U: return "unknown code";
            case NEG: return "not present";
            case POS: return "present";
            default: return "?";
        }
    }
}