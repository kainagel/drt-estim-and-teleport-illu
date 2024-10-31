package org.kainagel.drtEstimAndTeleportIllu;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.kainagel.drtEstimAndTeleportIllu.Main.getWorstPlan;

public class ModifiedVersion {

    public static void main(String[] args) throws IOException {
        // some parameters
        Random random = new Random(1);
        int populationSize = 10000;
        int maxIterations = 500;
        double proportionToSwitchOffInnovation = 0.9;
        int memorySize = 5;

        // mode choice probability
        double modeChoice = 0.1;
        // duplication of plans (due to time mutation and reroute)
        double duplicate = 0.2;

        // preference std (= 0 if everyone is the same)
        double preferenceStd = 0;

        // uncertainty level of DRT plans
        double sigma = 3;
        // advantage of mean score drt plan
//        double[] deltas = new double[]{-0.0001};
        double[] deltas = new double[]{-10, -5, -2, -1, -0.5, -0.2, -0.1, -0.0001, 0, 0.0001, 0.1, 0.2, 0.5, 1, 2, 5, 10};

        boolean printResults = false;

        // write down a tsv file for output
        String outputFolder = "/Users/luchengqi/Documents/TU-Berlin/Projects/DRT-estimate-and-teleport/mode-choice-study/with-preference-with-duplicate/" +
                "sigma_" + sigma + "-duplicate_" + duplicate;
        if (!Files.exists(Path.of(outputFolder))) {
            Files.createDirectories(Path.of(outputFolder));
            Files.createDirectories(Path.of(outputFolder + "/intermediate-results"));
        }
        CSVPrinter mainStatsWriter = new CSVPrinter(new FileWriter(outputFolder + "/main-stats.tsv"), CSVFormat.TDF);
        mainStatsWriter.printRecord("delta", "drt_mode_share", "sigma", "duplicate", "memory_size");

        for (double delta : deltas) {
            System.out.println("Simulating Delta: " + delta);
            // write down a tsv file for intermediate results:
            CSVPrinter intermediateResultsWriter =
                    new CSVPrinter(new FileWriter(outputFolder + "/intermediate-results/delta-" + delta + ".tsv"), CSVFormat.TDF);
            intermediateResultsWriter.printRecord("iter", "0", "1", "2", "3", "4", "5", "6", "drt_mode_share");

            // prepare initial population
            List<Main.Person> population = generateInitialPlans(populationSize);

            // generate different preference
            Map<Main.Person, Double> preferenceMap = new HashMap<>();
            for (Main.Person person : population) {
                preferenceMap.put(person, random.nextGaussian() * preferenceStd);
            }

            // run "simulation"
            double drtModeShare = 0;
            for (int iteration = 0; iteration < maxIterations; iteration++) {
                for (Main.Person person : population) {
                    // remove the worst plan if there are more plans than memory size
                    while (person.plans.size() > memorySize) {
                        final Main.Plan tmp = getWorstPlan(person);
                        person.plans.remove(tmp);
                    }

                    // if selected plan was the worst one and get removed, then randomly select a new plan
                    if (!person.plans.contains(person.selectedPlan)) {
                        person.selectRandomPlan(random);
                    }
                }

                // simulation
                // if the drt plan is the selected plan,
                // then we "simulate" it and generate a new score
                // (mean = 0, std = sigma)
                for (Main.Person person : population) {
                    Main.Plan selectedPlan = person.selectedPlan;
                    if (selectedPlan.type.equals(Main.Plan.Type.drt)) {
                        selectedPlan.score = random.nextGaussian() * sigma + delta + preferenceMap.get(person);
                    } else {
                        selectedPlan.score = 0;
                    }
                }

                // analysis for mode choice and score
                double numExecutedDrtPlans = 0;
                double sumExecutedScores = 0.;
                for (Main.Person person : population) {
                    Main.Plan selectedPlan = person.selectedPlan;
                    sumExecutedScores += selectedPlan.score;
                    if (selectedPlan.type.equals(Main.Plan.Type.drt)) {
                        numExecutedDrtPlans++;
                    }
                }

                // re-planning
                for (Main.Person person : population) {
                    if (iteration < maxIterations * proportionToSwitchOffInnovation) {
                        // perform innovation
                        double randomNumber = random.nextDouble();
                        if (randomNumber < modeChoice) {
                            // innovate by performing mode choice
                            // generate a new plan with random mode
                            Main.Plan newPlan = new Main.Plan();
                            int idx = random.nextInt(Main.Plan.Type.values().length);
                            newPlan.type = Main.Plan.Type.values()[idx];
                            newPlan.score = Double.MAX_VALUE;
                            person.plans.add(newPlan);
                            person.selectedPlan = newPlan;
                        } else if (randomNumber < modeChoice + duplicate) {
                            // generate a plan via duplication
                            Main.Plan newPlan = new Main.Plan();
                            newPlan.type = person.selectedPlan.type;
                            newPlan.score = Double.MAX_VALUE;
                            person.plans.add(newPlan);
                            person.selectedPlan = newPlan;
                        } else {
                            // change exp beta
                            person.changeExpBeta(random);
                        }
                    } else {
                        // change exp beta
                        person.changeExpBeta(random);
                    }
                }

                // statistics on num of DRT plans in memory
                double nDrtPlans = 0;
                double nOtherPlans = 0;

                double drt0 = 0;
                double drt1 = 0;
                double drt2 = 0;
                double drt3 = 0;
                double drt4 = 0;
                double drt5 = 0;
                double drt6 = 0;
                for (Main.Person person : population) {
                    int numDrtPlansInMemory = 0;
                    for (Main.Plan planInMemory : person.plans) {
                        if (planInMemory.type == Main.Plan.Type.drt) {
                            numDrtPlansInMemory++;
                            nDrtPlans++;
                        } else {
                            nOtherPlans++;
                        }
                    }
                    switch (numDrtPlansInMemory) {
                        case 0:
                            drt0++;
                            break;
                        case 1:
                            drt1++;
                            break;
                        case 2:
                            drt2++;
                            break;
                        case 3:
                            drt3++;
                            break;
                        case 4:
                            drt4++;
                            break;
                        case 5:
                            drt5++;
                            break;
                        case 6:
                            drt6++;
                            break;
                    }
                }

                // print out statistics
                if (printResults){
                    if (iteration == maxIterations * proportionToSwitchOffInnovation) {
                        System.out.println("-------------");
                    }
                    System.out.println("iteration = " + iteration
                            + "; avgExecutedScore = " + sumExecutedScores / population.size()
                            + "; drtModeShare = " + numExecutedDrtPlans / population.size()
                            + "; nTotalDrtPlans = " + nDrtPlans
                            + "; nTotalOtherPlans = " + nOtherPlans
                    );
                    System.out.println("Distribution of num of DRT plans in memory: "
                            + "; 0: " + drt0 / populationSize
                            + "; 1: " + drt1 / populationSize
                            + "; 2: " + drt2 / populationSize
                            + "; 3: " + drt3 / populationSize
                            + "; 4: " + drt4 / populationSize
                            + "; 5: " + drt5 / populationSize
                            + "; 6: " + drt6 / populationSize);
                }


                // keep updating until last iteration
                drtModeShare = numExecutedDrtPlans / population.size();

                intermediateResultsWriter.printRecord(
                        Integer.toString(iteration),
                        Double.toString(drt0 / populationSize),
                        Double.toString(drt1 / populationSize),
                        Double.toString(drt2 / populationSize),
                        Double.toString(drt3 / populationSize),
                        Double.toString(drt4 / populationSize),
                        Double.toString(drt5 / populationSize),
                        Double.toString(drt6 / populationSize),
                        Double.toString(drtModeShare)
                );
            }
            intermediateResultsWriter.close();

            mainStatsWriter.printRecord(
                    Double.toString(delta),
                    Double.toString(drtModeShare),
                    Double.toString(sigma),
                    Double.toString(duplicate),
                    Double.toString(memorySize)
            );
        }
        mainStatsWriter.close();
    }

    private static List<Main.Person> generateInitialPlans(int populationSize) {
        // create initial population (all "other" mode)
        List<Main.Person> persons = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            Main.Person person = new Main.Person();

            // generate one "other" plan and score it to 0
            final Main.Plan plan = new Main.Plan();
            plan.score = 0;
            plan.type = Main.Plan.Type.other;
            person.plans.add(plan);
            persons.add(person);
            person.selectedPlan = plan;
        }
        return persons;
    }

}
