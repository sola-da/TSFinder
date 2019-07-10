'''

@author: Andrew Habib

adopted from script by: Elisabetta Ghisu

'''

import os
import sys
import numpy as np

from igraph import Graph 
from collections import Counter, OrderedDict


class OrderedCounter(Counter, OrderedDict):
    'Counter that remembers the order elements are first encountered'

    def __repr__(self):
#         return '%s(%r)' % (self.__class__.__name__, OrderedDict(self))
        return ":s(:r)".format(self.__class__.__name__, OrderedDict(self))

    def __reduce__(self):
        return self.__class__, (OrderedDict(self),)

    
def get_adj_list(G):
    '''
    Params: list of graphs
    Returns: a list where each element is itself the adjacency list of the corresponding graph
    The adjacency list of a graph has the following format:
    A list where each element is a list containing the id of adjacent nodes
    '''

    ad_l = []
    for g in G:
        ad_l.append(g.get_adjlist())
    return ad_l


def get_adj_mat(G):
    '''
    Params: list of graphs
    Returns: a list where each element is the adjacency matrix of the graph
    The adjancency matrix is in iGraph format
    '''

    ad_m = []
    for g in G:
        ad_m.append(g.get_adjacency())
    return ad_m


def get_node_labels(G):
    '''
    Params: list of graphs
    Returns: list where each element contains the nodes labels for a graph
    '''

    att_name = 'label'
    node_l = []
    for g in G:
        node_l.append(g.vs[att_name])
    return node_l


def rename_nodes(node_label):
    '''
    Params: List of lists of nodes per graph
    Returns: Unique labels count and list of unique labels
    '''

    # number of graphs in the dataset
    n = len(node_label)

    # labels will store the new labels
    labels = [0] * n

    # dictionary containing the map from the old to the new labels
    label_lookup = {}

    # counter of unique labels
    label_counter = 0

    # for each graph in dataset
    for i in range(n):

        # number of nodes in graph[i]
        num_nodes = len(node_label[i])

        # will be used to store the new labels
        labels[i] = np.zeros(num_nodes, dtype=np.uint64)  # positive integers

        # for each node in the graph
        for j in range(num_nodes):

            # the node label to a string
            l_node_str = str(np.copy(node_label[i][j]))

            # if the string has not been observed yet
            # the corresponding node is assigned a new label
            # otherwise it will be named with the same label
            # already assigned to an identical string

            if not l_node_str in label_lookup:
                label_lookup[l_node_str] = label_counter
                labels[i][j] = label_counter
                label_counter += 1
            else:
                labels[i][j] = label_lookup[l_node_str]

    # total number of labels in the dataset
    L = label_counter
    print("Number of original labels: ", L)

    return L, labels


def load_labels_and_adj(class_list_file, graphs_dir):
    '''
    Params: path to file with list of class names, and directory of graphs
    Returns: list of lists of node labels per graph, and list of adjacency lists of graphs
    '''

    # load a list of names to graphml files
    f_graphs = load_file_list(class_list_file)

    # sample size
    n = len(f_graphs)

    # create a list of paths to the files
    f_graphs_path = []

    # for each graph in dataset
    for i in range(n):

        # index the graph
        graph_name = f_graphs[i]

        # path to the data folder
        path = "%s/%s" % (graphs_dir, graph_name)
        f_graphs_path.append(path)

    # Load the graphs in graphml format
    # G is a list of graphml graph
    G = load_graphml(f_graphs_path)

    # get adjacency list and matrix for all the graphs in G
    ad_list = get_adj_list(G)
#     ad_mat = get_adj_mat(G)

    # get a list containing lists of node labels
    node_label = get_node_labels(G)

    return node_label, ad_list


def load_graphs(corpus):
         
    G = []
    classes = []
    for c in os.listdir(corpus):
        for g in os.listdir(os.path.join(corpus, c)):
            classes.append(c)
            graph = Graph.Read_GraphML(os.path.join(os.path.join(corpus, c), g))
            G.append(graph)

    ad_list = get_adj_list(G)
    node_label = get_node_labels(G)
    return node_label, ad_list, OrderedCounter(classes)


def get_graphs_stats(path):
    '''
    Params: path to corpus of GraphML files
    Returns: total count of nodes and edges in all graphs
    '''

    tot_nodes = 0
    tot_edges = 0
    for g in os.listdir(path):
        G = Graph.Read_GraphML(os.path.join(path, g))
        tot_nodes += G.vcount()
        tot_edges += G.ecount()
    return tot_nodes, tot_edges
