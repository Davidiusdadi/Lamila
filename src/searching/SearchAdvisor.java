package searching;

/**
 * This class enables the user to minimize memory an bandwidth be modifying the
 * search behaviour.
 * 
 * @author David
 * 
 */
public interface SearchAdvisor {
	public static final int PUSH_MUCH = -1;
	public static final int PUSH_ALL = -2;

	/**
	 * Returns the sleeptime of the searcher
	 */
	public int getPulse();

	/**
	 * Returns the maximum count of request the searcher should listen to.
	 * 
	 * @return
	 */
	public int getMaximumRequestCount();

	/**
	 * Returns the maximum count of nodes the searcher should keep . The Path is
	 * a list of nodes ordered by their distance to the seached key in the
	 * network. In every searchstep "pushfactor" nodes are popped from the path
	 * and their links get requested and put onto the path.
	 * 
	 * @return
	 */
	public int getMaximumPathCount();

	/**
	 * Returns the count of requests the searcher should make in every search
	 * step;
	 * 
	 * @return
	 */
	public int getPushFactor();

	/**
	 * Returns the miliseconds the searcher should wait for a request.
	 * (A great request-nodetimeout will not slow down the searchprocess)
	 * 
	 * @return
	 */
	public int getNodeRequestTimeout();

	/**
	 * Returns true if the she search should be stopped.
	 * 
	 * @return
	 */
	public boolean isComplete();

	/**
	 * Tells the advisor to react to changes of his private data.
	 * The exact behavior will be specified be implementing classes.
	 */
	public void sync();

	public void start( Searchdata state );

	public abstract SearchAdvisor createCopy();
}
