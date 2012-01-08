package urlocator;

import iospeci.Buffercontent;
import iospeci.InvalidPackageException;

import java.io.IOException;

public interface Messagehandler {
	public void handlePackage( TransportLayer transp, Buffercontent message ) throws UnknowPacketTypeException , IgnoringPackageException , InvalidPackageException , IOException;
}
