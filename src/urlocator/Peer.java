package urlocator;

import globalstatic.Lamilastatics;
import globalstatic.Lamilastatics.OutputKind;
import iospeci.Buffercontent;
import iospeci.CryptManager;
import iospeci.DirectedRequestmessage;
import iospeci.DirectedResponsemessage;
import iospeci.InvalidPackageException;
import iospeci.KeyRequest;
import iospeci.KeyResponse;
import iospeci.PointerRequest;
import iospeci.PointerResponse;
import iospeci.Requestmessage;
import iospeci.Responsemessage;
import iospeci.StoreRequest;
import iospeci.StoreResponse;
import iospeci.transport.RecepientInputStream;
import iospeci.transport.ResponsePreview;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import javax.crypto.spec.SecretKeySpec;

import searching.SearchAdvisor;
import searching.Searcher;
import searching.Searchprocess;
import searching.StaticSeachStrategie;
import correlation.Correlator;
import correlation.Correlators;
import correlation.Hashcodecorrelator;
import correlation.IPAdressCorrelator;

/**
 * This class is the connection to the ptp-network and contains methods to
 * modify it.
 * 
 * @author David Rohmer
 */
public class Peer implements Messagehandler {
	static {
		Buffercontent.addOptainer( KeyRequest.class );
		Buffercontent.addOptainer( PointerRequest.class );
		Buffercontent.addOptainer( StoreRequest.class );
		Buffercontent.addOptainer( StoreResponse.class );
		Buffercontent.addOptainer( KeyResponse.class );
		Buffercontent.addOptainer( PointerResponse.class );
		Buffercontent.addOptainer( ResponsePreview.class );
	}
	public static final long hour = 1000 * 60 * 60;
	public static final long hour4 = hour * 4;
	public static final long hour24 = hour * 24;

	protected static SearchAdvisor republishingadvisor = new StaticSeachStrategie( 20000, 10, 3000, 3, Lamilastatics.tsTimeout( 10000 ), 10 );

	public IOCount iocount = Lamilastatics.countio ? new IOCount() : null;
	protected TransportLayer tranport;
	protected CryptManager crypt;
	protected Hashtable<Integer,List<PacketListener>> waitingnodes;
	protected Correlators correlators;
	protected Hashtable<Integer,Long> timestamp;
	protected LinkedList<Node> networkentys = new LinkedList<Node>();
	protected static ExecutorService executor = Executors.newCachedThreadPool();
	protected Timer timer = new Timer( "NetRefresh", true );
	private DistributingPeerListener dls = new DistributingPeerListener();
	protected Linkrejectingtask rejecttask;
	protected LinkStoringTask restroetask;
	protected Hashtable<ByteArrayWrapper,Node> hostednodes;
	protected int defaulttimeout = Lamilastatics.tsTimeout( 10000 );

	public static <T> T getFirst( List<T> list ) {
		if( !list.isEmpty() )
			return list.get( 0 );
		return null;
	}

	/**
	 * @param socket
	 *            The socket on which the communication with the network will
	 *            take place.
	 * @param correlators
	 *            The Correlators which are used to order the nodes in the
	 *            network
	 * @param hostednodes
	 *            The addresses of some nodes in the network. Can be null.
	 */
	public Peer( TransportLayer tranport , Correlators correlators ) {
		if( tranport == null || correlators == null )
			throw new IllegalArgumentException( "Parameters tranport and correlators cant be null!" );
		this.tranport = tranport;
		crypt = new CryptManager( tranport.getAdress() );
		tranport.setMessageHandler( this );
		tranport.setCrypter( crypt );

		this.correlators = correlators;
		this.hostednodes = new Hashtable<ByteArrayWrapper,Node>();
		waitingnodes = new Hashtable<Integer,List<PacketListener>>();
		setRejectLinksInterval( hour24, hour4 );
		setRestoreLinksInterval( hour4 );
	}

	/**
	 * Returns the Correlators this networkconnection is operating with.
	 */
	public Correlators getCorrelators() {
		return correlators;
	}

