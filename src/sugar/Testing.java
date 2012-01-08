package sugar;

import globalstatic.Lamilastatics;
import iospeci.Buffercontent;
import iospeci.PointerRequest;
import iospeci.Requestmessage;
import iospeci.StoreRequest;
import iospeci.StoreResponse;
import iospeci.transport.ResponsePreview;
import iospeci.transport.UDPTransport.UDPTransporter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import searching.Fireworksearchstrategie;
import searching.SearchAdvisor;
import urlocator.Link;
import urlocator.Node;
import urlocator.OperationFailedException;
import urlocator.Peer;
import correlation.Correlators;
import correlation.Hashcodecorrelator;
import correlation.MetaCorellator;

public class Testing {
	static Random random;
	static boolean randomtags = false;
	static List<String> names = new LinkedList<String>();
	static Iterator<String> keys;
	static {
		long time = System.nanoTime();
		random = new Random( 1 );
		names.add( "PROFILE,DAVID ROHMER,LEON DER PROFI" );
		names.add( "PROFILE,Lusie Rohmer,LUlady" );
		names.add( "PROFILE,LOLO,Maximilian" );
		names.add( "PROFILE,Killer3,Rambo" );
		names.add( "PROFILE,Piller4,Kim" );
		names.add( "PROFILE,KilleR§§,Tim" );
		names.add( "PROFILE,KILLEA99,Tomas" );
		names.add( "PROFILE,SUnyboy,Floris" );
		names.add( "PROFILE,Sigfried,Birgut" );
		names.add( "PROFILE,Jamboo,Jonas" );
		names.add( "PROFILE,Sieger,kasper" );
		names.add( "PROFILE,Mamboguy,Rocker" );
		names.add( "PROFILE,Lolly,Steffani" );
		names.add( "PROFILE,Jumpa,Jocker" );
		names.add( "PROFILE,Nico69,Tetser" );
		names.add( "PROFILE,LILA9,Joachim" );
		names.add( "A" );
		names.add( "AA" );
		names.add( "AAA" );
		names.add( "AAAA" );
		names.add( "AAAAA" );
		names.add( "AAAAAA" );
		names.add( "AAAAAAA" );
		names.add( "AAAAAAAA" );
		names.add( "AAAAAAAAAA" );
		names.add( "AAAAAAAAAAA" );
		names.add( "AAAAAAAAAAAA" );
		names.add( "AAAAAAAAAAAAA" );
		names.add( "AAAAAAAAAAAAAA" );
		names.add( "AAAAAAAAAAAAAAA" );
		names.add( "AAAAAAAAAAAAAAAA" );
		names.add( "AAAAAAAAAAAAAAAAA" );
		names.add( "AAAAAAAAAAAAAAAAAA" );
		names.add( "AAAAAAAAAAAAAAAAAAA" );
		names.add( "AAAAAAAAAAAAAAAAAAAA" );
		names.add( "AAAAAAAAAAAAAAAAAAAAA" );
		names.add( "AAAAAAAAAAAAAAAAAAAAAA" );
		names.add( "AAAAAAAAAAAAAAAAAAAAAAA" );
		names.add( "PROFILE,David" );
		names.add( "PROFILE,Davidius" );
		names.add( "PROFILE,Davidd" );
		names.add( "PROFILE,DaviT" );
		names.add( "PROFILE,Dadi" );
		names.add( "PROFILE,Doni" );
		names.add( "PROFILE,Coni" );
		names.add( "PROFILE,Die" );
		keys = names.iterator();
	}

	/**
	 * @param args
	 * @throws UnknownHostException
	 */
	public static void main( String[] args ) throws Exception {
		int bin = 0xF;
		int mask = 0x8;
		try {
			// new UDPTest( 1000 );
			// globalstatics.println(bin & mask);
			Lamilastatics.setup();
			testTagDiff();
			testMessages();
			testCommunication();
		} catch ( Exception e ) {
			e.printStackTrace();
			System.exit( 1 );
		}
		// System.exit(1);
	}

