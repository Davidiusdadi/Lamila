package iospeci;

import java.net.InetSocketAddress;

import urlocator.Node;
import correlation.Hashcodecorrelator;

public class UnspecificNode extends Node {

	public UnspecificNode( InetSocketAddress adress , byte[] nodeid ) {
		super( adress );
		putKey( Hashcodecorrelator.id, nodeid );
	}

	public UnspecificNode( Node tocopy ) {
		super( tocopy );
	}

	public UnspecificNode() {
		super();
	}

	@Override
	public void putKey( int correlatorid, byte[] key ) {
		super.putKey( correlatorid, key );
	}
}
