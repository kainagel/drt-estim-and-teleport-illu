package org.kainagel.drtEstimAndTeleportIllu;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.kainagel.drtEstimAndTeleportIllu.Person.getWorstPlan;

public class RunSimulation {

    public static void main(String[] args) throws IOException {
        long seed = 5;
        // some parameters
        Random random = new Random(seed);
        int populationSize = 10000;
        int maxIterations = 500;
        double proportionToSwitchOffInnovation = 0.8;
        int memorySize = 5;

        // mode choice probability
        double modeChoice = 0.1;
        // duplication of plans (due to time mutation and reroute)
        double duplicate = 0;

        // preference std (= 0 if everyone is the same)
        double preferenceStd = 0;
//        String preferenceDataSource = "/Users/luchengqi/Desktop/deltas.tsv";

        // uncertainty level of DRT plans
        double sigma = 1.64;

        // output folder name
//        String runSetup = "base";
        String runSetup = "matsim-ceb-impl-test/mc-" + modeChoice + "-seed-" + seed;

        // advantage of mean score drt plan
//        double[] deltas = new double[]{0};
        double[] deltas = new double[]{-10, -5, -2, -1, -0.5, -0.2, -0.1, -0.0001, 0, 0.0001, 0.1, 0.2, 0.5, 1, 2, 5, 10};

        // output folder
        String outputFolder = "/Users/luchengqi/Documents/TU-Berlin/Projects/ExpBetaStudy/" + runSetup +
                "/sigma_" + sigma;

        // Start running simulation
        Path outputFolderPath = Path.of(outputFolder);
        if (!Files.exists(outputFolderPath)) {
            Files.createDirectories(outputFolderPath);
            Files.createDirectories(Path.of(outputFolder + "/intermediate-results"));
        }
        CSVPrinter mainStatsWriter = new CSVPrinter(new FileWriter(outputFolder + "/main-stats.tsv"), CSVFormat.TDF);
        mainStatsWriter.printRecord("delta", "drt_mode_share", "sigma", "duplicate", "memory_size");

        for (double delta : deltas) {
            System.out.println("Simulating Delta: " + delta);
            // write down a tsv file for intermediate results:
            CSVPrinter intermediateResultsWriter =
                    new CSVPrinter(new FileWriter(outputFolder + "/intermediate-results/delta-" + delta + ".tsv"), CSVFormat.TDF);
            intermediateResultsWriter.printRecord("iter", "0", "1", "2", "3", "4", "5", "avg_drt_plans_in_memory");

            // prepare initial population
            List<Person> population = generateInitialPlans(populationSize);

            // prepare preference for DRT for each person
            // via normal distribution (std = 0 means no variation)
            prepareDrtPreferenceFromNormalDistribution(random, preferenceStd, population);
//            prepareDrtPreferenceFromFile(preferenceDataSource, population);

            // run "simulation"
            double drtModeShare = 0;
            for (int iteration = 0; iteration < maxIterations; iteration++) {
                // simulation
                for (Person person : population) {
                    Plan selectedPlan = person.selectedPlan;
                    if (selectedPlan.type.equals(Plan.Type.drt)) {
                        // if drt plan is the selected plan, then we "simulate" it and generate a new score
                        // drt plan score: mean = preference + delta, std = sigma
                        selectedPlan.score = random.nextGaussian() * sigma + delta + person.preferenceForDrt;
                    } else {
                        selectedPlan.score = 0;
                    }
                }

                // analysis for mode choice and score
                double numExecutedDrtPlans = 0;
                for (Person person : population) {
                    Plan selectedPlan = person.selectedPlan;
                    if (selectedPlan.type.equals(Plan.Type.drt)) {
                        numExecutedDrtPlans++;
                    }
                }

                // keep updating until last iteration
                drtModeShare = numExecutedDrtPlans / population.size();

                // plan removal
                for (Person person : population) {
                    // remove the worst plan if there are more plans than memory size
                    while (person.plans.size() > memorySize) {
                        final Plan tmp = getWorstPlan(person);
                        person.plans.remove(tmp);
                    }

                    // if selected plan was the worst one and get removed, then randomly select a new plan
                    if (!person.plans.contains(person.selectedPlan)) {
                        person.selectRandomPlan(random);
                    }
                }

                // intermediate result analysis
                // Note: in MATSim, the plan removal is performed after the intermediate plans are written!!!
                // For analysis purpose, we analyze the number of DRT plans in memory after the plan removal.
                intermediateAnalysis(intermediateResultsWriter, population, iteration);

                // re-planning
                // select a strategy
                for (Person person : population) {
                    if (iteration < maxIterations * proportionToSwitchOffInnovation) {
                        // perform innovation
                        double randomNumber = random.nextDouble();
                        if (randomNumber < modeChoice) {
                            // innovate by performing mode choice
                            // choose a random plan and change mode
                            // when possible, the new mode is always different!
                            Plan newPlan = new Plan();
                            // get a random plan (here, each plan only has one leg, we just extract the mode)
                            Plan.Type originalType = person.plans.get(random.nextInt(person.plans.size())).type;
                            // change to a different mode
                            List<Plan.Type> possibleTypes = Arrays.stream(Plan.Type.values()).filter(type -> type != originalType).toList();
                            int idx = random.nextInt(possibleTypes.size());
                            newPlan.type = possibleTypes.get(idx);
                            newPlan.score = Double.MAX_VALUE;
                            person.plans.add(newPlan);
                            person.selectedPlan = newPlan;
                        } else if (randomNumber < modeChoice + duplicate) {
                            // generate a plan via duplication
                            Plan newPlan = new Plan();
                            newPlan.type = person.selectedPlan.type;
                            newPlan.score = Double.MAX_VALUE;
                            person.plans.add(newPlan);
                            person.selectedPlan = newPlan;
                        } else {
                            // change exp beta
//                            person.changeExpBeta(random);
//                            person.selectRandomAndBest((double) 1 / 9, random);
                            person.changeExpBetaMATSimImpl(random);
                        }
                    } else {
                        // change exp beta
//                        person.changeExpBeta(random);
                        person.selectRandomAndBest((double) 1 / 9, random);
                    }
                }
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

    private static List<Person> generateInitialPlans(int populationSize) {
        // create initial population (all "other" mode)
        List<Person> persons = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            Person person = new Person();

            // generate one "other" plan and score it to 0
            final Plan plan = new Plan();
            plan.score = 0;
            plan.type = Plan.Type.other;
            person.plans.add(plan);
            persons.add(person);
            person.selectedPlan = plan;
        }
        return persons;
    }

    private static void prepareDrtPreferenceFromNormalDistribution(Random rnd, double std, List<Person> population) {
        for (Person person : population) {
            person.preferenceForDrt = rnd.nextGaussian() * std;
        }
    }

    private static void prepareDrtPreferenceFromFile(String fileName, List<Person> population) throws IOException {
        List<Double> preferenceValues = new ArrayList<>();
        try (CSVParser parser = new CSVParser(Files.newBufferedReader(Path.of(fileName)),
                CSVFormat.DEFAULT.withDelimiter(';').withFirstRecordAsHeader())) {
            for (CSVRecord record : parser) {
                double scoreDifference = Double.parseDouble(record.get(0));
                if (Double.isNaN(scoreDifference)) {
                    scoreDifference = 0.;
                }
                preferenceValues.add(scoreDifference);
            }
        }
        double avg = preferenceValues.stream().mapToDouble(v -> v).average().getAsDouble();
        preferenceValues = preferenceValues.stream().map(v -> v - avg).collect(Collectors.toList());
        if (preferenceValues.size() != population.size()) {
            System.err.println("Not enough data for all persons. The extra persons in population will have a " +
                    "default preference of 0");
        }
        for (int i = 0; i < preferenceValues.size(); i++) {
            population.get(i).preferenceForDrt = preferenceValues.get(i);
        }
    }

    private static void intermediateAnalysis(CSVPrinter intermediateResultsWriter,
                                             List<Person> population, int iteration) throws IOException {
        int populationSize = population.size();
        double nDrtPlans = 0;

        double drt0 = 0;
        double drt1 = 0;
        double drt2 = 0;
        double drt3 = 0;
        double drt4 = 0;
        double drt5 = 0;

        for (Person person : population) {
            int numDrtPlansInMemory = 0;
            for (Plan planInMemory : person.plans) {
                if (planInMemory.type == Plan.Type.drt) {
                    numDrtPlansInMemory++;
                    nDrtPlans++;
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
            }
        }

        intermediateResultsWriter.printRecord(
                Integer.toString(iteration),
                Double.toString(drt0 / populationSize),
                Double.toString(drt1 / populationSize),
                Double.toString(drt2 / populationSize),
                Double.toString(drt3 / populationSize),
                Double.toString(drt4 / populationSize),
                Double.toString(drt5 / populationSize),
                Double.toString(nDrtPlans / populationSize)
        );
    }
}
