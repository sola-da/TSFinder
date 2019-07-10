'''

@author: Andrew Habib

adopted from script by: Elisabetta Ghisu

'''

import copy
import numpy as np

from scipy.sparse import lil_matrix, csr_matrix
from sklearn.metrics.pairwise import cosine_similarity


def WL_compute(ad_list, node_label, h):
    '''
    Params: list of adjacency lists of graphs, list of lists of nodes,
                        the iteration h of the WL kernel
    Retruns: lists of WL graph kernel matrices, features vectors, dictionaies per iteraion i of h
    '''

    # Total number of graphs in the dataset
    n = len(ad_list)

    # Total number of nodes in dataset: initialized as zero
    tot_nodes = 0

    # list of kernel matrices
    K_WL = [0] * (h + 1)
    K_WLnormalized_base = [0] * (h + 1)
    K_WLnormalized = [0] * (h + 1)

    # list of feature matrices
    phi_list = [0] * (h + 1)

    # list of lookup dictonaries
    dic_list = list({} for i in range(h + 1))

    # total number of nodes in the dataset
    for i in range(n):
        tot_nodes = tot_nodes + int(len(ad_list[i]))

    # each column of phi will be the explicit feature representation for the graph j
    # all elements are initially zeros
    phi = lil_matrix((tot_nodes, n), dtype=np.uint32)

    # labels will be used to store the new labels
    labels = [0] * n

    # label lookup is a dictionary which will contain the mapping
    # from multiset labels (strings) to short labels (integers)
    label_lookup = {}

    # counter to create possibly new labels in the update step
    label_counter = 0

    # Note: here we are just renaming the node labels from 0,..,num_labels
    # for each graph
    for i in range(n):
        # copy the original labels
        l_aux = np.copy(node_label[i])

        # will be used to store the new labels
        labels[i] = np.zeros(len(l_aux), dtype=np.int32)

        # for each label in graph
        for j in range(len(l_aux)):
            l_aux_str = str(l_aux[j])

            # If the string do not already exist
            # then create a new short label
            if not l_aux_str in label_lookup:
                label_lookup[l_aux_str] = label_counter
                labels[i][j] = label_counter
                label_counter += 1
            else:
                labels[i][j] = label_lookup[l_aux_str]

            # node histograph of the new labels
            phi[labels[i][j], i] += 1

    L = label_counter
    print("--> Number of original labels: ", L)
    print()

    #####################
    # --- Main code --- #
    #####################

    # Now we are starting with the first iteration of WL

    # features obtained from the original node (renamed) labels
    phi_list[0] = phi

    # Kernel matrix based on original features
    K_WL[0] = phi.transpose().dot(phi).toarray().astype(np.float32)
    
    # Use cosine similarity to normalize the WL kernel
    K_WLnormalized_base[0] = cosine_similarity(phi.T, dense_output=False).toarray().astype(np.float32)
    K_WLnormalized[0] = K_WLnormalized_base[0]
    
    # lookup dictionar from the original graphs
    dic_list[0] = label_lookup

    print("--> K original is computed")
    print()

    # Initialize iterations to 0
    it = 0

    # copy of the original labels: will stored the new labels
    new_labels = np.copy(labels)

    # until the number of iterations is less than h
    while it < h:

        # Initialize dictionary and counter
        # (same meaning as before)
        label_lookup = {}
        label_counter = 0

        # Initialize phi as a sparse matrix
        phi = lil_matrix((tot_nodes, n), dtype=np.int32)
        # convert it to array
        phi = phi.toarray()

        print("Iteration: ", it)
        print("--> Phi is computed")

        # for each graph in the dataset
        for i in range(n):

            # will store the multilabel string
            l_aux_long = np.copy(labels[i])

            # for each node in graph
            for v in range(len(ad_list[i])):

                # the new labels convert to tuple
                new_node_label = tuple([l_aux_long[v]])

                # form a multiset label of the node neighbors
                new_ad = np.zeros(len(ad_list[i][v]))
                for j in range(len(ad_list[i][v])):
                    new_ad[j] = ad_list[i][v][j]

                ad_aux = tuple([l_aux_long[int(j)] for j in new_ad])

                # long labels: original node plus sorted neughbors
                long_label = tuple(tuple(new_node_label) + 
                                   tuple(sorted(ad_aux)))

                # if the multiset label has not yet occurred , add
                # it to the lookup table and assign a number to it
                if not long_label in label_lookup:
                    label_lookup[long_label] = str(label_counter)
                    new_labels[i][v] = str(label_counter)
                    label_counter += 1

                # else assign it the already existing number
                else:
                    new_labels[i][v] = label_lookup[long_label]

            # count the node label frequencies
            aux = np.bincount(new_labels[i])
            phi[new_labels[i], i] += aux[new_labels[i]]

        L = label_counter
        print("--> Compressed labels: ", L)

        # create phi for iteration it+1
        phi_sparse = lil_matrix(phi)
        phi_list[it + 1] = phi_sparse

        print("--> Phi sparse saved")

        # create K at iteration it+1
        K_WL[it + 1] = K_WL[it] + phi_sparse.transpose().dot(phi_sparse).toarray().astype(np.float32)

        # Use cosine similarity to normalize the WL kernel
        K_WLnormalized_base[it + 1] = cosine_similarity(phi_sparse.T, dense_output=False).toarray().astype(np.float32)
        for k in K_WLnormalized_base:
            K_WLnormalized[it + 1] += k
        K_WLnormalized[it + 1] = K_WLnormalized[it + 1] / (it + 2)

        # # lookup dictionary
        dic_list[it + 1] = label_lookup

        print("--> K is computed")
        print()

        # Initialize labels for the next iteration as the new just computed
        labels = copy.deepcopy(new_labels)

        # increment the iteration
        it = it + 1

    return K_WLnormalized, phi_list, dic_list


