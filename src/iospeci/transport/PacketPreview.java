package iospeci.transport;

import iospeci.Buffercontent;

/**
 * Represents the beginn of a response if the response in made up of more than
 * one package
 * 
 * @author David Rohmer
 * 
 */
public class PacketPreview extends Buffercontent {
	public static final int type = -1;

	public PacketPreview( int messagecount , int startid ) {
		initBuffer( 4, type, startid );
		bb.putInt( messagecount );
	}

	public PacketPreview( int length , byte[] bytes ) {
		super( length, bytes );
	}

	/**
	 * Returns how many responses will follow after this preview
	 */
	public int getIncommingMessageCount() {
		return bb.getInt( getBufferoffset() );
	}

	/**
	 * Returns the packetid of the first packet which will arrive in this
	 * Transfer. The packetid of packet n will be getFirstPacketId() + n
	 */
	public int getPreviewedId() {
		return super.getPacketId();
	}

	@Override
	public String toString() {
		return super.toString() + " count: " + getIncommingMessageCount();
	}
}
