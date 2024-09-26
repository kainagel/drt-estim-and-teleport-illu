package org.kainagel.drtEstimAndTeleportIllu;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.kainagel.drtEstimAndTeleportIllu.Main.getWorstPlan;

public class ModifiedVersion {

    public static void main(String[] args) {
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

        // uncertainty level of DRT plans
        double sigma = 0;
        // advantage of mean score drt plan
        double delta = -0.1;

        // prepare initial population
        List<Main.Person> population = generateInitialPlans(populationSize);

        // run "simulation"
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
            // if the drt plan is the selected plan, then we "simulate" it and generate a new score (mean = 0, std = sigma)
            for (Main.Person person : population) {
                Main.Plan selectedPlan = person.selectedPlan;
                if (selectedPlan.type.equals(Main.Plan.Type.drt)) {
                    selectedPlan.score = random.nextGaussian() * sigma + delta;
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
            if (iteration == maxIterations * proportionToSwitchOffInnovation) {
                System.out.println("---");
            }
            System.out.println("iteration = " + iteration
                    + "; avgExecutedScore = " + sumExecutedScores / population.size()
                    + "; drtModeShare = " + numExecutedDrtPlans / population.size()
                    + "; nTotalDrtPlans = " + nDrtPlans
                    + "; nTotalOtherPlans = " + nOtherPlans
            );
            System.out.println("DIstribution of num of DRT plans in memory: "
                    + "; 0: " + drt0 / populationSize
                    + "; 1: " + drt1 / populationSize
                    + "; 2: " + drt2 / populationSize
                    + "; 3: " + drt3 / populationSize
                    + "; 4: " + drt4 / populationSize
                    + "; 5: " + drt5 / populationSize
                    + "; 6: " + drt6 / populationSize);
        }
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
