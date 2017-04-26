from scipy import stats
import sys


if __name__ == "__main__":




    f = open(sys.argv[1])
    
    dados = []
    names = []
    for line in f:
        if line != "\n":
            tokens = line.strip().split(";")
            names.append(tokens[0])
            val = [float(tokens[i]) for i in range(1,len(tokens))]
            dados.append(val)
        else:            
            for i in [2,3]:
                for j in [0,1]:
                    x1 = stats.ttest_ind(dados[i],dados[j])                       
                    print names[i]+" VS "+names[j]+":   "+str(x1)

            dados = []
            names = []



    for i in [2,3]:
        for j in [0,1]:
            x1 = stats.ttest_ind(dados[i],dados[j])                       
            print names[i]+" VS "+names[j]+":   "+str(x1)
