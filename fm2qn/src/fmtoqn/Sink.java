package fmtoqn;

import java.util.ArrayList;
import java.util.List;

public class Sink extends QNelement {
	public final static String SINK_NAME = "sink";
	private List<QNelement> sinkSources;

	public Sink() {
		super(SINK_NAME, null);
		sinkSources = new ArrayList<QNelement>();
	}

	@Override
	public void setFather(QNelement father) {
		assert false : "Sink has no single father, but sink sources!";
	}

	@Override
	public void addChild(QNelement child) {
		assert false : "Sink has no children!";
	}

	@Override
	public List<QNelement> getChildren() {
		assert false : "Sink has no children!";
		return super.getChildren();
	}

	public void addSinkSource(QNelement sinkSource) {
		sinkSources.add(sinkSource);
	}

	public void removeSinkSource(QNelement sinkSource) {
		sinkSources.remove(sinkSource);
	}

	public List<QNelement> getSinkSources() {
		return sinkSources;
	}

	@Override
	public String toXml() {
		return "<node name=\"" + SINK_NAME + "\">\n" + "\t<section className=\"JobSink\"/>\n" + "</node>";
	}

	@Override
	public String connectionsToXml() {
		return "";
	}
}
