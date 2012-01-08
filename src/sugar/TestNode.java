package sugar;

import globalstatic.Lamilastatics;

import java.net.InetSocketAddress;

import urlocator.Node;
import correlation.Hashcodecorrelator;

public class TestNode extends Node {

	public TestNode( InetSocketAddress adress , String nodeid ) {
		super( adress );
		int length = 128;
		byte[] destenation = new byte[ length ];
		byte[] stringbytes = nodeid.getBytes( Lamilastatics.charset );
		if( stringbytes.length > length )
			for( int i = 0 ; i < length ; i++ )
				destenation[ i ] = stringbytes[ i ];
		else
			for( int i = 0 ; i < length ; i++ )
				if( stringbytes.length > i )
					destenation[ i ] = stringbytes[ i ];
				else
					destenation[ i ] = Byte.MIN_VALUE;
		putKey( Hashcodecorrelator.id, destenation );

	}

	public TestNode( Node tocopy ) {
		super( tocopy );
	}

	public TestNode() {
	}

	@Override
	public byte[] getUniqueId() {
		return getKey( Hashcodecorrelator.id );
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public void putKey( int correlatorid, byte[] key ) {
		super.putKey( correlatorid, key );
	}

}
