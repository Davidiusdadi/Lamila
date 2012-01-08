package iospeci.transport.UDPTransport;

import globalstatic.Lamilastatics;
import iospeci.Buffercontent;
import iospeci.UnknowTypeException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Timer;

/**
 * This map will nerver contain more than an implementation specifc count of
 * elements. The removal of elements by timeout is not implemented.
 */
class CrumblingMap<K, V> {
	private static Timer timer;
	public static int defaulttimeout = Lamilastatics.tsTimeout( 5000 );
	private HashMap<K,V> map;
	private LinkedList<K> order;
	private int timeout;

	static void setTimer( Timer timer ) {
		CrumblingMap.timer = timer;
	}

	public CrumblingMap() {
		if( timer == null )
			throw new IllegalStateException( "Please call CrumblingMap.setTimer once before" );
		this.timeout = defaulttimeout;
		map = new HashMap<K,V>();
		order = new LinkedList<K>();
	}

	public CrumblingMap( int timeout ) {
		if( timer == null )
			throw new IllegalStateException( "Please call CrumblingMap.setTimer once before" );
		this.timeout = timeout;
		map = new HashMap<K,V>();
		order = new LinkedList<K>();
	}

	public void put( K key, V value ) {
		// Task t = new Task( key , value );
		// timer.schedule( t , timeout , Long.MAX_VALUE );
		map.put( key, value );
		order.add( key );
		if( order.size() > 10 )
			map.remove( order.remove( 0 ) );
	}

	V get( K key ) {
		return map.get( key );
	}

	@Override
	public String toString() {
		try {
			if( !order.isEmpty() && map.get( order.getFirst() ) instanceof byte[] ) {
				StringBuilder text = new StringBuilder( 200 );
				text.append( "{" + getClass().getSimpleName() + ": " );

				for( Entry<K,V> o : map.entrySet() ) {
					byte[] b = (byte[]) o.getValue();

					try {
						Buffercontent buf = Buffercontent.convert( null, b.length, b );
						text.append( "  " );
						text.append( buf );
					} catch ( UnknowTypeException e ) {
						text.append( "unknow bytevalue" );
					}
				}
				text.append( "}" );
				return text.toString();
			}
			return map.toString();
		} finally {

		}

	}
}
