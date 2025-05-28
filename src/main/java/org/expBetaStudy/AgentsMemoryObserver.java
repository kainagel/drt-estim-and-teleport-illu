package org.expBetaStudy;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.mutable.MutableInt;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class AgentsMemoryObserver {
    private final String singleRunOutputDirectory;

    AgentsMemoryObserver(String singleRunOutputDirectory) {
        this.singleRunOutputDirectory = singleRunOutputDirectory;
    }

    void analyze(List<Person> persons, int memorySize) throws IOException {
        System.out.println("Performing analysis...");
        Map<Integer, MutableInt> countsMap = new HashMap<>();
        for (int i = 0; i <= memorySize; i++) {
            countsMap.put(i, new MutableInt());
        }

        double selectedModeBCount = 0;
        for (Person person : persons) {
            int modeBCount = 0;
            for (Plan plan : person.plans) {
                if (plan.mode == Mode.modeB) {
                    modeBCount++;
                }
            }
            countsMap.get(modeBCount).increment();

            if (person.selectedPlan.mode == Mode.modeB) {
                selectedModeBCount++;
            }
        }

        // write down results
        CSVPrinter memoryAnalysisWriter =
                new CSVPrinter(new FileWriter(singleRunOutputDirectory + "/memory-analysis.tsv"), CSVFormat.TDF);
        List<String> titleRow = new ArrayList<>();
        for (int i = 0; i <= memorySize; i++) {
            titleRow.add(i + "_sto_plans");
        }
        titleRow.add("expected_final_sto_mode_share");
        titleRow.add("actual_sto_mode_share");
        memoryAnalysisWriter.printRecord(titleRow);

        List<String> resultRow = new ArrayList<>();
        double expectedFinalModeShare = 0;
        for (int i = 0; i <= memorySize; i++) {
            double share = countsMap.get(i).doubleValue() / persons.size();
            resultRow.add(Double.toString(share));
            expectedFinalModeShare += share * i / memorySize;
        }
        resultRow.add(Double.toString(expectedFinalModeShare));
        resultRow.add(Double.toString(selectedModeBCount / persons.size()));
        memoryAnalysisWriter.printRecord(resultRow);
        memoryAnalysisWriter.close();
    }
}
