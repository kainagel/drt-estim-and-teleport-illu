package org.kainagel.drtEstimAndTeleportIllu;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Person {
    List<Plan> plans = new ArrayList<>();
    Plan selectedPlan = null;
    /**
     * Preference for drt: this includes not only the taste variance, but also the convenience to take the mode DRT
     * compared to the other mode. We use a simple value to represent all the factors.
     */
    double preferenceForDrt = 0;

    void selectRandomPlan(Random rnd) {
        if (!plans.isEmpty()) {
            selectedPlan = plans.get(rnd.nextInt(plans.size()));
        } else {
            throw new RuntimeException("No plans in memory. Please create at least one plan");
        }
    }

    void changeExpBeta(Random rnd) {
        if (plans.isEmpty()) {
            throw new RuntimeException("No plans in memory. Please create at least one plan");
        }

        // Randomly choose a plan
        // (it can also be the currently selected plan based on the current implementation in MATSim)
        Plan otherPlan = plans.get(rnd.nextInt(plans.size()));

        // assume beta = 1
        double beta = 1;
        double probabilityToSwitch = Math.exp(otherPlan.score * beta) / (Math.exp(selectedPlan.score * beta) + Math.exp(otherPlan.score * beta));
        if (rnd.nextDouble() < probabilityToSwitch) {
            selectedPlan = otherPlan;
        }
    }

    void changeExpBetaMATSimImpl(Random rnd) {
        if (plans.isEmpty()) {
            throw new RuntimeException("No plans in memory. Please create at least one plan");
        }

        // Randomly choose a plan
        // (it can also be the currently selected plan based on the current implementation in MATSim)
        Plan otherPlan = plans.get(rnd.nextInt(plans.size()));

        // assume beta = 1
        double beta = 1;

        // The MATSim implementation: with 0.01 as hard-coded value
        double weight = Math.exp(0.5 * beta * (otherPlan.score - selectedPlan.score));
        if (rnd.nextDouble() < 0.01 * weight) {
            selectedPlan = otherPlan;
        }
    }

    void selectBestPlan() {
        if (plans.isEmpty()) {
            throw new RuntimeException("No plans in memory. Please create at least one plan");
        }
        selectedPlan = getBestPlan(this);
    }

    void selectRandomAndBest(double proportionRnd, Random rnd) {
        if (plans.isEmpty()) {
            throw new RuntimeException("No plans in memory. Please create at least one plan");
        }
        if (rnd.nextDouble() < proportionRnd) {
            selectRandomPlan(rnd);
        } else {
            selectedPlan = getBestPlan(this);
        }
    }

    static Plan getWorstPlan(Person person) {
        double minScore = Double.POSITIVE_INFINITY;
        Plan tmp = null;
        for (Plan plan : person.plans) {
            if (plan.score < minScore) {
                minScore = plan.score;
                tmp = plan;
            }
        }
        return tmp;
    }

    static Plan getBestPlan(Person person) {
        double maxScore = Double.NEGATIVE_INFINITY;
        Plan tmp = null;
        for (Plan plan : person.plans) {
            if (plan.score > maxScore) {
                maxScore = plan.score;
                tmp = plan;
            }
        }
        return tmp;
    }

}
