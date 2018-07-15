# FM2QN: Feature models to queueing networks

## Benchmarks
A set of 6003 feature models have been generated with BeTTy. It can be dowloaded from [here](https://github.com/ERATOMMSD/fm2qn/raw/master/fm2qn.exps/benchmarks.zip)

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

  Example: java FM2QN -attName VAL -max -min -seq
