package ec.app.gpra;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import ec.Evaluator;
import ec.EvolutionState;
import ec.Evolve;
import ec.Individual;
import ec.Species;
import ec.Subpopulation;
import ec.app.data.InputData;
import ec.app.data.Item;
import ec.app.data.User;
import ec.app.util.CombMethods;
import ec.app.util.Metrics;
import ec.app.util.Pair;
import ec.app.util.UtilStatistics;
import ec.app.util.Utils;
import ec.gp.koza.KozaFitness;
import ec.simple.SimpleProblemForm;
import ec.simple.SimpleStatistics;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;


public class GPRA_Principal_nonich {

	private static int numUsedRanks = 0;
	private static int numItemsToUse =10;
	private static int numItemsToSuggest = 10;
	private static InputData dados = null;
	private static InputData dados2 = null;
	private static Vector<Individual> best_individuals_by_run = null;
	private static Vector<Individual> best_individuals_by_part;
	private static Individual best_individual_all;
	private static boolean need_backup = false;
	public static boolean use_outrank = true;
	private static int nruns = 5;
	private static boolean runGP = true;

	public static InputData getData(){
		return dados;
	}

	public static void SetData(InputData inputdata){
		dados = inputdata;
	}
	
	public static int getNumUsedRanks(){
		return dados.getNumRankings(); 
	}
	
