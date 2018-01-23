package ec.app.data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import net.librec.math.algorithm.SVD;
import net.librec.math.structure.DataMatrix;
import net.librec.math.structure.DenseMatrix;
import ec.app.util.Metrics;
import ec.app.util.Pair;
import ec.app.util.Utils;






import java.util.Arrays;
import java.util.List;
// 


import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;


public class User {

	private int ID;
	private int numRankings;
	private int NumItemsToUse;
	private Map<Integer, Item> Items;
	//private Vector<Pair<Integer,Double>> lda_probabilities;
	public Vector<Vector<Integer>> alternative_rankings;
	public Vector<Integer> validationRanking;
	public Vector<Integer> testRanking; //Vector contendo os items que o usuario avaliou de alguma maneira
	//public Vector<Pair<Integer,Integer>> testRanking2;	//TODO alterar para tambem guardar os ratings dados pelo usuario
	private Vector<Integer> gpra_ranking;
	private Vector<Double> gpra_ranking_scores;
	
	//private Vector<Integer[]>  originalRankings;
	private Vector<Vector<Integer>>  originalRankings;
	private Vector<Vector<Pair<Integer,Double>>>  originalRankingsWithScores;
	private boolean originalRankingsInitialized = false;

	
	/**
	 * 
	 * @param u ID do usuario
	 * @param numR Nmero de rankings dados como entrada
	 * @param numRankIt Nmero de elementos presentes em cada um dos rankings
	 */
	public User(int u, int numR, int i2use)
	{
		ID = u;
		numRankings = numR;
		Items = new HashMap<Integer, Item>();
		NumItemsToUse = i2use;
		//originalRankings = new Vector<Integer[]>();
		testRanking = new Vector<Integer>();
		validationRanking = new Vector<Integer>();
		
		//testRanking2= new Vector<Pair<Integer,Integer>>();				
		gpra_ranking = new Vector<Integer>();
		gpra_ranking_scores = new Vector<Double>();
		
		//inicializa o vetor para salvar o ranking de saida do GPRA
		
		for (int g = 0; g < NumItemsToUse; g++){
			gpra_ranking.add(0);
			gpra_ranking_scores.add(0.0);
			
		}
		
		
		originalRankings = new Vector<Vector<Integer>>();
		originalRankingsWithScores = new Vector<Vector<Pair<Integer,Double>>>();
		alternative_rankings  = new Vector<Vector<Integer>>();
		//Aloca espao para os rankings originais
		if(i2use > 0){
			initializeOriginalRankings();
			originalRankingsInitialized = true;
		}
	}
	
	public Vector<Vector<Integer>> getOriginalRankings(){
		
		return originalRankings;
	}
	
	
	public void freeOriginalRankings(){
		int num_original_rank = originalRankings.size();
		for (int i = 0; i < num_original_rank; i++){
			this.originalRankings.set(i,null);
		}
		this.originalRankings = null;
	}
	
	private void initializeOriginalRankings(){
		
		for(int i = 0; i < numRankings; i++){
			//originalRankings.add(new Integer[numRankItems]);
			originalRankings.add(new Vector<Integer>());
			originalRankingsWithScores.add(new Vector<Pair<Integer,Double>>());
		}
	}
	
	public boolean hasItem(int it){
		
		return Items.containsKey(it);
		//return(aggregatePositions.containsKey(it));
	}
	
	//adciona um item com scores e posicoes vazias
	public void addItem(int it){
		if(!this.hasItem(it)){
			Items.put(it, new Item(it, numRankings));			
		}
			
		
	}
	
	public void addItem(int it, int numGenericValues, boolean useGenericValue, boolean use_sparse){
		if(!this.hasItem(it)){
			Items.put(it, new Item(it, numGenericValues,useGenericValue,use_sparse));			
		}
			
		
	}
	
	
	/*public void setItemGenericValuesSparse(int item, int size, double[] values)
	{
		Items.get(item).setGenericValuesSparse(size, values);
	}*/
	
	public void setItemGenericValue(int item, int pos, double val){
		Items.get(item).setGenericValue(val, pos);
	}
	
	public void setItemScore(int item, int posScore, double score){
		Items.get(item).setRankScore(score, posScore);
	}
	
	
	public void setItemActualBordaScore(int item, int posScore, double score){
		Items.get(item).setActualBordaScore(score, posScore);
	}
	
