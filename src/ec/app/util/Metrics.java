package ec.app.util;

import java.util.Collections;
import java.util.Map;
import java.util.Vector;

import ec.app.data.InputData;
import ec.app.data.User;

public class Metrics {
	
	
	//TODO qual é o valor do dcg@100 para uma lista com apenas 10 elementos?
	public static double dcg(Vector<Integer> vet, int k) {

		double res = 0.0;
		if (vet.size() > 0) {
			res = vet.get(0);
			
			//TODO alterar i < vet.size() por i < k
			for (int i = 1; i < vet.size(); i++) {
				res += (Math.pow(2, vet.get(i)) - 1) / Utils.log2(i + 1);
			}
		}
		return res;
	}
	
	//TODO ERRADO conferir como implementar
	public static double NDCG(Vector<Pair<Integer,Integer>> realranking, Vector<Integer> predictedRanking, 
	           int k){
		
		Collections.sort(realranking);
		Vector<Integer> realRankingValues = new Vector<Integer>();
		
		for(int i = 0; i < realranking.size(); i++){
			realRankingValues.add(realranking.get(i).getSecond());
		}
		
		double real_r= dcg(realRankingValues,k);
		double pred_r = dcg(predictedRanking,k);
		
		
		return pred_r/real_r;
	}
	
	/**
	 * TODO estou fazendo uma funcao para calcular o NDCG e outra para calcular o NDCG m�dio do conjunto de testes,
	 * 
	 * 
	 * 
	 * @param ranking Lista de itens ordenados que ser� comparada com a ordena��o perfeita
	 * @param real Ordena��o real(perfeita) dos itens
	 * @param real_rates O valor de rate de cada um dos itens da ordena��o real, 
	 * os valores estao ordenados de acordo com a numeracao dos itens
	 * @return Valor do NDCG para o ranking
	 */
	public static double NDCG(Vector<Integer> ranking, Vector<Integer> real, 
			           Vector<Integer> real_rates){
		
		int i = 0;
		double ndcg = 0;
		double dcg_rank = 0, dcg_real = 0;
		
		while(i < ranking.size()){
			
			dcg_rank += ((float) Math.pow(2,real_rates.get(ranking.get(i)-1))-1)/Math.log(i + 2); //TODO estou usando log natural, ok?
			//System.out.println("rank: " + dcg_rank);
			dcg_real += ((float) Math.pow(2,real_rates.get(real.get(i)-1))-1)/Math.log(i + 2); //TODO estou usando log natural, ok?
			///System.out.println("real: " + dcg_real);
			
			i++;
		}
		
		ndcg = dcg_rank/dcg_real;
		
		
		return ndcg;
	}

	
/*	public static double MAP(Map<Integer,Vector<Integer>> testUsers, Vector<User> usuarios){
		
		
		double mp = 0;
		for(int i = 0; i < usuarios.size(); i++){
			int user = usuarios.get(i).getId();
			int numItems = testUsers.get(user).size();
			double p = precision(( testUsers.get(user).toArray(new Integer[numItems])),usuarios.get(i).getOriginalRanking(0));
			mp += p;
			
		}
			
		return mp/usuarios.size();
	}
	*/
	
	
	public static double precision(Integer[] realRanking,Integer[] rankingToEval, int numItemsToEval){
		
		double hits = 0.0;
		double avg_prec = 0.0;
		
		for(int i = 0; i < numItemsToEval; i++){ //int i = 0; i < rankingToEval.length; i++
			if(hasItem(realRanking,rankingToEval[i])){
				hits = hits+1;
				avg_prec += (double) hits/(i + 1);
			}
		}
		
		if (hits != 0){
			//double prec = hits/rankingToEval.length;//realRanking.length;
			double prec = avg_prec/Math.min(realRanking.length,numItemsToEval);//numItemsToEval;//rankingToEval.length;
			return prec;
		}
		else
		{
			return 0;
		}
		//return prec;
	}
	
	
	public static double precision_at(Vector<Integer> vector,Vector<Integer> saida_items, int numItemsToEval){
		
		double hits = 0.0;
		double avg_prec = 0.0;
		
		for(int i = 0; i < numItemsToEval; i++){ //int i = 0; i < rankingToEval.length; i++
			if(hasItem(vector,saida_items.get(i))){
				hits = hits+1.0;
			}
		}
		
		if (hits != 0){
			//double prec = hits/rankingToEval.length;//realRanking.length;
			double prec = hits/numItemsToEval;//rankingToEval.length;
			return prec;
		}
		else
		{
			return 0;
		}
		
	}
	
	
	
