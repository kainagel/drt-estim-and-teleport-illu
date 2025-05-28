package org.expBetaStudy;

import java.util.*;

public class Person {
    List<Plan> plans = new ArrayList<>();
    Plan selectedPlan = null;

    static Person createDefaultPerson() {
        Person person = new Person();
        Plan defaultPlan = new Plan();
        defaultPlan.mode = Mode.modeA;
        person.plans.add(defaultPlan);
        person.selectedPlan = defaultPlan;
        return person;
    }

    static List<Person> createInitialPopulation(int populationSize, double initialModeBShare, Random random) {
        List<Person> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            Person person = new Person();
            Plan defaultPlan = new Plan();
            if (random.nextDouble() < initialModeBShare) {
                defaultPlan.mode = Mode.modeB;
            } else {
                defaultPlan.mode = Mode.modeA;
            }
            person.plans.add(defaultPlan);
            person.selectedPlan = defaultPlan;
            population.add(person);
        }
        return population;
    }

    // innovative strategy: change single trip mode
    void performModeInnovation(Random random) {
        Plan newPlan = new Plan();
        // according to the MATSim book, change single trip mode will randomly pick a plan
        // and randomly pick a leg in the plan, and change the mode (when possible).
        Plan aRandomSelectedPlan = this.plans.get(random.nextInt(this.plans.size()));
        // since each person only has one leg in our experiment, we can read the mode directly.
        Mode originalMode = aRandomSelectedPlan.mode;
        List<Mode> possibleModes = Arrays.stream(Mode.values()).filter(mode -> mode != originalMode).toList();
        // switch to the new mode
        newPlan.mode = possibleModes.get(random.nextInt(possibleModes.size()));
        plans.add(newPlan);
        this.selectedPlan = newPlan;
    }

    // non-innovative strategies
    void changeExpBeta(Random random) {
        if (plans.isEmpty()) {
            throw new RuntimeException("No plans in memory. Please create at least one plan");
        }

        // Randomly choose a plan from the memory
        // (it can also be the currently selected plan based on the current implementation in MATSim)
        Plan otherPlan = plans.get(random.nextInt(plans.size()));

        // assume beta = 1
        double beta = 1;
        double probabilityToSwitch = Math.exp(otherPlan.score * beta) / (Math.exp(selectedPlan.score * beta) + Math.exp(otherPlan.score * beta));
        if (random.nextDouble() < probabilityToSwitch) {
            selectedPlan = otherPlan;
        }
    }

    void changeExpBetaMATSimImpl(Random random, double gamma) {
        if (plans.isEmpty()) {
            throw new RuntimeException("No plans in memory. Please create at least one plan");
        }

        // Randomly choose a plan
        // (it can also be the currently selected plan based on the current implementation in MATSim)
        Plan otherPlan = plans.get(random.nextInt(plans.size()));

        double beta = 1;

        // The MATSim implementation: with 0.01 as hard-coded value
        double weight = Math.exp(0.5 * beta * (otherPlan.score - selectedPlan.score));
//        double weight =  Math.min(Math.exp(0.5 * beta * (otherPlan.score - selectedPlan.score)), 1);
//      default value for gamma = 0.01 (in the original implementation)
        if (random.nextDouble() < gamma * weight) {
            selectedPlan = otherPlan;
        }
    }

    void selectExpBeta(Random random) {
        if (plans.isEmpty()) {
            throw new RuntimeException("No plans in memory. Please create at least one plan");
        }

        double beta = 1;
        Map<Plan, Double> weights = new HashMap<>();
        double sumWeights = 0;
        for (Plan plan : this.plans) {
            double weight = Math.exp(plan.score * beta);
            sumWeights += weight;
            weights.put(plan, weight);
        }

        double randomNumber = random.nextDouble() * sumWeights;
        for (Plan plan : weights.keySet()) {
            randomNumber -= weights.get(plan);
            selectedPlan = plan;
            if (randomNumber <= 0) {
                break;
            }
        }
    }

    Plan getWorstPlan() {
        if (this.plans.isEmpty()) {
            throw new RuntimeException("No plans in memory!");
        }
        double worstScore = Double.MAX_VALUE;
        Plan worstPlan = null;
        for (Plan plan : this.plans) {
            if (plan.score < worstScore) {
                worstScore = plan.score;
                worstPlan = plan;
            }
        }
        return worstPlan;
    }
}
