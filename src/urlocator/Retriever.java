package urlocator;

import globalstatic.Lamilastatics;
import iospeci.Buffercontent;
import iospeci.CommunicationException;
import iospeci.DirectedRequestmessage;
import iospeci.Responsemessage;
import iospeci.transport.ResponsePreview;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Retriever extends PacketListener {
	private static int allretid = 0;
	private final int retid = allretid++;
	protected Peer connection;
	private final List<Responsemessage> allresponses = new LinkedList<Responsemessage>();
	private final HashMap<Integer,Responsemessage> lastestresponses = new HashMap<Integer,Responsemessage>();
	private int towatch = 0;
	private int timeout;
	private boolean previewpassed = false;
	private int remainingmresponsesscount = -1;
	private int allresponescount = -1;
	private boolean isfinshed = false;
	private boolean iscanceled = false;
	private IOException exception = null;
	private CountDownLatch block = new CountDownLatch( 1 );
	private long time;
	RetrieverListener listener;

	public interface RetrieverListener {
		public void retrieverChanged( Retriever r );
	}

	public Retriever( Peer connection , DirectedRequestmessage request , int timedout ) {
		super( request );
		this.connection = connection;
		this.timeout = timedout;
		this.request = request;
	}

	public Retriever( Peer connection , DirectedRequestmessage request , int timedout , RetrieverListener listener ) {
		super( request );
		this.connection = connection;
		this.timeout = timedout;
		this.request = request;
		this.listener = listener;
	}

	/**
	 * Sends the request and starts listening.
	 * 
	 * @throws IOException
	 */
	public void send() throws IOException {
		Thread.currentThread().setName( "Retriever " + retid );
		if( !request.isMultiResponseRequest() ) {
			allresponescount = 1;
			remainingmresponsesscount = 1;
			previewpassed = true;
		}
		time = System.currentTimeMillis();
		try {
			connection.send( request, this );
		} catch ( IOException e ) {
			exception = e;
			unlock();
			throw e;
		}
	}

	public Responsemessage getAResponse() {
		Responsemessage aresponse;
		synchronized ( lastestresponses ) {
			aresponse = lastestresponses.remove( new Integer( towatch ) );
		}
		if( aresponse != null && towatch < allresponescount )
			towatch++;
		return aresponse;
	}

	/**
	 * Returns true if there is Response arrived.
	 */
	public boolean isResponsePopable() {
		return towatch < allresponescount - remainingmresponsesscount;
	}

	// public boolean hasNextResponseYet() {
	// return towatch < allresponescount ;
	// }
	/**
	 * Returns true if all expected responses are retrieved. This method will
	 * never return true if an error occurred.
	 */
	public boolean allResponsesPoped() {
		return towatch == allresponescount;
	}

	public boolean isResponseRetivementCommplete() {
		return isfinshed || iscanceled;
	}

	/**
	 * Returns null until all responses successfully arrived.
	 */
	public List<Responsemessage> getAllResponses() {
		if( isfinshed )
			return allresponses;
		else
			return null;
	}

	/**
	 * Counts the remaining responses.
	 */
	public int getRemainingResponsesCount() {
		return remainingmresponsesscount;
	}

	/**
	 * Returns previewed count of responses.
	 */
	public int getExpectedResponsesCount() {
		return allresponescount;
	}

	@Override
	public void packetRetrive( Buffercontent message ) {
		if( iscanceled )
			return;
		if( message instanceof Responsemessage == false )
			throw new RuntimeException( "Illegal Resonsemessage" );
		if( request.isMultiResponseRequest() ) {
			if( message instanceof ResponsePreview ) {
				if( previewpassed ) {
					fireListener();
					throw new CommunicationException( "The Responder repeated the ResponsePreview request: " + request + " current response " + message );
				} else
					previewpassed = true;
				ResponsePreview prev = (ResponsePreview) message;
				remainingmresponsesscount = prev.getIncommingMessageCount() + 1;
				allresponescount = remainingmresponsesscount;
			}
		}
		if( message instanceof Responsemessage ) {
			if( !previewpassed )
				throw new CommunicationException( "ResponsePreview required  request: " + request + " current response " + message );
			allresponses.add( (Responsemessage) message );
			synchronized ( lastestresponses ) {
				lastestresponses.put( allresponses.size() - 1, (Responsemessage) message );
			}
			remainingmresponsesscount--;
			if( remainingmresponsesscount == 0 ) {
				isfinshed = true;
				unlock();
			}
		}
		fireListener();
	}

	private void fireListener() {
		if( listener != null )
			listener.retrieverChanged( this );
	}

	private void unlock() {
		block.countDown();
		time = System.currentTimeMillis() - time;
		fireListener();
	}

	/**
	 * Returns if all responses arrived without problems.
	 */
	public boolean allresopnsesArrived() {
		return isfinshed;
	}

	/**
	 * Stops the retrieving of responses.
	 */
	public void cancel() {
		iscanceled = true;
		unlock();
	}

	/**
	 * Blocks until all responses arrived of an exception occurred by the
	 * retrieving.
	 * 
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws IOException
	 */
	public void wait4all() throws InterruptedException , TimeoutException , IOException {
		block.await( Lamilastatics.tsTimeout( timeout ), TimeUnit.MILLISECONDS );
		if( exception != null )
			throw exception;
		else if( !isResponseRetivementCommplete() ) {
			throw new TimeoutException( "after " + timeout + " miliseconds" );
		}
	}

	/**
	 * Returns the runtime of this listener till now if the retrieving is not
	 * complete or to the time when it was completed.
	 */
	public long getRuntime() {
		if( isResponseRetivementCommplete() )
			return time;
		else
			return System.currentTimeMillis() - time;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " retriving response to" + request;
	}
}
