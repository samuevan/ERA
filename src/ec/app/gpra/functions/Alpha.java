package ec.app.gpra.functions;
import ec.EvolutionState;
import ec.Problem;
import ec.app.gpra.DoubleData;
import ec.gp.*;
//import ec.gp.GPData;
//import ec.gp.GPIndividual;


public class Alpha extends GPNode{

	double alpha = 0;
    public String toString() { return ""+alpha; }

/*
  public void checkConstraints(final EvolutionState state,
  final int tree,
  final GPIndividual typicalIndividual,
  final Parameter individualBase)
  {
  super.checkConstraints(state,tree,typicalIndividual,individualBase);
  if (children.length!=0)
  state.output.error("Incorrect number of children for node " + 
  toStringForError() + " at " +
  individualBase);
  }
*/
    public int expectedChildren() { return 0; }

    
    //TODO Testar para ver se eu posso alterar a passagem de parametros desse mtodo
	public void eval(final EvolutionState state, final int thread,
			final GPData input, final ADFStack stack,
			final GPIndividual individual, final Problem problem) 
	{
		this.alpha = (2*Math.random())+1;
		
		DoubleData rd = ((DoubleData) (input));
		rd.x = this.alpha; 
	}
	
	
}
