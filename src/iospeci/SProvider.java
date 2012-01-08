package iospeci;

import java.lang.reflect.Field;

public class SProvider<T> {
	protected int classid;
	private Class<? extends T> theclass;

	public SProvider( final int classid , final Class<? extends T> subclass ) {
		if( subclass == null )
			throw new IllegalArgumentException( "Parameter subclass can't be null" );
		construct( classid, subclass );
	}

	public SProvider( Class<T> subclass ) {
		if( subclass == null )
			throw new IllegalArgumentException( "Parameter subclass can't be null" );
		try {
			Field f = subclass.getField( "type" );
			Integer id = (Integer) f.get( null );
			construct( id, subclass );
		} catch ( SecurityException e ) {
			e.printStackTrace();
			throw new RuntimeException( e );
		} catch ( Exception e ) {
			throw new IllegalArgumentException( "Parameter subclass has to define a static final int type", e );
		}

	}

	private void construct( final int classid, final Class<? extends T> subclass ) {
		this.classid = classid;
		theclass = subclass;
	}

	public final int getTypeId() {
		return classid;
	}

	public String getTypeName() {
		return theclass.getCanonicalName();
	}

	public Class<? extends T> getProvidingClass() {
		return theclass;
	}

	@Override
	public int hashCode() {
		return getTypeId();
	}

	@Override
	public String toString() {
		return getClass().getName() + ":{" + getTypeName() + ", " + getTypeId() + "}";
	}
}
