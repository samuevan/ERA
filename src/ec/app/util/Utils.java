package ec.app.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import ec.app.data.Item;
import ec.app.data.User;
import java.util.Random;

public class Utils {

	// TODO pensar se vale a pena representar o item como um par <id,score>
	//Insere o item itemID no vetor vetItem e o itemScore no vetor vetScore de forma que
    //o vetor vetScore se mantenha ordenado decrescente 
	public static void insertInOrder(Integer itemId, Double itemScore,
			Vector<Integer> vetItem, Vector<Double> vetScore) {

		int i = 0;
		if (!(vetScore.size() == 0)) {
			while (i < vetScore.size() && itemScore < vetScore.get(i)) { // i < vetScore.size() && itemScore < vetScore.get(i)
				i++;
			}
		}

		// insere o item na posicao correta de acordo com o seu score
		vetScore.insertElementAt(itemScore, i);
		vetItem.insertElementAt(itemId, i);

	}
	
	/**
	 * Remove a percentage of irrelevant items from the users 
	 * @param usuarios
	 */
	public static void randomUndersampling(Vector<User> users, double perc_reduce, int i2use){
		
		System.out.println("Starting Undersampling");
		if (!(perc_reduce == 0.0 | perc_reduce == 1.0))
		{
			
			
			for (User user : users){
				//Get the irrelevant items considering the validation dataset
				Vector<Integer> irrelevant = new Vector<Integer>();			
				Iterator<Integer> item_it = user.getItemIterator();
				while(item_it.hasNext()){				
					int curr_item_id = item_it.next();				
					if (!user.getValidationRanking().contains(curr_item_id)){
						irrelevant.add(curr_item_id);
					}				
				}

				//computes the maximum number of irrelevant items we can remove
				//without ending with less items than the number of itens we need to recommend
				int num_items_to_remove = (int)(perc_reduce*irrelevant.size());
				int num_items_after_under = user.getNumItems()-num_items_to_remove;
				//if (num_items_after_under < i2use)
				//	num_items_to_remove = user.getNumItems()-i2use;

				num_items_after_under = user.getNumItems() - num_items_to_remove;

				Random r = new Random();
				while (user.getNumItems() > num_items_after_under){								
					user.deleteItem(irrelevant.get(r.nextInt(irrelevant.size()-1)));
				}
			}
		}
		System.out.println("Ending Undersampling");
	}
	
	
	public static void normalize(Vector vet2norm,boolean sorted){
	
		
		
		if (vet2norm.firstElement() instanceof Double){
			
			Pair<Double,Double> max_min = null;
			if (sorted)
				max_min = new Pair<Double, Double>((double)vet2norm.firstElement(), (double)vet2norm.lastElement());									
			else
				//GET maxmin
				System.out.println("Unimplemented");
			
			for (int i = 0; i < vet2norm.size(); i++){
				double new_value = ((double) vet2norm.get(i)-max_min.getSecond());
				new_value /= max_min.getFirst() - max_min.getSecond();
				vet2norm.set(i,new_value);
			}

			
		}
		else if (vet2norm.firstElement() instanceof Pair){
			//in this case the vector is a pair where the value to be normalized is the second one
			Pair<Double,Double> max_min = null;
			if (sorted)
				max_min = new Pair<Double, Double>(((Pair<Integer,Double>)vet2norm.firstElement()).getSecond(), 
												   ((Pair<Integer,Double>)vet2norm.lastElement()).getSecond());									
			else
				//GET maxmin
				System.out.println("Unimplemented");
			
			for (int i = 0; i < vet2norm.size(); i++){
				double new_value = ((Pair<Integer,Double>) vet2norm.get(i)).getSecond()-max_min.getSecond();
				new_value /= max_min.getFirst() - max_min.getSecond();
				((Pair<Integer,Double>)vet2norm.get(i)).setSecond(new_value);
			}

			
			
		}
		
	}
	
	public static void normalize(double[] vet2norm){
		
		
	}

	
	
	public static double Jaccard(Vector<Integer> v1, Vector<Integer> v2) {

		Set<Integer> A = new LinkedHashSet<Integer>(v1);

		Set<Integer> A1 = new LinkedHashSet<Integer>(A);
		Set<Integer> A2 = new LinkedHashSet<Integer>(A);

		Set<Integer> B = new LinkedHashSet<Integer>(v2);

		A1.retainAll(B);
		int sizeIntersection = A1.size();

		A2.addAll(B);
		int sizeUnion = A2.size();

		if (!(sizeUnion == 0)) {
			double jaccard = (double) sizeIntersection / sizeUnion;

			return jaccard;
		} else
			return 0;
	}
	
	/**
	 * 
	 * @param v1 
	 * @param v2
	 * @return The size of intersection between A and B
	 */
	public static int intersectItems(Vector<Integer> v1, Vector<Integer> v2){
		
		Set<Integer> A = new LinkedHashSet<Integer>(v1);

		Set<Integer> A1 = new LinkedHashSet<Integer>(A);
		Set<Integer> A2 = new LinkedHashSet<Integer>(A);

		Set<Integer> B = new LinkedHashSet<Integer>(v2);

		A1.retainAll(B);
		
		return A1.size();
	}
	
