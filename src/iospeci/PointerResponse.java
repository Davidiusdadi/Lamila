package iospeci;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import urlocator.Node;

/**
 * Represents the Response the a Pointerrequest and tells which neighbors of the
 * requested node has a smaller distance tan the requester
 * 
 * @author David Rohmer
 * 
 */
public class PointerResponse extends DirectedResponsemessage {
	public static final int type = 4;

	public PointerResponse( PointerRequest req , Node n , int distance ) throws UnknownHostException {
		super( req );
		init( n, distance );
	}

	private void init( Node n, int distance ) throws UnknownHostException {
		byte[] hostbytes = n.getHost();
		initBuffer( 4 + 4 + 4 + 4 + hostbytes.length + n.getUniqueId().length, type );
		// bb.putInt(mesageid);
		bb.putInt( n.getPort() );
		bb.putInt( distance );
		bb.putInt( hostbytes.length );
		bb.putInt( n.getUniqueId().length );
		bb.put( hostbytes );
		bb.put( n.getUniqueId() );
	}

	public PointerResponse( int length , byte[] bytes ) {
		super( length, bytes );
	}

	/**
	 * Returns the requested node.
	 * The node knows its port, host, and id but no other information.
	 * Other node data's have to be requested especially.
	 * 
	 * @throws UnknownHostException
	 */
	public Node getNode() throws InvalidPackageException {
		bb.position( getBufferoffset() );
		int port = bb.getInt();
		bb.getInt(); // skip the distance value
		int hostlength = bb.getInt();
		int idlenght = bb.getInt();
		validateArLen( hostlength );
		validateArLen( idlenght );
		byte[] hostbytes = new byte[ hostlength ];
		bb.get( hostbytes );
		byte[] id = new byte[ idlenght ];
		bb.get( id );
		try {
			return new UnspecificNode( new InetSocketAddress( InetAddress.getByAddress( getNodeHost() ), getNodePort() ), id );
		} catch ( UnknownHostException e ) {// thrown if the ipadress is of illegal length
			throw new InvalidPackageException( e );
		}
	}

	public int getNodePort() {
		return bb.getInt( getBufferoffset() );
	}

	public byte[] getNodeHost() {
		bb.position( getBufferoffset() + 4 + 4 );
		int hostlength = bb.getInt();
		validateArLen( hostlength );
		bb.getInt();
		byte[] hostbytes = new byte[ hostlength ];
		bb.get( hostbytes );
		return hostbytes;
	}

	public byte[] getLayerKey() {
		bb.position( getBufferoffset() + 4 );
		int hostlength = bb.getInt();
		int idlenght = bb.getInt();
		bb.position( getBufferoffset() + 4 + 4 + 4 + hostlength );
		validateArLen( idlenght );
		byte[] keybytes = new byte[ idlenght ];
		bb.get( keybytes );
		return keybytes;
	}

	/**
	 * Returns the distance of the responded node to the requesting node
	 */
	public int getDistance() {
		bb.position( getBufferoffset() + 4 );
		return bb.getInt();
	}

	@Override
	public String toString() {
		return super.toString() + " dist: " + getDistance();
	}
}
