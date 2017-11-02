package ec.app.gpra;

import java.awt.List;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import ec.Individual;
import ec.app.data.User;
import ec.gp.koza.KozaFitness;
import ec.simple.SimpleStatistics;
import ec.util.Checkpoint;
import ec.util.Parameter;
import java.util.Random;

public class MySimpleEvolutionState extends ec.simple.SimpleEvolutionState {

	private static int itersWithoutImprove = 0;
	private int maxIterWithoutImprove = -1;
	private static double pastFit = 0;
	private int use_niching = 0;
	private long seed[] = {-16592, -21213, 13457, -33895, 29470,-23041, 18225, 17828, 18312, 19608,
	                     5699, -31501, 31151, -13867, -29392,33189, -1204, -35, -11219, -7766,
	                     18309, -5653, 33301, 2107, -18868,7656, 19977, -2954, -14739, 26855};
	File alternativeOutput;
	private PrintWriter alternativeOutWritter = null;
	private boolean isAltOutputOpen = false;

	public int evolve() {
		if (generation > 0)
			output.message("Generation " + generation);

		// EVALUATION
		statistics.preEvaluationStatistics(this);
		evaluator.evaluatePopulation(this);
		statistics.postEvaluationStatistics(this);

		if (generation % 10 == 0) {
			save_best_individual();			
		}

		// #################################################################
		// My statistics
		MyStatistics mystat = new MyStatistics(this.population);
		alternativeOutWritter.write("Generation " + generation + ":\n");
		alternativeOutWritter.write(mystat.toString());
		String niches_str = "Niches: ";
		// ######################################################################

		// Verify how many generations without improve
		if (generation == 1)
			pastFit = mystat.getbestFitnessVal();

		double fit = mystat.getbestFitnessVal();

		if (Math.abs(fit - pastFit) < 0.0001) {
			itersWithoutImprove++;
		} else {
			pastFit = fit;
			itersWithoutImprove = 0;
		}

		// SHOULD WE QUIT?
		if (itersWithoutImprove == maxIterWithoutImprove - 1) {
			output.message("Achieved " + maxIterWithoutImprove
					+ "generations without improve");
			alternativeOutWritter.write("Achieved " + maxIterWithoutImprove
					+ "generations without improve");
			alternativeOutWritter.close();

			return R_FAILURE;
		}

		// SHOULD WE QUIT?
		if (evaluator.runComplete(this) && quitOnRunComplete) {
			output.message("Found Ideal Individual");

			alternativeOutWritter.close();
			return R_SUCCESS;
		}

		// SHOULD WE QUIT?
		if (generation == numGenerations - 1) {
			alternativeOutWritter.close();
			return R_FAILURE;
		}

		// PRE-BREEDING EXCHANGING
		statistics.prePreBreedingExchangeStatistics(this);
		population = exchanger.preBreedingExchangePopulation(this);
		statistics.postPreBreedingExchangeStatistics(this);

		String exchangerWantsToShutdown = exchanger.runComplete(this);
		if (exchangerWantsToShutdown != null) {
			output.message(exchangerWantsToShutdown);
			/*
			 * Don't really know what to return here. The only place I could
			 * find where runComplete ever returns non-null is IslandExchange.
			 * However, that can return non-null whether or not the ideal
			 * individual was found (for example, if there was a communication
			 * error with the server).
			 * 
			 * Since the original version of this code didn't care, and the
			 * result was initialized to R_SUCCESS before the while loop, I'm
			 * just going to return R_SUCCESS here.
			 */

			return R_SUCCESS;
		}

		// BREEDING
		statistics.preBreedingStatistics(this);

		// Fitness Sharing
		Vector<Vector<MyIndividual>> niches = count_individuals_with_equal_fitness();
		use_niching = GPRA_Principal.get_use_niching();
		if (use_niching == 1) {
			// fitness_sharing(niches);
			double theta_share = parameters.getDouble(new Parameter(
					"gpra.niching.fitness_tshare"), new Parameter(
					"gpra.niching.tshare"), 0);
			fitness_sharing(use_niching, theta_share, 1.0);

		} else {
			if (use_niching == 2) {
				double theta_share = parameters.getDouble(new Parameter(
						"gpra.niching.pheno_tshare"), new Parameter(
						"gpra.niching.tshare"), 0);
				fitness_sharing(use_niching, theta_share, 1.0);
			}
		}
		// TODO salvar actual fitness
		for (int n = 0; n < niches.size(); n++) {
			niches_str += niches.get(n).size() + ",";
		}
		// alternativeOutWritter.write(niches_str+"\n");
		alternativeOutWritter.write("\n");

		alternativeOutWritter.flush();
		// End Fitness Sharing

		population = breeder.breedPopulation(this);

		// POST-BREEDING EXCHANGING
		statistics.postBreedingStatistics(this);

		// POST-BREEDING EXCHANGING
		statistics.prePostBreedingExchangeStatistics(this);
		population = exchanger.postBreedingExchangePopulation(this); // didn't
																		// do
																		// anything
		statistics.postPostBreedingExchangeStatistics(this);

		// INCREMENT GENERATION AND CHECKPOINT
		generation++;
		if (checkpoint && generation % checkpointModulo == 0) {
			output.message("Checkpointing");
			statistics.preCheckpointStatistics(this);
			Checkpoint.setCheckpoint(this);
			statistics.postCheckpointStatistics(this);
		}

		return R_NOTDONE;
	}