	public void setItemPosition(int item, int posScore, int pos){
		
		//if(posScore == Items.get(item).getNumRankings()){
			//ocorre no momento em que estou utilizando a funcao 
			//para adcionar um novo ranking ao usurio
			//Items.get(item).addNewRanking();
		//}
		
		Items.get(item).setPosition(pos, posScore);
		
	}
	
	public void setItemGivenScore(int item_id, int pos,
			double item_score) {
		// TODO Auto-generated method stub
		
		Items.get(item_id).setGiven_score(pos, item_score);
	}
	
	
	//Adciona um item a um ranking em uma posicao especifica
	public void addItemOriginalRanking(int it, int rank){
		originalRankings.get(rank).add(it);
	}
		
	//retorna um item de um ranking em uma posicao especifica
	public int getItemOriginalRanking(int rank, int position){
		return originalRankings.get(rank).get(position);
	}
	
	public Vector<Integer> getOriginalRanking(int rank){
		return originalRankings.get(rank);
	}
	
	public void addOriginalRanking(Vector<Integer> rank){
		originalRankings.add(rank);
	}
	
	public void addOriginalRankingWithScores(Vector<Pair<Integer,Double>> rank){
		originalRankingsWithScores.add(rank);
	}
	
	
	//Adciona um item a um ranking em uma posicao especifica
	/*public void addItemOriginalRanking(int it, int rank, int position){
		originalRankings.get(rank)[position] = it;
	}
	
	//retorna um item de um ranking em uma posicao especifica
	public int getItemOriginalRanking(int it, int rank, int position){
		return originalRankings.get(rank)[position];
	}
	
	
	public Integer[] getOriginalRanking(int rank){
		return originalRankings.get(rank);
	}*/
	
	public int getId(){
		return ID;
	}
	
	
	public Vector<Integer> getTestRanking() {
		return testRanking;
	}
	
	public void setTestRanking(Vector<Integer> testRanking){
		this.testRanking = testRanking; 
	}
	
	
	public Vector<Integer> getValidationRanking() {
		return validationRanking;
	}
	
	public void setValidationRanking(Vector<Integer> validationRanking) {
		this.validationRanking = validationRanking;
	}

	
	//TODO alterar nome para getItemIterator
	public Iterator<Integer> getItemIterator(){
		
		return Items.keySet().iterator();
	}



	public Item getItem(Integer item_key) {
		// TODO Auto-generated method stub
		return Items.get(item_key);
	}
	
	
	public int getNumRankings(){
		return numRankings;
	}

	
	public void setNumRankItems(int numRI){
		NumItemsToUse = numRI;
	}
	
	public int getNumRankItems(){
		return NumItemsToUse;
	}
	
	
	/**
	 * Inserts a new ranking and compute some features during the reading time
	 * It is used just to compute unsupervised rank aggregation methods (like Comb* and outrank) 
	 * in reading time
	 * 
	 * Not used anymore
	 * 
	 * @param newRanking
	 */
	public void addNewRanking(Vector<Integer> newRanking){
		
		this.originalRankings.add(new Vector<Integer>());
		numRankings++;
		
		Iterator<Integer> it = Items.keySet().iterator();
		
		//adciona uma posio a mais nos vetores de cada um dos itens
		while(it.hasNext()){
			int item = it.next();
			Items.get(item).addNewRanking();
		}
		
		
		for(int i = 0; i < newRanking.size(); i++){
			
			int item = newRanking.get(i);
			this.addItemOriginalRanking(item, numRankings-1);
			if(!hasItem(item)){
				this.addItem(item); 
				this.setItemPosition(item, numRankings-1, i + 1);
				this.setItemScore(item, numRankings-1,Metrics.calcRankNorm(i+ 1, NumItemsToUse));

			} 
			else {
				
				this.setItemPosition(item, numRankings-1, i + 1);
				this.setItemScore(item, numRankings-1,Metrics.calcRankNorm(i + 1, NumItemsToUse));
			}
		
		}
		
		
		Iterator<Integer> iter = this.getItemIterator();
		
		while(iter.hasNext()){
			
			int item_id = iter.next();
			int timesTop10 = 0;
			
			Item item = this.getItem(item_id);
			
			//itera pelas posicoes do item em cada um dos rankings
			for(int pos = 0; pos < item.getNumRankings(); pos++){
				
				int pos_r = item.getPosition(pos);
				
				//verifica se o item est presente no ranking corrente
				if(pos_r != -1){
					
					int numi = this.getNumItems();
					double d = 1.0 - ((pos_r - 1.0)/(float)(this.getNumItems())); //TODO verificar se |U| deve ser a quantidade de itens para o usuario, ou o tamanho da soma de todos os rankings
					item.setBordaScore(d, pos);
				}
				else
				{
					double init = 1.0 - (NumItemsToUse-1)/(float)this.getNumItems(); //calcula o valor inicial para que os itens que nao estao no ranking nao passem dos itens que estao no ranking
					init = init - (((NumItemsToUse-1)/(float)(2*this.getNumItems())) + 0.005);
					
					double d = init + ((NumItemsToUse-1)/(float)(2* this.getNumItems()));
					item.setBordaScore(d, pos);
				}
				
				
				
				
				//##################################################
				//Calcula o numero de vezes que o item est no top10
				
				if((pos_r != -1) && (pos_r <= 10))
					timesTop10++;
				
				
			}
			
			//##################################################
			//seta a prob do item estar no top10
			
			double p = ((double)timesTop10)/((double)item.getNumRankings());
			item.setProbTop10(p);
			
		}

		
		
		
	}
	
	
	public int getNumItems(){
		return Items.size();
	}
	