	public static String getNextTags() {
		if( randomtags )
			return createRandomTagString();
		else
			return keys.next();
	}

	public static int getNetworksize() {
		if( randomtags )
			return 100;
		else
			return names.size();
	}

	public static void testCommunication() throws Exception {
		Lamilastatics.println( "Teste Kommunikation" );
		Correlators correls = new Correlators();
		correls.registerCorrelator( Hashcodecorrelator.getInstance() );
		correls.registerCorrelator( MetaCorellator.getInstance() );

		InetSocketAddress adress = new InetSocketAddress( InetAddress.getLocalHost(), 4000 );

		DatagramSocket socket = new DatagramSocket( adress );
		UDPTransporter transport = new UDPTransporter( socket );
		Peer networkcon = new Peer( transport, correls );
		// networkcon.connect();
		Networkviewer viewer = new Networkviewer( networkcon, false );
		viewer.viewInFrame();
		TestNode networkentry = new TestNode( adress, "uri0" );
		networkentry.putKey( MetaCorellator.id, getNextTags().getBytes( Lamilastatics.charset ) );
		int convictions = 4;
		int leafe = 4;
		int networksize = getNetworksize();
		MetaCorellator correl = MetaCorellator.getInstance();
		// SearchAdvisor advisorstatic = new StaticSeachStrategie( 10000 , 5 , 10 , 5 , 2000 , 6 );
		SearchAdvisor advisorfire = new Fireworksearchstrategie( Lamilastatics.tsTimeout( 10000 ), convictions, leafe );
		networkcon.publish( correl, networkentry, advisorfire );
		long sleeptime = 0;
		Thread.sleep( sleeptime );
		TestNode[] allnodes = new TestNode[ networksize ];
		Lamilastatics.println( "Erzeuge Netzwerk mit " + networksize + " Knoten" );
		long time1 = System.currentTimeMillis();

		for( int i = 1 ; i < networksize ; i++ ) {
			allnodes[ i ] = new TestNode( adress, "uri" + i );
			allnodes[ i ].putKey( MetaCorellator.id, getNextTags().getBytes( Lamilastatics.charset ) );
			LinkedList<Node> resp = null;

			try {
				resp = networkcon.publish( correl, allnodes[ i ], advisorfire );
			} catch ( OperationFailedException e ) {
				e.printStackTrace();
			}
			Thread.sleep( sleeptime );
			if( resp == null ) {
				System.err.println( "publisching of " + allnodes[ i ] + " failed" );
				continue;
			}
			// viewer.addNode(allnodes[i], new MetaCorellator());
			assert ( resp.size() != 0 );
		}
		System.err.println( "Erzeugung abgeschlossen nach" + ( ( (double) System.currentTimeMillis() - time1 ) / 1000 ) + " Sekunden" );
		for( int i = 1 ; i < networksize ; i++ ) {
			// globalstatics.print( new String( allnodes[i].getKey( MetaCorellator.correlatorid ) , globalstatics.charset ) + ": " );
			for( Link l : allnodes[ i ].getLinks( MetaCorellator.id ) ) {
				Lamilastatics.print( new String( l.getNode().getKey( MetaCorellator.id ), Lamilastatics.charset ) + " | " );
			}
			Lamilastatics.println( "" );
		}
		time1 = System.currentTimeMillis();
		for( int i = 1 ; i < networksize ; i++ )
			for( int j = 1 ; j < networksize ; j++ ) {
				Node search = allnodes[ i ];
				LinkedList<Node> searchlist = new LinkedList<Node>();
				searchlist.add( new TestNode( allnodes[ j ] ) );
				try {
					Node result = networkcon.searchFor( MetaCorellator.id, search.getKey( MetaCorellator.id ), advisorfire, searchlist ).getFirst();
				} catch ( OperationFailedException e ) {
					e.printStackTrace();
				}
			}
		System.out.println( "in " + ( ( (double) System.currentTimeMillis() - time1 ) / ( (double) networksize * networksize ) / 1000 ) + " Sekunden" );
		System.out.println( "iocount: \n" + networkcon.iocount );
		Lamilastatics.println( "keine Probleme" );
		System.gc();
	}

