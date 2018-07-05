package fmtoqn.builder;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.util.IteratorIterable;

import fmtoqn.QNelement;
import fmtoqn.ReflectiveVisitor;
import fmtoqn.Sink;

public class CheckConstraints extends ReflectiveVisitor<QNelement> {

	public static void checkConstraints(String fileName) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(fileName);
		Element root = doc.getRootElement();
		Element sim = root.getChildren().get(0);
		Map<String, Set<String>> altSelectedChildren = new HashMap<String, Set<String>>();
		Map<String, Set<String>> orSelectedChildren = new HashMap<String, Set<String>>();
		for (Element e : sim.getChildren()) {
			if (e.getName().equals("node")) {
				String nodeName = e.getAttributeValue("name");
				if (nodeName.startsWith("OPT_")) {
					findProbabilities(e, nodeName);
				} else if (nodeName.contains("_ALT_")) {
					String altFatherName = nodeName.split("_ALT_")[0];
					if (!altSelectedChildren.containsKey(altFatherName)) {
						altSelectedChildren.put(altFatherName, new HashSet<String>());
					}
					String selectedDest = findProbabilities(e, nodeName);
					if (!selectedDest.equals(Sink.SINK_NAME)) {
						altSelectedChildren.get(altFatherName).add(selectedDest);
					}
				} else if (nodeName.contains("_OR_")) {
					String orFatherName = nodeName.split("_OR_")[0];
					if (!orSelectedChildren.containsKey(orFatherName)) {
						orSelectedChildren.put(orFatherName, new HashSet<String>());
					}
					String selectedDest = findProbabilities(e, nodeName);
					if (selectedDest != null && !selectedDest.equals(Sink.SINK_NAME)) {
						orSelectedChildren.get(orFatherName).add(selectedDest);
					}
				}
			}
		}
		for (String altFatherName : altSelectedChildren.keySet()) {
			if (altSelectedChildren.get(altFatherName).size() != 1) {
				System.err.println("Exactly one child must be selected in an alternative group. Group " + altFatherName
						+ " is malformed.");
			}
		}
		for (String orFatherName : orSelectedChildren.keySet()) {
			if (orSelectedChildren.get(orFatherName).size() == 0) {
				System.err.println(
						"At least one child must be selected in an OR group. Group " + orFatherName + " is malformed.");
			}
		}
	}

	private static String findProbabilities(Element router, String routerName) {
		IteratorIterable<Element> descs = router.getDescendants(Filters.element());
		while (descs.hasNext()) {
			Element e = descs.next();
			if (e.getName().equals("subParameter")) {
				if (e.getAttributeValue("name").equals("EmpiricalEntryArray")) {
					List<Element> children = e.getChildren();
					assert children.size() == 2;
					int numOnes = 0;
					String selectedDest = null;
					for (Element child : children) {
						assert child.getAttributeValue("name").equals("EmpiricalEntry");
						String name = null;
						String value = null;
						for (Element a : child.getChildren()) {
							String attributeName = a.getAttributeValue("name");
							if (attributeName.equals("stationName")) {
								name = a.getChildText("value");
							} else if (attributeName.equals("probability")) {
								value = a.getChildText("value");
							}
						}
						assert name != null && value != null;
						if (!value.equals("1.0") && !value.equals("0.0")) {
							System.err.println("Probability values must be either 0 or 1: connection to " + name
									+ " from " + routerName + " has value " + value);
							return null;
						}
						if (value.equals("1.0")) {
							numOnes++;
							selectedDest = name;
						}
					}
					if (numOnes != 1) {
						System.err.print("In a router, one destination must have probability 1 and the other 0.\nIn "
								+ routerName + " there are ");
						if (numOnes == 2) {
							System.err.println("two 1s");
						} else {
							System.err.println("two 0s");
						}
						return null;
					}
					return selectedDest;
				}
			}
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		checkConstraints("James.jsimg");
		// checkConstraints("servicesOnlyPrice.jsimg");
	}
}
