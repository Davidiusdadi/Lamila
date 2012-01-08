package searching;

/**
 * Represents the current state of a search through the network
 * 
 * @author David
 * 
 */
public abstract class AbstractSearchstate implements Searchstate {
	private int latenzes;
	private double averagelatnz;
	private int lastdist;
	private int minimumdist;
	private int requestcount;
	private int droppedrequests;

	/**
	 * 
	 */
	public AbstractSearchstate() {
		reset();
	}

	/**
	 * Tell that there was a response and its latenz.
	 * 
	 * @param milis
	 *            The time passed until there was a response to a request.
	 */
	public void fireLatenz( int milis ) {
		averagelatnz += milis;
		if( milis < minimumdist )
			minimumdist = milis;
		latenzes++;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see searching.Searchstate#getResponses()
	 */
	public int getResponseCount() {
		return latenzes;
	}

	/**
	 * Tells this object that a request was send.
	 */
	public void fireRequest() {
		requestcount++;
	}

	/**
	 * Tells this object that a listener to a response was removed.
	 */
	public void fireDropRequest() {
		droppedrequests++;
	}

	/**
	 * Tells this object that the distance to the goal is currently by.
	 * 
	 * @param dist
	 */
	public void firedist( int dist ) {
		lastdist = dist;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see searching.Searchstate#getAverageLatenz()
	 */
	public double getAverageLatenz() {
		// System.out.println("avla="+averagelatnz+"/"+latenzes+"=="+averagelatnz / latenzes);
		return averagelatnz / latenzes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see searching.Searchstate#getLatestGoalDistance()
	 */
	public int getLatestGoalDistance() {
		return lastdist;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see searching.Searchstate#getMinimumGoalDistance()
	 */
	public int getMinimumGoalDistance() {
		return minimumdist;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see searching.Searchstate#getRequestCount()
	 */
	public int getRequestCount() {
		return requestcount;
	}

	/**
	 * Resets all private Data of this object.
	 */
	private void reset() {
		latenzes = 0;
		averagelatnz = 0;
		lastdist = Integer.MAX_VALUE;
		minimumdist = Integer.MAX_VALUE;
		requestcount = 0;
		droppedrequests = 0;

	}

	@Override
	public abstract int getResultCount();

}