	/**
	 * If a node does not stores itself in the network continuously it will be
	 * removed by its neighbors.
	 * 
	 * @param time
	 *            The time which have to pass until a node will be forgotten by
	 *            this peer.
	 * @param sheduleintervall
	 *            The interval in which this peer will look if there are links
	 *            that are to old.
	 */
	public void setRejectLinksInterval( long time, long sheduleintervall ) {
		if( rejecttask != null )
			rejecttask.cancel();
		rejecttask = new Linkrejectingtask( time );
		timer.scheduleAtFixedRate( rejecttask, 0, sheduleintervall );
	}

	/**
	 * Sets the interval this peer will store its nodes in the network.
	 */
	public void setRestoreLinksInterval( long interval ) {
		if( restroetask != null )
			restroetask.cancel();
		restroetask = new LinkStoringTask();
		timer.scheduleAtFixedRate( restroetask, 0, interval );
	}

	/* @Override
	 * public void run(){
	 * Thread.currentThread().setName( "Socketlistener" );
	 * byte[] data = new byte[5000];
	 * DatagramPacket packet = new DatagramPacket( data , data.length );
	 * Thread thistread = Thread.currentThread();
	 * while ( !thistread.isInterrupted() ){
	 * try{
	 * socket.receive( packet );
	 * handlePackage( packet );
	 * } catch ( Exception e ){
	 * e.printStackTrace();
	 * dls.innerException( e );
	 * }
	 * }
	 * globalstatics.println( "Peer down" );
	 * } */
	/**
	 * Sends a response to a node into the network.
	 * 
	 * @param message
	 *            The request
	 * @throws IOException
	 */
	public void send( DirectedResponsemessage message ) throws IOException {
		Lamilastatics.println( OutputKind.INFO, "push   " + message.toString() + " mesid: " + message.getRequestMessageId() );
		if( Lamilastatics.countio )
			iocount.out( message );
		tranport.send( message );
	}

	/**
	 * Sends a request to a node in the network.
	 * 
	 * @param message
	 *            The request
	 * @param ls
	 *            A packetlistenr to be able to get the response of the other
	 *            node.
	 * @throws IOException
	 */
	public void send( DirectedRequestmessage message, PacketListener ls ) throws IOException {
		Lamilastatics.println( OutputKind.INFO, "sending " + message.toString() + " mesid: " + message.getMessageId() + " zu " + InetAddress.getByAddress( message.getHost() ) );
		List<PacketListener> list = waitingnodes.get( ls.desiredMessageId() );
		if( list == null ) {
			list = new LinkedList<PacketListener>();
			waitingnodes.put( ls.desiredMessageId(), list );
		}
		list.add( ls );
		if( Lamilastatics.countio )
			iocount.out( message );
		tranport.send( message );
	}

	public List<Responsemessage> sendAndRetrive( DirectedRequestmessage message ) throws OperationFailedException {
		return sendAndRetrive( message, getDefaultTimeout() );
	}

	/**
	 * Sends out a requst and returns after the response arrived or any error
	 * occurred. The error may be a nodetimeout or a connection problem.
	 * 
	 * @param message
	 *            The request
	 * @param timeout
	 *            The timeout
	 * @return The responses
	 * @throws OperationFailedException
	 */
	public List<Responsemessage> sendAndRetrive( DirectedRequestmessage message, int timeout ) throws OperationFailedException {
		try {
			Retriever retriever = new Retriever( this, message, timeout );
			retriever.send();
			retriever.wait4all();
			return retriever.getAllResponses();
		} catch ( IOException e ) {
			throw new OperationFailedException( e );
		} catch ( InterruptedException e ) {
			throw new OperationFailedException( e );
		} catch ( TimeoutException e ) {
			throw new OperationFailedException( e );
		}
	}

	/**
	 * Returns the nodes this connection provides.
	 */
	public Collection<Node> getHostednodes() {
		return hostednodes.values();
	}

