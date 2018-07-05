package fmtoqn.builder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import es.us.isa.ChocoReasoner.attributed.AttributedProduct;
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.AttributedFeature;
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.Dependency;
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.FAMAAttributedFeatureModel;
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.Relation;
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.RequiresDependency;
import es.us.isa.FAMA.models.FAMAAttributedfeatureModel.fileformats.AttributedReader;
import es.us.isa.FAMA.models.featureModel.Cardinality;
import es.us.isa.FAMA.models.featureModel.Constraint;
import es.us.isa.FAMA.models.featureModel.extended.GenericAttribute;
import fmtoqn.Fork;
import fmtoqn.Join;
import fmtoqn.QNelement;
import fmtoqn.Queue;
import fmtoqn.QueueNetwork;
import fmtoqn.Router;
import fmtoqn.RouterForOpt;
import fmtoqn.Sink;
import fmtoqn.Source;
import fmtoqn.products.OptProdExp;

public class BuildQNfromFM {
	public FAMAAttributedFeatureModel fm;
	QueueNetwork qn;
	String qnName;
	private Map<AttributedFeature, QNelement> mapFeatureQueue;
	private AttributedProduct product;
	private boolean byDefault = true;
	private Stack<QNelement> openJoins = new Stack<QNelement>();
	private int counter = 0;
	public static boolean parSemantics = true;

	public BuildQNfromFM(String fileName) throws Exception {
		AttributedReader reader = new AttributedReader();
		this.fm = (FAMAAttributedFeatureModel) reader.parseFile(fileName);
		mapFeatureQueue = new HashMap<AttributedFeature, QNelement>();
		Path path = Paths.get(fileName);
		qnName = path.getName(path.getNameCount() - 1).toString();
		qnName = qnName.substring(0, qnName.lastIndexOf("."));
	}

	public void buildQN() {
		qn = new QueueNetwork();
		AttributedFeature root = fm.getRoot();
		assert root != null;
		if (!byDefault) {
			product = OptProdExp.getProduct("", true, fm);
		}
		Source source = qn.getSource();
		assert source != null;
		// qn.addNode(source);//this seems wrong
		source.addChild(visit(root, source));
		for (Constraint c : fm.getConstraints()) {
			visitConstraint(c);
		}
	}

	public void visitConstraint(Constraint c) {
		if (c instanceof Dependency) {
			Dependency dependency = (Dependency) c;
			QNelement origin = mapFeatureQueue.get(dependency.getOrigin());
			QNelement destination = mapFeatureQueue.get(dependency.getDestination());
			if (dependency instanceof RequiresDependency) {
				qn.addRequires(origin, destination);
			} else {
				qn.addExcludes(origin, destination);
			}
		}
	}

	QNelement joiningElementForSeqSemantics;

