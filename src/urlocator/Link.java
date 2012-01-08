package urlocator;

/**
 * This class provides an node and the time of its last access.
 * 
 * @author David Rohmer
 */
public class Link {
	/**
	 * Inits this link this s and the current timemilis.
	 */
	public Link( Node s ) {
		this.node = s;
		since = System.currentTimeMillis();
	}

	/**
	 * Inits this link this s and the given time.
	 * 
	 * @param s
	 *            The node
	 * @param since
	 *            The timemilis
	 */
	public Link( Node s , long since ) {
		this.node = s;
		this.since = since;
	}

	/**
	 * Sets the last contact to the current timemilis.
	 */
	public void update() {
		since = System.currentTimeMillis();
	}

	private Node node;
	private long since;

	/**
	 * Returns the node this link provides.
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * Returns the time when this node was contacted the last time.
	 */
	public long lastContact() {
		return since;
	}

	/** calls {@link Node#hashCode()} */
	@Override
	public int hashCode() {
		return node.hashCode();
	}

	@Override
	/** calls {@link Node#equals()} */
	public boolean equals( Object obj ) {
		return node.equals( obj );
	}
}