	public void save_best_individual() {

		Individual best_ind_so_far = ((SimpleStatistics) statistics)
				.getBestSoFar()[0];

		HashMap<String, String> running_status = GPRA_Principal
				.get_curr_status();

		File best_inds_dir = new File(running_status.get("out_dir")
				+ "best_ind_binaries_partial/");
		if (!best_inds_dir.exists())
			best_inds_dir.mkdir();

		OutputStream out_stream_bestind = null;
		try {
			out_stream_bestind = new FileOutputStream(best_inds_dir.toString()
					+ "/u" + running_status.get("part") + ".run"
					+ running_status.get("run") + ".bestind_generation"
					+ generation);
			DataOutputStream data_out_stream = new DataOutputStream(
					out_stream_bestind);
			best_ind_so_far.writeIndividual(this, data_out_stream);
			data_out_stream.close();

		} catch (FileNotFoundException fne) {
			System.err
					.println("[ERR] Error when saven best individual of generation "
							+ generation);
			System.out.println(fne);
		} catch (IOException e) {
			// TODO: handle exception
			System.err
					.println("[ERR] Error when saven best individual of generation "
							+ generation);
			System.out.println(e);
		}
	}

	public void setAternativeOutput(File alternativeOut)
			throws FileNotFoundException {
		alternativeOutWritter = new PrintWriter(alternativeOut);
		isAltOutputOpen = true;

	}

	public void writeAlternativeOutput(String s) {
		if (isAltOutputOpen) {
			alternativeOutWritter.write(s + "\n");
		}
	}

	public void setMaxIterWithoutImprove(int max) {
		maxIterWithoutImprove = max;
	}

	/**
	 * 
	 * @param theta_share
	 * @param alpha
	 * 
	 *            Fitness sharing implementado da forma Fs_i = F_i /
	 *            sum{sh(d(i,j))}
	 */
	public void fitness_sharing(int use_niching, double theta_share,
			double alpha) {

		int num_users_to_eval = GPRA_Principal.getData().getNumUsers() / 50;
		int num_items_to_sug = GPRA_Principal.getData().getNumItemsToSuggest();
		int randomic_users_pos[] = new int[num_users_to_eval];

		for (int i = 0; i < num_users_to_eval; i++) {
			randomic_users_pos[i] = this.random[0].nextInt(GPRA_Principal
					.getData().getNumUsers());
		}

		for (Individual ind1 : population.subpops[0].individuals) {
			double ind_fit = ((KozaFitness) ind1.fitness).standardizedFitness();
			double sh_sum = 0;
			// double sh_sum_p = 0;
			for (Individual ind2 : population.subpops[0].individuals) {

				double dist = 0;
				if (use_niching == 1)
					dist = fitness_distance((MyIndividual) ind1,
							(MyIndividual) ind2);
				else
					dist = phenotype_distance((MyIndividual) ind1,
							(MyIndividual) ind2, randomic_users_pos);

				sh_sum += sharing_fitness(dist, theta_share, alpha);
				// sh_sum_p += sharing_fitness(dist, 0.85, 2);
				/*
				 * if (dist_p < 0.8){ System.out.println(ind1);
				 * System.out.println(ind2); System.out.println(dist_p); }
				 */
			}
			// Como o problema é de minimização (minimizo 1-map) eu tenho que
			// converter o valor
			// da fitness para 1-fit de forma que eu possa dividir pelo sh_sum
			// (o somatorio dos itens proximos)

			ind_fit = 1 - ind_fit;
			double new_fit = ind_fit / sh_sum;
			new_fit = 1 - new_fit;
			((KozaFitness) ind1.fitness).setStandardizedFitness(this, new_fit);
		}

	}

