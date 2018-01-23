package ec.app.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;

import ec.app.util.Utils;

public class PlainDataset {

	
	private static String base_dir;
	private static String output_dir;
	private static String param_file_path;
	private static String individuals_dir;
	private static int numItemsToUse;




	
	public double myMethod(Callable<Double> func) {
		double res = Double.NaN;		
		try {
			res = (double) func.call();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("Error when acessing function : "+func);
		}
		return res;
	}
	



	public static InputData inititialize_data(String data_folder, int numItemsToUse, int numItemsToSuggest, boolean istest, int part) throws IOException{

		if (istest)
			data_folder += "reeval/";
		
		Set<String> input_rankings = new HashSet<String>();
		File input_folder = new File(data_folder);
		File[] listOfFiles_reeval = input_folder.listFiles();

		for (int i = 0; i < listOfFiles_reeval.length; i++) {
			if (listOfFiles_reeval[i].isFile() && listOfFiles_reeval[i].getName().contains(".out") ) {
				String rank_name = listOfFiles_reeval[i].getName().substring(3);
				input_rankings.add(rank_name);
			} 
		}



		//for (int part = 1; part <= 5; part++){	
		String partition = "u"+part;

		Vector<File> vector_files = new Vector<File>();
		for (String input_rank : input_rankings){
			vector_files.add(new File(data_folder+partition+"-"+input_rank));
		}



		File test_file = new File(data_folder+partition+".test");
		File validation_file = test_file;
		if (!istest)
			validation_file = new File(data_folder+partition+".validation");
		File usermap = new File(data_folder+partition+".base.usermap");

		InputData dados_reeval = new InputData(vector_files,test_file,validation_file,
				usermap,numItemsToUse,numItemsToSuggest); 

		//GPRA_Principal.SetData(dados_reeval);


		return dados_reeval;
		
	}


	/**
	 * Pega os rankings de entrada e gera uma base onde cada par <user,item>  uma instancia.
	 * Cada instncia contm todos os terminais que sao utilizados pelo GP e a classe  a presena ou no do item
	 * na base de validao
	 * 
	 * @param dados InputData contendo todos os usurios lidos dos rankings de entrada
	 * @param output_folder 
	 * @throws FileNotFoundException 
	 */
	
	public static void writes_classification_dataset(InputData dados,String output_folder,boolean istest, int part) throws FileNotFoundException{
		
		
		File out_folder_aux = new File(output_folder);
		if (!out_folder_aux.exists())
			out_folder_aux.mkdir();
		
		Vector<User> users = dados.getUsers();
		String fextension = "";
		if (istest){
			fextension = "test";
		}
		else
			fextension = "train";
		
		PrintWriter output = new PrintWriter(new File(output_folder+"u"+part+"."+fextension+"_logit")); 	
		PrintWriter output_useritemmap = new PrintWriter(new File(output_folder+"u"+part+"."+fextension+".map"));		
				
		String sep = ";";
		String useritemmap = "";
		int item_pos = 0;
		for (User u : users){
			
			useritemmap = u.getId()+":";
			
			Iterator<Integer> item_iter = u.getItemIterator();
			
			while(item_iter.hasNext()){
				
				
				
				String line_data = "";
				Integer item_id = item_iter.next();
				Item item = u.getItem(item_id);
				
				//salvando o item de um usuario para o mapa de itens
				useritemmap += "("+item_pos + ","+item_id+");"; 
				item_pos++;
				//********************************************
				
				//Salvando os scores
				for (int nrank = 0; nrank < item.getNumRankings(); nrank++){
					//line_data += String.format(Locale.US,"%.3f", item.getRankScore(nrank))+sep;
					line_data += item.getRankScore(nrank)+sep;	
				}
				
				//salvando os outros terminais
				line_data += item.getTimesR()+sep;
				line_data += item.getProbTop10()+sep;
				//line_data += String.format(Locale.US,"%.3f",item.getProbTop20())+sep;
				//line_data += String.format(Locale.US,"%.3f",item.getProbTop30())+sep;
				//line_data += String.format(Locale.US,"%.3f", item.getOutrankScore()) + sep;
				line_data  +=item.getMeanAgreements() + sep;
				line_data  +=item.getOutrankScore() + sep;
				line_data += item.getBordaScoreComplete() + sep;
				line_data += item.getCombSUM() + sep;
				line_data += item.getCombMNZ() + sep;
				line_data += item.getRRF() + sep;
				
				//  +=String.format(Locale.US,"%.3f",  item.getVictoryScore()) + sep;
				//line_data  +=String.format(Locale.US,"%.3f",  item.getDefeatScore()) + sep;
				
				String class_item;
				
				
				//salvando a classe do item Relevante ou Irrelevante
				Vector<Integer> items_to_compare;
				if (istest)
					items_to_compare = u.testRanking;
				else
					items_to_compare = u.validationRanking;
				
				if (Utils.hasItem(items_to_compare,item_id))
					class_item = "1";
				else 
					class_item = "0";
				
				
				line_data += class_item;
				output.write(line_data+"\n");
			}
		
			output_useritemmap.write(useritemmap.substring(0, useritemmap.length()-1)+"\n");
				
		}
		
		output_useritemmap.close();
		output.close();
			
			
		}
		

