package org.monarchinitiative.loinc2hpo.model.codesystems;

import java.util.HashMap;
import java.util.Map;

public class InternalCodeSystem {
    private static final Map<InternalCode, OutcomeCode> internalCodeMap;

    static {
        internalCodeMap = new HashMap<>();
        internalCodeMap.put(InternalCode.A, new OutcomeCode( InternalCode.A, "abnormal"));
        internalCodeMap.put(InternalCode.L, new OutcomeCode(InternalCode.L, "low"));
        internalCodeMap.put(InternalCode.N, new OutcomeCode(InternalCode.N, "normal"));
        internalCodeMap.put(InternalCode.H, new OutcomeCode(InternalCode.H, "high"));
        internalCodeMap.put(InternalCode.U, new OutcomeCode(InternalCode.U, "unknown"));
        internalCodeMap.put(InternalCode.NEG, new OutcomeCode(InternalCode.NEG, "absent"));
        internalCodeMap.put(InternalCode.POS, new OutcomeCode(InternalCode.POS,"present"));
    }

    public static OutcomeCode getCode(InternalCode internalCode){
        return internalCodeMap.get(internalCode);
    }

    public static OutcomeCode abnormal() {
        return internalCodeMap.get(InternalCode.A);
    }

}
