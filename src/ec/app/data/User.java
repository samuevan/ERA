package ec.app.data;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import ec.app.util.Metrics;
import ec.app.util.Pair;
import ec.app.util.Utils;


public class User {

	private int ID;
	private int numRankings;
	private int NumItemsToUse;
	private Map<Integer, Item> Items;
	//private Vector<Pair<Integer,Double>> lda_probabilities;
	public Vector<Vector<Integer>> alternative_rankings;
	public Vector<Integer> validationRanking;
	public Vector<Integer> testRanking; //Vector contendo os items que o usuário avaliou de alguma maneira
	//public Vector<Pair<Integer,Integer>> testRanking2;	//TODO alterar para tambem guardar os ratings dados pelo usuario
	private Vector<Integer> gpra_ranking;
	private Vector<Double> gpra_ranking_scores;
	
	//private Vector<Integer[]>  originalRankings;
	private Vector<Vector<Integer>>  originalRankings2;

	private boolean originalRankingsInitialized = false;

	
	/**
	 * 
	 * @param u ID do usuario
	 * @param numR Número de rankings dados como entrada
	 * @param numRankIt Número de elementos presentes em cada um dos rankings
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
		
		
		originalRankings2 = new Vector<Vector<Integer>>();
		alternative_rankings  = new Vector<Vector<Integer>>();
		//Aloca espaço para os rankings originais
		if(i2use > 0){
			initializeOriginalRankings();
			originalRankingsInitialized = true;
		}
	}
	
	public Vector<Vector<Integer>> getOriginalRankings(){
		
		return originalRankings2;
	}
	
	
	public void freeOriginalRankings(){
		int num_original_rank = originalRankings2.size();
		for (int i = 0; i < num_original_rank; i++){
			this.originalRankings2.set(i,null);
		}
		this.originalRankings2 = null;
	}
	
	private void initializeOriginalRankings(){
		
		for(int i = 0; i < numRankings; i++){
			//originalRankings.add(new Integer[numRankItems]);
			originalRankings2.add(new Vector<Integer>());
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
	
	
	public void setItemGenericValuesSparse(int item, int size, double[] values)
	{
		Items.get(item).setGenericValuesSparse(size, values);
	}
	
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
			//para adcionar um novo ranking ao usuário
			//Items.get(item).addNewRanking();
		//}
		
		Items.get(item).setPosition(pos, posScore);
		
	}
	
	//Adciona um item a um ranking em uma posicao especifica
	public void addItemOriginalRanking(int it, int rank){
		originalRankings2.get(rank).add(it);
	}
		
	//retorna um item de um ranking em uma posicao especifica
	public int getItemOriginalRanking(int rank, int position){
		return originalRankings2.get(rank).get(position);
	}
	
	public Vector<Integer> getOriginalRanking(int rank){
		return originalRankings2.get(rank);
	}
	
	public void addOriginalRanking(Vector<Integer> rank){
		originalRankings2.add(rank);
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
		
		this.originalRankings2.add(new Vector<Integer>());
		numRankings++;
		
		Iterator<Integer> it = Items.keySet().iterator();
		
		//adciona uma posição a mais nos vetores de cada um dos itens
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
				
				//verifica se o item está presente no ranking corrente
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
				//Calcula o numero de vezes que o item está no top10
				
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
			item.setBordaScoreComplete(bsc);
			
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
			int times_on_rank = 0;
			for (int rank = 0; rank < numRankings; rank++ ){
				
				int item_pos = item.getPosition(rank);
						
				if((item_pos != -1))
					times_on_rank++;										
			} 
						
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
		
		for(Integer item_id : Items.keySet()){			
			Item item = Items.get(item_id);
			item.calcAgreements(window);
		}	
	}
	
	
	public void ComputeFeatures(){
		computeItemsRankScore();
		computeProbOnTopK(0.3); //TODO receive as parameter
		computeTimesOnRanks();
		computeAgreements(2); //TODO receive as parameter
		computeItemsBordaScore();
		computeCombMNZ();
		computeCombSUM();
		computeItemsRRF(60);
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
