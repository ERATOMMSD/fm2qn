# FM2QN: Feature Models to Queueing Networks

## Benchmarks
* A set of 6003 feature models have been generated with the [BeTTy](http://www.isa.us.es/fama/?BeTTy_Framework) tool. The generated benchmark set of synthetic models can be dowloaded from [here](https://github.com/ERATOMMSD/fm2qn/raw/master/fm2qn.exps/benchmarks/syntheticModels.zip).
* Other 931 feature models have been taken from the [SPLOT repository](http://www.splot-research.org/) and modified with BeTTy in order to add attributes and constraints between attributes. The benchmark set can be dowloaded from [here](https://github.com/ERATOMMSD/fm2qn/raw/master/fm2qn.exps/benchmarks/SPLOTmodels.zip).

## Results
The benchmark feature models have been translated assuming both a parallel and sequential semantics:
* Queueing networks with parallel semantics: [Synthetic models](https://github.com/ERATOMMSD/fm2qn/raw/master/fm2qn.exps/generatedQNs_parSem/QNs_parSem_syntheticModels.zip) - [SPLOT models](https://github.com/ERATOMMSD/fm2qn/raw/master/fm2qn.exps/generatedQNs_parSem/QNs_parSem_SPLOTmodels.zip)
* Queueing networks with sequential semantics: : [Synthetic models](https://github.com/ERATOMMSD/fm2qn/raw/master/fm2qn.exps/generatedQNs_seqSem/QNs_seqSem_syntheticModels.zip) - [SPLOT models](https://github.com/ERATOMMSD/fm2qn/raw/master/fm2qn.exps/generatedQNs_seqSem/QNs_seqSem_SPLOTmodels.zip)

## Tool
You can try the tool using the [FM2QN jar file](https://github.com/ERATOMMSD/fm2qn/raw/master/fm2qn/FM2QN.jar).

The tool accepts the following arguments:
```
java -jar FM2QN.jar [options...] fileName
 -attName VAL : attribute name
 -max         : generate network for maximum product (default: false)
 -min         : generate network for minumum product (default: false)
 -seq         : use sequential semantics (default: false)
 -slice       : slice the network (default: false)

  Example: java -jar FM2QN.jar -attName VAL -max -min -seq
```

The products are generated with the FAMA tool (version 1.1.2. Namely, FAMA Core v1.1.1, FaMa Feature Model v0.9.1, FaMa Attributed Feature Model v1.0.4, and ChocoReasoner v1.1.1)

The generated queueing networks can be simulated with the [JMT tool](http://jmt.sourceforge.net/)


## Contacts
For any question about the project, please contact the authors:<br/>
[Paolo Arcaini](http://group-mmm.org/~arcaini/), National Institute of Informatics, Tokyo, Japan<br/>
[Omar Inverso](https://www.gssi.it/people/professors/lectures-computer-science/item/1018-inverso-omar), Gran Sasso Science Institute, L'Aquila, Italy<br/>
[Catia Trubiani](http://cs.gssi.infn.it/catia.trubiani/), Gran Sasso Science Institute, L'Aquila, Italy
