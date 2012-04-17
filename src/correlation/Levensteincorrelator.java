package correlation;

import globalstatic.Lamilastatics;
import searching.Fireworksearchstrategie;
import searching.SearchAdvisor;

public class Levensteincorrelator implements Correlator {
	public static final int id = 3;
	private static Levensteincorrelator inst = null;

	public static Levensteincorrelator getInstance() {
		if( inst == null )
			inst = new Levensteincorrelator();
		return inst;
	}

	public Levensteincorrelator() {
	}

	@Override
	public int correlate( byte[] key1, byte[] key2 ) {
		return correlate( new String( key1 ), new String( key2 ) );
	}

	public int correlate( String key1, String key2 ) {
		return getLevenshteinDistance( key1, key2 );
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
		return new Fireworksearchstrategie( Lamilastatics.tsTimeout( 10000 ), 10, 3 );
	}

	public static int getLevenshteinDistance( String string1, String string2 ) {
		if( string1 == null || string2 == null ) {
			throw new IllegalArgumentException( "Strings must not be null" );
		}

		/* The difference between this impl. and the previous is that, rather
		 * than creating and retaining a matrix of size s.length()+1 by
		 * t.length()+1, we maintain two single-dimensional arrays of length
		 * s.length()+1. The first, d, is the 'current working' distance array
		 * that maintains the newest distance cost counts as we iterate through
		 * the characters of String s. Each time we increment the index of
		 * String t we are comparing, d is copied to p, the second int[]. Doing
		 * so allows us to retain the previous cost counts as required by the
		 * algorithm (taking the minimum of the cost count to the left, up one,
		 * and diagonally up and to the left of the current cost count being
		 * calculated). (Note that the arrays aren't really copied anymore, just
		 * switched...this is clearly much better than cloning an array or doing
		 * a System.arraycopy() each time through the outer loop.)
		 * 
		 * Effectively, the difference between the two implementations is this
		 * one does not cause an out of memory condition when calculating the LD
		 * over two very large strings. */

		int string1lengh = string1.length(); // length of s
		int string2lengh = string2.length(); // length of t

		if( string1lengh == 0 ) {
			return string2lengh;
		} else if( string2lengh == 0 ) {
			return string1lengh;
		}

		int p[] = new int[ string1lengh + 1 ]; // 'previous' cost array,
		// horizontally
		int d[] = new int[ string1lengh + 1 ]; // cost array, horizontally
		int _d[]; // placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		int cost; // cost

		for( i = 0 ; i <= string1lengh ; i++ ) {
			p[ i ] = i;
		}

		for( j = 1 ; j <= string2lengh ; j++ ) {
			t_j = string2.charAt( j - 1 );
			d[ 0 ] = j;

			for( i = 1 ; i <= string1lengh ; i++ ) {
				cost = string1.charAt( i - 1 ) == t_j ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left
				// and up +cost
				d[ i ] = Math.min( Math.min( d[ i - 1 ] + 1, p[ i ] + 1 ), p[ i - 1 ] + cost );
			}

			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		}

		// our last action in the above loop was to switch d and p, so p now
		// actually has the most recent cost counts
		return p[ string1lengh ];
	}

	@Override
	public Class<?> getKeyRepresentationType() {
		return String.class;
	}

}
