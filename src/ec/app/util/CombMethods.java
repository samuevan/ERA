package ec.app.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import ec.app.data.Item;
import ec.app.data.User;

public class CombMethods {


	/*public static Vector<Integer> CombSum(User user){

		Vector<Integer> outRank = new Vector<Integer>();
		Vector<Double> outRankScore = new Vector<Double>();

		Iterator it = user.getItemIterator();


		while(it.hasNext()){

			Item item =  user.getItem((Integer) it.next());

			double itScore = 0;
			for(int i = 0; i < user.getNumRankings(); i++){

				itScore += item.getRankScore(i);
			}

			Utils.insertInOrder(item.getItemId(), itScore, outRank, outRankScore);


		}

		return outRank;

	}*/

	public static Vector<Integer> CombMNZ(User user){

		Vector<Integer> outRank = new Vector<Integer>();
		Vector<Double> outRankScore = new Vector<Double>();

		Iterator it = user.getItemIterator();


		while(it.hasNext()){

			Item item =  user.getItem((Integer) it.next());

			double itScore = 0;
			for(int i = 0; i < user.getNumRankings(); i++){

				itScore += item.getRankScore(i);
			}

			Utils.insertInOrder(item.getItemId(), itScore, outRank, outRankScore);


		}

		return outRank;

	}
	
	
	public static Vector<Integer> BordaCount(User user){

		Vector<Integer> BordaRank = new Vector<Integer>();
		Vector<Double> BordaScores = new Vector<Double>();

		Iterator<Integer> it = user.getItemIterator();

		while(it.hasNext()){

			Item item =  user.getItem((Integer) it.next());

			double itScore = 0;
			for(int i = 0; i < user.getNumRankings(); i++){

				itScore += item.getActualBordaScore(i);
			}

			Utils.insertInOrder(item.getItemId(), itScore, BordaRank, BordaScores);


		}

		return BordaRank;

	}
	
	
	
	public static Vector<Integer> CombMNZ(Vector<Vector<Integer>> rankings){


		int sizer = rankings.get(0).size();

		User usr = new User(9999999, rankings.size(),sizer);

		for (Vector<Integer> rank : rankings){ 
			usr.addNewRanking(rank);
		}

		return CombMNZ(usr);
	}

	//a diferena entre o combSUM e o combMNZ  que o primeiro utiliza uma 
	//funcao de score que atribui score posicional para qualquer item
	// e o segundo usa uma funcao de score que s atribui score posicional se o item aparece em um ranking.
	public static Vector<Integer> CombSUM(User user){

		Vector<Integer> outRank = new Vector<Integer>();
		Vector<Double> outRankScore = new Vector<Double>();

		Iterator it = user.getItemIterator();


		while(it.hasNext()){

			Item item =  user.getItem((Integer) it.next());

			double itScore = 0;
			for(int i = 0; i < user.getNumRankings(); i++){

				itScore += item.getBordaScore(i);
			}
			//Utils.insertInOrder(1, 2.3, outRank, outRankScore);

			Utils.insertInOrder(item.getItemId(), itScore, outRank, outRankScore);


		}

		return outRank;

	}	

	
	public static Vector<Integer> CombSUM(Vector<Vector<Integer>> rankings){


		int sizer = rankings.get(0).size();

		User usr = new User(9999999, rankings.size(),sizer);

		for (Vector<Integer> rank : rankings){ 
			usr.addNewRanking(rank);
		}

		return CombSUM(usr);
	}



	
	
	
	
	public static Vector<Integer> outrank_approach(User u, int rankings_size, int num_rankings) {

		// TODO passar esses valores por parmetro ou decidi-los com base no
		// tamanho da entrada
		int min_dist_to_better = 0;
		int min_dist_to_worse = (int) Math.round(0.75*rankings_size);

		int conc_threshold = num_rankings/4;
		int disc_threshold = 0;

		Iterator<Integer> iter = u.getItemIterator();

		// tabela que vai guardar as comparacoes entre os itens
		int concordance_table[][] = new int[u.getNumItems()][u.getNumItems()];
		int discordance_table[][] = new int[u.getNumItems()][u.getNumItems()];
		int coalition_table[][] = new int[u.getNumItems()][u.getNumItems()];
		HashMap<Integer, Integer> items_pos = new HashMap<Integer, Integer>();

		// hash para mapear cada item do usurio em uma posicao que ser usada
		// na tabela comp_table
		int posi = 0;
		while (iter.hasNext()) {

			int itemx = iter.next();
			items_pos.put(itemx, posi);
			posi++;
		}

		Iterator<Integer> iter2 = u.getItemIterator();

		while (iter2.hasNext()) {
			int x = iter2.next();
			// System.out.println(x);
			for (int i = 0; i < u.getNumRankings(); i++) {
				Item item1 = u.getItem(x);
				if((item1.getPosition(i) > 0)){ //Garante que s so comparados itens que esto no mesmo ranking 

					for (int j = 0; j < u.getOriginalRanking(i).size(); j++) {
						int item2x = u.getItemOriginalRanking(i, j);


						Item item2 = u.getItem(item2x);

						// caso o item1 esteja em uma posicao anterior ao item2 com
						// uma distancia de no mnimo min_dist_to_better
						// somo mais 1 na tabela de melhor
						if ((item2.getPosition(i) - item1.getPosition(i)) >= min_dist_to_better) {

							int a = items_pos.get(item1.getItemId());
							int b = items_pos.get(item2x);
							concordance_table[a][b] += 1;
						} else {
							// caso contrrio  somado mais 1 na tabela de pior
							if ((item1.getPosition(i) - item2.getPosition(i)) >= min_dist_to_worse) {
								discordance_table[items_pos.get(item1.getItemId())][items_pos
								                                                    .get(item2x)] += 1;
							}
						}

					}
				}
			}

		}

		for (int i = 0; i < u.getNumItems(); i++) {
			for (int j = 0; j < u.getNumItems(); j++) {

				if ((concordance_table[i][j] >= conc_threshold)
						& (discordance_table[i][j] <= disc_threshold)) {
					coalition_table[i][j] = 1;
				}
			}
		}


		Vector<Integer> outranking_res = outrank_approach_aux(coalition_table, u, items_pos); 	

		return outranking_res;

		// (negativo)

	}
	
