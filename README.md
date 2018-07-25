# FM2QN: Feature models to queueing networks

## Benchmarks
A set of 6003 feature models have been generated with the [BeTTy](http://www.isa.us.es/fama/?BeTTy_Framework) tool. The generated benchmark set can be dowloaded from [here](https://github.com/ERATOMMSD/fm2qn/raw/master/fm2qn.exps/benchmarks.zip)

## Results
The benchmark feature models have been translated assuming both a parallel and sequential semantics:
* [Queueing networks with parallel semantics](https://github.com/ERATOMMSD/fm2qn/raw/master/fm2qn.exps/generatedQNs_parSem.zip)
* [Queueing networks with sequential semantics](https://github.com/ERATOMMSD/fm2qn/raw/master/fm2qn.exps/generatedQNs_seqSem.zip)

## Tool
You can try the tool using the [FM2QN jar file](https://github.com/ERATOMMSD/fm2qn/raw/master/fm2qn/FM2QN.jar).

The tool accepts the following arguments:
```
java -jar FM2QN.jar [options...] fileName
 -attName VAL : attribute name
 -max         : generate network for maximum product (default: false)
 -min         : generate network for minumum product (default: false)
 -seq         : use sequential semantics (default: false)

  Example: java -jar FM2QN.jar -attName VAL -max -min -seq
```

The generated queueing networks can be simulated with the [JMT tool](http://jmt.sourceforge.net/)


## Contacts
For any question about the project, please contact the authors:
[Paolo Arcaini](http://group-mmm.org/~arcaini/), National Institute of Informatics
[Catia Trubiani](http://cs.gssi.infn.it/catia.trubiani/), Gran Sasso Science Institute