	public void handlePackage( TransportLayer transp, Buffercontent message ) throws UnknowPacketTypeException , IgnoringPackageException , InvalidPackageException , IOException {
		if( Lamilastatics.debugoutput )
			System.out.println( "got    " + message );
		if( Lamilastatics.countio )
			iocount.in( message );
		if( message instanceof DirectedResponsemessage ) {
			Responsemessage responder = (Responsemessage) message;
			List<PacketListener> listeners = waitingnodes.get( responder.getRequestMessageId() );
			if( listeners == null ) {
				throw new IgnoringPackageException( "Unexpected Response" );
			}
			for( PacketListener listener : listeners ) {
				listener.packetRetrive( message );
				if( listener.isResponseRetivementCommplete() ) {
					List<PacketListener> pack = waitingnodes.get( responder.getRequestMessageId() );
					if( pack == null )
						throw new IgnoringPackageException( "Unexpected Response" );
					else
						pack.remove( new Integer( responder.getRequestMessageId() ) );
					if( pack.isEmpty() )
						waitingnodes.remove( new Integer( responder.getRequestMessageId() ) );
				}
			}
		} else if( message instanceof Requestmessage ) {
			handleRequest( (Requestmessage) message );
		} else
			throw new UnknowPacketTypeException();
	}

	private void handlePointerRequest( PointerRequest mes ) throws IgnoringPackageException , InvalidPackageException , IOException {
		byte[] key = mes.getNodeId();
		Node hosted = hostednodes.get( new ByteArrayWrapper( key ) );
		Lamilastatics.println( key );
		if( hosted == null ) {
			Lamilastatics.println( OutputKind.INFO, "hosting: " + hostednodes );
			throw new IgnoringPackageException( "Requested Data not hosted" + mes );
		}
		Correlator c = correlators.getCorrelator( mes.getCorrelatorId() );
		if( c == null )
			throw new IgnoringPackageException( "no such correlator" );
		int hostdiff = c.correlate( mes.getKey(), hosted.getKey( mes.getCorrelatorId() ) );

		List<Link> nodes = hosted.getLinks( mes.getCorrelatorId() );
		List<PointerResponse> responses = new LinkedList<PointerResponse>();
		for( Link l : nodes ) {
			Node n = l.getNode();
			PointerResponse presp = new PointerResponse( mes, n, c.correlate( mes.getKey(), n.getKey( mes.getCorrelatorId() ) ) );
			if( hostdiff > presp.getDistance() || mes.getLeafes() == SearchAdvisor.PUSH_ALL )
				responses.add( presp );
		}
		int responsecount = mes.getLeafes() < responses.size() && mes.getLeafes() >= SearchAdvisor.PUSH_MUCH ? mes.getLeafes() : responses.size();
		RecepientInputStream recstream = tranport.previewTransfere( mes.getFrom(), Buffercontent.reserveIds( responsecount + 1 ), mes.getCryption() );

		ResponsePreview resp = new ResponsePreview( mes, responsecount );
		recstream.append( resp );
		Collections.sort( responses, new Comparator() {
			@Override
			public int compare( Object p1, Object p2 ) {
				return ( (PointerResponse) p1 ).getDistance() - ( (PointerResponse) p2 ).getDistance();
			}
		} );
		/* globalstatics.print( ": " );
		 * for ( PointerResponse pre : responses )
		 * globalstatics.print( pre.getNode() + " (" + pre.getDistance() + ") ,"
		 * );
		 * globalstatics.println(); */

		Iterator<PointerResponse> it = responses.iterator();
		for( int i = 0 ; i < responsecount ; i++ )
			recstream.append( it.next() );
	}
	private int correlate( byte[] key, byte[] key2, int correlatorId ) throws NoSuchCorrelatorException {
		Correlator cor = correlators.getCorrelator( correlatorId );
		if( cor == null ) {
			throw new NoSuchCorrelatorException();
		}
		return cor.correlate( key, key2 );
	}
	private void handleKeyRequest( KeyRequest request ) throws IOException , IgnoringPackageException {
		Node n = hostednodes.get( new ByteArrayWrapper( request.getNodeId() ) );
		byte[] key = null;
		if( n == null )
			throw new IgnoringPackageException( "Requested Data not hosted" + request );
		key = n.getKey( request.getCorrelatorId() );
		if( key == null )
			throw new IgnoringPackageException( "Requested Data not found" );
		KeyResponse resp = new KeyResponse( request, key );
		send( resp );
	}

