package iospeci.transport;

import iospeci.Buffercontent;
import iospeci.OpenRange;

import java.io.IOException;

public interface RecepientInputStream {

	public abstract void applyRange( OpenRange range ) throws IOException;

	public abstract void append( Buffercontent b ) throws IOException;

	public abstract void flush() throws IOException;

}