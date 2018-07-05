package fmtoqn.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import es.us.isa.ChocoReasoner.attributed.AttributedProduct;
import fmtoqn.Fork;
import fmtoqn.Join;
import fmtoqn.QNelement;
import fmtoqn.Queue;
import fmtoqn.QueueNetwork;
import fmtoqn.Router;
import fmtoqn.Sink;
import fmtoqn.Source;

public class SimplifyQueueNetworkFromProduct {
	private static Logger logger = Logger.getLogger(SimplifyQueueNetworkFromProduct.class.getSimpleName());
	private static String attributeName;

	public static QueueNetwork simplifyOnlyQueues(QueueNetwork qn, AttributedProduct product) {
		QueueNetwork newQn = new QueueNetwork();
		Source newSource = newQn.getSource();
		Sink newSink = qn.getSink();
		for (QNelement c : qn.getSource().getChildren()) {
			ArrayList<Queue> newChildren = simplifyOnlyQueues(c, product, newSource, newSink, newQn);
			for (Queue newChild : newChildren) {
				newSource.addChild(newChild);
			}
		}
		return newQn;
	}

	private static ArrayList<Queue> simplifyOnlyQueues(QNelement element, AttributedProduct product,
			QNelement newFather, Sink sink, QueueNetwork qn) {
		ArrayList<Queue> queues = new ArrayList<Queue>();
		if (element instanceof Router) {
			Router r = (Router) element;
			for (QNelement child : r.getChildren()) {
				String childName = child.getName();
				if (product.containsFeature(childName)) {
					queues.addAll(simplifyOnlyQueues(child, product, newFather, sink, qn));
				}
			}
		} else if (element instanceof Fork) {
			Fork f = (Fork) element;
			for (QNelement child : f.getChildren()) {
				String childName = child.getName();
				if (child instanceof Router || product.containsFeature(childName)) {
					queues.addAll(simplifyOnlyQueues(child, product, newFather, sink, qn));
				}
			}
		} else if (element instanceof Queue) {
			Queue q = (Queue) element;
			if (product.containsFeature(q.getName())) {
				Queue newQueue = new Queue(q.getName(), newFather);
				qn.addNode(newQueue);
				for (QNelement c : q.getChildren()) {
					ArrayList<Queue> newChildren = simplifyOnlyQueues(c, product, newQueue, sink, qn);
					for (Queue newChild : newChildren) {
						newQueue.addChild(newChild);
					}
				}
				if (newQueue.getChildren().size() == 0) {
					newQueue.addChild(sink);
				}
				newQueue.setCost(product.getValue(product.getFeatureByName(q.getName()), attributeName));
				queues.add(newQueue);
			}
		}
		return queues;
	}

	public static QueueNetwork simplify(QueueNetwork qn, AttributedProduct product) {
		Handler handler = new ConsoleHandler();
		handler.setFormatter(new Formatter() {

			@Override
			public String format(LogRecord record) {
				return record.getMessage();
			}
		});
		logger.addHandler(handler);
		QueueNetwork newQn = new QueueNetwork();
		Source newSource = newQn.getSource();
		Sink newSink = newQn.getSink();
		Map<Join, Join> joins = new HashMap<Join, Join>();
		for (QNelement c : qn.getSource().getChildren()) {
			QNelement newChild = simplify(c, product, newSource, newQn, joins);
			if (newChild != null) {
				newSource.addChild(newChild);
			}
		}
		if (newSource.getChildren().size() == 0) {
			newSource.addChild(newSink);
		}
		filterJoins(newQn, joins.values());
		return newQn;
	}

