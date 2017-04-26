package ec.app.gpra.functions;
/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/



import ec.*;
import ec.app.gpra.DoubleData;
import ec.app.util.Utils;
import ec.gp.*;
import ec.util.*;

public class Log extends GPNode
    {
	
	
    public String toString() { return "log"; }

/*
  public void checkConstraints(final EvolutionState state,
  final int tree,
  final GPIndividual typicalIndividual,
  final Parameter individualBase)
  {
  super.checkConstraints(state,tree,typicalIndividual,individualBase);
  if (children.length!=2)
  state.output.error("Incorrect number of children for node " + 
  toStringForError() + " at " +
  individualBase);
  }
*/
    public int expectedChildren() { return 1; }

	public void eval(final EvolutionState state, final int thread,
			final GPData input, final ADFStack stack,
			final GPIndividual individual, final Problem problem) {
		
		double result;
		DoubleData rd = ((DoubleData) (input));

		children[0].eval(state, thread, input, stack, individual, problem);
		result = rd.x;
		
		//TODO log protegido
		if(result != 0)
			rd.x = Utils.log2(Math.abs(result));
		else
			rd.x = 0;
	}
    }