	public QNelement visit(AttributedFeature f, QNelement father) {
		Collection<GenericAttribute> attributes = f.getAttributes();
		assert attributes.size() <= 1 : attributes.size();
		Queue q = null;
		if (attributes.size() == 1) {
			Integer cost = (Integer) attributes.iterator().next().getDefaultValue();
			q = new Queue(f.getName(), cost, father);
		} else {
			q = new Queue(f.getName(), 0, father);
		}
		qn.addNode(q);

		mapFeatureQueue.put(f, q);

		Iterator<Relation> it = f.getRelations();
		if (!it.hasNext()) {
			if (parSemantics) {
				// leaves of the feature model are connected to the sink
				Sink sink = qn.getSink();
				if (openJoins.isEmpty()) {
					q.addChild(sink);
					sink.addSinkSource(q);
				} else {
					// Join join = openJoins.get(0);
					QNelement join = openJoins.get(0);
					join.addChild(sink);
					for (int i = 1; i < openJoins.size(); i++) {
						// Join newJoin = openJoins.get(i);
						// newJoin.setChild(join);
						// join.addJoinSource(newJoin);
						// join = newJoin;
						QNelement newJoin = openJoins.get(i);
						newJoin.addChild(join);
						join.setFather(newJoin);
						join = newJoin;
					}
					q.addChild(join);
				}
			} else {
				q.addChild(joiningElementForSeqSemantics);
			}
		}

		List<AttributedFeature> andGroupOpt = new ArrayList<AttributedFeature>();
		List<AttributedFeature> andGroupMand = new ArrayList<AttributedFeature>();
		List<List<AttributedFeature>> andGroupAlt = new ArrayList<List<AttributedFeature>>();
		List<List<AttributedFeature>> andGroupOr = new ArrayList<List<AttributedFeature>>();
		while (it.hasNext()) {
			Relation rel = it.next();
			// System.out.println(rel);
			Iterator<Cardinality> c = rel.getCardinalities();
			if (!c.hasNext())
				throw new Error("It must have a cardinality");
			Cardinality card = c.next();
			if (c.hasNext())
				throw new Error("It must have only one cardinality");
			ArrayList<AttributedFeature> children = new ArrayList<AttributedFeature>();
			Iterator<AttributedFeature> d = rel.getDestination();
			while (d.hasNext()) {
				children.add(d.next());
			}
			if (children.size() == 1) {
				assert card.getMax() == 1;
				if (card.getMin() == 0) {
					andGroupOpt.add(children.get(0));
					assert card.getMax() == 1;
				} else {
					assert card.getMin() == 1;
					andGroupMand.add(children.get(0));
				}
			} else if (children.size() > 1) {
				assert card.getMin() == 1;
				if (card.getMax() == 1) {
					andGroupAlt.add(children);
				} else {
					assert card.getMax() == children.size();
					andGroupOr.add(children);
				}
			}
		}
		if (andGroupOpt.size() + andGroupMand.size() + andGroupOr.size() + andGroupAlt.size() >= 1) {
			buildAnd(q, andGroupOpt, andGroupMand, andGroupOr, andGroupAlt, parSemantics);
		}
		return q;
	}

	private void buildAnd(Queue father, List<AttributedFeature> andGroupOpt, List<AttributedFeature> andGroupMand,
			List<List<AttributedFeature>> andGroupOr, List<List<AttributedFeature>> andGroupAlt, boolean parSemantics) {
		if (parSemantics) {
			buildAndParSemantics(father, andGroupOpt, andGroupMand, andGroupOr, andGroupAlt);
		} else {
			buildAndSeqSemantics(father, andGroupOpt, andGroupMand, andGroupOr, andGroupAlt);
		}
	}

	private void buildAndSeqSemantics(Queue father, List<AttributedFeature> andGroupOpt,
			List<AttributedFeature> andGroupMand, List<List<AttributedFeature>> andGroupOr,
			List<List<AttributedFeature>> andGroupAlt) {
		// int previousSize = openJoins.size();
		if (joiningElementForSeqSemantics == null) {
			if (openJoins.size() == 0) {
				joiningElementForSeqSemantics = getQn().getSink();
			} else {
				joiningElementForSeqSemantics = openJoins.get(openJoins.size() - 1);
			}
		}
		QNelement joinForSeqSemanticsBackup = joiningElementForSeqSemantics;
		for (AttributedFeature child : andGroupMand) {
			QNelement m = buildMandatory(father, child);
			// openJoins.add(m);
			joiningElementForSeqSemantics = m;
		}
		for (AttributedFeature child : andGroupOpt) {
			RouterForOpt opt = buildOptional(father, child, parSemantics);
			// openJoins.add(opt);
			joiningElementForSeqSemantics = opt;
		}
		for (List<AttributedFeature> children : andGroupOr) {
			QNelement or = buildOr(father, children, parSemantics);
			// openJoins.add(or);
			joiningElementForSeqSemantics = or;
		}
		for (List<AttributedFeature> children : andGroupAlt) {
			Router alt = buildAlternative(father, children);
			joiningElementForSeqSemantics = alt;
		}
		father.addChild(joiningElementForSeqSemantics);

		assert father != null;
		joiningElementForSeqSemantics = joinForSeqSemanticsBackup;
	}

