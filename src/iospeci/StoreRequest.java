package iospeci;

import globalstatic.Lamilastatics;

import java.net.UnknownHostException;

import urlocator.Node;

/** Instructs the Recipient to the Node which is described in this Message */
public class StoreRequest extends DirectedRequestmessage {
	public static final int type = 3;

	public StoreRequest( Node thestore , Node tostore , int layer ) throws UnknownHostException {
		super( thestore.getAdress() );
		initBuffer( 4 + thestore.getUniqueId().length + 4 + 4 + 4 + tostore.getUniqueId().length + tostore.getKey( layer ).length, type );
		bb.putInt( layer );
		bb.putInt( thestore.getUniqueId().length );
		bb.put( thestore.getUniqueId() );
		bb.putInt( tostore.getUniqueId().length );
		bb.putInt( tostore.getKey( layer ).length );
		bb.put( tostore.getUniqueId() );
		bb.put( tostore.getKey( layer ) );
	}

	public StoreRequest( final int length , final byte[] bytes ) {
		super( length, bytes );
	}

	public int getCorrelatorId() {
		return bb.getInt( getBufferoffset() );
	}

	public Node getToStore() throws UnknownHostException {
		bb.position( getBufferoffset() + 8 + bb.getInt( getBufferoffset() + 4 ) );
		int idlenght = bb.getInt();
		int keylength = bb.getInt();
		byte[] ident = new byte[ idlenght ];
		byte[] key = new byte[ keylength ];
		bb.get( ident );
		bb.get( key );
		UnspecificNode node = new UnspecificNode( getAddress(), ident );
		node.putKey( getCorrelatorId(), key );
		return node;
	}

	public byte[] getStoreNodeId() {
		bb.position( getBufferoffset() + 4 );
		int keysize = bb.getInt();
		byte[] key = new byte[ keysize ];
		bb.get( key );
		return key;
	}

	@Override
	public boolean isMultiResponseRequest() {
		return false;
	}

	@Override
	public String toString() {
		return super.toString() + " for " + new String( getStoreNodeId(), Lamilastatics.charset );
	}
}
