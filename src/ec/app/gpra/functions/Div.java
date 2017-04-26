package ec.app.gpra.functions;


import ec.*;
import ec.app.gpra.DoubleData;
import ec.gp.*;
import ec.util.*;

public class Div extends GPNode
    {
    public String toString() { return "/"; }

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
    public int expectedChildren() { return 2; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem)
        {
        double result;
        DoubleData rd = ((DoubleData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
        result = rd.x;

        children[1].eval(state,thread,input,stack,individual,problem);
        
        //TODO divisao protegida
        if(!(rd.x == 0))
        	rd.x = result / rd.x;
        else
        	rd.x = 1;
        }
    }

