'''

@author: Andrew Habib

adopted from script by: Elisabetta Ghisu

'''

import argparse
import os
import sys
import numpy as np
from timeit import time

import Config
from GraphsLoader import load_graphs
from WLFunctions import WL_compute


def convert_km_to_vec_per_class(km, class_name_to_graphs_count):
    ts_file = os.path.abspath(Config.THREAD_SAFE_CLASSES)
    nts_file = os.path.abspath(Config.THREAD_UNSAFE_CLASSES)
    with open(ts_file, 'r') as f:
        ts_classes = [i.rstrip("\n") for i in f.readlines()]
         
    with open(nts_file, 'r') as f:
        nts_classes = [i.rstrip("\n") for i in f.readlines()]
    
    vec_per_class = []
    vec_label = []
    names = []
    
    class_graphs_idx = 0
    for class_name, count in class_name_to_graphs_count.items():
        # Here is where we summarize the graphs per class into one vector 
        class_vec = compute_min_max_avg_per_feature(km[class_graphs_idx : class_graphs_idx+count, ...])
        vec_per_class.append(class_vec)
        names.append(class_name)
        if class_name in ts_classes:
            vec_label.append(Config.Label.TS)
        elif class_name in nts_classes:
            vec_label.append(Config.Label.nTS)
        else:
            sys.exit("Found class in KM with no known label")
            
        class_graphs_idx += count
        
    return names, vec_per_class, vec_label
        
def compute_min_max_avg_per_feature(matrix):
    return np.concatenate(np.array([[matrix[..., i].min()] + [matrix[..., i].max()] + [matrix[..., i].mean()] for i in range(matrix.shape[1])]))

def save_labeled_vectors_to_arff(names, vec_per_class, vec_label, arff_file, h):
    assert len(names) == len(vec_per_class) == len(vec_label), "Ewww. Number of vectors, labels, and names do not match"
    
    nb_features = len(vec_per_class[0])
    with open(arff_file, "w") as f:
        f.write("@RELATION TSFinder:gk-WL-h{}\n\n".format(h))
        f.write("@ATTRIBUTE className STRING\n")
        for i in range(nb_features):
            f.write("@ATTRIBUTE " + "a" + str(i+1) + " NUMERIC\n")
        f.write("@ATTRIBUTE class {{{}, {}}}\n\n".format(Config.Label.TS, Config.Label.nTS))
        
        f.write("@DATA\n")
        for i in range(len(names)):
            f.write(names[i] + "," )
#             f.write(",".join("%.18e" % i for i in vec_per_class[i]) + ",")
            f.write(",".join("{:f}".format(i) for i in vec_per_class[i]) + ",")
            f.write(vec_label[i] + "\n")
    

if __name__ == '__main__':

    parser = argparse.ArgumentParser(description="Compute the WL Kernel matrix")
    parser.add_argument("--corpus", required=True, help="Name(path) to the corpus")
    parser.add_argument("--h", required=True, type=int, help="WL iterations")
    args = parser.parse_args()

    # Paths
    corpus_root = os.path.abspath(str(args.corpus))
    
    # WL iterations
    h = args.h
    h = int(h)
    
    print()
    print("#################### Loading data ######################")
    print()

    node_label, ad_list, class_name_to_graphs_count = load_graphs(corpus_root)

    start_c = time.clock()
    start_w = time.time()
    
    print("========================================================")
    print('======= Computing the WL Graph Kernel for h = {:2d} ======='.format(h))
    print("========================================================")
    print()
    # Apply WL graph kernel
    # Get a list of h kernel matrices: K
    # get a list of h features maps: phi
    K, phi, dic = WL_compute(ad_list, node_label, h)

    print("Saving kernel matrices and feature maps to disk ...")
    print()

    # Path to the output folder
    PATH_TO_GK = os.path.join(Config.OUTPUT_DIR, "graphs_kernels")
    PATH_TO_ARFF = os.path.join(Config.OUTPUT_DIR, "graphs_vectors")
    
    # If the output directory does not exist, then create it
    if not os.path.exists(PATH_TO_GK):
        os.makedirs(PATH_TO_GK)
    if not os.path.exists(PATH_TO_ARFF):
        os.makedirs(PATH_TO_ARFF)

    # For each iteration of WL
    for j in range(h + 1):

        # save kernel matrix
#         file_name = "%s/h%d_ker_mat" % (PATH_TO_GK, j)
        file_name = "{:s}/h{:d}_ker_mat".format(PATH_TO_GK, j)
#         file_name = os.path.join(PATH_TO_GK, "")
        np.save(file_name, K[j])
       
        names, vec_per_class, vec_label = convert_km_to_vec_per_class(K[j], class_name_to_graphs_count)
        
        arff_file = "{:s}/grap-gk-h{}.arff".format(PATH_TO_ARFF, j)
        save_labeled_vectors_to_arff(names, vec_per_class, vec_label, arff_file, j)

        # save feature map
#         file_name = "%s/h%d_feat_map" % (PATH_TO_GK, j)
        file_name = "{:s}/h{:d}_feat_map".format(PATH_TO_GK, j)
        np.save(file_name, phi[j])

        # # save lookup dictionary
#         file_name = "%s/h%d_lbl_dic" % (PATH_TO_GK, j)
        file_name = "{:s}/h{:d}_lbl_dic".format(PATH_TO_GK, j)
        np.save(file_name, dic[j])

    print("########################## Done ########################")
    print()
    
    end_c = time.clock()
    end_w = time.time()
    print()
    print("Time elapsed: clock:", end_c - start_c)
    print()
    print("Time elapsed: wall:", end_w - start_w)
    print()
    
    

