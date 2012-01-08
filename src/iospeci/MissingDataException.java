package iospeci;

public class MissingDataException extends Exception {

	public MissingDataException( String arg0 ) {
		super( arg0 );
	}

	public MissingDataException() {
	}

	public MissingDataException( String arg0 , Throwable arg1 ) {
		super( arg0, arg1 );
	}

	public MissingDataException( Throwable arg0 ) {
		super( arg0 );
	}
}
