package ec.app.data;
import java.util.Vector;
import librec.data.SparseVector;




public class Item{
	
	
	private int itemID;
	private int numRankings = 0;
	private SparseVector sparse_doubles;
	private Vector<Integer> positions;
	private Vector<Double> rankScores;
	private Vector<Double> bordaScores; //Esse nao eh o bordascore, segundo a definicao de borda
	private Vector<Double> actual_bordaScores; 
	private Vector<Double> lda_scores;
	private double probTop10 = -1;
	private int timesR = 0;
	private double outrank_score;
	private Vector<Integer> categories;
	private double categories_score = 0;
	private Vector<Double> genericDoubles;
	private boolean useGenericValues = false;
	private boolean use_sparse;
	
	//TODO Posso armazenar somente os indices das categorias as quais o filme pertence

	private Vector<Integer> agreements;
	private double meanAgreements;

	public Item(int id, int numR){
		
		itemID = id;
		numRankings = numR;
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
	
	public void setGenericValuesSparse(int size, double[] values){
		this.sparse_doubles = new SparseVector(size, values);
	}
	
	public double getGenericValueSparse(int pos){
		return this.sparse_doubles.get(pos);
	}
	

	public double getGenericValue(int pos){
		return this.genericDoubles.get(pos);
	}
	
	public Vector<Double> getGenericValues(){
		return this.genericDoubles;
	}
	
	public int getGenericValuesSize(){
		if (this.use_sparse)
			return this.sparse_doubles.size();
		else
			return this.genericDoubles.size();
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
	
	public double getRankScore(int rankPos){
		return rankScores.get(rankPos);
	}
	
	/**
	 * 
	 * @param rankPos indica qual é ranking de interesse.
	 * @return o valor de bordaScore para o item presente no score escolhido
	 */
	public double getBordaScore(int rankPos){
		return bordaScores.get(rankPos);
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
	

	public int getTimesR() {
		return timesR;
	}


	public void setTimesR(int timesR) {
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

	public void calcAgreements(int window){
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
	}

	public Vector<Integer> getAgreements(){
		return agreements;
	}
	public double getMeanAgreements(){
		return meanAgreements;
	}

}
