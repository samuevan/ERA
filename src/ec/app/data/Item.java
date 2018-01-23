package ec.app.data;
import java.util.Collections;
import java.util.Vector;




public class Item{
	
	
	private int itemID;
	private int numRankings = 0;
	private Vector<Double> svd_coeficients; 
	//private SparseVector sparse_doubles;
	private Vector<Integer> positions;
	//given_scores stores the scores attributed to the item by the each of the input rankings
	//this vector is paired with the vector positions.
	private double GS_avg = Double.NaN;
	private double GS_median = Double.NaN;
	private double GS_max = Double.NaN;
	private double GS_min = Double.NaN;
	private double GS_std = Double.NaN;
	
	private Vector<Double> given_scores;  
	private Vector<Double> rankScores;
	private Vector<Double> bordaScores; //Esse nao eh o bordascore, segundo a definicao de borda
	private Vector<Double> actual_bordaScores; 
	private Vector<Double> lda_scores;
	private double probTop10 = -1;
	private double timesR = 0;
	private double outrank_score;
	private Vector<Integer> categories;
	private double categories_score = 0;
	private Vector<Double> genericDoubles;
	private boolean useGenericValues = false;
	private boolean use_sparse;
	private double bordaScoreComplete = 0; //The sum of the borda scores for all rankings
	private double combSUMscore;
	private double combMNZscore;
	private double RRFscore;
	
	//TODO Posso armazenar somente os indices das categorias as quais o filme pertence

	private Vector<Integer> agreements;
	private double meanAgreements;

	public Item(int id, int numR){
		
		itemID = id;
		numRankings = numR;
		svd_coeficients = new Vector<Double>(4);
		given_scores = new Vector<Double>(numRankings);
		positions = new Vector<Integer>(numRankings);
		rankScores = new Vector<Double>(numRankings);		
		bordaScores = new Vector<Double>(numRankings);
		actual_bordaScores = new Vector<Double>(numRankings);
		genericDoubles = new Vector<Double>();
		//lda_scores = new Vector<Double>();
		//categories = new Vector<Integer>();
		initializeVectors();
	}
	
	
public Item(int id, int numGenericValues, boolean useGenericValues,boolean use_sparse){
		
		itemID = id;
		this.use_sparse = use_sparse;
		if (!use_sparse){
			genericDoubles =  new Vector<Double>(numGenericValues);
			this.useGenericValues = useGenericValues;
			for (int i = 0; i < numGenericValues; i++)
				genericDoubles.add(0.0);
		}
		
	}
	
	
	private void initializeVectors()
	{
		for(int i = 0; i < numRankings; i++)
		{
			positions.add(-1);
			given_scores.add(Double.NaN);
			rankScores.add(0.0);
			bordaScores.add(0.0);
			actual_bordaScores.add(0.0);
		}
		
	}

	
	
	public void setGenericValue(double val, int pos){
		this.genericDoubles.set(pos, val);
	}
	
	public void setGenericValues(Vector<Double> values){
		this.genericDoubles = new Vector<Double>(values);
	}
	
	/*public void setGenericValuesSparse(int size, double[] values){
		this.sparse_doubles = new SparseVector(size, values);
	}
	
	public double getGenericValueSparse(int pos){
		return this.sparse_doubles.get(pos);
	}*/
	

	public double getGenericValue(int pos){
		return this.genericDoubles.get(pos);
	}
	
	public Vector<Double> getGenericValues(){
		return this.genericDoubles;
	}
	
	public int getGenericValuesSize(){
		/*if (this.use_sparse)
			//return this.sparse_doubles.size();
		else*/
		return this.genericDoubles.size();
	}
	
	
	public Vector<Double> getSVDCoeficients(){
		return svd_coeficients;
	}
	
	public void setSVDCoeficients(Vector<Double> coefs){
		this.svd_coeficients = coefs;
	}
	
	
	public void setRankScore(double score, int rankPos){
		rankScores.set(rankPos, score);
	}
	
	public void addRankScore(double score){
		rankScores.add(score);
	}
	
	public void setBordaScore(double score, int rankPos){
		bordaScores.set(rankPos, score);
	}
	
	public void setActualBordaScore(double score, int rankPos){
		actual_bordaScores.set(rankPos, score);
	}
	
	
	public void setPosition(int pos, int rankPos){
		positions.set(rankPos, pos);
	}
	
	public void setGiven_score(int pos, double given_score){
		given_scores.set(pos,given_score);
	}
	
	public double getRankScore(int rankPos){
		return rankScores.get(rankPos);
	}
	
	/**
	 * 
	 * @param rankPos indica qual  ranking de interesse.
	 * @return o valor de bordaScore para o item presente no score escolhido
	 */
	public double getBordaScore(int rankPos){
		return bordaScores.get(rankPos);
	}
	
	
	public double getBordaScoreComplete(){
		return bordaScoreComplete;
	}
	
	public void setBordaScoreComplete(double bsc){
		bordaScoreComplete = bsc;
	}
	
	
	public double getActualBordaScore(int rankPos){
		return actual_bordaScores.get(rankPos);
	}
	
	public int getItemId(){
		return itemID;
	}
	
	public String toString(){
		
		String s = ""+itemID;				
		
		return s;
	}
	
	public int getNumRankings(){
		
		return numRankings;
	}
	
	
	public int getPosition(int rank){
		return positions.get(rank);
	}
	
	
	public double getProbTop10(){
		return probTop10;
	}
	
	
	public void setProbTop10(double prob){
		this.probTop10 = prob;
	}
	