	/**
	 * Pega os rankings de entrada e gera uma base onde cada par <user,item>  uma instancia.
	 * Cada instncia contm todos os terminais que sao utilizados pelo GP. Esse metodo nao insere a classe das instancias.
	 * Os datasets gerados aqui serao utilizados pelo GP    
	 * 
	 * 
	 * @param dados InputData contendo todos os usurios lidos dos rankings de entrada
	 * @param output_folder 
	 * @throws FileNotFoundException 
	 */
	public static void writes_plainGP_dataset(InputData dados,String output_folder,boolean istest, int part) throws FileNotFoundException{
		
		Vector<User> users = dados.getUsers();
		String fextension = "train";
		if (istest){
			output_folder += "reeval/";			
		}
		
		File out_folder_test = new File(output_folder);
		if (!out_folder_test.exists())
			out_folder_test.mkdir();	
		
		
		
		PrintWriter output = new PrintWriter(new File(output_folder+"u"+part+".plain."+fextension)); 	
		PrintWriter output_useritemmap = new PrintWriter(new File(output_folder+"u"+part+"."+fextension+".map"));		
				
		String sep = ";";
		String useritemmap = "";
		int item_pos = 0;
		for (User u : users){
			
			useritemmap = u.getId()+":";
			
			Iterator<Integer> item_iter = u.getItemIterator();
			
			while(item_iter.hasNext()){
												
				String line_data = "";
				Integer item_id = item_iter.next();
				Item item = u.getItem(item_id);
				
				//salvando o item de um usuario para o mapa de itens
				useritemmap += "("+item_pos + ","+item_id+");"; 
				item_pos++;
				//********************************************
				
				//Salvando os scores
				for (int nrank = 0; nrank < item.getNumRankings(); nrank++){
					line_data += item.getRankScore(nrank)+sep;
										
				}
				
				//salvando os outros terminais
				line_data += item.getTimesR()+sep;
				line_data += item.getProbTop10()+sep;
				//line_data += String.format(Locale.US,"%.3f",item.getProbTop20())+sep;
				//line_data += String.format(Locale.US,"%.3f",item.getProbTop30())+sep;
				//line_data += String.format(Locale.US,"%.3f", item.getOutrankScore()) + sep;
				line_data  +=item.getMeanAgreements() + sep;
				line_data  +=item.getOutrankScore() + sep;
				line_data += item.getBordaScoreComplete() + sep;
				line_data += item.getCombSUM() + sep;
				line_data += item.getCombMNZ() + sep;
				line_data += item.getRRF();
				
				/*String class_item;
				
				
				//salvando a classe do item Relevante ou Irrelevante
				Vector<Integer> items_to_compare;
				if (istest)
					items_to_compare = u.testRanking;
				else
					items_to_compare = u.validationRanking;
				
				if (Utils.hasItem(items_to_compare,item_id))
					class_item = "1";
				else 
					class_item = "0";
				
				
				line_data += class_item;*/
				output.write(line_data+"\n");
			}
			
			//usa substring para retirar o ltimo ponto e virgula (;) da linha
			output_useritemmap.write(useritemmap.substring(0, useritemmap.length()-1)+"\n");
				
		}
		
		output_useritemmap.close();
		output.close();
			
			
		}
	
	public static void parse_parameters(String args[]){

		for(int ar = 0; ar < args.length; ar++){
			//System.out.println(args[ar]);
			String param = args[ar];
			if (param.startsWith("-")){
				String x[] = args[ar].split("=");
				String attr = args[ar].split("=")[0].substring(1); //pega o nome do parametro ignorando o -
				switch(attr){

				case "base_dir":
					base_dir = args[ar].split("=")[1];
					break;
				case "out_dir":
					output_dir = args[ar].split("=")[1];
					break;
				case "param": 
					param_file_path = args[ar].split("=")[1];
					break;			
				case "ind_dir": 
					individuals_dir = args[ar].split("=")[1];
					break;
				case "i2use":
					numItemsToUse = Integer.parseInt(args[ar].split("=")[1]);
					break;
				default:
					System.err.println("Param "+ attr + "doesn't exist");
					System.err.println("Parameters: base_dir, out_dir, param, ind_dir,i2use,use_plain,used_atts,reeval_all");
					return;
				}


			}		
		}

	}
	
	

	public static void SavePlainDataset(String data_folder, String output_dir, int numItemsToUse, int pini, int pend) throws IOException{

		//TODO colocar um usage
		/*int pini = 1, pend = 5; 
		
		if (args.length < 3){
			
			System.out.println("Usage:");
			System.out.println("java -jar Gen_dataset_GPRA.jar [data_folder] [output_dir] [numItemsTouse] <Classification> -pini");
			return;
		}
		
		
		String data_folder = args[0];
		String output_dir = args[1];		
		int numItemsToUse = Integer.parseInt(args[2]);
		
		
		if (args.length > 3){
			pini = Integer.parseInt(args[3]);
			pend = Integer.parseInt(args[4]);
		}
		*/
		int numItemsToSuggest = 10;
		File out_dir = new File(output_dir);
		if (!out_dir.exists())
			out_dir.mkdir();
		
		
		
		for(int part = pini ; part <= pend; part++){
			
			System.out.println("Part " + part);
			
			InputData dados = inititialize_data(data_folder, numItemsToUse, numItemsToSuggest,false, part);
			writes_classification_dataset(dados,output_dir+"classif/",false,part);
			writes_plainGP_dataset(dados,output_dir,false,part);
			
			InputData dados_reeval = inititialize_data(data_folder, numItemsToUse, numItemsToSuggest,true, part);			
			writes_plainGP_dataset(dados_reeval,output_dir,true,part);
		
		}

	}

	
	
	
	
}
