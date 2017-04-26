package ec.app.gpra.terminals;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import ec.EvolutionState;
import ec.Problem;
import ec.app.gpra.DoubleData;
import ec.gp.*;
import ec.util.Code;
import ec.util.DecodeReturn;

public class AlphaERC extends ERC {
	public double value;

	public String toStringForHumans() {
		return "ERC [" + value+"]";
	}
	
	public String toString(){
		return "ERC ["+value + "]";
	}

	public String encode() {
		return Code.encode(value);
	}

	public boolean decode(DecodeReturn dret) {
		int pos = dret.pos;
		String data = dret.data;
		Code.decode(dret);
		if (dret.type != DecodeReturn.T_DOUBLE) // uh oh! Restore and signal error.
		{ 
			dret.data = data; 
			dret.pos = pos; 
			return false; 
		}
		value = dret.d;
		return true;
	}

	public boolean nodeEquals(GPNode node) {
		return (node.getClass() == this.getClass() && ((AlphaERC) node).value == value);
	}

	public void readNode(EvolutionState state, DataInput dataInput)
			throws IOException {
		value = dataInput.readDouble();
	}

	public void writeNode(EvolutionState state, DataOutput dataOutput)
			throws IOException {
		dataOutput.writeDouble(value);
	}

	public void resetNode(EvolutionState state, int thread) {
		value = state.random[thread].nextDouble();
		//state.random[thread].nextInt(3) +
		//System.out.println("ERC " + value);
		
	}

	public void mutateNode(EvolutionState state, int thread) {
		double v;
		do
			v = value + state.random[thread].nextGaussian() * 0.01;
		while (v < 0.0 || v >= 1.0);
		value = v;
	}

	public void eval(EvolutionState state, int thread, GPData data, ADFStack stack, GPIndividual individual, 
			Problem problem) 
	{
		((DoubleData) data).x = value;
	}

}
