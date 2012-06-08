package iospeci;

import globalstatic.Lamilastatics;
import iospeci.transport.OpenIdRange;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

public abstract class Buffercontent {
	private static Integer ident = 0;
	private static HashMap<Integer,SProvider<? extends Buffercontent>> optainers = new HashMap<Integer,SProvider<? extends Buffercontent>>();
	public static final byte NO_ENCRYPTION = 0;
	public static final byte BLOWFISH_ENCRYPTION = 1;
	public static final byte RSA_ENCRYPTION = 2;

	public static void addOptainer( final SProvider<? extends Buffercontent> optainer ) {
		SProvider<? extends Buffercontent> s = optainers.get( optainer.getTypeId() );
		if( s != null )
			throw new IllegalArgumentException( "The type " + s + "and the type " + optainer + " are holding the same classid(SOptainer.classid)" );
		optainers.put( optainer.getTypeId(), optainer );
	}

	public static boolean addOptainer( Class<? extends Buffercontent> toprovide ) {
		SProvider<? extends Buffercontent> optainer = new SProvider( toprovide );
		SProvider<? extends Buffercontent> s = optainers.get( optainer.getTypeId() );
		if( s != null && toprovide == optainer.getProvidingClass() ) {
			throw new IllegalArgumentException( "The type " + s + "and the type " + optainer + " are holding the same classid(SOptainer.classid)" );
		}
		optainers.put( optainer.getTypeId(), optainer );
		return true;
	}

	public static Class<? extends Buffercontent> getMessaetypeById( int mesagetype ) {
		SProvider<? extends Buffercontent> s = optainers.get( mesagetype );
		if( s == null )
			throw new UnknowTypeException( "Id " + mesagetype + " is unknow" );
		return s.getProvidingClass();
	}

	public static Buffercontent convert( InetSocketAddress from, int length, byte[] bytes ) throws UnknowTypeException {
		Integer mesagetype = extractInt( bytes, 4 + 1 );
		SProvider<? extends Buffercontent> optainer = optainers.get( mesagetype );
		Buffercontent buf = null;
		if( optainer == null )
			throw new UnknowTypeException( "unknow id " + mesagetype );
		try {
			buf = optainer.getProvidingClass().getConstructor( int.class, byte[].class ).newInstance( length, bytes );
		} catch ( Exception e ) {
			throw new UnknowTypeException( optainer.getProvidingClass() + " has no Constructor with the parameters int and byte[]", e );
		}

		if( buf instanceof DirectedRequestmessage )
			( (DirectedRequestmessage) buf ).setFrom( from );
		return buf;
	}

	public static int getNextId() {
		synchronized ( ident ) {
			return ident++;
		}
	}

	public static int getEncryptionOffset() {
		return 1;
	}

	public static int extractInt( byte[] buffer, int offset ) {
		int value = ( 0xFF & buffer[ offset ] ) << 24;
		value |= ( 0xFF & buffer[ offset + 1 ] ) << 16;
		value |= ( 0xFF & buffer[ offset + 2 ] ) << 8;
		value |= ( 0xFF & buffer[ offset + 3 ] );
		return value;
	}

	public static OpenRange reserveIds( int count ) {
		if( count <= 0 )
			throw new IllegalArgumentException();
		int startid;
		synchronized ( ident ) {
			startid = ident;
			ident += count;
		}
		return new OpenIdRange( startid, count );
	}

	public static int extractRealLength( byte[] bytes ) {
		if( bytes.length <= 9 )
			throw new IllegalArgumentException();
		return extractInt( bytes, 9 );
	}

	public static byte getCryption( byte[] bytes ) {
		return bytes[ 0 ];
	}

