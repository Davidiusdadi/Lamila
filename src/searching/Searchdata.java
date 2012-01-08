package searching;

import globalstatic.Lamilastatics;
import globalstatic.Lamilastatics.OutputKind;
import iospeci.InvalidPackageException;
import iospeci.PointerResponse;
import iospeci.Responsemessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.TimeoutException;

import searching.Searcher.ResponseWaiter;
import urlocator.Node;
import urlocator.Retriever;
import correlation.Correlator;

/**
 * Organizes the data of a search.
 * 
 * @author David Rohmer
 * 
 */
public class Searchdata extends AbstractSearchstate {
	byte[] key;
	private HashMap<Integer,Integer> node2dist;
	private LinkedList<Node> pathpointer;
	private LinkedList<Retriever> incommings;
	private Correlator searchlayercorrelator;
	private LinkedList<Node> result;
	private SearchAdvisor advisor;
	private int maxresults;
	private boolean prevent_reps = true;

	/**
	 * Creates a Searchdata
	 * 
	 * @param key
	 *            The key which the searchprocess searches for
	 * @param searchlayercorrelator
	 *            The way to get the distance between nodes
	 * @param advisor
	 */
	public Searchdata( byte[] key , Correlator searchlayercorrelator , SearchAdvisor advisor , int resultcount ) {
		this.key = key;
		this.maxresults = resultcount;
		if( key == null )
			throw new IllegalArgumentException( "key can't be null" );
		this.advisor = advisor;
		incommings = new LinkedList<Retriever>();
		node2dist = new HashMap<Integer,Integer>();
		pathpointer = new LinkedList<Node>();
		result = new LinkedList<Node>();
		this.searchlayercorrelator = searchlayercorrelator;
	}

	/**
	 * Tell this cache to wait the results of r and put their results on the
	 * path.
	 * 
	 * @param r
	 */
	public void integrate( Retriever r ) {
		synchronized ( incommings ) {
			incommings.add( r );
			fireRequest();
			if( incommings.size() > advisor.getMaximumRequestCount() ) {
				incommings.getLast().cancel();
				incommings.removeLast();
				fireDropRequest();
			}
		}
	}

	/**
	 * Returns the node from the path which is next to the searched key.
	 * 
	 * @return
	 */
	public Node pop() {
		synchronized ( pathpointer ) {
			if( pathpointer.isEmpty() )
				return null;
			Node toreturn = pathpointer.removeFirst();
			Lamilastatics.println( OutputKind.INFO, "pop " + toreturn );
			while ( pathpointer.size() > advisor.getMaximumPathCount() ) {
				pathpointer.removeLast();
			}
			// System.out.println("pathpointer " + pathpointer.size());
			return toreturn;
		}
	}

	/**
	 * Processes new arrived responses.
	 * 
	 * @throws TimeoutException
	 */
	public int processincomming( ResponseWaiter waiter ) throws TimeoutException {
		advisor.sync();
		int count = 0;
		int rems = 0;
		while ( true )
			synchronized ( incommings ) {
				Iterator<Retriever> it = incommings.iterator();
				waiter.listen();
				while ( it.hasNext() ) {
					Retriever next = it.next();
					if( next.isResponsePopable() ) {
						processResopnse( next );
						count++;
					}
					if( next.allResponsesPoped() ) {
						it.remove();
						rems++;
						fireLatenz( (int) next.getRuntime() );
					}
				}
				// System.out.println("count: " + count + " inco: "
				// + incommings.size() + " path: " + pathpointer.size()
				// + " rems: " + rems);
				if( count == 0 && incommings.size() != 0 && pathpointer.size() == 0 )
					try {
						waiter.await();
					} catch ( InterruptedException e ) {
						e.printStackTrace();
					}
				else {
					return count;
				}
				// System.out.println("incommings: " + incommings.size());
				// return count;
			}
	}

