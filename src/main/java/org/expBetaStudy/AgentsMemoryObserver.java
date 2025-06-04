package org.expBetaStudy;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.mutable.MutableInt;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

class AgentsMemoryObserver {
    private final String singleRunOutputDirectory;
    private final int memorySize;
    private final List<Person> persons;

    private final Map<Integer, Map<Integer, MutableInt>> modeBMemoryCounterMap = new LinkedHashMap<>();
    private final Map<Integer, Double> modeShareMap = new HashMap<>();

    AgentsMemoryObserver(String singleRunOutputDirectory, int memorySize, List<Person> persons) throws IOException {
        this.singleRunOutputDirectory = singleRunOutputDirectory;
        this.memorySize = memorySize;
        this.persons = persons;
    }

    void printResults() throws IOException {
        // print title row
        CSVPrinter memoryAnalysisWriter =
                new CSVPrinter(new FileWriter(singleRunOutputDirectory + "/memory-analysis.tsv"), CSVFormat.TDF);
        List<String> titleRow = new ArrayList<>();
        titleRow.add("iteration");
        for (int i = 0; i <= memorySize; i++) {
            titleRow.add(i + "_sto_plans");
        }
        titleRow.add("expected_final_sto_mode_share");
        titleRow.add("actual_sto_mode_share");
        memoryAnalysisWriter.printRecord(titleRow);

        // print results rows
        for (Map.Entry<Integer, Map<Integer, MutableInt>> entry : modeBMemoryCounterMap.entrySet()) {
            int iteration = entry.getKey();
            Map<Integer, MutableInt> countsMap = entry.getValue();
            List<String> resultRow = new ArrayList<>();
            resultRow.add(Integer.toString(iteration));

            double expectedFinalModeShare = 0;
            for (int i = 0; i <= memorySize; i++) {
                double share = countsMap.get(i).doubleValue() / persons.size();
                resultRow.add(Double.toString(share));
                expectedFinalModeShare += share * i / memorySize;
            }
            resultRow.add(Double.toString(expectedFinalModeShare));
            resultRow.add(Double.toString(modeShareMap.get(iteration)));
            memoryAnalysisWriter.printRecord(resultRow);
        }
        memoryAnalysisWriter.close();
    }

    void analyze(List<Person> persons, int memorySize, int iteration) {
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

        modeBMemoryCounterMap.put(iteration, countsMap);
        modeShareMap.put(iteration, selectedModeBCount/persons.size());
    }
}
