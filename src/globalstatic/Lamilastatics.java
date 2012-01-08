package globalstatic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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
	public static final boolean debugoutput = true;
	public static final boolean countio = true;
	public static final ExecutorService executor = Executors.newCachedThreadPool();
	private static DynamicContent configuration;
	private static boolean ready = false;
	public static Logger logger = Logger.getLogger( "Webtouchlog" );

	public enum OutputKind {
		INFO , WARNING , ERROR , FATALERROR , UNKNOW;
	}

	public DynamicContent getConfiguration() {
		if( configuration == null )
			throw new RuntimeException( "static variables not initialiyed\nPlease call globalstatics.setup once before." );
		return configuration;
	}

	/**
	 * Initializes static members in this package and loads properties
	 * 
	 * @throws IOException
	 *             If the propertyfile couldn't be read.
	 */
	public static void setup() {

		try {
			configuration = new DynamicContent( "\\ini\\Messageusetage.properties" );
			File f = new File( "C:/Users/David/Desktop/webtouchlog.log" );
			f.delete();
			f.createNewFile();
			Outsplitter p = new Outsplitter( f, System.out );
			System.setOut( p );
			System.setErr( p );
			// This block configure the logger with handler and formatter
			FileHandler fh = new FileHandler( "C:/Users/David/Desktop/webtouchlog.log", true );
			logger.addHandler( fh );
			logger.setLevel( Level.ALL );
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter( formatter );

			// the following statement is used to log any messages
			// logger.log( Level.WARNING , "My first log" );

		} catch ( SecurityException e ) {
			e.printStackTrace();
		} catch ( IOException e ) {
			e.printStackTrace();
		}

	}

	public static void print( String s ) {
		if( debugoutput )
			print( OutputKind.UNKNOW, s );
	}

	public static void print( OutputKind level, String s ) {
		if( debugoutput || level.ordinal() >= OutputKind.ERROR.ordinal() )
			System.out.print( s );
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
		print( s + "\n" );
	}

	public static void println() {
		print( "\n" );
	}

	public static long tsTimeout( long timeout ) {
		// return timeout;
		return Long.MAX_VALUE - System.currentTimeMillis() - 1;
	}

	public static int tsTimeout( int timeout ) {
		return (int) tsTimeout( (long) timeout );
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