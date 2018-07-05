package fmtoqn.products;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;

import es.us.isa.ChocoReasoner.attributed.AttributedProduct;
import es.us.isa.ChocoReasoner.attributed.ChocoReasoner;
import es.us.isa.ChocoReasoner.attributed.questions.ChocoOneProductQuestion;
import es.us.isa.ChocoReasoner.attributed.questions.ChocoOptimalProducts;
import es.us.isa.ChocoReasoner.attributed.questions.ChocoProductsQuestion;
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.FAMAAttributedFeatureModel;
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.Relation;
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.fileformats.AttributedReader;
import es.us.isa.FAMA.models.featureModel.Cardinality;
import es.us.isa.FAMA.models.featureModel.Constraint;
import es.us.isa.FAMA.models.featureModel.GenericRelation;
import es.us.isa.FAMA.models.featureModel.Product;

public class OptProdExp {

	public static void optimalProducts(String model, String attribute, boolean minimize) throws Exception {
		AttributedReader reader = new AttributedReader();
		FAMAAttributedFeatureModel fm = (FAMAAttributedFeatureModel) reader.parseFile(model);
		optimalProducts(attribute, minimize, fm);
	}

	public static Collection<AttributedProduct> getOptimalProducts(String attribute, boolean minimize,
			FAMAAttributedFeatureModel fm) {
		ChocoReasoner reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);

		ChocoOptimalProducts q = new ChocoOptimalProducts(minimize);
		q.setAttributeName(attribute);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(out);
		PrintStream err = System.err;
		System.setErr(printStream);
		String successRes = null;
		try {
			reasoner.ask(q);
			successRes = out.toString();
		}
		catch(Error|Exception e ) {
			successRes = "Exception ";
		}
		System.setErr(err);
		if (successRes.contains("Exception ")) {
			return Collections.EMPTY_LIST;
		} else {
			return q.getAttributedProducts();
		}
	}

	public static AttributedProduct getProduct(String attribute, boolean minimal, FAMAAttributedFeatureModel fm) {
		Collection<AttributedProduct> products = getOptimalProducts(attribute, minimal, fm);
		//System.out.println(products.size());
		Optional<AttributedProduct> res = null;
		if (minimal) {
			res = products.stream().sorted().findFirst();
		} else {
			res = products.stream().sorted(Comparator.reverseOrder()).findFirst();
		}
		if (res.isPresent()) {
			return res.get();
		}
		return null;
	}

	private static void optimalProducts(String attribute, boolean minimize, FAMAAttributedFeatureModel fm) {
		Collection<AttributedProduct> products = getOptimalProducts(attribute, minimize, fm);
		if (minimize) {
			System.out.println("Minimal products");
		} else {
			System.out.println("Maximal products");
		}
		if(products.size() > 0) {
			for (AttributedProduct p : products) {
				System.out.println(p);
			}
		}
		else {
			System.out.println("BAD GENERATION");
		}
	}

	private static void visitFM(FAMAAttributedFeatureModel fm) {
		for (GenericRelation r : fm.getRelations()) {
			System.out.println(r.getClass().getSimpleName());
			if (r instanceof Relation) {
				Relation rel = (Relation) r;
				Iterator<Cardinality> card = rel.getCardinalities();
				while (card.hasNext()) {
					System.out.println(card.next());
				}
			} else if (r instanceof Constraint) {

			}
		}
	}

	public static Collection<Product> getProducts(FAMAAttributedFeatureModel fm) {
		ChocoReasoner reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
		ChocoProductsQuestion q = new ChocoProductsQuestion();
		reasoner.ask(q);
		return q.getAllProducts();
	}

	public static AttributedProduct getRndProduct(FAMAAttributedFeatureModel fm) {
		ChocoReasoner reasoner = new ChocoReasoner();
		fm.transformTo(reasoner);
		ChocoOneProductQuestion q = new ChocoOneProductQuestion();
		reasoner.ask(q);
		AttributedProduct product = (AttributedProduct) q.getProduct();
		return product;
	}
}
