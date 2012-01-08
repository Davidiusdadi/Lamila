package urlocator;

import java.net.InetSocketAddress;

public class PointerNode extends Node {
	byte[] id;

	public PointerNode( InetSocketAddress adress , byte[] id ) {
		super( adress );
		this.id = id;
	}

	public PointerNode( InetSocketAddress adress , long id ) {
		super( adress );
		this.id = Node.convertLongToByteArray( id );
	}

	@Override
	public byte[] getUniqueId() {
		return id;
	}

}
