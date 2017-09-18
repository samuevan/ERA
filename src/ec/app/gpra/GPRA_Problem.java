package ec.app.gpra;
/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

//TODO Alterar o nome da classe
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream.PutField;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Vector;

import ec.util.*;
import ec.*;
import ec.app.data.InputData;
import ec.app.data.Item;
import ec.app.data.User;
import ec.app.util.Metrics;
import ec.app.util.Pair;
import ec.app.util.Utils;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;
import librec.data.SparseVector;




public class GPRA_Problem extends GPProblem implements
		SimpleProblemForm {
	private static final long serialVersionUID = 1;

	private InputData dados;
	private boolean save_ranking=false;
	public double meanAgreements;
	private int numUsedRanks = 0;
	public double[] scores; //= new double[20];//{-1000,-1000,-1000,-1000,-1000,-1000,-1000,-1000,-1000,-1000};
	
	public double[] genericDoubles; //= new double[20];
	//public SparseVector genericDoubles;
	public double probTop10 = 0;
	public int timesOnRankings = 0;
	public double outrank_score = 0;
	public double bordaComplete = 0;
	
	public void setup(final EvolutionState state, final Parameter base) {
		
		
		super.setup(state, base);
		//TODO pegar os parâmetros a partir do arquivo de parametros
		
		System.out.println("Evaluation");
		dados = GPRA_Principal.getData();
		numUsedRanks = GPRA_Principal.getNumUsedRanks();
		
		
    	//*******************************************************************
		
		// verify our input is the right class (or subclasses from it)
		if (!(input instanceof DoubleData))
			state.output.fatal("GPData class must subclass from "
					+ DoubleData.class, base.push(P_DATA), null);
	
	
	}
	
	public void set_data(InputData new_data){
		this.dados = null;
		this.dados = new_data;
	}
	
	public void set_save_ranking(boolean save){		
		save_ranking = save;
	}

	
	public Vector<Integer> generate_ranking(final EvolutionState state, final Individual ind,
			final int subpopulation, final int threadnum, User usr){
		
		DoubleData input = (DoubleData) (this.input);

		//int numItemsToEval = dados.getNumRankItems();
		int numItemsToSuggest = dados.getNumItemsToSuggest();
		int numItemsToUse = dados.getNumRankItems();
		numUsedRanks = dados.getNumRankings();
		//int hits = 0;
		//double sum = 0.0;
		//double expectedResult;
		//double result;
		
		Vector<Pair<Integer,Double>> saida = new Vector<Pair<Integer,Double>>(); //<Item,Novo Score>
	
		//Vector<Integer> saida_items = new Vector<Integer>();
		//Vector<Double> saida_scores = new Vector<Double>();		

		//itera pelos usuarios presentes nos rankings	
		Vector<Integer> saida_items = new Vector<Integer>();
		Vector<Double> saida_scores = new Vector<Double>();

		
		Iterator<Integer> item_iter = usr.getItemIterator();
		
		while(item_iter.hasNext()){

			Integer item_key = item_iter.next();
			Item item = usr.getItem(item_key);

			
			genericDoubles = new double[20];
			//genericDoubles = new SparseVector(20);
			if (!dados.UsingSparse()){
				for (int gds = 0; gds < item.getGenericValuesSize(); gds++){
					//genericDoubles.add(gds,item.getGenericValue(gds));
					genericDoubles[gds] = item.getGenericValue(gds);
				}
			}else{
				
				for (int gds = 0; gds < item.getGenericValuesSize(); gds++){
					//genericDoubles.add(gds,item.getGenericValueSparse(gds));
					genericDoubles[gds] = item.getGenericValueSparse(gds);
				}
			}
			
			
			
			//Get the values of rank scores
			for(int nr = 0; nr < numUsedRanks; nr++){
				scores[nr] = item.getRankScore(nr);
			}

			//Get the values of borda scores
			//for(int nr = 0; nr < numUsedRanks; nr++){
			//	bordascores[nr] = item.getBordaScore(nr);
			//}
			probTop10 = item.getProbTop10();			
			timesOnRankings = item.getTimesR();
			outrank_score = item.getOutrankScore()*4; //TODO pq estou multiplicando por 4???			
			meanAgreements = item.getMeanAgreements();
			bordaComplete = item.getBordaScoreComplete();
			//categorie_score = item.get_categories_score();

			((GPIndividual) ind).trees[0].child.eval(state, threadnum,
					input, stack, ((GPIndividual) ind), this);


			double newScore = input.x;
			//System.out.println(ind);
			//insertInOrder(new Pair<Integer,Double>(item.getItemId(), newScore), saida);//Insere o item e o novo ranking
			Utils.insertInOrder(item_key,newScore,saida_items,saida_scores);
		}

		
		return saida_items;
		
		
	}
	
	
	public void evaluate(final EvolutionState state, final Individual ind,
			final int subpopulation, final int threadnum) {
		if (!ind.evaluated) // don't bother reevaluating
		{
			
			DoubleData input = (DoubleData) (this.input);

			//int numItemsToEval = dados.getNumRankItems();
			int numItemsToSuggest = dados.getNumItemsToSuggest();
			int numItemsToUse = dados.getNumRankItems();
			//I'm taking the number of rankings from the user, 
			//therefore i can control how many rankings are used by  each user
			//The first step to handle users with different number of rankings
			//numUsedRanks = dados.getNumRankings(); 
			int hits = 0;
			double sum = 0.0;
			double expectedResult;
			double result;
			
			Vector<Pair<Integer,Double>> saida = new Vector<Pair<Integer,Double>>(); //<Item,Novo Score>
		
			//Vector<Integer> saida_items = new Vector<Integer>();
			//Vector<Double> saida_scores = new Vector<Double>();
			
			Vector<User> users = dados.getUsers();
			
			double prec_test = 0;
			double prec_val = 0;
			float mean_hits_sug = 0;
			float mean_hits_use = 0;
			double prec_5_t = 0, prec_5_v = 0, prec_10_t = 0, prec_10_v = 0;
			double map_5_t = 0, map_5_v = 0, map_10_t = 0, map_10_v = 0;
			
			
			//itera pelos usuarios presentes nos rankings
			for(int user_pos = 0; user_pos<users.size(); user_pos++){
				
				Vector<Integer> saida_items = new Vector<Integer>();
				Vector<Double> saida_scores = new Vector<Double>();
				
				User curr_user = users.get(user_pos); 
				numUsedRanks = curr_user.getNumRankings();
				
				Iterator<Integer> item_iter = curr_user.getItemIterator();
				

				
				
				while(item_iter.hasNext()){
					
					Integer item_key = item_iter.next();
					Item item = curr_user.getItem(item_key);
					
					
					genericDoubles = new double[20];
					//genericDoubles = new SparseVector(20);
					if (!dados.UsingSparse()){
						for (int gds = 0; gds < item.getGenericValuesSize(); gds++){
							//genericDoubles.add(gds,item.getGenericValue(gds));
							genericDoubles[gds] = item.getGenericValue(gds);
						}
					}else{
						
						for (int gds = 0; gds < item.getGenericValuesSize(); gds++){
							//genericDoubles.add(gds,item.getGenericValueSparse(gds));
							genericDoubles[gds] = item.getGenericValueSparse(gds);
						}
					}
										
					//Get the values of rank scores
					scores = new double[20];
					for(int nr = 0; nr < numUsedRanks; nr++){
						scores[nr] = item.getRankScore(nr);
					}
					
					//Get the values of borda scores
					/*for(int nr = 0; nr < numUsedRanks; nr++){
						bordascores[nr] = item.getBordaScore(nr);
					}*/
					
					probTop10 = item.getProbTop10();
					
					timesOnRankings = item.getTimesR();
					
					outrank_score = item.getOutrankScore();
					
					meanAgreements = item.getMeanAgreements();
					bordaComplete = item.getBordaScoreComplete();

					//categorie_score = item.get_categories_score();
					
					
					((GPIndividual) ind).trees[0].child.eval(state, threadnum,
							input, stack, ((GPIndividual) ind), this);
					
					
					double newScore = input.x;
					//System.out.println(ind);
					//insertInOrder(new Pair<Integer,Double>(item.getItemId(), newScore), saida);//Insere o item e o novo ranking
					Utils.insertInOrder(item_key,newScore,saida_items,saida_scores);
				}
				
				
				//This guarantess that any output ranking will always have a minimum size of numItemsToSuggest
				//by adding fake items at the end of a ranking if it does not have the minimum size
				if (saida_items.size() < numItemsToSuggest){
					int num_to_intersert = numItemsToSuggest - saida_items.size();
					double smallest = saida_scores.lastElement();
					for (int i = 0; i < num_to_intersert; i++){
						saida_items.add(9999999);
						saida_scores.add(smallest - 100);
					}
				}
				
				
				
				
				
				
				long siz = ind.size();
				Vector<Integer> user_hits = new Vector<Integer>();
				double prec_test_aux = Metrics.precision(curr_user.getTestRanking(), saida_items,null,numItemsToSuggest);
				double prec_val_aux = Metrics.precision(curr_user.getValidationRanking(), saida_items, user_hits,numItemsToSuggest);								
				
				//System.out.println(saida_items);
				//System.out.println(saida_scores);
				
				//armazena o ranking gerado para o usuario
				if (save_ranking){
					
					for(int j = 0; j < numItemsToSuggest; j++){
						Vector<Integer> r = curr_user.getGpra_ranking();
						curr_user.getGpra_ranking().set(j,saida_items.get(j));
						curr_user.getGpra_ranking_scores().set(j,saida_scores.get(j)); // add(saida_scores.get(j));
					}
					
				}
				
				//TODO alterar pra pegar a soma diretamente na funcao de precisao
				int sum_items = 0;
				for (Integer ix : user_hits){
					sum_items += ix;
				}
				
				//Inserindo os hits do usuario para fazer Fitness Sharing posteriormente
				((MyIndividual) ind).insert_user_hits(new Pair<Integer, Integer>(user_pos, sum_items));
				
				
				
				int hitsx_sug = Metrics.numHits(saida_items, curr_user.getValidationRanking(), numItemsToSuggest);
				//int hitsx_use = Metrics.numHits(saida_items, curr_user.getValidationRanking(), numItemsToEval);
				mean_hits_sug += hitsx_sug;
				//mean_hits_use += hitsx_use;
				prec_test += prec_test_aux;
				prec_val += prec_val_aux;
				
				
				
				
				
				//*************************LOG******************
				/*
				prec_5_t += Metrics.precision_at(curr_user.getTestRanking(), saida_items,5);
				prec_5_v += Metrics.precision_at(curr_user.getValidationRanking(), saida_items,5);
				prec_10_t += Metrics.precision_at(curr_user.getTestRanking(), saida_items,10);
				prec_10_v += Metrics.precision_at(curr_user.getValidationRanking(), saida_items,10);
				
				map_5_t += Metrics.precision(curr_user.getTestRanking(), saida_items,5);
				map_5_v += Metrics.precision(curr_user.getValidationRanking(), saida_items,5);
				map_10_t += Metrics.precision(curr_user.getTestRanking(), saida_items,10);
				map_10_v += Metrics.precision(curr_user.getValidationRanking(), saida_items,10);
				*/
				//*************************END LOG ********************************************
			}
			//long siz = ind.size();
			//***************************LOG******************************
			/*prec_5_t /= dados.getNumUsersTestHasElem();
			prec_5_v /= dados.getNumUsersValHasElem();
			prec_10_t /= dados.getNumUsersTestHasElem();
			prec_10_v /= dados.getNumUsersTestHasElem();
			
			map_5_t /= dados.getNumUsersTestHasElem();
			map_5_v /= dados.getNumUsersTestHasElem();
			map_10_t /= dados.getNumUsersTestHasElem();
			map_10_v /= dados.getNumUsersTestHasElem();
			*/
			((MyIndividual)ind).setPrec_5(new double[] {prec_5_t,prec_5_v});
			((MyIndividual)ind).setPrec_10(new double[] {prec_10_t,prec_10_v});
			((MyIndividual)ind).setMap_5(new double[] {map_5_t,map_5_v});
			((MyIndividual)ind).setMap_10(new double[] {map_10_t,map_10_v});
			//****************************END LOG ***********************
			
			
			
			//TODO dados.getNumUsersTestHasElem(); retorna o numero de usuarios no arquivo de teste
			//contudo, pode ser que um usuario esteja no teste mas não nos rankings de entrada
			//na ML1M isso acontece com o user 5850
			
			int a = dados.getNumUsersTestHasElem();
			int b = dados.getNumUsersValHasElem();
			
			double map_test = prec_test/dados.getNumUsersTestHasElem();
			double map_val = prec_val/dados.getNumUsersValHasElem();
			
			
			
			// the fitness better be KozaFitness!
			KozaFitness f = ((KozaFitness) ind.fitness);
			//f.setFitness(state, map,false); //set the fitness but say it is not ideal
			f.setStandardizedFitness(state, 1-map_val);
			((MyIndividual)ind).setTestFitness(1-map_test);
			((MyIndividual)ind).setValidationFitness(1-map_val);
			
			((MyIndividual)ind).setValidationHits(mean_hits_sug/dados.getNumUsersValHasElem());
			((MyIndividual)ind).setValidationHits_use(mean_hits_use/dados.getNumUsersValHasElem());
			
			
			f.hits = Math.round(mean_hits_sug/dados.getNumUsersValHasElem());
			ind.evaluated = true;
			 //System.out.println("Individual " + ind);
			 //System.out.println("Fitness: " + f);
		}
	}
	
	
	
	public void insertInOrder(Pair<Integer,Double> x, Vector<Pair<Integer,Double>> vet){
		
		
		int i = 0;
		while(x.getSecond() < vet.get(i).getSecond()){
			i++;
		}
		
		vet.insertElementAt(x, i); //insere o item na posicao correta de acordo com o seu score
		
	}


	public void set_num_used_rankings(int numRankings) {
		// TODO Auto-generated method stub
		numUsedRanks = numRankings;
	}
	
	
	public void initialize_input(){
		this.input = new GPData();
	}
	
	
}
