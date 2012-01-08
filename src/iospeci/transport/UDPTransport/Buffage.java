package iospeci.transport.UDPTransport;

import iospeci.DirectedMessage;

class Bufage {
	private long lastneed;
	private DirectedMessage data;

	public Bufage( DirectedMessage data ) {
		this.data = data;
		touch();
	}

	public long getLastTouch() {
		return lastneed;
	}

	public void touch() {
		lastneed = System.currentTimeMillis();
	}

}