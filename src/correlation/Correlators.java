package correlation;

import java.util.Collection;
import java.util.HashMap;

/**
 * This class serves as a global registry for {@link Correlator}s.<br>
 * But this class is also indented to be used as user instanced container for of {@link Correlator}.<br>
 * It is not possible to create and fill up your own Correlators instance without also filling up the global registry.<br>
 * <br>
 * Since it is easier to deal with {@link Correlator} id's than with {@link Correlator} objects this class can be used to reliable resolve a {@link Correlator} by its id.
 * Therefore it is not possible to register 2 {@link Correlator} with the same id.
 **/
public class Correlators {
	private static Correlators global_correlators = new Correlators();

	private HashMap<Integer,Correlator> bucket = new HashMap<Integer,Correlator>();

	/** Will create a empty Correlators instance. */
	public Correlators() {
	}

	/**
	 * Makes a {@link Correlator} available for global lookup by its <code>id</code> and makes sure that there is only one correlator with this id.<br>
	 * Registering the same {@link Correlator} twice will have no effect.
	 * 
	 * @throws DublicateCorrelatorIdException
	 *             if there is already an other {@link Correlator} registered with the same id.
	 */
	// TODO There should more checks regarding the signature of a given Correlator. E.g a singeleton interface( no public constructors and a get)
	public static void registerCorrelator( Correlator c ) throws DublicateCorrelatorIdException {
		if( c == null )
			throw new IllegalArgumentException( "given Correlator must not be null" );

		Correlator other = global_correlators.getCorrelator( c.getCorrelatorId() );
		if( other != null && other != c )
			throw new IllegalArgumentException( "The Correlators " + c + " and other  have the same id." );
		global_correlators.putCorrelator( c );
	}
	/** Returns if there has been registered a correlator with the given id. */
	public static boolean isSuchCorrelatorRegistered( int cid ) {
		return global_correlators.containsSuchCorrelator( cid );
	}

	/** Returns if the given correlator is in this "Container". */
	public boolean containsThisCorrelator( Correlator c ) {
		Correlator other = bucket.get( c.getCorrelatorId() );
		return c == other;
	}

	/**
	 * Allows to resolve a correlator considering all correlator's that have been ever registered.<br>
	 * Please note that {@link #putCorrelator(Correlator)} also registers {@link Correlator}s
	 */
	public static Correlator resolveCorrelator( int layer ) {
		return global_correlators.getCorrelator( layer );
	}

	/** Returns if this correlator contains a correlator with given id */
	public boolean containsSuchCorrelator( int cid ) {
		return bucket.get( cid ) != null;
	}

	/** Puts the given correlator on this container and registers is using {@link #registerCorrelator(Correlator)} */
	public void putCorrelator( Correlator c ) throws DublicateCorrelatorIdException {
		if( c == null )
			throw new IllegalArgumentException( "given Correlator must not be null" );

		if( getCorrelator( c.getCorrelatorId() ) != null )
			throw new IllegalArgumentException( this + " allready contains a correlator with the id " + c.getCorrelatorId() );

		if( this != global_correlators ) { // to prevent recursion
			if( !isSuchCorrelatorRegistered( c.getCorrelatorId() ) )
				registerCorrelator( c );
		}
		bucket.put( c.getCorrelatorId(), c );
	}

	/** Removes the correlator with the given id from this container */
	public Correlator removeCorrelator( int lid ) {
		return bucket.remove( lid );
	}

	/** Allows to resolve correlator using all correlator's put on this container */
	public Correlator getCorrelator( int layer ) {
		return bucket.get( layer );
	}

	public Collection<Correlator> getCorrelators() {
		return bucket.values();
	}

	/** This exception will be thrown if there are two different {@link Correlator}s with the same <code>id</code> */
	class DublicateCorrelatorIdException extends RuntimeException {

		public DublicateCorrelatorIdException() {
			super();
		}

		public DublicateCorrelatorIdException( String arg0 , Throwable arg1 ) {
			super( arg0, arg1 );
		}

		public DublicateCorrelatorIdException( String arg0 ) {
			super( arg0 );
		}

		public DublicateCorrelatorIdException( Throwable arg0 ) {
			super( arg0 );
		}

	}
}
