package ec.app.gpra.terminals;
import ec.EvolutionState;
import ec.Problem;
import ec.app.gpra.DoubleData;
import ec.app.gpra.GPRA_Problem;
import ec.gp.*;

//import ec.gp.GPData;
//import ec.gp.GPIndividual;


public class GS_min extends GPNode{

    public String toString() { return "GS_min"; }

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
		DoubleData rd = ((DoubleData) (input));
		rd.x = ((GPRA_Problem) problem).GS_min;
		//rd.x = ((GPRA_Problem) problem).genericDoubles.get(0); //TODO pode ser que eu use um vetor na classe de 
		                                                   //avaliacao e aqui eu pegue a posicao que eu quero 
	}
	
	
}
