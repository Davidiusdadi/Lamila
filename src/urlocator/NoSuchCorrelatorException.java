package urlocator;

public class NoSuchCorrelatorException extends Exception {

	public NoSuchCorrelatorException() {
		super();
	}

	public NoSuchCorrelatorException( String arg0 , Throwable arg1 ) {
		super( arg0, arg1 );
	}

	public NoSuchCorrelatorException( String arg0 ) {
		super( arg0 );
	}

	public NoSuchCorrelatorException( Throwable arg0 ) {
		super( arg0 );
	}
}
