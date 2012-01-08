package correlation;

import globalstatic.Lamilastatics;
import searching.Fireworksearchstrategie;
import searching.SearchAdvisor;

public class MetaCorellator implements Correlator {

	public static final String keywords = "keywords";
	public static final int id = 1;
	private static MetaCorellator inst;

	protected MetaCorellator() {
	}

	@Override
	public int correlate( byte[] key1, byte[] key2 ) {
		String keys1 = new String( key1, Lamilastatics.charset );
		String keys2 = new String( key2, Lamilastatics.charset );
		return getTagsDiff( keys1, keys2 );
	}

	public static int getTagsDiff( String tagsstring, String tagsstring2 ) {
		String[] tags = tagsstring.length() < tagsstring2.length() ? tagsstring.split( "," ) : tagsstring2.split( "," );
		String[] tags2 = tagsstring.length() >= tagsstring2.length() ? tagsstring.split( "," ) : tagsstring2.split( "," );
		String[][] sortedtags = new String[ tags.length ][ tags2.length ];

		int totaldiff = 0;
		int[] tagdiffs = new int[ tags.length ];
		int fittingindex;
		int fittingdiff;
		for( int i = 0 ; i < tags.length ; i++ ) {
			fittingindex = Integer.MAX_VALUE;
			fittingdiff = Integer.MAX_VALUE;
			for( int j = 0 ; j < tags2.length ; j++ ) {
				int curdiff = Levensteincorrelator.getLevenshteinDistance( tags[ i ], tags2[ j ] );
				if( curdiff < fittingdiff ) {
					fittingdiff = curdiff;
					fittingindex = j;
				}

			}
			tagdiffs[ i ] = fittingdiff;
			totaldiff += fittingdiff;
		}
		totaldiff += ( tags2.length - tags.length ) * 2;
		return totaldiff;
	}

	@Override
	public int getConvictionCount() {
		return 4;
	}

	@Override
	public int getCorrelatorId() {
		return id;
	}

	@Override
	public SearchAdvisor getSearchAdvisor() {
		return new Fireworksearchstrategie( Lamilastatics.tsTimeout( 10000 ), 1, 3 );
	}

	public static MetaCorellator getInstance() {
		if( inst == null )
			inst = new MetaCorellator();
		return inst;
	}

	@Override
	public Class<?> getKeyRepresentationType() {
		return String.class;
	}

}
