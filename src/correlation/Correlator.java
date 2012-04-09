package correlation;

import searching.SearchAdvisor;
import urlocator.Node;
import urlocator.Peer;

/**
 * A Correlator is used ( for example by {@link Peer}) to organize {@link Node}s and its
 * derived classes. All functions a Correlator interface provides do
 * serve this purpose. Node s which are organized using the same
 * Correlator are called to be on the same <b>layer</b>.<br>
 **/
public interface Correlator {
	/**
	 * Allows to compare the identities of a Node. Its that keys may be raw
	 * numbers or strings. byte[] is the return type because keys are expected
	 * to be transfered over the network and in some cases the marshaling to the
	 * specific type wont be needed for every Correlator.
	 */
	public int correlate( byte[] key1, byte[] key2 );

	/**
	 * Definies the preferred number of neighbors a Node have to be interested
	 * in.
	 */
	public int getConvictionCount();

	/** Every {@link Correlator} derived class must return its own id. */
	public int getCorrelatorId();

	/** Allows to search each networklayer in a different specialized way */
	public SearchAdvisor getSearchAdvisor();

	public Class<?> getKeyRepresentationType();

	// public Class<?> getKeyTransfereType();
}
