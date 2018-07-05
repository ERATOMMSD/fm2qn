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

public class AttributedProduct extends Product implements Comparable<AttributedProduct> {
	private Map<String, Map<GenericFeature, Double>> attrValues;
	private Map<String, GenericFeature> featureName;
	private String mainAttribute;
	private boolean isValidProduct = true;

	public AttributedProduct() {
		attrValues = new HashMap<String, Map<GenericFeature, Double>>();
		featureName = new HashMap<String, GenericFeature>();
	}

	public void addFeatureAttributeValue(GenericFeature feature, String attribute, double value) {
		if (!attrValues.containsKey(attribute)) {
			attrValues.put(attribute, new HashMap<>());
		}
		assert !attrValues.get(attribute).containsKey(feature) : attrValues.get(attribute).get(feature);
		attrValues.get(attribute).put(feature, value);
		addFeatureName(feature);
	}

	public void addFeatureName(GenericFeature feature) {
		featureName.put(feature.getName(), feature);
	}

	public double getValue(GenericFeature feature, String attribute) {
		return (attrValues.containsKey(attribute) && attrValues.get(attribute).containsKey(feature))
				? attrValues.get(attribute).get(feature)
				: 0;
	}

	public double totalValue(String attribute) {
		double sum = 0;
		Map<GenericFeature, Double> map = attrValues.get(attribute);
		if (map != null) {
			for (Double i : map.values()) {
				sum += i;
			}
		}
		return sum;
	}

	public void filterCosts(int ratio) {
		filterCosts(ratio, mainAttribute);
	}

	public void filterCosts(int ratio, String attribute) {
		Map<GenericFeature, Double> attributeMap = attrValues.get(attribute);
		// it could be null if there are no features in the product
		if (attributeMap != null) {
			for (GenericFeature feature : attributeMap.keySet()) {
				// System.err.println(feature.getName() + " " + attributeMap.get(feature));
				Double value = attributeMap.get(feature) / ratio;
				attributeMap.put(feature, value);
				// System.err.println(feature.getName() + " " + attributeMap.get(feature));
			}
		}
	}

	public GenericFeature getFeatureByName(String name) {
		return featureName.get(name);
	}

	public boolean containsFeature(String name) {
		return (featureName.get(name) != null);
	}

	public void setMainAttribute(String mainAttribute) {
		this.mainAttribute = mainAttribute;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String attribute : attrValues.keySet()) {
			sb.append("{").append(attribute).append(" ");
			Map<GenericFeature, Double> attributeMap = attrValues.get(attribute);
			Iterator<GenericFeature> it = listOfFeatures.iterator();
			while (it.hasNext()) {
				GenericFeature feat = it.next();
				sb.append(feat.getName());
				if (attributeMap.containsKey(feat)) {
					sb.append("(" + attributeMap.get(feat) + ")");
				}
				sb.append("; ");
			}
			sb.append("[Tot = " + totalValue(attribute) + "]} ");
		}
		return sb.toString();
	}

	@Override
	public int compareTo(AttributedProduct o) {
		double diff = totalValue(mainAttribute) - o.totalValue(mainAttribute);
		if (diff < 0) {
			return -1;
		} else if (diff == 0) {
			return 0;
		} else {
			return 1;
		}
	}
}