	public int getNumAlternativeRankings(){
		return alternative_rankings.size();
	}
	
	public void addAlternativeRanking(Vector<Integer> alt_r){
		alternative_rankings.add(alt_r);
	}
	
	public Vector<Integer> getAlternativeRanking(int pos){
		return alternative_rankings.get(pos);
	}
	
	
	public Vector<Vector<Integer>> getAlternativeRankings(){
		return alternative_rankings;
	}
	
	/**
	 * Computes the features RankScore to all the items recommended
	 * to this user. It is important to notice that there is one feature to each 
	 * input ranking. The features are stored in the respective items
	 * 
	 */
	public void computeItemsRankScore(){
		
		//For each item recommended to the user
		for(Integer item_id : Items.keySet()){			
			
			Item item = Items.get(item_id);
			//Computes the value of the feature for each input ranking
			for (int rank = 0; rank < numRankings; rank++ ){
				int item_pos = item.getPosition(rank);
				if (item_pos != -1){
					double rsc = Metrics.calcRankNorm(item_pos, NumItemsToUse);
					item.setRankScore(rsc, rank);
				}
			} 
			
			
		}
		
	}

	
	
	public void computeItemsBordaScore(){
		
		//For each item recommended to the user
		
		double bsc_max = 0;
		double bsc_min = 0;
		
		for(Integer item_id : Items.keySet()){			
			
			Item item = Items.get(item_id);
			//Computes the value of the feature for each input ranking
			double bsc = 0.0;
			for (int rank = 0; rank < numRankings; rank++ ){
				int item_pos = item.getPosition(rank);
				if (item_pos != -1){
					bsc += NumItemsToUse - (item_pos -1);
				}
			}
			if (bsc > bsc_max)
				bsc_max = bsc;
			else
				if (bsc < bsc_min)
					bsc_min = bsc;
			
			
			item.setBordaScoreComplete(bsc);
			
		}
		
		//NORMALIZING
		for(Integer item_id : Items.keySet()){
			
			Item item = Items.get(item_id);
			double curr_borda = item.getBordaScoreComplete();
			curr_borda = (curr_borda - bsc_min)/(bsc_max - bsc_min);		
					
			item.setBordaScoreComplete(curr_borda);
			
		}
		
		
	}
	
	
	public void computeItemsRRF(int k){
		
		//For each item recommended to the user
		for(Integer item_id : Items.keySet()){			
			
			Item item = Items.get(item_id);
			//Computes the value of the feature for each input ranking
			double rrf = 0.0;
			for (int rank = 0; rank < numRankings; rank++ ){
				int item_pos = item.getPosition(rank);
				if (item_pos != -1){
					rrf += 1.0/(item_pos + k);
				}
			}
			item.setRRF(rrf);
			
		}
		
	}
	
	
	public void computeCombSUM(){

		//For each item recommended to the user
		for(Integer item_id : Items.keySet()){			
			
			Item item = Items.get(item_id);
			//Computes the value of the feature for each input ranking
			double score = 0.0;
			for (int rank = 0; rank < numRankings; rank++ ){
				int item_pos = item.getPosition(rank);
				if (item_pos != -1){
					score += 1.0 - (item_pos-1.0)/this.getNumRankItems();
				}
			}
			item.setCombSUM(score);
			
		}
		

	}	
	
