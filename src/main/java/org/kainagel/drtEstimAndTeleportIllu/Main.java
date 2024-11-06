package org.kainagel.drtEstimAndTeleportIllu;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.kainagel.drtEstimAndTeleportIllu.Person.getBestPlan;
import static org.kainagel.drtEstimAndTeleportIllu.Person.getWorstPlan;

@Deprecated
/**
 * The modified version now provides more comprehensive setups. Please use RunSimulation to configure and run simulations.
 */
class Main {
    private static int cnt = 0;

    public static void main(String[] args) {
        Random rnd = new Random();
        List<Person> persons = new ArrayList<>();
        for (int ii = 0; ii < 1000; ii++) {
            Person person = new Person();

            // generate ONE new plan and immediately score it:
            final Plan plan = generateNewPlanWithScore(rnd);

            person.plans.add(plan);
            persons.add(person);
        }

        final long maxTime = 10000;
        for (long time = 0; time < maxTime; time++) {
            // remove worst plan if more than 5:
            for (Person person : persons) {
                while (person.plans.size() > 5) { //  maybe not possible since in loop?
                    final Plan tmp = getWorstPlan(person);
                    person.plans.remove(tmp);
                }
            }

            // re-compute score or add new plan:
            for (Person person : persons) {
                if (rnd.nextDouble() < 0.9) {
                    // find best plan:
                    final Plan tmp = getBestPlan(person);
                    // ... and generate new score if applicable:
                    if (tmp.type == Plan.Type.drt) {
                        tmp.score = rnd.nextGaussian() * 10;
                    }
                } else if (time < maxTime - 10) {
                    Plan plan = generateNewPlanWithScore(rnd);
                    person.plans.add(plan);
                } else if (cnt == 0) {
                    cnt++;
                    System.out.println("---");
                }
            }

            // compute output:
            double sumScore = 0.;
            double nDrtPlans = 0;
            double nOtherPlans = 0;
            for (Person person : persons) {
                Plan plan = getBestPlan(person);
                sumScore += plan.score;
                for (Plan plan1 : person.plans) {
                    if (plan1.type == Plan.Type.drt) {
                        nDrtPlans++;
                    } else {
                        nOtherPlans++;
                    }
                }
            }
            System.out.println("time=" + time
                    + "; avScore=" + sumScore / persons.size()
                    + "; nDrtPlans=" + nDrtPlans / persons.size()
                    + "; nOtherPlans=" + nOtherPlans / persons.size()
            );
        }
    }

    private static Plan generateNewPlanWithScore(Random rnd) {
        Plan plan = new Plan();
        if (Math.random() < 0.5) {
            plan.type = Plan.Type.drt;
            plan.score = 10 * rnd.nextGaussian(); // mean 0
        } else {
            plan.type = Plan.Type.other;
            plan.score = 0.;
        }
        return plan;
    }

}
