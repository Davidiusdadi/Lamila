package iospeci;

/**
 * This Exception is thrown if packets arrive in the wrong order or count.
 * 
 * @author David Rohmer
 * 
 */
public class CommunicationException extends RuntimeException {

	public CommunicationException() {
		super();
	}

	public CommunicationException( String arg0 , Throwable arg1 ) {
		super( arg0, arg1 );
	}

	public CommunicationException( String arg0 ) {
		super( arg0 );
	}

	public CommunicationException( Throwable arg0 ) {
		super( arg0 );
	}

}
