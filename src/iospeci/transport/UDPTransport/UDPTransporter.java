package iospeci.transport.UDPTransport;

import globalstatic.Lamilastatics;
import globalstatic.Lamilastatics.OutputKind;
import iospeci.Buffercontent;
import iospeci.CryptManager;
import iospeci.DeCryptException;
import iospeci.DirectedMessage;
import iospeci.EnCryptException;
import iospeci.IdRange;
import iospeci.InvalidPackageException;
import iospeci.OpenRange;
import iospeci.UnknowTypeException;
import iospeci.transport.OpenIdRange;
import iospeci.transport.OrderMessage;
import iospeci.transport.PacketPreview;
import iospeci.transport.RecepientInputStream;
import iospeci.transport.Reordermessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import urlocator.IgnoringPackageException;
import urlocator.Messagehandler;
import urlocator.TransportLayer;
import urlocator.UnknowPacketTypeException;

public class UDPTransporter implements TransportLayer {
	private static Timer timer;
	static {
		Buffercontent.addOptainer( OrderMessage.class );
		Buffercontent.addOptainer( PacketPreview.class );
		Buffercontent.addOptainer( Reordermessage.class );

		timer = new Timer( "PacketSheduler", true );
	}
	private Thread thisthread;
	private DatagramSocket socket;
	private Messagehandler handeler;
	private CryptManager crypt;

	/** Contains the Tasks which will send packages. Contains no canceled tasks */
	private HashMap<Integer,PacketDeliverer> connector;
	/** <othersocket, <packetid, transfereid>> */
	private Hashtable<SocketAddress,Hashtable<Integer,Integer>> awaited;
	// private Hashtable<SocketAddress, Hashtable<Integer, Recepient>> recept;
	/** <othersocket, <transfereid,ordertask >> */
	private Hashtable<SocketAddress,HashMap<Integer,PacketDeliverer>> activeorders;
	/** The data this transporter is about to deliver via UDP */
	private Hashtable<Integer,RecepientInputStream> todeliver;
	/** The packages have been delivered but might not have been arrived */
	private Hashtable<SocketAddress,CrumblingMap<Integer,byte[]>> tokeep;
	/** <othersocket, <transfereid,Recepient >> */
	private Hashtable<SocketAddress,HashMap<Integer,Recepient>> recepiencts;

	private long timeout = Lamilastatics.tsTimeout( 5000 );
	private long repeatcount = 10;
	private long interval = timeout / ( repeatcount - 1 );

	public UDPTransporter( DatagramSocket socket ) {
		this.socket = socket;

		// Constructor<Hashtable<Integer, Integer>> constr = Hashtable.class.getConstructor();
		connector = new HashMap<Integer,UDPTransporter.PacketDeliverer>();

		awaited = new Hashtable<SocketAddress,Hashtable<Integer,Integer>>();

		// recept = new Hashtable<SocketAddress, Hashtable<Integer, Recepient>>();
		activeorders = new Hashtable<SocketAddress,HashMap<Integer,PacketDeliverer>>();
		todeliver = new Hashtable<Integer,RecepientInputStream>();
		tokeep = new Hashtable<SocketAddress,CrumblingMap<Integer,byte[]>>();
		recepiencts = new Hashtable<SocketAddress,HashMap<Integer,Recepient>>();
		new Thread( this ).start();

	}