	private void buildAndParSemantics(Queue father, List<AttributedFeature> andGroupOpt,
			List<AttributedFeature> andGroupMand, List<List<AttributedFeature>> andGroupOr,
			List<List<AttributedFeature>> andGroupAlt) {
		if (andGroupOpt.size() + andGroupMand.size() + andGroupOr.size() + andGroupAlt.size() > 1) {
			counter++;
			Fork fork = new Fork(father.getName() + "_AND" + counter + "_fork", father);
			Join join = new Join(father.getName() + "_AND" + counter + "_join", fork);
			fork.setJoin(join);
			father.addChild(fork);
			qn.addNode(fork);
			qn.addNode(join);
			openJoins.push(join);
			for (AttributedFeature child : andGroupOpt) {
				fork.addChild(buildOptional(fork, child, parSemantics), true);
			}
			for (AttributedFeature child : andGroupMand) {
				fork.addChild(buildMandatory(fork, child), true);
			}
			for (List<AttributedFeature> children : andGroupOr) {
				fork.addChild(buildOr(fork, children, parSemantics));
			}
			for (List<AttributedFeature> children : andGroupAlt) {
				fork.addChild(buildAlternative(fork, children), true);
			}
			openJoins.pop();
		} else {
			if (andGroupOpt.size() == 1) {
				father.addChild(buildOptional(father, andGroupOpt.get(0), parSemantics));
			} else if (andGroupMand.size() == 1) {
				father.addChild(buildMandatory(father, andGroupMand.get(0)));
			} else if (andGroupOr.size() == 1) {
				father.addChild(buildOr(father, andGroupOr.get(0), parSemantics));
			} else if (andGroupAlt.size() == 1) {
				father.addChild(buildAlternative(father, andGroupAlt.get(0)));
			}
		}
	}

	private QNelement buildOr(QNelement father, List<AttributedFeature> children, boolean parSemantics) {
		if (parSemantics) {
			return buildOrParSemantics(father, children);
		} else {
			return buildOrSeqSemantics(father, children);
		}
	}

	private RouterForOpt buildOrSeqSemantics(QNelement father, List<AttributedFeature> children) {
		counter++;
		String orName = father.getName() + "_OR" + counter;
		RouterForOpt r = null;
		RouterForOpt previousR = null;
		AttributedFeature previousC = null;
		QNelement previousVisitedChild = null;
		assert joiningElementForSeqSemantics != null;
		QNelement joinForSeqSemanticsBackup = joiningElementForSeqSemantics;
		for (AttributedFeature c : children) {
			String name = orName + "_" + c.getName();

			r = new RouterForOpt(name, null);// TODO we should set the father after visiting the "previous" child
			QNelement visitedChild = visit(c, r);
			r.addChild(visitedChild);
			r.setMainChild(visitedChild);
			assert joiningElementForSeqSemantics != null : r.getName();
			r.addChild(joiningElementForSeqSemantics, false);
			qn.addNode(r);
			// openJoins.add(r);
			joiningElementForSeqSemantics = r;

			if (previousR != null) {
				// previousR.addChild(joinForSeqSemantics, false);
				assert previousR.getChildren().size() == 2 : previousR.getChildren();
				boolean childSelected = byDefault || product.getFeatures().contains(previousC);
				List<QNelement> previousSelectedChildren = previousR.getSelectedChildren();
				List<QNelement> previousChildren = previousR.getChildren();
				previousSelectedChildren.clear();
				if (childSelected) {
					previousSelectedChildren.add(previousVisitedChild);
				} else {
					QNelement otherChild = previousChildren
							.get((previousChildren.indexOf(previousVisitedChild) + 1) % 2);
					previousSelectedChildren.add(otherChild);
				}
			}
			previousR = r;
			previousC = c;
			previousVisitedChild = visitedChild;
		}
		// r.addChild(previousR);

		assert previousR.getChildren().size() == 2;
		boolean childSelected = byDefault || product.getFeatures().contains(previousC);
		List<QNelement> previousSelectedChildren = previousR.getSelectedChildren();
		List<QNelement> previousChildren = previousR.getChildren();
		previousSelectedChildren.clear();
		if (childSelected) {
			previousSelectedChildren.add(previousVisitedChild);
		} else {
			QNelement otherChild = previousChildren.get((previousChildren.indexOf(previousVisitedChild) + 1) % 2);
			previousSelectedChildren.add(otherChild);
		}

		joiningElementForSeqSemantics = joinForSeqSemanticsBackup;
		return r;
	}

