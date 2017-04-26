import numpy as np
import datetime
import sys
import os
import argparse


from sklearn import feature_selection
from sklearn import preprocessing
from sklearn import linear_model, decomposition, datasets
from sklearn.pipeline import Pipeline
from sklearn.grid_search import GridSearchCV
from sklearn.svm import l1_min_c
from operator import itemgetter
from sklearn.ensemble import RandomForestClassifier
from sklearn.svm import LinearSVC
from sklearn.linear_model import SGDClassifier




def read_data_ml(inputf):
    data = open(inputf,'r')
        
    user_items = {}
    past_usr,past_mov,past_rat = data.readline().strip().split('\t')
    user_items[int(past_usr)] = [int(past_mov)]
    #movies_freq[int(past_mov)] = 1

    line_usr = past_mov
    #nratings = 1
    nusers = 1;
    for line in data:
        usr,mov,rat = line.strip().split('\t')
   
        #creating user line
        if usr == past_usr:
            user_items[int(usr)].append(int(mov))
            #line_usr += ' '+mov
            past_usr,past_mov = usr,mov
        else:
            user_items[int(usr)] = [int(mov)]
            nusers += 1
            past_usr,past_mov = usr,mov


    return user_items


def read_data(inputf,atts_to_use='-1'):
    print(inputf)
    dataf = open(inputf)
    data = []
    Y = []
    set_of_atts = atts_to_use.split(',')
    for line in dataf:
        tokens = line.strip().split(';')
        #print tokens
        Y.append(int(tokens[-1]))
        #usa todos os atts    
        if atts_to_use == "-1":
            data.append([float(x) for x in tokens[:-1]])
        else:
            atts = []

            for att_range in set_of_atts: #selects the attributs that will be used in the training/test
                
                if '-' in att_range:
                    att_ini,att_end = att_range.split('-')
                    att_ini = int(att_ini)
                    att_end = int(att_end)+1
                    atts = atts + [float(x) for x in tokens[att_ini:att_end]]
                else:
                    atts.append(float(tokens[int(att_range)]))

            data.append(atts)

    return data, Y

    
def do_chi_squared(X,y,chisq_f):
    scaler = preprocessing.MinMaxScaler()
    X_scaled = scaler.fit_transform(X)
    chisq_res, pval= feature_selection.chi2(X_scaled, y)

    features = [str(x) for x in range(len(chisq_res))]

    atts_chisq = []
    i = 0
    for att in features:
        atts_chisq.append((att,chisq_res[i],pval[i]))
        i += 1

    atts_chisq = sorted(atts_chisq, key= lambda tup: tup[1] ,reverse=True)
    sorted_atts = [x for x,y,z in atts_chisq]

    for att_i in range(len(atts_chisq)-1):
        
        att,chisq_val,pval_val = atts_chisq[att_i]
        chisq_f.write("%s (%.2f,%.2f), " %(att,chisq_val,pval_val))

    att_i = len(features)-1
    att,chisq_val,pval_val = atts_chisq[att_i]
    chisq_f.write("%s (%.2f,%.2f)\n" %(att,chisq_val,pval_val))

    

    return chisq_res, pval, sorted_atts


def parse_args():
    """Parses command line parameters through argparse and returns parsed args.
    """
    parser = argparse.ArgumentParser()
    parser.add_argument("-alg",type=str,default="LR",
                        help="the algoritm that will be used [LR,RF,SGD,SVM]")
    parser.add_argument("-data", type=str)
    parser.add_argument("-o", "--output_dir", type=str, default=".",
                        help="Folder where the output files will be saved")
    parser.add_argument("-C",type=float,default=1.0,
                        help = "C parameter for LR and SVM")    
    parser.add_argument("-penalty", type=str, default="l1")

    parser.add_argument("-loss", type=str, default="modified_huber")

    parser.add_argument("-pref_chisq",type=str,default="alg", help="the prefix to be used in the chisq file")
    #parser.add_argument('-h',type=int, default=0, help="svm shrinking heuristics")

    parser.add_argument("-t",type=int,default=2,help="The kernel type of SVM 0: linear - 2: rbf")
    parser.add_argument('-att_range', type=str, default='-1', 
                        help="the range of the atributes that will be used. " +
                        "When set to -1 we will use all the attributes")

    #o n_jobs soh esta disponivel a partir da versao 0.17
    #parser.add_argument("-n_jobs", type=int,default=1)



    return parser.parse_args()



learning_algs = {
    "chisq" : None,
    "SVM" : None,
    "LR" : linear_model.LogisticRegression,
    "RF" : RandomForestClassifier,
    "SGD" : SGDClassifier
}


learning_params = {
    'chisq' : [],
    'SVM' : ['C'],
    'LR' : ['penalty','C'],
    'RF' : ['n_estimators'],
    'SGD' : ['loss','penalty']
}




def CombSUM(rankings):

    scores = {}    

    for rank in rankings:
        for pos,elem in enumerate(rank):
            if elem in scores :            
                scores[elem] += 1 -  float(pos)/len(rank)
            else:
                scores[elem] = 1 - float(pos)/len(rank)



    final_rank = [x for x,y in sorted(scores.items(), key = lambda tup : tup[1],reverse=True)]

    return final_rank



def run(basedir,outdir):

    if not os.path.isdir(outdir):
        os.mkdir(outdir)
  

    #clf_name = args.alg
    clf_name = "chisq"
    #for clf,clf_name in classifiers:
    
    #if args.alg == 'chisq':
    chisq_f = open(os.path.join(outdir,'features_chisq'),'w')


    print("Testing "+clf_name)
    
    for part in range(1,6):
        X,y = read_data(basedir+"u"+str(part)+".train_logit",'-1')
        #print "INICIO PARTICAO " + str(part)
        sorted_atts = []
        if clf_name == 'chisq':
            chisq_f.write("Partition "+str(part)+"\n")
            chisq_f.write("Validation\n")
            chi2_res, pval, sorted_atts_part = do_chi_squared(X,y,chisq_f)
            sorted_atts.append(sorted_atts_part)        
    final_atts_order = CombSUM(sorted_atts)

    #if args.alg == 'chisq':
    chisq_f.close()


    return final_atts_order



if __name__ == "__main__":

    args = parse_args()
    
    #recupera somente os parametros relacionados ao algoritmo que sera executado
    alg_args = {x:args.__dict__[x] for x in learning_params[args.alg]}
    
    basedir = args.data#sys.argv[1]
    outdir = args.output_dir #sys.argv[2]

    x = run(basedir,outdir)
    print(x)

