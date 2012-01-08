package urlocator;

import iospeci.Requestmessage;
import correlation.Correlator;

/**
 * Enables the user to react to changes in the network which might be relevant to this peer.
 * 
 * @author David Rohmer
 * 
 */
public interface PeerListener {
	public void scheduleLinkRejecting();
	public void scheduleLinkStoring();
	public void linkAdded( Node hostednode, Correlator layer, Node linkednode );
	public void linkRemoved( Node hostednode, Correlator layer, Node linkednode );
	public void requested( Requestmessage m );
	public void innerException( Exception e );
}
