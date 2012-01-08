package urlocator;

/**
 * Thrown if a packet arrives that do not match any Messagespecification.
 * 
 * @see iospeci Lokation of the Messagespecifications
 * @author David Rohmer
 * 
 */
public class UnknowPacketTypeException extends RuntimeException {

	public UnknowPacketTypeException() {
		super();
	}

	public UnknowPacketTypeException( String arg0 , Throwable arg1 ) {
		super( arg0, arg1 );
	}

	public UnknowPacketTypeException( String arg0 ) {
		super( arg0 );
	}

	public UnknowPacketTypeException( Throwable arg0 ) {
		super( arg0 );
	}

}
