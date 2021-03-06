package ec.app.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;







import ec.app.data.User;
//import lda_gpra.LDARA;
//import lda_gpra.LDAUser;
import ec.app.gpra.GPRA_Principal;
import ec.app.util.Metrics;
import ec.app.util.Pair;
import ec.app.util.Utils;



public class InputData {
	
	private Map<Integer, Vector<Integer>> aggregate;
	private HashMap<Integer, Integer> map_posicao_user;
	private HashMap<Integer, Integer> map_user_posicao;
	//private HashMap<Integer, Integer> map_item_posicao;
	//private HashMap<Integer, Integer> map_posicao_item;
	public Map<Integer, Vector<Integer>> testRankings;
	
	
	public Vector<User> Usuarios;
	private int numItems;
	private int numItemsToUse = 20; //alterar a entrada desse valor, ainda tem problemas com a inicializacao do usuario
	//private int sizeRankings = 20; //TODO alterar, pegar esse valor como parametro
	private int numRankings;
	private int numUsersTestHasElem = 0;
	private int numUsersValHasElem = 0;
	private int numItemsToSuggest;
	private boolean use_sparse = false;
	
	
	
	
	/**
	 * Construct a mapping linking the group_ids to the users that are part of the group.
	 * A user can belongs to more than one group
	 * 
	 * @param groupfile The file containing the groups. 
	 * Each line has the following structure -> group_id: u1,u2,u3...,un
	 * 
	 * @return A hashmap where the key is the group_id and the values the list of 
	 * users whose belongs to the groups
	 * 
	 * @throws FileNotFoundException
	 */
	private HashMap<Integer,Vector<Integer>> ConstructGroupMap(File groupfile) throws FileNotFoundException{        
        HashMap<Integer,Vector<Integer>> map_group_users = new HashMap<Integer, Vector<Integer>>();        
        Scanner group_scanner = new Scanner(groupfile);        
        while(group_scanner.hasNextLine()){            
            String[] tokens = group_scanner.nextLine().trim().split(":");
            int group_id = Integer.parseInt(tokens[0]);
            Vector<Integer>  group_users = new Vector<Integer>();
            for(String usr : tokens[1].trim().split(",")){
            	group_users.add(Integer.parseInt(usr));
            	
            }             
            map_group_users.put(group_id,group_users);
            
        }   
        group_scanner.close();
        return map_group_users;
    } 
    
	
	/**
	 * Read a file containing the input rankings for all the users and returns these rankings
	 * 
	 * @param input_ranking The input rankings for all the users
	 * @return A Vector containg the rankings (here we are considering that the users 
	 * appears in the input ranking in the same order they appear in the file used to construc the usermap. 
	 * If it was not the case the ideal would be contruct a mapping where the key is the user_id and the 
	 * values are the rankings recommended to each user)
	 * 
	 * 
	 * @throws FileNotFoundException
	 */
	private Vector<Vector<Integer>> LoadInputRanking(File input_ranking_file) throws FileNotFoundException{
		
		Scanner scann_input = new Scanner(input_ranking_file);
		Vector<Vector<Integer>> input_ranking = new Vector<Vector<Integer>>();
		//Read the ranking of each user (each line corresponds to a user
		
		while (scann_input.hasNextLine()){
			String line_user[] = scann_input.nextLine().split("\t");				
			String items[] = line_user[1].substring(1, line_user[1].length()-1).split(",");
			
			
			input_ranking.add(new Vector<Integer>());
			for (String item_and_score : items){
				
				int item_id = Integer.parseInt(item_and_score.split(":")[0]);
				input_ranking.lastElement().add(item_id);								
			}
				
			
		}
		
		scann_input.close();
		return input_ranking;
	}
	
	
	
