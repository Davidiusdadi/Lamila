package urlocator;

public class NotImplementedException extends RuntimeException {
	public NotImplementedException() {
	}

	public NotImplementedException( final String arg0 ) {
		super( arg0 );
	}

	public NotImplementedException( final Throwable arg0 ) {
		super( arg0 );
	}

	public NotImplementedException( final String arg0 , final Throwable arg1 ) {
		super( arg0, arg1 );
	}

	public static void criticalImplementionWarning( final String message ) {
		Throwable dep = new Throwable( message );
		dep.printStackTrace();
	}
}