	public double sharing_fitness(double dist, double theta_share, double alpha) {

		double sh_value = 0;

		if (dist < theta_share) {
			sh_value = 1 - Math.pow((dist / theta_share), alpha);
		}

		return sh_value;
	}

	/**
	 * @param niches
	 *            matriz contendo os grupos de indivíduos que possuem o mesmo
	 *            valor de fitness
	 * 
	 * 
	 *            Recebe uma matriz que agrupa os individuos com os mesmos
	 *            valores de fitness e aumenta a fitness de cada um desses
	 *            individuos proporcionalmente à quantidade de individuos no
	 *            mesmo grupo
	 */
	private void fitness_sharing(Vector<Vector<MyIndividual>> niches) {

		for (Vector<MyIndividual> niche : niches) {

			for (MyIndividual ind : niche) {

				double fit = ((KozaFitness) ind.fitness).standardizedFitness();
				double new_fit = fit * (1 + niche.size() / 100.0); // TODO
																	// Verificar
																	// melhor
																	// estratégia
																	// aqui
				new_fit = Math.min(new_fit, 1);
				((KozaFitness) ind.fitness).setStandardizedFitness(this,
						new_fit);
			}

		}

	}

	private Vector<Vector<MyIndividual>> count_individuals_with_equal_fitness() {
		// MyIndividual array_individuals[] =
		// (MyIndividual[])population.subpops[0].individuals;
		ArrayList<MyIndividual> sorted_inds = new ArrayList<MyIndividual>();// (Arrays.asList(array_individuals));

		for (Individual ind : population.subpops[0].individuals) {
			sorted_inds.add((MyIndividual) ind);
		}

		Collections.sort(sorted_inds);
		Vector<Vector<MyIndividual>> equals = new Vector<Vector<MyIndividual>>();

		equals.add(new Vector<MyIndividual>());
		int eq = 0;
		for (int i = 1; i < sorted_inds.size(); i++) {
			if (((KozaFitness) sorted_inds.get(i).fitness)
					.standardizedFitness() == ((KozaFitness) sorted_inds
					.get(i - 1).fitness).standardizedFitness()) {
				equals.lastElement().add(sorted_inds.get(i - 1));
				eq += 1;
			} else {
				if (equals.lastElement().size() != 0) {
					equals.lastElement().add(sorted_inds.get(i - 1));
					equals.add(new Vector<MyIndividual>());
				}
				eq = 1;
			}
		}

		if (equals.lastElement().isEmpty())
			equals.remove(equals.size() - 1);

		return equals;

	}

	public double fitness_distance(MyIndividual ind1, MyIndividual ind2) {

		double dist = 0;

		dist = ((KozaFitness) ind1.fitness).standardizedFitness()
				- ((KozaFitness) ind2.fitness).standardizedFitness();

		return Math.abs(dist);

	}

	public double phenotype_distance(MyIndividual ind1, MyIndividual ind2,
			int[] randomic_users_pos) {

		Vector<User> users = GPRA_Principal.getData().getUsers();
		// int num_users_to_eval = GPRA_Principal.getData().getNumUsers()/20;
		int num_items_to_sug = GPRA_Principal.getData().getNumItemsToSuggest();
		// int randomic_users_pos[] = new int[num_users_to_eval];

		// for (int i = 0 ; i < num_users_to_eval; i++){
		// randomic_users_pos[i] = this.random[0].nextInt(users.size());
		// }

		float perc_diff_items = 0;
		int num_equal_items = 0;
		for (int user_pos : randomic_users_pos) {

			Vector<Integer> rank_ind1 = ((GPRA_Problem) this.evaluator.p_problem)
					.generate_ranking(this, ind1, 0, 0, users.get(user_pos));

			Vector<Integer> rank_ind2 = ((GPRA_Problem) this.evaluator.p_problem)
					.generate_ranking(this, ind2, 0, 0, users.get(user_pos));

			for (int i = 0; i < num_items_to_sug; i++) {
				if (rank_ind1.get(i) != rank_ind2.get(i)) {
					num_equal_items++;
				}
			}

		}

		perc_diff_items = ((float) num_equal_items)
				/ (randomic_users_pos.length * num_items_to_sug);

		return perc_diff_items;
	}
	
	
	public long getSeed(){
		return seed[generation/10];		
	}

}