	/**
	 * Read a file containing the input rankings for all the users and returns these rankings
	 * 
	 * @param input_ranking The input rankings for all the users
	 * @return A mapping where the key is the user_id and the value is the ranking associated to this user
	 * Different from LoadInputRankings in this method the ranking is represented as a list of pairs <item,score>    
	 * e.g [<145,0.345>,<124,0.245>,<1455,0.205>,...,<1,0.005>]
	 * 
	 * @throws FileNotFoundException
	 */
	private HashMap<Integer,Vector<Pair<Integer,Double>>> LoadInputRankingMap(File input_ranking_file) throws FileNotFoundException{
		
		Scanner scann_input = new Scanner(input_ranking_file);
		HashMap<Integer,Vector<Pair<Integer,Double>>> input_ranking = new HashMap<Integer,Vector<Pair<Integer,Double>>>();
		//Read the ranking of each user (each line corresponds to a user
		
		while (scann_input.hasNextLine()){
			String line_user[] = scann_input.nextLine().split("\t");
			int user_id = Integer.parseInt(line_user[0]);
			String items[] = line_user[1].substring(1, line_user[1].length()-1).split(",");
			
			
			
			//Vector<Integer> user_ranking_items = new Vector<Integer>();
			//Vector<Double> user_ranking_scores = new Vector<Double>();
			Vector<Pair<Integer,Double>> user_ranking = new Vector<Pair<Integer,Double>>();
			for (String item_and_score : items){
				
				String ins[] = item_and_score.split(":");
				int item_id = Integer.parseInt(ins[0]);
				double item_score = Double.parseDouble(ins[1]);
				
				user_ranking.add(new Pair<Integer, Double>(item_id,item_score));
				//user_ranking_items.add(item_id);
				//user_ranking_scores.add(item_score);
			}
			Utils.normalize(user_ranking,true);
			//Utils.normalize(user_ranking_scores,true);
			input_ranking.put(user_id,user_ranking);	
			
		}
		
		scann_input.close();
		return input_ranking;
	}
	
	
	
	
	private HashSet<Integer> itemsUnion(Vector<Integer> users_in_grp, HashMap<Integer,Vector<Integer>> train_ratings){
		
		HashSet<Integer> items_union = new HashSet<Integer>();
		int first_usr_pos = users_in_grp.firstElement();
		items_union.addAll(train_ratings.get(first_usr_pos));
		
		for (int i = 1; i < users_in_grp.size(); i++){
			
			int usr_pos = users_in_grp.get(i);
			items_union.addAll(train_ratings.get(usr_pos));						
		} 
		
		
		return items_union;
	}
	
	
	private void removeItemsInTrain(Vector<Pair<Integer,Double>> current_rank, HashSet<Integer> group_train_items){

		Vector<Pair<Integer,Double>> to_remove = new Vector<Pair<Integer,Double>>();
		for (Integer item_id : group_train_items){
			//System.out.println();			
			for(Pair<Integer,Double> item_id_score : current_rank){
				if (item_id.compareTo(item_id_score.getFirst()) == 0){
					//usr_ranking.remove(item_id); //remove the items already rated by any group user
					to_remove.add(item_id_score);
				}
			}
		}
		if (to_remove.size() > 0)
			current_rank.removeAll(to_remove);

		
	}
	
	
    public void ExtractGroupFeatures(Vector<File> inputs, File groupfile, File train_ratings_file, File test_file,  int numItemsToUse, int numItemsToSuggest) throws FileNotFoundException{
    	Usuarios = new Vector<User>();
    	//construct the map containing the group_ids and the users that belongs to each group
    	HashMap<Integer, Vector<Integer>>group_map = ConstructGroupMap(groupfile);
    	
    	//read the *.base files. The files that contains the ratings given by the users and are used to train the base recommenders
    	HashMap<Integer,Vector<Integer>> train_ratings = readRatingsFile(train_ratings_file);
    	
    	Integer[] group_ids_sorted = new Integer[group_map.keySet().size()];  
    	group_ids_sorted = group_map.keySet().toArray(group_ids_sorted);
    	Arrays.sort(group_ids_sorted);
    	
    	
    	int pos_input_ranking = 0;
    	for (File input_ranking_file : inputs){ 

    		//read the input file correspondent to the base recommender that will be used
    		//in the future we can use more than one ranking as input, therefore the function signature 
    		//receives a vector of files
    		HashMap<Integer,Vector<Pair<Integer,Double>>> input_ranking_map =LoadInputRankingMap(input_ranking_file);
    		//Vector<Vector<Integer>> input_ranking =	LoadInputRanking(input_ranking_file);
    		
    		for(Integer group_id : group_ids_sorted){//group_map.keySet()){
    			
    			Vector<Integer> users_in_grp = group_map.get(group_id); //get the users in the group
    			int grp_size = users_in_grp.size();
    			User grp_usr;    			

    			//If it is the first ranking read for the groups    			    			
    			if (pos_input_ranking == 0){
    				grp_usr = new User(group_id,grp_size*inputs.size(),numItemsToUse); //construct a pseudo user that represents the group
    				grp_usr.setNumRankingsGroup(inputs.size());
    				Usuarios.add(grp_usr);
    			}else{
    				grp_usr = Usuarios.get(group_id);
    			}
    			

    			//gets the items already rated by the group's users in the training matrix
    			//it avoids including an item already rated(and that will not appear in the test) 
    			//in the group ranking
    			HashSet<Integer> group_train_items = itemsUnion(users_in_grp, train_ratings);
    			
    			int usr_pos_in_grp = pos_input_ranking*grp_size;
    			for (Integer usr : users_in_grp){    			
    				int usr_position = map_user_posicao.get(usr);//take the position the user should have in the input_ranking

    				//Cuts the input ranking to use just numItemsToUse
    				//Vector<Integer> usr_ranking = null;
    				Vector<Pair<Integer,Double>> usr_ranking_item_score = null;
    				try{
    					usr_ranking_item_score = new Vector<Pair<Integer,Double>>(input_ranking_map.get(usr).subList(0, numItemsToUse));
    					//usr_ranking = new Vector<Integer>(input_ranking.get(usr_position).subList(0, numItemsToUse));
    				}
    				catch (Exception e){
    					System.out.println("ERRO");
    				}


    				grp_usr.addOriginalRankingWithScores(usr_ranking_item_score); //store the ranking correspondent to the user in the group    			
    				Vector<Integer> original_ranking_itemids = new Vector<Integer>(); //clone the original ranking to use the positions the items are in this ranking
    				
    				for (Pair<Integer,Double> item_id_score : usr_ranking_item_score){
    					original_ranking_itemids.add(item_id_score.getFirst());
    				}
    				//REMOVE items already in train from rankings
    				
    				removeItemsInTrain(usr_ranking_item_score,group_train_items);
    				for (int item_pos = 0; item_pos < usr_ranking_item_score.size(); item_pos++){
    					Pair<Integer,Double> item_id_score = usr_ranking_item_score.get(item_pos); 
    					int item_id = item_id_score.getFirst();     				
    					grp_usr.addItem(item_id);
    					int original_item_pos = original_ranking_itemids.indexOf(item_id);
    					grp_usr.setItemPosition(item_id, usr_pos_in_grp, original_item_pos+1);
    					double item_score = item_id_score.getSecond();
    					grp_usr.setItemGivenScore(item_id,usr_pos_in_grp,item_score);
    					//grp_usr.setItemScore(item_id, usr_pos_in_grp, Metrics.calcRankNorm(it+1, sizeRankings));
    				}    			    			

    				usr_pos_in_grp++;
    			}
    			//Chama todas as funcoes de calculo de feature;;;
    			//Changed to the end of input_rankings loop
    			//grp_usr.ComputeFeatures();    		    		
    		}
    		
    		pos_input_ranking++;

    	}
    	//Effectivelly calls the method to compute the features for each user
    	group_ids_sorted = group_map.keySet().toArray(group_ids_sorted);
    	Arrays.sort(group_ids_sorted);
    	for(Integer group_id : group_ids_sorted){
    		//System.out.println(group_id);
    		User grp_usr = Usuarios.get(group_id);
    		grp_usr.ComputeFeatures();
    	}
    	
    	
    	
    	
    	
    	
    	
		readGroupsTestFile(test_file, groupfile, "test");
		


    }
	
    
    /*public InputData(Vector<File> inputs, File groupfile, File testInput, File usermap, int numItemsToUse, int numItemsToSuggest) throws IOException{
    	
    	ConstructUserMaps(usermap);
    	ExtractGroupFeatures(inputs, groupfile, numItemsToUse, numItemsToSuggest);
    	
    		
    
    }*/
    
    
	/**
	 * Construct the maps that will be used in the vector Usuarios
	 * With this mappings we can interchangeably refer to the user_id or to its position in Usuarios vector
	 * The mappings are stored in the class attributes map_posicao_user and map_user_posicao
	 *  
	 * @param usermap  
	 * @throws FileNotFoundException
	 */
    private void ConstructUserMaps(File usermap) throws FileNotFoundException{
    	
			map_posicao_user = new HashMap<Integer, Integer>();
			map_user_posicao = new HashMap<Integer, Integer>();
			Scanner scann = new Scanner(usermap);
			while(scann.hasNextLine()){
				String dados[] = scann.nextLine().split("\t");
				int user_id = Integer.parseInt(dados[0]);
				int user_pos = Integer.parseInt(dados[1]);
				
				map_posicao_user.put(user_pos, user_id);
				map_user_posicao.put(user_id,user_pos);
			}
			
			scann.close();
		
    }
    
    
    private void normalizeItemsAttributes(){
    	
    }
    
    
    
