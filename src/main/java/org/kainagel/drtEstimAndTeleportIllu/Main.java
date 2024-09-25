package playground.kairuns.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Main{
	static class Person {
		List<Plan> plans = new ArrayList<>();
	}
	static class Plan {
		enum Type {drt, other};
		Type type;
		double score;
	}
	public static void main( String[] args ){
		Random rnd = new Random();
		List<Person> persons = new ArrayList<>();
		for ( int ii=0; ii<1000; ii++ ) {
			Person person = new Person();

			// generate ONE new plan and immediately score it:
			final Plan plan = generateNewPlanWithScore( rnd );

			person.plans.add( plan );
			persons.add( person );
		}

		final long maxTime = 10000;
		for( long time = 0 ; time< maxTime ; time++ ){
			// remove worst plan if more than 5:
			for( Person person : persons ){
				while( person.plans.size() > 5 ){ //  maybe not possible since in loop?
					final Plan tmp = getWorstPlan( person );
					person.plans.remove( tmp );
				}
			}

			// re-compute score or add new plan:
			for( Person person : persons ){
				if( rnd.nextDouble() < 0.9 ){
					// find best plan:
					final Plan tmp = getBestPlan( person );
					// ... and generate new score if applicable:
					if( tmp.type == Plan.Type.drt ){
						tmp.score = rnd.nextGaussian();
					}
				} else if ( time < maxTime-10 ){
					Plan plan = generateNewPlanWithScore( rnd );
					person.plans.add( plan );
				}
			}

			// compute output:
			double sumScore = 0.;
			double nDrtPlans = 0;
			double nOtherPlans = 0;
			for( Person person : persons ){
				Plan plan = getBestPlan( person );
				sumScore += plan.score;
				for( Plan plan1 : person.plans ){
					if ( plan1.type== Plan.Type.drt ) {
						nDrtPlans++;
					} else {
						nOtherPlans++;
					}
				}
			}
			System.out.println( "time=" + time
							    + "; avScore=" + sumScore/persons.size()
							    + "; nDrtPlans=" + nDrtPlans/persons.size()
							    + "; nOtherPlans=" + nOtherPlans/persons.size()
					  );


		}


	}
	private static Plan generateNewPlanWithScore( Random rnd ){
		Plan plan = new Plan();
		if ( Math.random()<0.5 ) {
			plan.type = Plan.Type.drt;
			plan.score = 10.* rnd.nextGaussian(); // mean 0
		} else {
			plan.type = Plan.Type.other;
			plan.score = 0.;
		}
		return plan;
	}
	private static Plan getWorstPlan( Person person ){
		double minScore = Double.POSITIVE_INFINITY;
		Plan tmp = null;
		for( Plan plan : person.plans ){
			if( plan.score < minScore ){
				minScore = plan.score;
				tmp = plan;
			}
		}
		return tmp;
	}
	private static Plan getBestPlan( Person person ){
		double maxScore = Double.NEGATIVE_INFINITY;
		Plan tmp = null;
		for( Plan plan : person.plans ){
			if( plan.score > maxScore ){
				maxScore = plan.score;
				tmp = plan;
			}
		}
		return tmp;
	}
}