	public static void writeObject( final ByteBuffer bb, final Object value ) {
		if( value instanceof Integer ) {
			bb.putInt( 0 );
			bb.putInt( Integer.SIZE / 8 );
			bb.putInt( (Integer) value );
		} else if( value instanceof String ) {
			byte[] valuebytes = ( (String) value ).getBytes( Lamilastatics.charset );
			bb.putInt( 1 );
			bb.putInt( valuebytes.length );
			bb.put( valuebytes );
		} else if( value instanceof Double ) {
			bb.putInt( 2 );
			bb.putInt( Double.SIZE / 8 );
			bb.putDouble( (Double) value );
		} else if( value instanceof Long ) {
			bb.putInt( 3 );
			bb.putInt( Long.SIZE / 8 );
			bb.putLong( (Long) value );
		} else if( value instanceof Short ) {
			bb.putInt( 4 );
			bb.putInt( Short.SIZE / 8 );
			bb.putShort( (Short) value );
		} else if( value instanceof Character ) {
			bb.putInt( 5 );
			bb.putInt( Character.SIZE / 8 );
			bb.putChar( (Character) value );
		} else if( value instanceof byte[] ) {
			byte[] valuebytes = ( (byte[]) value );
			bb.putInt( 6 );
			bb.putInt( valuebytes.length );
			bb.put( (byte[]) value );
			assert ( bb.position() == bb.capacity() );
		} /*
			* else if ( value instanceof WtField ) { WtField container = ( WtField ) value; bb.putInt( 7 ); bb.putInt( Integer.SIZE / 8 ); bb.putInt( container.size() ); assert ( bb.position() == bb.capacity() ); }
			*/else {
			throw new IllegalArgumentException( "No rule to send data of type" + value.getClass() );
		}
	}

	public static int calculateObjectSize( final Object value ) {
		int size = 8;
		if( value instanceof Integer ) {
			return size + Integer.SIZE / 8;
		} else if( value instanceof String ) {
			byte[] valuebytes = ( (String) value ).getBytes( Lamilastatics.charset );
			return size + valuebytes.length;
		} else if( value instanceof Double ) {
			return size + Double.SIZE / 8;
		} else if( value instanceof Long ) {
			return size + Long.SIZE / 8;
		} else if( value instanceof Short ) {
			return size + Short.SIZE / 8;
		} else if( value instanceof Character ) {
			return size + Character.SIZE / 8;
		} else if( value instanceof byte[] ) {
			byte[] valuebytes = ( (byte[]) value );
			return size + valuebytes.length;
		} /*
			* else if ( value instanceof WtField ) { WtField container = ( WtField ) value; return size + Integer.SIZE / 8; }
			*/else {
			throw new IllegalArgumentException( "No rule to encode object data of type" + ( value != null ? value.getClass() : "null" ) );
		}
	}

	public static Object readObject( final ByteBuffer bb ) {
		int typeid = bb.getInt();
		int len = bb.getInt();

		// valuebytes = new byte[)];
		// bb.get(valuebytes);

		switch ( typeid ) {
			case 0:// Integer
				Buffercontent.validateArLen( len, Integer.SIZE / 8, Integer.SIZE / 8 );
				return bb.getInt();
			case 1:// String
				byte[] stringbytes = new byte[ len ];
				bb.get( stringbytes );
				return new String( stringbytes, Lamilastatics.charset );
			case 2:// Double
				Buffercontent.validateArLen( len, Double.SIZE / 8, Double.SIZE / 8 );
				return bb.getDouble();
			case 3:// Long
				Buffercontent.validateArLen( len, Long.SIZE / 8, Long.SIZE / 8 );
				return bb.getLong();
			case 4:// Short
				Buffercontent.validateArLen( len, Short.SIZE / 8, Short.SIZE / 8 );
				return bb.getShort();
			case 5:// Character
				Buffercontent.validateArLen( len, Character.SIZE / 8, Character.SIZE / 8 );
				return bb.getChar();
			case 6:// byte[]
				byte[] bytes = new byte[ len ];
				bb.get( bytes );
				return bytes;
				/*
				 * case 7 :// AbstractContainer Buffercontent.validateArLen( len , Integer.SIZE / 8 , Integer.SIZE / 8 );
				 * 
				 * if (tmpl instanceof MinimalListTemplate) { return new RemoteList<Object>(bb.getInt(), (MinimalListTemplate) tmpl); }
				 * 
				 * 
				 * return new WtContainer( bb.getInt() );
				 */
			default :
				throw new InvalidPackageException( "typeid " + typeid + " is unknow" );
				// break;
		}
	}

	private final int bufferoffset = 9 + 4;

	protected int getBufferoffset() {
		return bufferoffset;
	}

	protected ByteBuffer bb;
	private static final int maxlength = 1000;

