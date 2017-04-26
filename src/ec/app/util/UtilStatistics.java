package ec.app.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import javax.rmi.CORBA.Util;

import ec.app.data.InputData;
import ec.app.data.Item;
import ec.app.data.User;

public class UtilStatistics {
	
	
	public static void RankingStatistics(InputData input, PrintWriter out) throws IOException{
		
		
		Vector<User> users = input.getUsers();
		//OutputStreamWriter out = new OutputStreamWriter(outStream);
		
		Vector<Vector<Item>> items = Utils.hits(users);
		
		int numRankings = items.size(); //posso pegar o numero de rankings por aqui, 
										//uma vez que na funcao hits eu uso o # de rankings para criar esse vetor de items 
		 
		Vector<String> rankingsItems = new Vector<String>();
		for(int i = 0; i < numRankings; i++){
		
			String rankItems = "";
			rankItems += items.get(i).get(0).getItemId(); //pega o primeiro item
			
			for(int j = 1; j < items.get(i).size(); j++){
				rankItems += ","+items.get(i).get(j).getItemId();
				
			}
			out.println(rankItems);
			rankingsItems.add(rankItems);
			
		}
				
		
		
	}
	
	public static void printMAP(InputData input, int numItemsToUse, int numItemsToSuggest,boolean useTest, PrintWriter log_out) throws IOException{
		
		
		//PrintWriter log_out = new PrintWriter(outStream);
		
		if(useTest)
			log_out.write("\n\nResults Using the Test Set");
		else
			log_out.write("\n\nResults using the Validation Set");
		
		
		log_out.write("\n\nMAP: "+numItemsToUse+" elements\n");
		//Rankings originais e CombSum
		double p_comb = 0;
		double p_out = 0;
		
		int num_alt_r = input.Usuarios.firstElement().getNumAlternativeRankings();
		
		double p_alt[] = new double[num_alt_r];
		double p[] = new double[input.getNumRankings()];
		
		/*for(int i = 0; i < input.Usuarios.size();i++){
			//int size = dados.Usuarios.get(i).testRanking.size();
			
			Vector<Integer> v = CombMethods.CombSum(input.Usuarios.get(i));
			
			Vector<Integer> test = null;
			if(useTest)
				test = input.Usuarios.get(i).getTestRanking();
			else
				test = input.Usuarios.get(i).getValidationRanking();
			
			
			double aux_comb = Metrics.precision(test,v,numItemsToUse);
			p_comb += aux_comb;
		}
		
		for(int i = 0; i < input.Usuarios.size();i++){
			//int size = dados.Usuarios.get(i).testRanking.size();
			
			Vector<Integer> v = CombMethods.outrank_approach(input.Usuarios.get(i));
			
			Vector<Integer> test = null;
			if(useTest)
				test = input.Usuarios.get(i).getTestRanking();
			else
				test = input.Usuarios.get(i).getValidationRanking();
			
			
			double aux_out = Metrics.precision(test,v,numItemsToUse);
			p_out += aux_out;
		}
		
		*/
		
		for(int i = 0; i < input.Usuarios.size();i++){
			for(int j = 0;j < input.getNumRankings(); j++){
				Vector<Integer> test = null;
				
				if(useTest)
					test = input.Usuarios.get(i).getTestRanking();
				else
					test = input.Usuarios.get(i).getValidationRanking();
				
				double aux_0 = Metrics.precision(test,input.Usuarios.get(i).getOriginalRanking(j),null,numItemsToUse);
				
				p[j] += aux_0;
			}
			
			for(int j = 0; j < num_alt_r; j++){
				Vector<Integer> test = null;
				
				if(useTest)
					test = input.Usuarios.get(i).getTestRanking();
				else
					test = input.Usuarios.get(i).getValidationRanking();
				
				double aux_0 = Metrics.precision(test,input.Usuarios.get(i).getAlternativeRanking(j),null,numItemsToUse);
				
				p_alt[j] += aux_0;
				
			}
		}
		
		int numUsersHasElem = 0;
		
		if(useTest)
			numUsersHasElem = input.getNumUsersTestHasElem();
		else
			numUsersHasElem = input.getNumUsersValHasElem();
		
		for(int m = 0; m < input.getNumRankings(); m++){			
			log_out.write("MAP "+ m+": "+p[m]/numUsersHasElem+"\n" );
		}
		
		log_out.write("Baselines Rankings\n");
		for(int m = 0; m < num_alt_r; m++){			
			log_out.write("MAP "+ m+": "+p_alt[m]/numUsersHasElem+"\n" );
		}
		
		
		//#########################################################################################################################
		
		p = new double[input.getNumRankings()];
		p_alt = new double[num_alt_r];
		
		
		for(int i = 0; i < input.Usuarios.size();i++){
			for(int j = 0;j < input.getNumRankings(); j++){
				
				Vector<Integer> test = null;
				
				if(useTest)
					test = input.Usuarios.get(i).getTestRanking();
				else
					test = input.Usuarios.get(i).getValidationRanking();
				
				double aux_0 = Metrics.precision(test,input.Usuarios.get(i).getOriginalRanking(j),null,numItemsToSuggest);
				p[j] += aux_0;
			}
			
			for(int j = 0; j < num_alt_r; j++){
				Vector<Integer> test = null;
				
				if(useTest)
					test = input.Usuarios.get(i).getTestRanking();
				else
					test = input.Usuarios.get(i).getValidationRanking();
				
				double aux_0 = Metrics.precision(test,input.Usuarios.get(i).getAlternativeRanking(j),null,numItemsToSuggest);
				
				p_alt[j] += aux_0;
				
			}
		}
		
		log_out.write("\n\nMAP: "+numItemsToSuggest+" Elements\n\n");
		
		
		numUsersHasElem = 0;
		
		if(useTest)
			numUsersHasElem = input.getNumUsersTestHasElem();
		else
			numUsersHasElem = input.getNumUsersValHasElem();
		
		for(int m = 0; m < input.getNumRankings(); m++){
			log_out.write("MAP "+ m+": "+p[m]/numUsersHasElem+"\n" );
		}
		
		
		//int n = dados.getNumUsersTestHasElem();
		
		log_out.write("Baselines Rankings\n");
		for(int m = 0; m < num_alt_r; m++){			
			log_out.write("MAP "+ m+": "+p_alt[m]/numUsersHasElem+"\n" );
		}
		
		//log_out.close();
		
		
	}
	public static void meanJaccard(InputData input, PrintWriter log_out, boolean useTest) throws IOException{
		
		log_out.println();
		log_out.println();
		//OutputStreamWriter log_out = new OutputStreamWriter(outStream);
		//PrintWriter log_out = new PrintWriter(outStream);
		Vector<User> usuarios = input.getUsers();
		
		Vector<Double> meanJacc = new Vector<Double>();
		Vector<Double> meanJaccHits = new Vector<Double>();
		Vector<Double> meanInter = new Vector<Double>();
		Vector<Double> meanInterHits = new Vector<Double>();


		int totalHits[] = new int[input.getNumRankings()];
		int size_jacc = (input.getNumRankings() *(input.getNumRankings()-1))/2; 
		
		for(int j = 0; j < size_jacc; j++){
			meanJacc.add(0.0);
			meanJaccHits.add(0.0);
			meanInter.add(0.0);
			meanInterHits.add(0.0);
		}
		
		
		for(int u = 0; u < usuarios.size(); u++){
			
			User usr = usuarios.get(u);
			
			Vector<Double> jaccs = Utils.JaccadAll(usr.getOriginalRankings()); //Calcula o coeficiente de jaccard para todos os pares de rankings para todos os usuarios
			
			Vector<Integer> test = null;
			
			if(useTest)
				test = usr.getTestRanking();
				else
					test = usr.getValidationRanking();
			
			Vector<Vector<Integer>> hits = Utils.hits(usr.getOriginalRankings(),test); //Retorna os itens que tiveram um hit para cada ranking e usuário
			Vector<Double> jaccsHits = Utils.JaccadAll(hits); //Calcula o coeficiente de jaccard para todos os pares de rankings considerando somente os itens onde ouve um hit
			
			Vector<Integer> inter = Utils.intersectAll(usr.getOriginalRankings()); //Retorna as interseções entre rankings considerando todos os items
			Vector<Integer> interHits = Utils.intersectAll(hits); //Retorna as interseções entre rankings considerando os itens que tiveram hit
			
			for(int i = 0; i < jaccs.size(); i++){
				
				//System.out.print(jaccs.get(i)+" - ");
				meanJacc.set(i, meanJacc.get(i)+jaccs.get(i));
				meanJaccHits.set(i,meanJaccHits.get(i) + jaccsHits.get(i));
				meanInter.set(i,meanInter.get(i)+inter.get(i));
				meanInterHits.set(i,meanInterHits.get(i)+interHits.get(i));
			}
			
			for(int j = 0; j < hits.size(); j++){
				totalHits[j] += hits.get(j).size();
			}
			
			//System.out.println();
		}
		
		//dividindo pelo número de usuários
		for(int j = 0; j < size_jacc; j++){
			
			meanJacc.set(j, meanJacc.get(j)/input.getNumUsers());
		
			int numUsersHasElem = 0; //numero de usuarios que tem mais de 0 elementos no arquivo de teste/validacao
			if(useTest) 
				numUsersHasElem = input.getNumUsersTestHasElem();
				else
					numUsersHasElem = input.getNumUsersValHasElem();
			
			meanJaccHits.set(j,meanJaccHits.get(j)/numUsersHasElem);
			
			meanInter.set(j,meanInter.get(j)/input.getNumUsers());
			meanInterHits.set(j,meanInterHits.get(j)/input.getNumUsers());
		}
		
		
		//###############LOG#############################
		log_out.write("Mean Jaccard(All Items)\n\n");
		log_out.write("\t");
		Utils.writeTriangularMatrix(meanJacc, input.getNumRankings(), log_out);
		
		
		log_out.write("Mean Jaccard(Hit Items)\n\n");
		log_out.write("\t");
		Utils.writeTriangularMatrix(meanJaccHits, input.getNumRankings(), log_out);
		

		log_out.write("Shared Items(All Items)\n\n");
		log_out.write("\t");
		Utils.writeTriangularMatrix(meanInter, input.getNumRankings(), log_out);
		
		log_out.write("Shared Items(Hit Items)\n\n");
		log_out.write("\t");
		Utils.writeTriangularMatrix(meanInterHits, input.getNumRankings(), log_out);
		
		//###############LOG#############################

		//log_out.close();

		
	}
	