	public static double precision(Vector<Integer> realRanking,Vector<Integer> rankingToEval,Vector<Integer> hits_ids, int numItemsToEval){
		
		double hits = 0.0;
		double avg_prec = 0.0;
		
		
		
		//Considerando somente os 100 primeiros
		for(int i = 0; i < numItemsToEval; i++){ //int i = 0; i < rankingToEval.size(); i++
			int kx = rankingToEval.get(i);
			if(hasItem(realRanking,rankingToEval.get(i))){
				hits = hits+1;
				avg_prec += (double) hits/(i + 1);
				//verifica se devo salvar os hits 
				if (hits_ids != null) 
					hits_ids.add(rankingToEval.get(i));
			}
		}
		
		if (hits != 0){
			//double prec = hits/rankingToEval.length;//realRanking.length;
			double prec = avg_prec/Math.min(realRanking.size(),numItemsToEval);//numItemsToEval;//rankingToEval.size();//
			return prec;
		}
		else
		{
			return 0;
		}
		//return prec;
	}
	

	//TODO mover o hasItem para a classe Utils 
	private static boolean hasItem(Vector<Integer> list, int it){
	
		for(int i = 0; i < list.size(); i++){
			if(list.get(i) == it)
				return true;
		}
	
		return false;
	}

	private static boolean hasItem(Integer[] list, int it){
		
		for(int i = 0; i < list.length; i++){
			if(list[i] == it)
				return true;
		}
		
		return false;
	}
	
	
	public static double calcRankNorm(int pos, int sizeRanking){
		
		
		double d = 1 - ((double) pos - 1)/sizeRanking;
		
		return d;
		
	}

	
	public double calcBordaNorm(int item, Vector<Integer> ranking, int pos, int sizeU){
		
		if(hasItem(ranking, item)){
			double d = 1 - ((double) pos - 1) / sizeU;
			return d;
		}else
		{
			return (0.5 + (ranking.size()-1)/(2*sizeU)); 
		}
		
	}
	
	
	
	
	
	/**
	 * Calc the best MAP using the elements in the input rankings
	 * @return
	 */
	public double bestMap(Vector<User> users){
		
		double map = 0.0;
		
		Vector<Integer> aggregate = new Vector<Integer>();
		
		for(int u = 0; u < users.size(); u++ ){
			
			int numR = users.get(u).getNumRankings();
			
			for(int r = 0; r < numR; numR++){
				
				int size = users.get(u).getNumRankItems();
				
				for(int i = 0; i<size; i++){
					
				}
			}
			
		}
		
		
		return map;
	}
	
	public static void posicaoMediaHits(Vector<User> usuarios){
		
		Vector<Vector<Integer>> hitsPositions = new Vector<Vector<Integer>>();
		
		int numRankings = usuarios.get(0).getNumRankings();
		
		for(int i = 0; i < numRankings; i++){
			hitsPositions.add(new Vector<Integer>());
		}
		
		for(User usr : usuarios){
			Vector<Vector<Integer>> positions = Utils.hitsPositions(usr.getOriginalRankings(),
																	usr.getValidationRanking());
			
			for(int i = 0; i < hitsPositions.size(); i++){
					hitsPositions.get(i).addAll(positions.get(i));
			}
			
		}
		
		
		for(Vector<Integer> v : hitsPositions){
			
			System.out.println(v);
		}
		
		
	}
	
	
	
	public static int numHits(Vector<Integer> rank_to_test, Vector<Integer> test, int num_itens){
		
		int hits = 0;
		
		for(int i = 0; i < num_itens; i++){
			int item = rank_to_test.get(i);
			if(hasItem(test,item))
				hits++;
			
		}
		
		return hits;
		
	}
	
	
}