	public InputData(Vector<File> inputs, File testInput, File usermap, int numItemsToUse, int numItemsToSuggest) throws IOException{
		
		//*********************************************READ MAPS***********************************
		ConstructUserMaps(usermap);		
		//***************************************END READ MAPS***************************************
		
		
		//Vetor que armazena todos os rankings de entrada
		Vector<Scanner> scanns = new Vector<Scanner>();	
		Usuarios = new Vector<User>();
		numRankings = inputs.size();
		
		//carrega todos os rankings de entrada
		for (int s = 0; s < inputs.size(); s++)
		{
			Scanner sc = new Scanner(inputs.get(s));
			scanns.add(sc);//cria um scanner para cada um dos arquivos de entrada
		}
		
		
		boolean hasNextUser = true;
		int userPos = 0; //posicao do usuario
		
		while(hasNextUser){
			//System.out.println(userPos);
			int usr_id = map_posicao_user.get(userPos);
			
			
			
			User user = new User(usr_id,numRankings,numItemsToUse); //nesse momento inicializa o usuario
																	//User user = new User(usr_id,numRankings,numRankItems)
			Usuarios.add(user);
			
			//itera pelos diferentes rankings(arquivos de entrada)
			for(int rankId = 0 ; rankId < scanns.size(); rankId++){
				String lineUser[] = scanns.get(rankId).nextLine().split("\t");

				String items[] = lineUser[1].substring(1, lineUser[1].length()-1).split(","); //exclui os [ do comeco e do final e quebra por virgula 
			
				
				//controla o numero de itens usados na entrada, se o parametro passado no construtor for igual ou menor a zero usa o tamanho 
				// da lista de entrada
				if(numItemsToUse <= 0){
					this.numItemsToUse = items.length; //seta o tamanho dos rankings
					//this.sizeRankings = items.length;
					user.setNumRankItems(items.length);
					numItemsToUse = items.length;
				}else
				{
					this.numItemsToUse = numItemsToUse; //seta o tamanho dos rankings
					//this.sizeRankings = numItemsToUse;
					user.setNumRankItems(numItemsToUse);
					
				}
				
				//itera pelos items de sugeridos para cada usuario
				//adiciona esses itens para o usuario em questao
				for (int it = 0; it < numItemsToUse; it++)
				{
					String itemid_val = items[it].split(":")[0];
					int item = Integer.parseInt(itemid_val); //<item>:<score>
					
					//user.addItemOriginalRanking(item, rankId, it); //metodo usando array
					user.addItemOriginalRanking(item, rankId);//metodo usando vector
									
					//A insercao ja verifica se o item ja existe na base
					user.addItem(item);
					user.setItemPosition(item, rankId, it+1);
					user.setItemScore(item, rankId, Metrics.calcRankNorm(it+1, numItemsToUse));
					
					//Calc the actual borda score, acoording to the definition score = 1/pos
					//double d_actualborda = sizeRankings-it;//1.0/(it+1);
					//user.setItemActualBordaScore(item, rankId,d_actualborda);
					//************************************************************************											
										
				}		
				
				if(!scanns.get(rankId).hasNext())
					hasNextUser = false;
			}
			
			//#########################################################################################
			//Calcula o borda score para cada um dos itens do usuario
			
			Iterator<Integer> iter = user.getItemIterator();
			
			while(iter.hasNext()){				
				int item_id = iter.next();
				int timesTop10 = 0;
				
				Item item = user.getItem(item_id);
				int timesR = 0;
				//itera pelas posicoes do item em cada um dos rankings
				for(int rank_id = 0; rank_id < item.getNumRankings(); rank_id++){
					
					int pos_r = item.getPosition(rank_id);
					
					//verifica se o item esta presente no ranking corrente
					if(pos_r != -1){
						
						int numi = user.getNumItems();
						double d = 1.0 - ((pos_r - 1.0)/(float)(user.getNumItems())); //TODO verificar se |U| deve ser a quantidade de itens para o usuario, ou o tamanho da soma de todos os rankings
						//item.setBordaScore(d, rank_id);
						
						
					}
					else
					{
						float user_num_items = (float)user.getNumItems();
						double init = 1.0 - (numItemsToUse-1)/(float)user.getNumItems(); //calcula o valor inicial para que os itens que nao estao no ranking nao passem dos itens que estao no ranking
						init = init - (((numItemsToUse-1)/(float)(2*user.getNumItems())) + 0.005);
						
						double d = init + ((numItemsToUse-1)/(float)(2* user.getNumItems()));
						//item.setBordaScore(d, rank_id);
					}
					
					//##################################################
					//Calcula o numero de vezes que o item esta no top10
					
					//TODO pass this as parameter
					if((pos_r != -1) && (pos_r <= 0.30*this.numItemsToUse))
						timesTop10++;
					
					
					//if((pos_r != -1) && (pos_r <= 10))//0.10*this.sizeRankings))
					//	timesTop10++;
					
					if(pos_r != -1){
						timesR++;
					}
					
					
				}
				
				//##################################################
				//seta a prob do item estar no top10
				
				double p = ((double)timesTop10)/((double)item.getNumRankings());
				item.setProbTop10(p);
				item.setTimesR(timesR);

				item.calcAgreements(2); // TODO: definir janela de concordancia por parametro
				
				
			}
			userPos++;
		}	
		//********************Outranking Approach****************************
		boolean no_outrank = GPRA_Principal.getParameters().getBoolean("no_outrank"); 
		
		if (!no_outrank){
			System.out.println("Calculando outrank");
			long t = System.currentTimeMillis();

			for(User u : Usuarios){
				competitionTable(u);
			}

			t = t - System.currentTimeMillis();

			System.out.println("Fim outrank - "+t+"(s)");
		}
		//********************Outranking Approach(end)****************************
		
		
		
		
		free_map_posicao_user();
		readTestFile(testInput,"test");
		//Libera espaco dos rankings de baseline
		/*for (User u : Usuarios){
			u.freeOriginalRankings();
		}*/
			
	}
	
	
	
