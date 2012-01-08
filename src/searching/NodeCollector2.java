package searching;

import globalstatic.Lamilastatics;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import urlocator.Node;
import urlocator.Peer;
import correlation.Correlator;

public class NodeCollector2 implements Searchprocess, Callable<LinkedList<Node>> {
	private Searchdata cache;
	private Future<LinkedList<Node>> fullresult;
	private boolean wasSuccesful = false;
	private SearchAdvisor advisor;
	private int resultcount;
	private Peer peer;
	private Node center;
	private Correlator correlator;

	public NodeCollector2( Peer peer , Node center , Correlator correlator , SearchAdvisor advisor , int resultcount ) {
		this.peer = peer;
		this.center = center;
		this.correlator = correlator;
		this.advisor = advisor;
		this.resultcount = resultcount;
		cache = new Searchdata( center.getKey( correlator.getCorrelatorId() ), correlator, advisor, resultcount );
	}

	@Override
	public LinkedList<Node> getResults() {
		return cache.getNearestNodes();
	}

	@Override
	public Searchstate getStatus() {
		return cache;
	}

	@Override
	public boolean isActive() {
		return fullresult.isDone();
	}

	@Override
	public Future<LinkedList<Node>> startSearch() {
		return fullresult = Lamilastatics.executor.submit( this );
	}

	@Override
	public boolean wasStarted() {
		return fullresult != null;
	}

	@Override
	public boolean wasSuccesful() {
		return cache.getNearestNodes().size() == resultcount;
	}

	@Override
	public LinkedList<Node> call() throws Exception {
		throw new RuntimeException();
	}
}
