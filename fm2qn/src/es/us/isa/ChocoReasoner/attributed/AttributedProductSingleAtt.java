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

package es.us.isa.ChocoReasoner.attributed;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import es.us.isa.FAMA.models.featureModel.GenericFeature;
import es.us.isa.FAMA.models.featureModel.Product;

public class AttributedProductSingleAtt extends Product implements Comparable<AttributedProductSingleAtt> {
	private Map<GenericFeature, Double> attrValues;
	private Map<String, GenericFeature> featureName;

	public AttributedProductSingleAtt() {
		attrValues = new HashMap<GenericFeature, Double>();
		featureName = new HashMap<String, GenericFeature>();
	}

	public void addFeatureAttributeValue(GenericFeature feature, double value) {
		attrValues.put(feature, value);
		addFeatureName(feature);
	}

	public void addFeatureName(GenericFeature feature) {
		featureName.put(feature.getName(), feature);
	}

	public double getValue(GenericFeature feature) {
		return attrValues.containsKey(feature)?attrValues.get(feature):0;
	}

	public double totalValue() {
		double sum = 0;
		for (Double i : attrValues.values()) {
			sum += i;
		}
		return sum;
	}

	public void filterCosts(int ratio) {
		for (GenericFeature feature : attrValues.keySet()) {
			//System.err.println(feature.getName() + " " + attrValues.get(feature));
			Double value = attrValues.get(feature)/ratio;
			attrValues.put(feature, value);
			//System.err.println(feature.getName() + " " + attrValues.get(feature));
		}
	}

	public GenericFeature getFeatureByName(String name) {
		return featureName.get(name);
	}

	public boolean containsFeature(String name) {
		return (featureName.get(name) != null);
	}

	@Override
	public String toString() {
		Iterator<GenericFeature> it = listOfFeatures.iterator();
		StringBuilder sb = new StringBuilder();
		while (it.hasNext()) {
			GenericFeature feat = it.next();
			sb.append(feat.getName());
			if (attrValues.containsKey(feat)) {
				sb.append("(" + attrValues.get(feat) + ")");
			}
			sb.append("; ");
		}
		sb.append("[Tot = " + totalValue() + "]");
		return sb.toString();
	}

	@Override
	public int compareTo(AttributedProductSingleAtt o) {
		double diff = totalValue() - o.totalValue();
		if (diff < 0) {
			return -1;
		} else if (diff == 0) {
			return 0;
		} else {
			return 1;
		}
	}
}