	/**
	 * This constructor reads a file in a classification dataset format. Each line corresponds to an item recommended to a user
	 * and the features computed to this pair (user,item)
	 * 
	 * 
	 * 
	 * @param Features_dataset : Dataset where each line corresponds to a pair (user,item)
	 * @param useritemmap : Maps which lines corresponds to wich user in the dataset 
	 * @param testInput : Test dataset in movilens format
	 * @param validationInput : Validation dataset in movilens format
	 * @param numItemsToUse : Number of items that will be used in the Agg process (this value it is not necessary here, since the process of creating the dataset already considers this)
	 * @param numItemsToSug : Size of the final rankings that will be returned to to user
	 * @throws FileNotFoundException
	 */
	
	public InputData(File Features_dataset, File useritemmap, File testInput,File validationInput, int numItemsToUse, int numItemsToSug, boolean use_sparse) throws FileNotFoundException{
		Usuarios = new Vector<User>();
		map_posicao_user = new HashMap<Integer, Integer>();
		map_user_posicao = new HashMap<Integer, Integer>();
		this.use_sparse = use_sparse;
		
		this.numItemsToUse = numItemsToUse; //seta o tamanho dos rankings
		//this.sizeRankings = numItemsToUse;		
						
		
		//Cada linda do arquivo tem a seguinte forma 
		//<usr_id>:(pos,item_id);(pos,item_id);(pos,item_id);(pos,item_id)
		Scanner scann_useritem_map = new Scanner(useritemmap);		
		
		//armazena todos os items recomendados para cada usuario
		Vector<Vector<Integer>> items_per_usr = new Vector<Vector<Integer>>(); 		
		int usr_pos = 0;
		while (scann_useritem_map.hasNextLine()){
			
			String line = scann_useritem_map.nextLine();			
			int usr_id = Integer.parseInt(line.split(":")[0]);
			String[] items =  line.split(":")[1].replaceAll("[( )]", "").split(";");
			
			//cria um novo vetor para armazenar os items recomendados para o usuario
			//o tamanho do veteor de cada usuario indica o numero de items (aka linhas no arquivo de treino)
			//correspondentes aquele usuario 
			items_per_usr.add(new Vector<Integer>());
			
			for (String pos_item : items){				
				int item_id = Integer.parseInt(pos_item.split(",")[1]);
				items_per_usr.lastElement().add(item_id);								
			}   			
			
			map_user_posicao.put(usr_id, usr_pos);
			map_posicao_user.put(usr_pos,usr_id);
			
			usr_pos++;
			
		}
		
		scann_useritem_map.close();
		
		
		Scanner data_file = new Scanner(Features_dataset);
		
		
		usr_pos = 0;
		while (data_file.hasNextLine()){

			int usr_id = map_posicao_user.get(usr_pos);
			User user = new User(usr_id,numRankings,numItemsToUse); //nesse momento inicializa o usuario
			Usuarios.add(user);
			for (int item_id : items_per_usr.get(usr_pos)){
				//para cada item recomendado para o usuario e colocado no vetor items_per_usr 

				//faz a leitura do arquivo de entrada da linha correspondente ao item
				//para cada atributo
				String[] usr_item_attributes = data_file.nextLine().split(";");
				//adiciona o items para o usuario
				user.addItem(item_id,usr_item_attributes.length,true,use_sparse);
				if (use_sparse){					
					
					double[] atts_values = new double[usr_item_attributes.length];
					for (int pos_att = 0; pos_att < usr_item_attributes.length; pos_att++){						
						atts_values[pos_att] = Double.parseDouble(usr_item_attributes[pos_att]);																								 
					}
					//TODO if using sparse fix above					
					//user.setItemGenericValuesSparse(item_id,usr_item_attributes.length,atts_values);
				}
				else
				{	
					for (int pos = 0; pos < usr_item_attributes.length; pos++){
						//isere o atributo para o item considerando que eh um atributo generico
						user.setItemGenericValue(item_id, pos, Double.parseDouble(usr_item_attributes[pos]));
					} 
				} 
				
			} 
			
			usr_pos++;
			
		}
				
		data_file.close();
		readTestFile(testInput, "test");
		this.numItemsToSuggest = numItemsToSug;
		readTestFile(validationInput, "validation");
		//free_map_user_posicao();
		//System.gc();
		
	}
	
	
	public InputData(Vector<File> inputs, File validationInput,File testInput,File usermap, int numItemsToUse, int numItemsToSugg) throws IOException{
		
		this(inputs,testInput,usermap,numItemsToUse,numItemsToSugg);
		this.numItemsToSuggest = numItemsToSugg;
		readTestFile(validationInput, "validation");
		//free_map_user_posicao();
		System.gc();
	}
	
	
	public InputData(Vector<File> inputs,File train_ratings_file,  File validationInput,File testInput, File usermap, File groupfile, int numItemsToUse, int numItemsToSugg) throws IOException{
		this.numItemsToSuggest = numItemsToSugg;
		this.numItemsToUse = numItemsToUse;
		
		ConstructUserMaps(usermap);
		ExtractGroupFeatures(inputs, groupfile, train_ratings_file, testInput, numItemsToUse, numItemsToSugg);
		readGroupsTestFile(validationInput, groupfile, "validation");		
	}
	
	
	public void insertNewRanking(Vector<Vector<Integer>> newR){
		
		
		numRankings++;
		for(int i = 0; i < Usuarios.size(); i++){
			Usuarios.get(i).addNewRanking(newR.get(i));
		}
		
	}	
	