	protected void handleRequest( Requestmessage r ) throws InvalidPackageException , IgnoringPackageException , IOException {
		dls.requested( r );
		if( r instanceof PointerRequest ) {
			handlePointerRequest( (PointerRequest) r );
		} else if( r instanceof StoreRequest ) {
			handleStoreRequest( (StoreRequest) r );
		} else if( r instanceof KeyRequest ) {
			handleKeyRequest( (KeyRequest) r );
		}
	}

	private void handleStoreRequest( StoreRequest mes ) throws IgnoringPackageException , IOException {
		Node tostore = mes.getToStore();
		Node hosted = hostednodes.get( new ByteArrayWrapper( mes.getStoreNodeId() ) );
		if( hosted == null )
			throw new IgnoringPackageException( "Requested Data not hosted" + mes );
		hosted.addLink( mes.getCorrelatorId(), tostore );
		dls.linkAdded( hosted, correlators.getCorrelator( mes.getCorrelatorId() ), tostore );
		StoreResponse resp = new StoreResponse( mes, System.currentTimeMillis() + 1000 * 60 * 60 * 24 );
		send( resp );
	}

	/**
	 * Publishes a node in the network.
	 * 
	 * @param correlatoridlayer
	 *            The layer on which this node should be published
	 * @param newnode
	 *            The node which should be published
	 * @param advisor
	 *            The behavior by which the searchers will walk through the
	 *            network
	 * @return The nodes which will point to the published node and published
	 *         node points at.
	 */
	public LinkedList<Node> publish( final Correlator correlator, final Node newnode, final SearchAdvisor advisor ) throws OperationFailedException {
		LinkedList<Node> nodes = null;
		if( newnode == null )
			throw new IllegalArgumentException( "node can't be null" );
		if( newnode.getAdress() == null ) {
			newnode.setAdress( getAdress() );
		}
		if( !newnode.isValid() )
			throw new IllegalArgumentException( "node have to be valid ( isValid())" );
		if( networkentys.isEmpty() ) {
			Lamilastatics.println( OutputKind.INFO, "hosting " + newnode.getUniqueId().toString() + " / " + new String( newnode.getUniqueId(), Lamilastatics.charset ) );
			putNode( newnode.getUniqueId(), newnode );
			Lamilastatics.println( OutputKind.INFO, "netentry " + new String( newnode.getUniqueId(), Lamilastatics.charset ) );
			addNetworkEnty( newnode );
		} else {
			double time1 = System.currentTimeMillis() / (double) 1000;
			nodes = searchFor( correlator, newnode.getKey( correlator.getCorrelatorId() ), advisor, networkentys );
			double time2 = System.currentTimeMillis() / (double) 1000;
			if( nodes == null || nodes.isEmpty() )
				return null;
			ListIterator<Node> it = nodes.listIterator();
			for( int i = 0 ; i < correlator.getConvictionCount() ; i++ )
				if( it.hasNext() ) {
					Node next = it.next();
					try {
						fillInKey( next, correlator.getCorrelatorId(), advisor.getNodeRequestTimeout() );
						storeActive( correlator, next, newnode, advisor.getNodeRequestTimeout() );
					} catch ( Exception e1 ) {
						e1.printStackTrace();
					}
				} else
					break;
		}
		return nodes;
	}

	private void storeActive( Correlator correlator, Node thestore, Node tostore, int timeout ) throws Exception {
		StoreRequest storerequest = new StoreRequest( thestore, tostore, correlator.getCorrelatorId() );
		StoreResponse resp = (StoreResponse) sendAndRetrive( storerequest, timeout ).get( 0 );
		if( resp.getTimelapse() != -1 ) {
			putNode( tostore.getUniqueId(), tostore );
			tostore.addLink( correlator.getCorrelatorId(), thestore );
			dls.linkAdded( tostore, correlator, thestore );
		}
	}

