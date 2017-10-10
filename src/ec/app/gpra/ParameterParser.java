package ec.app.gpra;
import java.util.List;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import static net.sourceforge.argparse4j.impl.Arguments.storeTrue;


public class ParameterParser {
	
	
	
	public static Namespace parse_arguments(String args[]){
		
		Namespace ns = null;
		
		ArgumentParser parser = ArgumentParsers.newArgumentParser("ERA")
                .defaultHelp(true)
                .description("Evolutionary Rank Aggregation");
		//TODO insert the option to include files in the MQ200X-agg format
        parser.addArgument("-b", "--base_dir")
        		.required(true)
                .type(String.class)                
                .help("Folder containing the dataset to be used in the rank aggregation. "
                		+ "could indicate a folder containing sets of rankings or a folder"
                		+ "containing plain files where the features are already computed");
        
        parser.addArgument("-o","--out_dir")
        		.setDefault("./outputERA/")
                .type(String.class)
                .help("Folder to save ERA outputs");

        //TODO verify if this argument is necessary
        parser.addArgument("-p","--part")
        		.setDefault("u1")
        		.type(String.class)
        		.help("Partition to be used in the aggregation");
        
        
        parser.addArgument("-g","--numg")
        		.type(Integer.class)
        		.setDefault(100)
        		.help("Number of generations to be used in the evolutionary process");
        
        parser.addArgument("-i","--numi")
				.type(Integer.class)
				.setDefault(50)
				.help("Number of individuals to be used in the evolutionary process");
        
        parser.addArgument("-m","--mut")
				.type(Double.class)
				.setDefault(0.35)
				.help("Mutation probability. Use range [0-1]");

        parser.addArgument("-x","--xover")
				.type(Double.class)
				.setDefault(0.65)
				.help("Crossover probability. Use range [0-1]");
        
        parser.addArgument("-r","--rep")
				.type(Double.class)
				.setDefault(0.1)
				.help("Mutation probability. Use range [0-1]");
        
        //change this name. As it is seems that is the maximum iter in general, and not without improvement 
        
        parser.addArgument("--max_iter")
				.type(Integer.class)
				.setDefault(50)
				.help("Number of generation without fitness improvement");
        
        parser.addArgument("--tree_size")
				.type(Integer.class)
				.setDefault(10)
				.help("The maximum number of tree levels. "
						+ "An individual is a mathematical expression represented as a tree");
        
        parser.addArgument("--init_run")
				.type(Integer.class)
				.setDefault(0)
				.help("The run to begin execution (Controls which set of seed will be used)");
        
        parser.addArgument("--i2use")
				.type(Integer.class)
				.setDefault(20)
				.help("The number of items to be used by the aggregation algorithm");
        
        parser.addArgument("--i2sug")
				.type(Integer.class)
				.setDefault(10)
				.help("The number of items to be saved in the aggregated ranking. "
						+ "It also controls the number of items used in the fitness function");
                
        parser.addArgument("--pini")
				.type(Integer.class)
				.setDefault(1)
				.help("The partiton to start execution");
        
        
        parser.addArgument("--pend")
				.type(Integer.class)
				.setDefault(5)
				.help("The partiton to end execution");

        parser.addArgument("--no_bkp")
				.type(Boolean.class)
				.action(storeTrue())
				.help("Dissable best individuals backup ");

        parser.addArgument("-K")
				.type(Integer.class)
				.setDefault(7)
				.help("Tournament size. (Notice the parameter is a capital K");
        
        parser.addArgument("--nruns")
				.type(Integer.class)
				.setDefault(5)
				.help("Number of runs to be used");
        
        parser.addArgument("--nthreads")
				.type(Integer.class)
				.setDefault(1)
				.help("The number of threads to be used during reproduction and evaluation "
						+ "(ECJ multithread)");
        
        parser.addArgument("--noGP")
				.type(Boolean.class)
				.setDefault(false)
				.action(storeTrue())
				.help("Dissable GP execution. "
						+ "Can be used to just save the atributes or to reeval the individuals");

        //it makes no sense to set the default as true and use the action store_true
        //however, i'm using this just to store the value true for this parameter
        //
        parser.addArgument("--no_outrank")
				.type(Boolean.class)
				.setDefault(false)
				.action(storeTrue())
				.help("Dissable the construction of the feature Outrank.");
        
        parser.addArgument("--use_sparse")
				.type(Boolean.class)
				.setDefault(false)
				.action(storeTrue())
				.help("enable the use of sparse vectors to store the attributes");
        
        parser.addArgument("--param")
				.type(String.class)
				.setDefault("./params/gpra.params")
				.help("File containing the ECJ parameters to be used");
        
        //TODO if use this we need to use another parameter to define the niching strategy
        parser.addArgument("--nich")
				.type(Integer.class)
				.choices(0, 1, 2)
				.setDefault(0)				
				.help("Chooses the type of niching to be used. 0) No niching 1) type1 2) type2 (NOT IMPLEMENTED)");

        parser.addArgument("--tshare")
        		.type(Double.class)
        		.setDefault(0.001)
        		.help("Theta share. Parameter used in fitness sharing niching strategie");
        

        parser.addArgument("--use_plain")
				.type(Boolean.class)
				.setDefault(false)
				.action(storeTrue())
				.help("Enables the use of a plain dataset containing all the features already computed");

        
        parser.addArgument("--gr")
				.type(Boolean.class)
				.setDefault(false)
				.action(storeTrue())
				.help("Run the ERA to the Group Recomendation problem. When enabled one needs to set"
						+ "the file containing the groups");                
        
        //TODO verify if it is possible to merge this parameter with --gr  
        parser.addArgument("--grs")
        		.nargs("*")
        		.help("List of Recommenders to be used in the group aggregation");
        
        parser.addArgument("--groups_file")
		.setDefault("")
        .type(String.class)
        .help("File contaning the groups information");

        
        
        parser.addArgument("--used_atts").nargs("+")
				.type(Integer.class)				
				.help("Position of the used attributes in the plain dataset");


        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
		
		
		return ns;
	}

}
