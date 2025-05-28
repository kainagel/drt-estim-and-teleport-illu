package org.expBetaStudy;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.mutable.MutableInt;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RunChangeExpBeta {
    public static void main(String[] args) throws IOException {
        // key parameters
        long seed = 1;
        int numIterations = 3000;
        int numPersons = 10000;
        double initialModeBShare = 0.;
        int memorySize = 5;

        // MSA
        boolean useMSA = true;

        // gamma for change exp beta
        // double[] gammas = new double[]{0.01}
        double[] gammas = new double[]{0.01, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};

        // level of uncertainty on modeB
        double sigma = 0.3;
        // difference between the base score of the two modes
        double[] deltas = new double[]{0};
        //        double[] deltas = new double[]{-10, -5, -2, -1, -0.5, -0.2, -0.1, -0.0001, 0, 0.0001, 0.1, 0.2, 0.5, 1, 2, 5, 10};
//
        // strategy setup
        double proportionToSwitchOffInnovation = 0.333;
        double modeInnovation = 0.1;

        // output directory
        String outputDirectoryRoot = "/Users/luchengqi/Desktop/ChangeExpBeta-test/illu-output/msa-analysis/ChangeExpBeta-automated";

        // ----------------------- No need to change from here --------------------------------------------------------
        // Main part from here
        String outputDirectory = outputDirectoryRoot + "/ChangeExpBeta-sigma_" + sigma;
        if (useMSA) {
            outputDirectory = outputDirectory + "-" + MSA.WITH_MSA;
        }

        Path outputFolderPath = Path.of(outputDirectory);
        if (!Files.exists(outputFolderPath)) {
            Files.createDirectories(outputFolderPath);
        }

        CSVPrinter mainStatsWriter = new CSVPrinter(new FileWriter(outputDirectory + "/main-stats.tsv"), CSVFormat.TDF);
        mainStatsWriter.printRecord("delta", "modeA", "modeB", "sigma", "memory_size", "gamma");
        for (double delta : deltas) {
            for (double gamma : gammas) {
                System.out.printf("Simulating for case: Delta = %.1f, gamma = %.2f%n", delta, gamma);
                String singleRunFolder = outputDirectory + "/intermediate-results/delta_" + delta + "-gamma_" + gamma;
                Files.createDirectories(Path.of(singleRunFolder));

                // write down a tsv file for intermediate results:
                CSVPrinter intermediateResultsWriter =
                        new CSVPrinter(new FileWriter(singleRunFolder + "/mode-choice-evolution.tsv"), CSVFormat.TDF);
                intermediateResultsWriter.printRecord("iter", "modeA", "modeB");

                // initialization
                Random random = new Random(seed);
                List<Person> persons = Person.createInitialPopulation(numPersons, initialModeBShare, random);

                // simulation
                double modeAShare = 0;
                double modeBShare = 0;
                MSA msa = new MSA();
                for (int iteration = 0; iteration <= numIterations; iteration++) {
                    Map<Mode, MutableInt> modesCount = new HashMap<>();
                    for (Mode mode : Mode.values()) {
                        modesCount.put(mode, new MutableInt());
                    }
                    for (Person person : persons) {
                        // "simulate" and score the plans
                        if (person.selectedPlan.mode == Mode.modeA) {
                            person.selectedPlan.score = 0;
                        } else if (person.selectedPlan.mode == Mode.modeB) {
                            double simulatedScore = delta + random.nextGaussian() * sigma;
                            // apply MSA
                            if (iteration >= numIterations * proportionToSwitchOffInnovation && useMSA) {
                                simulatedScore = msa.applyMSA(person.selectedPlan, simulatedScore);
                            }
                            person.selectedPlan.score = simulatedScore;
                        }
                        modesCount.get(person.selectedPlan.mode).increment();
                    }

                    // write down mode share for the iteration
                    modeAShare = modesCount.get(Mode.modeA).doubleValue() / numPersons;
                    modeBShare = modesCount.get(Mode.modeB).doubleValue() / numPersons;
                    intermediateResultsWriter.printRecord(
                            Integer.toString(iteration),
                            Double.toString(modeAShare),
                            Double.toString(modeBShare)
                    );

                    // remove the worst plan if max memory size is exceeded
                    for (Person person : persons) {
                        if (person.plans.size() > memorySize) {
                            Plan worstPlan = person.getWorstPlan();
                            person.plans.remove(worstPlan);
                            // if the selected plan is removed, then set one of the random plans left as the selected plan
                            if (person.selectedPlan == worstPlan) {
                                person.selectedPlan = person.plans.get(random.nextInt(person.plans.size()));
                            }
                        }
                    }

                    // re-planning
                    if (iteration < numIterations) {
                        for (Person person : persons) {
                            if (iteration <= numIterations * proportionToSwitchOffInnovation && random.nextDouble() < modeInnovation) {
                                person.performModeInnovation(random);
                            } else {
                                person.changeExpBetaMATSimImpl(random, gamma);
                            }
                        }
                    }
                }
                intermediateResultsWriter.close();

                // analyze the number of mode B plans in the memory
                new AgentsMemoryObserver(singleRunFolder).analyze(persons, memorySize);

                // overall analysis
                mainStatsWriter.printRecord(
                        Double.toString(delta),
                        Double.toString(modeAShare),
                        Double.toString(modeBShare),
                        Double.toString(sigma),
                        Double.toString(memorySize),
                        Double.toString(gamma)
                );
            }
        }
        mainStatsWriter.close();
    }
}