	private Fork buildOrParSemantics(QNelement father, List<AttributedFeature> children) {
		counter++;
		Fork fork = new Fork(father.getName() + "_OR" + counter, father);
		Join join = new Join(father.getName() + "_OR" + counter + "_join", fork);
		fork.setJoin(join);
		father.addChild(fork);
		qn.addNode(fork);
		qn.addNode(join);
		openJoins.push(join);
		for (AttributedFeature child : children) {
			QNelement qn_el = visit(child, fork);
			// by default, we select all the children of the OR
			boolean childSelected = byDefault || product.getFeatures().contains(child);
			fork.addChild(qn_el, childSelected);
		}
		openJoins.pop();
		return fork;
	}

	private Router buildAlternative(QNelement father, List<AttributedFeature> children) {
		counter++;
		String name = father.getName() + "_ALT" + counter;
		Router r = new Router(name, father);
		// father.addChild(r);//this should be not necessary as done in the calling
		// method.
		qn.addNode(r);
		boolean first = true;
		for (AttributedFeature child : children) {
			QNelement qn_el = visit(child, r);
			if (byDefault) {
				if (first) {
					r.addChild(qn_el, true);// by default, we select the first child of the alternative
					first = false;
				} else {
					r.addChild(qn_el, false);
				}
			} else {
				boolean childSelected = product.getFeatures().contains(child);
				r.addChild(qn_el, childSelected);
			}
		}
		// qn.addAlternative(father, r);
		return r;
	}

	private QNelement buildMandatory(QNelement father, AttributedFeature child) {
		return visit(child, father);
	}

	private RouterForOpt buildOptional(QNelement father, AttributedFeature child, boolean parSemantics) {
		if (parSemantics) {
			return buildOptionalForParSematics(father, child);
		} else {
			return buildOptionalForSeqSematics(father, child);
		}
	}

	private RouterForOpt buildOptionalForSeqSematics(QNelement father, AttributedFeature child) {
		String name = "OPT_" + child.getName();
		RouterForOpt r = new RouterForOpt(name, father);
		qn.addNode(r);
		QNelement qn_el = visit(child, r);

		// by default, we select the optional feature
		boolean childSelected = byDefault || product.getFeatures().contains(child);
		r.addChild(qn_el, childSelected);
		r.addChild(joiningElementForSeqSemantics, !childSelected);
		r.setMainChild(qn_el);
		qn.addOptional(r);
		return r;
	}

	private RouterForOpt buildOptionalForParSematics(QNelement father, AttributedFeature child) {
		String name = "OPT_" + child.getName();
		RouterForOpt r = new RouterForOpt(name, father);
		qn.addNode(r);
		QNelement qn_el = visit(child, r);

		// by default, we select the optional feature
		boolean childSelected = byDefault || product.getFeatures().contains(child);
		r.addChild(qn_el, childSelected);

		r.setMainChild(qn_el);

		Sink sink = qn.getSink();
		if (openJoins.isEmpty()) {
			r.addChild(sink, !childSelected);
			sink.addSinkSource(r);
		} else {
			QNelement joiningElement = openJoins.get(0);
			joiningElement.addChild(sink);
			for (int i = 1; i < openJoins.size(); i++) {
				QNelement newJoin = openJoins.get(i);
				newJoin.addChild(joiningElement);
				joiningElement.setFather(newJoin);
				joiningElement = newJoin;
			}
			r.addChild(joiningElement, !childSelected);
		}
		qn.addOptional(r);
		return r;
	}

	public void saveQn() throws IOException {
		saveQn(qnName + ".jsimg");
	}

	public void saveQn(String name) throws IOException {
		assert qn != null;
		saveQn(qn, name);
	}

	public static void saveQn(QueueNetwork qn, String name) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(name)));
		qn.setFileName(name);
		bw.write(qn.toXml());
		bw.close();
	}

	public QueueNetwork getQn() {
		return qn;
	}

	public String getQnName() {
		return qnName;
	}
}
