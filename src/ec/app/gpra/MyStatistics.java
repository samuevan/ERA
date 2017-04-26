package ec.app.gpra;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ec.Population;
import ec.Subpopulation;
import ec.gp.koza.KozaFitness;

public class MyStatistics {
	
	HashMap<String,Integer> popTerminalsCount;
	private double averageFitnessTest;
	private double averageFitness;
	private double averageFitnessVal;
	private double bestFitnessVal;
	private double bestFitnessTest;
	private double bestHits;
	private double bestHits_use;
	private double prec_5[] = new double[2];
	private double prec_10[] = new double[2];
	private double map_5[] = new double[2];
	private double map_10[] = new double[2];
	
	
	
	public Map<String, Integer> getPopTerminalsCount() {
		return popTerminalsCount;
	}

	public double getAverageFitnessTest() {
		return averageFitnessTest;
	}

	public double getAverageFitnessVal() {
		return averageFitnessVal;
	}

	public double getbestFitnessVal() {
		return bestFitnessVal;
	}




	public MyStatistics(Population pop){
		
		popTerminalsCount = new HashMap<String, Integer>();
		
		//inicialmente so considero uma subpopulacao
		Subpopulation subpop = pop.subpops[0];
		double avgFitTest = 0;
		double avgFitVal = 0;
		double avgFit = 0;
		int subpopSize = subpop.individuals.length;
		double bestFit = 1000; //TODO alterar pra receber inf
		double bestFitTest = 1000;
		double bestHits_x = 0;
		double bestHits_use_x = 0;
		MyIndividual bestInd = null;
		
		for(int i  = 0; i < subpopSize; i++){
			
			MyIndividual myind = (MyIndividual) subpop.individuals[i];
			//################################################
			//retrieve the couting o terminals
			/*String s = subpop.individuals[i].genotypeToString();
			 
			
			HashMap<String,Integer> indTerminals = myind.getTerminalsCount();
			
			Iterator<String> iter = indTerminals.keySet().iterator();
			while(iter.hasNext()){
				String key = iter.next();
				
				if(popTerminalsCount.containsKey(key)){
					//atualiza o numero de terminais da populacao com o numero de terminais do individuo
					popTerminalsCount.put(key,popTerminalsCount.get(key)+indTerminals.get(key));
					
				}else
				{
					popTerminalsCount.put(key, indTerminals.get(key));
				}
			}*/
		
			//######################################################
		
			if(bestFit > ((KozaFitness)subpop.individuals[i].fitness).standardizedFitness()){
				bestFit = ((KozaFitness)subpop.individuals[i].fitness).standardizedFitness();
				bestFitTest = ((MyIndividual)subpop.individuals[i]).getTestFitness();
				bestInd = myind;
				
			}
			
			if(bestHits_x < ((MyIndividual)subpop.individuals[i]).getValidationHits()){
				bestHits_x = ((MyIndividual)subpop.individuals[i]).getValidationHits();
			}
			
			
			if(bestHits_use_x < ((MyIndividual)subpop.individuals[i]).getValidationHits_use()){
				bestHits_use_x = ((MyIndividual)subpop.individuals[i]).getValidationHits_use();
			}
			/*
			//TODO alterar para pegar o fitness correspondendo ao bestFitVal
			if(bestFitTest > ((MyIndividual)subpop.individuals[i]).getTestFitness())
				bestFitTest = ((MyIndividual)subpop.individuals[i]).getTestFitness();
			*/
			
			avgFit += ((KozaFitness)subpop.individuals[i].fitness).standardizedFitness();
			
			avgFitVal += ((MyIndividual)subpop.individuals[i]).getValidationFitness();
			avgFitTest += ((MyIndividual)subpop.individuals[i]).getTestFitness();
			
		}
		avgFit /= subpopSize;
		avgFitVal /= subpopSize;
		avgFitTest /= subpopSize;
		
		averageFitness = avgFit;
		averageFitnessVal = avgFitVal;
		averageFitnessTest = avgFitTest;
		
		bestFitnessVal = bestFit;
		bestFitnessTest = bestFitTest;
		
		bestHits = bestHits_x;
		bestHits_use = bestHits_use_x;
		prec_5 = bestInd.getPrec_5();
		prec_10 = bestInd.getPrec_10();
		map_5 = bestInd.getMap_5();
		map_10 = bestInd.getMap_10();
	}
	
	
	
	public String toString(){
		
		String s = "";
		s += "Terminals: " + popTerminalsCount.toString() + "\n";
		s += "Average Fitness: " + averageFitness + "\n";
		s += "Average Fitness Validation: " + averageFitnessVal + "\n";
		s += "Average Fitness Test: " + averageFitnessTest + "\n";
		s += "Best Fitness Validation: " + bestFitnessVal + "\n";
		s += "Best Hits: " + bestHits + "\n";
		s += "Best Hits_use: " + bestHits_use + "\n";
		s += "Best Fitness Test: " + bestFitnessTest + "\n";
		s += "Best prec@5 Test: "  + prec_5[0] +"\n";
		s += "Best prec@10 Test: "  + prec_10[0] +"\n";
		s += "Best prec@5 Val: "  + prec_5[1] +"\n";
		s += "Best prec@10 Val: "  + prec_10[1] +"\n";
		s += "Best map@5 Test: "  + map_5[0] +"\n";
		s += "Best map@10 Test: "  + map_10[0] +"\n";
		s += "Best map@5 Val: "  + map_5[1] +"\n";
		s += "Best map@10 Val: "  + map_10[1] +"\n";
		
		return s;
		
		
	}

}
