package iospeci;

public abstract class SOptainer<T> extends SProvider {

	public SOptainer( int classid , Class<T> subclass ) {
		super( classid, subclass );
	}

	public SOptainer( Class<T> subclass ) {
		super( subclass );
	}

	public abstract T optain( SyncKey id ) throws MissingDataException;

}
