package org.monarchinitiative.loinc2hpo.model.codesystems;


import org.h2.command.ddl.CreateTable;
import org.monarchinitiative.loinc2hpo.except.Loinc2HpoRunTimeException;

public enum InternalCode {

    A,
    L,
    N,
    H,
    NP,
    P,
    U,
    NOM,
    NEG,
    POS;

    public static OutcomeCode fromString(String codeString) throws Loinc2HpoRunTimeException  {
        return switch (codeString) {
            case "A" -> ABNORMAL;
            case "L" -> LOW;
            case "N" -> NORMAL;
            case "H" -> HIGH;
            case "NP", "NEG" -> NOT_PRESENT;
            case "NOM" -> NOMINAL;
            case "P", "POS" -> PRESENT;
            default -> throw new Loinc2HpoRunTimeException("Did not recognize internal code: \"" + codeString + "\"");
        };
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
            case NOM -> "NOM";
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
            case NOM -> "nominal";
        };
    }

    public final static OutcomeCode ABNORMAL = new OutcomeCode(InternalCode.A, "abnormal");
    public final static OutcomeCode LOW = new OutcomeCode(InternalCode.L, "below normal range");
    public final static OutcomeCode HIGH = new OutcomeCode(InternalCode.H, "above normal range");
    public final static OutcomeCode NORMAL = new OutcomeCode(InternalCode.L, "normal");
    public final static OutcomeCode NOT_PRESENT = new OutcomeCode(InternalCode.NP, "not present");
    public final static OutcomeCode PRESENT = new OutcomeCode(InternalCode.P, "present");
    public final static OutcomeCode NOMINAL = new OutcomeCode(InternalCode.NOM, "nominal");
}
