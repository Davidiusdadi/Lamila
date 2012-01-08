package iospeci;

/**
 * This Exception is thrown if an array may be that long that it could cause an
 * OutOfMemoryError.
 * 
 * @author David Rohmer
 */
public class CriticalByteArreyLenghtException extends RuntimeException {

	public CriticalByteArreyLenghtException() {
		super();
	}

	public CriticalByteArreyLenghtException( String arg0 , Throwable arg1 ) {
		super( arg0, arg1 );
	}

	public CriticalByteArreyLenghtException( String arg0 ) {
		super( arg0 );
	}

	public CriticalByteArreyLenghtException( Throwable arg0 ) {
		super( arg0 );
	}

}
