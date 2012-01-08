package sugar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.apache.commons.collections15.Transformer;

import urlocator.Link;
import urlocator.Node;
import urlocator.Peer;
import urlocator.PeerAdapter;
import correlation.Correlator;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Graphs;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.util.Animator;
import globalstatic.Lamilastatics;
import globalstatic.Lamilastatics.OutputKind;

public class Networkviewer {
	// Graph<V, E> where V is the type of the vertices
	// and E is the type of the edges
	Graph<MyVertex,MyEdge> g;
	AbstractLayout<MyVertex,MyEdge> layout;
	VisualizationViewer<MyVertex,MyEdge> vv;
	Peer p;
	boolean dircetd;

	public Networkviewer( boolean dircetd ) {
		init( dircetd );
	}

	public Networkviewer( Peer p , boolean dircetd ) {
		init( dircetd );
		this.p = p;
		p.addPeerListener( new PeerAdapter() {
			@Override
			public void linkAdded( Node hostednode, Correlator layer, Node linkednode ) {
				addNode( hostednode, layer );
			}

			@Override
			public void linkRemoved( Node hostednode, Correlator layer, Node linkednode ) {
				addNode( hostednode, layer );
			}
		} );
	}

	private void init( boolean dircetd ) {
		this.dircetd = dircetd;
		Graph<MyVertex,MyEdge> ig;
		if( dircetd )
			ig = Graphs.<MyVertex,MyEdge>synchronizedDirectedGraph( new DirectedSparseMultigraph<MyVertex,MyEdge>() );
		else
			ig = Graphs.<MyVertex,MyEdge>synchronizedUndirectedGraph( new UndirectedSparseMultigraph<MyVertex,MyEdge>() );
		ObservableGraph<MyVertex,MyEdge> og = new ObservableGraph<MyVertex,MyEdge>( ig );
		this.g = og;
		layout = new FRLayout2<MyVertex,MyEdge>( g );
		layout.setSize( new Dimension( 1024, 768 ) ); // sets the initial size
														// of
		// the
		configureRenderer();
	}

	int lastedgeid = 0;

	private MyVertex getEqualVertex( MyVertex s ) {
		for( MyVertex v : g.getVertices() )
			if( s.equals( v ) )
				return v;
		return null;
	}

	private List<MyVertex> getLinks( MyVertex s ) {
		List<MyVertex> list = new LinkedList<MyVertex>();
		for( MyEdge v : g.getEdges() )
			if( v.connects( s.n ) ) {
				MyVertex node2;
				if( v.n.equals( s.n ) )
					node2 = new MyVertex( v.n2, s.correlatoridlayer );
				else
					node2 = new MyVertex( v.n, s.correlatoridlayer );
				list.add( getEqualVertex( node2 ) );
			}
		return list;
	}

	Animator animator;

	public synchronized void addNode( Node node, Correlator c ) {
		// globalstatics.println("Refrsh " + node);
		// layout.lock(true);
		vv.getRenderContext().getPickedVertexState().clear();
		vv.getRenderContext().getPickedEdgeState().clear();
		MyVertex nodeasvertex = new MyVertex( node, c.getCorrelatorId() );
		if( !g.containsVertex( nodeasvertex ) ) {
			// globalstatics.println("node aufgenommen");
			g.addVertex( nodeasvertex );
		} else {
			// globalstatics.println("node bereits angezeigt");
			nodeasvertex = getEqualVertex( nodeasvertex );
		}
		vv.getRenderContext().getPickedVertexState().pick( nodeasvertex, true );
		List<Link> reallinks = node.getLinks( c.getCorrelatorId() );
		List<MyVertex> viewedlinked = getLinks( nodeasvertex );
		boolean updateui = false;
		for( MyVertex v : viewedlinked ) {
			boolean isuptodate = false;
			for( Link l : reallinks )
				if( l.getNode().equals( v.n ) )
					isuptodate = true;
			if( !isuptodate ) {
				updateui = true;
				g.removeVertex( v );
				Lamilastatics.println( OutputKind.INFO, "Trenne" + node + " von " + v.n );
			}
		}
		for( Link l : reallinks ) {
			Node n = l.getNode();
			MyVertex toolink = new MyVertex( n, c.getCorrelatorId() );
			if( !g.containsVertex( toolink ) ) {
				g.addVertex( toolink );
				Lamilastatics.println( OutputKind.INFO, "Neuer nachbar " + n );
			} else
				toolink = getEqualVertex( toolink );
			MyEdge newedge = getEdge( node, n, c );
			if( dircetd ) {
				if( g.findEdge( nodeasvertex, toolink ) == null ) {
					g.addEdge( newedge, nodeasvertex, toolink, EdgeType.DIRECTED );
					updateui = true;
				}
			} else if( !dircetd && g.findEdge( nodeasvertex, toolink ) == null && g.findEdge( toolink, nodeasvertex ) == null ) {
				g.addEdge( newedge, nodeasvertex, toolink, EdgeType.UNDIRECTED );
				updateui = true;
			} else
				continue;
			vv.getRenderContext().getPickedEdgeState().pick( newedge, true );
		}
		if( !updateui ) {
			return;
		}
		layout.initialize();
		Relaxer relaxer = new VisRunner( (IterativeContext) layout );
		relaxer.stop();
		relaxer.prerelax();
		StaticLayout<MyVertex,MyEdge> staticLayout = new StaticLayout<MyVertex,MyEdge>( g, layout );
		LayoutTransition<MyVertex,MyEdge> lt = new LayoutTransition<MyVertex,MyEdge>( vv, vv.getGraphLayout(), staticLayout );
		if( animator != null )
			animator.stop();
		animator = new Animator( lt );
		animator.setSleepTime( 35 );
		animator.start();
		vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
		vv.repaint();
	}

