package ec.app.gpra.terminals;

import ec.EvolutionState;
import ec.Problem;
import ec.app.gpra.DoubleData;
import ec.app.gpra.GPRA_Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

public class ProbTop10 extends GPNode{

	@Override
	public String toString() {
		return ("pt10");
	}
	
    public int expectedChildren(){
    	return 0; 
    }


	@Override
	public void eval(EvolutionState state, int thread, GPData input,
			ADFStack stack, GPIndividual individual, Problem problem) {
		// TODO Auto-generated method stub
		
		DoubleData data = (DoubleData) input;
		
		data.x = ((GPRA_Problem) problem).probTop10;
		
		
		
	}

	
	
}
