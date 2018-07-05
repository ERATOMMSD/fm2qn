package fmtoqn;

import java.util.ArrayList;

public class Source extends QNelement {
	public final static String SOURCE_NAME = "rootSource";

	public Source() {
		super(SOURCE_NAME, null);
		children = new ArrayList<QNelement>();
	}

	@Override
	public void setFather(QNelement father) {
		assert false: "Source has no father!";
	}
	
	@Override
	public String toXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<node name=\"" + getName() + "\">\n");
		sb.append("\t<section className=\"RandomSource\">\n");
		sb.append("\t\t<parameter array=\"true\" classPath=\"jmt.engine.NetStrategies.ServiceStrategy\" name=\"ServiceStrategy\">\n");
		sb.append("\t\t\t<refClass>Class1</refClass>\n");
		sb.append("\t\t\t<subParameter classPath=\"jmt.engine.NetStrategies.ServiceStrategies.ServiceTimeStrategy\" name=\"ServiceTimeStrategy\">\n");
		sb.append("\t\t\t\t<subParameter classPath=\"jmt.engine.random.Exponential\" name=\"Exponential\"/>\n");
		sb.append("\t\t\t\t<subParameter classPath=\"jmt.engine.random.ExponentialPar\" name=\"distrPar\">\n");
		sb.append("\t\t\t\t\t<subParameter classPath=\"java.lang.Double\" name=\"lambda\">\n");
		sb.append("\t\t\t\t\t\t<value>0.5</value>\n");
		sb.append("\t\t\t\t\t</subParameter>\n");
		sb.append("\t\t\t\t</subParameter>\n");
		sb.append("\t\t\t</subParameter>\n");
		sb.append("\t\t</parameter>\n");
		sb.append("\t</section>\n");
		sb.append("\t<section className=\"ServiceTunnel\"/>\n");
		sb.append("\t<section className=\"Router\">\n");
		sb.append("\t\t<parameter array=\"true\" classPath=\"jmt.engine.NetStrategies.RoutingStrategy\" name=\"RoutingStrategy\">\n");
		sb.append("\t\t\t<refClass>Class1</refClass>\n");
		sb.append("\t\t\t<subParameter classPath=\"jmt.engine.NetStrategies.RoutingStrategies.RandomStrategy\" name=\"Random\"/>\n");
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
