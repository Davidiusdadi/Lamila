package iospeci;

import iospeci.transport.ResponsePreview;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Groundclass for all Requests
 * 
 * @author David Rohmer
 */
public abstract class DirectedRequestmessage extends Requestmessage implements DirectedMessage {
	private int offset;
	private InetSocketAddress adress;

	public DirectedRequestmessage( int length , byte[] bytes ) {
		super( length, bytes );
		offset = super.getBufferoffset() + 8 + bb.getInt( super.getBufferoffset() + 4 );
	}

	@Override
	protected void initBuffer( int size, int messagetype, int packetid ) {
		byte[] hostdata = adress.getAddress().getAddress();
		int port = adress.getPort();
		super.initBuffer( size + 4 + 4 + hostdata.length, messagetype, packetid );
		bb.putInt( port );
		bb.putInt( hostdata.length );
		bb.put( hostdata );
		adress = null;
		offset = bb.position();
	}

	@Override
	protected int getBufferoffset() {
		return offset;
	}

	public DirectedRequestmessage( InetSocketAddress adress ) {
		this.adress = adress;
	}

	public DirectedRequestmessage( InetSocketAddress adress , int messagetype , int size ) throws UnknownHostException {
		this.adress = adress;
		initBuffer( size, messagetype );
	}

	/* (non-Javadoc)
	 * 
	 * @see iospeci.DirectedMessage#getHost() */
	@Override
	public byte[] getHost() {
		bb.position( super.getBufferoffset() + 4 );
		int hostdatalegth = bb.getInt();
		byte[] hostdata = new byte[ hostdatalegth ];
		bb.get( hostdata );
		return hostdata;
	}

	/* (non-Javadoc)
	 * 
	 * @see iospeci.DirectedMessage#getPort() */
	@Override
	public int getPort() {
		return bb.getInt( super.getBufferoffset() );
	}

	public InetSocketAddress getAdress() {
		try {
			return new InetSocketAddress( InetAddress.getByAddress( getHost() ), getPort() );
		} catch ( UnknownHostException e ) {
			throw new InvalidPackageException( e );
		}
	}

	public DatagramPacket getAsPacket() throws UnknownHostException , SocketException {
		return new DatagramPacket( bb.array(), bb.array().length, adress );
	}

	@Override
	public Buffercontent getContent() {
		return this;
	}

	public void setFrom( InetSocketAddress from ) {
		adress = from;
	}

	public InetSocketAddress getFrom() {
		return adress;
	}

	/**
	 * Returns true if the response will arrive in fragments. That mean the
	 * response will start with a {@link ResponsePreview}
	 */
	abstract public boolean isMultiResponseRequest();

}