	public Map<Integer, Vector<Integer>> getAggregateRanking(){	
		return aggregate;
	}
	
	
	public int getNumRankings(){
		return numRankings;
	}
	
	public int getNumUsers(){
		return Usuarios.size();
	}
	
	/**
	 * 
	 * @param testInput arquivo com os dados a serem lidos
	 * @param type - indica se o arquivo eh um arquivo de teste um ou arquivo de validacao ("test","validation")
	 * @throws FileNotFoundException
	 */
	//TODO nao esta lendo a primeira linha do arquivo CORRIGIR
	public void readTestFile(File testInput, String type) throws FileNotFoundException{
		
		
		//System.out.println("Teste");
		Scanner sc = new Scanner(testInput);
		testRankings = new HashMap<Integer, Vector<Integer>>();
		int usr = 0, usr_ant = 0;
		
		String line[] = sc.nextLine().split("\t");
		usr = Integer.parseInt(line[0]);
		int item = Integer.parseInt(line[1]);
		int value = Integer.parseInt(line[2]);
		usr_ant = usr;
		
		
		int usr_i = map_user_posicao.get(usr);
		while(sc.hasNext()){
			
			if(type.equalsIgnoreCase("test")){
				if(usr == usr_ant ){
					usr_i = map_user_posicao.get(usr);
					Usuarios.get(usr_i).testRanking.add(item);
					//Usuarios.get(usr_i).testRanking2.add(new Pair<Integer,Integer>(item,value)); //TODO pode ser necessario alterar o value para double
				}else
				{
					/*if((usr-usr_ant) > 1)
						usr_i = usr-1;
					else
						usr_i++;*/
					if (!map_user_posicao.containsKey(usr)){
						System.err.println("Test Does not contain user "+usr);
						continue;
					}
					
					
					usr_i = map_user_posicao.get(usr);

					Usuarios.get(usr_i).testRanking.add(item);
					//Usuarios.get(usr_i).testRanking2.add(new Pair<Integer,Integer>(item,value));
					numUsersTestHasElem++;
					
				}
			}else
			{
				if(type.equalsIgnoreCase("validation")){
					if(usr == usr_ant ){
						usr_i = map_user_posicao.get(usr);
						Usuarios.get(usr_i).validationRanking.add(item);
					}else
					{
						
						/*if((usr-usr_ant) > 1)
							usr_i = usr-1;
						else
							usr_i++;*/
						
						usr_i = map_user_posicao.get(usr);
						Usuarios.get(usr_i).validationRanking.add(item);
						numUsersValHasElem++;
						
					} 
				}
			}
			
			usr_ant = usr;
			line = sc.nextLine().split("\t");
			int userx = Integer.parseInt(line[0]);
			while (!map_user_posicao.containsKey(userx)){
				System.err.println("Does not contain user "+userx);
				line = sc.nextLine().split("\t");
				userx = Integer.parseInt(line[0]);
				continue;
			}
			usr = Integer.parseInt(line[0]);
			item = Integer.parseInt(line[1]);
			
			
			//TODO deletar
			if(!testRankings.containsKey(usr)){
				
				testRankings.put(usr, new Vector<Integer>());
				testRankings.get(usr).add(item);
			}
			else{
				testRankings.get(usr).add(item);
			}
			
			
			
			
		}
		numUsersValHasElem++;
		numUsersTestHasElem++;//add 1 para contar o primeiro usuario
		//Adciona o ultimo elemento do arquivo
		if(type.equalsIgnoreCase("test")){
			if(usr == usr_ant ){
				usr_i = map_user_posicao.get(usr);
				Usuarios.get(usr_i).testRanking.add(item);
			}else
			{
				//usr_i++;
				usr_i = map_user_posicao.get(usr);
				Usuarios.get(usr_i).testRanking.add(item);
				
			}
		}
		else
		{
			if(type.equalsIgnoreCase("validation")){
				if(usr == usr_ant ){
					usr_i = map_user_posicao.get(usr);
					Usuarios.get(usr_i).validationRanking.add(item);
				}else
				{
					//usr_i++;
					usr_i = map_user_posicao.get(usr);
					Usuarios.get(usr_i).validationRanking.add(item);
					
				}
			}	
		}
		
		//TODO deletar
		if(!testRankings.containsKey(usr)){
			
			testRankings.put(usr, new Vector<Integer>());
			testRankings.get(usr).add(item);
		}
		else{
			testRankings.get(usr).add(item);
		}
		
		sc.close();
		
	}
	
public HashMap<Integer, Vector<Integer>> readRatingsFile(File ratingsFile) throws FileNotFoundException{
	
	//System.out.println("Teste");
	Scanner sc = new Scanner(ratingsFile);
	HashMap<Integer, Vector<Integer>> ratings_map = new HashMap<Integer, Vector<Integer>>();
	int usr = -1; //, usr_ant = 0;
	
	String line[]; //= sc.nextLine().split("\t");
	//usr = Integer.parseInt(line[0]);
	int item = -1; //= Integer.parseInt(line[1]);
	int value; //= Integer.parseInt(line[2]);
	//usr_ant = usr;
	
	
	//int usr_i = map_user_posicao.get(usr);
	while(sc.hasNext()){
		
		//usr_ant = usr;
		line = sc.nextLine().split("\t");
		int userx = Integer.parseInt(line[0]);
		while (!map_user_posicao.containsKey(userx)){
			System.err.println("Does not contain user "+userx);
			line = sc.nextLine().split("\t");
			userx = Integer.parseInt(line[0]);
			continue;
		}
		usr = Integer.parseInt(line[0]);
		item = Integer.parseInt(line[1]);
		
		
		//TODO deletar
		if(!ratings_map.containsKey(usr)){
			
			ratings_map.put(usr, new Vector<Integer>());
			ratings_map.get(usr).add(item);
		}
		else{
			ratings_map.get(usr).add(item);
		}			
		
	}

	//LAST ELEMENT
	// This piece of code is note necessary
	/*if(!ratings_map.containsKey(usr)){
		
		ratings_map.put(usr, new Vector<Integer>());
		ratings_map.get(usr).add(item);
	}
	else{
		ratings_map.get(usr).add(item);
	}*/
	
	sc.close();
	
	return ratings_map;
	
	
	
}
	
	
public void readGroupsTestFile(File testInput, File groupfile, String type) throws FileNotFoundException{
		
		
		//System.out.println("Teste");
		Scanner sc = new Scanner(testInput);
		HashMap<Integer, Vector<Integer>> testRankings_aux = new HashMap<Integer, Vector<Integer>>();
		int usr = -1;//, usr_ant = 0;
		
		String line[];// = sc.nextLine().split("\t");
		//usr = Integer.parseInt(line[0]);
		int item = -1;//Integer.parseInt(line[1]);
		int value;// = Integer.parseInt(line[2]);
		//usr_ant = usr;
		
		
		//int usr_i = map_user_posicao.get(usr);
		while(sc.hasNext()){
			
			//usr_ant = usr;
			line = sc.nextLine().split("\t");
			int userx = Integer.parseInt(line[0]);
			while (!map_user_posicao.containsKey(userx)){
				System.err.println("Does not contain user "+userx);
				line = sc.nextLine().split("\t");
				userx = Integer.parseInt(line[0]);
				continue;
			}
			usr = Integer.parseInt(line[0]);
			item = Integer.parseInt(line[1]);
			
			
			if(!testRankings_aux.containsKey(usr)){
				
				testRankings_aux.put(usr, new Vector<Integer>());
				testRankings_aux.get(usr).add(item);
			}
			else{
				testRankings_aux.get(usr).add(item);
			}			
			
		}

		//LAST ELEMENT
		// This is not necessary
		/*if(!testRankings_aux.containsKey(usr)){
			
			testRankings_aux.put(usr, new Vector<Integer>());
			testRankings_aux.get(usr).add(item);
		}
		else{
			testRankings_aux.get(usr).add(item);
		}*/
		
		sc.close();
		
		//TODO receive groupmap as parameter or put this in the global features
		HashMap<Integer,Vector<Integer>> group_map = ConstructGroupMap(groupfile);
		
		for (Integer grp_id : group_map.keySet()){
			Vector<Integer> users_in_grp = group_map.get(grp_id);
			int first_usr = users_in_grp.firstElement();
			
			HashSet<Integer> item_intersection = new HashSet<Integer>();
			//if the user has no items in the test set, it means that the intersection for this group
			//will be zero. We guarantee this when we initialize the first user with an empty set.
			if(testRankings_aux.containsKey(users_in_grp.firstElement())){
				item_intersection = new HashSet<Integer>(testRankings_aux.get(first_usr));
			}
			
			for (int usr_pos = 1; usr_pos < users_in_grp.size(); usr_pos++){
				System.out.println(users_in_grp.get(usr_pos));
				//Construct the intersection between the items in the test set for all the group members 
				Vector<Integer > test_curr_usr = new Vector<Integer>();
				//if the user has no items in the test set, it means that the intersection for this group
				//will be zero. We guarantee this when we mantain the test_curr_usr empty.
				if(testRankings_aux.containsKey(users_in_grp.get(usr_pos))){
					test_curr_usr = testRankings_aux.get(users_in_grp.get(usr_pos));
				}
				item_intersection.retainAll(test_curr_usr);
			}
			
			if (type.contains("test")){
				Usuarios.get(grp_id).setTestRanking(new Vector<Integer>(item_intersection));
				if (item_intersection.size() > 0){
					numUsersTestHasElem++;
				}			
			}else{
				Usuarios.get(grp_id).setValidationRanking(new Vector<Integer>(item_intersection));
				if (item_intersection.size() > 0){
					numUsersValHasElem++;
				}
			}						
			
			
		}
		
		
		
		
	}
	
	
	
	
	public boolean UsingSparse(){
		return use_sparse;
	}
	
	
	public int getNumUsersTestHasElem()
	{
		return numUsersTestHasElem;
	}
	
		
	public void saveToFile(File output){
		
	}
	
