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
        return switch (this) {
            case A -> "A";
            case L -> "L";
            case N -> "N";
            case H -> "H";
            case NP, NEG -> "NEG";
            case P, POS -> "POS";
            case U -> "U";
        };
    }

    public String getDisplay(){
        return switch (this) {
            case A -> "abnormal";
            case L -> "below normal range";
            case N -> "within normal range";
            case H -> "above normal range";
            case NP -> "not present";
            case P -> "present";
            case U -> "unknown code";
            case NEG -> "not present";
            case POS -> "present";
        };
    }
}