	public static Vector<Integer> outrank_approach(Vector<Vector<Integer>> rankings, int rankings_size, int num_rankings){


		int sizer = rankings.get(0).size();

		User usr = new User(9999999, rankings.size(),sizer);

		for (Vector<Integer> rank : rankings){ 
			usr.addNewRanking(rank);
		}

		return outrank_approach(usr,rankings_size,num_rankings);
	}


	//TODO dar nome apropriado
	private static Vector<Integer> outrank_approach_aux(int[][] coalition_table, User u, HashMap<Integer,Integer> items_pos){


		if (items_pos.size() < 1){

			Vector<Integer> results = new Vector<Integer>();

			for(int item : items_pos.keySet()){
				results.add(item);
			}

			return results;

		}
		else{

			/*
		Iterator<Integer> iter_pos = items_pos.keySet()
		while (iter.hasNext()) {
			int item = iter.next();
			for(int item2 : items_pos.keySet()){ 
			//for (int j = 0; j < u.getNumItems(); j++) {
				concordance_vector[items_pos.get(item)] += coalition_table[items_pos
						.get(item)][item2];
			}
		}


		iter = u.getItemIterator();
		while (iter.hasNext()) {
			int item = iter.next();
			for(int item2 : items_pos.keySet()){ 
			//for (int j = 0; j < u.getNumItems(); j++) {
				discordance_vector[items_pos.get(item)] += coalition_table[item2][items_pos
						.get(item)];
			}
		}
			 */

			int concordance_vector[] = new int[u.getNumItems()];
			for (int item1 : items_pos.keySet()){
				for(int item2 : items_pos.keySet()){

					if(item1 != item2){
						concordance_vector[items_pos.get(item1)] += 
								coalition_table[items_pos.get(item1)][items_pos.get(item2)];
					}

				}

			}


			int discordance_vector[] = new int[u.getNumItems()];
			for (int item1 : items_pos.keySet()){
				for(int item2 : items_pos.keySet()){

					if(item1 != item2){
						discordance_vector[items_pos.get(item1)] += 
								coalition_table[items_pos.get(item2)][items_pos.get(item1)];
					}

				}

			}



			//TODO refazer com array
			double coalition_vector[] = new double[u.getNumItems()];
			/*iter = u.getItemIterator();
		double max = Double.NEGATIVE_INFINITY;
		while (iter.hasNext()) {
			int item = iter.next();
			for (int j = 0; j < u.getNumItems(); j++) {
				coalition_vector[items_pos.get(item)] = concordance_vector[items_pos.get(item)] - 
						discordance_vector[items_pos.get(item)];
				if (max < coalition_vector[items_pos.get(item)])
					max = coalition_vector[items_pos.get(item)];
			}
		}*/

			double max = Double.NEGATIVE_INFINITY;
			for (int item1 : items_pos.keySet()) {
				coalition_vector[items_pos.get(item1)] = concordance_vector[items_pos.get(item1)] - 
						discordance_vector[items_pos.get(item1)];
				if (max < coalition_vector[items_pos.get(item1)])
					max = coalition_vector[items_pos.get(item1)];

			}


			/*
		Vector<Integer> result = new Vector<Integer>(); 
		while (iter.hasNext()) {
			int item = iter.next();
			for (int j = 0; j < u.getNumItems(); j++) {
				if (coalition_vector[items_pos.get(item)] == max){
					result.add(item);
					coalition_vector[items_pos.get(item)] = Double.NEGATIVE_INFINITY;
					items_pos.remove(item);
				} 
			}
		}

			 */

			Vector<Integer> result = new Vector<Integer>();
			HashMap<Integer, Integer> new_items_pos = new HashMap<Integer, Integer>();
			for (int item1 : items_pos.keySet()) {
				if (coalition_vector[items_pos.get(item1)] == max){
					result.add(item1);
					coalition_vector[items_pos.get(item1)] = Double.NEGATIVE_INFINITY;
					//items_pos.remove(item1);
				}
				else{
					new_items_pos.put(item1, items_pos.get(item1));
				}

			}

			result.addAll(outrank_approach_aux(coalition_table, u, new_items_pos));

			return result;
		}

	}

}