	public static void testMessages() throws UnknownHostException {
		Lamilastatics.println( "testing Messages" );
		TestNode testnode = new TestNode( new InetSocketAddress( InetAddress.getLocalHost(), 4000 ), "testnode" );
		testnode.putKey( MetaCorellator.id, "profile:/lastlord".getBytes( Lamilastatics.charset ) );
		PointerRequest prq = new PointerRequest( testnode.getAdress(), testnode.getUniqueId(), MetaCorellator.id, "hallo Welt".getBytes( Lamilastatics.charset ), 2 );
		prq.setFrom( testnode.getAdress() );
		PointerRequest prq2 = new PointerRequest( prq.getBytes().length, prq.getBytes() );
		Lamilastatics.println( Arrays.toString( prq.getBytes() ) );
		Lamilastatics.println( Arrays.toString( prq2.getBytes() ) );
		assert ( Arrays.equals( prq.getBytes(), prq2.getBytes() ) );
		assert ( new String( prq.getKey(), Lamilastatics.charset ).equals( "hallo Welt" ) );
		assert ( prq.getCorrelatorId() == MetaCorellator.id );
		assert ( Arrays.equals( testnode.getUniqueId(), prq.getNodeId() ) );
		assert ( Arrays.equals( testnode.getUniqueId(), prq2.getNodeId() ) );
		List<Node> sns = new LinkedList<Node>();
		sns.add( new TestNode( testnode ) );
		sns.add( new TestNode( testnode ) );
		sns.add( new TestNode( testnode ) );
		sns.add( new TestNode( testnode ) );
		sns.add( new TestNode( testnode ) );
		sns.add( new TestNode( testnode ) );
		ResponsePreview prp = new ResponsePreview( prq, 2 );
		ResponsePreview prp2 = new ResponsePreview( prp.getBytes().length, prp.getBytes() );
		assert ( Arrays.equals( prp.getBytes(), prp2.getBytes() ) );
		assert ( prp.getIncommingMessageCount() == 2 );
		assert ( prp.getRequestMessageId() == 1 );
		StoreRequest storereq = new StoreRequest( new TestNode( testnode ), testnode, MetaCorellator.id );
		assert ( storereq.getCorrelatorId() == MetaCorellator.id );
		assert ( Arrays.equals( storereq.getStoreNodeId(), testnode.getUniqueId() ) );
		assert ( Arrays.equals( storereq.getToStore().getKey( MetaCorellator.id ), testnode.getKey( MetaCorellator.id ) ) );
		assert ( Arrays.equals( storereq.getToStore().getUniqueId(), testnode.getUniqueId() ) );
		assert ( storereq.getMessageId() == Requestmessage.getNextId() - 1 );
		// System.out.println(storereq.getMessageId()+"=="+(Requestmessage.getNextId()-2));
		StoreResponse storeresp = new StoreResponse( prq, 1001020112 );
		assert ( storeresp.getRequestMessageId() == prp.getRequestMessageId() );
		assert ( storeresp.getTimelapse() == 1001020112 );
		storeresp.setCryption( Buffercontent.RSA_ENCRYPTION );
		assert ( storeresp.getCryption() == Buffercontent.RSA_ENCRYPTION );
		storeresp.setCryption( Buffercontent.BLOWFISH_ENCRYPTION );
		assert ( storeresp.getCryption() == Buffercontent.BLOWFISH_ENCRYPTION );
		// assert(storereq.getMessageId()==Requestmessage.getNextId()-1);
		Lamilastatics.println( "no problems" );
	}

