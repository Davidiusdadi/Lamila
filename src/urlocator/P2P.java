package urlocator;

import globalstatic.Lamilastatics;
import globalstatic.Lamilastatics.OutputKind;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.Enumeration;

public class P2P {
	public static void main( String[] args ) throws UnknownHostException {
		try {
			Enumeration networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while ( networkInterfaces.hasMoreElements() ) {
				NetworkInterface networkInterface = (NetworkInterface) networkInterfaces.nextElement();
				Lamilastatics.println( OutputKind.INFO, networkInterface.getDisplayName() + ": " );
				Enumeration<InetAddress> adresses = networkInterface.getInetAddresses();
				while ( adresses.hasMoreElements() ) {
					Lamilastatics.println( (InetAddress) adresses.nextElement() );
				}

			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		String localHost = InetAddress.getLocalHost().getHostName();
		for( InetAddress ia : InetAddress.getAllByName( localHost ) )
			Lamilastatics.println( ia );

		for( InetAddress ia : InetAddress.getAllByName( "www.tutego.com" ) )
			Lamilastatics.println( ia );
		Lamilastatics.println( md5( "David" ) );
		Lamilastatics.println( md5( "David" ) );
	}

	private static String md5( String message ) {
		try {
			MessageDigest md = MessageDigest.getInstance( "MD5" );
			return hex( md.digest( message.getBytes( "CP1252" ) ) );
		} catch ( Exception e ) {
		}
		return null;
	}

	private static String hex( byte[] array ) {
		StringBuffer sb = new StringBuffer();
		for( int i = 0 ; i < array.length ; ++i ) {
			sb.append( Integer.toHexString( ( array[ i ] & 0xFF ) | 0x100 ).toLowerCase().substring( 1, 3 ) );
		}
		return sb.toString();
	}
}
