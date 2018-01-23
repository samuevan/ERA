package ec.app.gpra.terminals;
import ec.EvolutionState;
import ec.Problem;
import ec.app.gpra.DoubleData;
import ec.app.gpra.GPRA_Problem;
import ec.gp.*;
//import ec.gp.GPData;
//import ec.gp.GPIndividual;


public class Score1 extends GPNode{

    public String toString() { return "s1"; }

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

	public void eval(final EvolutionState state, final int thread,
			final GPData input, final ADFStack stack,
			final GPIndividual individual, final Problem problem) 
	{
		DoubleData rd = ((DoubleData) (input));
		rd.x = ((GPRA_Problem) problem).scores[1]; //TODO pode ser que eu use um vetor na classe de 
		                                                   //avaliao e aqui eu pegue a posicao que eu quero 
	}
	
	
}
