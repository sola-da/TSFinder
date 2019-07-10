# TSFinder: machine learning based framework to infer thread-safety of Java classes.

This work is detailed and published in our ASE 2018 paper:

    Is This Class Thread-Safe? Inferring Documentation using Graph-Based Learning.
    Andrew Habib and Michael Pradel.
    In Proceedings of the 33rd ACM/IEEE International Conference on Automated Software Engineering (ASE),
    pp. 41-52. ACM, 2018.
    
This repository contains the source code of TSFinder along with data scripts required to reproduce the results in the *ASE2018* paper above.


## Requirement to run

- `JDK`
- `bash`
- `python3`
- `python-igraph` (`pip3 install python-igraph`)


## Reproducing ASE2018 results

Reproducing our ASE2018 results is almost fully automated. 
Run the shell script [`scripts/runASEexperiments.sh`](scripts/runASEexperiments.sh) and then follow on-screen instructions.


## Repository structure

### Source code and 3rd party libs:
- [`src`](src) has the java files for running soot to extract baseline data as well as to build field-focused graphs.
- [`python/graph-kernel`](python/graph-kernel/) has python scripts to compute the WL-graph kernel and produce a single vector per class.
- [`lib`](lib) has third-party java libs such as soot, gs-core, and weka.
	
### Data:
- [`benchmark`](benchmark) has the lists of thread-safe and thread-unsafe classes used in the ASE2019 paper. It also has the jdk_rt.jar
- [`benchmark/ase2018`](benchmark/ase2018) has the weka experiments configuration file and the results file from weka will be saved there.
- `output` is created after running the experiment script or explicitly calling the individual components of TSFinder. It includes the vectors generated for the baseline, the generated field-focused graphs, computed graph-kernels and associated meta-data; and the vectors representing classes based on the summary of the graph-kernel.  


## Using TSFinderr

### I) Supervised training 
1- Specify the path to the list of thread-safe and thread-unsafe classes and the path to the target classes you want to analyze in:
[`src/tsfinder/Config.java`](src/tsfinder/Config.java) and [`python/graph-kernel/Config.py`](python/graph-kernel/Config.py)

2- To build field-focused graphs, first compile the java sources using ``javac -cp lib/sootclasses-trunk-jar-with-dependencies.jar:lib/gs-core-1.3.jar:bin/ -d bin/ `find src/ -name "*.java"```

3- Now, run the following command to generate field-focused graphs `java -cp bin/:lib/sootclasses-trunk-jar-with-dependencies.jar:lib/jdk-8u152-linux-x64/jdk1.8.0_152/:lib/gs-core-1.3.jar tsfinder.graphs.GraphsBuilder`

4- To compute the WL graph-kernel of field-focused graphs and obtain a vector per-class, run `python3 python/graph-kernel/WLCompute.py --corpus output/graphs_raw/ --h 7` 

5- Run weka through `java -jar lib/weka.jar` and load the classes vectors file from `output/graphs_vectors`.

### II) Classifying a new class
	coming soon ...