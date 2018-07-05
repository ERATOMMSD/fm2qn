package fmtoqn;

public class Queue extends QNelement {
	//public static double MIN_COST = 0.0000000000000000000001;
	private double cost;

	public Queue(String name, QNelement father) {
		this(name, 0, father);
	}

	public Queue(String name, double cost, QNelement father) {
		super(name, father);
		this.cost = cost;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	@Override
	public String toXml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<node name=\"" + getName() + "\">\n");
		sb.append("\t<section className=\"Queue\">\n");
		sb.append("\t\t<parameter classPath=\"java.lang.Integer\" name=\"size\">\n");
		sb.append("\t\t<value>-1</value>\n");
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
		sb.append("\t<section className=\"Server\">\n");
		sb.append("\t\t<parameter classPath=\"java.lang.Integer\" name=\"maxJobs\">\n");
		sb.append("\t\t\t<value>1</value>\n");
		sb.append("\t\t</parameter>\n");
		sb.append("\t\t<parameter array=\"true\" classPath=\"java.lang.Integer\" name=\"numberOfVisits\">\n");
		sb.append("\t\t\t<refClass>Class1</refClass>\n");
		sb.append("\t\t\t<subParameter classPath=\"java.lang.Integer\" name=\"numberOfVisits\">\n");
		sb.append("\t\t\t\t<value>1</value>\n");
		sb.append("\t\t\t</subParameter>\n");
		sb.append("\t\t</parameter>\n");
		sb.append(
				"\t\t<parameter array=\"true\" classPath=\"jmt.engine.NetStrategies.ServiceStrategy\" name=\"ServiceStrategy\">\n");
		sb.append("\t\t\t<refClass>Class1</refClass>\n");
		if (cost != 0) {
			sb.append(
					"\t\t\t<subParameter classPath=\"jmt.engine.NetStrategies.ServiceStrategies.ServiceTimeStrategy\" name=\"ServiceTimeStrategy\">\n");
			sb.append("\t\t\t\t<subParameter classPath=\"jmt.engine.random.Exponential\" name=\"Exponential\"/>\n");
			sb.append("\t\t\t\t<subParameter classPath=\"jmt.engine.random.ExponentialPar\" name=\"distrPar\">\n");
			sb.append("\t\t\t\t\t<subParameter classPath=\"java.lang.Double\" name=\"lambda\">\n");
			sb.append("\t\t\t\t\t\t<value>" + (1.0 / cost) + "</value>\n");
			sb.append("\t\t\t\t\t</subParameter>\n");
			sb.append("\t\t\t\t</subParameter>\n");
			sb.append("\t\t\t</subParameter>\n");
		} else {
			sb.append(
					"\t\t\t<subParameter classPath=\"jmt.engine.NetStrategies.ServiceStrategies.ZeroServiceTimeStrategy\" name=\"ZeroTimeServiceStrategy\"/>\n");
		}
		sb.append("\t\t</parameter>\n");
		sb.append("\t</section>\n");
		sb.append("\t<section className=\"Router\">\n");
		sb.append(
				"\t\t<parameter array=\"true\" classPath=\"jmt.engine.NetStrategies.RoutingStrategy\" name=\"RoutingStrategy\">\n");
		sb.append("\t\t\t<refClass>Class1</refClass>\n");
		sb.append(
				"\t\t\t<subParameter classPath=\"jmt.engine.NetStrategies.RoutingStrategies.RandomStrategy\" name=\"Random\"/>\n");
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
