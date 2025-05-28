package org.expBetaStudy;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.HashMap;
import java.util.Map;

public class MSA {
    final static String WITH_MSA = "with_msa";

    private final Map<Plan, Double> msaScoresOfPlans = new HashMap<>();
    private final Map<Plan, MutableInt> counterMap = new HashMap<>();

    double applyMSA(Plan plan, double currentScore) {
        if (!counterMap.containsKey(plan)) {
            // the plan is executed for the first time after MSA is switched on
            // → write it down in the map and return the original score
            counterMap.put(plan, new MutableInt(1));
            msaScoresOfPlans.put(plan, currentScore);
            return currentScore;
        }

        double ratio = 1 - counterMap.get(plan).doubleValue() / (counterMap.get(plan).doubleValue() + 1);
        double updatedMsaScore = currentScore * ratio + msaScoresOfPlans.get(plan) * (1 - ratio);
        counterMap.get(plan).increment();
        msaScoresOfPlans.put(plan, updatedMsaScore);
        return updatedMsaScore;
    }
}
