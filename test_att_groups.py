'''Este script recebe como parametro o caminho de uma base de dados onde cada linha representa um par usuario item
a primeira parte do script realiza um teste qui quadrado para ordenar os atributos.
Esses atributos são então utilizados para treinar o GP.
Os parametros do GP podem ser passados diretamente para o script
'''


import os
import sys
import perform_att_selection as att_select
import argparse
import random




def parse_args():

    parser = argparse.ArgumentParser()
    
    parser.add_argument("-data", type=str)
    parser.add_argument("-o", "--output_dir", type=str, default=".",
                        help="Folder where the output files will be saved")

    parser.add_argument("-numi",type=int, default=50,
        help="Number of indidividuals")
    parser.add_argument("-numg",type=int, default=200)
    parser.add_argument("-i2use", type=int, default=10)
    parser.add_argument("-i2sug", type=int, default=10)
    parser.add_argument("-pini", type=int, default=1)
    parser.add_argument("-pend", type=int, default=5)
    parser.add_argument("-use_plain", type=str,default='')
    parser.add_argument("-used_atts", type=str,default='[]')
    parser.add_argument("-mut",type=float, default=0.25)
    parser.add_argument("-xover", type=float, default=0.65)
    parser.add_argument("-rep", type=float, default=0.1)
    parser.add_argument("-n",type=int,default=1, help="Number of processess to run")
    parser.add_argument("-natt",type=int,default=5)
    parser.add_argument("-Xmx",type=int,default=5)
    parser.add_argument("-atts_file",type=str,default="",
        help="file containing the atributes ordered by a previous step of att selection")
    parser.add_argument("-tree_size",type=int,default=10,
        help="Number of levels used by the individuals")
    parser.add_argument("-nruns",type=int,default=5,
        help="Number of runs used by in ERA repetitions")

    


    return parser.parse_args()


def system_cmd(cmd):

    tmp_f = "tmp_file"+str(random.randint(0,10000))        
    os.system(cmd+" >> " + tmp_f)
        
    with open(tmp_f) as f:
        result = f.readlines()
    return result
    
    os.system("rm "+tmp_f)



def get_selected_attributes(atts_f):
    cmd_retrieve = "grep '(' " + atts_f 
    cmd_retrieve += " | sed 's/([0-9]*\\.[0-9]*,[0-9]*\\.[0-9]*)//g'"
    atts_retrieve = system_cmd(cmd_retrieve)

    atts_lists = []
    for line in atts_retrieve:
        atts_lists.append(line.strip().replace(' ','').split(','))

    return att_select.CombSUM(atts_lists)


    


if __name__ == '__main__':

    args = parse_args()

    if not args.atts_file:
        sorted_atts = att_select.run(args.data+"classif/",args.output_dir)    
    else:
        print("USING PRE DEFINED ATTS LISTS")
        sorted_atts = get_selected_attributes(args.atts_file)


    print(sorted_atts)

    #num_atts_to_use = [5,10,15,18]
    #for num_atts in num_atts_to_use:
    num_atts = args.natt
    sorted_atts_run = ','.join(sorted_atts[:num_atts])
    args.used_atts = sorted_atts_run

    cmd = "java -Xmx{Xmx}g -jar GPRA_versaoCEC_20161211.jar -base_dir={data} -out_dir={output_dir}"+str(num_atts)+"_atts/" +" -numg={numg} -numi={numi} -mut={mut} -xover={xover} -rep={rep} -i2use={i2use} -i2sug={i2sug} -use_plain -used_atts={used_atts} -pini={pini} -pend={pend} -tree_size={tree_size} -nruns={nruns} -nthreads={n}"
   
    os.system(cmd.format(**args.__dict__))


