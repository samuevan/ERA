import sys
import glob
import os
import calc_metrics
from multiprocessing import Queue
from multiprocessing import Process






def run_ERA():
    
    global Q

    while not Q.empty():
        params = Q.get()
        os.system("java -jar GPRA_versaoCEC_20160830.jar -base_dir=%s -out_dir=res_param_set%d/ -numg=%d -numi=%d -mut=%f -xover=%f -rep=%f -tree_size=10 -nruns=%d -i2use=10 -i2sug=10 -pini=1 -pend=5" %(params['basedir'],params['param_set'],params['numg'],params['numi'],params['mut'],params['xover'],params['rep'], params['nruns']))



if __name__ == '__main__':

    global Q

    basedir = sys.argv[1]

    generations = [5,10,20]
    individuals = [5,10,20]
    
    rep_values = [0.1,0.05]
    xover_values = [0.65,0.55,0.45]
    mut = 0.25
    
    runs = 1

    
    Q = Queue(9)
    parameter_set = []
    param_set = 0
    for numg in generations:
        for numi in individuals:                 
            Q.put({'numg':numg,'numi':numi,'rep':rep_values[0],
                                'xover':xover_values[0],'mut':mut,
                                'param_set':param_set, 'nruns':runs,
                                'basedir':basedir})                   
            param_set += 1                   


    consumers = []
    for i in range(3):
        p = Process(target=run_ERA)
        consumers.append(p)
        p.start()

    for p in consumers:
        print p
        p.join()

    #main(args)



            #os.system("java -jar GPRA_versaoCEC_20160830.jar -base_dir=%s -out_dir=res_param_set%d/ -numg=%d -numi=%d -mut=%f -xover=%f -rep=%f -tree_size=10 -nruns=%d -i2use=10 -i2sug=10 -pini=1 -pend=5 &" %(basedir,param_set,numg,numi,mut,xover_values[0],rep_values[0],runs))
            #param_set += 1



