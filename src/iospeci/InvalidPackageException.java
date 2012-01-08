package iospeci;

/**
 * This Exception is thrown if a packet is maleformed.
 * 
 * @author David Rohmer
 * 
 */
public class InvalidPackageException extends RuntimeException {

	public InvalidPackageException() {

	}

	public InvalidPackageException( String arg0 ) {
		super( arg0 );
	}

	public InvalidPackageException( Throwable arg0 ) {
		super( arg0 );
	}

	public InvalidPackageException( String arg0 , Throwable arg1 ) {
		super( arg0, arg1 );
	}

}