	/**
	 * Imprime o numero medio de hits que cada um dos rankings atinge no arquivo de validação
	 * @param input
	 * @param outStream
	 * @throws IOException
	 */
	public static void meanHitts(InputData input, boolean useTest,PrintWriter log_out) throws IOException{
		//TODO passar parametro para controlar o uso do arquivo de validacao ou teste
		//PrintWriter log_out = new PrintWriter(outStream);
		
		int num_alt_r = input.Usuarios.firstElement().getNumAlternativeRankings();
		
		int totalHits[] = new int[input.getNumRankings()];
		int totalHits_alt[] = new int[num_alt_r]; //hits dos rankings alternativos (baselines)

		Vector<User> usuarios = input.getUsers();
		
		for(int u = 0; u < usuarios.size(); u++){
			
			User usr = usuarios.get(u);
			
			Vector<Integer> test;
			if(useTest)
				test = usr.getTestRanking();
			else
				test = usr.getValidationRanking();
			
			Vector<Vector<Integer>> hits = Utils.hits(usr.getOriginalRankings(),test);
			Vector<Vector<Integer>> hits_alt = Utils.hits(usr.getAlternativeRankings(),test);
	
			for(int j = 0; j < hits.size(); j++){
				totalHits[j] += hits.get(j).size();
			}
			
			for(int j = 0; j < hits_alt.size(); j++){
				totalHits_alt[j] += hits_alt.get(j).size();
			}
		}
		
		String val_or_test = "";
		if(useTest)
			val_or_test = "Test";
		else
			val_or_test = "Validation";
		
		
		log_out.write("Mean Hits("+val_or_test+"): \n");
		
		for(int j = 0; j < totalHits.length; j++){
			log_out.write("Hits " + (j+1) + ")" + (double)totalHits[j]/input.getNumUsersTestHasElem()+"\n");
		}
		
		log_out.write("Mean Hits Baselines("+val_or_test+"): \n");
		
		for(int j = 0; j < totalHits_alt.length; j++){
			log_out.write("Hits " + (j+1) + ")" + (double)totalHits_alt[j]/input.getNumUsersTestHasElem()+"\n");
		}
		
		//log_out.close();

		
	}
	
	
	
