package fmtoqn.cli;

import static org.kohsuke.args4j.ExampleMode.ALL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import es.us.isa.ChocoReasoner.attributed.AttributedProduct;
import fmtoqn.QueueNetwork;
import fmtoqn.builder.BuildQNfromFM;
import fmtoqn.builder.SimplifyQueueNetworkFromProduct;
import fmtoqn.products.OptProdExp;

public class FM2QNcli {
	@Option(name = "-seq", usage = "use sequential semantics")
	private boolean seqSem;

	@Option(name = "-max", usage = "generate network for maximum product")
	private boolean maxProd;

	@Option(name = "-min", usage = "generate network for minumum product")
	private boolean minProd;

	@Option(name = "-slice", usage = "slice the network")
	private boolean slice;

	@Option(name = "-attName", usage = "attribute name")
	private String attributeName;

	@Argument
	private String fileName;

	public static void main(String[] args) throws Exception {
		new FM2QNcli().doMain(args);
	}

	public void doMain(String[] args) throws Exception {
		CmdLineParser parser = new CmdLineParser(this);
		parser.setUsageWidth(80);
		try {
			parser.parseArgument(args);
			if (fileName == null) {
				throw new CmdLineException(parser, "No feature model is given");
			}
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("java -jar FM2QN.jar [options...] fileName");
			parser.printUsage(System.err);
			System.err.println();

			System.err.println("  Example: java -jar FM2QN.jar " + parser.printExample(ALL));
			return;
		}

		BuildQNfromFM.parSemantics = !seqSem;

		if (!new File(fileName).exists()) {
			throw new CmdLineException("File " + fileName + " does not exist!");
		}
		
		if(!fileName.endsWith(".afm")) {
			throw new CmdLineException("Unsopported format of " + fileName + "\nFile extension must be afm");
		}

		BuildQNfromFM b = new BuildQNfromFM(fileName);
		b.buildQN();
		QueueNetwork qn = b.getQn();
		qn.printConstraints(new PrintStream(new FileOutputStream(new File(fileName.substring(0, fileName.length() - 4) + "_constr.txt"))));
		String semantics = (BuildQNfromFM.parSemantics ? "_parSem" : "_seqSem");
		SimplifyQueueNetworkFromProduct.logger.setLevel(java.util.logging.Level.SEVERE);
		if (maxProd) {
			if (attributeName == null) {
				throw new CmdLineException("You need to specify an attribute name in order to generate a product!");
			}
			AttributedProduct maxProd = OptProdExp.getProduct(attributeName, false, b.fm);
			qn.setAttributeName(attributeName);
			qn.setProductInQN(maxProd);
			if(slice) {
				QueueNetwork newQn = SimplifyQueueNetworkFromProduct.simplify(b.getQn(), maxProd);
				newQn.setProductInQN(maxProd);
				BuildQNfromFM.saveQn(newQn, b.getQnName() + semantics + "_maxProd_sliced.jsimg");
			}
			else {
				b.saveQn(b.getQnName() + semantics + "_maxProd.jsimg");
			}
			System.out.println("Max product: " + maxProd);
		}
		if (minProd) {
			if (attributeName == null) {
				throw new CmdLineException("You need to specify an attribute name in order to generate a product!");
			}
			AttributedProduct minProd = OptProdExp.getProduct(attributeName, true, b.fm);
			qn.setAttributeName(attributeName);
			qn.setProductInQN(minProd);
			if(slice) {
				QueueNetwork newQn = SimplifyQueueNetworkFromProduct.simplify(qn, minProd);
				newQn.setProductInQN(minProd);
				BuildQNfromFM.saveQn(newQn, b.getQnName() + semantics + "_minProd_sliced.jsimg");
			}
			else {
				b.saveQn(b.getQnName() + semantics + "_minProd.jsimg");
			}
			System.out.println("Min product: " + minProd);
		}
		if (!minProd && !maxProd) {
			b.saveQn(b.getQnName() + ".jsimg");
		}
	}
}
