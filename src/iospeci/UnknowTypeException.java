package iospeci;

public class UnknowTypeException extends RuntimeException {

	public UnknowTypeException() {
		super();
	}

	public UnknowTypeException( String arg0 , Throwable arg1 ) {
		super( arg0, arg1 );
	}

	public UnknowTypeException( String arg0 ) {
		super( arg0 );
	}

	public UnknowTypeException( Throwable arg0 ) {
		super( arg0 );
	}

}
