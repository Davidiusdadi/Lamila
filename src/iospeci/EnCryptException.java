package iospeci;

public class EnCryptException extends RuntimeException {

	public EnCryptException() {
	}

	public EnCryptException( String arg0 ) {
		super( arg0 );
	}

	public EnCryptException( Throwable arg0 ) {
		super( arg0 );
	}

	public EnCryptException( String arg0 , Throwable arg1 ) {
		super( arg0, arg1 );
	}

}