	public static LinkedHashSet<Integer> intersectItems_set(Vector<Integer> v1, Vector<Integer> v2){
		
		Set<Integer> A = new LinkedHashSet<Integer>(v1);

		LinkedHashSet<Integer> A1 = new LinkedHashSet<Integer>(A);

		Set<Integer> B = new LinkedHashSet<Integer>(v2);

		A1.retainAll(B);
		
		return A1;
	}
	
	public static Vector<Integer> intersectAll_set(Vector<Vector<Integer>> rankings){
		
		Vector<Integer> intersections = new Vector<Integer>();
		LinkedHashSet<Integer> A1 = new LinkedHashSet<Integer>(rankings.get(0));
		
		for (int r1 = 1; r1 < rankings.size(); r1++) {
			A1.retainAll(rankings.get(r1));
		}

		return intersections;
		
		
	}

	
	public static Vector<Integer> intersectAll(Vector<Vector<Integer>> rankings){
		
		Vector<Integer> intersections = new Vector<Integer>();

		for (int r1 = 0; r1 < rankings.size(); r1++) {

			for (int r2 = r1 + 1; r2 < rankings.size(); r2++) {

				int inter = intersectItems(rankings.get(r1), rankings.get(r2));
				intersections.add(inter);

			}

		}

		return intersections;
		
		
	}
	
	public static Vector<Double> JaccadAll(Vector<Vector<Integer>> rankings) {

		Vector<Double> jaccards = new Vector<Double>();

		for (int r1 = 0; r1 < rankings.size(); r1++) {

			for (int r2 = r1 + 1; r2 < rankings.size(); r2++) {

				double jaccard = Jaccard(rankings.get(r1), rankings.get(r2));
				jaccards.add(jaccard);

			}

		}

		return jaccards;
	}

	/**
	 * Return the items of all rankings that occur in the test ranking
	 * 
	 * @param rankings
	 *            - The rankings used as input
	 * @param testRanking
	 *            - Rank used to test the GP
	 */
	public static void rankingsHits(Vector<Vector<Integer>> rankings,
			Vector<Integer> testRanking) {

		Set<Integer> ItemsHits = new LinkedHashSet<Integer>(); // items que
																// ocorrem em
																// alguma das
																// listas e na
																// lista de
																// teste

		for (int i = 0; i < rankings.get(0).size(); i++) {// considero que todos
															// os rankings
															// possuem o mesmo
															// tamanho
			for (int j = 0; j < rankings.size(); j++) {

				int item = rankings.get(j).get(i);
				if (hasItem(testRanking, item)) {
					ItemsHits.add(item);
				}
			}

		}

	}

	
	/**
	 * 
	 * @param ranking
	 * @param testRanking
	 * @return
	 */
	public static Vector<Vector<Integer>> hits(Vector<Vector<Integer>> ranking,
			Vector<Integer> testRanking) {

		Vector<Vector<Integer>> res = new Vector<Vector<Integer>>();
		for (int i = 0; i < ranking.size(); i++) {
			res.add(new Vector<Integer>());
			for (int j = 0; j < ranking.get(i).size(); j++) {
				int it = ranking.get(i).get(j);
				if (hasItem(testRanking, it)) {
					res.get(i).add(it);
					//System.out.println(i + " - " + j +": "+it);
				}

			}
		}
		return res;
	}
	
	/**
	 * Retorna uma matriz com os items que tiveram um hit na validao, considera os rankings separadamente
	 * 
	 * @param users Vetor de usuarios
	 * @return items que tiveram um hit na validacao separados por ranking
	 */
	public static Vector<Vector<Item>> hits(Vector<User> users){
		
		Vector<Vector<Item>> itemsHits = new Vector<Vector<Item>>();
		int numRankings = users.get(0).getNumRankings();
		
		for(int i = 0; i < numRankings; i++){
			itemsHits.add(new Vector<Item>());
		}
		
		
		for(User usr : users){
			
			Vector<Integer> validationR = usr.getValidationRanking();
			for(int i = 0; i < numRankings; i++){
				Vector<Integer> ranking = usr.getOriginalRanking(i);
				for(int j = 0; j < ranking.size(); j++){
			
					if(hasItem(validationR, ranking.get(j))){
						itemsHits.get(i).add(usr.getItem(ranking.get(j))); //adiciona o item que esta na posicao j do ranking i do usuario 
					}
				}
				
			}
			
			
		}
		
		return itemsHits;
		
		
	}
	
