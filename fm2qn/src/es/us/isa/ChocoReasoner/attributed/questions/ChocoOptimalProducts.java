/**
 * 	This file is part of FaMaTS.
 *
 *     FaMaTS is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FaMaTS is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with FaMaTS.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.us.isa.ChocoReasoner.attributed.questions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import choco.Choco;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;
import es.us.isa.ChocoReasoner.ChocoResult;
import es.us.isa.ChocoReasoner.attributed.AttributedProduct;
import es.us.isa.ChocoReasoner.attributed.ChocoQuestion;
import es.us.isa.ChocoReasoner.attributed.ChocoReasoner;
import es.us.isa.FAMA.Benchmarking.PerformanceResult;
import es.us.isa.FAMA.Exceptions.FAMAException;
import es.us.isa.FAMA.Reasoner.AttributedFeatureModelReasoner;
import es.us.isa.FAMA.Reasoner.Reasoner;
import es.us.isa.FAMA.Reasoner.questions.extended.OptimalProducts;
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.AttributedFeature;
import es.us.isa.FAMA.models.featureModel.GenericFeature;
import es.us.isa.FAMA.models.featureModel.Product;
import es.us.isa.FAMA.models.featureModel.extended.GenericAttribute;

public class ChocoOptimalProducts extends ChocoQuestion implements OptimalProducts {
	Collection<Product> products = new ArrayList<Product>();
	// Collection<AttributedProduct> attributedProducts= new
	// ArrayList<AttributedProduct>();
	Collection<AttributedProduct> attributedProducts = new ArrayList<AttributedProduct>();
	String attname = "";
	private boolean minimize;

	public ChocoOptimalProducts() {
		this(true);
	}

	public ChocoOptimalProducts(boolean minimize) {
		this.minimize = minimize;
	}

	@Override
	public PerformanceResult answer(Reasoner r) throws FAMAException {
		//System.err.println(attname);
		ChocoReasoner reasoner = (ChocoReasoner) r;
		ChocoResult res = new ChocoResult();
		Solver sol = new CPSolver();
		Model p = reasoner.getProblem();
		Map<String, IntegerVariable> atributesVar = reasoner.getAttributesVariables();

		// primero cramos la coleccion con los atributos que nos interesan
		// dependiendo de la cadena de entrada
		int maxSum = 0;
		Collection<IntegerVariable> selectedAtts = new ArrayList<IntegerVariable>();
		Iterator<Entry<String, IntegerVariable>> atributesIt = atributesVar.entrySet().iterator();
		while (atributesIt.hasNext()) {
			Entry<String, IntegerVariable> entry = atributesIt.next();
			if (entry.getKey().contains("." + attname)) {
				maxSum += entry.getValue().getUppB();
				selectedAtts.add(entry.getValue());
			}
			
		}
		//System.out.println("maxSum " + maxSum);

		// Ahora necesitamos crear una variable suma de todos los atributos anteriores"
		IntegerVariable[] reifieds = new IntegerVariable[selectedAtts.size()];

		IntegerVariable suma = Choco.makeIntVar("_suma", 0, maxSum);
		IntegerExpressionVariable sumatorio = Choco.sum(selectedAtts.toArray(reifieds));
		Constraint sumReifieds = Choco.eq(suma, sumatorio);

		p.addConstraint(sumReifieds);

		sol.read(p);
		try {
			sol.propagate();
		} catch (ContradictionException e1) {
			e1.printStackTrace();
		}
		IntDomainVar maxVar = sol.getVar(suma);

		// sol.minimize(maxVar, false);
		if (minimize) {
			sol.minimize(maxVar, false);
		} else {
			sol.maximize(maxVar, false);
		}

		// Buscamos el minimo valor de la suma. es la misma chapuza de explain errors :S
		Solver sol2 = new CPSolver();
		Constraint cons2 = Choco.eq(suma, sol.getVar(suma).getVal());
		p.addConstraint(cons2);

		sol2.read(p);

		try {
			sol2.propagate();
		} catch (ContradictionException e1) {
			e1.printStackTrace();
		}
		Set<String> notSelectedFeatures = new HashSet<>();
		// Obtener todo los valores que tengan ese valor
		if (sol2.solve() == Boolean.TRUE && sol2.isFeasible()) {
			do {
				//System.out.println(sol2.solutionToString());
				Product prod = new Product();
				// AttributedProduct attributedProduct = new AttributedProduct();
				AttributedProduct attributedProduct = new AttributedProduct();
				for (int i = 0; i < p.getNbIntVars(); i++) {
					IntDomainVar aux = sol2.getVar(p.getIntVar(i));
					int value = aux.getVal();
					// assert value >= 0;
					//System.out.println(aux);
					if (value >= 0 && !aux.getName().startsWith("rel") && !aux.getName().startsWith("_")) {
						GenericFeature f = getFeature(aux, reasoner);
						if (f != null && f instanceof AttributedFeature) {
							System.err.println(f.getName() + ": " + value);
							if(value == 1) {
								prod.addFeature(f);
								attributedProduct.addFeature(f);
								attributedProduct.addFeatureName(f);
							}
							else {
								notSelectedFeatures.add(f.getName());
							}
						} else {
							System.err.println(aux.getName());
							GenericAttribute af = getAttribute(aux, reasoner);
							// assert af != null: aux;
							if (af != null) {
								//System.err.println(af.getFeature().getName());
								if(!notSelectedFeatures.contains(af.getFeature().getName())) {
									System.err.println(af.getFullName() + " -> " + value);
									attributedProduct.addFeatureAttributeValue(af.getFeature(), af.getName(), value);
								}
							} else {
								System.err.println("no feature " + aux);
							}
						}
					}
				}
				products.add(prod);
				attributedProducts.add(attributedProduct);
			} while (sol2.nextSolution() == Boolean.TRUE);
		}
		res.fillFields(sol2);
		return res;
	}

	private GenericFeature getFeature(IntDomainVar aux, ChocoReasoner reasoner) {
		String temp = new String(aux.toString().substring(0, aux.toString().indexOf(":")));
		GenericFeature f = reasoner.searchFeatureByName(temp);
		return f;
	}

	private GenericAttribute getAttribute(IntDomainVar aux, ChocoReasoner reasoner) {
		String temp = new String(aux.toString().substring(0, aux.toString().indexOf(":")));
		for (GenericAttribute a : reasoner.getAllAttributes()) {
			// System.out.println(a.getName());
			if (aux.getName().equals(a.getFullName())) {
				return a;
			}
		}
		return null;
	}

	@Override
	public Collection<Product> getProducts() {
		return products;
	}

	@Override
	public void setAttributeName(String name) {
		this.attname = name;
	}

	@Override
	public Class<? extends AttributedFeatureModelReasoner> getReasonerClass() {
		return ChocoReasoner.class;
	}

	public Collection<AttributedProduct> getAttributedProducts() {
		return attributedProducts;
	}
}