	@Override
	public void run() {
		thisthread = Thread.currentThread();
		byte[] data = new byte[ 5000 ];
		DatagramPacket packet = new DatagramPacket( data, data.length );
		while ( !Thread.currentThread().isInterrupted() ) {
			try {
				socket.receive( packet );
				handle( packet );
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
		Lamilastatics.println( OutputKind.WARNING, "Peer down" );
	}

	@Override
	public void setMessageHandler( Messagehandler handler ) {
		this.handeler = handler;
	}

	@Override
	public Thread getThread() {
		return thisthread;
	}

	@Override
	public void send( Buffercontent m, byte[] adress, int port ) throws UnknownHostException , IOException {

		send( new DirectedContet( m, adress, port ) );
	}

	private void handle( DatagramPacket packet ) throws IOException , UnknowTypeException , InvalidPackageException , UnknowPacketTypeException , IgnoringPackageException , DeCryptException {
		byte[] bytes = decryptAsNessesary( (InetSocketAddress) packet.getSocketAddress(), packet.getData(), packet.getLength() );

		Buffercontent buf = Buffercontent.convert( (InetSocketAddress) packet.getSocketAddress(), bytes.length, bytes );
		Lamilastatics.println( OutputKind.TRANSPORT, "handle " + buf );
		if( buf.getMessageType() < 0 )
			processTransport( (InetSocketAddress) packet.getSocketAddress(), buf );
		else
			processPackage( packet.getSocketAddress(), buf );
	}

	private synchronized void send( DatagramPacket pack ) throws IOException {
		try {
			Buffercontent c = Buffercontent.convert( (InetSocketAddress) pack.getSocketAddress(), pack.getData().length, pack.getData() );

			if( Lamilastatics.debugoutput ) {
				Lamilastatics.println( OutputKind.TRANSPORT, "write  " + c.toString() + " :" );
				/*
				StackTraceElement[] stack = Thread.currentThread().getStackTrace();
				for( int i = 0 ; i < stack.length ; i++ ) {
					if( !stack[ i ].toString().contains( ".send" ) && i > 0 ) {
						Lamilastatics.println( OutputKind.TRANSPORT, stack[ i ].toString() );
						break;
					}
				}*/
				// new Throwable().printStackTrace( System.out );
			}
		} catch ( Exception e ) {
			if( Buffercontent.getCryption( pack.getData() ) != Buffercontent.NO_ENCRYPTION )
				Lamilastatics.println( OutputKind.TRANSPORT, "Send Crypted XXX" );
			else
				throw new RuntimeException( e );
		}
		// tokeep.get( pack.getSocketAddress() );
		// if( pack.getSocketAddress() == socket.getLocalSocketAddress() )
		// System.out.println( "loopback" );
		socket.send( pack );
	}

	private void processPackage( SocketAddress adres, Buffercontent buf ) throws SocketException , InvalidPackageException , IOException , UnknowPacketTypeException , IgnoringPackageException {

		Hashtable<Integer,Integer> aw = awaited.get( adres );
		if( aw == null || aw.isEmpty() )
			return;
		if( aw.isEmpty() ) {
			awaited.remove( adres );
			Lamilastatics.println( OutputKind.ERROR, "Dropped (not awaited)" + buf );
		}
		Integer transid = aw.remove( buf.getPacketId() );

		HashMap<Integer,Recepient> recs = recepiencts.get( adres );
		if( recs == null ) {
			Lamilastatics.println( OutputKind.ERROR, "Dropped (unkow peer)" + buf );
			return;
		}
		Recepient r = recs.get( transid );
		if( r == null ) {
			Lamilastatics.println( OutputKind.ERROR, "Dropped (no recepienct)" + buf );
			return;
		}
		HashMap<Integer,PacketDeliverer> orders = activeorders.get( adres );// Stop the active ordering
		if( orders != null ) {
			PacketDeliverer deliv = orders.remove( transid );
			if( deliv != null )
				deliv.cancel();
			if( orders.isEmpty() )
				activeorders.remove( adres );
		}

		r.feed( buf );
	}

	private void processTransport( InetSocketAddress from, Buffercontent buf ) throws IOException {
		if( buf instanceof PacketPreview ) {
			PacketPreview prev = (PacketPreview) buf;

			// Check if the message is a duplicate
			HashMap<Integer,PacketDeliverer> otherorders = activeorders.get( from );
			if( otherorders == null )
				otherorders = new HashMap<Integer,PacketDeliverer>( 4 );
			else if( otherorders.containsKey( prev.getPreviewedId() ) ) {
				Lamilastatics.println( OutputKind.ERROR, "Discard dublicate" + prev.getPacketId() );
				return;
			}
			// Prepare Receiving
			Hashtable<Integer,Integer> awaitedpackages = awaited.get( from );
			if( awaitedpackages == null ) {
				awaitedpackages = new Hashtable<Integer,Integer>();
				awaited.put( from, awaitedpackages );
			}

			int transid = prev.getPreviewedId();
			for( int i = 0 ; i < prev.getIncommingMessageCount() ; ++i ) {
				if( awaitedpackages.contains( transid + i ) )
					System.out.println( "Doppelte Arbeit" );
				awaitedpackages.put( transid + i, transid );
			}

			HashMap<Integer,Recepient> recs = recepiencts.get( from );
			if( recs == null ) {
				recs = new HashMap<Integer,UDPTransporter.Recepient>();
				recepiencts.put( from, recs );
			}
			Recepient recepient = new Recepient( from, prev.getPreviewedId(), prev.getIncommingMessageCount(), prev.getCryption() );
			recs.put( prev.getPreviewedId(), recepient );

			// Order the Data
			OrderMessage order = new OrderMessage( prev.getPreviewedId() );
			byte[] bytes = encryptAsNessesary( from, order );
			DatagramPacket packet = new DatagramPacket( bytes, bytes.length, from );
			PacketDeliverer delv = new PacketDeliverer( packet, repeatcount );
			otherorders.put( prev.getPreviewedId(), delv );
			activeorders.put( from, otherorders );
			timer.schedule( delv, Lamilastatics.tsTimeout( interval ), interval );
			send( packet );
		} else if( buf instanceof OrderMessage ) {
			OrderMessage order = (OrderMessage) buf;
			// Stop sending the connecting message if its not already done.
			PacketDeliverer delv = connector.remove( new Integer( order.getPacketId() ) );
			if( delv != null ) {
				delv.cancel();
			} else {
				/**
				 * Occurs if the RecepientInputStream(see lines below ) failes
				 * to reach the destenation. In this case the destenation
				 * resends the Order over and over again until a timeout
				 */
				Lamilastatics.println( OutputKind.WARNING, "Recurring Order for" + order.getPacketId() );
			}
			RecepientInputStream todeliv = todeliver.get( order.getPacketId() );
			if( todeliv == null ) {
				/**
				 * This case occurs if the delivery failed the first time. The
				 * requested data will than tried to be resent be looking up
				 * cached data
				 */

				if( resent( socket.getLocalSocketAddress(), order.getPacketId() ) )
					Lamilastatics.println( "Resend" + order.getPacketId() + " successful" );
				else
					Lamilastatics.println( "Resend" + order.getPacketId() + " failed" );
			} else
				todeliv.flush();// deliver the requested data for the first time
		} else if( buf instanceof Reordermessage ) {
			resent( socket.getLocalSocketAddress(), ( (Reordermessage) buf ).getOrederdpacketId() );
		} else
			throw new UnknowPacketTypeException( "Does not know how to handle" + buf );

	}

	private boolean resent( SocketAddress sock, int packid ) throws IOException {
		CrumblingMap<Integer,byte[]> map = tokeep.get( sock );
		tokeep.toString();
		if( map != null ) {
			byte[] buff = map.get( packid );
			if( buff != null ) {
				/* try{
				 * if( Buffercontent.convert( buff.length , buff
				 * ).getPacketId() != packid )
				 * System.out.println( "Illegalll" );
				 * } catch ( UnknowMessagetypeException e ){
				 * e.printStackTrace();
				 * } */

				send( new DatagramPacket( buff, buff.length, sock ) );
				return true;
				// Found and sent an cached message
			}
		}
		return false;// The is no cached data for the connection or the message
	}

	public void repeatPacket( DatagramPacket packet ) {
		long repeates = this.repeatcount;
		while ( repeates > 0 ) {
			try {
				send( packet );
				Thread.sleep( interval );
			} catch ( InterruptedException e ) {
				return;
			} catch ( IOException e ) {
				e.printStackTrace();
			}
			repeates--;
		}
		// senders.remove( m.getPacketId() );
	}

	/* private void send( SocketAddress address , DirectedMessage packet )
	 * throws IOException{
	 * byte[] data = packet.getContent().getBytes();
	 * DatagramPacket datagrampacket = new DatagramPacket( data , data.length );
	 * socket.send( datagrampacket );
	 * } */

	/***/
	@Override
	public void send( DirectedMessage mes ) throws UnknownHostException , IOException {
		// if( mes.getPort() == socket.getLocalPort() && Arrays.equals( mes.getHost() , socket.getLocalAddress().getAddress() ) )
		// System.out.println( "loopback" + mes.getPort() + ", " + socket.getLocalPort() );
		Integer packageid = mes.getContent().getPacketId();
		SocketAddress adress = new InetSocketAddress( InetAddress.getByAddress( mes.getHost() ), mes.getPort() );
		// Keep the data for delivery
		RecepientInputStream recin = new RecepientInputStreamImpl( new OpenIdRange( packageid, 1 ), todeliver.values(), adress );
		recin.append( mes.getContent() );
		todeliver.put( packageid, recin );

		sendPreview( packageid, 1, adress, mes.getContent().getCryption() );

	}

	private void sendPreview( IdRange range, SocketAddress adress, byte cryption ) throws IOException {
		sendPreview( range.getTransfereId(), range.getTotalIdCount(), adress, cryption );
	}

	private void sendPreview( int packageid, int count, SocketAddress adress, byte cryption ) throws IOException {
		// Preview the data to the sender
		PacketPreview preview = new PacketPreview( count, packageid );
		preview.setCryption( cryption );
		byte bytes[] = encryptAsNessesary( (InetSocketAddress) adress, preview );
		DatagramPacket packet = new DatagramPacket( bytes, bytes.length, adress );
		PacketDeliverer deliver = new PacketDeliverer( packet, repeatcount );
		// System.out.println( "put packegeid " + packageid );
		connector.put( packageid, deliver );// Save the Task to be able to cancel it if a response arrives
		timer.schedule( deliver, Lamilastatics.tsTimeout( (long) interval ), (long) interval );
		send( packet );// Sending the data a first time because the timer
						// will shedule the given task delayed.

	}

	private void send( Integer packid, DatagramPacket pack ) throws IOException {
		CrumblingMap<Integer,byte[]> m = tokeep.get( pack.getSocketAddress() );
		if( m == null ) {
			m = new CrumblingMap<Integer,byte[]>( 100 );// TODO made max mapsize configurable
			tokeep.put( pack.getSocketAddress(), m );
		}
		m.put( packid, pack.getData() );
		send( pack );
	}

	@Override
	public InetSocketAddress getAdress() {
		return (InetSocketAddress) socket.getLocalSocketAddress();
	}

	@Override
	public RecepientInputStream previewTransfere( SocketAddress adress, OpenRange range, byte cryption ) throws IOException {
		RecepientInputStream recin = new RecepientInputStreamImpl( range, todeliver.values(), adress );
		todeliver.put( range.getTransfereId(), recin );
		sendPreview( range, adress, cryption );
		return recin;
	}

	public void setCrypter( CryptManager crypt ) {
		this.crypt = crypt;
	}

	public byte[] encryptAsNessesary( InetSocketAddress adress, Buffercontent buf ) throws EnCryptException {
		byte cryption = buf.getCryption();
		byte[] thebytes = buf.getBytes();
		if( cryption == Buffercontent.NO_ENCRYPTION )
			return thebytes;
		else if( cryption == Buffercontent.BLOWFISH_ENCRYPTION )
			return crypt.encryptBlow( adress, buf.getBytes() );
		else if( cryption == Buffercontent.RSA_ENCRYPTION )
			return crypt.encryptRSA( adress, buf.getBytes() );
		throw new IllegalArgumentException( "Unknow Cryption Flag" );
	}

	public byte[] decryptAsNessesary( InetSocketAddress adress, byte[] bytes, int length ) throws DeCryptException {
		byte cryption = Buffercontent.getCryption( bytes );
		if( cryption == Buffercontent.NO_ENCRYPTION )
			return Arrays.copyOf( bytes, length );
		else if( cryption == Buffercontent.BLOWFISH_ENCRYPTION )
			return crypt.decryptBlow( adress, bytes, 0, length );
		else if( cryption == Buffercontent.RSA_ENCRYPTION )
			return crypt.decryptRSA( adress, bytes, 0, length );
		throw new DeCryptException( "Unknow Cryption Flag" );
	}

	class PacketDeliverer extends TimerTask {
		private DatagramPacket tosend;
		private long gangways;
		int bytekey;

		// Exception e;

		public PacketDeliverer( DatagramPacket packet , long gangways ) {
			tosend = new DatagramPacket( packet.getData(), packet.getOffset(), packet.getLength(), packet.getAddress(), packet.getPort() );
			this.gangways = gangways;
			bytekey = Arrays.hashCode( packet.getData() );
			// e = new Exception();
			// System.out.println( this + " created" );
			// e.printStackTrace( System.out );
		}

		@Override
		public void run() {
			System.out.print( "repeat " + gangways + " " );
			// System.out.println( toString() + " send again (" + gangways + ") = " + tosend.hashCode() );
			// e.printStackTrace( System.out );
			try {
				assert ( bytekey == Arrays.hashCode( tosend.getData() ) );
				send( tosend );
			} catch ( Exception e ) {
				e.printStackTrace();
				cancel();
			}
			if( --gangways <= 0 ) {
				cancel();
			}
		}

		@Override
		public boolean cancel() {
			super.cancel();
			assert ( bytekey == Arrays.hashCode( tosend.getData() ) );
			return super.cancel();

		}

		@Override
		public String toString() {
			return super.toString();
		}

	}

	class Recepient extends TimerTask {
		LinkedList<Buffercontent> buffer;
		int deliverindex;
		LinkedList<Integer> missingpackages;
		SocketAddress adess;
		long lastcontact;
		byte cryption;

		public Recepient( SocketAddress adess , int trans , int count , byte cryption ) {
			this.adess = adess;
			this.cryption = cryption;
			missingpackages = new LinkedList<Integer>();
			buffer = new LinkedList<Buffercontent>();
			deliverindex = trans;
			for( int i = 0 ; i < count ; ++i )
				missingpackages.add( trans + i );
			lastcontact = System.currentTimeMillis();
		}

		public void feed( Buffercontent buf ) throws SocketException , IOException , InvalidPackageException , UnknowPacketTypeException , IgnoringPackageException {
			int bid = buf.getPacketId();
			synchronized ( missingpackages ) {
				missingpackages.remove( new Integer( bid ) );
				buffer.add( buf );
			}
			for( Integer id : missingpackages ) {
				if( id < bid ) {
					Reordermessage reord = new Reordermessage( id );
					reord.setCryption( cryption );
					byte[] bytes = encryptAsNessesary( (InetSocketAddress) adess, reord );
					send( new DatagramPacket( bytes, bytes.length, adess ) );
				} else
					return;
			}
			Collections.sort( buffer, new Comparator<Buffercontent>() {

				@Override
				public int compare( Buffercontent o1, Buffercontent o2 ) {
					return o1.getPacketId() - o2.getPacketId();
				}
			} );
			Iterator<Buffercontent> it = buffer.iterator();
			while ( it.hasNext() ) {
				Buffercontent b = it.next();
				if( b.getPacketId() == deliverindex ) {
					++deliverindex;
					Lamilastatics.println( OutputKind.TRANSPORT, "feed   " + b );
					handeler.handlePackage( UDPTransporter.this, b );
					it.remove();
				}
			}
		}

		@Override
		public void run() {
			if( System.currentTimeMillis() - lastcontact > timeout ) {
				cancel();
			}
			for( Integer id : missingpackages ) {

				byte[] bytes = encryptAsNessesary( (InetSocketAddress) adess, new Reordermessage( id ) );
				try {
					send( new DatagramPacket( bytes, bytes.length, adess ) );
				} catch ( Exception e ) {
					e.printStackTrace();
					cancel();
				}
			}
		}

		@Override
		public boolean cancel() {
			return super.cancel();
		}

		@Override
		protected void finalize() throws Throwable {
			super.finalize();
		}
	}

	class RecepientInputStreamImpl implements RecepientInputStream {
		private final LinkedList<Buffercontent> list;
		private final int totransfere;
		private final Collection<RecepientInputStream> parentcontainer;
		private final DatagramPacket pack;
		private OpenRange range = null;

		private boolean flushed = false;
		private int transfered = 0;

		public RecepientInputStreamImpl( OpenRange range , Collection<RecepientInputStream> parentcontainer , SocketAddress address ) throws SocketException {
			totransfere = range.getTotalIdCount();
			this.parentcontainer = parentcontainer;
			list = new LinkedList<Buffercontent>();
			pack = new DatagramPacket( new byte[ 1 ], 1, address );
			this.range = range;
		}

		/* (non-Javadoc)
		 * 
		 * @see iospeci.transport.RecepientInputStream#flush() */
		@Override
		public synchronized void flush() throws IOException {
			try {
				Iterator<Buffercontent> it = list.iterator();
				while ( it.hasNext() ) {
					Buffercontent cont = it.next();
					byte[] buf = encryptAsNessesary( (InetSocketAddress) pack.getSocketAddress(), cont );
					pack.setData( buf, 0, buf.length );
					send( cont.getPacketId(), pack );
				}
				transfered = list.size();
				flushed = true;
				list.clear();
			} finally {
				tryToStop();
			}
		}

		private void tryToStop() {
			if( transfered >= totransfere ) {
				synchronized ( parentcontainer ) {
					// new Exception().printStackTrace( System.out );
					parentcontainer.remove( this );
				}
			}
		}

		/* (non-Javadoc)
		 * 
		 * @see
		 * iospeci.transport.RecepientInputStream#append(iospeci.Buffercontent) */
		@Override
		public void append( Buffercontent b ) throws IOException {
			b.setId( range );

			if( flushed ) {
				byte[] buf = encryptAsNessesary( (InetSocketAddress) pack.getSocketAddress(), b );
				pack.setData( buf, 0, buf.length );
				send( pack );
				transfered++;
				tryToStop();
			} else
				list.add( b );
		}

		@Override
		public void applyRange( OpenRange range ) throws IOException {
			this.range = range;
		}
	}

}