	//gives direct acess to the vector Usuarios
	public Vector<User> getUsers(){
		return Usuarios;
	}
	
	public void setNumItems(int nItems){
		
		numItems = nItems;
		
	}
	
	public void setNumRankings(int nRank){
		
		numRankings = nRank;
	}
	
	/*public void setSizeRankings(int sizeR){
		
		sizeRankings = sizeR;
	}*/
	
	public int getNumRankItems()
	{
		return numItemsToUse;
	}
	
	public int getNumItemsToSuggest(){
		return numItemsToSuggest;
	}

	public int getNumUsersValHasElem() {
		return numUsersValHasElem;
	}
	
	public void free_map_user_posicao(){
		map_user_posicao.clear();
		map_user_posicao = null;
	}
	
	public void free_map_posicao_user(){
		map_posicao_user.clear();
		map_posicao_user = null;
	}
	
	

	public void competitionTable(User u){

		// TODO passar esses valores por parametro ou decidi-los com base no
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

		// hash para mapear cada item do usuario em uma posicao que sera usada
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
				if((item1.getPosition(i) > 0)){ //Garante que soh sao comparados itens que estao no mesmo ranking 

					for (int j = 0; j < u.getOriginalRanking(i).size(); j++) {
						int item2x = u.getItemOriginalRanking(i, j);


						Item item2 = u.getItem(item2x);

						// caso o item1 esteja em uma posicao anterior ao item2 com
						// uma distancia de no minimo min_dist_to_better
						// somo mais 1 na tabela de melhor
						if ((item2.getPosition(i) - item1.getPosition(i)) >= min_dist_to_better) {

							int a = items_pos.get(item1.getItemId());
							int b = items_pos.get(item2x);
							concordance_table[a][b] += 1;
						} else {
							// caso contrario eh somado mais 1 na tabela de pior
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
