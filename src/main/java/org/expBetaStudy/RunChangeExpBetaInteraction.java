package org.expBetaStudy;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.mutable.MutableInt;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RunChangeExpBetaInteraction {
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
        double[] gammas = new double[]{0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};

        // level of uncertainty on modeB
        double sigma = 3;
        // difference between the base score of the two modes
        double[] deltas = new double[]{0};
        //        double[] deltas = new double[]{-10, -5, -2, -1, -0.5, -0.2, -0.1, -0.0001, 0, 0.0001, 0.1, 0.2, 0.5, 1, 2, 5, 10};
//
        // strategy setup
        double proportionToSwitchOffInnovation = 0.333;
        double modeInnovation = 0.1;

        // output directory
        String outputDirectoryRoot = "/Users/luchengqi/Desktop/ChangeExpBeta-test/illu-output/msa-analysis/ChangeExpBeta-with-interaction";

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
                intermediateResultsWriter.printRecord("iter", "modeA", "modeB", "modeA_score", "modeB_score");

                // initialization
                Random random = new Random(seed);
                List<Person> persons = Person.createInitialPopulation(numPersons, initialModeBShare, random);
                // initialize memory observer
                AgentsMemoryObserver agentsMemoryObserver = new AgentsMemoryObserver(singleRunFolder, memorySize, persons);

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
                        modesCount.get(person.selectedPlan.mode).increment();
                    }

                    modeAShare = modesCount.get(Mode.modeA).doubleValue() / numPersons;
                    modeBShare = modesCount.get(Mode.modeB).doubleValue() / numPersons;

                    // assume 1 minute = 0.1 (score)
                    double modeAScore = -0.1 * (10 + 30 * modeAShare);
                    double modeBScore = -0.1 * (15 + 10 * (1 - modeAShare));
                    // target equilibrium point: modeAShare = 0.375, modeBShare = 0.625

                    for (Person person : persons) {
                        if (person.selectedPlan.mode == Mode.modeA) {
                            double currentScore = modeAScore;
                            // apply MSA
                            if (useMSA && iteration >= numIterations * proportionToSwitchOffInnovation) {
                                currentScore = msa.applyMSA(person.selectedPlan, currentScore);
                            }
                            person.selectedPlan.score = currentScore;
                        } else if (person.selectedPlan.mode == Mode.modeB) {
                            double currentScore = modeBScore;
                            // apply MSA
                            if (useMSA && iteration >= numIterations * proportionToSwitchOffInnovation) {
                                currentScore = msa.applyMSA(person.selectedPlan, currentScore);
                            }
                            person.selectedPlan.score = currentScore;
                        }
                    }

                    // write down mode share for the iteration
                    intermediateResultsWriter.printRecord(
                            Integer.toString(iteration),
                            Double.toString(modeAShare),
                            Double.toString(modeBShare),
                            Double.toString(modeAScore),
                            Double.toString(modeBScore)
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

                    // analyze the number of mode B plans in the memory
                    agentsMemoryObserver.analyze(persons, memorySize, iteration);

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
                agentsMemoryObserver.printResults();

                // write overall analysis entry
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