	public static int get_numItemsToUse(){
		return numItemsToUse;
	}

	
	//funcao usada para armazenar os melhores indivduos de cada run
	public static void backup_individual(Vector<Individual>bkp_vet,Individual ind, int pos){
		if (bkp_vet.get(pos) != null){
				if (((KozaFitness)ind.fitness).standardizedFitness() < 
						((KozaFitness)bkp_vet.get(pos).fitness).standardizedFitness()){
					//eh preciso clonar o individuo para que ele no tenha a sua fitness alterada
					//uma vez que essa fitness eh calculada na validacao e o individuo sera reavaliado
					bkp_vet.set(pos, (Individual) ind.clone());
				}
		}
		else{
			bkp_vet.set(pos, (Individual) ind.clone());
		}
		
		
		if (best_individual_all != null){
		
			if (((KozaFitness)ind.fitness).standardizedFitness() < 
			((KozaFitness)best_individual_all.fitness).standardizedFitness()){
				best_individual_all = (Individual) ind.clone();
			}
			else{
				if 	(((KozaFitness)ind.fitness).standardizedFitness() == 
						((KozaFitness)best_individual_all.fitness).standardizedFitness()){
					best_individual_all = (Individual) ind.clone();
				}
			}
		}
		else{
			best_individual_all = (Individual) ind.clone();
		}
		
	}
	
	
	/**
	 * Reavalia os melhores individuos para cada rodada, para cada partio e o melhor individuo de todas as geraes/rodadas
	 * 
	 * @param data_folder pasta onde esto os rankings de entrada
	 * @param parameters Parametros usados no EvalutionState
	 * @param output_dir
	 * @throws IOException
	 */
	public static void reeval_best_individuals(String data_folder,ParameterDatabase parameters, String output_dir) throws IOException{
		
		
		//resultados das parties armazenadas por linha
		Vector<Double> map_best_all = new Vector<Double>(); 
		Vector<Vector<Double>> map_best_individuals_run = new Vector<Vector<Double>>();
		Vector<Vector<Double>> map_best_individuals_par = new Vector<Vector<Double>>();
		
		
		
		//READ THE DATA TO REEVALUATE THE BEST INDIVIDUAL
		Set<String> reeval_rankings = new HashSet<String>();

		File folder_reeval = new File(data_folder);
		File[] listOfFiles_reeval = folder_reeval.listFiles();

		for (int i = 0; i < listOfFiles_reeval.length; i++) {
			if (listOfFiles_reeval[i].isFile() && listOfFiles_reeval[i].getName().contains(".out") ) {
				String rank_name = listOfFiles_reeval[i].getName().substring(3);
				reeval_rankings.add(rank_name);
			} 
		}
		
		MySimpleEvolutionState evolution_state = (MySimpleEvolutionState)Evolve.initialize(parameters, 0);
		PrintWriter out_reeval = new PrintWriter(new BufferedWriter(new FileWriter(output_dir+"best_individuals.log")));
		String ind_part_str = "";
		String ind_run_str = "";
		String ind_best_str = "";
		
		for (int part = 1; part <= 5; part++){
			map_best_individuals_par.add(new Vector<Double>());
			map_best_individuals_run.add(new Vector<Double>());
			String partition = "u"+part;
			
			Vector<File> vf_reeval = new Vector<File>();
			for (String input_rank : reeval_rankings){
				vf_reeval.add(new File(data_folder+partition+"-"+input_rank));
			}


			File test_reeval = new File(data_folder+partition+".test");					
			File usermap_reeval = new File(data_folder+partition+".base.usermap");

			InputData dados_reeval = new InputData(vf_reeval,test_reeval,test_reeval,
					usermap_reeval,numItemsToUse,numItemsToSuggest); 

			//******************** END READING ***************************************

			evolution_state.startFresh();
			GPRA_Problem problem = (GPRA_Problem) evolution_state.evaluator.p_problem;
			
	
			
			((ec.Problem)problem).prepareToEvaluate(evolution_state,0);
			((GPRA_Problem)problem).set_data(dados_reeval);
			((GPRA_Problem)problem).set_save_ranking(true);      
				
			//Reevaluate the best individuals of each run
			for (Individual ind : best_individuals_by_run){
				if (ind != null){
					System.out.println(((KozaFitness)ind.fitness).standardizedFitness());
					ind.evaluated = false;
					problem.evaluate(evolution_state,ind, 0, 0);
					//ind_run_str += ind.genotypeToStringForHumans()+"\n";
					map_best_individuals_run.lastElement().add(((KozaFitness)ind.fitness).standardizedFitness());
				}
			}
			//Reevaluate the best individuals of each partition
			for (Individual ind : best_individuals_by_part){
				if (ind != null){
					System.out.println(((KozaFitness)ind.fitness).standardizedFitness());
					ind.evaluated = false;
					problem.evaluate(evolution_state,ind, 0, 0);
					//ind_part_str += ind.genotypeToStringForHumans()+"\n";
					map_best_individuals_par.lastElement().add(((KozaFitness)ind.fitness).standardizedFitness());
				}
			}
			
			
			System.out.println("Best");
			System.out.println(((KozaFitness)best_individual_all.fitness).standardizedFitness());
			//Reevaluate the best individual of all runs x partitions
			
			best_individual_all.evaluated = false;
			problem.evaluate(evolution_state, best_individual_all, 0, 0);
			//ind_best_str = best_individual_all.genotypeToStringForHumans()+"\n";
			map_best_all.add(((KozaFitness)best_individual_all.fitness).standardizedFitness());
			
			
			
			
			
			((ec.Problem)problem).finishEvaluating(evolution_state,0);
			
			
			
			PrintWriter print_ranking = new PrintWriter(new BufferedWriter(
					new FileWriter(output_dir+partition+"best_all"+"_GPRA.out")));

			for (User u : dados_reeval.Usuarios){
				print_ranking.write(u.print_gpra_ranking(numItemsToUse)+"\n");
			}

			print_ranking.close();
			
			
		}
		
		Evolve.cleanup(evolution_state);
		out_reeval.write("Map Best Individuals by partition\n");
		
		//Na impressao dos resultados a linha e a coluna das matrizes de resultado esto invertidas
		//fiz isso para que os resultados por indivduo aparecam por linha na sada
		// 		u1;u2;u3;u4;u5
		//i1	xx;xx;xx;xx;xx
		//i2	xx;x;;xx;xx;xx
		
		for (int k = 0; k < 5; k++){
			ind_part_str += best_individuals_by_part.get(k).genotypeToStringForHumans()+"\n";
			String s = map_best_individuals_par.get(0).get(k)+""; //
			for (int h = 1; h < nruns; h ++){
				s += ";"+map_best_individuals_par.get(h).get(k);
			}
			out_reeval.write(s+"\n");
			
		}
		
		out_reeval.write("Map Best Individuals by run\n");
		
		for (int k = 0; k < nruns; k++){
			ind_run_str += best_individuals_by_run.get(k).genotypeToStringForHumans()+"\n";
			String s = map_best_individuals_run.get(0).get(k)+"";
			for (int h = 1; h < 5; h ++){
				s += ";"+map_best_individuals_run.get(h).get(k);
			}
			out_reeval.write(s+"\n");
		}

		
		out_reeval.write("Map Best Individual\n");
		String s = ""+map_best_all.get(0);
		for (int k = 1; k < 5; k++){
			s += ";"+map_best_all.get(k);
		}
		
		out_reeval.write(s+"\n");
		ind_best_str = best_individual_all.genotypeToStringForHumans();
		
		out_reeval.write("Best Individuals Phenotype\n");
		out_reeval.write(ind_part_str);
		out_reeval.write(ind_run_str);
		out_reeval.write(ind_best_str);		
		out_reeval.close();
		System.out.println("FIM");
		
		
	}