def WL_compute_efficient(tot_nodes_m, h, feat_list, dic_list, ad_list, node_label):
    '''
    Params: list of adjacency lists of graphs, list of lists of nodes, the iteration h of the WL kernel
    Retruns: lists of WL graph kernel matrices, features vectors, dictionaies per iteraion i of h
    '''

    # Total number of graphs in the dataset
    n = len(ad_list)

    # Total number of nodes in dataset: initialized as zero
    tot_nodes = tot_nodes_m

    # each column of phi will be the explicit feature representation for the graph j
    phi = lil_matrix((tot_nodes, n), dtype=np.uint32)

    # labels will be used to store the new labels

    labels = [0] * n

    # label lookup is a dictionary which will contain the mapping
    # from multiset labels (strings) to short labels (integers)

    label_lookup = dic_list[0]

    # counter to create possibly new labels in the update step

    label_counter = len(label_lookup)

    # Note: here we are just renaming the node labels from 0,..,num_labels
    # for each graph
    for i in range(n):
        # copy the original labels
        l_aux = np.copy(node_label[i])

        # will be used to store the new labels
        labels[i] = np.zeros(len(l_aux), dtype=np.int32)

        # for each label in graph
        for j in range(len(l_aux)):
            l_aux_str = str(l_aux[j])

            # If the string do not already exist
            # then create a new short label
            if not l_aux_str in label_lookup:
                label_lookup[l_aux_str] = label_counter
                labels[i][j] = label_counter
                label_counter += 1
            else:
                labels[i][j] = label_lookup[l_aux_str]

            # node histograph of the new labels
            phi[labels[i][j], i] += 1

# 	L = label_counter
# 	print 'Number of original labels %d' % L

    #####################
    # --- Main code --- #
    #####################

    # Now we are starting with the first iteration of WL

    # features obtained from the original node (renamed) labels
    # print phi.transpose().shape
    # print feat_list[0].shape
    K = phi.transpose().dot(feat_list[0]).astype(np.float32)
    K_normalized = cosine_similarity(phi.T, lil_matrix(feat_list[0]).T, dense_output=False).toarray().astype(np.float32)

# 	print "K original is computed"

    # Initialize iterations to 0
    it = 0

    # copy of the original labels: will stored the new labels
    new_labels = np.copy(labels)

    # until the number of iterations is less than h
    while it < h:

        # Initialize dictionary and counter
        # (same meaning as before)
        label_lookup = dic_list[it + 1]
        label_counter = len(label_lookup)

        # Initialize phi as a sparse matrix
        phi = lil_matrix((tot_nodes, n), dtype=np.int32)
        # convert it to array
        phi = phi.toarray()

# 		print "Iteration %d: phi is computed" % it

        # for each graph in the dataset
        for i in range(n):

            # will store the multilabel string
            l_aux_long = np.copy(labels[i])

            # for each node in graph
            for v in range(len(ad_list[i])):

                # the new labels convert to tuple
                new_node_label = tuple([l_aux_long[v]])

                # form a multiset label of the node neighbors
                new_ad = np.zeros(len(ad_list[i][v]))
                for j in range(len(ad_list[i][v])):
                    new_ad[j] = ad_list[i][v][j]

                ad_aux = tuple([l_aux_long[int(j)] for j in new_ad])

                # long labels: original node plus sorted neughbors
                long_label = tuple(tuple(new_node_label) + 
                                   tuple(sorted(ad_aux)))

                # if the multiset label has not yet occurred , add
                # it to the lookup table and assign a number to it
                if not long_label in label_lookup:
                    label_lookup[long_label] = str(label_counter)
                    new_labels[i][v] = str(label_counter)
                    label_counter += 1

                # else assign it the already existing number
                else:
                    new_labels[i][v] = label_lookup[long_label]

            # count the node label frequencies
            aux = np.bincount(new_labels[i])
            phi[new_labels[i], i] += aux[new_labels[i]]

# 		L = label_counter
# 		print 'Number of compressed labels %d' % L

        # create phi for iteration it+1
        phi_sparse = lil_matrix(phi)

# 		print "Iteration %d: phi sparse saved" % it

        # create K at iteration it+1
        K = K + phi_sparse.transpose().dot(feat_list[it + 1]).astype(np.float32)
        K_normalized = K_normalized + \
            cosine_similarity(phi_sparse.T, lil_matrix(feat_list[it + 1]).T, dense_output=False).toarray().astype(np.float32)
# 		print "Iteration %d: K is computed" % it

        # Initialize labels for the next iteration as the new just computed
        labels = copy.deepcopy(new_labels)

        # increment the iteration
        it = it + 1

    return K_normalized / (it + 1)