	public static void HitItemsStatistics(Vector<User> users, PrintWriter out){
		
		
		//OutputStreamWriter out = new OutputStreamWriter(outStream);
		
		Set<Item> itemsHits = Utils.allHits(users);
		
		
		Iterator<Item> iter  = itemsHits.iterator();
		double avgRepetitionsHits = 0;
		double avgRepetitionsNoHits = 0;
		double avgRepetitionsAll = 0;
		
		Item it = iter.next();
		avgRepetitionsHits += it.getTimesR();
		
		String repetitionsHits = ""+it.getTimesR();
		
		
		while(iter.hasNext()){
			it = iter.next();
			avgRepetitionsHits += it.getTimesR();
			repetitionsHits += ","+it.getTimesR();	
		}
		
		avgRepetitionsHits = avgRepetitionsHits/itemsHits.size();
		
		
		
		int numItemsNoHits = 0;
		int numItemsAll = 0;
		
		for(User usr : users){
			Iterator<Integer> iter2 = usr.getItemIterator();
			
			while(iter2.hasNext()){
				int it_key = iter2.next();
				it = usr.getItem(it_key);
				if(!Utils.hasItem(usr.validationRanking, it_key)){
					
					avgRepetitionsNoHits += it.getTimesR();
					numItemsNoHits++;
				}
				avgRepetitionsAll += it.getTimesR();
				numItemsAll++;
			}
		}
		
		avgRepetitionsNoHits = avgRepetitionsNoHits/numItemsNoHits;
		avgRepetitionsAll = avgRepetitionsAll/numItemsAll;
		
		out.println();
		out.println("Items Average repetition in Rankings");
		out.println("Hits: "+avgRepetitionsHits);
		out.println("No Hits: "+avgRepetitionsNoHits);
		out.println("All: "+avgRepetitionsAll);
		
		
		
	}
	
	
	public static void hitsPositions_aggregation(Vector<User> usuarios, PrintWriter out){
		
		Vector<Vector<Integer>> hitsPositions_val = new Vector<Vector<Integer>>();
		Vector<Vector<Integer>> hitsPositions_test = new Vector<Vector<Integer>>();
		
		int numRankings = usuarios.get(0).getNumAlternativeRankings();
		
		for(int i = 0; i < numRankings; i++){
			hitsPositions_val.add(new Vector<Integer>());
			hitsPositions_test.add(new Vector<Integer>());
		}
		
		for(User usr : usuarios){
			Vector<Vector<Integer>> val_positions = Utils.hitsPositions(usr.getAlternativeRankings(),
																	usr.getValidationRanking());
			
			Vector<Vector<Integer>> test_positions = Utils.hitsPositions(usr.getAlternativeRankings(),
					usr.getTestRanking());

			
			for(int i = 0; i < hitsPositions_val.size(); i++){
					hitsPositions_val.get(i).addAll(val_positions.get(i));
					hitsPositions_test.get(i).addAll(test_positions.get(i));
			}
			
		}
		
		out.println("Validation");
		for(Vector<Integer> v : hitsPositions_val){
			String s = "";
			if (v.size() > 0) {
				s = "" + v.get(0);
				for (int x = 0; x < v.size(); x++) {
					s += "," + v.get(x);
				}
			}
			
			
			out.println(s);
			
			
		}	
		
		
		
		out.println("Test");
		for(Vector<Integer> v : hitsPositions_test){
			String s = "";
			if (v.size() > 0) {
				s = "" + v.get(0);
				for (int x = 0; x < v.size(); x++) {
					s += "," + v.get(x);
				}
			}
			
			
			out.println(s);
			
			
		}
		
	}

	
	public static void hitsPositions(Vector<User> usuarios, PrintWriter out){
		
		Vector<Vector<Integer>> hitsPositions_val = new Vector<Vector<Integer>>();
		Vector<Vector<Integer>> hitsPositions_test = new Vector<Vector<Integer>>();
		
		int numRankings = usuarios.get(0).getNumRankings();
		
		for(int i = 0; i < numRankings; i++){
			hitsPositions_val.add(new Vector<Integer>());
			hitsPositions_test.add(new Vector<Integer>());
		}
		
		for(User usr : usuarios){
			Vector<Vector<Integer>> val_positions = Utils.hitsPositions(usr.getOriginalRankings(),
																	usr.getValidationRanking());
			
			Vector<Vector<Integer>> test_positions = Utils.hitsPositions(usr.getOriginalRankings(),
					usr.getTestRanking());

			
			for(int i = 0; i < hitsPositions_val.size(); i++){
					hitsPositions_val.get(i).addAll(val_positions.get(i));
					hitsPositions_test.get(i).addAll(test_positions.get(i));
			}
			
		}
		
		out.println("Validation");
		for(Vector<Integer> v : hitsPositions_val){
			String s = "";
			if (v.size() > 0) {
				s = "" + v.get(0);
				for (int x = 0; x < v.size(); x++) {
					s += "," + v.get(x);
				}
			}
			
			
			out.println(s);
			
			
		}	
		
		
		
		out.println("Test");
		for(Vector<Integer> v : hitsPositions_test){
			String s = "";
			if (v.size() > 0) {
				s = "" + v.get(0);
				for (int x = 0; x < v.size(); x++) {
					s += "," + v.get(x);
				}
			}
			
			
			out.println(s);
			
			
		}
		
	}
	
	public static void top_k_hits_aggreement(Vector<User> users, int k){
		
		
		for(User u : users){
			Vector<LinkedHashSet<Integer>> top_k_ranks = new Vector<LinkedHashSet<Integer>>();
			Vector<Vector<Integer>> top_k_ranks_vet = new Vector<Vector<Integer>>();
			
			Vector<Vector<Integer>> rankings = u.getOriginalRankings();
			
			for(int r = 0; r < u.getNumRankings(); r++){
				
				top_k_ranks_vet.add(new Vector<Integer>(rankings.get(r).subList(0, k)));
				top_k_ranks.add(new LinkedHashSet<Integer>(rankings.get(r).subList(0, k)));
			}
			Vector<Vector<Integer>> hits = Utils.hits(top_k_ranks_vet,u.getTestRanking());
			
			Vector<Integer> inter_topk = Utils.intersectAll(top_k_ranks_vet);
			Utils.intersectAll_set(top_k_ranks_vet);
			Vector<Integer> inter_hits_topk = Utils.intersectAll(hits);
			
			
		}
		
		
	}

	
	
}