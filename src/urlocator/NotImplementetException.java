package urlocator;

public class NotImplementetException extends RuntimeException {
	public NotImplementetException() {
	}

	public NotImplementetException( final String arg0 ) {
		super( arg0 );
	}

	public NotImplementetException( final Throwable arg0 ) {
		super( arg0 );
	}

	public NotImplementetException( final String arg0 , final Throwable arg1 ) {
		super( arg0, arg1 );
	}

	public static void criticalImplementionWarning( final String message ) {
		Throwable dep = new Throwable( message );
		dep.printStackTrace();
	}
}
