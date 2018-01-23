package ec.app.gpra;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

import ec.EvolutionState;
import ec.app.util.Pair;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.util.Parameter;

public class MyIndividual extends GPIndividual{

	private double validationFitness;
	private double testFitness;
	private double validationHits;
	private double validationHits_use;
	private double testnHits;
	private Vector<Pair<Integer,Integer>> hits_by_user;
	//teste e validacao
	private double prec_5[] = new double[2];
	private double prec_10[] = new double[2];
	private double map_5[] = new double[2];
	private double map_10[] = new double[2];
	
	
	
	public void setup(final EvolutionState state, final Parameter base){
		super.setup(state, base);
		
		hits_by_user = new Vector<Pair<Integer,Integer>>();
				
	}
	
	public String toString(){
		
		return DFS_print(trees[0].child, "");
	}
	
	public String DFS_print(GPNode node,String s){
		
		GPNode p[] = node.children;
		if(node.children.length == 0){
			s += " "+node.toString();
			//return s;
		} 
		else{
			if(node.children.length == 1){
				s += node.toString() + "(";
				s = DFS_print(node.children[0],s);
				s += ")";
			}
			else
			{
				if(node.children.length == 2){
					s += node.toString() + "(";
					s = DFS_print(node.children[0],s);
					s = DFS_print(node.children[1],s);
					s += ")";
				}
			}
		}
		return s;
	}
	
	public HashMap<String,Integer> getTerminalsCount(){
		
		HashMap<String,Integer> terminals = new HashMap<String, Integer>();
		
		for(int t = 0; t < trees.length; t++){
		 
			int numChildren = trees[0].child.children.length;
			
			LinkedList<GPNode> Q = new LinkedList<GPNode>();
			
			Q.add(trees[0].child);
			
			
			//implementando um BFS
			while(!Q.isEmpty()){
				GPNode curr = Q.removeFirst();
				
				//garante que a raiz seja inserida, caso ela seja um terminal
				if(curr.children.length == 0)
				{
					String nodeName = curr.toString();
					//verificaao mal feita de que o n na verdade  uma constante real
					if(nodeName.contains(".")){
						nodeName = "const";
					}
					if(terminals.containsKey(nodeName)){
						
						int kk = terminals.get(nodeName).intValue() + 1;
						terminals.put(nodeName,kk);
					}else
					{
						terminals.put(nodeName, 1);
					}
				}
				
				
				for(int i = 0; i < curr.children.length; i++){
					//Se o numero de filhos  zero chegamos a uma folha
					if(curr.children[i].children.length == 0)
					{
						String nodeName = curr.children[i].toString();
						
						//TODO Consertar verificaao mal feita de que o n na verdade  uma constante real
						if(nodeName.contains(".")){
							nodeName = "const";
						}
						if(terminals.containsKey(nodeName)){
							
							int kk = terminals.get(nodeName).intValue() + 1;
							terminals.put(nodeName,kk);
						}else
						{
							terminals.put(nodeName, 1);
						}
					}
					else{
						Q.add(curr.children[i]);
					}
					
				}
				
				
			}
			
			
		}
		
		
		
		return terminals;
		
	}


	public void insert_user_hits(Pair<Integer,Integer> user_hits){
		
		hits_by_user.addElement(user_hits);
		
	}
	
	public Pair<Integer,Integer> get_user_hits(int u){
		
		return hits_by_user.get(u);
	}
	
	

	public double getValidationFitness() {
		return validationFitness;
	}

	public void setValidationFitness(double validationFitness) {
		this.validationFitness = validationFitness;
	}

	public double getTestFitness() {
		return testFitness;
	}

	public void setTestFitness(double testFitness) {
		this.testFitness = testFitness;
	}

	public double getValidationHits() {
		return validationHits;
	}

	public void setValidationHits(double validationHits) {
		this.validationHits = validationHits;
	}

	public double getTestnHits() {
		return testnHits;
	}

	public void setTestnHits(double testnHits) {
		this.testnHits = testnHits;
	}

	public double getValidationHits_use() {
		return validationHits_use;
	}

	public void setValidationHits_use(double validationHits_use) {
		this.validationHits_use = validationHits_use;
	}

	public double[] getPrec_5() {
		return prec_5;
	}

	public void setPrec_5(double prec_5[]) {
		this.prec_5 = prec_5;
	}

	public double[] getPrec_10() {
		return prec_10;
	}

	public void setPrec_10(double prec_10[]) {
		this.prec_10 = prec_10;
	}

	public double[] getMap_5() {
		return map_5;
	}

	public void setMap_5(double map_5[]) {
		this.map_5 = map_5;
	}

	public double[] getMap_10() {
		return map_10;
	}

	public void setMap_10(double map_10[]) {
		this.map_10 = map_10;
	}
	
	
	
}
