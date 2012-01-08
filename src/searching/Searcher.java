package searching;

import globalstatic.Lamilastatics;
import globalstatic.Lamilastatics.OutputKind;
import iospeci.PointerRequest;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import urlocator.Node;
import urlocator.Peer;
import urlocator.Retriever;
import urlocator.Retriever.RetrieverListener;
import correlation.Correlator;

/**
 * A Searcher which will find a key in the network faster than other searchers.
 * If your seach for more than one result the result might be not optimal.
 * 
 * @author David Rohmer
 */
public class Searcher implements Callable<LinkedList<Node>>, Searchprocess {
	class ResponseWaiter implements RetrieverListener {
		int timeout;

		public ResponseWaiter( int timeout ) {
			this.timeout = timeout;
		}

		Object lock = new Object();
		CountDownLatch latch = new CountDownLatch( 1 );

		@Override
		public void retrieverChanged( Retriever r ) {
			latch.countDown();
		}

		public void await() throws InterruptedException , TimeoutException {
			if( !latch.await( Lamilastatics.tsTimeout( timeout ), TimeUnit.MILLISECONDS ) ) {
				throw new TimeoutException( "Waiting " + timeout + " milis: " + cache );
			}
		}

		public void listen() {
			// System.out.print("Lis");
			synchronized ( this ) {
				latch = new CountDownLatch( 1 );
			}
			// System.out.println("ten");
		}
	}

	private final Correlator correlator;
	private final byte[] key;
	private Peer connection;
	private LinkedList<Node> searchentys = new LinkedList<Node>();
	private Searchdata cache;
	private ResponseWaiter waiter;
	private boolean wasSuccesful = false;
	private Future<LinkedList<Node>> futur = null;
	private SearchAdvisor advisor;
	private boolean wasstarted = false;

	/**
	 * @param connection
	 *            The socket-connection to the network
	 * @param correlator
	 *            The Correlator
	 * @param key
	 *            The searched key
	 * @param entrys
	 *            The entry in the network
	 * @param advisor
	 *            The advisor of this search
	 */
	public Searcher( Peer connection , Correlator correlator , byte[] key , LinkedList<Node> entrys , SearchAdvisor advisor , int resultcount ) {
		this.key = key;
		this.advisor = advisor;
		this.correlator = correlator;
		this.connection = connection;
		this.searchentys = entrys;
		waiter = new ResponseWaiter( advisor.getNodeRequestTimeout() );
		cache = new Searchdata( key, correlator, advisor, resultcount );
	}

	public Future<LinkedList<Node>> startSearch() {
		wasstarted = true;
		return futur = Lamilastatics.executor.submit( this );
	}

	public LinkedList<Node> getResults() {
		return cache.getNearestNodes();
	}

	@Override
	public LinkedList<Node> call() throws Exception {
		int loops = 0;
		int emtyloops = 0;
		// System.out.println("Starte Suche");
		Thread.currentThread().setName( "Searcher" );
		advisor.start( cache );
		for( Node sn : searchentys )
			try {
				Retriever ret = new Retriever( connection, new PointerRequest( sn.getAdress(), sn.getUniqueId(), correlator.getCorrelatorId(), key, advisor.getPushFactor() ), advisor.getNodeRequestTimeout(), waiter );
				cache.integrate( ret );
				ret.send();
				cache.useAsResult( sn );
			} catch ( UnknownHostException e ) {
				e.printStackTrace();
			}
		while ( true ) {
			int inco = cache.processincomming( waiter );
			Node node = cache.pop();
			if( node != null )
				Lamilastatics.println( OutputKind.INFO, "pop: " + node );
			else if( inco == 0 ) {
				if( advisor.isComplete() )
					break;
				// waiter.await(Integer.MAX_VALUE);
			}
			if( node != null ) {
				Retriever retr = new Retriever( connection, new PointerRequest( node.getAdress(), node.getUniqueId(), correlator.getCorrelatorId(), key, advisor.getPushFactor() ), advisor.getNodeRequestTimeout(), waiter );
				cache.integrate( retr );
				retr.send();
			} else {
				emtyloops++;
				// if(inco!=0)
			}
			// Thread.sleep(advisor.getPulse());
			loops++;
		}
		wasSuccesful = true;

		// System.out.println( "Loops: " + loops + " ," + emtyloops );
		return cache.getNearestNodes();
	}

	@Override
	public Searchstate getStatus() {
		return cache;
	}

	@Override
	public boolean isActive() {
		return !futur.isDone();
	}

	@Override
	public boolean wasSuccesful() {
		return wasSuccesful;
	}

	@Override
	public boolean wasStarted() {
		return wasstarted;
	}
}
