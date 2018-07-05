package fmtoqn;

import java.util.ArrayList;
import java.util.List;

public class Join extends QNelement {
	private Fork fork;
	private List<QNelement> joinSources;

	public Join(String name, Fork fork) {
		super(name, null);
		joinSources = new ArrayList<QNelement>();
		setFork(fork);
	}

	public Join(String name) {
		this(name, null);
	}

	@Override
	public void setFather(QNelement father) {
		//assert false : "Join has no single father, but sink sources!";
		addJoinSource(father);
	}

	@Override
	public QNelement getFather() {
		assert false : "Sink has no single father, but sink sources!";
		return null;
	}

	public Fork getFork() {
		return fork;
	}

	public void setFork(Fork fork) {
		this.fork = fork;
	}

	public void addJoinSource(QNelement joinSource) {
		joinSources.add(joinSource);
	}

	public void removeJoinSource(QNelement joinSource) {
		joinSources.remove(joinSource);
	}

	public List<QNelement> getJoinSources() {
		return joinSources;
	}

	public void setChild(QNelement child) {
		assert children.size() <= 1;
		if (children.size() == 1) {
			children.remove(0);
		}
		addChild(child);
	}

	public QNelement getChild() {
		assert children.size() == 1: children.size();
		return children.get(0);
	}

	@Override
	public String toXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<node name=\"" + getName() + "\">\n");
		sb.append("\t<section className=\"Join\">\n");
		sb.append(
				"\t\t<parameter array=\"true\" classPath=\"jmt.engine.NetStrategies.JoinStrategy\" name=\"JoinStrategy\">\n");
		sb.append("\t\t\t<refClass>Class1</refClass>\n");
		sb.append(
				"\t\t\t<subParameter classPath=\"jmt.engine.NetStrategies.JoinStrategies.NormalJoin\" name=\"Standard Join\">\n");
		sb.append("\t\t\t\t<subParameter classPath=\"java.lang.Integer\" name=\"numRequired\">\n");
		sb.append("\t\t\t\t\t<value>-1</value>\n");
		sb.append("\t\t\t\t</subParameter>\n");
		sb.append("\t\t\t</subParameter>\n");
		sb.append("\t\t</parameter>\n");
		sb.append("\t</section>\n");
		sb.append("\t<section className=\"ServiceTunnel\"/>\n");
		sb.append("\t<section className=\"Router\">\n");
		sb.append(
				"\t\t<parameter array=\"true\" classPath=\"jmt.engine.NetStrategies.RoutingStrategy\" name=\"RoutingStrategy\">\n");
		sb.append("\t\t\t<refClass>Class1</refClass>\n");
		sb.append(
				"\t\t\t<subParameter classPath=\"jmt.engine.NetStrategies.RoutingStrategies.RandomStrategy\" name=\"Random\"/>\n");
		sb.append("\t\t</parameter>\n");
		sb.append("\t</section>\n");
		sb.append("</node>\n");
		return sb.toString();
	}

	@Override
	public String connectionsToXml() {
		return "<connection source=\"" + getName() + "\" target=\"" + getChild().getName() + "\"/>";
	}
}