	private void processResopnse( Retriever r ) throws InvalidPackageException {
		synchronized ( r ) {
			if( !r.isResponsePopable() ) {
				return;
			}
		}
		Responsemessage aresponse = r.getAResponse();
		assert ( aresponse != null );
		if( aresponse instanceof PointerResponse ) {
			PointerResponse response = (PointerResponse) aresponse;
			int dist = response.getDistance();
			firedist( dist );
			Node node;
			try {
				node = response.getNode();
			} catch ( Exception e ) {
				throw new InvalidPackageException();
			}
			useAsPath( node, dist );
		}
	}

	/**
	 * Sorts a parametrer nodewithkey into the result list.
	 * 
	 * @param nodewithkey
	 */
	public void useAsResult( Node nodewithkey ) {
		byte[] itskey = nodewithkey.getKey( searchlayercorrelator.getCorrelatorId() );
		if( itskey == null )
			throw new IllegalArgumentException();
		int dist = searchlayercorrelator.correlate( key, itskey );
		Lamilastatics.println( OutputKind.INFO, "recognize dis: " + dist + " node: " + nodewithkey );
		int nodeindex;
		synchronized ( result ) {
			nodeindex = getListPosi( result, dist );
			result.add( nodeindex, nodewithkey );
			Lamilastatics.println( "pre sort in 4 result : node" + nodewithkey + " dis: " + dist );
			if( result.size() > maxresults )
				result.removeLast();
		}
		node2dist.put( Arrays.hashCode( nodewithkey.getUniqueId() ), dist );
	}

	public void useAsPath( Node node, int dist ) {
		if( prevent_reps && node2dist.get( Arrays.hashCode( node.getUniqueId() ) ) != null )
			return;
		int nodeindex;
		synchronized ( pathpointer ) {
			nodeindex = getListPosi( pathpointer, dist );
			pathpointer.add( nodeindex, node );
			Lamilastatics.println( OutputKind.INFO, "sortin for pop : node" + node + " dis: " + dist );
			if( pathpointer.size() > advisor.getMaximumPathCount() )
				pathpointer.removeLast();
		}
		synchronized ( result ) {
			if( result.contains( node ) )
				return;
			nodeindex = getListPosi( result, dist );
			result.add( nodeindex, node );
			Lamilastatics.println( "sortin 4 result : node" + node + " dis: " + dist );
			// if (Peer.printio)
			if( result.size() > searchlayercorrelator.getConvictionCount() ) {
				result.removeLast();
			}
		}
		Lamilastatics.println( "so " + result );
		node2dist.put( Arrays.hashCode( node.getUniqueId() ), dist );
	}

	private int getListPosi( LinkedList<Node> list, Integer dist ) {
		if( list.isEmpty() || dist < node2dist.get( Arrays.hashCode( list.getFirst().getUniqueId() ) ) )
			return 0;
		else {
			ListIterator<Node> iterator = list.listIterator( list.size() );
			while ( iterator.hasPrevious() ) {
				Node cur = iterator.previous();
				Integer nodedist = node2dist.get( Arrays.hashCode( cur.getUniqueId() ) );
				if( dist > nodedist ) {
					// list.add(iterator.nextIndex() + 1, node);
					return iterator.nextIndex() + 1;
				}
			}
			return list.size();
		}
	}

	/**
	 * Returns true if the are no unrequested messages and no nodes in the path.
	 * If this method returns true pop() will always return null.
	 * 
	 * @return
	 */
	public boolean isComplete() {
		return incommings.size() == 0 && pathpointer.size() == 0;
	}

	/**
	 * The Result of the search up to now.
	 * 
	 * @return
	 */
	public LinkedList<Node> getNearestNodes() {
		return result;
	}

	@Override
	public int getResultCount() {
		return result.size();
	}

	public void chanceRequests() {
		for( int i = 0 ; i < incommings.size() ; i++ )
			fireDropRequest();
		incommings.clear();
	}

	public void preventRequestRepitition( boolean prevent ) {
		this.prevent_reps = prevent;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " awaiting " + incommings;
	}
}
