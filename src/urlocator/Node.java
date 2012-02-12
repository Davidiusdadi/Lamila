package urlocator;

import globalstatic.Lamilastatics;
import globalstatic.Lamilastatics.OutputKind;
import iospeci.InvalidPackageException;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import correlation.Correlator;
import correlation.Hashcodecorrelator;
import correlation.MetaCorellator;

/**
 * Represents one node in the network AND the view of a node in the network. If
 * you are not the provider of this node you will probably never know its real
 * state. All changes of this object will not change the network. Changes to the
 * network will only be made trough the {@link Peer}.
 * 
 * @author David Rohmer
 */
public abstract class Node {

	private InetSocketAddress adress;
	private final Map<Integer,byte[]> key;// TODO make the nodes keys sorted. This could be important for Syncpaths who order their identifers
	private final Map<Integer,List<Link>> links;

	public static String hashMD5String( String message ) {
		try {
			MessageDigest md = MessageDigest.getInstance( "MD5" );
			return hex( md.digest( message.getBytes( "CP1252" ) ) );
		} catch ( Exception e ) {
		}
		return null;
	}

	public static String hex( byte[] array ) {
		StringBuffer sb = new StringBuffer();
		for( int i = 0 ; i < array.length ; ++i ) {
			sb.append( Integer.toHexString( ( array[ i ] & 0xFF ) | 0x100 ).toLowerCase().substring( 1, 3 ) );
		}
		return sb.toString();
	}

	public static byte[] hashMD5Raw( String s ) {
		return hashMD5( s.getBytes( Lamilastatics.charset ) );
	}

	public static byte[] hashMD5( byte b[] ) {
		MessageDigest dig;
		try {
			dig = MessageDigest.getInstance( "MD5" );
		} catch ( NoSuchAlgorithmException e ) {
			e.printStackTrace();
			throw new RuntimeException( e );
		}
		return dig.digest( b );
	}

	public static byte[] hashMD5( byte b[], long id ) {
		MessageDigest dig;
		try {
			dig = MessageDigest.getInstance( "MD5" );
		} catch ( NoSuchAlgorithmException e ) {
			e.printStackTrace();
			throw new RuntimeException( e );
		}
		dig.update( b );
		dig.update( Node.convertLongToByteArray( id ) );
		return dig.digest();
	}

	public static byte[] convertIntToByteArray( int val ) {
		byte[] buffer = new byte[ 4 ];

		buffer[ 0 ] = (byte) ( val >>> 24 );
		buffer[ 1 ] = (byte) ( val >>> 16 );
		buffer[ 2 ] = (byte) ( val >>> 8 );
		buffer[ 3 ] = (byte) val;

		return buffer;
	}

	public static long convertBytes2Long( byte[] ba ) {
		return new BigInteger( ba ).longValue();
	}

	public static int convertBytes2Int( byte[] ba ) {// TODO care about signed and unsigned on 2 byte arrays
		return new BigInteger( ba ).intValue();
	}

	public static byte[] convertLongToByteArray( long l ) {
		return new byte[]{ (byte) ( ( l >> 56 ) & 0xff ), (byte) ( ( l >> 48 ) & 0xff ), (byte) ( ( l >> 40 ) & 0xff ), (byte) ( ( l >> 32 ) & 0xff ), (byte) ( ( l >> 24 ) & 0xff ), (byte) ( ( l >> 16 ) & 0xff ), (byte) ( ( l >> 8 ) & 0xff ), (byte) ( ( l >> 0 ) & 0xff ), };
	}

	public static String convertByteArrayToHexString( byte in[] ) {
		byte c = 0;
		char[] out = new char[ in.length * 2 ];

		char[] chars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		int index = out.length;
		int lastnonnull = index - 1;
		for( int i = in.length - 1 ; i != -1 ; i-- ) {
			c = (byte) ( in[ i ] & 0x0F );
			out[ --index ] = chars[ (short) c ];

			if( c != 0 )
				lastnonnull = index;

			c = (byte) ( in[ i ] & 0xF0 );
			c = (byte) ( c >>> 4 );
			c = (byte) ( c & 0x0F );// cut of the -

			out[ --index ] = chars[ (short) c ];

			if( c != 0 )
				lastnonnull = index;

		}
		return new String( out, lastnonnull, out.length - lastnonnull );
	}

	public static byte[] convertHexStringToByteArray( String hex ) {
		if( hex.length() % 2 != 0 ) {// TODO Not use HexBinaryAdapter to convert
			hex = "0" + hex;
		}
		HexBinaryAdapter adapter = new HexBinaryAdapter();
		byte[] bytes = adapter.unmarshal( hex );
		return bytes;
	}

	public static byte[] convertHexLongToByteArray( String hex ) {
		return convertLongToByteArray( Long.valueOf( hex, 16 ) );
	}

	public Node( InetSocketAddress adress ) {
		if( adress == null || adress.isUnresolved() )
			throw new IllegalArgumentException( "adress cant be null or Unresolved" );
		this.adress = adress;
		this.key = (Map<Integer,byte[]>) Collections.synchronizedMap( new HashMap<Integer,byte[]>() );
		links = (Map<Integer,List<Link>>) Collections.synchronizedMap( new HashMap<Integer,List<Link>>() );
		// putKey( Hashcodecorrelator.id , nodeid );
	}

	/**
	 * Copys the given node flat.
	 */
	public Node( Node tocopy ) {
		key = tocopy.key;
		links = tocopy.links;
		adress = tocopy.adress;
	}