	/**
	 * To make the initialisation complete, you have to call {@link Buffercontent#initBuffer(int, int)}
	 */
	public Buffercontent() {
	}

	protected final void initBuffer( int size, int messagetype ) {
		initBuffer( size, messagetype, getNextId() );
	}

	protected final void initBuffer( int size, int messagetype, OpenRange range ) {
		initBuffer( size, messagetype, range.popId() );
	}

	protected void initBuffer( int size, int messagetype, int packetid ) {
		try {
			assert ( getClass().getField( "type" ).getInt( null ) == messagetype ) : "Please define static int type= a unique numer";
		} catch ( Exception e ) {
			throw new AssertionError( e );
		}
		int legth = size + bufferoffset;
		bb = ByteBuffer.allocate( legth );
		bb.order( ByteOrder.BIG_ENDIAN );
		bb.put( (byte) 0 );
		bb.putInt( packetid );
		bb.putInt( messagetype );
		bb.putInt( legth );
		assert ( bb.position() == bufferoffset ) : bb.position() + " , " + bufferoffset;
	}

	public Buffercontent( int length , byte[] bytes ) {
		bb = ByteBuffer.allocate( length );
		bb.put( bytes, 0, length );
	}

	public int getMessageType() {
		try {
			return bb.getInt( 4 + 1 );
		} catch ( Exception e ) {
			throw new InvalidPackageException( e );
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": pid=" + getPacketId();
	}

	/**
	 * To prevent OutOfMemoryError caused by illeagal packages. (Some bytes in a
	 * package my represent the lenght of an array. And this is the way to
	 * prevent the creation of an array with the lenght Interger.maxvale)
	 */
	public static int validateArLen( int lenght ) throws CriticalByteArreyLenghtException {
		if( lenght > maxlength )
			throw new CriticalByteArreyLenghtException();
		return lenght;
	}

	/**
	 * Like Buffercontent#validateArLen(int) but also uses parameter 2
	 * as additional limiter
	 * 
	 * @see Buffercontent#validateArLen(int)
	 */
	public static int validateArLen( int lenght, int max ) throws CriticalByteArreyLenghtException {
		if( lenght > max )
			throw new CriticalByteArreyLenghtException();
		return validateArLen( lenght );
	}

	/**
	 * Like Buffercontent#validateArLen(int) but also uses parameter 2
	 * as additional limiter
	 * 
	 * @see Buffercontent#validateArLen(int)
	 */
	public static int validateArLen( int lenght, int min, int max ) throws CriticalByteArreyLenghtException {
		if( lenght < min || lenght > max )
			throw new CriticalByteArreyLenghtException();
		return validateArLen( lenght );
	}

	public byte[] getBytes() {
		return bb.array();
	}

	/**
	 * Every Packet has its own id. The difference between this id and the
	 * messageid is that the to one messageid can belong to many packages but
	 * every datapacket has its own packetid.
	 */
	public int getPacketId() {
		try {
			return bb.getInt( 1 );
		} catch ( Exception e ) {
			throw new InvalidPackageException( e );
		}
	}

	public void setId( OpenRange range ) {
		int id = range.popId();
		bb.putInt( 1, id );
		assert ( getPacketId() == id );
	}

	/* public boolean isBlowFishCryption(){
	 * return ( bb.get( 0 ) & BLOWFISH_ENCRYPTION ) == BLOWFISH_ENCRYPTION;
	 * }
	 * 
	 * public boolean isRSACryption(){
	 * return ( bb.get( 0 ) & RSA_ENCRYPTION ) == RSA_ENCRYPTION;
	 * } */

	public byte getCryption() {
		return bb.get( 0 );
	}

	public void setCryption( byte cryption ) {
		assert ( cryption == RSA_ENCRYPTION || cryption == BLOWFISH_ENCRYPTION || cryption == NO_ENCRYPTION );
		bb.put( 0, cryption );
		/* byte newflags = bb.get( 0 );
		 * if( encry ){
		 * bb.put( 0 , ( byte ) ( newflags | BLOWFISH_ENCRYPTION ) );
		 * }
		 * else{
		 * bb.put( 0 , ( byte ) ( newflags & ~BLOWFISH_ENCRYPTION ) );
		 * } */
	}

	public int getLength() {
		return bb.getInt( 9 );
	}
}
