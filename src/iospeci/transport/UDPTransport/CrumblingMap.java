package iospeci.transport.UDPTransport;

import iospeci.Buffercontent;
import iospeci.UnknowTypeException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This map will nerver contain more than an implementation specifc count of
 * elements.
 */
public class CrumblingMap<K, V> {
	protected Map<K,V> map;
	protected LinkedList<K> order;
	protected final int maxelements;

	public CrumblingMap( int maxelements ) {
		this.maxelements = maxelements;
		map = new HashMap<K,V>();
		order = new LinkedList<K>();
	}

	public void put( K key, V value ) {
		map.put( key, value );
		order.add( key );
		if( order.size() > maxelements )
			map.remove( order.remove( 0 ) );
	}

	public V get( K key ) {
		return map.get( key );
	}

	public void clear() {
		map.clear();
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
