package iospeci.transport.UDPTransport;

import iospeci.Buffercontent;

public class ReorderMessage extends Buffercontent {
	public static final int type = -3;

	public ReorderMessage( int packetid ) {
		super();
		initBuffer( 0, type, packetid );
	}

	public ReorderMessage( int length , byte[] bytes ) {
		super( length, bytes );
	}

	public int getOrederdpacketId() {
		return getPacketId();
	}
}
