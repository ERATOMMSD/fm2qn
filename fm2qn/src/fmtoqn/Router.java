package fmtoqn;

import java.util.ArrayList;
import java.util.List;

public class Router extends QNelement {
	private List<QNelement> selectedChildren;

	public Router(String name, QNelement father) {
		super(name, father);
		children = new ArrayList<QNelement>();
		selectedChildren = new ArrayList<QNelement>();
	}

	@Override
	public void addChild(QNelement child) {
		addChild(child, true);
	}

	public void addChild(QNelement child, boolean selected) {
		super.addChild(child);
		if (selected) {
			selectedChildren.add(child);
		}
	}

	public String getChildName(QNelement child) {
		assert children.contains(child);
		return getName() + "_TO_" + child.getName();
	}

	@Override
	public List<QNelement> getChildren() {
		return children;
	}

	public List<QNelement> getSelectedChildren() {
		return selectedChildren;
	}

	@Override
	public String toXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<node name=\"" + getName() + "\">\n");
		sb.append("\t<section className=\"Queue\">\n");
		sb.append("\t\t<parameter classPath=\"java.lang.Integer\" name=\"size\">\n");
		sb.append("\t\t\t<value>-1</value>\n");
		sb.append("\t\t</parameter>\n");
		sb.append("\t\t<parameter array=\"true\" classPath=\"java.lang.String\" name=\"dropStrategies\">\n");
		sb.append("\t\t\t<refClass>Class1</refClass>\n");
		sb.append("\t\t\t<subParameter classPath=\"java.lang.String\" name=\"dropStrategy\">\n");
		sb.append("\t\t\t\t<value>drop</value>\n");
		sb.append("\t\t\t</subParameter>\n");
		sb.append("\t\t</parameter>\n");
		sb.append(
				"\t\t<parameter classPath=\"jmt.engine.NetStrategies.QueueGetStrategies.FCFSstrategy\" name=\"FCFSstrategy\"/>\n");
		sb.append(
				"\t\t<parameter array=\"true\" classPath=\"jmt.engine.NetStrategies.QueuePutStrategy\" name=\"NetStrategy\">\n");
		sb.append("\t\t\t<refClass>Class1</refClass>\n");
		sb.append(
				"\t\t\t<subParameter classPath=\"jmt.engine.NetStrategies.QueuePutStrategies.TailStrategy\" name=\"TailStrategy\"/>\n");
		sb.append("\t\t</parameter>\n");
		sb.append("\t</section>\n");
		sb.append("\t<section className=\"ServiceTunnel\"/>\n");
		sb.append("\t\t<section className=\"Router\">\n");
		sb.append(
				"\t\t<parameter array=\"true\" classPath=\"jmt.engine.NetStrategies.RoutingStrategy\" name=\"RoutingStrategy\">\n");
		sb.append("\t\t\t<refClass>Class1</refClass>\n");
		sb.append(
				"\t\t\t<subParameter classPath=\"jmt.engine.NetStrategies.RoutingStrategies.EmpiricalStrategy\" name=\"Probabilities\">\n");
		sb.append(
				"\t\t\t\t<subParameter array=\"true\" classPath=\"jmt.engine.random.EmpiricalEntry\" name=\"EmpiricalEntryArray\">\n");
		for (QNelement c : children) {
			sb.append(
					"\t\t\t\t\t<subParameter classPath=\"jmt.engine.random.EmpiricalEntry\" name=\"EmpiricalEntry\">\n");
			sb.append("\t\t\t\t\t\t<subParameter classPath=\"java.lang.String\" name=\"stationName\">\n");
			sb.append("\t\t\t\t\t\t<value>" + c.getName() + "</value>\n");
			sb.append("\t\t\t\t\t\t</subParameter>\n");
			sb.append("\t\t\t\t\t\t<subParameter classPath=\"java.lang.Double\" name=\"probability\">\n");
			sb.append("\t\t\t\t\t\t<value>" + ((selectedChildren.contains(c)) ? "1" : "0") + ".0</value>\n");
			sb.append("\t\t\t\t\t\t</subParameter>\n");
			sb.append("\t\t\t\t\t</subParameter>\n");
		}
		sb.append("\t\t\t\t</subParameter>\n");
		sb.append("\t\t\t</subParameter>\n");
		sb.append("\t\t</parameter>\n");
		sb.append("\t</section>\n");
		sb.append("</node>");
		return sb.toString();
	}

	@Override
	public String connectionsToXml() {
		StringBuilder sb = new StringBuilder();
		for (QNelement child : children) {
			sb.append("<connection source=\"" + getName() + "\" target=\"" + child.getName() + "\"/>\n");
		}
		return sb.toString();
	}
}
