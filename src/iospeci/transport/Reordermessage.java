package iospeci.transport;

import iospeci.Buffercontent;

public class Reordermessage extends Buffercontent {
	public static final int type = -3;

	public Reordermessage( int packetid ) {
		super();
		initBuffer( 0, type, packetid );
		// bb.putInt( packetid );
	}

	public Reordermessage( int length , byte[] bytes ) {
		super( length, bytes );
	}

	public int getOrederdpacketId() {
		return getPacketId();
	}
}
