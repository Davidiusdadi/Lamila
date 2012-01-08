package searching;

public interface Searchstate {

	/**
	 * Returns the count of recognized responses which are full retrieved without any problems.
	 * 
	 * @return
	 */
	public abstract int getResponseCount();

	/**
	 * returns the average response time.
	 * 
	 * @return
	 */
	public abstract double getAverageLatenz();

	/**
	 * Returns the latest goaldistance-
	 * 
	 * @return
	 */
	public abstract int getLatestGoalDistance();

	/**
	 * Returns the smallest goaldistance-
	 * 
	 * @return
	 */
	public abstract int getMinimumGoalDistance();

	/**
	 * Returns the total count of requests.
	 * 
	 * @return
	 */
	public abstract int getRequestCount();
	public abstract int getResultCount();

}