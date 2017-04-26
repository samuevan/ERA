package ec.app.gpra;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import ec.Individual;
import ec.util.Checkpoint;

public class MySimpleEvolutionState_nonich extends ec.simple.SimpleEvolutionState{

	private static int itersWithoutImprove = 0;
	private int maxIterWithoutImprove = -1;
	private static double pastFit = 0; 
	
	
	File alternativeOutput; 
	private PrintWriter alternativeOutWritter = null;
	private boolean isAltOutputOpen = false;
	
	public int evolve()
    {
    if (generation > 0) 
        output.message("Generation " + generation);

    // EVALUATION
    statistics.preEvaluationStatistics(this);
    evaluator.evaluatePopulation(this);
    statistics.postEvaluationStatistics(this);
    
    //#################################################################
    //My statistics
    MyStatistics mystat = new MyStatistics(this.population);
    alternativeOutWritter.write("Generation " + generation + ":\n");
    alternativeOutWritter.write(mystat.toString());
    alternativeOutWritter.write("\n");
    alternativeOutWritter.flush();
    //######################################################################
    
    //Verify how many generations without improve
    if (generation == 1)
    	pastFit = mystat.getbestFitnessVal();
    
    
    double fit = mystat.getbestFitnessVal();
    
    if(Math.abs(fit - pastFit) < 0.0001 )
    {
    	itersWithoutImprove++;
    }else
    {
    	pastFit = fit;
    	itersWithoutImprove = 0;
    }
    
    
    //SHOULD WE QUIT?
    if(itersWithoutImprove == maxIterWithoutImprove-1){
    	output.message("Achieved " + maxIterWithoutImprove + "generations without improve");
    	alternativeOutWritter.write("Achieved " + maxIterWithoutImprove + "generations without improve");
    	alternativeOutWritter.close();
    	
    	return R_FAILURE;
    }
        
    // SHOULD WE QUIT?
    if (evaluator.runComplete(this) && quitOnRunComplete)
        {
        output.message("Found Ideal Individual");
        
        alternativeOutWritter.close();
        return R_SUCCESS;
        }

    // SHOULD WE QUIT?
    if (generation == numGenerations-1)
        {
    	alternativeOutWritter.close();
    	return R_FAILURE;
        }

    // PRE-BREEDING EXCHANGING
    statistics.prePreBreedingExchangeStatistics(this);
    population = exchanger.preBreedingExchangePopulation(this);
    statistics.postPreBreedingExchangeStatistics(this);

    String exchangerWantsToShutdown = exchanger.runComplete(this);
    if (exchangerWantsToShutdown!=null)
        { 
        output.message(exchangerWantsToShutdown);
        /*
         * Don't really know what to return here.  The only place I could
         * find where runComplete ever returns non-null is 
         * IslandExchange.  However, that can return non-null whether or
         * not the ideal individual was found (for example, if there was
         * a communication error with the server).
         * 
         * Since the original version of this code didn't care, and the
         * result was initialized to R_SUCCESS before the while loop, I'm 
         * just going to return R_SUCCESS here. 
         */
        
        return R_SUCCESS;
        }

    // BREEDING
    statistics.preBreedingStatistics(this);

    population = breeder.breedPopulation(this);
    
    // POST-BREEDING EXCHANGING
    statistics.postBreedingStatistics(this);
        
    // POST-BREEDING EXCHANGING
    statistics.prePostBreedingExchangeStatistics(this);
    population = exchanger.postBreedingExchangePopulation(this);
    statistics.postPostBreedingExchangeStatistics(this);

    // INCREMENT GENERATION AND CHECKPOINT
    generation++;
    if (checkpoint && generation%checkpointModulo == 0) 
        {
        output.message("Checkpointing");
        statistics.preCheckpointStatistics(this);
        Checkpoint.setCheckpoint(this);
        statistics.postCheckpointStatistics(this);
        }

    return R_NOTDONE;
    }
	
	public void setAternativeOutput(File alternativeOut) throws FileNotFoundException{
		alternativeOutWritter = new PrintWriter(alternativeOut);
		isAltOutputOpen = true;
		
	}
	
	public void writeAlternativeOutput(String s){
		if(isAltOutputOpen){
			alternativeOutWritter.write(s+"\n");
		}
	}
	
	public void setMaxIterWithoutImprove(int max){
		maxIterWithoutImprove = max;
	}
	
	
	private void fitness_sharing(){
		
		for (Individual ind : population.subpops[0].individuals){
			
			
		}
	}
	
	
}