	/**
	 * Finds nodes in the network which are similar or equal to the given key.
	 * 
	 * @param correlatoridlayer
	 *            The layer to which the key belongs and on which search will
	 *            happen.
	 * @param key
	 *            The key which is searched
	 * @param enty
	 *            The entries into the network
	 * @param advisor
	 *            The behavior by which the searchers will walk through the
	 *            network
	 * @return The result
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws OperationFailedException
	 */
	public LinkedList<Node> searchFor( Correlator c, byte[] key, SearchAdvisor advisor, Collection<Node> enty ) throws OperationFailedException {
		if( c instanceof IPAdressCorrelator ) {
			return new LinkedList<Node>();// A IPadress alone does not last to find a node.
		}
		if( enty.isEmpty() )
			throw new OperationFailedException( "No entys to start the search" );
		if( key == null )
			throw new OperationFailedException( "There is no key on the given layer" );
		Searchprocess searcher = new Searcher( this, c, key, fillInKey( enty, c.getCorrelatorId(), advisor.getNodeRequestTimeout() ), advisor, c.getConvictionCount() );
		try {
			return searcher.startSearch().get();
		} catch ( InterruptedException e ) {
			throw new OperationFailedException( e );
		} catch ( ExecutionException e ) {
			throw new OperationFailedException( e );
		}
	}

	public LinkedList<Node> searchFor( Correlator c, byte[] key ) throws OperationFailedException {
		return searchFor( c, key, c.getSearchAdvisor(), getNetworkentrys( c.getCorrelatorId(), Lamilastatics.tsTimeout( getDefaultTimeout() ) ) );
	}

	public LinkedList<Node> searchFor( int correlatorid, byte[] key, SearchAdvisor advisor, Collection<Node> searchlist ) throws OperationFailedException {
		return searchFor( correlators.getCorrelator( correlatorid ), key, advisor, searchlist );
	}

	public LinkedList<Node> searchFor( int correlatorid, byte[] key, SearchAdvisor advisor ) throws OperationFailedException {
		return searchFor( correlators.getCorrelator( correlatorid ), key, advisor, getNetworkentrys( correlatorid, Lamilastatics.tsTimeout( getDefaultTimeout() ) ) );
	}

	public LinkedList<Node> searchFor( int correlatorid, byte[] key ) throws OperationFailedException {
		return searchFor( correlators.getCorrelator( correlatorid ), key );
	}

	/**
	 * Looks up is hosted or recently requested/found node match the search. The
	 * returned list may be unmodifiable or/and empty.
	 */
	public List<Node> searchCache( int correlatorid, byte[] key ) {// TODO fully implement caching of notes
		if( correlatorid == Hashcodecorrelator.id ) {
			List<Node> list = new LinkedList<Node>();
			Node n = hostednodes.get( new ByteArrayWrapper( key ) );
			if( n != null ) {
				list.add( n );
			}
			// do more here
			return list;
		}
		return Collections.emptyList();

	}

	/**
	 * Puts the given node into the entymap.
	 */
	public void addNetworkEnty( Node n ) {
		Lamilastatics.println( OutputKind.INFO, "Add Networkenty: " + new String( n.getUniqueId(), Lamilastatics.charset ) + "," + Arrays.hashCode( n.getUniqueId() ) + "," + Arrays.toString( n.getUniqueId() ) );
		networkentys.add( n );
	}

	/**
	 * Removes the given node from the entymap.
	 */
	public void removeNetworkEnty( Node n ) {
		networkentys.remove( n );
	}

	/**
	 * Connects the given nodes and requests their key data on the given layer
	 * if there are not already key datas.
	 * 
	 * @param correlatoridlayer
	 *            The layer to which the keys belong.
	 * @throws OperationFailedException
	 */
	public LinkedList<Node> fillInKey( Collection<Node> nodes, int correlatorid, int timeout ) throws OperationFailedException {

		LinkedList<Node> updateednodes = new LinkedList<Node>();
		if( !nodes.isEmpty() )
			synchronized ( nodes ) {
				Iterator<Node> i = nodes.iterator();
				Node n;
				while ( i.hasNext() ) {
					n = i.next();
					try {
						fillInKey( n, correlatorid, timeout );
						updateednodes.add( (Node) n );
					} catch ( OperationFailedException e ) {
						e.printStackTrace();
						// TODO Log this critical OperationFailedException
					}
				}
			}
		if( !nodes.isEmpty() && updateednodes.isEmpty() )
			throw new OperationFailedException( "Couldn't reach any of the specified Nodes: " + nodes );
		return updateednodes;
	}

