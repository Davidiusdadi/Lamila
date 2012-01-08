package correlation;

import globalstatic.Lamilastatics;
import searching.Fireworksearchstrategie;
import searching.SearchAdvisor;

public class Hashcodecorrelator implements Correlator {
	public static final int id = 0;
	private static Hashcodecorrelator inst = null;

	protected Hashcodecorrelator() {
	}

	@Override
	public int correlate( byte[] key1, byte[] key2 ) {
		return getHashDiff( key1, key2 );
	}

	public int getHashDiff( byte[] key1, byte[] key2 ) {
		int bits = 0;
		if( key1.length > key2.length ) {
			byte[] tmp = key1;
			key1 = key2;
			key2 = tmp;
			tmp = null;
			bits = key2.length - key1.length;
		}

		// if( key1.length != key2.length )
		// throw new IllegalArgumentException( "The Parameters have to be hashed the same way." );
		byte[] mask = new byte[ key1.length ];
		for( int i = 0 ; i < key1.length ; i++ )
			mask[ i ] = (byte) ( key1[ i ] ^ key2[ i ] );

		for( int i = 0 ; i < key1.length ; i++ )
			bits += Integer.bitCount( mask[ i ] );
		return bits;
	}

	@Override
	public int getConvictionCount() {
		return 5;
	}

	@Override
	public int getCorrelatorId() {
		return id;
	}

	@Override
	public SearchAdvisor getSearchAdvisor() {
		return new Fireworksearchstrategie( Lamilastatics.tsTimeout( 10000 ), 1, 3 );
	}

	public static Hashcodecorrelator getInstance() {
		if( inst == null )
			inst = new Hashcodecorrelator();
		return inst;
	}

	@Override
	public Class<?> getKeyRepresentationType() {
		return byte[].class;
	}
}