	public void computeCombMNZ(){

		//For each item recommended to the user
		for(Integer item_id : Items.keySet()){			
			
			Item item = Items.get(item_id);
			//Computes the value of the feature for each input ranking
			double score = 0.0;
			int times_on_rank = 0;
			for (int rank = 0; rank < numRankings; rank++ ){
				int item_pos = item.getPosition(rank);
				if (item_pos != -1){
					score += 1.0 - (item_pos-1.0)/this.getNumRankItems();
					times_on_rank++;
				}
			}
			score *= times_on_rank; 
			item.setCombMNZ(score);
			
		}
		

	}	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Computes the probability of the items be placed before the threshold.
	 * @param threshold The percentile to be used as threshold. 
	 * For example, if threshold = 0.3 and the rank size = 10, them we are computing the 
	 * probability of the items be placed in the top 3 positions 
	 */
	public void computeProbOnTopK(double threshold){		
				
		//For each item recommended to the user
		for(Integer item_id : Items.keySet()){			
			
			Item item = Items.get(item_id);
			//Computes the value of the feature for each input ranking
			int times_top_k = 0;
			for (int rank = 0; rank < numRankings; rank++ ){
				int pos_threshold = (int)(threshold * NumItemsToUse);
				int item_pos = item.getPosition(rank);
						
				if((item_pos != -1) && (item_pos <= pos_threshold))
					times_top_k++;
											
			} 
			
			double prob_top_k = (float)times_top_k/numRankings;
			item.setProbTop10(prob_top_k);
			
			
		}
		
	}
	
	
	/**
	 * Computes the number of times each item are recommended to the user
	 *  TODO verify if it is better to store the percentage instead of number of times 
	 */
	public void computeTimesOnRanks(){		
				
		//For each item recommended to the user
		for(Integer item_id : Items.keySet()){			
			
			Item item = Items.get(item_id);
			//Computes the value of the feature for each input ranking
			double times_on_rank = 0;
			for (int rank = 0; rank < numRankings; rank++ ){
				
				int item_pos = item.getPosition(rank);
						
				if((item_pos != -1))
					times_on_rank++;										
			} 
			times_on_rank /= this.numRankings; 			
			item.setTimesR(times_on_rank);
			
			
		}
		
	}
	
	/**
	 * Computes the agreament as defined in the paper Evolutionary RAnk Aggregation for Recommender Systems
	 * This feature consists in the agreement between the rankings considering a sliding windows
	 * 
	 * @param window The size of the sliding window
	 * TODO Verify if it is better to place the function in the User class
	 * TODO Verify if the window is the size of the sliding window or the number of items before and after
	 * 
	 */
	
	public void computeAgreements(int window){
		
		double curr_agg, max_agg = 0, min_agg = 0;
		
		for(Integer item_id : Items.keySet()){			
			Item item = Items.get(item_id);
			curr_agg = item.calcAgreements(window);
			
			if (curr_agg > max_agg)
				max_agg = curr_agg;
			else
				if (curr_agg < min_agg)
					min_agg = curr_agg;			
		}	
		
		
		//NORMALIZING
		for(Integer item_id : Items.keySet()){
			
			Item item = Items.get(item_id);
			double norm_agg = item.getMeanAgreements();
			norm_agg = (norm_agg - min_agg)/(max_agg - min_agg);		
					
			item.setMeanAgreement(norm_agg);;
			
		}
		
		
		
	}
	
	
	/**
	 * This functions returns an average of the difference between the log (natural log) rank positions of items   
	 * item1 and item2 among all the rankings
	 *  
	 * The functions itemsPairwise* are inspired by the formulation presented in the papers:
	 * [1] Learning to rank by aggregating expert preferences
	 * [2] Rank Aggregation via Nuclear Norm Minimization  
	 * @param item1
	 * @param item2
	 * @return
	 */
	private double itemsPairwiseLogRankDifference(Item item1, Item item2){
		
		double val = 0.0;
		double den = 0;
		for (int i = 0; i < numRankings; i++){
			//just compute if both items are present in rank
			if (!((item1.getPosition(i) == -1) | (item2.getPosition(i) == -1))){
				den += 1.0;
				if (item1.getPosition(i) < item2.getPosition(i)){				
					double val_aux = Math.log(item2.getPosition(i))-Math.log(item1.getPosition(i));
					val += val_aux/Math.log(NumItemsToUse);				
				}
			}					
		}
		if (den == 0)				
			return val;
		else
			return val/den;
		
	}
	
