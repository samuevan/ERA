import os
import sys


if __name__ == "__main__":

    base = sys.argv[1]
    

    mut = [10,15]
    xover = [90,85]
    rep = [1]
    tourn = [3,5]
     
    for r in rep:
        for m,x in zip(mut,xover):
            for k in tourn:
                out_dir = "exp_m%dx%dr%d_k=%d/" %(m,x-r,r,k)
                os.system("java -jar gpra_victor_20160127.jar -base_dir=%s -out_dir=%s " 
                            "-numg=200 -numi=100 -outrank=true"
                            "-pini=1 -pend=5 -nruns=1 -mut=%d -xover=%d -rep=%d -K=%d&"
                            %(base,out_dir,m,x-r,r,k))




