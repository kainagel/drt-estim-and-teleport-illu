package org.expBetaStudy;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.mutable.MutableInt;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RunSimulation {
    public static void main(String[] args) throws IOException {
        // key parameters
        long seed = 1;
        int numIterations = 1000;
        int numPersons = 10000;
        int memorySize = 5;

        // level of uncertainty on modeB
        double sigma = 0.3;
        // difference between the base score of the two modes
//        double[] deltas = new double[]{-10, -5, -2, -1, -0.5, -0.2, -0.1, -0.0001, 0, 0.0001, 0.1, 0.2, 0.5, 1, 2, 5, 10};
        double[] deltas = new double[]{0};
//
        // strategy setup
        double proportionToSwitchOffInnovation = 0.75;
        double modeInnovation = 0.1;

        // output directory
        String outputDirectory = "/Users/luchengqi/Desktop/changeExpBetaTest/illu-output/ChangeExpBeta-sigma_0.3-1000-iters";

        Path outputFolderPath = Path.of(outputDirectory);
        if (!Files.exists(outputFolderPath)) {
            Files.createDirectories(outputFolderPath);
            Files.createDirectories(Path.of(outputDirectory + "/intermediate-results"));
        }

        CSVPrinter mainStatsWriter = new CSVPrinter(new FileWriter(outputDirectory + "/main-stats.tsv"), CSVFormat.TDF);
        mainStatsWriter.printRecord("delta", "modeA", "modeB", "sigma", "memory_size");
        for (double delta : deltas) {
            System.out.println("Simulating Delta: " + delta);
            // write down a tsv file for intermediate results:
            CSVPrinter intermediateResultsWriter =
                    new CSVPrinter(new FileWriter(outputDirectory + "/intermediate-results/delta-" + delta + ".tsv"), CSVFormat.TDF);
            intermediateResultsWriter.printRecord("iter", "modeA", "modeB");

            // initialization
            Random random = new Random(seed);
            List<Person> persons = new ArrayList<>();
            for (int i = 0; i < numPersons; i++) {
                persons.add(Person.createDefaultPerson());
            }

            // simulation
            double modeAShare = 0;
            double modeBShare = 0;
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
                        person.selectedPlan.score = delta + random.nextGaussian() * sigma;
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
                        if (person.selectedPlan == worstPlan){
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
                            person.changeExpBetaMATSimImpl(random);
//                            person.selectExpBeta(random);
                        }
                    }
                }
            }

            intermediateResultsWriter.close();

            // overall analysis
            mainStatsWriter.printRecord(
                    Double.toString(delta),
                    Double.toString(modeAShare),
                    Double.toString(modeBShare),
                    Double.toString(sigma),
                    Double.toString(memorySize)
            );
        }
        mainStatsWriter.close();
    }
}
