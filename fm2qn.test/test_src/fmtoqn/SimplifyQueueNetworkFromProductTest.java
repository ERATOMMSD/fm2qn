package fmtoqn;

import es.us.isa.ChocoReasoner.attributed.AttributedProduct;
import fmtoqn.builder.BuildQNfromFM;
import fmtoqn.builder.SimplifyQueueNetworkFromProduct;
import fmtoqn.products.OptProdExp;

public class SimplifyQueueNetworkFromProductTest {

	public static void main(String[] args) throws Exception {
		BuildQNfromFM b = new BuildQNfromFM("./generatedWithBetty/genModel_15_20_5_5_20_0.afm");
		b.buildQN();

		AttributedProduct firstMin = OptProdExp.getProduct("", true, b.fm);
		firstMin.filterCosts(1000);
		b.getQn().setProductInQN(firstMin);
		b.saveQn(b.getQnName() + "_minProdTotal.jsimg");
		QueueNetwork newQn = SimplifyQueueNetworkFromProduct.simplify(b.getQn(), firstMin);
		BuildQNfromFM.saveQn(newQn, b.getQnName() + "_minProdSimpl.jsimg");
	}
}