	protected Node() {
		this.adress = null;
		this.key = (Map<Integer,byte[]>) Collections.synchronizedMap( new HashMap<Integer,byte[]>() );
		links = (Map<Integer,List<Link>>) Collections.synchronizedMap( new HashMap<Integer,List<Link>>() );
		// putKey( Hashcodecorrelator.id , nodeid );
	}

	/**
	 * Returns the host of this node
	 */
	public byte[] getHost() {
		return adress.getAddress().getAddress();
	}

	/**
	 * Returns the port of this node
	 **/
	public int getPort() {
		return adress.getPort();
	}

	/**
	 * Returns the address of the peer this node belongs to. Shall be used only
	 * for lazy initializing of the address. So it throws a {@link IllegalStateException} if the internal address is already set.
	 * All parameters have to be non null.
	 * 
	 * @throws IllegalStateException
	 * @throws IllegalArgumentException
	 * 
	 **/
	public void setAdress( InetSocketAddress adress ) {
		if( adress == null )
			throw new IllegalArgumentException();
		if( this.adress != null )
			throw new IllegalStateException( "Adress already set" );
		this.adress = adress;

	}

	/** Returns the address or null if the address has not yet been set. */
	public InetSocketAddress getAdress() {// TODO Create a mechanism which updates the address on change
		return adress;
	}

	/**
	 * Returns the (known) Links of this node on the given correlationidlayer.
	 */
	public List<Link> getLinks( Integer layer ) {
		List<Link> layerlinks = links.get( layer );
		if( layerlinks == null ) {
			layerlinks = new LinkedList<Link>();
			links.put( layer, layerlinks );
		}
		return layerlinks;
	}

	/**
	 * Adds a link on the given layer.
	 */
	public void addLink( Integer layer, Node node ) {
		if( node.getKey( layer ) == null )
			throw new IllegalArgumentException( "The node does not define the given Layerkez" );
		List<Link> layerlinks = links.get( layer );
		if( layerlinks == null ) {
			layerlinks = new LinkedList<Link>();
			links.put( layer, layerlinks );
		}
		int index;
		if( ( index = layerlinks.indexOf( node ) ) != -1 ) {
			Lamilastatics.println( OutputKind.UNKNOW, "Node schon vorhanden" );
			layerlinks.get( index ).update();
			return;
		}
		layerlinks.add( new Link( node ) );
	}

	/**
	 * Removes a link on the given layer.
	 */
	public boolean removeLink( Integer layer, Node node ) {
		List<Link> layerlinks = links.get( layer );
		if( layerlinks == null )
			return false;
		return layerlinks.remove( node );
	}

	/**
	 * Sets the key of this node on the given layer
	 */
	protected void putKey( int correlatorid, byte[] key ) {
		this.key.put( correlatorid, key );
	}

	/**
	 * Sets the key of this node on the given layer
	 */
	protected void putKey( int correlatorid, String key ) {
		this.key.put( correlatorid, key.getBytes( Lamilastatics.charset ) );
	}

	/**
	 * Gets the key of this node on the given layer. If there is no value null
	 * will be returned.
	 */
	public byte[] getKey( Integer layer ) {
		return key.get( layer );
	}

	/**
	 * Gets the key of this node on the given layer
	 */
	public byte[] getKey( Correlator layer ) {
		return key.get( layer.getCorrelatorId() );
	}

	@Override
	public String toString() {
		byte[] key = getKey( MetaCorellator.id );
		String name = new String( getUniqueId(), Lamilastatics.charset );// (key==null?new
		// String(getNodeIdent(),
		// Buffercontent.cset):new
		// String(key,
		// Buffercontent.cset));
		String host = "";
		try {
			host = InetAddress.getByAddress( getHost() ).getHostAddress();
		} catch ( UnknownHostException e ) {
			host = "could not be resolved";
		}
		return getClass().getName() + " id: " + convertBytes2Long( getUniqueId() ) + " host: " + host + " port: " + getPort();
	}

	/**
	 * Gets the id of this node in the network
	 */
	public byte[] getUniqueId() {
		return getKey( Hashcodecorrelator.id );
	}

	/** Calls {@link Arrays#hashCode(byte[])} with the nodeid as parameter. */
	@Override
	public int hashCode() {
		return Arrays.hashCode( getUniqueId() );
	}

	/**
	 * Checks if the hashcodes are equal.
	 **/
	@Override
	public boolean equals( Object a ) {
		return new Integer( hashCode() ).equals( a.hashCode() );// see hashCode() above
	}

	boolean isValid() {
		return adress != null && getUniqueId() != null;
	}

	/**
	 * Returns a new list, containing the layerid this node can be published at<br>
	 * The list is not ordered in a specific way.
	 */
	public LinkedList<Integer> layers() {
		return new LinkedList<Integer>( key.keySet() );
	}

	/**
	 * Returns an array with length <code>length</code> and the value <code>bytes</code>. The added bytes will have the value 0.
	 */
	public static byte[] expandByteArray( byte[] bytes, final int length ) throws InvalidPackageException {
		if( bytes.length > length )
			throw new IllegalArgumentException();
		if( bytes.length == length )
			return bytes;
		ByteBuffer buffer = ByteBuffer.allocate( bytes.length > length ? bytes.length : length );
		buffer.position( buffer.limit() - bytes.length );
		buffer.put( bytes );
		return buffer.array();
	}
}
