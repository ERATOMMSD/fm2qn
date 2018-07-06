package fmtoqn;

import experiments.Utils;
import fmtoqn.products.OptProdExp;

public class OptProdExpTest {

	public static void main(String[] args) throws Exception {
		// optimalProducts("examplesMaxMinProdSemantics/smallEx.afm", "cost", true);
		// optimalProducts("examplesMaxMinProdSemantics/smallEx.afm", "cost", false);
		// optimalProducts("examplesMaxMinProdSemantics/smallExQNsem.afm", "QNsem",
		// true);
		// optimalProducts("examplesMaxMinProdSemantics/smallExQNsem.afm", "QNsem",
		// false);

		OptProdExp.optimalProducts("generatedQnSem/genModel_5_5_5_10_20_1.afm", Utils.QN_PAR_SEM_ATTRIBUTE, true);
		OptProdExp.optimalProducts("generatedQnSem/genModel_5_5_5_10_20_1.afm", Utils.QN_PAR_SEM_ATTRIBUTE, false);
		//optimalProducts("examplesMaxMinProdSemantics/smallExQNsem2.afm", Utils.QN_SEM_ATTRIBUTE, false);
		//optimalProducts("examplesMaxMinProdSemantics/smallExQNsem2.afm", Utils.QN_SEM_ATTRIBUTE, true);

		 //optimalProducts("fm-samples/atts/James/James.afm", "cost", true);
		 //optimalProducts("fm-samples/atts/James/James.afm", "cost", false);
		// optimalProductsGalindo("fm-samples/atts/James/James.afm", "cost");
		// optimalProducts("fm-samples/atts/Modules/modules.afm", "version", true);
		// optimalProducts("fm-samples/atts/Modules/modules.afm", "version", false);
		// optimalProductsGalindo("fm-samples/atts/Modules/modules.afm", "version");
		// optimalProducts("fm-samples/atts/Player/player.afm", "cost", true);
		// optimalProducts("fm-samples/atts/Player/player.afm", "cost", false);
		// optimalProducts("fm-samples/atts/services/servicesOnlyPrice.afm", "price",
		// true);
		// optimalProducts("examplesFMtoQnetworks/exampleOptimal.afm", "cost", true);
		// System.out.println();
		// optimalProducts("examplesFMtoQnetworks/exampleOptimal.afm", "cost", false);
		// optimalProducts("fm-samples/atts/services/servicesOnlyPrice.afm", "price",
		// false);
		// optimalProducts("ourModels/ourModel2.afm", "cost", true);
		// optimalProducts("ourModels/ourModel2.afm", "cost", false);
	}
}
