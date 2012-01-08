package searching;

import java.util.LinkedList;
import java.util.concurrent.Future;

import urlocator.Node;

/**
 * Searches for Nodes in the Network
 * 
 * @author David Rohmer
 * 
 */
public interface Searchprocess {

	/**
	 * Startes the search as a new Thread.
	 * 
	 * @return The LinkedList<Node> of the Future is the Result of the Search.
	 */
	public Future<LinkedList<Node>> startSearch();

	/**
	 * Returns the Nodes which are found by this Search.
	 * 
	 * @return The Node list can be empty or incomplete if the Search was not successful or is not complete.
	 */
	public LinkedList<Node> getResults();

	/**
	 * Returns if this process is currently searching.
	 * 
	 * @return
	 */
	public boolean isActive();

	/**
	 * The status of this search.
	 * 
	 * @return
	 */
	public Searchstate getStatus();

	/**
	 * Returns if this process was successful.
	 * 
	 * @return
	 */
	public boolean wasSuccesful();
	/**
	 * Return if this process was started.
	 * 
	 * @return
	 */
	public boolean wasStarted();

}