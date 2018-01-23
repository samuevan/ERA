package ec.app.gpra;
import java.util.HashMap;
import java.util.Iterator;
import ec.app.data.Item;
import ec.app.data.User;


public class Outrank {

	public void competitionTable(User u){

		// TODO passar esses valores por parmetro ou decidi-los com base no
		// tamanho da entrada
		
		int min_dist_to_better = 2;
		int min_dist_to_worse = 5;
		
		
		//TODO
		//Aqui eu modifico os parametros de acordo com o tamanho da entrada
		if (0.1 * u.getNumRankItems() > 2)
			min_dist_to_better = (int)0.1 * u.getNumRankItems();
		
		if (0.5 * u.getNumRankItems() > 5)
			min_dist_to_better = (int)0.5 * u.getNumRankItems();
		
		
		int conc_threshold = 2;
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
	

		//A partir daqui eu transformo pra um score, de modo que eu possa usar no GP
		
		double max = Double.NEGATIVE_INFINITY;
		double min = Double.POSITIVE_INFINITY;
		for (int item1 : items_pos.keySet()) {
			coalition_vector[items_pos.get(item1)] = concordance_vector[items_pos.get(item1)] - 
					discordance_vector[items_pos.get(item1)];
			if (max < coalition_vector[items_pos.get(item1)])
				max = coalition_vector[items_pos.get(item1)];

			if(min > coalition_vector[items_pos.get(item1)])
				min = coalition_vector[items_pos.get(item1)];
		}
		
		
		for(int i = 0; i < coalition_vector.length; i++){
			if(coalition_vector[i] > 0)
				coalition_vector[i]= coalition_vector[i]/Math.max(1.0,max);
			else
				coalition_vector[i] =  coalition_vector[i]/Math.max(1.0,Math.abs(min));
		}
				
		
		iter = u.getItemIterator();
		int it = 0;
		while(iter.hasNext()){
			
			int item = iter.next();
			u.getItem(item).setOutrankScore(coalition_vector[it]);
			it++;
		}
		
	}	

	
}
