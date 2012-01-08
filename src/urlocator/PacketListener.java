package urlocator;

import iospeci.Buffercontent;
import iospeci.DirectedRequestmessage;

/**
 * A Listener for Responsemessages
 * 
 * @author David Rohmer
 */
public abstract class PacketListener {
	protected DirectedRequestmessage request;

	/**
	 * Inits this listener with the messageid of the given request.
	 */
	public PacketListener( DirectedRequestmessage request ) {
		this.request = request;
	}

	/**
	 * Called when a message this listener is listening for arrives.
	 */
	public abstract void packetRetrive( Buffercontent message );

	/**
	 * Returns if the Listener is still listening for packages.
	 * If it returns false packetRetrive() wont be called anymore
	 */
	public abstract boolean isResponseRetivementCommplete();

	/**
	 * Returns the message id this listener is listening for.
	 */
	public int desiredMessageId() {
		return request.getMessageId();
	}
}