	/**
	 * Connects the given node and requests its key data on the given layer if
	 * there are not already key datas.
	 * 
	 * @param correlatoridlayer
	 *            The layer to which the keys belong.
	 * @throws OperationFailedException
	 */
	public void fillInKey( Node node, int correlatorid, int timeout ) throws OperationFailedException {
		if( node.getKey( correlatorid ) == null ) {
			KeyRequest request;
			List<Responsemessage> resp;
			try {
				request = new KeyRequest( node.getAdress(), node.getUniqueId(), correlatorid );
				resp = sendAndRetrive( request, timeout );
			} catch ( UnknownHostException e ) {
				throw new OperationFailedException();
			}
			if( resp.size() != 0 ) {
				Responsemessage theresponse = resp.get( 0 );
				if( theresponse instanceof KeyResponse )
					node.putKey( correlatorid, ( (KeyResponse) theresponse ).getKey() );
			}
		}
	}

	/**
	 * Returns all networkenties on the given layer.
	 * 
	 * @param correlationidlayer
	 * @return
	 * @throws OperationFailedException
	 */
	public LinkedList<Node> getNetworkentrys( int correlatorid, int timeout ) throws OperationFailedException {
		return fillInKey( networkentys, correlatorid, timeout );
	}

	/**
	 * Adds a PeerListener. if p will be added several times the listener will
	 * be invoked several times.
	 */
	public void addPeerListener( PeerListener p ) {
		dls.addPeerListener( p );
	}

	/**
	 * Removes the first PeerListener which is equal to the given one.
	 */
	public void removePeerListener( PeerListener p ) {
		dls.removePeerListener( p );

	}

	public LinkedList<Node> publish( int correlatorid, Node newnode ) throws OperationFailedException {
		Correlator correl = correlators.resolveCorrelator( correlatorid );
		return publish( correl, newnode, correl.getSearchAdvisor() );
	}

	public LinkedList<Integer> publish( Node newnode ) throws OperationFailedException {
		LinkedList<Integer> layers = newnode.layers();
		LinkedList<Integer> removed = new LinkedList<Integer>();
		for( Integer integ : layers ) {
			Correlator correl = correlators.resolveCorrelator( integ );
			try {
				publish( correl, newnode, correl.getSearchAdvisor() );
				removed.add( integ );
			} catch ( OperationFailedException e ) {
				e.printStackTrace();
			}
		}
		layers.removeAll( removed );
		return layers;
	}

	public Node getHostedNode( byte[] nodeIdent ) {
		return hostednodes.get( new ByteArrayWrapper( nodeIdent ) );
	}

	public InetSocketAddress getAdress() {
		return tranport.getAdress();
	}

	protected void putNode( byte[] key, Node node ) {
		hostednodes.put( new ByteArrayWrapper( key ), node );
	}

	public RSAPublicKeySpec getRSAPublicKey() {
		return crypt.getPublicKey();
	}

	public void addBlowKey( final InetSocketAddress from, final SecretKeySpec key ) {
		crypt.addBlowKey( from, key );
	}

	public void addBlowKey( final InetSocketAddress from, final byte[] rawkey ) {
		crypt.addBlowKey( from, rawkey );
	}

	public void setDefaultTimeout( int defaulttimeout ) {
		this.defaulttimeout = defaulttimeout;
	}

	public int getDefaultTimeout() {
		return defaulttimeout;
	}

	// i--o++
	class IOCount {
		private int icount = 0;
		private int ocount = 0;
		private int orqcount = 0;
		private int orscount = 0;
		private int irqcount = 0;
		private int irscount = 0;
		private int orsrcount = 0;;
		private int irsrcount = 0;

		public synchronized void out( Buffercontent buf ) {
			if( buf instanceof Requestmessage )
				orqcount++;
			else if( buf instanceof Responsemessage ) {
				if( buf instanceof PointerResponse == false ) {
					orscount++;
				} else {
					orsrcount++;
				}
			} else
				throw new RuntimeException( "Unknow IOMessagetype" );
			ocount++;
		}

