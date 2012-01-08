package urlocator;

import iospeci.Requestmessage;
import correlation.Correlator;

public class PeerAdapter implements PeerListener {

	@Override
	public void linkAdded( Node hostednode, Correlator layer, Node linkednode ) {
	}

	@Override
	public void linkRemoved( Node hostednode, Correlator layer, Node linkednode ) {
	}

	@Override
	public void scheduleLinkRejecting() {
	}

	@Override
	public void scheduleLinkStoring() {
	}

	@Override
	public void requested( Requestmessage m ) {
	}

	@Override
	public void innerException( Exception e ) {
	}

}
