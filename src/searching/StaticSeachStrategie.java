package searching;

import globalstatic.Lamilastatics;

/**
 * A very simple SearchAdvisor-Implementation
 * 
 * @author David Rohmer
 * 
 */
public class StaticSeachStrategie implements SearchAdvisor {

	int time, maxrequests, pulse, leafs, requesttimeout, maxpahtlenght;
	long starttime;
	Searchdata state;

	/**
	 * @param time
	 *            The maximal runntime @see {@link SearchAdvisor#getMaxSearchTime()}
	 * @param maxrequest
	 *            The maximal count of requests {@link SearchAdvisor#getMaximumRequestCount()}
	 * @param maxresults
	 *            The maximal resultcount {@link SearchAdvisor#getMaximumResultsCount()}
	 * @param pushfactor
	 *            The count of request by every searchstep {@link SearchAdvisor#getPushFactor()}
	 * @param requesttimeout
	 *            The nodetimeout for requests {@link SearchAdvisor#getNodeRequestTimeout()}
	 * @param maxpahtlenght
	 *            The maximal pathlengh {@link SearchAdvisor#getMaximumPathCount()}
	 * 
	 * @see SearchAdvisor
	 */
	public StaticSeachStrategie( int time , int maxrequest , int pulse , int pushfactor , int requesttimeout , int maxpahtlenght ) {
		this.time = time;
		this.maxrequests = maxrequest;
		this.pulse = pulse;
		this.maxpahtlenght = maxpahtlenght;
		this.leafs = pushfactor;
		this.requesttimeout = requesttimeout;
	}

	@Override
	public boolean isComplete() {
		return System.currentTimeMillis() - starttime > time || state.isComplete();
	}

	@Override
	public int getMaximumRequestCount() {
		return maxrequests;
	}

	@Override
	public int getPushFactor() {
		return leafs;
	}

	@Override
	public int getNodeRequestTimeout() {
		return requesttimeout;
	}

	@Override
	public void sync() {

	}

	/**
	 * returns a new StaticSeachStrategie(20000, 10, 10, 3, 2000,10)
	 * 
	 * @return
	 */
	public static StaticSeachStrategie getStdAdvisor() {
		return new StaticSeachStrategie( 20000, 10, 10, 3, Lamilastatics.tsTimeout( 2000 ), 10 );
	}

	@Override
	public int getMaximumPathCount() {
		return maxpahtlenght;
	}

	@Override
	public int getPulse() {
		return pulse;
	}

	@Override
	public void start( Searchdata state ) {
		starttime = System.currentTimeMillis();
		this.state = state;
	}

	@Override
	public SearchAdvisor createCopy() {
		return new StaticSeachStrategie( time, maxrequests, pulse, leafs, requesttimeout, maxpahtlenght );
	}

}
