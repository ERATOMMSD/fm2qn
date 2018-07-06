package fmtoqn;

import es.us.isa.ChocoReasoner.attributed.AttributedProduct;
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.FAMAAttributedFeatureModel;
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.fileformats.AttributedReader;
import experiments.Utils;
import fmtoqn.builder.BuildQNfromFM;
import fmtoqn.builder.SimplifyQueueNetwork;
import fmtoqn.products.OptProdExp;

public class BuildQNfromFMTest {

	public static void main(String[] args) throws Exception {
		// BuildQNfromFM b = new BuildQNfromFM("fm-samples/atts/James/James.afm");
		// BuildQNfromFM b = new BuildQNfromFM("examplesFMtoQnetworks/doubleOR.afm");
		// BuildQNfromFM b = new
		// BuildQNfromFM("examplesFMtoQnetworks/andTwoOptionals.afm");
		// BuildQNfromFM b = new
		// BuildQNfromFM("fm-samples/atts/services/servicesOnlyPrice.afm");
		// BuildQNfromFM b = new BuildQNfromFM("fm-samples/atts/Player/Player.afm");
		// BuildQNfromFM b = new BuildQNfromFM("fm-samples/mandatory.afm");
		// BuildQNfromFM b = new BuildQNfromFM("ourModels/childrenAndOptMand.afm");
		// BuildQNfromFM b = new
		// BuildQNfromFM("./ourModels/modelGeneratedWithBetty.afm");
		// BuildQNfromFM b = new BuildQNfromFM("experiments/exampleForPaper.afm");

		String modelName = "genModel_40_5_5_5_15_1.afm";
		// String modelName = "genModel_7_15_5_10_20_0.afm";

		BuildQNfromFM b = new BuildQNfromFM(Utils.ORIG_MODELS_LESS_CONSTR + modelName);
		b.buildQN();
		AttributedReader reader = new AttributedReader();
		FAMAAttributedFeatureModel fmForProd = (FAMAAttributedFeatureModel) reader
				.parseFile(Utils.MODELS_PROD_GEN_LESS_CONSTR + modelName);

		AttributedProduct firstMax = OptProdExp.getProduct(Utils.QN_PAR_SEM_ATTRIBUTE, false, fmForProd);
		if (firstMax != null) {
			firstMax.setMainAttribute(Utils.ATT_NAME);
			firstMax.filterCosts(1000);
			System.err.println(firstMax);
		}

		AttributedProduct firstMin = OptProdExp.getProduct(Utils.QN_PAR_SEM_ATTRIBUTE, true, fmForProd);
		if (firstMin != null) {
			firstMin.setMainAttribute(Utils.ATT_NAME);
			firstMin.filterCosts(1000);
			System.err.println(firstMin);
		}

		QueueNetwork newQn = null;
		if (firstMax != null) {
			b.getQn().setAttributeName(Utils.ATT_NAME);
			b.getQn().setProductInQN(firstMax);
			b.saveQn(b.getQnName() + "_maxProdTotal.jsimg");
			b.getQn().runSimulation();
			newQn = SimplifyQueueNetwork.simplify(b.getQn(), firstMax);
			BuildQNfromFM.saveQn(newQn, b.getQnName() + "_maxProdSimpl.jsimg");
			newQn.runSimulation();
		} else {
			System.out.println("Error in generation of max model");
		}

		if (firstMin != null) {
			b.getQn().setAttributeName(Utils.ATT_NAME);
			b.getQn().setProductInQN(firstMin);
			b.saveQn(b.getQnName() + "_minProdTotal.jsimg");
			b.getQn().runSimulation();
			newQn = SimplifyQueueNetwork.simplify(b.getQn(), firstMin);
			BuildQNfromFM.saveQn(newQn, b.getQnName() + "_minProdSimpl.jsimg");
			newQn.runSimulation();
		} else {
			System.out.println("Error in generation of min model");
		}
	}
}
