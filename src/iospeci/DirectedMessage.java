package iospeci;

public interface DirectedMessage {
	public byte[] getHost();

	public int getPort();

	public Buffercontent getContent();
}