package fmtoqn.cli;

import static org.kohsuke.args4j.ExampleMode.ALL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import es.us.isa.ChocoReasoner.attributed.AttributedProduct;
import fmtoqn.QueueNetwork;
import fmtoqn.builder.BuildQNfromFM;
import fmtoqn.builder.SimplifyQueueNetwork;
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

		if (!fileName.endsWith(".afm")) {
			throw new CmdLineException("Unsopported format of " + fileName + "\nFile extension must be afm");
		}

		BuildQNfromFM b = new BuildQNfromFM(fileName);
		b.buildQN();
		String semantics = (BuildQNfromFM.parSemantics ? "_parSem" : "_seqSem");
		QueueNetwork qn = b.getQn();
		String constrFileName = fileName.substring(0, fileName.length() - 4) + semantics + "_constr.txt";
		qn.printConstraints(new PrintStream(new FileOutputStream(new File(constrFileName))));
		SimplifyQueueNetwork.logger.setLevel(java.util.logging.Level.SEVERE);
		if (maxProd) {
			instantiate(b, qn, semantics, false);
		}
		if (minProd) {
			instantiate(b, qn, semantics, true);
		}
		if (!minProd && !maxProd) {
			b.saveQn(b.getQnName() + ".jsimg");
		}
	}

	private void instantiate(BuildQNfromFM b, QueueNetwork qn, String semantics, boolean minProd)
			throws CmdLineException, IOException {
		if (attributeName == null) {
			throw new CmdLineException("You need to specify an attribute name in order to generate a product!");
		}
		AttributedProduct prod = OptProdExp.getProduct(attributeName, minProd, b.fm);
		qn.setAttributeName(attributeName);
		qn.setProductInQN(prod);
		String minMax = minProd ? "Min" : "Max";
		if (slice) {
			QueueNetwork newQn = SimplifyQueueNetwork.simplify(b.getQn(), prod);
			newQn.setAttributeName(attributeName);
			newQn.setProductInQN(prod);
			BuildQNfromFM.saveQn(newQn, b.getQnName() + semantics + "_" + minMax + "Prod_sliced.jsimg");
		} else {
			b.saveQn(b.getQnName() + semantics + "_" + minMax + "Prod.jsimg");
		}
		System.out.println(minMax + " product: " + prod);
	}
}