	/**
	 * This functions returns an average of the difference between the rank positions of items   
	 * item1 and item2 among all the rankings
	 *  
	 * The functions itemsPairwise* are inspired by the formulation presented in the papers:
	 * [1] Learning to rank by aggregating expert preferences
	 * [2] Rank Aggregation via Nuclear Norm Minimization  
	 * @param item1
	 * @param item2
	 * @return
	 */
	private double itemsPairwiseRankDifference(Item item1, Item item2){
		
		double val = 0.0;
		double den = 0;
		for (int i = 0; i < numRankings; i++){
			//just compute if both items are present in rank
			if (!((item1.getPosition(i) == -1) | (item2.getPosition(i) == -1))){
				den += 1.0;
				if (item1.getPosition(i) < item2.getPosition(i)){				
					double val_aux = item2.getPosition(i)-item1.getPosition(i);
					val += val_aux/NumItemsToUse;				
				}
			}					
		}
		if (den == 0)				
			return val;
		else
			return val/den;
		
	}
	
	
	
	
	/**
	 * This functions returns the number of times item1 is positioned in a better position  
	 * than the item2 
	 *  
	 * The function itemsPairwise* are inspired by the formulation presented in the papers:
	 * [1] Learning to rank by aggregating expert preferences
	 * [2] Rank Aggregation via Nuclear Norm Minimization  
	 * @param item1
	 * @param item2
	 * @return
	 */
	private double itemsPairwiseOccurenceDifference(Item item1, Item item2){
		
		double val = 0.0;
		
		for (int i = 0; i < numRankings; i++){
						
			if (item1.getPosition(i) <= item2.getPosition(i))
				val++;
		}		
		return val;	
	}
	
	
	
	/**
	 * This functions returns 1 if the item1 is positioned in a better position  
	 * than the item2 in the majority of the rankings used as input (more than numRankings/2)
	 *  
	 * The function itemsPairwise* are inspired by the formulation presented in the papers:
	 * [1] Learning to rank by aggregating expert preferences
	 * [2] Rank Aggregation via Nuclear Norm Minimization  
	 * @param item1
	 * @param item2
	 * @return
	 */
	private double itemsPairwiseBinaryDifference(Item item1, Item item2){
		
		double val = 0.0;
		double numRanksIntersection = 0;
		for (int i = 0; i < numRankings; i++){
			if ((item1.getPosition(i) != -1) & (item2.getPosition(i)!= -1)){
				numRanksIntersection++;
				if (item1.getPosition(i) <= item2.getPosition(i))
					val++;
				
			}
		}
		
		if (val >= (numRanksIntersection/2.0) & numRanksIntersection > 0)
			return 1;
		else
			return 0;	
		
	}
	
	
	/*
	private double mean(double[] vet){
		
		double m = 0.0;
		for(int i = 0; i < vet.length; i++){
			m += vet[i];
		}
		
		return m/vet.length;
		
	}
	
	
	private double variance(double[] vet){
		
		double meanx = mean(vet);
		double var = 0.0;
		for(int i = 0; i < vet.length; i++){
			var += Math.pow((vet[i]-meanx),2)/(vet.length-1);
					
		}
		
		return var;
		
	}
	
	private double[] centerData(double vet[]){
		double centered_vet[] = new double[vet.length];
		double meanx = mean(vet);
		double varx = variance(vet);
		
		for (int i = 0; i < centered_vet.length; i++){
			if (varx!=0)
				centered_vet[i] = (vet[i]-meanx)/Math.sqrt(varx);
			else
				centered_vet[i] = (vet[i]-meanx);
			
		}
		
		return centered_vet;
		
	}*/

	
	
