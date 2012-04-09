package globalstatic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class provides framework-global data
 * 
 * @author David Rohmer
 */
public class Lamilastatics {
	/**
	 * String encoding method
	 */
	public final static Charset charset = Charset.forName( "UTF8" );
	/**
	 * if true there will be debug output
	 */
	public static final boolean debugoutput = false;
	public static final boolean local_debug = false;
	private static PrintStream printstream = System.out;
	public static final ExecutorService executor = Executors.newCachedThreadPool();

	public enum OutputKind {
		INFO , SEARCH , WARNING , IO , TRANSPORT , ERROR , FATALERROR , UNKNOW;
	}

	public static void print( String s ) {
		if( debugoutput )
			print( OutputKind.UNKNOW, s );
	}

	public static void print( OutputKind level, String s ) {
		if( debugoutput && ( ( level.ordinal() <= OutputKind.INFO.ordinal() ) ) || ( level.ordinal() >= OutputKind.ERROR.ordinal() ) )
			printstream.print( s );
	}

	public static void print( Object s ) {
		print( s.toString() );
	}

	public static void println( String s ) {
		print( s + "\n" );
	}

	public static void println( Object s ) {
		print( s + "\n" );
	}

	public static void println( OutputKind level, Object s ) {
		print( level, s + "\n" );
	}

	public static void println() {
		print( "\n" );
	}

	public static long tsTimeout( long timeout ) {
		return timeout;
		// return Long.MAX_VALUE - System.currentTimeMillis() - 1;
	}

	public static int tsTimeout( int timeout ) {
		return (int) tsTimeout( (long) timeout );
	}

	public static void setPrintStream( PrintStream p ) {
		printstream = p;
	}

}

class Outsplitter extends PrintStream {
	PrintStream[] pr;

	public Outsplitter( File f , PrintStream... p ) throws FileNotFoundException {
		super( f );
		this.pr = p;
	}

	@Override
	public void print( String x ) {
		if( x.contains( "Exception" ) ) {
			int debugdumz = 1;
			debugdumz++;
		}

		for( PrintStream p : pr )
			p.println( x );
		super.print( x );
	}

	public void println() {
		for( PrintStream p : pr )
			p.println();
		super.println();
	}

	@Override
	public void print( Object obj ) {
		for( PrintStream p : pr )
			p.print( obj );
		super.print( obj );
	}

}