package iospeci;

public class SyncKey {
	private long number;

	public SyncKey( final long id ) {
		number = id;
	}

	public long getValue() {
		return number;
	}
}
