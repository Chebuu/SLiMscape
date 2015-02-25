#SLiMscape
SLiMScape, a <a href="http://www.cytoscape.org/">Cytoscape</a> plugin, is a platform for performing short linear motif analyses of protein interaction networks by integrating motif discovery and search tools in a network visualization environment. This aims to aid in the discovery of novel short linear motifs, as well as visualisation of the distribution of known motifs.

##Input
The minimum input for SLiMScape is an interaction network which contains a Uniprot attribute for each node OR a run ID from the UNSW servers. At the moment, the name of the node needs to be its Uniprot ID for the server to know what to do with it. 

Once a search has been completed, the nodes that contain SLiMs will change from the default blue circles to red diamonds. The rest of the nodes will remain as blue circles. 

##Results
Results for SLiMProb and SLiMFinder are displayed identically, in two panels. 
**Main Results Panel** contains:
- N\_Occ
- E\_Occ
- p\_Occ
- pUnd\_Occ
- N\_Seq
- E\_Seq
- p\_Seq
**OCC Panel** contains:
- Motif: The motif found in the sequence.
- Seq: The name of the sequence.
- Start\_Pos: The start position of the sequence.
- End\_Pos: The end position of the sequence.

Further results can be obtained on the internet, via "http://rest.slimsuite.unsw.edu.au/retrieve&jobid={jobid produced when the run is performed}"

##SLiMProb
SLiMProb searches the protein sequences of the selected nodes for occurrences of a specified regular expressions; useful for locating new instances of a motif found using SLiMFinder. 

SLiMProb has one input; a SLiM to be searched for in the nodes selected on the graph. To run SLiMProb you must provide a regular expressions in the motifs box and select one or more nodes. Alternatively, if you know the run ID of a previous search, you can instead input that to the Run ID textbox.

If the motif is found, the colour and shape of the target node will change.

###SLiMProb Options
**Masking**
- Disorder Masking is used to mask residues which have an IUPred disorder score of less than 0.3
- Conservation Masking is used to mask residues which have a relative local conservation score of less than X.
**SLiMChance**
- Probability Cutoff is the cutoff for returned motifs.
**Custom Parameters**
- Custom parameters can be used to add other command line arguments which can be found <a href="http://docs.slimsuite.unsw.edu.au/software/slimsuite/readme/tools/slimsearch.html">here</a>.

##SLiMFinder
SLiMFinder aims to discover new motifs in the selected protein interaction network by searching for statistically overrepresented sequences withoin a set of proteins.

It has no mandatory inputs other than a selection of graph nodes or a run ID. If a motif is found in a node, the node will change colour and shape. The specific motif is presented in an output table, along with the Uniprot ID and other data. 

###SLiMFinder Options
**Masking**
- Disorder Masking is used to mask residues which have an IUPred disorder score of less than 0.3
- Conservation Masking is used to mask residues which have a relative local conservation score of less than X.
- Feature Masking is used to mask residues which occur in features such as domains or transmembrane regions.
**SLiMChance**
- Probability Cutoff is the cutoff for returned motifs.
**Miscellaneous Options**
- Walltime is the maximum runtime of a single run.
- Custom parameters is used to add other command line arguments which can be found <a href="http://docs.slimsuite.unsw.edu.au/software/slimsuite/readme/tools/slimfinder.html">here</a>.
