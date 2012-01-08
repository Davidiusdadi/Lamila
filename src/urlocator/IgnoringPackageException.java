package urlocator;

/**
 * Thrown if the a request cant be responded. This may be the case if the
 * requested data is not hostet or the incomming message is for example an
 * response to something that was not requested.
 */
public class IgnoringPackageException extends Exception {
	public IgnoringPackageException() {
		super();
	}

	public IgnoringPackageException( String message , Throwable cause ) {
		super( message, cause );
	}

	public IgnoringPackageException( String message ) {
		super( message );
	}

	public IgnoringPackageException( Throwable cause ) {
		super( cause );
	}
}
