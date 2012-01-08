package iospeci;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public abstract class DirectedResponsemessage extends Responsemessage implements DirectedMessage {
	private InetSocketAddress adress;
	byte encrypt;
	int offset;

	public DirectedResponsemessage( int length , byte[] bytes ) {
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
		hostdata = null;
		offset = bb.position();
		setCryption( encrypt );
	}

	@Override
	protected int getBufferoffset() {
		return offset;
	}

	public DirectedResponsemessage( DirectedRequestmessage req , int messagetype , int size ) throws UnknownHostException {
		super( req.getMessageId() );
		this.adress = req.getFrom();
		encrypt = req.getCryption();
		initBuffer( size, messagetype );
	}

	public DirectedResponsemessage( DirectedRequestmessage req ) {
		super( req.getMessageId() );
		this.adress = req.getFrom();
		encrypt = req.getCryption();
	}

	public byte[] getHost() {
		bb.position( super.getBufferoffset() + 4 );
		int hostdatalegth = bb.getInt();
		validateArLen( hostdatalegth );
		byte[] hostdata = new byte[ hostdatalegth ];
		bb.get( hostdata );
		return hostdata;
	}

	public int getPort() {
		return bb.getInt( super.getBufferoffset() );
	}

	public InetSocketAddress getDestenationAdress() {
		try {
			return new InetSocketAddress( InetAddress.getByAddress( getHost() ), getPort() );
		} catch ( UnknownHostException e ) {
			throw new InvalidPackageException( "invalid ip format" );
		}
	}

	@Override
	public Buffercontent getContent() {
		return this;
	}

	public DatagramPacket getAsPacket() throws UnknownHostException , SocketException {
		return new DatagramPacket( bb.array(), bb.array().length, adress );
	}
}
