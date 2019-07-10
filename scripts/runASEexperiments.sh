#!/bin/bash

# Author: Andrew Habib
# Created on: July 8th, 2018

echo
echo "Welcome to TSFinder"

echo
echo "Compiling resources"
if [ -d bin/ ]; then rm -rf bin/; fi
mkdir bin/
javac -cp lib/sootclasses-trunk-jar-with-dependencies.jar:lib/gs-core-1.3.jar:bin/ -d bin/ `find src/ -name "*.java"`

echo
echo "Running ASE2018 experiments"

echo
echo "(1) Running baseline using simple class features."
java -cp bin/:lib/sootclasses-trunk-jar-with-dependencies.jar:lib/gs-core-1.3.jar tsfinder.stats.StatsBuilder

echo
echo "(2) Running TSFinder using graph kernels"
echo

echo "(2.1) Extracting Field-focused graphs from classes."
java -cp bin/:lib/sootclasses-trunk-jar-with-dependencies.jar:lib/gs-core-1.3.jar tsfinder.graphs.GraphsBuilder

echo
echo "(2.1) Extracting Field-focused graphs from classes."
python3 python/graph-kernel/WLCompute.py --corpus output/graphs_raw/ --h 7

echo
echo "(3) Training models used in the ASE18 paper using weka CLI"
echo
java -cp lib/weka.jar weka.experiment.Experiment -l benchmark/ase2018/ase2018.exp -r -verbose

echo 
echo "Now you can use weka Experimenter to analyze results."
echo "In weka GUI, click on Experimenter, then choose Analyse."
echo "Click on File, and load the benchmark/ase2018/ase2018-detailed-results.arff file."
java -jar lib/weka.jar

