package correlation;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import searching.SearchAdvisor;
import urlocator.Node;
import urlocator.NotImplementetException;
import urlocator.Peer;

/**
 * When using this Correlator the keys are expected to be ip addresses
 * in byteform. Therefore there is no need to correlate this value, because
 * itself already is the destination.<br>
 * This also means that searching on this layer makes no sense.<br>
 * The reason why this class exists is:<br>
 * If you want to perform an operation rather than a lookup on a {@link Node} derived object you may use its keys on specific layers or iterate through
 * them. It may simplify the logic of such operations if you access the ip
 * address in the same way like all other keys.<br>
 * In practice the search on a {@link Peer} using this correlator will simply
 * return a {@link Node} with the address of the searched key.
 */
public class IPAdressCorrelator implements Correlator {

	public static final int id = 5;
	private static IPAdressCorrelator inst = null;

	public static IPAdressCorrelator getInstance() {
		if( inst == null )
			inst = new IPAdressCorrelator();
		return inst;
	}

	@Override
	public int correlate( byte[] key1, byte[] key2 ) {
		throw new NotImplementetException( "see class decription" );
	}

	@Override
	public int getConvictionCount() {
		throw new NotImplementetException( "see class decription" );
	}

	@Override
	public int getCorrelatorId() {
		return id;
	}

	@Override
	public SearchAdvisor getSearchAdvisor() {
		throw new NotImplementetException( "see class decription" );
	}

	public static InetSocketAddress getFromKey( byte[] key ) throws UnknownHostException {
		if( key.length == 8 ) {// 4 bytes for address 4 bytes for port
			byte[] address = new byte[ 4 ];
			for( int i = 0 ; i < 4 ; i++ ) {
				address[ i ] = key[ i ];
			}
			byte[] port = new byte[ 4 ];
			for( int i = 4 ; i < 8 ; i++ ) {
				port[ i ] = key[ i ];
			}

			return new InetSocketAddress( Inet4Address.getByAddress( address ), Node.convertBytes2Int( port ) );
		} else if( key.length == 20 )// 16 bytes for address 4 bytes for port
		{
			byte[] address = new byte[ 16 ];
			for( int i = 0 ; i < 16 ; i++ ) {
				address[ i ] = key[ i ];
			}
			byte[] port = new byte[ 4 ];
			for( int i = 16 ; i < 20 ; i++ ) {
				port[ i ] = key[ i ];
			}

			return new InetSocketAddress( Inet6Address.getByAddress( address ), Node.convertBytes2Int( port ) );
		} else
			throw new IllegalArgumentException( "Wrong bytearraylenght" );

	}

	public static byte[] createKey( InetSocketAddress address ) {
		assert ( address != null );
		byte[] addressbytes = address.getAddress().getAddress();
		ByteBuffer buf = ByteBuffer.allocate( addressbytes.length + 4 );
		buf.put( addressbytes );
		buf.putInt( address.getPort() );
		return buf.array();

	}

	@Override
	public Class<?> getKeyRepresentationType() {
		return InetSocketAddress.class;
	}

}