	/**
	 * INCOMPLETE
	 * @param array
	 */
	public void computeSVD(double[][] array){
				
		Array2DRowRealMatrix data = new Array2DRowRealMatrix(array);
							
		SingularValueDecomposition svd = new SingularValueDecomposition(data);
		
		Array2DRowRealMatrix U = (Array2DRowRealMatrix) svd.getU();
		Array2DRowRealMatrix VT = (Array2DRowRealMatrix) svd.getVT();		
		
	}
	
	
	
	
	public void computeItemsPairwiseMatrix(){
		
		int NUM_SVD_COEF = 2;
		
		double[][] itemsByItems = new double[Items.size()][Items.size()];
		
		DenseMatrix itemsByItemsBin = new DenseMatrix(Items.size(), Items.size());		
		DenseMatrix itemsByItemsOccur = new DenseMatrix(Items.size(), Items.size());
		DenseMatrix itemsByItemsRank = new DenseMatrix(Items.size(), Items.size());
		DenseMatrix itemsByItemsLogRank = new DenseMatrix(Items.size(), Items.size());
		ArrayList<Integer> items_ids = new ArrayList<Integer>(Items.keySet());
		
		
		
		
		Collections.sort(items_ids);
		//Construct matrix
		for (int i = 0; i < items_ids.size();i++){			
			for(int j = 0; j < items_ids.size();j++){
				if (i!=j){
					double items_diff = itemsPairwiseBinaryDifference(Items.get(items_ids.get(i)), Items.get(items_ids.get(j)));
					itemsByItemsBin.set(i, j, items_diff);
					itemsByItems[i][j] = items_diff;
					
					double items_diffOccu = itemsPairwiseOccurenceDifference(Items.get(items_ids.get(i)), Items.get(items_ids.get(j)));
					itemsByItemsOccur.set(i, j, items_diffOccu);
					
					double items_diffRank = itemsPairwiseRankDifference(Items.get(items_ids.get(i)), Items.get(items_ids.get(j)));
					itemsByItemsRank.set(i, j, items_diffRank);
					
					
					double items_diffLog = itemsPairwiseLogRankDifference(Items.get(items_ids.get(i)), Items.get(items_ids.get(j)));
					itemsByItemsLogRank.set(i, j, items_diffLog);
					
					
				}
				
			}
		}
		
		int num_items = items_ids.size();

		computeSVD(itemsByItems);
		SVD svd = new SVD(itemsByItemsBin);				
		
		DenseMatrix U_reduced = svd.getU().getSubMatrix(0, num_items-1, 0, NUM_SVD_COEF-1);
		DenseMatrix V_reduced = svd.getV().getSubMatrix(0, num_items-1, 0, NUM_SVD_COEF-1);
		double S_reduced[] = Arrays.copyOfRange(svd.getSingularValues(),0,NUM_SVD_COEF-1);
		
		//TODO atribuir os valores ao item
		
		
		
		for (int i = 0; i < num_items; i++){
			
			Vector<Double> svd_coefs = new Vector<Double>();
			
			svd_coefs.add(itemsByItemsBin.sumOfRow(i)/(float)num_items);
			svd_coefs.add(itemsByItemsOccur.sumOfRow(i)/(float)num_items);
			svd_coefs.add(itemsByItemsRank.sumOfRow(i)/(float)num_items);
			svd_coefs.add(itemsByItemsLogRank.sumOfRow(i)/(float)num_items);
			
			Items.get(items_ids.get(i)).setSVDCoeficients(svd_coefs);
			
		}
		
		
		
		
	}
	
	
	
	private void computeGivenScoresStats() {
		// TODO Auto-generated method stub
		
		for(Integer item_id : Items.keySet()){
			Items.get(item_id).setGS_avg();
			Items.get(item_id).setGS_median();
			Items.get(item_id).setGS_max();
			Items.get(item_id).setGS_min();
			Items.get(item_id).setGS_std();			
		}
		
	}
	
	
	public void ComputeFeatures(){
		//computeItemsPairwiseMatrix();
		computeItemsRankScore();
		computeProbOnTopK(0.3); //TODO receive as parameter
		computeTimesOnRanks();
		computeAgreements(2); //TODO receive as parameter
		computeItemsBordaScore();
		computeCombMNZ();
		computeCombSUM();
		computeItemsRRF(60);
		computeGivenScoresStats();
	}
		
	
	

	public Vector<Integer> getGpra_ranking() {
		return gpra_ranking;
	}

	public void setGpra_ranking(Vector<Integer> gpra_ranking) {
		this.gpra_ranking = gpra_ranking;
	}

	public Vector<Double> getGpra_ranking_scores() {
		return gpra_ranking_scores;
	}

	public void setGpra_ranking_scores(Vector<Double> gpra_ranking_scores) {
		this.gpra_ranking_scores = gpra_ranking_scores;
	}
	
	
	
	
	
	public String print_gpra_ranking(int size_ranking){
		String s = this.ID+"\t[";
		
		for (int i = 0; i < size_ranking-1; i++){
			s += gpra_ranking.get(i)+":"+gpra_ranking_scores.get(i)+",";
		}
		
		s += gpra_ranking.get(size_ranking-1)+":"+gpra_ranking_scores.get(size_ranking-1)+"]";
	
		
		return s;
	}


}
