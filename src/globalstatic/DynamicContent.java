package globalstatic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DynamicContent {
	private Properties props = new Properties();

	public DynamicContent( String path2inifile ) throws IOException {

		try {
			InputStream s = ClassLoader.getSystemResourceAsStream( path2inifile );
			if( s == null )
				throw new IOException( "Could not load DynamicContent " + path2inifile + " using ClassLoader.getSystemResourceAsStream" );
			props.load( s );
		} catch ( IOException e ) {
			System.out.println( "Couldn't load ini File: " + e.toString() );
			System.out.println( "from dir " + new File( "./" ).getCanonicalPath() );
			throw e;
		}
	}
}