	/**
	 * Retorna os items que tiveram um hit na validacao considerando qualquer um dos rankings
	 * 
	 * @param users
	 * @return
	 */	
	public static Set<Item> allHits(Vector<User> users){
		
		Set<Item> itemsHits = new LinkedHashSet<Item>();
		int numRankings = users.get(0).getNumRankings();
		
		for(User usr : users){
			
			Vector<Integer> validationR = usr.getValidationRanking();
			for(int i = 0; i < numRankings; i++){
				Vector<Integer> ranking = usr.getOriginalRanking(i);
				for(int j = 0; j < ranking.size(); j++){
			
					if(hasItem(validationR, ranking.get(j))){
						itemsHits.add(usr.getItem(ranking.get(j))); //adiciona o item que esta na posicao j do ranking i do usuario 
					}
				}
				
			}						
		}	
		return itemsHits;		
		
	}
	
	//Retorna as posicoes onde os hits aconteceram em cada um dos rankings
	public static Vector<Vector<Integer>> hitsPositions(Vector<Vector<Integer>> ranking,
			Vector<Integer> testRanking) {

		Vector<Vector<Integer>> res = new Vector<Vector<Integer>>();
		for (int i = 0; i < ranking.size(); i++) {
			res.add(new Vector<Integer>());
			for (int j = 0; j < ranking.get(i).size(); j++) {
				int it = ranking.get(i).get(j);
				if (hasItem(testRanking, it)) {
					res.get(i).add(j);
					//System.out.println(i + " - " + j +": "+it);
				}

			}
		}
		return res;
	}
	
	
	public static Vector<Integer>  generateBestRanking(Vector<Vector<Integer>> rankings, Vector<Integer> testRanking, int numItemsToUse){
		Vector<Vector<Integer>> rankingHits = hits(rankings,testRanking);		
		LinkedHashSet<Integer> A = new LinkedHashSet<Integer>(rankingHits.get(0));
		for(int j = 1; j<rankingHits.size(); j++){
			A.addAll(new LinkedHashSet<Integer>(rankingHits.get(j)));	
		}
		
		int s = A.size();
		Vector<Integer> res = new Vector<Integer>(A);
		Random r = new Random();
		while(s < numItemsToUse){
			res.add(-r.nextInt(10000));
			s++;
		}							
		return res;
	
	}
	
	public static boolean hasItem(Vector<Integer> list, int it) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i) == it)
				return true;
		}
		return false;
	}

	
	public static int fat(int n) {
		int f = 1;
		for (int i = 1; i <= n; i++) {

			f *= i;
		}
		return f;
	}
	
	
	public static void writeTriangularMatrix(Vector numbers, int size,
			PrintWriter out_file) throws IOException {

		// escreve a primeira linha da tabela
		for (int j = 1; j < size; j++) {
			out_file.write(j + "\t");
		}
		out_file.write("\n");			
		int k = 0;
		for (int i = 0; i < size; i++) {
			out_file.write(i + ")");
			// cria os espacamentos da matriz triangular
			for (int kc = 0; kc < i + 1; kc++)
				out_file.write("\t");

			for (int j = i + 1; j < size; j++) {
				// escreve os dados
				double x = Double.parseDouble(numbers.get(k).toString()); //TODO Descobrir se tem um jeito "mais elegante" de se fazer essa conversao, considerando Integers
				out_file.write(String.format("%.2f", x) + "\t");
				k++;
			}
			out_file.write("\n");
		}
		
		
		
		out_file.write("\n\n");

	}	
	
	
	public static double log2(double x){				
		double res = Math.log10(x)/Math.log10(2);
		return res;	
	}
	
	
	public static void save_alternative_ranking(Vector<User> users, String partition ) throws FileNotFoundException{
		
		int num_alt_rank = users.get(0).getNumAlternativeRankings();
		for(int i = 0; i < num_alt_rank ; i++){
			
			PrintWriter out = new PrintWriter(new File(partition+"-comb"+i+".out"));
			String s = "";
			for(User u : users){
				int user_id = u.getId();
				Vector<Integer> rank = u.getAlternativeRanking(i);
				s += user_id + "\t" + "[";
				for(int m = 0; m < rank.size()-1; m++){
					s += rank.get(m) + ":0.0,";
				}
				
				s += rank.lastElement()+":0.0]\n";

			}
			out.print(s);
			out.close();
		}
	}
	
		
	public static void save_alternative_ranking(Vector<User> users, String out_dir, String partition ) throws FileNotFoundException{
		
		int num_alt_rank = users.get(0).getNumAlternativeRankings();
		for(int i = 0; i < num_alt_rank ; i++){
			
			PrintWriter out = new PrintWriter(new File(out_dir+partition+"-comb"+i+".out"));
			
			for(User u : users){
				String s = "";
				int user_id = u.getId();
				Vector<Integer> rank = u.getAlternativeRanking(i);
				s += user_id + "\t" + "[";
				for(int m = 0; m < rank.size()-1; m++){
					s += rank.get(m) + ":0.0,";
				}
				
				s += rank.lastElement()+":0.0]\n";
				out.print(s);
			}
			
			out.close();
		}
	}

	
	
	public static boolean containsAny(String str_to_compare, List<String> items){
		
		for (String item_str : items){
			if (str_to_compare.contains(item_str)){
				return true;
			}
		}
		return false;
	}
		

}
