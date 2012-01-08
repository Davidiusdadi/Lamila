package iospeci.transport.UDPTransport;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class DefConstrMap<K, V> {
	Map<K,V> map;
	Constructor<V> constr;

	public DefConstrMap( Constructor constr ) {
		if( constr.getGenericParameterTypes().length != 0 )
			throw new IllegalArgumentException( "Defaultconstructor expectet" );
		map = new HashMap<K,V>();
		this.constr = constr;
	}

	public DefConstrMap( Constructor constr , Map<K,V> map ) {
		if( constr.getGenericParameterTypes().length != 0 )
			throw new IllegalArgumentException( "Defaultconstructor expectet" );
		this.map = map;
		this.constr = constr;
	}

	public V get( K key ) {
		V value = map.get( key );
		return value;
	}

	public V require( K key ) {
		V value = map.get( key );
		if( value == null )
			try {
				value = constr.newInstance();
				put( key, value );
			} catch ( Exception e ) {
				throw new RuntimeException( "Wrong Constructor delivered to this Instance", e );
			}
		return value;
	}

	public void put( K key, V value ) {
		map.put( key, value );
	}
}
