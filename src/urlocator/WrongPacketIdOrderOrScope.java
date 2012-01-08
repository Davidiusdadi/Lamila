package urlocator;

public class WrongPacketIdOrderOrScope extends IllegalArgumentException {
	public WrongPacketIdOrderOrScope() {
	}

	public WrongPacketIdOrderOrScope( String arg0 ) {
		super( arg0 );
	}

	public WrongPacketIdOrderOrScope( Throwable arg0 ) {
		super( arg0 );
	}

	public WrongPacketIdOrderOrScope( String arg0 , Throwable arg1 ) {
		super( arg0, arg1 );
	}
}
