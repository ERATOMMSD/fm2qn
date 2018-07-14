package fmtoqn;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import es.us.isa.ChocoReasoner.attributed.AttributedProduct;
import jmt.common.exception.LoadException;
import jmt.common.exception.NetException;

public class QueueNetwork {
	private static final String SINK_RESPONSE_TIME = "<measure alpha=\"0.01\" name=\"sink_Response Time per Sink\" nodeType=\"station\" precision=\"0.03\" referenceNode=\"sink\" referenceUserClass=\"\" type=\"Response Time per Sink\" verbose=\"false\"/>\n";
	private static final String SYSTEM_RESPONSE_TIME = "<measure alpha=\"0.01\" name=\"System Response Time\" nodeType=\"station\" precision=\"0.03\" referenceNode=\"\" referenceUserClass=\"\" type=\"System Response Time\" verbose=\"false\"/>\n";

	private Source source;
	private Sink sink;
	private ArrayList<QNelement> nodes;
	private Set<Router> optionals;
	private Map<Queue, Fork> ors;
	private Map<QNelement, Router> alternatives;
	private Set<ArrayList<QNelement>[]> requires;
	private Set<ArrayList<QNelement>[]> excludes;
	private String fileName;
	private String attributeName;

	public QueueNetwork() {
		nodes = new ArrayList<QNelement>();
		source = new Source();
		sink = new Sink();
		optionals = new HashSet<Router>();
		ors = new HashMap<Queue, Fork>();
		alternatives = new HashMap<QNelement, Router>();
		requires = new HashSet<ArrayList<QNelement>[]>();
		excludes = new HashSet<ArrayList<QNelement>[]>();
		fileName = "out.jsimg";
	}

	public void addNode(QNelement el) {
		ArrayList<QNelement> nodes = getNodes();
		for(QNelement n: nodes) {
			String elName = el.getName();
			//assert !n.getName().equals(elName): elName;
			if(n.getName().equals(elName)) {
				return;
			}
		}
		nodes.add(el);
	}

	public void removeNode(QNelement el) {
		getNodes().remove(el);
	}

	public ArrayList<QNelement> getNodes() {
		return nodes;
	}

	public Source getSource() {
		return source;
	}

	public Sink getSink() {
		return sink;
	}

	public void addRequires(QNelement origin, QNelement destination) {
		requires.add(new ArrayList[] { getElements(origin), getElements(destination) });
	}

	public void addExcludes(QNelement origin, QNelement destination) {
		excludes.add(new ArrayList[] { getElements(origin), getElements(destination) });
	}

	public ArrayList<QNelement> getElements(QNelement el) {
		ArrayList<QNelement> elements = new ArrayList<QNelement>();
		elements.add(el);
		QNelement father = null;
		while ((el !=null)  && ((father = el.getFather()) != null)) {
			/*
			 * if (father instanceof Router) { elements.add(father); } else if (father
			 * instanceof Fork) { elements.add(el); //assert el instanceof
			 * elements.add(father); }
			 */
			elements.add(father);
			el = father;
		}
		return elements;
	}

	public boolean contains(QNelement el) {
		return nodes.contains(el);
	}

	public void addOptional(Router router) {
		optionals.add(router);
	}

	public void addOR(Queue father, Fork fork) {
		ors.put(father, fork);
	}

	public void addAlternative(QNelement father, Router router) {
		alternatives.put(father, router);
	}

	public void setProductInQN(AttributedProduct product) {
		for (QNelement c : source.getChildren()) {
			setProductInQN(c, product);
		}
	}

	private void setProductInQN(QNelement element, AttributedProduct product) {
		if (element instanceof RouterForOpt) {
			RouterForOpt r = (RouterForOpt) element;
			List<QNelement> children = r.getChildren();
			List<QNelement> selectedChildren = r.getSelectedChildren();
			selectedChildren.clear();
			assert children.size() == 2;
			QNelement child = r.getMainChild();
			if (!product.containsFeature(child.getName())) {
				child = children.get((children.indexOf(child) + 1)%2);
			}
			selectedChildren.add(child);
			setProductInQN(child, product);
			
			
			if (selectedChildren.size() == 0) {
				throw new Error("Exactly one child must be selected!");
			}
		}
		else if (element instanceof Router) {
			Router r = (Router) element;
			List<QNelement> children = r.getChildren();
			List<QNelement> selectedChildren = r.getSelectedChildren();
			selectedChildren.clear();
			for (QNelement child : children) {
				String childName = child.getName();
				if (product.containsFeature(childName)) {
					selectedChildren.add(child);
					setProductInQN(child, product);
				}
			}
			if (selectedChildren.size() == 0) {
				assert children.size() == 2;
				QNelement notSelectedOption = children.get(1);
				assert !(notSelectedOption instanceof Queue) : notSelectedOption.getClass().getSimpleName();
				selectedChildren.add(notSelectedOption);
			}
		} else if (element instanceof Fork) {
			Fork f = (Fork) element;
			f.getSelectedChildren().clear();
			for (QNelement child : f.getChildren()) {
				String childName = child.getName();
				if (child instanceof Router || product.containsFeature(childName)) {
					f.getSelectedChildren().add(child);
					setProductInQN(child, product);
				}
			}
		} else if (element instanceof Queue) {
			Queue q = (Queue) element;
			if (product.containsFeature(q.getName())) {
				q.setCost(product.getValue(product.getFeatureByName(q.getName()), attributeName));
			} else {
				// System.err.println(q.getName());
				// q.setCost(0);
			}
			for (QNelement c : q.getChildren()) {
				// System.err.println(c.getName());
				setProductInQN(c, product);
			}
		}
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String toXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\r\n"
				+ "<archive xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" name=\"" + fileName
				+ "\" timestamp=\"" + new Date() + "\" xsi:noNamespaceSchemaLocation=\"Archive.xsd\">\r\n"
				+ "<sim disableStatisticStop=\"false\" logDecimalSeparator=\".\" logDelimiter=\",\" logPath=\"./\" logReplaceMode=\"0\" maxSamples=\"1000000\" name=\""
				+ fileName + "\" polling=\"1.0\" xsi:noNamespaceSchemaLocation=\"SIMmodeldefinition.xsd\">\n"
				+ "<userClass name=\"Class1\" priority=\"0\" referenceSource=\"" + source.getName() + "\" type=\"open\"/>\n");
		sb.append(source.toXml()).append("\n");
		for (QNelement node : nodes) {
			sb.append(node.toXml()).append("\n");
		}
		sb.append(sink.toXml()).append("\n");