	public void addNewRanking(){
		positions.add(-1);
		rankScores.add(0.0);
		bordaScores.add(0.0);
	}
	

	public double getTimesR() {
		return timesR;
	}


	public void setTimesR(double timesR) {
		this.timesR = timesR;
	}

	public void setOutrankScore(double outS){
		outrank_score = outS;
	}
	
	public double getOutrankScore(){
		return outrank_score;
	}
	
	public void add_lda_score(double value){
		lda_scores.add(value);
	}
	
	public double get_lda_score(int pos){
		return lda_scores.get(pos);
	}
	
	public int number_lda_scores(){
		return lda_scores.size();
	}

	public void set_lda_scores(Vector<Double> lda_s){
		lda_scores = lda_s;
	}
	
	
	public int num_categories(){
		return categories.size();
	} 
	
	public void add_categorie(int cat_v){
		categories.add(cat_v);
	}
	
	public int get_categorie(int p){
		return categories.get(p);
	}
	
	public void set_categorie(int p, int cat_v){
		categories.set(p, cat_v);
	}
	public void set_categorie(int[] cat_v){
		
		for(int d : cat_v){
			categories.add(d);
			
		}
	}
	
	public Vector<Integer> get_categories(){
		return categories;
	}
	
	public void set_categories_score(double d){
		categories_score = d;
	}
	
	public double get_categories_score(){
		return categories_score;
	}

	public double calcAgreements(int window){
		agreements = new Vector<Integer>();
		for (int p : positions){
			int cur_a = 0;
			if (p != -1) {
				for (int q : positions) {
					if (p - window <= q && p + window >= q){
						cur_a++;
					}
				}
			}
			agreements.add(cur_a);
		}
		meanAgreements = 0;
		for (int a : agreements){
			meanAgreements += a;
		}
		meanAgreements /= numRankings;
		
		return meanAgreements;
	}

	public Vector<Integer> getAgreements(){
		return agreements;
	}
	
	public void setMeanAgreement(double mean_agg){
		this.meanAgreements = mean_agg;
	}
	
	public double getMeanAgreements(){
		return meanAgreements;
	}
	
	
	public void setRRF(double rrf){
		this.RRFscore = rrf;
	}
	
	public double getRRF(){
		return this.RRFscore;
	}
	
	
	public void setCombSUM(double sum){
		this.combSUMscore = sum;
	}
	
	public double getCombSUM(){
		return this.combSUMscore;
	}
	
	
	public void setCombMNZ(double mnz){
		this.combMNZscore = mnz;
	}
	
	public double getCombMNZ(){
		return this.combMNZscore;
	}
	
	
	public double getGS_avg() {
		if (GS_avg == Double.NaN)
			setGS_avg();		
		return GS_avg;
	}


	public void setGS_avg() {
		GS_avg = 0.0;
		int not_nan = 0;
		for (int i = 0; i < numRankings; i++)
			if (!given_scores.get(i).isNaN()){
				GS_avg += given_scores.get(i);
				not_nan++;
			}
		GS_avg /= not_nan;
		
	}


	public double getGS_median() {
		if (GS_median == Double.NaN)
			setGS_median();
		return GS_median;
	}


	public void setGS_median() {
		GS_median = 0.0;
		Vector<Double> valid_given_scores = new Vector<Double>();
		for (int i = 0; i < numRankings; i++)
			if (!given_scores.get(i).isNaN()){
				valid_given_scores.add(given_scores.get(i));
			}
		
		Collections.sort(valid_given_scores);		
		if (valid_given_scores.size()%2 == 0){
			double aux = valid_given_scores.get((int)Math.ceil(valid_given_scores.size()/2)) +
					valid_given_scores.get((int)Math.floor(valid_given_scores.size()/2));
			aux /= 2;					
			 GS_median = aux; 
		}
		else
			GS_median = valid_given_scores.get((int)Math.ceil(valid_given_scores.size()/2));
		
	}


	public double getGS_max() {
		if (GS_max == Double.NaN)
			setGS_median();
		return GS_max;
	}


	public void setGS_max() {
		
		Vector<Double> valid_given_scores = new Vector<Double>();
		for (int i = 0; i < numRankings; i++)
			if (!given_scores.get(i).isNaN()){
				valid_given_scores.add(given_scores.get(i));
			}
		
		GS_max = Collections.max(valid_given_scores);
	}


	public double getGS_min() {
		if (GS_min == Double.NaN)
			setGS_min();
		return GS_min;
	}


	public void setGS_min() {
		
		Vector<Double> valid_given_scores = new Vector<Double>();
		for (int i = 0; i < numRankings; i++)
			if (!given_scores.get(i).isNaN()){
				valid_given_scores.add(given_scores.get(i));
			}
		
		GS_min = Collections.min(valid_given_scores);
	}


	public double getGS_std() {
		if (GS_std == Double.NaN)
			setGS_std();
		return GS_std;
	}


	public void setGS_std() {
		double aux = 0.0;
		int not_nan = 0;
		for (int i = 0; i < numRankings; i++){
			if (!given_scores.get(i).isNaN()){
				aux += Math.pow((given_scores.get(i)-getGS_avg()),2);
				not_nan++;
			}
		}
		
		if (not_nan <=1)
			aux = 0;
		else
			aux = Math.sqrt(aux/(not_nan-1));
		
		GS_std = aux;
	}

	
	
	
	

}
