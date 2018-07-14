# FM2QN: Feature models to queueing networks

[Benchmarks](https://github.com/ERATOMMSD/fm2qn/raw/master/fm2qn.exps/benchmarks.zip)

You can try the tool using the [FM2QN jar file](https://github.com/ERATOMMSD/fm2qn/raw/master/fm2qn/FM2QN.jar).

The tool accepts the following arguments:
```
java -jar FM2QN.jar [options...] fileName
 -attName VAL : attribute name
 -max         : generate network for maximum product (default: false)
 -min         : generate network for minumum product (default: false)
 -seq         : use sequential semantics (default: false)

  Example: java FM2QN -attName VAL -max -min -seq