	private static void filterJoins(QueueNetwork newQn, Collection<Join> newJoins) {
		logger.log(Level.INFO, "filter joins");
		for (Join join : newJoins) {
			if (!newQn.contains(join)) {
				// the join could have been deleted
				continue;
			}
			List<QNelement> joinSources = join.getJoinSources();
			assert joinSources.size() >= 1 : joinSources.size();
			if (joinSources.size() == 1) {
				QNelement source = joinSources.get(0);
				/*
				 * if (source instanceof Queue) { Queue q = (Queue) source; QNelement father =
				 * q.getFather(); QNelement grandfather = father.getFather();
				 * q.setFather(grandfather); q.removeChild(join); q.addChild(join.getChild());
				 * grandfather.removeChild(father); grandfather.addChild(q);
				 * newQn.removeNode(father); } else if (source instanceof Fork) { Fork fork =
				 * (Fork) source; QNelement father = fork.getFather(); father.removeChild(fork);
				 * father.addChild(join.getChild()); newQn.removeNode(fork); }
				 */
				QNelement joinChild = join.getChild();
				QNelement joinSource = joinSources.get(0);
				joinSource.removeChild(join);
				joinSource.addChild(joinChild);
				if (joinChild instanceof Queue) {
					joinChild.setFather(joinSource);
				} else if (joinChild instanceof Join) {
					Join j = (Join) joinChild;
					j.removeJoinSource(join);
					j.addJoinSource(joinSource);
				} else if (joinChild instanceof Sink) {
					Sink s = (Sink) joinChild;
					s.removeSinkSource(join);
					s.addSinkSource(joinSource);
				}
				Fork fork = join.getFork();
				assert fork != null;
				assert fork.getChildren().size() == 1 : fork.getChildren().size();
				QNelement forkChild = fork.getChildren().get(0);
				QNelement forkFather = fork.getFather();
				assert forkFather != null;
				forkFather.removeChild(fork);
				forkFather.addChild(forkChild);
				if (forkChild instanceof Queue) {
					forkChild.setFather(forkFather);
				} else if (forkChild instanceof Join) {
					Join j = (Join) forkChild;
					j.removeJoinSource(fork);
					j.addJoinSource(forkFather);
				} else if (forkChild instanceof Sink) {
					Sink s = (Sink) forkChild;
					s.removeSinkSource(fork);
					s.addSinkSource(forkFather);
				}

				newQn.removeNode(join);
				newQn.removeNode(fork);
			}
		}
	}

	private static QNelement simplify(QNelement element, AttributedProduct product, QNelement newFather,
			QueueNetwork newQn, Map<Join, Join> joins) {
		QNelement newElement = null;
		logger.log(Level.INFO, "add " + element.getName());
		if (element instanceof Router) {
			Router r = (Router) element;
			for (QNelement child : r.getChildren()) {
				String childName = child.getName();
				if (product.containsFeature(childName)) {
					newElement = simplify(child, product, newFather, newQn, joins);
				}
			}
		} else if (element instanceof Fork) {
			Fork fork = (Fork) element;
			Fork newFork = new Fork(fork.getName(), newFather);
			newQn.addNode(newFork);
			Join newJoin = (Join) simplify(fork.getJoin(), product, null, newQn, joins);
			newFork.setJoin(newJoin);
			newJoin.setFork(newFork);
			for (QNelement child : fork.getChildren()) {
				QNelement newChild = simplify(child, product, newFork, newQn, joins);
				if (newChild != null) {
					newFork.addChild(newChild, true);
				}
			}
			if (newFork.getChildren().size() == 0) {
				newFork.addChild(newJoin);
				newJoin.addJoinSource(newFork);
			}
			newElement = newFork;
		} else if (element instanceof Queue) {
			Queue q = (Queue) element;
			if (product.containsFeature(q.getName())) {
				Queue newQueue = new Queue(q.getName(), newFather);
				newQn.addNode(newQueue);
				for (QNelement c : q.getChildren()) {
					QNelement newChild = simplify(c, product, newQueue, newQn, joins);
					if (newChild != null) {
						newQueue.addChild(newChild);
					}
				}
				if (newQueue.getChildren().size() == 0) {
					newQueue.addChild(newQn.getSink());
				}
				newQueue.setCost(product.getValue(product.getFeatureByName(q.getName()), attributeName));
				newElement = newQueue;
			}
		} else if (element instanceof Join) {
			Join join = (Join) element;
			Join newJoin;
			if (!joins.containsKey(join)) {
				newJoin = new Join(join.getName());
				joins.put(join, newJoin);
				newQn.addNode(newJoin);
				QNelement newChild = simplify(join.getChild(), product, newJoin, newQn, joins);
				if (newChild != null) {
					newJoin.setChild(newChild);
				}
			} else {
				newJoin = joins.get(join);
			}
			if (newFather != null) {
				newJoin.addJoinSource(newFather);
			}
			newElement = newJoin;
		} else if (element instanceof Sink) {
			newElement = newQn.getSink();
		}
		return newElement;
	}
}