		sb.append(SYSTEM_RESPONSE_TIME);
		sb.append(source.connectionsToXml());
		for (QNelement node : nodes) {
			sb.append(node.connectionsToXml());
		}
		sb.append("</sim>\n</archive>");
		return sb.toString();
	}

	public void printConstraints() {
		for (Router o : optionals) {
			List<QNelement> children = o.getChildren();
			assert children.size() == 2;
			System.out.println(o.getChildName(children.get(0)) + " + " + o.getChildName(children.get(1)) + " = 1");
		}
		for (Queue or : ors.keySet()) {
			List<String> operands = new ArrayList<String>();
			Fork fork = ors.get(or);
			for (QNelement g : fork.getChildren()) {
				operands.add(fork.getChildName(g));
			}
			for (int i = 0; i < operands.size() - 1; i++) {
				System.out.print(operands.get(i) + " + ");
			}
			System.out.println(operands.get(operands.size() - 1) + " >= 1");
		}
		for (QNelement alt : alternatives.keySet()) {
			List<String> operands = new ArrayList<String>();
			Router router = alternatives.get(alt);
			for (QNelement g : router.getChildren()) {
				operands.add(router.getChildName(g));
			}
			for (int i = 0; i < operands.size() - 1; i++) {
				System.out.print(operands.get(i) + " + ");
			}
			System.out.println(operands.get(operands.size() - 1) + " = 1");
		}
		for (ArrayList<QNelement>[] require : requires) {
			ArrayList<String> list = filter(require[0]);

			System.out.print("(" + list.get(0));
			for (int i = 1; i < list.size(); i++) {
				System.out.print(" & " + list.get(i));
			}
			list = filter(require[1]);
			System.out.print(") -> (" + list.get(0));
			for (int i = 1; i < list.size(); i++) {
				System.out.print(" & " + list.get(i));
			}
			System.out.println(")");
		}
		for (ArrayList<QNelement>[] exclude : excludes) {
			ArrayList<String> list = filter(exclude[0]);

			System.out.print("(" + list.get(0));
			for (int i = 1; i < list.size(); i++) {
				System.out.print(" & " + list.get(i));
			}
			list = filter(exclude[1]);
			System.out.print(") -> !(" + list.get(0));
			for (int i = 1; i < list.size(); i++) {
				System.out.print(" & " + list.get(i));
			}
			System.out.println(")");
		}
	}

	private static ArrayList<String> filter(ArrayList<QNelement> list) {
		ArrayList<String> newList = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			QNelement q = list.get(i);
			if (q instanceof Router || q instanceof Fork) {
				i++;
				assert i < list.size();
				QNelement child = list.get(i);
				newList.add(((Fork) q).getChildName(child));
			}
		}
		return newList;
	}

	public String runSimulation() throws LoadException, NetException, Exception {
		runSimulation(fileName);
		return readSimulationResults();
	}

	public String readSimulationResults() throws JDOMException, IOException {
		return readSimulationResults(fileName + "-result.jsim");
	}

	public static void runSimulation(String fileName) throws LoadException, NetException, Exception {
		jmt.commandline.Jmt.main(new String[] { "sim", fileName });
	}

	public static String readSimulationResults(String fileName) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(fileName);
		Element root = doc.getRootElement();
		Element result = root.getChildren().get(0);
		double meanValue = Double.parseDouble(result.getAttributeValue("meanValue"));
		DecimalFormat df = new DecimalFormat("#.####");
		//System.out.println("Average value = " + df.format(meanValue));
		return df.format(meanValue);
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
}