	public static void testTagDiff() {
		Lamilastatics.println( "Testing Tagdifferecefunction" );
		String tags1 = "David,Rohmer";
		String tags2 = "Rohmer,Dvvid";
		// globalstatics.print( tags1 + "->" + tags2 );
		Lamilastatics.println( " = " + MetaCorellator.getTagsDiff( tags1, tags2 ) );
		tags1 = "David,Rohmer";
		tags2 = "Rohmer,Dvvid";
		Lamilastatics.print( tags1 + "->" + tags2 );
		Lamilastatics.println( " = " + MetaCorellator.getTagsDiff( tags1, tags2 ) );
		tags1 = "David,Rohmer";
		tags2 = "Rohmer,Dvvidddd";
		Lamilastatics.print( tags1 + "->" + tags2 );
		Lamilastatics.println( " = " + MetaCorellator.getTagsDiff( tags1, tags2 ) );
		tags1 = "David,Rohmer";
		tags2 = "XD,lol";
		Lamilastatics.print( tags1 + "->" + tags2 );
		Lamilastatics.println( " = " + MetaCorellator.getTagsDiff( tags1, tags2 ) );
		tags1 = "David,Rohmer";
		tags2 = "myy,Dvvid";
		Lamilastatics.print( tags1 + "->" + tags2 );
		Lamilastatics.println( " = " + MetaCorellator.getTagsDiff( tags1, tags2 ) );
		tags1 = "David,Rohmer";
		tags2 = "Rohmer";
		Lamilastatics.print( tags1 + "->" + tags2 );
		Lamilastatics.println( " = " + MetaCorellator.getTagsDiff( tags1, tags2 ) );
		tags2 = "David,Rohmer";
		tags1 = "Rohmera";
		Lamilastatics.print( tags1 + "->" + tags2 );
		Lamilastatics.println( " = " + MetaCorellator.getTagsDiff( tags1, tags2 ) );
		tags2 = "David,Rohmer";
		tags1 = "Rohmera";
		Lamilastatics.print( tags1 + "->" + tags2 );
		Lamilastatics.println( " = " + MetaCorellator.getTagsDiff( tags1, tags2 ) );
		tags2 = "A";
		tags1 = "AAA";
		Lamilastatics.print( tags1 + "->" + tags2 );
		Lamilastatics.println( " = " + MetaCorellator.getTagsDiff( tags1, tags2 ) );
		tags2 = "";
		tags1 = "AAA";
		Lamilastatics.print( tags1 + "->" + tags2 );
		Lamilastatics.println( " = " + MetaCorellator.getTagsDiff( tags1, tags2 ) );
		tags2 = "";
		tags1 = "";
		Lamilastatics.print( tags1 + "->" + tags2 );
		Lamilastatics.println( " = " + MetaCorellator.getTagsDiff( tags1, tags2 ) );
		// System.out.println("155 "+Integer.parseInt(Integer.toBinaryString(155),2));
		int byte1 = Integer.parseInt( "100110", 2 );
		int byte2 = Integer.parseInt( "010111", 2 );
		byte[] b2 = { (byte) byte1, 0 };
		byte[] b1 = { (byte) byte2, 0 };
		// System.out.println("HAshdiff: "+Correlators.getHashDiff(b1, b2));
		// System.out.println();
	}

	public static String createRandomTagString() {
		int charcount = (int) ( random.nextFloat() * 45 + 5 );
		int seperators = (int) ( random.nextFloat() * 6 );
		char[] chars = new char[ charcount ];
		for( int i = 0 ; i < charcount ; i++ )
			chars[ i ] = (char) ( 49 + random.nextFloat() * ( 60 ) );
		for( int i = 0 ; i < seperators ; i++ )
			chars[ (int) ( random.nextFloat() * charcount ) ] = ',';
		return new String( chars );
	}
}

class UDPTest {
	DatagramSocket datagram;
	DatagramPacket packet;
	InetAddress adress;

	public UDPTest( int messagecount ) throws IOException {
		adress = InetAddress.getByName( "127.0.0.1" );
		datagram = new DatagramSocket( 8009, adress );
		packet = new DatagramPacket( new byte[ 1000 ], 1000, adress, 8009 );
		int i = 0;
		for( ; i < messagecount ; i++ )
			datagram.send( packet );
		i = 1;
		while ( true ) {
			datagram.receive( packet );
			System.out.println( "ret:" + i++ );
		}
	}
}