		public synchronized void in( Buffercontent buf ) {
			if( buf instanceof Requestmessage )
				irqcount++;
			else if( buf instanceof Responsemessage ) {
				if( buf instanceof PointerResponse == false ) {
					irscount++;
				} else {
					irsrcount++;
				}
			} else
				throw new RuntimeException( "Unknow IOMessagetype" );
			icount++;
		}

		@Override
		public String toString() {
			return "i: " + icount + "( " + irqcount + "q / " + irscount + " & " + irsrcount + "s )" + "\no: " + ocount + "( " + orqcount + "q / " + orscount + " & " + orsrcount + "s )";
		}
	}

	protected class DistributingPeerListener implements PeerListener {
		private LinkedList<PeerListener> listeners = new LinkedList<PeerListener>();

		public synchronized void addPeerListener( PeerListener p ) {
			synchronized ( listeners ) {
				listeners.add( p );
			}
		}

		public synchronized void removePeerListener( PeerListener p ) {
			synchronized ( listeners ) {
				listeners.remove( p );
			}
		}

		@Override
		public void linkAdded( Node hostednode, Correlator layer, Node linkednode ) {
			for( PeerListener l : listeners )
				l.linkAdded( hostednode, layer, linkednode );
		}

		@Override
		public void linkRemoved( Node hostednode, Correlator layer, Node linkednode ) {
			for( PeerListener l : listeners )
				l.linkRemoved( hostednode, layer, linkednode );
		}

		@Override
		public void requested( Requestmessage m ) {
			for( PeerListener l : listeners )
				l.requested( m );
		}

		@Override
		public void scheduleLinkRejecting() {
			for( PeerListener l : listeners )
				l.scheduleLinkRejecting();
		}

		@Override
		public void scheduleLinkStoring() {
			for( PeerListener l : listeners )
				l.scheduleLinkStoring();
		}

		@Override
		public void innerException( Exception e ) {
			for( PeerListener l : listeners )
				l.innerException( e );
		}
	}

	protected class Linkrejectingtask extends TimerTask {
		final long rejectafter;

		public Linkrejectingtask( long time ) {
			rejectafter = time;
		}

		@Override
		public void run() {
			Lamilastatics.println( OutputKind.INFO, "Clean up old links" );
			dls.scheduleLinkRejecting();
			long time = System.currentTimeMillis();
			synchronized ( hostednodes ) {
				for( Correlator c : correlators.getCorrelators() ) {
					for( Node n : hostednodes.values() ) {
						List<Link> ls = n.getLinks( c.getCorrelatorId() );
						for( Link l : ls ) {
							long dif = time - l.lastContact();
							if( dif > rejectafter ) {
								n.removeLink( c.getCorrelatorId(), l.getNode() );
								dls.linkRemoved( n, c, l.getNode() );
							}
						}
					}
				}
			}
		}
	}

	protected class LinkStoringTask extends TimerTask {
		@Override
		public void run() {
			Lamilastatics.println( OutputKind.INFO, "ResortLinks" );
			long time = System.currentTimeMillis();
			dls.scheduleLinkStoring();
			synchronized ( hostednodes ) {
				for( Correlator c : correlators.getCorrelators() ) {
					for( Node n : hostednodes.values() ) {
						List<Link> ls = n.getLinks( c.getCorrelatorId() );
						// for ( Link l : ls ){//FIXME get the LinkStoringTask (republisching) right
						try {
							publish( c, n, republishingadvisor );
						} catch ( OperationFailedException e ) {
							System.err.println( "Republishen fehlgeschlagen." );
							e.printStackTrace();
						}
						// }
					}
				}
			}
		}
	}

	private final class ByteArrayWrapper {
		private final byte[] towarp;

		public ByteArrayWrapper( byte[] data ) {
			if( data == null ) {
				throw new NullPointerException();
			}
			this.towarp = data;
		}

		@Override
		public boolean equals( Object o ) {
			if( o instanceof ByteArrayWrapper )
				return Arrays.equals( towarp, ( (ByteArrayWrapper) o ).towarp );
			return false;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode( towarp );
		}

		@Override
		public String toString() {
			return Arrays.toString( towarp );
		}
	}
}
