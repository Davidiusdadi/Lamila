package iospeci;

import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class CryptManager {
	private HashMap<InetSocketAddress,PublicKey> rsakeys;
	private HashMap<InetSocketAddress,CiperPair> blowkeys;
	private RSAPublicKeySpec rsapublicKey;
	private RSAPrivateKeySpec rsaprivateKey;
	private KeyFactory rsakeyfact;
	private KeyGenerator blowkeygen;
	private Cipher rsadecoder;
	private int inpacketoffset = Buffercontent.getEncryptionOffset();
	private String blowmode = "Blowfish";

	public CryptManager( InetSocketAddress ownadress ) {
		try {

			KeyPairGenerator pairgen;
			pairgen = KeyPairGenerator.getInstance( "RSA" );
			pairgen.initialize( 2048 );
			KeyPair kp = pairgen.genKeyPair();

			rsakeyfact = KeyFactory.getInstance( "RSA" );
			rsapublicKey = (RSAPublicKeySpec) rsakeyfact.getKeySpec( kp.getPublic(), RSAPublicKeySpec.class );
			rsaprivateKey = (RSAPrivateKeySpec) rsakeyfact.getKeySpec( kp.getPrivate(), RSAPrivateKeySpec.class );

			rsadecoder = Cipher.getInstance( "RSA" );
			rsadecoder.init( Cipher.DECRYPT_MODE, rsakeyfact.generatePrivate( rsaprivateKey ) );

			blowkeygen = KeyGenerator.getInstance( "Blowfish" );
			blowkeygen.init( 128 );

			blowkeys = new HashMap<InetSocketAddress,CiperPair>();
			rsakeys = new HashMap<InetSocketAddress,PublicKey>();
			addRSAEncoder( ownadress, rsapublicKey );
			addBlowKey( ownadress, chooseBlowKey( ownadress ) );

		} catch ( NoSuchAlgorithmException e ) {
			throw new Error( e );
		} catch ( InvalidKeySpecException e ) {
			throw new Error( e );
		} catch ( InvalidKeyException e ) {
			throw new Error( e );
		} catch ( NoSuchPaddingException e ) {
			throw new Error( e );
		}

	}

	public byte[] decryptRSA( InetSocketAddress from, byte[] bytes, int offset, int length ) throws DeCryptException {
		byte[] result = new byte[ rsadecoder.getOutputSize( bytes.length ) + 1 ];
		synchronized ( rsadecoder ) {
			try {
				rsadecoder.doFinal( bytes, inpacketoffset + offset, length - inpacketoffset, result, inpacketoffset );
			} catch ( Exception e ) {
				throw new DeCryptException( e );
			}
		}
		for( int i = 0 ; i < inpacketoffset ; i++ )
			result[ i ] = bytes[ i ];
		result = Arrays.copyOf( result, Buffercontent.extractRealLength( result ) );

		return result;
	}

	public byte[] decryptBlow( InetSocketAddress from, byte[] bytes, int offset, int length ) throws DeCryptException {
		Cipher decrypter = blowkeys.get( from ).decryper;
		byte[] result = new byte[ decrypter.getOutputSize( bytes.length ) + 1 ];
		synchronized ( decrypter ) {
			try {
				decrypter.doFinal( bytes, inpacketoffset + offset, length - inpacketoffset, result, inpacketoffset );
			} catch ( Exception e ) {
				throw new DeCryptException( e );
			}
		}
		for( int i = 0 ; i < inpacketoffset ; i++ )
			result[ i ] = bytes[ i ];
		result = Arrays.copyOf( result, Buffercontent.extractRealLength( result ) );

		return result;
	}

	public byte[] encryptBlow( InetSocketAddress to, byte[] bytes ) throws EnCryptException {
		Cipher encrypter = blowkeys.get( to ).encrypter;
		byte[] result = new byte[ encrypter.getOutputSize( bytes.length ) + 1 ];
		synchronized ( encrypter ) {
			try {
				encrypter.doFinal( bytes, inpacketoffset, bytes.length - inpacketoffset, result, inpacketoffset );
			} catch ( Exception e ) {
				throw new EnCryptException( e );
			}
		}
		for( int i = 0 ; i < inpacketoffset ; i++ )
			result[ i ] = bytes[ i ];

		return result;
	}

	public byte[] encryptRSA( InetSocketAddress to, byte[] bytes ) throws EnCryptException {
		try {
			Cipher c = Cipher.getInstance( "RSA" );
			PublicKey publickey = rsakeys.get( to );
			if( publickey == null )
				throw new RuntimeException( "Do not hold the required public key." );
			c.init( Cipher.ENCRYPT_MODE, publickey );
			byte[] result = new byte[ c.getOutputSize( bytes.length ) + 1 ];
			// c.doFinal( bytes , offset , bytes.length - offset , result , offset );
			c.doFinal( bytes, inpacketoffset, bytes.length - inpacketoffset, result, inpacketoffset );

			for( int i = 0 ; i < inpacketoffset ; i++ )
				result[ i ] = bytes[ i ];

			// byte[] check = decryptRSA( to , result , 0 , result.length );
			// System.out.println( "Allright: " + Arrays.equals( check , bytes ) );

			return result;
		} catch ( Throwable e ) {
			throw new EnCryptException( e );
		}
	}

	public void addRSAEncoder( InetSocketAddress from, RSAPublicKeySpec key ) {
		try {
			rsakeys.put( from, rsakeyfact.generatePublic( key ) );
		} catch ( InvalidKeySpecException e ) {
			new RuntimeException( e );
		}
	}

	public void addBlowKey( InetSocketAddress from, SecretKeySpec key ) {
		try {
			Cipher decryper = Cipher.getInstance( blowmode );// Blowfish/CFB/NoPadding
			Cipher encrypter = Cipher.getInstance( blowmode );// Blowfish/CFB/NoPadding
			encrypter.init( Cipher.ENCRYPT_MODE, key );
			decryper.init( Cipher.DECRYPT_MODE, key );

			blowkeys.put( from, new CiperPair( decryper, encrypter ) );
		} catch ( Exception e ) {
			throw new Error( e );// shouldn't occur
		}
	}

	public void addBlowKey( InetSocketAddress from, byte[] rawkey ) {
		addBlowKey( from, new SecretKeySpec( rawkey, blowmode ) );
	}

	public RSAPublicKeySpec getPublicKey() {
		return rsapublicKey;
	}

	public SecretKeySpec chooseBlowKey( InetSocketAddress to ) {
		SecretKeySpec blowkey = (SecretKeySpec) blowkeygen.generateKey();
		addBlowKey( to, blowkey );
		return blowkey;
	}

	private class CiperPair {
		public CiperPair( Cipher decryper , Cipher encrypter ) {
			this.decryper = decryper;
			this.encrypter = encrypter;
		}

		public Cipher decryper;
		public Cipher encrypter;
	}
}