	public String getNode2String( Node n, int correlatorid ) {
		return new String( n.getKey( correlatorid ), Lamilastatics.charset ) + "(" + n.getLinks( correlatorid ).size() + ")";
	}

	public MyEdge getEdge( Node n, Node n2, Correlator c ) {
		return new MyEdge( c.correlate( n.getKey( c.getCorrelatorId() ), n2.getKey( c.getCorrelatorId() ) ), n, n2 );
	}

	private void configureRenderer() {
		vv = new VisualizationViewer<MyVertex,MyEdge>( layout );
		vv.setPreferredSize( new Dimension( 350, 350 ) ); // Sets the viewing
															// area
		vv.getModel().getRelaxer().setSleepTime( 3000 );
		DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
		gm.setMode( ModalGraphMouse.Mode.PICKING );
		vv.setGraphMouse( gm );
		vv.addKeyListener( gm.getModeKeyListener() );
		// vv.setGraphMouse(new DefaultModalGraphMouse<MyVertex, MyEdge>());
		// PluggableGraphMouse gm = new PluggableGraphMouse();
		// gm.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON1_MASK));
		// gm.add(new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0,
		// 1.1f, 0.9f));
		vv.getRenderer().getVertexLabelRenderer().setPosition( Renderer.VertexLabel.Position.CNTR );
		vv.getRenderContext().setVertexLabelTransformer( new Transformer<MyVertex,String>() {
			@Override
			public String transform( MyVertex v ) {
				return v.toString();
			}
		} );
		vv.getRenderContext().getEdgeLabelRenderer().setRotateEdgeLabels( true );
		vv.getRenderContext().setEdgeLabelTransformer( new Transformer<MyEdge,String>() {
			@Override
			public String transform( MyEdge e ) {
				return e.toString();
			}
		} );
		vv.setForeground( Color.black );
		vv.setBackground( Color.white );
		vv.getRenderContext().setEdgeFontTransformer( new Transformer<MyEdge,Font>() {
			@Override
			public Font transform( MyEdge arg0 ) {
				return new Font( "David", Font.BOLD, 14 );
			}
		} );
		vv.getRenderContext().setVertexShapeTransformer( new Transformer<MyVertex,Shape>() {
			@Override
			public Shape transform( MyVertex v ) {
				Node n = p.getHostedNode( v.n.getUniqueId() );
				if( n == null )
					n = v.n;
				Font font = vv.getRenderContext().getVertexFontTransformer().transform( v );
				// boolean
				// isselected=vv.getRenderContext().getPickedVertexState().isPicked(v);
				// //Dimension
				// d=vv.getRenderContext().getVertexLabelRenderer().getVertexLabelRendererComponent(vv,
				// v.toString(),font , isselected, v).getSize();
				FontMetrics fm = vv.getFontMetrics( font );
				Dimension d = fm.getStringBounds( v.toString(), vv.getGraphics() ).getBounds().getSize();
				d.height = (int) ( d.height * (double) ( n.getLinks( v.correlatoridlayer ).size() / (double) 10 ) );
				return new Rectangle( -d.width / 2, -d.height / 2, d.width, d.height );// new
				// Rectangle(-d.width/2,-d.height/2,d.width,d.height);
			}
		} );
	}

	public void viewInFrame() {
		JFrame frame = new JFrame( "Simple Graph View" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.getContentPane().add( new JScrollPane( vv ) );
		frame.setSize( layout.getSize() );
		frame.setVisible( true );
	}

	// public static void main(String[] args) {
	// new Networkviewer().test();
	// }
	class MyVertex {
		Node n;
		int correlatoridlayer;

		public MyVertex( Node n , int correlatorid ) {
			this.n = n;
			this.correlatoridlayer = correlatorid;
		}

		@Override
		public String toString() {
			return getNode2String( n, correlatoridlayer );
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode( n.getUniqueId() );
		}

		@Override
		public boolean equals( Object obj ) {
			if( obj instanceof MyVertex )
				if( Arrays.equals( ( (MyVertex) obj ).n.getUniqueId(), n.getUniqueId() ) )
					return true;
			return false;
		}
	}

	class MyEdge {
		String label;
		Node n, n2;

		public MyEdge( String label ) {
			this.label = label;
		}

		public MyEdge( int dist , Node n , Node n2 ) {
			label = dist + "";
			this.n = n;
			this.n2 = n2;
		}

		public boolean connects( Node node ) {
			return node.equals( n ) || node.equals( n2 );
		}

		@Override
		public boolean equals( Object obj ) {
			if( obj instanceof MyEdge ) {
				MyEdge m = (MyEdge) obj;
				return m.n.equals( n ) && m.n2.equals( n2 );
			}
			return false;
		}

		@Override
		public String toString() {
			return label;
		}
	}
}
