package org.expBetaStudy;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.mutable.MutableInt;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Simulator {
    static void simulateInteractionWithTwoModes(List<Person> population, CSVPrinter intermediateResultsWriter,
                                                int iteration) throws IOException {
        double populationSize = population.size();
        Map<Mode, MutableInt> modesCount = new HashMap<>();
        for (Mode mode : Mode.values()) {
            modesCount.put(mode, new MutableInt());
        }
        for (Person person : population) {
            modesCount.get(person.selectedPlan.mode).increment();
        }

        double modeAShare = modesCount.get(Mode.modeA).doubleValue() / populationSize;
        double modeBShare = modesCount.get(Mode.modeB).doubleValue() / populationSize;

        // assume 1 minute = 0.1 (score)
        double modeAScore = -0.1 * (10 + 30 * modeAShare);
        double modeBScore = -0.1 * (15 + 10 * (1 - modeAShare));
        // target equilibrium point: modeAShare = 0.375, modeBShare = 0.625

        for (Person person : population) {
            if (person.selectedPlan.mode == Mode.modeA) {
                person.selectedPlan.score = modeAScore;
            } else if (person.selectedPlan.mode == Mode.modeB) {
                person.selectedPlan.score = modeBScore;
            }
        }

        intermediateResultsWriter.printRecord(
                Integer.toString(iteration),
                Double.toString(modeAShare),
                Double.toString(modeBShare)
        );

    }
}