	public static void main(String args[]) throws IOException, URISyntaxException{

							
		String base_dir = "../GP_RankingAggregation/rankings/20151202/ml-100k/";
		String out_dir = "out_test/";
		String partition = "u1";
		int numGenerations = 50;
		int numIndividuals = 100;
		int maxIterWithoutImprove = 50;
		int maxTreeSize = 10;
		int tournament_size = 7;
		int pini = 1, pend = 1;
		double mutationProb = 0.65;
		double xoverProb = 0.25;	
		double reproductionProb = 0.1;
		use_outrank = true;

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
					out_dir = args[ar].split("=")[1];
					break;
				case "base": 
					partition = args[ar].split("=")[1];
					break;
				case "numg":
					numGenerations = Integer.parseInt(args[ar].split("=")[1]);
					break;
				case "numi":
					numIndividuals = Integer.parseInt(args[ar].split("=")[1]);
					break;
				case "mut":
					mutationProb = Double.parseDouble(args[ar].split("=")[1]);
					break;
				case "xover":
					xoverProb = Double.parseDouble(args[ar].split("=")[1]);
					break;
				case "rep":
					reproductionProb = Double.parseDouble(args[ar].split("=")[1]);
					break;
				case "stop":
					maxIterWithoutImprove = Integer.parseInt(args[ar].split("=")[1]);
					break;
				case "tree_size":
					maxTreeSize = Integer.parseInt(args[ar].split("=")[1]);
					break;
				case "i2use":
					numItemsToUse = Integer.parseInt(args[ar].split("=")[1]);
					break;
				case "i2sug":
					numItemsToSuggest = Integer.parseInt(args[ar].split("=")[1]);
					break;
				case "pini":
					pini = Integer.parseInt(args[ar].split("=")[1]);
					break;
				case "pend":
					pend = Integer.parseInt(args[ar].split("=")[1]);
					break;
				case "outrank":
					use_outrank = Boolean.parseBoolean(args[ar].split("=")[1]);
					break;
				case "bkp":
					need_backup = Boolean.parseBoolean(args[ar].split("=")[1]);
					break;
				case "K":
					tournament_size = Integer.parseInt(args[ar].split("=")[1]);					
					break;
				case "nruns":
					nruns = Integer.parseInt(args[ar].split("=")[1]);
					break;
				case "noGP":
					runGP = false;
					break;
				default:
					System.err.println("Param "+ attr + "doesn't exist");
					System.err.println("Parameters: base, numg, numi, mut, xover, rep, stop, tree_size");
					return;
				}


			}
		}
		
		
		
		
		
		
		
		
		
		
		//int nruns = 5;
		File output_dir = new File(out_dir);
		if (!output_dir.exists())
			output_dir.mkdir();
		
		//vetores com os melhores individuos por rodada e por particao
		best_individuals_by_run = new Vector<Individual>(nruns);
		best_individuals_by_part = new Vector<Individual>(nruns);
		for(int ii = 0 ; ii < nruns; ii++){
			best_individuals_by_part.add(null);
			best_individuals_by_run.add(null);
		}
		//********************************end*********************************	
			
		
		Set<String> input_rankings = new HashSet<String>();
		
		File folder = new File(base_dir);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains(".out") ) {
				String rank_name = listOfFiles[i].getName().substring(3);
				input_rankings.add(rank_name);
			} 
		}
		    
		
		
		
		//iterao para as parties
		for(int part =pini; part <= pend; part++){
			dados = null;

			partition = "u"+part;
			Vector<File> vf = new Vector<File>();
			for (String input_rank : input_rankings){
				vf.add(new File(base_dir+partition+"-"+input_rank));
			}



			//Carrega os arquivos de teste, validacao e o arquivo que mapeia a o ID de cada usuario pela
			//posicao que este usuario aparece nos arquivos de treino e teste
			File test = new File(base_dir+partition+".test");
			File validacao = new File(base_dir+partition+".validation");
			File usermap = new File(base_dir+partition+".base.usermap");

			try {
				//dados = new InputData(vf, test,numItemsToUse,numItemsToSuggest);
				//dados = new InputData(vf,validacao,test,numItemsToUse,numItemsToSuggest);
				//System.out.println("0");
				dados = new InputData(vf,validacao,test,usermap,numItemsToUse,numItemsToSuggest); //read data and usermap
				//dados.read_user_categories(new File(base_dir+"matrix_user_cat_"+partition+".base"));
				//dados.read_item_categories(new File(base_dir+"matrix_item_cat"));
				//dados.calc_item_categories_scores(new File(base_dir+"matrix_item_cat"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			Vector<User> usuarios = dados.getUsers();
			//System.out.println("1");
			for(User u : usuarios){
				u.addAlternativeRanking(new Vector<Integer>(CombMethods.CombSUM(u).subList(0, numItemsToUse)));
				u.addAlternativeRanking(new Vector<Integer>(CombMethods.CombMNZ(u).subList(0, numItemsToUse)));
				u.addAlternativeRanking(new Vector<Integer>(CombMethods.BordaCount(u).subList(0, numItemsToUse)));
				//u.addAlternativeRanking(new Vector<Integer>(
				//		CombMethods.outrank_approach(
				//				u,numItemsToUse,dados.getNumRankings()).subList(0, numItemsToUse)));
			}


			//PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("Log_"+"r"+i+"_r"+j+partition+".data")));
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(out_dir+partition+"_baselines"+".log")));

			int iri = 0; 
			for (String input_rank : input_rankings){
				out.write("MAP "+iri+" : "+input_rank+"\n");
				iri++;
			} 

			UtilStatistics.printMAP(dados, numItemsToUse, numItemsToSuggest, false, out);
			UtilStatistics.printMAP(dados, numItemsToUse, numItemsToSuggest, true, out);
			
			for (User u : usuarios){
				u.freeOriginalRankings();
			}
			System.gc();
			//UtilStatistics.meanJaccard(dados, out, false);
			//UtilStatistics.meanHitts(dados,false,out);
			//UtilStatistics.meanHitts(dados,true,out);
			//UtilStatistics.HitItemsStatistics(usuarios, out);

			out.close();

			
			//READ THE DATA TO REEVALUATE THE BEST INDIVIDUAL
			Set<String> reeval_rankings = new HashSet<String>();

			File folder_reeval = new File(base_dir+"reeval/");
			File[] listOfFiles_reeval = folder_reeval.listFiles();

			for (int i = 0; i < listOfFiles_reeval.length; i++) {
				if (listOfFiles_reeval[i].isFile() && listOfFiles_reeval[i].getName().contains(".out") ) {
					String rank_name = listOfFiles_reeval[i].getName().substring(3);
					reeval_rankings.add(rank_name);
				} 
			}

			Vector<File> vf_reeval = new Vector<File>();
			for (String input_rank : reeval_rankings){
				vf_reeval.add(new File(base_dir+"reeval/"+partition+"-"+input_rank));
			}


			File test_reeval = new File(base_dir+"reeval/"+partition+".test");					
			File usermap_reeval = new File(base_dir+"reeval/"+partition+".base.usermap");

			InputData dados_reeval = new InputData(vf_reeval,test_reeval,test_reeval,
					usermap_reeval,numItemsToUse,numItemsToSuggest);
			
			Vector<User> usuarios_reeval = dados_reeval.getUsers();
			for(User u : usuarios_reeval){
				u.addAlternativeRanking(new Vector<Integer>(CombMethods.CombSUM(u).subList(0, numItemsToUse)));
				u.addAlternativeRanking(new Vector<Integer>(CombMethods.CombMNZ(u).subList(0, numItemsToUse)));
				u.addAlternativeRanking(new Vector<Integer>(CombMethods.BordaCount(u).subList(0, numItemsToUse)));
				u.addAlternativeRanking(new Vector<Integer>(
						CombMethods.outrank_approach(
								u,numItemsToUse,dados_reeval.getNumRankings()).subList(0, numItemsToUse)));
			}
			//***********************************END****************************************************

			/*PrintWriter outrhp = new PrintWriter(new BufferedWriter(new FileWriter("rankHitPositions_"+partition)));
				outrhp.println("Hits baselines");
				UtilStatistics.hitsPositions(usuarios, outrhp);
				outrhp.println("Hits Aggregation");
				UtilStatistics.hitsPositions_aggregation(usuarios, outrhp);

				Utils.save_alternative_ranking(usuarios,partition);

				System.out.println("4");
				outrhp.close();
			 */

			//CombMethods.outrank_approach(usuarios.firstElement());

			PrintWriter out_reeval = new PrintWriter(new BufferedWriter(new FileWriter(out_dir+partition+"baselines"+"_reeval.log")));
			//UtilStatistics.printMAP(dados_reeval, numItemsToUse, numItemsToSuggest, false, out)
			UtilStatistics.printMAP(dados_reeval, numItemsToUse, numItemsToSuggest, true, out_reeval);

			out_reeval.close();

			//######################################## Start GP ################################################
			ParameterDatabase parameters = null;
			if (runGP)
			{
				
				File param_file = new File("./params/gpra.params");
				parameters = new ParameterDatabase(param_file);
				
				System.out.println("Start GP");

				//URL fileURL = GPRA_Principal.class.getResource("./params/gpra.params"); 

				//ParameterDatabase parameters = new ParameterDatabase(param_file,new String[] {" -file", param_file.getCanonicalPath()});
				//System.out.println(parameters.getLong(new Parameter("seed.0"),null));
				long seeds0[] = {212341, -1234, 9801, 123121, -67212};
				long seeds1[] = {97382, 16352, -982, 1561, 5423};
				long seeds2[] = {-8233, 6723, -43342, 873223, 345};
				long seeds3[] = {-7273, -2673, 675467, 3243, 837212};


				parameters.set(new Parameter("generations"),""+numGenerations);
				parameters.set(new Parameter("pop.subpop.0.size"),""+numIndividuals);
				parameters.set(new Parameter("pop.subpop.0.species.pipe.source.0.prob"),""+xoverProb);
				parameters.set(new Parameter("pop.subpop.0.species.pipe.source.1.prob"),""+mutationProb);
				parameters.set(new Parameter("pop.subpop.0.species.pipe.source.2.prob"),""+reproductionProb);
				parameters.set(new Parameter("select.tournament.size"),""+tournament_size);
				parameters.set(new Parameter("gp.koza.xover.maxdepth"), ""+maxTreeSize);
				parameters.set(new Parameter("gp.koza.mutate.maxdepth"), ""+maxTreeSize);



				for (int run = 0; run < nruns; run++)
				{
					long time = System.currentTimeMillis();

					//parameters.set(new Parameter("seed.0"), ""+seeds0[run]);
					//parameters.set(new Parameter("seed.1"), ""+seeds1[run]);
					//parameters.set(new Parameter("seed.2"), ""+seeds2[run]);
					//parameters.set(new Parameter("seed.3"), ""+seeds3[run]);


					Output outGP = Evolve.buildOutput();
					outGP.setFilePrefix(out_dir+partition+".run"+run+"_individuals_");
					outGP.getLog(0).silent = true; // stdout
					outGP.getLog(1).silent = false; // stder


					MySimpleEvolutionState evaluatedState = (MySimpleEvolutionState)Evolve.initialize(parameters, 0, outGP);


					evaluatedState.setMaxIterWithoutImprove(maxIterWithoutImprove);
					evaluatedState.setAternativeOutput(new File(out_dir+partition+".run"+run+"_statistics"));

					//########## LOG ##############################################
					String gpParams = "Num Generations:" + numGenerations + "\n";
					gpParams += "Num Individuals: " +  numIndividuals + "\n";
					gpParams += "Xover Prob: " + xoverProb + "\n";
					gpParams += "Mutation Prob: " + mutationProb + "\n";
					gpParams += "Reproduction Prob: " + reproductionProb + "\n";
					gpParams += "Max Tree Size: " + maxTreeSize + "\n";
					gpParams += "Num Itens to Use: " + numItemsToUse + "\n";
					gpParams += "Num Itens to Suggest: " + numItemsToSuggest + "\n";
					gpParams += "Max Gen. Without Improve: " + maxIterWithoutImprove + "\n";
					//System.out.println(gpParams);
					evaluatedState.writeAlternativeOutput(gpParams);
					//##############################################################

					evaluatedState.run(EvolutionState.C_STARTED_FRESH);

					time = System.currentTimeMillis() - time;
					//System.out.println("Tempo total: " + time);
					outGP.message("Total Time(s): " + time/1000);

					outGP.print("\n\nTotal Time(s): " + time/1000, 2);

					Individual[] best_inds; 
					best_inds =  ((SimpleStatistics)(evaluatedState.statistics)).getBestSoFar();


					//Salvando o melhor individuo em um arquivo
					File best_inds_dir = new File(out_dir+"best_ind_binaries/");
					if (!best_inds_dir.exists())
						best_inds_dir.mkdir();

					OutputStream out_stream_bestind = new FileOutputStream(best_inds_dir.toString()+"/u"+part+".run"+run+".bestind");			    				DataOutputStream data_out_stream = new DataOutputStream(out_stream_bestind);
					best_inds[0].writeIndividual(evaluatedState, data_out_stream);			    
					data_out_stream.close();

					//**************************************end**********************************************

					//Salvando o melhor individuo da rodada para aquela particao
					//caso o individuo seja melhor que o individuo de outras particoes ele sera armazenado
					//caso contrario sera descartado
					if (need_backup){
						backup_individual(best_individuals_by_run, best_inds[0], run);
						backup_individual(best_individuals_by_part, best_inds[0], part-1);
					}
					//dados.Usuarios = null;	

					Evolve.cleanup(evaluatedState);


					//***************************REEVALUATE THE BEST INDIVIDUAL**********************************

					out_reeval = new PrintWriter(new BufferedWriter(new FileWriter(out_dir+partition+".run"+run+"_reeval.log")));
					//UtilStatistics.printMAP(dados_reeval, numItemsToUse, numItemsToSuggest, false, out);




					UtilStatistics.printMAP(dados_reeval, numItemsToUse, numItemsToSuggest, true, out_reeval);


					//******************** Reeval ***************************************

					//SimpleProblemForm problem = (SimpleProblemForm) evaluatedState.evaluator.p_problem;

					//*******************Initialize Evolution State******************************
					MySimpleEvolutionState evolution_state_reeval = (MySimpleEvolutionState)Evolve.initialize(parameters, 0);
					evolution_state_reeval.startFresh();
					GPRA_Problem problem = (GPRA_Problem) evolution_state_reeval.evaluator.p_problem;

					((ec.Problem)problem).prepareToEvaluate(evolution_state_reeval,0);
					((GPRA_Problem)problem).set_data(dados_reeval);
					((GPRA_Problem)problem).set_save_ranking(true);  

					//*******************End Initialize Evolution State******************************


					((ec.Problem)problem).prepareToEvaluate(evaluatedState,0);
					((GPRA_Problem)problem).set_data(dados_reeval);
					((GPRA_Problem)problem).set_save_ranking(true);      

					Individual[] inds = best_inds;
					for (int xi = 0; xi < inds.length; xi++){
						inds[xi].evaluated = false;
						problem.evaluate(evaluatedState,inds[xi], 0, 0);
					}

					((ec.Problem)problem).finishEvaluating(evaluatedState,0);

					out_reeval.write("Best Individual"+"\n");
					double fit = 1-((KozaFitness)inds[0].fitness).standardizedFitness();
					out_reeval.write(fit+"\n");

					System.out.println("BEST INDIVIDUAL RESULT");
					System.out.println(inds[0].fitness.fitness());
					System.out.println(inds[0].fitness.fitnessToStringForHumans());

					out_reeval.close();


					PrintWriter print_ranking = new PrintWriter(new BufferedWriter(
							new FileWriter(out_dir+partition+"run"+run+"_GPRA.out")));

					for (User u : usuarios_reeval){
						print_ranking.write(u.print_gpra_ranking(numItemsToUse)+"\n");
					}

					print_ranking.close();
				}

				//########################################## END GP ###################################################3


			}
			//S realiza a reavaliacao completa quando o GP rodou para todas as particoes
			if (pini == 1 & pend == 5 & nruns==5)
				reeval_best_individuals(base_dir+"reeval/",parameters, out_dir);


		}
	}
	//}




}
