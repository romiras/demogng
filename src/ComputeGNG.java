// ========================================================================== ;
//                                                                            ;
// Copyright 1996-1998 Hartmut S. Loos, Instit. f. Neuroinformatik, Bochum    ;
// Copyright 2012-2013 Bernd Fritzke                                          ;
//                                                                            ;
// This program is free software; you can redistribute it and/or modify       ;
// it under the terms of the GNU General Public License as published by       ;
// the Free Software Foundation; either version 1, or (at your option)        ;
// any later version.                                                         ;
//                                                                            ;
// This program is distributed in the hope that it will be useful,            ;
// but WITHOUT ANY WARRANTY; without even the implied warranty of             ;
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              ;
// GNU General Public License for more details.                               ;
//                                                                            ;
// You should have received a copy of the GNU General Public License          ;
// along with this program; if not, write to the Free Software                ;
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.                  ;
//                                                                            ;
// ========================================================================== ;

//import java.awt.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 * A class which implements the network algorithms.
 * It implements many functions/algorithms.
 * The most important method is 'learn' which implements all adaptation methods
 * 
 */
@SuppressWarnings("serial")
class ComputeGNG extends JPanel implements 
Runnable, 
MouseMotionListener, 
MouseListener, 
ComponentListener,
ChangeListener
{
	Voronoi voro;
	int delay = 10;
	void log(String prefix, String txt) {
		System.out.println(timeStamp()+" C: "+prefix+txt);
	}
	void log(String txt) {
		log("####### ", txt);
	}
	void syslog(String txt) {
		log("####### ", txt);
	}
	public void wasResized() {
		Dimension d = getSize();
		panelWidth = d.width;
		panelHeight = d.height;
		log("Compute:wasResized()"+String.format("w: %d h: %d", d.width, d.height));
	}
	public void setSize(Dimension d) {
		log("COM: setsize");
	}
	final static private SimpleDateFormat format
	= new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");
	public synchronized String timeStamp() {
	    long lDateTime = new Date().getTime();
		String str;
		str = format.format(new java.util.Date())+"."+String.format("%03d",lDateTime%1000);
		return str;
	}
	/**
	 * The flag for debugging.
	 */
	protected final boolean DEBUG = false;
	/**
	 * The maximum number of elements to draw/calculate for the distributions.
	 */
	protected final int MAX_COMPLEX = 58;
	/**
	 * The maximum number of nodes.
	 */
	protected final int MAX_NODES = 10000;
	/**
	 * The maximum number of edges (3 * maximum number of nodes).
	 */
	protected final int MAX_EDGES = 6 * MAX_NODES;
	/**
	 * The maximum number of Voronoi lines (5 * maximum number of nodes).
	 */
	protected final int MAX_V_LINES = 6 * MAX_NODES;
	/**
	 * The maximum stepsize.
	 */
	protected final int MAX_STEPSIZE = 200;
	/**
	 * The size of the DiscreteMixture signal set.
	 */
	protected final int MIXTURE_SIZE = 500;
	/**
	 * The maximum number of discrete signals.
	 */
	protected final int MAX_DISCRETE_SIGNALS = 10000;
	/**
	 * The maximum x size of the grid array.
	 */
	protected final int MAX_GRID_X = 10000;
	/**
	 * The maximum y size of the grid array.
	 */
	protected final int MAX_GRID_Y = 100;
	/**
	 * The number of errors added for the mean square error.
	 */
	protected final int NUM_GLOBAL_GRAPH = 500;
	/**
	 * The factor for the ring-thickness (distribution).
	 */
	protected final float RING_FACTOR = 0.4f;	// Factor < 1 
	/**
	 * The version of the Growing Neural Gas Demo.
	 */
	protected final String DGNG_VERSION = "v2.1"; // Version
	/**
	 * The current maximum number of nodes.
	 */
	protected int maxNodes = 100;
	/**
	 * The current number of runs to insert a new node (GNG).
	 */
	protected int lambdaGNG = 600;
	/**
	 * The current number of input signals used for adaptation.
	 */
	protected int sigs = 0;
	public int getSigs() {
		return sigs;
	}

	/**
	 * The temporal backup of a run.
	 */
	protected int sigsTmp = 0;
	/**
	 * The x-position of the actual signal.
	 */
	protected int SignalX = 0;
	/**
	 * The y-position of the actual signal.
	 */
	protected int SignalY = 0;
	/**
	 * The initial width of the drawing area.
	 * This value can only be changed by resizing the appletviewer.
	 */
	protected int panelWidth = 550;
	/**
	 * The initial height of the drawing area.
	 * This value can only be changed by resizing the appletviewer.
	 */
	protected int panelHeight = 310;

	protected DemoGNG graph;
	/**
	 * The actual number of nodes.
	 */
	protected int nNodes = 0;
	/**
	 * The array of the actual used nodes.
	 */
	protected NodeGNG nodes[] = new NodeGNG[MAX_NODES];
	/**
	 * The sorted array of indices of nodes.
	 * The indices of the nodes are sorted by their distance from the actual
	 * signal. snodes[1] is the index of the nearest node.
	 */
	protected int snodes[] = new int[MAX_NODES + 1];
	/**
	 * The array of the nodes in the grid.
	 */
	protected GridNodeGNG grid[][] = new GridNodeGNG[MAX_GRID_X][MAX_GRID_Y];
	/**
	 * The array of the last computed signals (x-coordinate).
	 */
	protected float lastSignalsX[] = new float[MAX_STEPSIZE];
	/**
	 * The array of the last computed signals (y-coordinate).
	 */
	protected float lastSignalsY[] = new float[MAX_STEPSIZE];
	/**
	 * The array of the discrete signals (x-coordinate).
	 */
	protected float discreteSignalsX[] = new float[MAX_DISCRETE_SIGNALS];
	/**
	 * The array of the discrete signals (y-coordinate).
	 */
	protected float discreteSignalsY[] = new float[MAX_DISCRETE_SIGNALS];
	/**
	 * The array of the best distance (discrete signals).
	 */
	protected float discreteSignalsD1[] = new float[MAX_DISCRETE_SIGNALS];
	/**
	 * The array of the second best distance (discrete signals).
	 */
	protected float discreteSignalsD2[] = new float[MAX_DISCRETE_SIGNALS];
	/**
	 * The array of the second best distance (discrete signals).
	 */
	protected FPoint Cbest[] = new FPoint[MAX_NODES];

	/**
	 * The current number of discrete signals.
	 */
	protected int numDiscreteSignals = 500;
	/**
	 * The actual number of edges.
	 */
	protected int nEdges = 0;
	/**
	 * The array of the actual used edges.
	 */
	protected EdgeGNG edges[] = new EdgeGNG[MAX_EDGES];
	/**
	 * The actual number of Voronoi lines.
	 */
	protected int nlines = 0;
	/**
	 * The array of the actual used lines.
	 */
	protected LineGNG lines[] = new LineGNG[MAX_V_LINES];
	/**
	 * The array of boolean to distinguish between Voronoi and Delaunay lines.
	 */
	protected boolean vd[] = new boolean[MAX_V_LINES];

	Thread relaxer;
	GraphGNG errorGraph;

	/**
	 * The flag for playing the sound for a new inserted node.
	 */
	protected boolean insertedSoundB = false;
	/**
	 * The flag for a white background. Useful for making hardcopies
	 */
	protected boolean whiteB = false;
	/**
	 * The flag for random init. The nodes will be placed only in the specified
	 *  distribution or not.
	 */
	protected boolean rndInitB = false;
	/**
	 * The flag for entering the fine-tuning phase (GG).
	 */
	protected boolean fineTuningB = false;
	/**
	 * The flag for showing the signal.
	 *  This variable can be set by the user and shows the last input signals.
	 */
	protected boolean signalsB = false;
	/**
	 * stop the algo when max number of nodes is reached
	 */
	protected boolean autoStopB = true;
	/**
	 * stop the GG algo when max number of nodes is reached
	 */
	protected boolean autoStopGGB = false;
	/**
	 * stop the GNG algo when max number of nodes is reached
	 */
	protected boolean autoStopGNGB = false;
	/**
	 * display GG network in mapSpace
	 */
	protected boolean mapSpaceGGB = false;
	/**
	 * display SOM network in mapSpace
	 */
	protected boolean mapSpaceSOMB = false;
	/**
	 * The flag for displaying tau values
	 */
	protected boolean tauB = false;
	/**
	 * close GG to a torus
	 */
	protected boolean torusGGB = false;
	/**
	 * close SOM to a torus
	 */
	protected boolean torusSOMB = false;
	/**
	 * The flag for displaying usage
	 */
	protected boolean usageB = false;
	/**
	 * The flag for inserting new nodes in GNG.
	 *  This variable can be set by the user. If true no new nodes are
	 *  inserted.
	 */
	protected boolean noNewNodesGNGB = false;
	/**
	 * The flag for inserting new nodes in GG.
	 *  This variable can be set by the user. If true no new nodes are
	 *  inserted.
	 */
	protected boolean noNewNodesGGB = false;
	/**
	 * The flag for stopping the demo.
	 *  This variable can be set by the user. If true no calculation is done.
	 */
	private boolean stopB = false;
	/**
	 * The flag for the sound.
	 *  This variable can be set by the user. If false no sound is played.
	 */
	protected boolean soundB = false;
	/**
	 * The flag for the teach-mode.
	 *  This variable can be set by the user. If true a legend is displayed
	 *  which describes the new form and color of some nodes. Furthermore
	 *  all calculation is very slow.
	 */
	protected boolean teachB = false;
	/**
	 * The flag for variable movement (HCL).
	 *  This variable can be set by the user. 
	 */
	protected boolean variableB = false;
	/**
	 * The flag for displaying the edges.
	 *  This variable can be set by the user. 
	 */
	protected boolean edgesB = true;
	/**
	 * The flag for displaying the nodes.
	 *  This variable can be set by the user. 
	 */
	protected boolean nodesB = true;
	/**
	 * The flag for displaying the (motion) traces.
	 *  This variable can be set by the user. 
	 */
	protected boolean tracesB = false;
	/**
	 * The flag for displaying the error graph.
	 *  This variable can be set by the user. 
	 */
	protected boolean errorGraphB = false;
	/**
	 * The flag for the global error (more signals).
	 *  This variable can be set by the user. 
	 */
	protected boolean globalGraphB = true;
	/**
	 * The flag for displaying the Voronoi diagram.
	 *  This variable can be set by the user. 
	 */
	protected boolean voronoiB = false;
	/**
	 * The flag for displaying the Delaunay triangulation.
	 *  This variable can be set by the user. 
	 */
	protected boolean delaunayB = false;
	/**
	 * The flag for any moved nodes (to compute the Voronoi diagram/Delaunay
	 *  triangulation).
	 */
	protected boolean nodesMovedB = true;
	/**
	 * The flag for using utility (GNG-U).
	 */
	protected boolean GNG_U_B = false;
	/**
	 * The flag for changed number of nodes.
	 */
	protected boolean nNodesChangedB = true;
	/**
	 * The flag for only discrete signals
	 */
	protected boolean discreteSigB = true;
	/**
	 * The flag for LBG-U method
	 */
	protected boolean LBG_U_B = false;
	/**
	 * The flag for end of calculation (LBG)
	 */
	protected boolean readyLBG_B = false;
	/**
	 * The selected distribution.
	 *  This variable can be set by the user. 
	 */
	protected PD pd=PD.Rectangle;
	/**
	 * The selected algorithm.
	 */
	Algo algo = Algo.NG;
	/**
	 * The current maximum number to delete an old edge (GNG,NGwCHL).
	 *  This variable can be set by the user. 
	 */
	protected int MAX_EDGE_AGE = 88;
	/**
	 * The current number of calculations done in one step. 
	 *  This variable can be set by the user. After <TT> stepSize </TT>
	 *  calculations the result is displayed.
	 */
	protected int stepSize = 50;
	/**
	 * This variable determines how long the compute thread sleeps. In this time
	 *  the user can interact with the program. Slow machines and/or slow
	 *  WWW-browsers need more time than fast machines and/or browsers.
	 *  This variable can be set by the user.
	 */
	protected int tSleep = 10; //should be taken from the speed choice
	/**
	 * The actual x size of the grid array.
	 */
	protected int gridWidth = 0;
	/**
	 * The actual y size of the grid array.
	 */
	protected int gridHeight = 0;
	/**
	 * The direction factor for the x axis (-1 or 1) used for the
	 * 'Moving Rectangle' distribution
	 */
	protected int bounceX = -1;
	/**
	 * The direction factor for the y axis (-1 or 1) used for the
	 * 'Moving Rectangle' distribution
	 */
	protected int bounceY = -1;
	/**
	 * The x coordinate for the 'Jumping Rectangle' and 'R.Mouse' distribution
	 */
	protected int jumpX = 250;
	/**
	 * The y coordinate for the 'Jumping Rectangle' and 'R.Mouse' distribution
	 */
	protected int jumpY = 250;
	/**
	 * Stores the old x value of the remainder in order to detect the bounce
	 * (used for the 'Moving Rectangle' distribution)
	 */
	protected double bounceX_old = 1;
	/**
	 * Stores the old y value of the remainder in order to detect the bounce
	 * (used for the 'Moving Rectangle' distribution)
	 */
	protected double bounceY_old = 1;

	/**
	 * The value epsilon for the HCL algorithm.
	 *  This variable can be set by the user.
	 */
	protected float epsilon = 0.1f;
	/**
	 * The value epsilon for the GNG algorithm (winner).
	 *  This variable can be set by the user.
	 */
	protected float epsilonGNG = 0.1f;
	/**
	 * The value epsilon for the GNG algorithm (second).
	 *  This variable can be set by the user.
	 */
	protected float epsilonGNG2 = 0.001f;
	/**
	 * The value alpha for the GNG algorithm.
	 *  This variable can be set by the user.
	 */
	protected float alphaGNG = 0.5f;
	/**
	 * The value beta for the GNG algorithm.
	 *  This variable can be set by the user.
	 */
	protected float betaGNG = 0.0005f;
	/**
	 * maximum width of grid
	 *  
	 */
	protected int maxYGG = 0;
	/**
	 * The utility factor for the GNG-U algorithm.
	 *  This variable can be set by the user.
	 */
	protected float utilityGNG = 3.0f;
	/**
	 * The decay factor for utility
	 */
	protected float decayFactorUtility = 1.0f - betaGNG;
	/**
	 * The factor to forget old values.
	 */
	protected float decayFactor = 1.0f - betaGNG;
	/**
	 * The value lambda initial for the NG,NGwCHL,GG algorithms.
	 *  This variable can be set by the user.
	 */
	protected float l_i = 30.0f;
	/**
	 * The value lambda final for the NG,NGwCHL,GG algorithms.
	 *  This variable can be set by the user.
	 */
	protected float l_f = 0.01f;
	/**
	 * The value epsilon(t) for the NG,NGwCHL algorithms.
	 */
	protected float e_t = 0.0f;
	/**
	 * The value epsilon initial for the NG,NGwCHL,GG algorithms.
	 *  This variable can be set by the user.
	 */
	protected float e_i = 0.3f;
	/**
	 * The value epsilon final for the NG,NGwCHL,GG algorithms.
	 *  This variable can be set by the user.
	 */
	protected float e_f = 0.05f;
	/**
	 * The value t_max for the NG,NGwCHL,SOM algorithms.
	 *  This variable can be set by the user.
	 */
	protected float t_max = 40000.0f;
	/**
	 * The value delete edge initial for the NGwCHL algorithm.
	 *  This variable can be set by the user.
	 */
	protected float delEdge_i = 20.0f;
	/**
	 * The value delete edge final for the NGwCHL algorithm.
	 *  This variable can be set by the user.
	 */
	protected float delEdge_f = 200.0f;
	/**
	 * The value sigma for the GG algorithm.
	 *  This variable can be set by the user.
	 */
	protected float sigma = 0.9f;
	/**
	 * The value sigma_i for the SOM algorithm.
	 *  This variable can be set by the user.
	 */
	protected float sigma_i = 5.0f;
	/**
	 * The value sigma_f for the SOM algorithm.
	 *  This variable can be set by the user.
	 */
	protected float sigma_f = 5.0f;
	/**
	 * This value is displayed in the error graph.
	 */
	protected float valueGraph = 0.0f;
	/**
	 * This value contains the best error value for LBG-U up to now.
	 */
	protected float errorBestLBG_U = Float.MAX_VALUE;
	/**
	 * The string shown in the fine-tuning phase of the method GG.
	 */
	protected String fineTuningS = "";

	/**
	 * The constructor.
	 * 
	 * @param graph	The drawing area
	 */
	ComputeGNG(DemoGNG graph) {
		addMouseMotionListener(this);
		addMouseListener(this);
		addComponentListener(this);
		this.graph = graph;
		this.voro = new Voronoi(this);
	}

	/**
	 * Add a node. The new node will be randomly placed within the 
	 *  given dimension or according to the current distribution.
	 * 
	 * @param d          The dimension of the initial drawing area
	 * @return           The index of the new node
	 */
	protected int addNode(Dimension d) {
		if ( (nNodes == MAX_NODES) || (nNodes >= maxNodes) )
			return -1;

		NodeGNG n = new NodeGNG();

		if (rndInitB) {
			// init from rectangle 0.8x0.8+(0.1,0.1)
			n.x = (float) (10 + (d.width-20) * Math.random());
			n.y = (float) (10 + (d.height-20) * Math.random());
		} else {
			// init from current distribution
			getSignal(pd);
			n.x = SignalX;
			n.y = SignalY;
		}
		//System.out.printf("addNode() and width is %d and rndInit is %s, x=%f, y=%f\n",d.width, rndInitB,n.x,n.y);

		n.nNeighbor = 0;
		if (algo.isDiscrete())
			n.hasMoved = true;
		nodes[nNodes] = n;
		nNodesChangedB = true;
		return nNodes++;
	}

	/**
	 * Add a node. The new node will be placed at the
	 *  given coordinates.
	 * 
	 * @param x          The x-coordinate of the new node
	 * @param y          The y-coordinate of the new node
	 * @return           The index of the new node
	 */
	protected int addNode(int x, int y) {
		if ( (nNodes == MAX_NODES) || (nNodes >= maxNodes) )
			return -1;
		NodeGNG n = new NodeGNG();
		n.x = x;
		n.y = y;
		if (algo.isDiscrete()) // LBG
			n.hasMoved = true;
		nodes[nNodes] = n;
		nNodesChangedB = true;
		return nNodes++;
	}

	/**
	 * Add a node. The new node will be placed between the
	 *  given nodes which must be connected. The existing edge is splitted.
	 *  The new node gets the average of the interesting values of
	 *  the two given nodes.
	 * 
	 * @param n1         The index of a node
	 * @param n2         The index of a node
	 * @return           The index of the new node
	 */
	protected int insertNode(int n1, int n2) {
		if ( (nNodes == MAX_NODES) || (nNodes >= maxNodes) )
			return -1;
		if ( (n1 < 0) || (n2 < 0) )
			return -1;
		NodeGNG n = new NodeGNG();
		float dx = (nodes[n1].x - nodes[n2].x) / 2.0f;
		float dy = (nodes[n1].y - nodes[n2].y) / 2.0f;
		// reduce errors of neighbor nodes of the new unit
		nodes[n1].error *= (1.0f - alphaGNG);
		nodes[n2].error *= (1.0f - alphaGNG);
		
		// interpolate error from neighbors
		n.error = (nodes[n1].error + nodes[n2].error)/2.0f;
		// interpolate utility from neighbors
		n.utility = (nodes[n1].utility + nodes[n2].utility)/2.0f;
		// interpolate coordinates
		n.x = nodes[n1].x - dx;
		n.y = nodes[n1].y - dy;
		n.isMostRecentlyInserted = true;
		nodes[nNodes] = n;
		deleteEdge(n1, n2);
		addEdge(n1, nNodes); //n1<->new
		addEdge(n2, nNodes); //n2<->new
		nNodesChangedB = true;

		return nNodes++;
	}

	/**
	 * Add a node into the grid. The new node will be randomly placed within the 
	 *  given dimension.
	 * 
	 * @param x          The x coordinate of the grid array
	 * @param y          The y coordinate of the grid array
	 * @param d          The dimension of the initial drawing area
	 * @return           The index of the new node
	 */
	protected int addGridNode(int x, int y, Dimension d) {
		if ( (x > MAX_GRID_X) || (y > MAX_GRID_Y) )
			return -1;

		int n = addNode(d);
		nodes[n].x_grid = x;
		nodes[n].y_grid = y;

		grid[x][y] = new GridNodeGNG(n, nodes[n]);

		return n;
	}

	/**
	 * Initialize the grid. The new nodes will be randomly placed within the 
	 *  given dimension.
	 * 
	 * @param width          The width of the grid array
	 * @param height          The height of the grid array
	 * @param d          The dimension of the initial drawing area
	 */
	protected void initGrid(int width, int height, Dimension d) {
		if ( (width > MAX_GRID_X) || (height > MAX_GRID_Y) ){
			throw new IllegalStateException ( ); // hack bf
			//return;
		}
		int i, j;

		if (algo == Algo.SOM)
			maxNodes = width * height;

		//
		// add nodes
		//
		for (i = 0; i < width; i++)
			for (j = 0; j < height; j++)
			{
				addGridNode(i, j, d);
//				if (i+j<=2) {
//					NodeGNG n = grid[i][j].node;
//					System.out.printf("initGrid(): new node: %d %d x_grid= %d y_grid = %d  x=%5.3f y=%5.3f\n", i,j,n.x_grid,n.y_grid,n.x,n.y);
//				}
			}

		gridWidth = width;
		gridHeight = height;

		//
		// add edges
		//
		for (i = 0; i < gridWidth; i++) {
			for (j = 0; j < gridHeight; j++) {
				if ( i < (gridWidth - 1) )
					addEdge(grid[i][j].index, grid[i+1][j].index);
				if ( j < (gridHeight - 1) )
					addEdge(grid[i][j].index, grid[i][j+1].index);
			}
		}
	}

	/**
	 * Prepare to insert a row or column into the grid.
	 * 
	 * @return		The index of the last inserted node or -1
	 */
	protected boolean enlargeGrid() {
		float tau = 0;
		int x = 0;
		int y = 0;
		int n = -1;
		float d1 = 0, d2 = 0, d3 = 0, d4 = 0, max = 0;

		for (int j = 0; j < gridHeight; j++) {
			for (int i = 0; i < gridWidth; i++) {
				// Find maximum resource value tau
				if ( grid[i][j].node.tau > tau ) {
					tau = grid[i][j].node.tau;
					x = i;
					y = j;
				}
				//System.out.print(String.format("%06.03f", grid[i][j].node.tau));
				//System.out.print(", ");
				
				grid[i][j].node.tau = 0; // clear tau value
				grid[i][j].node.isMostRecentlyInserted = false;
			}
			//System.out.println();
		}
		//log(String.format("maximum tau value: %f  x: %d  y: %d  width: %d  height: %d", tau,x,y,gridWidth, gridHeight));
		// assertion: grid[x][y] is the node with maximum tau value

		// Identify the neighbor of (x,y) with the most different reference vector

		// left neighbor
		if (x > 0)
			d1 = (grid[x-1][y].node.x - grid[x][y].node.x) *
			     (grid[x-1][y].node.x - grid[x][y].node.x) +
			     (grid[x-1][y].node.y - grid[x][y].node.y) *
			     (grid[x-1][y].node.y - grid[x][y].node.y);

		// upper neighbor
		if (y > 0)
			d2 = (grid[x][y-1].node.x - grid[x][y].node.x) *
			     (grid[x][y-1].node.x - grid[x][y].node.x) +
			     (grid[x][y-1].node.y - grid[x][y].node.y) *
			     (grid[x][y-1].node.y - grid[x][y].node.y);

		// right neighbor
		if (x < gridWidth - 1)
			d3 = (grid[x+1][y].node.x - grid[x][y].node.x) *
			     (grid[x+1][y].node.x - grid[x][y].node.x) +
			     (grid[x+1][y].node.y - grid[x][y].node.y) *
			     (grid[x+1][y].node.y - grid[x][y].node.y);

		// lower neighbor
		if (y < gridHeight - 1)
			d4 = (grid[x][y+1].node.x - grid[x][y].node.x) *
			     (grid[x][y+1].node.x - grid[x][y].node.x) +
			     (grid[x][y+1].node.y - grid[x][y].node.y) *
			     (grid[x][y+1].node.y - grid[x][y].node.y);
		if ((maxYGG > 0) && (gridHeight >= maxYGG )) { //
			d4=0;
			d2=0;
		}

		max = Math.max(d1, Math.max(d2, Math.max(d3, d4)));

		if (max == d1)        // left
			n = insertColumn(x - 1);
		else if (max == d2)   // upper
			n = insertRow(y - 1);
		else if (max == d3)   // right
			n = insertColumn(x);
		else                  // lower
			n = insertRow(y);

		if (n == -1)
			return false;
		else
			return true;
	}

	/**
	 * Add a row into the grid. The new row will be placed after index y.
	 * 
	 * @param y          The row index
	 * @return           The index of the last inserted node or -1
	 */
	protected int insertRow(int y) {
		if ( (gridHeight == MAX_GRID_Y) || (nNodes + gridWidth > maxNodes) )
			return -1;

		int n = -1;
		int i, j;

		// Insert nodes for the new row
		for (i = 0; i < gridWidth; i++) {
			n = addNode(0,0);
			nodes[n].x_grid = i;
			nodes[n].y_grid = gridHeight;
			grid[i][gridHeight] = new GridNodeGNG(n, nodes[n]);

			// Add Edges
			if (i != 0)
				addEdge(grid[i][gridHeight].index, grid[i-1][gridHeight].index);
			addEdge(grid[i][gridHeight].index, grid[i][gridHeight-1].index);
		}

		// Now change the parameters (position)
		for (j = gridHeight; j > y+1; j--) {
			for (i = 0; i < gridWidth; i++) {
				grid[i][j].node.x = grid[i][j-1].node.x;
				grid[i][j].node.y = grid[i][j-1].node.y;
			}
		}

		for (i = 0; i < gridWidth; i++) {
			grid[i][y+1].node.x = (grid[i][y].node.x + grid[i][y+2].node.x)*0.5f;
			grid[i][y+1].node.y = (grid[i][y].node.y + grid[i][y+2].node.y)*0.5f;
			grid[i][y+1].node.tau = (grid[i][y].node.tau + grid[i][y+2].node.tau)*0.33f;
			grid[i][y].node.tau *= 0.66f;
			grid[i][y+2].node.tau *= 0.66f;

			grid[i][y+1].node.isMostRecentlyInserted = true;
		}

		// Make the new row official
		gridHeight++;

		return n;
	}

	/**
	 * Add a column into the grid. The new column will be placed after index x.
	 * 
	 * @param x          The column index
	 * @return           The index of the last inserted node or -1
	 */
	protected int insertColumn(int x) {
		if ( (gridWidth == MAX_GRID_X) || (nNodes + gridHeight > maxNodes) )
			// max size or dimension reached: no further insert!
			return -1;

		int n = -1;
		int i, j;

		// Insert nodes for the new Column (x-index: gridwidth)
		for (j = 0; j < gridHeight; j++) {
			n = addNode(0,0);
			nodes[n].x_grid = gridWidth;
			nodes[n].y_grid = j;
			grid[gridWidth][j] = new GridNodeGNG(n, nodes[n]);

			// Add Edges
			if (j != 0)
				addEdge(grid[gridWidth][j].index, grid[gridWidth][j-1].index);
			addEdge(grid[gridWidth][j].index, grid[gridWidth-1][j].index);
		}

		// Now change the parameters (position)
		// i.e. make space for new column x+1 by moving columns x+1 and following to the right
		//
		for (i = gridWidth; i > x+1; i--) {
			for (j = 0; j < gridHeight; j++) {
				grid[i][j].node.x = grid[i-1][j].node.x;
				grid[i][j].node.y = grid[i-1][j].node.y;
			}
		}
		
		// interpolate positions for column x+1
		// relies on the fact that column x+1 still contains the old values which are also copied to x+2
		for (j = 0; j < gridHeight; j++) {
			//System.out.printf("interp: x=%d,  j=%d\n",x,j);
			grid[x+1][j].node.x = (grid[x][j].node.x + grid[x+2][j].node.x)*0.5f;
			grid[x+1][j].node.y = (grid[x][j].node.y + grid[x+2][j].node.y)*0.5f;
			grid[x+1][j].node.tau =    (grid[x][j].node.tau     +grid[x+2][j].node.tau)*1/3;
			grid[x][j].node.tau *= 0.66;
			grid[x+2][j].node.tau *= 0.66;
			grid[x+1][j].node.isMostRecentlyInserted = true;
		}

		// Make the new row official
		gridWidth++;

		return n;
	}

	/**
	 * Delete the given node.
	 * 
	 * @param n          The index of a node
	 */
	protected void deleteNode(int n) {
		NodeGNG node = nodes[n];
		int num = node.numNeighbors();
		int i;

		for (i = 0; i < num; i++)
			deleteEdge(n, node.neighbor(0));

		nNodesChangedB = true;
		nNodes--;
		nodes[n] = nodes[nNodes];
		nodes[nNodes] = null;

		// Now rename all occurances of nodes[nnodes] to nodes[n]
		for (i = 0 ; i < nNodes ; i++)
			nodes[i].replaceNeighbor(nNodes, n);
		for (i = 0 ; i < nEdges ; i++)
			edges[i].replace(nNodes, n);

		return;
	}

	/**
	 * Connect two nodes or reset the age of their edge. 
	 * 
	 * @param from          The index of the first node
	 * @param to            The index of the second node
	 */
	protected void addEdge(int from, int to) {
		if (nNodes < 2)
			return;

		if (nodes[from].isNeighbor(to)) {
			// Find edge(from,to) and reset age
			int i = findEdge(from, to);

			if (i != -1)
				edges[i].age = 0;
			return;
		}

		if (nEdges == MAX_EDGES)
			return;

		if ( (nodes[from].moreNeighbors()) && (nodes[to].moreNeighbors()) ) {
			nodes[to].addNeighbor(from);
			nodes[from].addNeighbor(to);
		} else
			return;

		// Add new edge
		EdgeGNG e = new EdgeGNG();
		e.from = from;
		e.to = to;
		edges[nEdges] = e;
		nEdges++;
	}

	/**
	 * Disconnect two nodes.
	 * 
	 * @param from          The index of the first node
	 * @param to            The index of the second node
	 */
	protected void deleteEdge(int from, int to) {
		int i = findEdge(from, to);
		if (i != -1) {
			nodes[edges[i].from].deleteNeighbor(edges[i].to);
			nodes[edges[i].to].deleteNeighbor(edges[i].from);
			nEdges--;
			edges[i] = edges[nEdges];
			edges[nEdges] = null;
		}
		return;
	}

	/**
	 * Delete an edge.
	 * 
	 * @param from          The index of the edge
	 */
	protected void deleteEdge(int edgeNr) {
		nodes[edges[edgeNr].from].deleteNeighbor(edges[edgeNr].to);
		nodes[edges[edgeNr].to].deleteNeighbor(edges[edgeNr].from);
		nEdges--;
		edges[edgeNr] = edges[nEdges];
		edges[nEdges] = null;
	}

	/**
	 * Find an edge. Find the edge between the two given nodes.
	 * 
	 * @param from          The index of the first node
	 * @param to            The index of the second node
	 * @return              The index of the found edge or -1
	 */
	protected int findEdge(int from, int to) {

		for (int i = 0; i < nEdges; i++)
			if (( (edges[i].from == from) && (edges[i].to == to) ) || 
					( (edges[i].from == to) && (edges[i].to == from) ) )
				return i;
		return -1;
	}

	/**
	 * All edges starting from the given node are aged.
	 *  Too old edges are deleted.
	 * 
	 * @param node          The index of a node
	 * @see ComputeGNG#MAX_EDGE_AGE
	 */
	protected void ageEdgesOfNode(int node) {
        // TODO: this is inefficient for large number of edges, perhaps keep local list of edges per node
		for (int i = nEdges - 1; i > -1; i--) {
			if ( (edges[i].from == node) || (edges[i].to == node) )
				edges[i].age++;
			if (edges[i].age > MAX_EDGE_AGE)
				deleteEdge(i);
		}
	}

	/**
	 * Find neighbor with the highest error.
	 * 
	 * @param master          The index of a node
	 * @return                The index of a node
	 */
	protected int maximumErrorNeighbor(int master) {
		float ws = Float.MIN_VALUE;
		int wn = -1;
		int n = -1;
		int num = nodes[master].numNeighbors();
		for (int i = 0; i < num; i++) {
			n = nodes[master].neighbor(i);
			if (ws < nodes[n].error) {
				ws = nodes[n].error;
				wn = n;
			}
		}

		return wn;
	}

	protected void rescaleDiscreteSignals(double x, double y) {
		for (int i = 0; i < MAX_DISCRETE_SIGNALS; i++) {
			discreteSignalsX[i] *= x;
			discreteSignalsY[i] *= y;
		}
	}
	/**
	 * Generate discrete signals for the given distribution.
	 *  The result goes into the global arrays <TT> discreteSignalsX </TT>
	 *  and <TT> discreteSignalsY </TT>.
	 * 
	 * @param distrib          The specified distribution
	 * @param w          		 The width of the drawing area
	 * @param h          		 The height of the drawing area
	 */
	protected void initDiscreteSignals(PD pd) {
		Dimension d = getSize();
		int w = d.width;
		int h = d.height;
		int kx = 1;
		int ky = 1;
		int l = 0;
		float dSX[] = discreteSignalsX;
		float dSY[] = discreteSignalsY;
        log("init discrete");
		if (pd != PD.DiscreteMixture) {
	        log("init discrete from cont");
			//
			// generate a finite signal set from continuous distribution
			//
			for (int i = 0; i < MAX_DISCRETE_SIGNALS; i++) {
				getSignal(pd);
				discreteSignalsX[i] = SignalX;
				discreteSignalsY[i] = SignalY;
			}
		} else {
	        log("init discrete from mixture");
			//
			// DiscreteMixture Distribution (500 Points)
			//
			
			// scale according to app dimension
			if (w > h) {
				kx = w/4;
				l = h;
			} else {
				ky = h/4;
				l = w;
			}
			
			int discreteSize = 500; // size of discrete mixture distribution
			dSX[0]=(float)(kx+l*0.13814); dSY[0]=(float)(ky+l*(1.0-0.29675));
			dSX[1]=(float)(kx+l*0.19548); dSY[1]=(float)(ky+l*(1.0-0.09674));
			dSX[2]=(float)(kx+l*0.73576); dSY[2]=(float)(ky+l*(1.0-0.86994));
			dSX[3]=(float)(kx+l*0.73065); dSY[3]=(float)(ky+l*(1.0-0.19024));
			dSX[4]=(float)(kx+l*0.83479); dSY[4]=(float)(ky+l*(1.0-0.34258));
			dSX[5]=(float)(kx+l*0.13184); dSY[5]=(float)(ky+l*(1.0-0.56509));
			dSX[6]=(float)(kx+l*0.15959); dSY[6]=(float)(ky+l*(1.0-0.59065));
			dSX[7]=(float)(kx+l*0.21696); dSY[7]=(float)(ky+l*(1.0-0.1402));
			dSX[8]=(float)(kx+l*0.61592); dSY[8]=(float)(ky+l*(1.0-0.16657));
			dSX[9]=(float)(kx+l*0.10513); dSY[9]=(float)(ky+l*(1.0-0.21708));
			dSX[10]=(float)(kx+l*0.1864); dSY[10]=(float)(ky+l*(1.0-0.1454));
			dSX[11]=(float)(kx+l*0.36696); dSY[11]=(float)(ky+l*(1.0-0.74924));
			dSX[12]=(float)(kx+l*0.18345); dSY[12]=(float)(ky+l*(1.0-0.80946));
			dSX[13]=(float)(kx+l*0.8509); dSY[13]=(float)(ky+l*(1.0-0.38268));
			dSX[14]=(float)(kx+l*0.19476); dSY[14]=(float)(ky+l*(1.0-0.74262));
			dSX[15]=(float)(kx+l*0.49164); dSY[15]=(float)(ky+l*(1.0-0.65776));
			dSX[16]=(float)(kx+l*0.86552); dSY[16]=(float)(ky+l*(1.0-0.38373));
			dSX[17]=(float)(kx+l*0.73176); dSY[17]=(float)(ky+l*(1.0-0.84414));
			dSX[18]=(float)(kx+l*0.71978); dSY[18]=(float)(ky+l*(1.0-0.86979));
			dSX[19]=(float)(kx+l*0.83034); dSY[19]=(float)(ky+l*(1.0-0.36613));
			dSX[20]=(float)(kx+l*0.39886); dSY[20]=(float)(ky+l*(1.0-0.71479));
			dSX[21]=(float)(kx+l*0.09955); dSY[21]=(float)(ky+l*(1.0-0.20342));
			dSX[22]=(float)(kx+l*0.07091); dSY[22]=(float)(ky+l*(1.0-0.17197));
			dSX[23]=(float)(kx+l*0.21896); dSY[23]=(float)(ky+l*(1.0-0.10398));
			dSX[24]=(float)(kx+l*0.72465); dSY[24]=(float)(ky+l*(1.0-0.13984));
			dSX[25]=(float)(kx+l*0.71034); dSY[25]=(float)(ky+l*(1.0-0.87981));
			dSX[26]=(float)(kx+l*0.83547); dSY[26]=(float)(ky+l*(1.0-0.36065));
			dSX[27]=(float)(kx+l*0.13907); dSY[27]=(float)(ky+l*(1.0-0.56451));
			dSX[28]=(float)(kx+l*0.62124); dSY[28]=(float)(ky+l*(1.0-0.20175));
			dSX[29]=(float)(kx+l*0.65543); dSY[29]=(float)(ky+l*(1.0-0.17331));
			dSX[30]=(float)(kx+l*0.72349); dSY[30]=(float)(ky+l*(1.0-0.14375));
			dSX[31]=(float)(kx+l*0.82495); dSY[31]=(float)(ky+l*(1.0-0.40116));
			dSX[32]=(float)(kx+l*0.76586); dSY[32]=(float)(ky+l*(1.0-0.82376));
			dSX[33]=(float)(kx+l*0.24648); dSY[33]=(float)(ky+l*(1.0-0.11987));
			dSX[34]=(float)(kx+l*0.14817); dSY[34]=(float)(ky+l*(1.0-0.59985));
			dSX[35]=(float)(kx+l*0.82663); dSY[35]=(float)(ky+l*(1.0-0.38964));
			dSX[36]=(float)(kx+l*0.37131); dSY[36]=(float)(ky+l*(1.0-0.72726));
			dSX[37]=(float)(kx+l*0.12176); dSY[37]=(float)(ky+l*(1.0-0.60139));
			dSX[38]=(float)(kx+l*0.73587); dSY[38]=(float)(ky+l*(1.0-0.86952));
			dSX[39]=(float)(kx+l*0.59645); dSY[39]=(float)(ky+l*(1.0-0.21302));
			dSX[40]=(float)(kx+l*0.39489); dSY[40]=(float)(ky+l*(1.0-0.63452));
			dSX[41]=(float)(kx+l*0.234); dSY[41]=(float)(ky+l*(1.0-0.10385));
			dSX[42]=(float)(kx+l*0.51314); dSY[42]=(float)(ky+l*(1.0-0.67151));
			dSX[43]=(float)(kx+l*0.13499); dSY[43]=(float)(ky+l*(1.0-0.56896));
			dSX[44]=(float)(kx+l*0.10815); dSY[44]=(float)(ky+l*(1.0-0.62515));
			dSX[45]=(float)(kx+l*0.35487); dSY[45]=(float)(ky+l*(1.0-0.65635));
			dSX[46]=(float)(kx+l*0.13939); dSY[46]=(float)(ky+l*(1.0-0.24579));
			dSX[47]=(float)(kx+l*0.22087); dSY[47]=(float)(ky+l*(1.0-0.20651));
			dSX[48]=(float)(kx+l*0.12274); dSY[48]=(float)(ky+l*(1.0-0.61131));
			dSX[49]=(float)(kx+l*0.47888); dSY[49]=(float)(ky+l*(1.0-0.65166));
			dSX[50]=(float)(kx+l*0.18836); dSY[50]=(float)(ky+l*(1.0-0.6895));
			dSX[51]=(float)(kx+l*0.2511); dSY[51]=(float)(ky+l*(1.0-0.12476));
			dSX[52]=(float)(kx+l*0.84242); dSY[52]=(float)(ky+l*(1.0-0.3685));
			dSX[53]=(float)(kx+l*0.70824); dSY[53]=(float)(ky+l*(1.0-0.18571));
			dSX[54]=(float)(kx+l*0.2548); dSY[54]=(float)(ky+l*(1.0-0.77552));
			dSX[55]=(float)(kx+l*0.3659); dSY[55]=(float)(ky+l*(1.0-0.64852));
			dSX[56]=(float)(kx+l*0.78094); dSY[56]=(float)(ky+l*(1.0-0.37826));
			dSX[57]=(float)(kx+l*0.34205); dSY[57]=(float)(ky+l*(1.0-0.7295));
			dSX[58]=(float)(kx+l*0.83349); dSY[58]=(float)(ky+l*(1.0-0.37511));
			dSX[59]=(float)(kx+l*0.35477); dSY[59]=(float)(ky+l*(1.0-0.68483));
			dSX[60]=(float)(kx+l*0.13761); dSY[60]=(float)(ky+l*(1.0-0.17267));
			dSX[61]=(float)(kx+l*0.46041); dSY[61]=(float)(ky+l*(1.0-0.72594));
			dSX[62]=(float)(kx+l*0.12945); dSY[62]=(float)(ky+l*(1.0-0.58863));
			dSX[63]=(float)(kx+l*0.27379); dSY[63]=(float)(ky+l*(1.0-0.14071));
			dSX[64]=(float)(kx+l*0.4097); dSY[64]=(float)(ky+l*(1.0-0.77705));
			dSX[65]=(float)(kx+l*0.7175); dSY[65]=(float)(ky+l*(1.0-0.87696));
			dSX[66]=(float)(kx+l*0.43969); dSY[66]=(float)(ky+l*(1.0-0.66972));
			dSX[67]=(float)(kx+l*0.48588); dSY[67]=(float)(ky+l*(1.0-0.63899));
			dSX[68]=(float)(kx+l*0.69263); dSY[68]=(float)(ky+l*(1.0-0.20386));
			dSX[69]=(float)(kx+l*0.7374); dSY[69]=(float)(ky+l*(1.0-0.8667));
			dSX[70]=(float)(kx+l*0.67306); dSY[70]=(float)(ky+l*(1.0-0.18347));
			dSX[71]=(float)(kx+l*0.21203); dSY[71]=(float)(ky+l*(1.0-0.12508));
			dSX[72]=(float)(kx+l*0.48821); dSY[72]=(float)(ky+l*(1.0-0.67574));
			dSX[73]=(float)(kx+l*0.45742); dSY[73]=(float)(ky+l*(1.0-0.67679));
			dSX[74]=(float)(kx+l*0.67982); dSY[74]=(float)(ky+l*(1.0-0.1421));
			dSX[75]=(float)(kx+l*0.13429); dSY[75]=(float)(ky+l*(1.0-0.56728));
			dSX[76]=(float)(kx+l*0.2402); dSY[76]=(float)(ky+l*(1.0-0.76521));
			dSX[77]=(float)(kx+l*0.15482); dSY[77]=(float)(ky+l*(1.0-0.178));
			dSX[78]=(float)(kx+l*0.71594); dSY[78]=(float)(ky+l*(1.0-0.15844));
			dSX[79]=(float)(kx+l*0.10534); dSY[79]=(float)(ky+l*(1.0-0.59961));
			dSX[80]=(float)(kx+l*0.44167); dSY[80]=(float)(ky+l*(1.0-0.69823));
			dSX[81]=(float)(kx+l*0.46529); dSY[81]=(float)(ky+l*(1.0-0.70682));
			dSX[82]=(float)(kx+l*0.13842); dSY[82]=(float)(ky+l*(1.0-0.56618));
			dSX[83]=(float)(kx+l*0.09876); dSY[83]=(float)(ky+l*(1.0-0.5795));
			dSX[84]=(float)(kx+l*0.12101); dSY[84]=(float)(ky+l*(1.0-0.57408));
			dSX[85]=(float)(kx+l*0.44963); dSY[85]=(float)(ky+l*(1.0-0.74847));
			dSX[86]=(float)(kx+l*0.12532); dSY[86]=(float)(ky+l*(1.0-0.56478));
			dSX[87]=(float)(kx+l*0.18264); dSY[87]=(float)(ky+l*(1.0-0.77186));
			dSX[88]=(float)(kx+l*0.80443); dSY[88]=(float)(ky+l*(1.0-0.35896));
			dSX[89]=(float)(kx+l*0.72038); dSY[89]=(float)(ky+l*(1.0-0.90205));
			dSX[90]=(float)(kx+l*0.24934); dSY[90]=(float)(ky+l*(1.0-0.77047));
			dSX[91]=(float)(kx+l*0.35552); dSY[91]=(float)(ky+l*(1.0-0.70131));
			dSX[92]=(float)(kx+l*0.49591); dSY[92]=(float)(ky+l*(1.0-0.71126));
			dSX[93]=(float)(kx+l*0.36426); dSY[93]=(float)(ky+l*(1.0-0.72803));
			dSX[94]=(float)(kx+l*0.21113); dSY[94]=(float)(ky+l*(1.0-0.08745));
			dSX[95]=(float)(kx+l*0.33412); dSY[95]=(float)(ky+l*(1.0-0.68345));
			dSX[96]=(float)(kx+l*0.17158); dSY[96]=(float)(ky+l*(1.0-0.226));
			dSX[97]=(float)(kx+l*0.69135); dSY[97]=(float)(ky+l*(1.0-0.26172));
			dSX[98]=(float)(kx+l*0.80362); dSY[98]=(float)(ky+l*(1.0-0.34908));
			dSX[99]=(float)(kx+l*0.49367); dSY[99]=(float)(ky+l*(1.0-0.61372));
			dSX[100]=(float)(kx+l*0.67809); dSY[100]=(float)(ky+l*(1.0-0.16071));
			dSX[101]=(float)(kx+l*0.42288); dSY[101]=(float)(ky+l*(1.0-0.7547));
			dSX[102]=(float)(kx+l*0.21535); dSY[102]=(float)(ky+l*(1.0-0.71766));
			dSX[103]=(float)(kx+l*0.26248); dSY[103]=(float)(ky+l*(1.0-0.0794));
			dSX[104]=(float)(kx+l*0.65766); dSY[104]=(float)(ky+l*(1.0-0.11433));
			dSX[105]=(float)(kx+l*0.81799); dSY[105]=(float)(ky+l*(1.0-0.36416));
			dSX[106]=(float)(kx+l*0.80867); dSY[106]=(float)(ky+l*(1.0-0.39382));
			dSX[107]=(float)(kx+l*0.2401); dSY[107]=(float)(ky+l*(1.0-0.83207));
			dSX[108]=(float)(kx+l*0.83016); dSY[108]=(float)(ky+l*(1.0-0.37551));
			dSX[109]=(float)(kx+l*0.30746); dSY[109]=(float)(ky+l*(1.0-0.78597));
			dSX[110]=(float)(kx+l*0.22122); dSY[110]=(float)(ky+l*(1.0-0.19961));
			dSX[111]=(float)(kx+l*0.81422); dSY[111]=(float)(ky+l*(1.0-0.39008));
			dSX[112]=(float)(kx+l*0.28025); dSY[112]=(float)(ky+l*(1.0-0.16485));
			dSX[113]=(float)(kx+l*0.42936); dSY[113]=(float)(ky+l*(1.0-0.70449));
			dSX[114]=(float)(kx+l*0.20721); dSY[114]=(float)(ky+l*(1.0-0.79412));
			dSX[115]=(float)(kx+l*0.1023); dSY[115]=(float)(ky+l*(1.0-0.59687));
			dSX[116]=(float)(kx+l*0.49873); dSY[116]=(float)(ky+l*(1.0-0.68088));
			dSX[117]=(float)(kx+l*0.44373); dSY[117]=(float)(ky+l*(1.0-0.60472));
			dSX[118]=(float)(kx+l*0.12955); dSY[118]=(float)(ky+l*(1.0-0.58045));
			dSX[119]=(float)(kx+l*0.40319); dSY[119]=(float)(ky+l*(1.0-0.64087));
			dSX[120]=(float)(kx+l*0.39597); dSY[120]=(float)(ky+l*(1.0-0.74223));
			dSX[121]=(float)(kx+l*0.37318); dSY[121]=(float)(ky+l*(1.0-0.74561));
			dSX[122]=(float)(kx+l*0.48026); dSY[122]=(float)(ky+l*(1.0-0.65002));
			dSX[123]=(float)(kx+l*0.09824); dSY[123]=(float)(ky+l*(1.0-0.15969));
			dSX[124]=(float)(kx+l*0.68454); dSY[124]=(float)(ky+l*(1.0-0.17986));
			dSX[125]=(float)(kx+l*0.11659); dSY[125]=(float)(ky+l*(1.0-0.30008));
			dSX[126]=(float)(kx+l*0.73836); dSY[126]=(float)(ky+l*(1.0-0.87819));
			dSX[127]=(float)(kx+l*0.37924); dSY[127]=(float)(ky+l*(1.0-0.72885));
			dSX[128]=(float)(kx+l*0.07252); dSY[128]=(float)(ky+l*(1.0-0.21803));
			dSX[129]=(float)(kx+l*0.22104); dSY[129]=(float)(ky+l*(1.0-0.81961));
			dSX[130]=(float)(kx+l*0.23872); dSY[130]=(float)(ky+l*(1.0-0.06629));
			dSX[131]=(float)(kx+l*0.27114); dSY[131]=(float)(ky+l*(1.0-0.77851));
			dSX[132]=(float)(kx+l*0.84307); dSY[132]=(float)(ky+l*(1.0-0.35729));
			dSX[133]=(float)(kx+l*0.83856); dSY[133]=(float)(ky+l*(1.0-0.38892));
			dSX[134]=(float)(kx+l*0.84041); dSY[134]=(float)(ky+l*(1.0-0.33806));
			dSX[135]=(float)(kx+l*0.72441); dSY[135]=(float)(ky+l*(1.0-0.84423));
			dSX[136]=(float)(kx+l*0.45169); dSY[136]=(float)(ky+l*(1.0-0.66888));
			dSX[137]=(float)(kx+l*0.7291); dSY[137]=(float)(ky+l*(1.0-0.85748));
			dSX[138]=(float)(kx+l*0.38792); dSY[138]=(float)(ky+l*(1.0-0.74045));
			dSX[139]=(float)(kx+l*0.69006); dSY[139]=(float)(ky+l*(1.0-0.88995));
			dSX[140]=(float)(kx+l*0.09004); dSY[140]=(float)(ky+l*(1.0-0.57847));
			dSX[141]=(float)(kx+l*0.20986); dSY[141]=(float)(ky+l*(1.0-0.21552));
			dSX[142]=(float)(kx+l*0.22969); dSY[142]=(float)(ky+l*(1.0-0.79372));
			dSX[143]=(float)(kx+l*0.2407); dSY[143]=(float)(ky+l*(1.0-0.78147));
			dSX[144]=(float)(kx+l*0.83483); dSY[144]=(float)(ky+l*(1.0-0.35725));
			dSX[145]=(float)(kx+l*0.74069); dSY[145]=(float)(ky+l*(1.0-0.87034));
			dSX[146]=(float)(kx+l*0.53127); dSY[146]=(float)(ky+l*(1.0-0.69099));
			dSX[147]=(float)(kx+l*0.73562); dSY[147]=(float)(ky+l*(1.0-0.89203));
			dSX[148]=(float)(kx+l*0.22449); dSY[148]=(float)(ky+l*(1.0-0.14296));
			dSX[149]=(float)(kx+l*0.74473); dSY[149]=(float)(ky+l*(1.0-0.85085));
			dSX[150]=(float)(kx+l*0.80492); dSY[150]=(float)(ky+l*(1.0-0.40119));
			dSX[151]=(float)(kx+l*0.66545); dSY[151]=(float)(ky+l*(1.0-0.14658));
			dSX[152]=(float)(kx+l*0.74401); dSY[152]=(float)(ky+l*(1.0-0.88545));
			dSX[153]=(float)(kx+l*0.16486); dSY[153]=(float)(ky+l*(1.0-0.81768));
			dSX[154]=(float)(kx+l*0.10909); dSY[154]=(float)(ky+l*(1.0-0.58963));
			dSX[155]=(float)(kx+l*0.36812); dSY[155]=(float)(ky+l*(1.0-0.71451));
			dSX[156]=(float)(kx+l*0.77083); dSY[156]=(float)(ky+l*(1.0-0.86754));
			dSX[157]=(float)(kx+l*0.19709); dSY[157]=(float)(ky+l*(1.0-0.16813));
			dSX[158]=(float)(kx+l*0.08257); dSY[158]=(float)(ky+l*(1.0-0.57901));
			dSX[159]=(float)(kx+l*0.81561); dSY[159]=(float)(ky+l*(1.0-0.38789));
			dSX[160]=(float)(kx+l*0.11613); dSY[160]=(float)(ky+l*(1.0-0.61403));
			dSX[161]=(float)(kx+l*0.16391); dSY[161]=(float)(ky+l*(1.0-0.10041));
			dSX[162]=(float)(kx+l*0.36024); dSY[162]=(float)(ky+l*(1.0-0.75178));
			dSX[163]=(float)(kx+l*0.73822); dSY[163]=(float)(ky+l*(1.0-0.84884));
			dSX[164]=(float)(kx+l*0.22963); dSY[164]=(float)(ky+l*(1.0-0.11442));
			dSX[165]=(float)(kx+l*0.01152); dSY[165]=(float)(ky+l*(1.0-0.27939));
			dSX[166]=(float)(kx+l*0.74314); dSY[166]=(float)(ky+l*(1.0-0.87522));
			dSX[167]=(float)(kx+l*0.22871); dSY[167]=(float)(ky+l*(1.0-0.134));
			dSX[168]=(float)(kx+l*0.14996); dSY[168]=(float)(ky+l*(1.0-0.54459));
			dSX[169]=(float)(kx+l*0.14354); dSY[169]=(float)(ky+l*(1.0-0.25589));
			dSX[170]=(float)(kx+l*0.0779); dSY[170]=(float)(ky+l*(1.0-0.2636));
			dSX[171]=(float)(kx+l*0.13208); dSY[171]=(float)(ky+l*(1.0-0.28005));
			dSX[172]=(float)(kx+l*0.2498); dSY[172]=(float)(ky+l*(1.0-0.75765));
			dSX[173]=(float)(kx+l*0.30859); dSY[173]=(float)(ky+l*(1.0-0.08592));
			dSX[174]=(float)(kx+l*0.03277); dSY[174]=(float)(ky+l*(1.0-0.25141));
			dSX[175]=(float)(kx+l*0.69026); dSY[175]=(float)(ky+l*(1.0-0.11579));
			dSX[176]=(float)(kx+l*0.70569); dSY[176]=(float)(ky+l*(1.0-0.20655));
			dSX[177]=(float)(kx+l*0.19796); dSY[177]=(float)(ky+l*(1.0-0.1327));
			dSX[178]=(float)(kx+l*0.10402); dSY[178]=(float)(ky+l*(1.0-0.18623));
			dSX[179]=(float)(kx+l*0.20623); dSY[179]=(float)(ky+l*(1.0-0.17315));
			dSX[180]=(float)(kx+l*0.14383); dSY[180]=(float)(ky+l*(1.0-0.16819));
			dSX[181]=(float)(kx+l*0.43416); dSY[181]=(float)(ky+l*(1.0-0.81161));
			dSX[182]=(float)(kx+l*0.21801); dSY[182]=(float)(ky+l*(1.0-0.1926));
			dSX[183]=(float)(kx+l*0.80582); dSY[183]=(float)(ky+l*(1.0-0.40684));
			dSX[184]=(float)(kx+l*0.47273); dSY[184]=(float)(ky+l*(1.0-0.66746));
			dSX[185]=(float)(kx+l*0.72923); dSY[185]=(float)(ky+l*(1.0-0.91807));
			dSX[186]=(float)(kx+l*0.21609); dSY[186]=(float)(ky+l*(1.0-0.14719));
			dSX[187]=(float)(kx+l*0.61592); dSY[187]=(float)(ky+l*(1.0-0.17603));
			dSX[188]=(float)(kx+l*0.25956); dSY[188]=(float)(ky+l*(1.0-0.74824));
			dSX[189]=(float)(kx+l*0.10157); dSY[189]=(float)(ky+l*(1.0-0.25437));
			dSX[190]=(float)(kx+l*0.34822); dSY[190]=(float)(ky+l*(1.0-0.74119));
			dSX[191]=(float)(kx+l*0.37535); dSY[191]=(float)(ky+l*(1.0-0.68263));
			dSX[192]=(float)(kx+l*0.11609); dSY[192]=(float)(ky+l*(1.0-0.25491));
			dSX[193]=(float)(kx+l*0.84751); dSY[193]=(float)(ky+l*(1.0-0.36326));
			dSX[194]=(float)(kx+l*0.48434); dSY[194]=(float)(ky+l*(1.0-0.71852));
			dSX[195]=(float)(kx+l*0.82894); dSY[195]=(float)(ky+l*(1.0-0.38072));
			dSX[196]=(float)(kx+l*0.23618); dSY[196]=(float)(ky+l*(1.0-0.78797));
			dSX[197]=(float)(kx+l*0.70894); dSY[197]=(float)(ky+l*(1.0-0.84481));
			dSX[198]=(float)(kx+l*0.21377); dSY[198]=(float)(ky+l*(1.0-0.08697));
			dSX[199]=(float)(kx+l*0.08777); dSY[199]=(float)(ky+l*(1.0-0.23077));
			dSX[200]=(float)(kx+l*0.4627); dSY[200]=(float)(ky+l*(1.0-0.68689));
			dSX[201]=(float)(kx+l*0.1064); dSY[201]=(float)(ky+l*(1.0-0.13423));
			dSX[202]=(float)(kx+l*0.34044); dSY[202]=(float)(ky+l*(1.0-0.71728));
			dSX[203]=(float)(kx+l*0.14377); dSY[203]=(float)(ky+l*(1.0-0.10488));
			dSX[204]=(float)(kx+l*0.83586); dSY[204]=(float)(ky+l*(1.0-0.39654));
			dSX[205]=(float)(kx+l*0.23719); dSY[205]=(float)(ky+l*(1.0-0.75877));
			dSX[206]=(float)(kx+l*0.72909); dSY[206]=(float)(ky+l*(1.0-0.83794));
			dSX[207]=(float)(kx+l*0.11163); dSY[207]=(float)(ky+l*(1.0-0.57717));
			dSX[208]=(float)(kx+l*0.82082); dSY[208]=(float)(ky+l*(1.0-0.38887));
			dSX[209]=(float)(kx+l*0.23973); dSY[209]=(float)(ky+l*(1.0-0.09762));
			dSX[210]=(float)(kx+l*0.18049); dSY[210]=(float)(ky+l*(1.0-0.7213));
			dSX[211]=(float)(kx+l*0.17251); dSY[211]=(float)(ky+l*(1.0-0.06261));
			dSX[212]=(float)(kx+l*0.73943); dSY[212]=(float)(ky+l*(1.0-0.1515));
			dSX[213]=(float)(kx+l*0.12257); dSY[213]=(float)(ky+l*(1.0-0.21737));
			dSX[214]=(float)(kx+l*0.72598); dSY[214]=(float)(ky+l*(1.0-0.87021));
			dSX[215]=(float)(kx+l*0.7244); dSY[215]=(float)(ky+l*(1.0-0.88142));
			dSX[216]=(float)(kx+l*0.21058); dSY[216]=(float)(ky+l*(1.0-0.83842));
			dSX[217]=(float)(kx+l*0.34401); dSY[217]=(float)(ky+l*(1.0-0.72108));
			dSX[218]=(float)(kx+l*0.65233); dSY[218]=(float)(ky+l*(1.0-0.15241));
			dSX[219]=(float)(kx+l*0.1184); dSY[219]=(float)(ky+l*(1.0-0.59815));
			dSX[220]=(float)(kx+l*0.20673); dSY[220]=(float)(ky+l*(1.0-0.09814));
			dSX[221]=(float)(kx+l*0.65673); dSY[221]=(float)(ky+l*(1.0-0.19377));
			dSX[222]=(float)(kx+l*0.46674); dSY[222]=(float)(ky+l*(1.0-0.7408));
			dSX[223]=(float)(kx+l*0.14444); dSY[223]=(float)(ky+l*(1.0-0.18892));
			dSX[224]=(float)(kx+l*0.44631); dSY[224]=(float)(ky+l*(1.0-0.72023));
			dSX[225]=(float)(kx+l*0.18501); dSY[225]=(float)(ky+l*(1.0-0.81523));
			dSX[226]=(float)(kx+l*0.67013); dSY[226]=(float)(ky+l*(1.0-0.17383));
			dSX[227]=(float)(kx+l*0.21007); dSY[227]=(float)(ky+l*(1.0-0.11003));
			dSX[228]=(float)(kx+l*0.28895); dSY[228]=(float)(ky+l*(1.0-0.79667));
			dSX[229]=(float)(kx+l*0.355); dSY[229]=(float)(ky+l*(1.0-0.77679));
			dSX[230]=(float)(kx+l*0.8031); dSY[230]=(float)(ky+l*(1.0-0.40707));
			dSX[231]=(float)(kx+l*0.20507); dSY[231]=(float)(ky+l*(1.0-0.23746));
			dSX[232]=(float)(kx+l*0.2091); dSY[232]=(float)(ky+l*(1.0-0.1445));
			dSX[233]=(float)(kx+l*0.69395); dSY[233]=(float)(ky+l*(1.0-0.87292));
			dSX[234]=(float)(kx+l*0.26225); dSY[234]=(float)(ky+l*(1.0-0.83517));
			dSX[235]=(float)(kx+l*0.46057); dSY[235]=(float)(ky+l*(1.0-0.66066));
			dSX[236]=(float)(kx+l*0.46715); dSY[236]=(float)(ky+l*(1.0-0.62083));
			dSX[237]=(float)(kx+l*0.15991); dSY[237]=(float)(ky+l*(1.0-0.16164));
			dSX[238]=(float)(kx+l*0.66818); dSY[238]=(float)(ky+l*(1.0-0.18336));
			dSX[239]=(float)(kx+l*0.1206); dSY[239]=(float)(ky+l*(1.0-0.20415));
			dSX[240]=(float)(kx+l*0.11134); dSY[240]=(float)(ky+l*(1.0-0.17899));
			dSX[241]=(float)(kx+l*0.81705); dSY[241]=(float)(ky+l*(1.0-0.3876));
			dSX[242]=(float)(kx+l*0.16158); dSY[242]=(float)(ky+l*(1.0-0.58197));
			dSX[243]=(float)(kx+l*0.71638); dSY[243]=(float)(ky+l*(1.0-0.92072));
			dSX[244]=(float)(kx+l*0.70824); dSY[244]=(float)(ky+l*(1.0-0.86881));
			dSX[245]=(float)(kx+l*0.21828); dSY[245]=(float)(ky+l*(1.0-0.07292));
			dSX[246]=(float)(kx+l*0.23573); dSY[246]=(float)(ky+l*(1.0-0.05748));
			dSX[247]=(float)(kx+l*0.11584); dSY[247]=(float)(ky+l*(1.0-0.70939));
			dSX[248]=(float)(kx+l*0.1235); dSY[248]=(float)(ky+l*(1.0-0.23567));
			dSX[249]=(float)(kx+l*0.18015); dSY[249]=(float)(ky+l*(1.0-0.73396));
			dSX[250]=(float)(kx+l*0.04505); dSY[250]=(float)(ky+l*(1.0-0.22103));
			dSX[251]=(float)(kx+l*0.75712); dSY[251]=(float)(ky+l*(1.0-0.89093));
			dSX[252]=(float)(kx+l*0.51855); dSY[252]=(float)(ky+l*(1.0-0.68719));
			dSX[253]=(float)(kx+l*0.20628); dSY[253]=(float)(ky+l*(1.0-0.78227));
			dSX[254]=(float)(kx+l*0.3027); dSY[254]=(float)(ky+l*(1.0-0.74326));
			dSX[255]=(float)(kx+l*0.72344); dSY[255]=(float)(ky+l*(1.0-0.86115));
			dSX[256]=(float)(kx+l*0.7823); dSY[256]=(float)(ky+l*(1.0-0.40698));
			dSX[257]=(float)(kx+l*0.80725); dSY[257]=(float)(ky+l*(1.0-0.36859));
			dSX[258]=(float)(kx+l*0.22767); dSY[258]=(float)(ky+l*(1.0-0.72875));
			dSX[259]=(float)(kx+l*0.8389); dSY[259]=(float)(ky+l*(1.0-0.36071));
			dSX[260]=(float)(kx+l*0.15643); dSY[260]=(float)(ky+l*(1.0-0.1861));
			dSX[261]=(float)(kx+l*0.70301); dSY[261]=(float)(ky+l*(1.0-0.13106));
			dSX[262]=(float)(kx+l*0.27509); dSY[262]=(float)(ky+l*(1.0-0.07504));
			dSX[263]=(float)(kx+l*0.26088); dSY[263]=(float)(ky+l*(1.0-0.7257));
			dSX[264]=(float)(kx+l*0.21206); dSY[264]=(float)(ky+l*(1.0-0.75771));
			dSX[265]=(float)(kx+l*0.13805); dSY[265]=(float)(ky+l*(1.0-0.58384));
			dSX[266]=(float)(kx+l*0.77447); dSY[266]=(float)(ky+l*(1.0-0.88097));
			dSX[267]=(float)(kx+l*0.24243); dSY[267]=(float)(ky+l*(1.0-0.76475));
			dSX[268]=(float)(kx+l*0.22454); dSY[268]=ky+l; // -0.01632
			dSX[269]=(float)(kx+l*0.46329); dSY[269]=(float)(ky+l*(1.0-0.66386));
			dSX[270]=(float)(kx+l*0.42856); dSY[270]=(float)(ky+l*(1.0-0.77059));
			dSX[271]=(float)(kx+l*0.09912); dSY[271]=(float)(ky+l*(1.0-0.23061));
			dSX[272]=(float)(kx+l*0.40031); dSY[272]=(float)(ky+l*(1.0-0.7123));
			dSX[273]=(float)(kx+l*0.41085); dSY[273]=(float)(ky+l*(1.0-0.68234));
			dSX[274]=(float)(kx+l*0.16843); dSY[274]=(float)(ky+l*(1.0-0.21484));
			dSX[275]=(float)(kx+l*0.19902); dSY[275]=(float)(ky+l*(1.0-0.74001));
			dSX[276]=(float)(kx+l*0.23913); dSY[276]=(float)(ky+l*(1.0-0.83891));
			dSX[277]=(float)(kx+l*0.52281); dSY[277]=(float)(ky+l*(1.0-0.68538));
			dSX[278]=(float)(kx+l*0.77193); dSY[278]=(float)(ky+l*(1.0-0.85292));
			dSX[279]=(float)(kx+l*0.62099); dSY[279]=(float)(ky+l*(1.0-0.1954));
			dSX[280]=(float)(kx+l*0.76508); dSY[280]=(float)(ky+l*(1.0-0.88685));
			dSX[281]=(float)(kx+l*0.17655); dSY[281]=(float)(ky+l*(1.0-0.19809));
			dSX[282]=(float)(kx+l*0.25752); dSY[282]=(float)(ky+l*(1.0-0.14649));
			dSX[283]=(float)(kx+l*0.41938); dSY[283]=(float)(ky+l*(1.0-0.66025));
			dSX[284]=(float)(kx+l*0.64649); dSY[284]=(float)(ky+l*(1.0-0.16984));
			dSX[285]=(float)(kx+l*0.83039); dSY[285]=(float)(ky+l*(1.0-0.36848));
			dSX[286]=(float)(kx+l*0.47482); dSY[286]=(float)(ky+l*(1.0-0.75725));
			dSX[287]=(float)(kx+l*0.36224); dSY[287]=(float)(ky+l*(1.0-0.72407));
			dSX[288]=(float)(kx+l*0.41691); dSY[288]=(float)(ky+l*(1.0-0.76211));
			dSX[289]=(float)(kx+l*0.71197); dSY[289]=(float)(ky+l*(1.0-0.88301));
			dSX[290]=(float)(kx+l*0.38875); dSY[290]=(float)(ky+l*(1.0-0.66923));
			dSX[291]=(float)(kx+l*0.65122); dSY[291]=(float)(ky+l*(1.0-0.2006));
			dSX[292]=(float)(kx+l*0.14963); dSY[292]=(float)(ky+l*(1.0-0.58176));
			dSX[293]=(float)(kx+l*0.12702); dSY[293]=(float)(ky+l*(1.0-0.20527));
			dSX[294]=(float)(kx+l*0.21471); dSY[294]=(float)(ky+l*(1.0-0.80336));
			dSX[295]=(float)(kx+l*0.1117); dSY[295]=(float)(ky+l*(1.0-0.58565));
			dSX[296]=(float)(kx+l*0.21964); dSY[296]=(float)(ky+l*(1.0-0.05787));
			dSX[297]=(float)(kx+l*0.52603); dSY[297]=(float)(ky+l*(1.0-0.68563));
			dSX[298]=(float)(kx+l*0.2513); dSY[298]=(float)(ky+l*(1.0-0.11038));
			dSX[299]=(float)(kx+l*0.23048); dSY[299]=(float)(ky+l*(1.0-0.81628));
			dSX[300]=(float)(kx+l*0.07918); dSY[300]=(float)(ky+l*(1.0-0.59745));
			dSX[301]=(float)(kx+l*0.39107); dSY[301]=(float)(ky+l*(1.0-0.70391));
			dSX[302]=(float)(kx+l*0.1921); dSY[302]=(float)(ky+l*(1.0-0.75531));
			dSX[303]=(float)(kx+l*0.69606); dSY[303]=(float)(ky+l*(1.0-0.21785));
			dSX[304]=(float)(kx+l*0.14619); dSY[304]=(float)(ky+l*(1.0-0.57905));
			dSX[305]=(float)(kx+l*0.79628); dSY[305]=(float)(ky+l*(1.0-0.3461));
			dSX[306]=(float)(kx+l*0.26276); dSY[306]=(float)(ky+l*(1.0-0.17608));
			dSX[307]=(float)(kx+l*0.32383); dSY[307]=(float)(ky+l*(1.0-0.74517));
			dSX[308]=(float)(kx+l*0.71259); dSY[308]=(float)(ky+l*(1.0-0.85462));
			dSX[309]=(float)(kx+l*0.11917); dSY[309]=(float)(ky+l*(1.0-0.25115));
			dSX[310]=(float)(kx+l*0.15771); dSY[310]=(float)(ky+l*(1.0-0.5723));
			dSX[311]=(float)(kx+l*0.74207); dSY[311]=(float)(ky+l*(1.0-0.86498));
			dSX[312]=(float)(kx+l*0.30246); dSY[312]=(float)(ky+l*(1.0-0.66994));
			dSX[313]=(float)(kx+l*0.20864); dSY[313]=(float)(ky+l*(1.0-0.16323));
			dSX[314]=(float)(kx+l*0.1412); dSY[314]=(float)(ky+l*(1.0-0.56028));
			dSX[315]=(float)(kx+l*0.82053); dSY[315]=(float)(ky+l*(1.0-0.35693));
			dSX[316]=(float)(kx+l*0.22989); dSY[316]=(float)(ky+l*(1.0-0.81021));
			dSX[317]=(float)(kx+l*0.10676); dSY[317]=(float)(ky+l*(1.0-0.20945));
			dSX[318]=(float)(kx+l*0.24867); dSY[318]=(float)(ky+l*(1.0-0.13241));
			dSX[319]=(float)(kx+l*0.18081); dSY[319]=(float)(ky+l*(1.0-0.77961));
			dSX[320]=(float)(kx+l*0.8051); dSY[320]=(float)(ky+l*(1.0-0.37642));
			dSX[321]=(float)(kx+l*0.71303); dSY[321]=(float)(ky+l*(1.0-0.87868));
			dSX[322]=(float)(kx+l*0.20502); dSY[322]=(float)(ky+l*(1.0-0.20587));
			dSX[323]=(float)(kx+l*0.47605); dSY[323]=(float)(ky+l*(1.0-0.68292));
			dSX[324]=(float)(kx+l*0.20975); dSY[324]=(float)(ky+l*(1.0-0.13444));
			dSX[325]=(float)(kx+l*0.7098); dSY[325]=(float)(ky+l*(1.0-0.85967));
			dSX[326]=(float)(kx+l*0.19912); dSY[326]=(float)(ky+l*(1.0-0.11887));
			dSX[327]=(float)(kx+l*0.21338); dSY[327]=(float)(ky+l*(1.0-0.15242));
			dSX[328]=(float)(kx+l*0.0816); dSY[328]=(float)(ky+l*(1.0-0.20505));
			dSX[329]=(float)(kx+l*0.81617); dSY[329]=(float)(ky+l*(1.0-0.37632));
			dSX[330]=(float)(kx+l*0.11072); dSY[330]=(float)(ky+l*(1.0-0.1742));
			dSX[331]=(float)(kx+l*0.44663); dSY[331]=(float)(ky+l*(1.0-0.7283));
			dSX[332]=(float)(kx+l*0.43758); dSY[332]=(float)(ky+l*(1.0-0.7116));
			dSX[333]=(float)(kx+l*0.11169); dSY[333]=(float)(ky+l*(1.0-0.58286));
			dSX[334]=(float)(kx+l*0.21739); dSY[334]=(float)(ky+l*(1.0-0.808));
			dSX[335]=(float)(kx+l*0.11504); dSY[335]=(float)(ky+l*(1.0-0.58542));
			dSX[336]=(float)(kx+l*0.22232); dSY[336]=(float)(ky+l*(1.0-0.10244));
			dSX[337]=(float)(kx+l*0.13277); dSY[337]=(float)(ky+l*(1.0-0.5679));
			dSX[338]=(float)(kx+l*0.41598); dSY[338]=(float)(ky+l*(1.0-0.73469));
			dSX[339]=(float)(kx+l*0.23372); dSY[339]=(float)(ky+l*(1.0-0.76431));
			dSX[340]=(float)(kx+l*0.32057); dSY[340]=(float)(ky+l*(1.0-0.75133));
			dSX[341]=(float)(kx+l*0.82525); dSY[341]=(float)(ky+l*(1.0-0.39564));
			dSX[342]=(float)(kx+l*0.15967); dSY[342]=(float)(ky+l*(1.0-0.17686));
			dSX[343]=(float)(kx+l*0.65594); dSY[343]=(float)(ky+l*(1.0-0.90155));
			dSX[344]=(float)(kx+l*0.71754); dSY[344]=(float)(ky+l*(1.0-0.87787));
			dSX[345]=(float)(kx+l*0.11191); dSY[345]=(float)(ky+l*(1.0-0.59932));
			dSX[346]=(float)(kx+l*0.2125); dSY[346]=(float)(ky+l*(1.0-0.05011));
			dSX[347]=(float)(kx+l*0.21381); dSY[347]=(float)(ky+l*(1.0-0.13874));
			dSX[348]=(float)(kx+l*0.32597); dSY[348]=(float)(ky+l*(1.0-0.702));
			dSX[349]=(float)(kx+l*0.84447); dSY[349]=(float)(ky+l*(1.0-0.377));
			dSX[350]=(float)(kx+l*0.23257); dSY[350]=(float)(ky+l*(1.0-0.0836));
			dSX[351]=(float)(kx+l*0.09849); dSY[351]=(float)(ky+l*(1.0-0.15117));
			dSX[352]=(float)(kx+l*0.25526); dSY[352]=(float)(ky+l*(1.0-0.156));
			dSX[353]=(float)(kx+l*0.46334); dSY[353]=(float)(ky+l*(1.0-0.69123));
			dSX[354]=(float)(kx+l*0.48943); dSY[354]=(float)(ky+l*(1.0-0.75123));
			dSX[355]=(float)(kx+l*0.7088); dSY[355]=(float)(ky+l*(1.0-0.8525));
			dSX[356]=(float)(kx+l*0.29138); dSY[356]=(float)(ky+l*(1.0-0.73165));
			dSX[357]=(float)(kx+l*0.15562); dSY[357]=(float)(ky+l*(1.0-0.80957));
			dSX[358]=(float)(kx+l*0.45633); dSY[358]=(float)(ky+l*(1.0-0.62115));
			dSX[359]=(float)(kx+l*0.22247); dSY[359]=(float)(ky+l*(1.0-0.73574));
			dSX[360]=(float)(kx+l*0.20278); dSY[360]=(float)(ky+l*(1.0-0.02718));
			dSX[361]=(float)(kx+l*0.1757); dSY[361]=(float)(ky+l*(1.0-0.77329));
			dSX[362]=(float)(kx+l*0.81154); dSY[362]=(float)(ky+l*(1.0-0.34851));
			dSX[363]=(float)(kx+l*0.63127); dSY[363]=(float)(ky+l*(1.0-0.19212));
			dSX[364]=(float)(kx+l*0.80712); dSY[364]=(float)(ky+l*(1.0-0.3727));
			dSX[365]=(float)(kx+l*0.79678); dSY[365]=(float)(ky+l*(1.0-0.37069));
			dSX[366]=(float)(kx+l*0.65493); dSY[366]=(float)(ky+l*(1.0-0.17201));
			dSX[367]=(float)(kx+l*0.11119); dSY[367]=(float)(ky+l*(1.0-0.55032));
			dSX[368]=(float)(kx+l*0.35914); dSY[368]=(float)(ky+l*(1.0-0.69928));
			dSX[369]=(float)(kx+l*0.84783); dSY[369]=(float)(ky+l*(1.0-0.38467));
			dSX[370]=(float)(kx+l*0.25637); dSY[370]=(float)(ky+l*(1.0-0.16449));
			dSX[371]=(float)(kx+l*0.4251); dSY[371]=(float)(ky+l*(1.0-0.75901));
			dSX[372]=(float)(kx+l*0.19824); dSY[372]=(float)(ky+l*(1.0-0.85476));
			dSX[373]=(float)(kx+l*0.49887); dSY[373]=(float)(ky+l*(1.0-0.69768));
			dSX[374]=(float)(kx+l*0.86102); dSY[374]=(float)(ky+l*(1.0-0.37142));
			dSX[375]=(float)(kx+l*0.19372); dSY[375]=(float)(ky+l*(1.0-0.80485));
			dSX[376]=(float)(kx+l*0.11601); dSY[376]=(float)(ky+l*(1.0-0.55327));
			dSX[377]=(float)(kx+l*0.72774); dSY[377]=(float)(ky+l*(1.0-0.87631));
			dSX[378]=(float)(kx+l*0.24923); dSY[378]=(float)(ky+l*(1.0-0.79912));
			dSX[379]=(float)(kx+l*0.4765); dSY[379]=(float)(ky+l*(1.0-0.68893));
			dSX[380]=(float)(kx+l*0.82476); dSY[380]=(float)(ky+l*(1.0-0.35662));
			dSX[381]=(float)(kx+l*0.73111); dSY[381]=(float)(ky+l*(1.0-0.17849));
			dSX[382]=(float)(kx+l*0.23645); dSY[382]=(float)(ky+l*(1.0-0.8192));
			dSX[383]=(float)(kx+l*0.24282); dSY[383]=(float)(ky+l*(1.0-0.79375));
			dSX[384]=(float)(kx+l*0.32193); dSY[384]=(float)(ky+l*(1.0-0.73014));
			dSX[385]=(float)(kx+l*0.18991); dSY[385]=(float)(ky+l*(1.0-0.76666));
			dSX[386]=(float)(kx+l*0.4943); dSY[386]=(float)(ky+l*(1.0-0.64545));
			dSX[387]=(float)(kx+l*0.45752); dSY[387]=(float)(ky+l*(1.0-0.68871));
			dSX[388]=(float)(kx+l*0.27258); dSY[388]=(float)(ky+l*(1.0-0.75787));
			dSX[389]=(float)(kx+l*0.48832); dSY[389]=(float)(ky+l*(1.0-0.66738));
			dSX[390]=(float)(kx+l*0.70802); dSY[390]=(float)(ky+l*(1.0-0.84396));
			dSX[391]=(float)(kx+l*0.36794); dSY[391]=(float)(ky+l*(1.0-0.63548));
			dSX[392]=(float)(kx+l*0.37738); dSY[392]=(float)(ky+l*(1.0-0.73531));
			dSX[393]=(float)(kx+l*0.23611); dSY[393]=(float)(ky+l*(1.0-0.10068));
			dSX[394]=(float)(kx+l*0.2211); dSY[394]=(float)(ky+l*(1.0-0.77825));
			dSX[395]=(float)(kx+l*0.21163); dSY[395]=(float)(ky+l*(1.0-0.12681));
			dSX[396]=(float)(kx+l*0.18089); dSY[396]=(float)(ky+l*(1.0-0.73562));
			dSX[397]=(float)(kx+l*0.66299); dSY[397]=(float)(ky+l*(1.0-0.22315));
			dSX[398]=(float)(kx+l*0.21315); dSY[398]=(float)(ky+l*(1.0-0.13477));
			dSX[399]=(float)(kx+l*0.71559); dSY[399]=(float)(ky+l*(1.0-0.89859));
			dSX[400]=(float)(kx+l*0.43222); dSY[400]=(float)(ky+l*(1.0-0.77103));
			dSX[401]=(float)(kx+l*0.87618); dSY[401]=(float)(ky+l*(1.0-0.37878));
			dSX[402]=(float)(kx+l*0.26296); dSY[402]=(float)(ky+l*(1.0-0.15748));
			dSX[403]=(float)(kx+l*0.1686); dSY[403]=(float)(ky+l*(1.0-0.20094));
			dSX[404]=(float)(kx+l*0.6815); dSY[404]=(float)(ky+l*(1.0-0.10764));
			dSX[405]=(float)(kx+l*0.37811); dSY[405]=(float)(ky+l*(1.0-0.70541));
			dSX[406]=(float)(kx+l*0.36094); dSY[406]=(float)(ky+l*(1.0-0.67579));
			dSX[407]=(float)(kx+l*0.82511); dSY[407]=(float)(ky+l*(1.0-0.41853));
			dSX[408]=(float)(kx+l*0.14138); dSY[408]=(float)(ky+l*(1.0-0.23299));
			dSX[409]=(float)(kx+l*0.67249); dSY[409]=(float)(ky+l*(1.0-0.20002));
			dSX[410]=(float)(kx+l*0.23894); dSY[410]=(float)(ky+l*(1.0-0.17142));
			dSX[411]=(float)(kx+l*0.75744); dSY[411]=(float)(ky+l*(1.0-0.14058));
			dSX[412]=(float)(kx+l*0.17161); dSY[412]=(float)(ky+l*(1.0-0.10035));
			dSX[413]=(float)(kx+l*0.48828); dSY[413]=(float)(ky+l*(1.0-0.66026));
			dSX[414]=(float)(kx+l*0.09221); dSY[414]=(float)(ky+l*(1.0-0.24637));
			dSX[415]=(float)(kx+l*0.16063); dSY[415]=(float)(ky+l*(1.0-0.59428));
			dSX[416]=(float)(kx+l*0.12893); dSY[416]=(float)(ky+l*(1.0-0.59674));
			dSX[417]=(float)(kx+l*0.35694); dSY[417]=(float)(ky+l*(1.0-0.78796));
			dSX[418]=(float)(kx+l*0.41546); dSY[418]=(float)(ky+l*(1.0-0.76092));
			dSX[419]=(float)(kx+l*0.16968); dSY[419]=(float)(ky+l*(1.0-0.83991));
			dSX[420]=(float)(kx+l*0.10334); dSY[420]=(float)(ky+l*(1.0-0.13985));
			dSX[421]=(float)(kx+l*0.16873); dSY[421]=(float)(ky+l*(1.0-0.03174));
			dSX[422]=(float)(kx+l*0.09976); dSY[422]=(float)(ky+l*(1.0-0.57833));
			dSX[423]=(float)(kx+l*0.73443); dSY[423]=(float)(ky+l*(1.0-0.86841));
			dSX[424]=(float)(kx+l*0.2138); dSY[424]=(float)(ky+l*(1.0-0.14457));
			dSX[425]=(float)(kx+l*0.18475); dSY[425]=(float)(ky+l*(1.0-0.73202));
			dSX[426]=(float)(kx+l*0.48298); dSY[426]=(float)(ky+l*(1.0-0.70441));
			dSX[427]=(float)(kx+l*0.18751); dSY[427]=(float)(ky+l*(1.0-0.17179));
			dSX[428]=(float)(kx+l*0.15242); dSY[428]=(float)(ky+l*(1.0-0.56863));
			dSX[429]=(float)(kx+l*0.47199); dSY[429]=(float)(ky+l*(1.0-0.60514));
			dSX[430]=(float)(kx+l*0.08912); dSY[430]=(float)(ky+l*(1.0-0.59353));
			dSX[431]=(float)(kx+l*0.14872); dSY[431]=(float)(ky+l*(1.0-0.63872));
			dSX[432]=(float)(kx+l*0.79864); dSY[432]=(float)(ky+l*(1.0-0.35493));
			dSX[433]=(float)(kx+l*0.35112); dSY[433]=(float)(ky+l*(1.0-0.78383));
			dSX[434]=(float)(kx+l*0.69891); dSY[434]=(float)(ky+l*(1.0-0.84894));
			dSX[435]=(float)(kx+l*0.80731); dSY[435]=(float)(ky+l*(1.0-0.39325));
			dSX[436]=(float)(kx+l*0.82968); dSY[436]=(float)(ky+l*(1.0-0.3552));
			dSX[437]=(float)(kx+l*0.72571); dSY[437]=(float)(ky+l*(1.0-0.19687));
			dSX[438]=(float)(kx+l*0.69843); dSY[438]=(float)(ky+l*(1.0-0.84846));
			dSX[439]=(float)(kx+l*0.84693); dSY[439]=(float)(ky+l*(1.0-0.40964));
			dSX[440]=(float)(kx+l*0.20669); dSY[440]=(float)(ky+l*(1.0-0.77071));
			dSX[441]=(float)(kx+l*0.12141); dSY[441]=(float)(ky+l*(1.0-0.58855));
			dSX[442]=(float)(kx+l*0.2279); dSY[442]=(float)(ky+l*(1.0-0.12276));
			dSX[443]=(float)(kx+l*0.83297); dSY[443]=(float)(ky+l*(1.0-0.39735));
			dSX[444]=(float)(kx+l*0.14542); dSY[444]=(float)(ky+l*(1.0-0.56013));
			dSX[445]=(float)(kx+l*0.12433); dSY[445]=(float)(ky+l*(1.0-0.20911));
			dSX[446]=(float)(kx+l*0.72573); dSY[446]=(float)(ky+l*(1.0-0.8408));
			dSX[447]=(float)(kx+l*0.09379); dSY[447]=(float)(ky+l*(1.0-0.55713));
			dSX[448]=(float)(kx+l*0.14829); dSY[448]=(float)(ky+l*(1.0-0.23154));
			dSX[449]=(float)(kx+l*0.4523); dSY[449]=(float)(ky+l*(1.0-0.67249));
			dSX[450]=(float)(kx+l*0.11726); dSY[450]=(float)(ky+l*(1.0-0.19693));
			dSX[451]=(float)(kx+l*0.11815); dSY[451]=(float)(ky+l*(1.0-0.25814));
			dSX[452]=(float)(kx+l*0.67506); dSY[452]=(float)(ky+l*(1.0-0.17122));
			dSX[453]=(float)(kx+l*0.83483); dSY[453]=(float)(ky+l*(1.0-0.40775));
			dSX[454]=(float)(kx+l*0.07239); dSY[454]=(float)(ky+l*(1.0-0.18731));
			dSX[455]=(float)(kx+l*0.3272); dSY[455]=(float)(ky+l*(1.0-0.7225));
			dSX[456]=(float)(kx+l*0.16136); dSY[456]=(float)(ky+l*(1.0-0.61121));
			dSX[457]=(float)(kx+l*0.21065); dSY[457]=(float)(ky+l*(1.0-0.13483));
			dSX[458]=(float)(kx+l*0.71889); dSY[458]=(float)(ky+l*(1.0-0.20099));
			dSX[459]=(float)(kx+l*0.36902); dSY[459]=(float)(ky+l*(1.0-0.7864));
			dSX[460]=(float)(kx+l*0.84165); dSY[460]=(float)(ky+l*(1.0-0.36644));
			dSX[461]=(float)(kx+l*0.68612); dSY[461]=(float)(ky+l*(1.0-0.12094));
			dSX[462]=(float)(kx+l*0.22926); dSY[462]=(float)(ky+l*(1.0-0.16182));
			dSX[463]=(float)(kx+l*0.18717); dSY[463]=(float)(ky+l*(1.0-0.11579));
			dSX[464]=(float)(kx+l*0.80286); dSY[464]=(float)(ky+l*(1.0-0.32103));
			dSX[465]=(float)(kx+l*0.25034); dSY[465]=(float)(ky+l*(1.0-0.04969));
			dSX[466]=(float)(kx+l*0.25102); dSY[466]=(float)(ky+l*(1.0-0.81178));
			dSX[467]=(float)(kx+l*0.40104); dSY[467]=(float)(ky+l*(1.0-0.70706));
			dSX[468]=(float)(kx+l*0.47589); dSY[468]=(float)(ky+l*(1.0-0.62965));
			dSX[469]=(float)(kx+l*0.4878); dSY[469]=(float)(ky+l*(1.0-0.77431));
			dSX[470]=(float)(kx+l*0.44168); dSY[470]=(float)(ky+l*(1.0-0.69073));
			dSX[471]=(float)(kx+l*0.10281); dSY[471]=(float)(ky+l*(1.0-0.2403));
			dSX[472]=(float)(kx+l*0.82296); dSY[472]=(float)(ky+l*(1.0-0.3797));
			dSX[473]=(float)(kx+l*0.48731); dSY[473]=(float)(ky+l*(1.0-0.69098));
			dSX[474]=(float)(kx+l*0.63263); dSY[474]=(float)(ky+l*(1.0-0.22947));
			dSX[475]=(float)(kx+l*0.67528); dSY[475]=(float)(ky+l*(1.0-0.20604));
			dSX[476]=(float)(kx+l*0.19472); dSY[476]=(float)(ky+l*(1.0-0.11076));
			dSX[477]=(float)(kx+l*0.84223); dSY[477]=(float)(ky+l*(1.0-0.34943));
			dSX[478]=(float)(kx+l*0.66502); dSY[478]=(float)(ky+l*(1.0-0.2078));
			dSX[479]=(float)(kx+l*0.5253); dSY[479]=(float)(ky+l*(1.0-0.70939));
			dSX[480]=(float)(kx+l*0.26947); dSY[480]=(float)(ky+l*(1.0-0.10746));
			dSX[481]=(float)(kx+l*0.22708); dSY[481]=(float)(ky+l*(1.0-0.7998));
			dSX[482]=(float)(kx+l*0.39279); dSY[482]=(float)(ky+l*(1.0-0.6481));
			dSX[483]=(float)(kx+l*0.36533); dSY[483]=(float)(ky+l*(1.0-0.74434));
			dSX[484]=(float)(kx+l*0.21924); dSY[484]=(float)(ky+l*(1.0-0.76278));
			dSX[485]=(float)(kx+l*0.17686); dSY[485]=(float)(ky+l*(1.0-0.18335));
			dSX[486]=(float)(kx+l*0.51587); dSY[486]=(float)(ky+l*(1.0-0.5951));
			dSX[487]=(float)(kx+l*0.8566); dSY[487]=(float)(ky+l*(1.0-0.40405));
			dSX[488]=(float)(kx+l*0.12652); dSY[488]=(float)(ky+l*(1.0-0.57607));
			dSX[489]=(float)(kx+l*0.22685); dSY[489]=(float)(ky+l*(1.0-0.79786));
			dSX[490]=(float)(kx+l*0.68578); dSY[490]=(float)(ky+l*(1.0-0.8548));
			dSX[491]=(float)(kx+l*0.38968); dSY[491]=(float)(ky+l*(1.0-0.73713));
			dSX[492]=(float)(kx+l*0.70811); dSY[492]=(float)(ky+l*(1.0-0.19062));
			dSX[493]=(float)(kx+l*0.46795); dSY[493]=(float)(ky+l*(1.0-0.68742));
			dSX[494]=(float)(kx+l*0.70386); dSY[494]=(float)(ky+l*(1.0-0.13081));
			dSX[495]=(float)(kx+l*0.12347); dSY[495]=(float)(ky+l*(1.0-0.57067));
			dSX[496]=(float)(kx+l*0.07499); dSY[496]=(float)(ky+l*(1.0-0.58753));
			dSX[497]=(float)(kx+l*0.26596); dSY[497]=(float)(ky+l*(1.0-0.75632));
			dSX[498]=(float)(kx+l*0.2196); dSY[498]=(float)(ky+l*(1.0-0.81844));
			dSX[499]=(float)(kx+l*0.22364); dSY[499]=(float)(ky+l*(1.0-0.11876));
			
			for (int i = 0; i < discreteSize;i++) {
				discreteSignalsX[i]=dSX[i];
				discreteSignalsY[i]=dSY[i];
			}
//			for (int i = 0; i < MAX_DISCRETE_SIGNALS; i++) {
//				getSignal(pd);
//				discreteSignalsX[i] = SignalX;
//				discreteSignalsY[i] = SignalY;
//			}
		} 
	}
    protected Point2D.Double circlePoint() {
    	Point2D.Double origin = new Point2D.Double(0,0);
    	Point2D.Double signal = new Point2D.Double(0,0);
		do {
			signal.x = Math.random()-0.5;
			signal.y = Math.random()-0.5;
		} while (origin.distanceSq(signal) > 0.25 );
		// assertion: signal is on circle around origin with diameter 1, radius 0.5
		//System.out.printf("%f %f \n",signal.getX(),signal.getY());
		return signal;
    }

	/**
	 * Generate a signal for the given distribution.
	 *  The result goes into the global variables <TT> SignalX </TT>
	 *  and <TT> SignalY </TT>.
	 * 
	 * @param pd          The specified distribution
	 */
	protected void getSignal(PD pd) {
		int wi = panelWidth;
		int hi = panelHeight;

		int ll;
		int lr;
		int r2;
		int l2;
		int cx = wi/2;
		int cy = hi/2;
		int xA[] = new int[MAX_COMPLEX];
		int yA[] = new int[MAX_COMPLEX];
		int ringRadius;
		int w;
		int h;
		int z;
		int mindim;
		double remainderX = 0;
		double remainderY = 0;
		float rdist;
        mindim=(wi < hi) ? wi : hi;

		switch (pd) {
		case Rectangle:
		ll = wi/20;
		lr = hi/20;
		r2 = wi*9/10;
		l2 = hi*9/10;
		SignalX = (int) (ll + (r2 * Math.random()));
		SignalY = (int) (lr + (l2 * Math.random()));
		break;
		case Circle: 

			int diameter = mindim*9/10; // Diameter is proportional to the smallest panel dimension 

			Point.Double sig = circlePoint();
			SignalX = (int) (sig.getX()*diameter+cx);
			SignalY = (int) (sig.getY()*diameter+cy);

			break;
		case TwoCircles: 
            // circle space circle (3x1)
			if (wi/3 < hi){
				// limiting dimension is width
				l2=(int) (wi/2.6);
			} else {
				// limiting dimension is height
				l2=(int) (hi*0.95);
			}
			diameter = l2; // Diameter is proportional to the smallest panel dimension 
			sig = circlePoint();
			int dx;

			if (Math.random()>0.5) {
				dx = -l2*3/4;
			} else {
				dx = l2*3/4;	
			}
			SignalX = (int) (sig.getX()*diameter+cx+dx);
			SignalY = (int) (sig.getY()*diameter+cy);	
			break;
		case Ring: 

			l2 = (cx < cy) ? cx : cy; // Diameter

			ll = cx - l2;
			lr = cy - l2;
			ringRadius = (int) (l2 * RING_FACTOR);

			do {
				SignalX = (int) (ll + (2*l2 * Math.random()));
				SignalY = (int) (lr + (2*l2 * Math.random()));
				rdist = (float) Math.sqrt(((cx - SignalX) *
						(cx - SignalX) +
						(cy - SignalY) *
						(cy - SignalY)));
			} while ( (rdist > l2) || (rdist < (l2 - ringRadius)) );
			break;
		case UNI: 
			w = wi/9;
			h = hi/5;
			xA[0] = w;
			yA[0] = h;
			xA[1] = w;
			yA[1] = 2*h;
			xA[2] = w;
			yA[2] = 3*h;
			xA[3] = 2*w;
			yA[3] = 3*h;
			xA[4] = 3*w;
			yA[4] = 3*h;
			xA[5] = 3*w;
			yA[5] = 2*h;
			xA[6] = 3*w;
			yA[6] = h;
			xA[7] = 4*w;
			yA[7] = h;
			xA[8] = 5*w;
			yA[8] = h;
			xA[9] = 5*w;
			yA[9] = 2*h;
			xA[10] = 5*w;
			yA[10] = 3*h;
			xA[11] = 7*w;
			yA[11] = h;
			xA[12] = 7*w;
			yA[12] = 2*h;
			xA[13] = 7*w;
			yA[13] = 3*h;

			z = (int) (14 * Math.random());
			SignalX = (int) (xA[z] + (w * Math.random()));
			SignalY = (int) (yA[z] + (h * Math.random()));

			break;
		case SmallSpirals:
			w = wi/9;
			h = hi/7;
			xA[0] = w;
			yA[0] = 5*h;
			xA[1] = w;
			yA[1] = 4*h;
			xA[2] = w;
			yA[2] = 3*h;
			xA[3] = w;
			yA[3] = 2*h;
			xA[4] = 1*w;
			yA[4] = h;
			xA[5] = 2*w;
			yA[5] = h;
			xA[6] = 3*w;
			yA[6] = h;
			xA[7] = 4*w;
			yA[7] = h;
			xA[8] = 5*w;
			yA[8] = 1*h;
			xA[9] = 5*w;
			yA[9] = 2*h;
			xA[10] = 5*w;
			yA[10] = 3*h;
			xA[11] = 3*w;
			yA[11] = 3*h;
			xA[12] = 3*w;
			yA[12] = 4*h;
			xA[13] = 3*w;
			yA[13] = 5*h;
			xA[14] = 4*w;
			yA[14] = 5*h;
			xA[15] = 5*w;
			yA[15] = 5*h;
			xA[16] = 6*w;
			yA[16] = 5*h;
			xA[17] = 7*w;
			yA[17] = 5*h;
			xA[18] = 7*w;
			yA[18] = 4*h;
			xA[19] = 7*w;
			yA[19] = 3*h;
			xA[20] = 7*w;
			yA[20] = 2*h;
			xA[21] = 7*w;
			yA[21] = 1*h;

			z = (int) (22 * Math.random());
			SignalX = (int) (xA[z] + (w * Math.random()));
			SignalY = (int) (yA[z] + (h * Math.random()));

			break;
		case LargeSpirals: 
			w = wi/13;
			h = hi/11;
			xA[0] = w;
			yA[0] = h;
			xA[1] = w;
			yA[1] = 2*h;
			xA[2] = w;
			yA[2] = 3*h;
			xA[3] = w;
			yA[3] = 4*h;
			xA[4] = 1*w;
			yA[4] = 5*h;
			xA[5] = 1*w;
			yA[5] = 6*h;
			xA[6] = 1*w;
			yA[6] = 7*h;
			xA[7] = 1*w;
			yA[7] = 8*h;
			xA[8] = 1*w;
			yA[8] = 9*h;
			xA[9] = 2*w;
			yA[9] = 1*h;
			xA[10] = 3*w;
			yA[10] = 1*h;
			xA[11] = 4*w;
			yA[11] = 1*h;
			xA[12] = 5*w;
			yA[12] = 1*h;
			xA[13] = 6*w;
			yA[13] = 1*h;
			xA[14] = 7*w;
			yA[14] = 1*h;
			xA[15] = 8*w;
			yA[15] = 1*h;
			xA[16] = 9*w;
			yA[16] = 1*h;
			xA[17] = 9*w;
			yA[17] = 2*h;
			xA[18] = 9*w;
			yA[18] = 3*h;
			xA[19] = 9*w;
			yA[19] = 4*h;
			xA[20] = 9*w;
			yA[20] = 5*h;
			xA[21] = 9*w;
			yA[21] = 6*h;
			xA[22] = 9*w;
			yA[22] = 7*h;
			xA[23] = 8*w;
			yA[23] = 7*h;
			xA[24] = 7*w;
			yA[24] = 7*h;
			xA[25] = 6*w;
			yA[25] = 7*h;
			xA[26] = 5*w;
			yA[26] = 7*h;
			xA[27] = 5*w;
			yA[27] = 6*h;
			xA[28] = 5*w;
			yA[28] = 5*h;
			xA[29] = 3*w;
			yA[29] = 3*h;
			xA[30] = 3*w;
			yA[30] = 4*h;
			xA[31] = 3*w;
			yA[31] = 5*h;
			xA[32] = 3*w;
			yA[32] = 6*h;
			xA[33] = 3*w;
			yA[33] = 7*h;
			xA[34] = 3*w;
			yA[34] = 8*h;
			xA[35] = 3*w;
			yA[35] = 9*h;
			xA[36] = 4*w;
			yA[36] = 3*h;
			xA[37] = 5*w;
			yA[37] = 3*h;
			xA[38] = 6*w;
			yA[38] = 3*h;
			xA[39] = 7*w;
			yA[39] = 3*h;
			xA[40] = 7*w;
			yA[40] = 4*h;
			xA[41] = 7*w;
			yA[41] = 5*h;
			xA[42] = 4*w;
			yA[42] = 9*h;
			xA[43] = 5*w;
			yA[43] = 9*h;
			xA[44] = 6*w;
			yA[44] = 9*h;
			xA[45] = 7*w;
			yA[45] = 9*h;
			xA[46] = 8*w;
			yA[46] = 9*h;
			xA[47] = 9*w;
			yA[47] = 9*h;
			xA[48] =10*w;
			yA[48] = 9*h;
			xA[49] =11*w;
			yA[49] = 9*h;
			xA[50] =11*w;
			yA[50] = 8*h;
			xA[51] =11*w;
			yA[51] = 7*h;
			xA[52] =11*w;
			yA[52] = 6*h;
			xA[53] =11*w;
			yA[53] = 5*h;
			xA[54] =11*w;
			yA[54] = 4*h;
			xA[55] =11*w;
			yA[55] = 3*h;
			xA[56] =11*w;
			yA[56] = 2*h;
			xA[57] =11*w;
			yA[57] = 1*h;

			z = (int) (58 * Math.random());
			SignalX = (int) (xA[z] + (w * Math.random()));
			SignalY = (int) (yA[z] + (h * Math.random()));

			break;

		case HiLoDensity: 
			w = wi/10;
			h = hi/10;
			xA[0] = 2 * w;
			yA[0] = 4 * h;
			xA[1] = 5 * w;
			yA[1] = 1 * h;

			z = (int) (2 * Math.random());
			if (z == 0) {
				SignalX = (int) (xA[z] + (w * Math.random()));
				SignalY = (int) (yA[z] + (h * Math.random()));
			} else {
				SignalX = (int) (xA[z] + (4 * w * Math.random()));
				SignalY = (int) (yA[z] + (8 * h * Math.random()));
			}

			break;

		case DiscreteMixture: 
			z = (int) (MIXTURE_SIZE * Math.random());
			SignalX = Math.round(discreteSignalsX[z]);
			SignalY = Math.round(discreteSignalsY[z]);

			break;

		case UNIT: 
			w = wi/17;
			h = hi/8;
			xA[0] = w;
			yA[0] = 2*h;
			xA[1] = w;
			yA[1] = 3*h;
			xA[2] = w;
			yA[2] = 4*h;
			xA[3] = w;
			yA[3] = 5*h;
			xA[4] = 2*w;
			yA[4] = 5*h;
			xA[5] = 3*w;
			yA[5] = 5*h;
			xA[6] = 3*w;
			yA[6] = 4*h;
			xA[7] = 3*w;
			yA[7] = 3*h;
			xA[8] = 3*w;
			yA[8] = 2*h;
			xA[9] = 4*w;
			yA[9] = 2*h;
			xA[10] = 5*w;
			yA[10] = 2*h;
			xA[11] = 6*w;
			yA[11] = 2*h;
			xA[12] = 7*w;
			yA[12] = 2*h;
			xA[13] = 7*w;
			yA[13] = 3*h;
			xA[14] = 7*w;
			yA[14] = 4*h;
			xA[15] = 7*w;
			yA[15] = 5*h;
			xA[16] = 8*w;
			yA[16] = 5*h;
			xA[17] = 9*w;
			yA[17] = 5*h;
			xA[18] = 10*w;
			yA[18] = 5*h;
			xA[19] = 11*w;
			yA[19] = 5*h;
			xA[20] = 11*w;
			yA[20] = 4*h;
			xA[21] = 11*w;
			yA[21] = 3*h;
			xA[22] = 11*w;
			yA[22] = 2*h;
			xA[23] = 14*w;
			yA[23] = 2*h;
			xA[24] = 15*w;
			yA[24] = 2*h;
			xA[25] = 15*w;
			yA[25] = 3*h;
			xA[26] = 15*w;
			yA[26] = 4*h;
			xA[27] = 15*w;
			yA[27] = 5*h;

			z = (int) (28 * Math.random());
			SignalX = (int) (xA[z] + (w * Math.random()));
			SignalY = (int) (yA[z] + (h * Math.random()));

			break;
		case MoveJump: // Moving and Jumping Rectangle
			r2 = wi/4;
			l2 = hi/4;
			ll = (int) (0.75 * (wi/2 +
					Math.IEEEremainder(0.2 * sigs,(wi))));
			lr = (int) (0.75 * (hi/2 +
					Math.IEEEremainder(0.2 * sigs,(hi))));

			SignalX = (int) (ll + (r2 * Math.random()));
			SignalY = (int) (lr + (l2 * Math.random()));
			break;
		case Move: // Moving Rectangle
			r2 = wi/4;
			l2 = hi/4;

			remainderX = Math.IEEEremainder(0.2 * sigs,(wi));
			remainderY = Math.IEEEremainder(0.2 * sigs,(hi));

			if ( (bounceX_old > 0) && (remainderX < 0) )
				bounceX = bounceX * (-1);
			if ( (bounceY_old > 0) && (remainderY < 0) )
				bounceY = bounceY * (-1);

			ll = (int) (0.75 * (wi/2 + bounceX * remainderX));
			lr = (int) (0.75 * (hi/2 + bounceY * remainderY));

			bounceX_old = remainderX;
			bounceY_old = remainderY;

			SignalX = (int) (ll + (r2 * Math.random()));
			SignalY = (int) (lr + (l2 * Math.random()));
			break;

		case Jump: // Jumping Rectangle
			r2 = wi/4;
			l2 = hi/4;

			if (Math.ceil(Math.IEEEremainder(sigs, 1000.0)) == 0) {
				jumpX = (int) ((wi - r2) * Math.random());
				jumpY = (int) ((hi - l2) * Math.random());
			}

			SignalX = (int) (jumpX + (r2 * Math.random()));
			SignalY = (int) (jumpY + (l2 * Math.random()));
			break;

		case RightMouseB: // R.Mouse Rectangle
			r2 = wi/4;
			l2 = hi/4;

			SignalX = (int) (jumpX + (r2 * Math.random()));
			SignalY = (int) (jumpY + (l2 * Math.random()));
			break;
		}
	}

	/**
	 * Do resize calculations, start the learning method.
	 * 
	 */
	@Override
	public void run() {
		int i;
		log("run(): run and learn until stopped .............................");
		if (graph.isGuiInitialized()){
			log("run(): gui already initialized ....");
		} else {
			log("run(): gui not yet initialized ....");
			//graph.prepareAlgo(algo);
		}
		while (true) {

			// Relativate Positions
			Dimension d = getSize();
			if ( (d.width != panelWidth) || (d.height != panelHeight) ) {
				//
				// panel size has changed! ==> rescaling needed
				//
				nodesMovedB = true; // for Voronoi
				NodeGNG n;
				for (i = 0 ; i < nNodes ; i++) {
					n = nodes[i];

					n.x = n.x * d.width / panelWidth;
					n.y = n.y * d.height / panelHeight;
				}

				if (pd == PD.DiscreteMixture || algo.isDiscrete()){
					rescaleDiscreteSignals(1.0*d.width / panelWidth, 1.0*d.height / panelHeight);
				}
				panelWidth = d.width;
				panelHeight = d.height;
				//initDiscreteSignals(pd);
				if ( ( nNodes == 0) && (algo.isDiscrete()) ) {
					// can this happen???
					// Generate some nodes
					int z = (int) (numDiscreteSignals * Math.random());
					int mod = 0;
					for (i = 0; i < maxNodes; i++) {
						mod = (z+i)%numDiscreteSignals;
						addNode(Math.round(discreteSignalsX[mod]),
								Math.round(discreteSignalsY[mod]));
					}
				}
				repaint();
			}

			// Calculate the new positions
			if (!stopB) {
				learn();
				nodesMovedB = true;
			}

			// update error graph
			if (errorGraphB && !stopB)
				errorGraph.graph.add(valueGraph);

			if (stopB) 
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}
			else {
				repaint(); // todo: only when needed?
			}

			if (teachB) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					break;
				}
			} else {
				try {
					Thread.sleep(tSleep);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
		log("run() end");
	}

	/**
	 * Build a minimum-heap.
	 * 
	 * @param i          The start of the intervall
	 * @param k          The end of the intervall
	 */
	protected void reheap_min(int i, int k) {
		int j = i;
		int son;

		while (2*j <= k) {
			if (2*j+1 <= k)
				if (nodes[snodes[2*j]].sqrDist < nodes[snodes[2*j+1]].sqrDist)
					son = 2*j;
				else
					son = 2*j + 1;
			else
				son = 2*j;

			if (nodes[snodes[j]].sqrDist > nodes[snodes[son]].sqrDist) {
				int exchange = snodes[j];
				snodes[j] = snodes[son];
				snodes[son] = exchange;
				j = son;
			} else
				return;
		}
	}
	/**
	 * Do the learning. An input signal is generated for the given distribution
	 *  and forwarded to the switched algorithm.
	 *  Available Algorithms (abbrev):
	 *   Growing Neural Gas (GNG),
	 *   Growing Neural Gas w. Utility (GNG-U),
	 *   Hard Competitive Learning (HCL),
	 *   Neural Gas (NG),
	 *   Neural Gas with Competitive Hebbian Learning (NGwCHL) and
	 *   Competitive Hebbian Learning (CHL).
	 *   LBG (LBG).
	 *   LBG with Utility (LBG-U).
	 *   Growing Grid (GG).
	 *   Self-Organizing Map (SOM).
	 * 
	 */
	protected synchronized void learn() {
		//
		// learning is done for stepSize steps
		//
		Dimension d = getSize();
		int curr1stIdx, curr2ndIdx;
		int i, j, k, l, m;
		int x, y;
		int numError, minUtilityNode;
		int numNb;
		int toDelete;
		float dx, dy;
		float dstSgmExp;
		float bestSqrDist, nextBestDist;
		float h_l = 0.0f;
		float l_t = 0.0f;
		float maxError, minUtility;
		NodeGNG curr1st, curr2nd, n_i, node;

		SignalX = d.width/2;
		SignalY = d.height/2;

		valueGraph = 0.0f;
        if (stopB)
        	return;
        
		// do stepSize adaption steps using random signals
		for (k = 0; k < stepSize; k++) {

			sigs++;
//			if (sigs%100==0) {
//				//
//				try {
//					Thread.sleep(delay);
//				} catch (InterruptedException e) {
//					break;
//				}
//			}

			if (!algo.isDiscrete()) { // neither LBG nor LBG-U
				//
				// generate random signal and determine winner etc.
				//
				curr1stIdx = 0;
				curr2ndIdx = 0;
				curr1st = nodes[curr1stIdx];
				curr2nd = nodes[curr2ndIdx];
				numError = 0;
				minUtilityNode = 0;
				maxError = 0.0f;
				minUtility = Float.MAX_VALUE;
				bestSqrDist = Float.MAX_VALUE;
				nextBestDist = Float.MAX_VALUE;
				toDelete = -1;

				// Get a random signal out of the selected distribution
				getSignal(pd);

				// Save the signals
				lastSignalsX[k] = SignalX;
				lastSignalsY[k] = SignalY;
				
				// Locate the nearest node (winner) and the second-nearest (runner-up)
				for (i = 0 ; i < nNodes ; i++) {
					n_i = nodes[i];
					n_i.isWinner = n_i.isSecond = n_i.hasMoved = false;

					if ((algo.isGNGType()) && (!noNewNodesGNGB) && ((sigs % lambdaGNG) == 0))
						n_i.isMostRecentlyInserted = false;

					// Mark node without neighbors (one each run is enough)
					if (n_i.numNeighbors() == 0)
						toDelete = i;

					// Calculate squared distance to input signal
					n_i.sqrDist = 
							(n_i.x - SignalX) * (n_i.x - SignalX) +
							(n_i.y - SignalY) * (n_i.y - SignalY);
					
					// Decay error and utility
					n_i.error *= decayFactor;
					n_i.utility *= decayFactorUtility;
					n_i.tau *= decayFactor;

					// Keep track of current first and second winner
					if (n_i.sqrDist <= bestSqrDist) { // changed to <= to handle strange cases with all nodes in one position
						curr2nd = curr1st;
						curr2ndIdx = curr1stIdx;
						curr1st = n_i;
						curr1stIdx = i;
						nextBestDist = bestSqrDist;
						bestSqrDist = n_i.sqrDist;
					}
					
					// Calculate node with maximal Error
					if (n_i.error > maxError) {
						maxError = n_i.error;
						numError = i;
					}
					
					// Calculate node with mininimum utility (GNG-U)
					if (n_i.utility < minUtility) {
						minUtility = n_i.utility;
						minUtilityNode = i;
					}
				}
				//
				// assertion: winner should be determined here
				//
				if (Float.isNaN(curr1st.x)){ // hack
					System.out.printf("learn(): Float.isNaN(pick.x) stopping .....\n");
					graph.stop();
					stop();
					return;
				}
				valueGraph += bestSqrDist;

				// Mark winner for teach-mode
				curr1st.isWinner = true;
				curr1st.x_old = curr1st.x;
				curr1st.y_old = curr1st.y;
				//System.out.printf("learn(): x_grid = %d  y_grid = %d \n", pick.x_grid, pick.x_grid); //hack bf
				// Adapt nodes
				
				// Trial: only adapt if signal is closer than delta
				//if (bestDist>400){
				//	break;
				//}

				//
				// do adaptation ("learning") according to current model
				//
				switch (algo) {
				//
				// Growing Neural Gas
				//
				case GNGU: //does not occur, since it is mapped to GNG
				case GNG:
					// Find second-closest node (continued)
					if (curr1stIdx == curr2ndIdx) {
						curr2ndIdx++;
						nextBestDist = Float.MAX_VALUE;
						curr2nd = nodes[curr2ndIdx];
					}
					for (i = curr1stIdx + 1 ; i < nNodes ; i++) {
						//n_i = nodes[i];
						if (nodes[i].sqrDist < nextBestDist) {
							curr2nd = nodes[i];
							curr2ndIdx = i;
							nextBestDist = nodes[i].sqrDist;
						}
					}
					// Mark second for teach-mode
					curr2nd.isSecond = true;
					curr2nd.x_old = curr2nd.x;
					curr2nd.y_old = curr2nd.y;

					
					// Adaptation of Winner:
					dx = epsilonGNG * (SignalX - curr1st.x);
					dy = epsilonGNG * (SignalY - curr1st.y);
					curr1st.adapt(dx,dy);
					
					numNb = curr1st.numNeighbors();
					
					// Adaptation of Neighbors:
					int nn;
					for (i = 0; i < numNb; i++) {
						nn = curr1st.neighbor(i);

						nodes[nn].hasMoved = true;
						curr2nd.x_old = curr2nd.x;
						curr2nd.y_old = curr2nd.y;

						dx = epsilonGNG2 * (SignalX - nodes[nn].x);
						dy = epsilonGNG2 * (SignalY - nodes[nn].y);
						nodes[nn].adapt(dx,dy);
					}

					// Accumulate square error
					curr1st.error += bestSqrDist;

					// Accumulate utility
					curr1st.utility += (nextBestDist - bestSqrDist);

					// Connect the two winning nodes
					addEdge(curr1stIdx, curr2ndIdx);

					// Calculate the age of the connected edges and delete too old edges
					ageEdgesOfNode(curr1stIdx);

					// Check inserting node and insert if necessary
					if ( (sigs % lambdaGNG) == 0 ) {
						if (!noNewNodesGNGB) {
							if (autoStopB) {
								if (nNodes >= maxNodes || (GNG_U_B && (sigs > 300000))) {
									graph.stop();
									break;
								}
							}
							insertedSoundB =
							( -1 != insertNode(numError, maximumErrorNeighbor(numError)) );
						}
					}

					// Delete Node without Neighbors (not GNG-U)
					if ((toDelete != -1) && (nNodes > 2) && !GNG_U_B )
						deleteNode(toDelete);
					// Delete Node with very low utility
					else {
						if ( maxError > minUtility * utilityGNG) {
							if (GNG_U_B && (nNodes > 2)) {
								deleteNode(minUtilityNode);
							}
						} else if (  (nNodes > 2) && (nNodes > maxNodes) ) {
							// relevant if maxnodes is lowered during the simulation
							deleteNode(minUtilityNode);
						}
					}

					break;

					//
					// Hard Competitive Learning
					//
				case HCL:
					if ((sigs >= t_max) && (variableB || autoStopB)) {
						graph.stop();
						repaint();
						break;
					}

					// Adapt picked node
					if (variableB) {
						e_t = (float)(e_i * Math.pow(e_f/e_i, sigs/t_max));
						dx = e_t * (SignalX - curr1st.x);
						dy = e_t * (SignalY - curr1st.y);
						curr1st.adapt(dx,dy);
						
					} else {
						dx = epsilon * (SignalX - curr1st.x);
						dy = epsilon * (SignalY - curr1st.y);
						curr1st.adapt(dx,dy);
					}
					break;

					//
					// Neural Gas
					//
				case NG:
					if (sigs >= t_max) {
						graph.stop();
						repaint();
						break;
					}

					// Initialize the sorted node array, if necessary
					if (nNodesChangedB) {
						for (i = 1; i <= nNodes; i++)
							snodes[i] = i-1;
						nNodesChangedB = false;
					}

					l_t = (float)(l_i * Math.pow(l_f/l_i, sigs/t_max));
					e_t = (float)(e_i * Math.pow(e_f/e_i, sigs/t_max));

					// Build a minimum heap
					for (i = nNodes/2; i > 0; i--)
						reheap_min(i, nNodes);

					{
						int decrNnodes = nNodes - 1;
						int minimum;

						// Fetch minimum, calculate new position and reheap
						for (i = nNodes; i > 0; i--) {

							minimum = snodes[1];
							snodes[1] = snodes[i];
							snodes[i] = minimum;
							n_i = nodes[minimum];

							// Mark second for teach-mode
							if (i == decrNnodes)
								nodes[minimum].isSecond = true;

							h_l = (float)(Math.exp(-(nNodes - i)/l_t));

							// Adapt nodes
							dx = e_t * h_l * (SignalX - n_i.x);
							dy = e_t * h_l * (SignalY - n_i.y);
							n_i.adapt(dx, dy);

							if ( (Math.abs(dx) < 1.0) &&
									(Math.abs(dy) < 1.0) &&
									(i < decrNnodes) )
								break;

							reheap_min(1, i-1);
						}
					}
					break;

					//
					// Neural Gas with Competitive Hebbian Learning
					//
				case NGCHL:
					if (sigs >= t_max) {
						graph.stop();
						repaint();
						break;
					}

					// Initialize the sorted node array, if necessary
					if (nNodesChangedB) {
						for (i = 1; i <= nNodes; i++)
							snodes[i] = i-1;
						nNodesChangedB = false;
					}

					l_t = (float)(l_i * Math.pow(l_f/l_i, sigs/t_max));
					e_t = (float)(e_i * Math.pow(e_f/e_i, sigs/t_max));

					// Calculate the new edge-deleting term
					MAX_EDGE_AGE = (int) (delEdge_i *
							Math.pow(delEdge_f/delEdge_i, sigs/t_max));

					// Build a minimum heap
					for (i = nNodes/2; i > 0; i--)
						reheap_min(i, nNodes);

					{
						int decrNnodes = nNodes - 1;
						int minimum;

						// Fetch minimum, calculate new position and reheap
						for (i = nNodes; i > 0; i--) {

							minimum = snodes[1];
							snodes[1] = snodes[i];
							snodes[i] = minimum;
							n_i = nodes[minimum];

							// Mark second for teach-mode
							if (i == decrNnodes) {
								nodes[minimum].isSecond = true;
								// This is the only difference between NG and NGwCHL:
								// 	- Connect the first and the second node
								addEdge(snodes[nNodes],snodes[nNodes - 1]);
								// 	- Calculate the age of the connected edges and
								//	  delete too old edges
								ageEdgesOfNode(snodes[nNodes]);      
							}

							h_l = (float)(Math.exp(-(nNodes - i)/l_t));

							// Adapt nodes
							dx = e_t * h_l * (SignalX - n_i.x);
							dy = e_t * h_l * (SignalY - n_i.y);
							n_i.adapt(dx, dy);

							if ( (Math.abs(dx) < 1.0) &&
									(Math.abs(dy) < 1.0) &&
									(i < decrNnodes) )
								break;

							reheap_min(1, i-1);
						}
					}
					break;

					//
					// Competitive Hebbian Learning
					//
				case CHL:
					if ((sigs >= 50000) && autoStopB) {
						graph.stop();
						repaint();
						break;
					}
					// Find second node (continued)
					if (curr1stIdx == curr2ndIdx) {
						curr2ndIdx++;
						nextBestDist = Float.MAX_VALUE;
						curr2nd = nodes[curr2ndIdx];
					}
					for (i = curr1stIdx + 1 ; i < nNodes ; i++) {
						n_i = nodes[i];
						if (n_i.sqrDist < nextBestDist) {
							curr2nd = n_i;
							curr2ndIdx = i;
							nextBestDist = n_i.sqrDist;
						}
					}
					// Mark second for teach-mode
					curr2nd.isSecond = true;
					curr2nd.x_old = curr2nd.x;
					curr2nd.y_old = curr2nd.y;

					// Connect the nodes
					addEdge(curr1stIdx, curr2ndIdx);

					break;

					//
					// Growing Grid
					//
				case GG:
				case GR:
					x = curr1st.x_grid;
					y = curr1st.y_grid;
					if ((x < 0)||(y<0)){
						System.out.printf("learn(): Desaster, x_grid or y_grid < 0, x=%d, y=%d, pick.x = %f pick.y = %f continuing not! .....\n",x,y,curr1st.x, curr1st.y);
						graph.stop();
						break;
					}
					// count signals (leads to density estimation by node density)
					grid[x][y].node.tau+=1.0;

					if (fineTuningB) {
						int percent;
						// tmax proportional to network size
						float tmax = gridWidth * gridHeight * l_f;
						if (algo.equals(Algo.GR)) {
							tmax = gridWidth * gridHeight * 10;
						}
						e_t = (float)(e_i * Math.pow(e_f/e_i, (sigs-sigsTmp)/tmax));
						percent = (int) (((sigs-sigsTmp)*100)/tmax);
						if (percent >= 100) {
							fineTuningS = "Fine-tuning (100%)";
							log("fine tuning at 100%, calling stop ....");
							repaint();
							graph.stop(); // stop after fine-tuning
							break;
						}
						fineTuningS = "Fine-tuning (" + String.valueOf(percent) + "%)";
					} else {
						e_t = e_i;
					}

					int dist;
					for (i = 0; i < gridWidth; i++) {
						for (j = 0; j < gridHeight; j++) {
							if (torusGGB) {
							// Manhattan Torus Distance
								float xxx = Math.min(Math.abs(x-i), Math.min(x+Math.abs(gridWidth-1-i),i+Math.abs(gridWidth-1-x)));
								dist = (int) xxx + Math.abs(y - j);
							} else {
								// Manhattan Distance
								dist = Math.abs(x - i) + Math.abs(y - j);
							}
							node = grid[i][j].node;

							dstSgmExp = (float) (Math.exp(-(dist*dist)/(2.0 * sigma * sigma)));
							dx = e_t * dstSgmExp * (SignalX - node.x);
							dy = e_t * dstSgmExp * (SignalY - node.y);
							node.adapt(dx, dy);

							if ( dstSgmExp > 0.5f )
								node.isSecond = true;
						}
					}

					// Check inserting nodes and insert if necessary
					if ( (sigs % (gridWidth * gridHeight * l_i) == 0) && (!fineTuningB) ) {
						if (!noNewNodesGGB) {
							if(enlargeGrid()) {
								insertedSoundB = true;
								fineTuningB = false;
							} else {
								insertedSoundB = false;
								fineTuningB = true;
								log("fine-tuning .....");

							}
							sigsTmp = sigs;
//							if (inserted) {
//								break;
//							}
						}
					}

					break;

					//
					// Self-Organizing Map
					//
				case SOM:
					if (sigs >= t_max){
						graph.stop(); // stop adaptation
						break;
					}

					// Adapt nodes
					x = curr1st.x_grid;
					y = curr1st.y_grid;

					e_t = (float)(e_i * Math.pow(e_f/e_i, sigs/t_max));
					sigma = (float)(sigma_i * Math.pow(sigma_f, sigs/t_max));

					for (i = 0; i < gridWidth; i++) {
						for (j = 0; j < gridHeight; j++) {
							// Distance
							if (torusSOMB) {
								// Manhattan Torus Distance
								float xxx = Math.min(Math.abs(x-i), Math.min(x+Math.abs(gridWidth-1-i),i+Math.abs(gridWidth-1-x)));
								dist = (int) xxx + Math.abs(y - j);
							} else {
								// Manhattan Distance
								dist = Math.abs(x - i) + Math.abs(y - j);
							}
							node = grid[i][j].node;

							dstSgmExp = (float) (Math.exp(-(dist*dist)/(2.0 * sigma * sigma)));
							dx = e_t * dstSgmExp * (SignalX - node.x);
							dy = e_t * dstSgmExp * (SignalY - node.y);
							node.adapt(dx, dy);

							if ( dstSgmExp > 0.5f )
								node.isSecond = true;
						}
					}

					break;
				case LBG:
				case LBGU:
					// can not happen since in this branch only non-discrete distributions
					// are handled
					break;

				} // switch
			} else 
			{
				//
				// discrete algo (LBG/LBG-U)
				//
				readyLBG_B = true;
				int noOfSignals, sig;
				int wa = 0, wb = 0;
				float bestDistLBG, nextBestDistLBG;
				float utility = 0.0f;
				float minUtilityLBG = Float.MAX_VALUE;
				float error = 0.0f;
				float maxErrorLBG = 0.0f;
				float errorAct = 0.0f;
				FPoint tmpFP;

				curr1stIdx = 0;
				
				// loop over finite set of input signals
				for (j = 0; j < numDiscreteSignals; j++) {
					curr1st = nodes[0];
					curr2nd = nodes[0];
					bestDistLBG = Float.MAX_VALUE;
					nextBestDistLBG = Float.MAX_VALUE;

					// Locate the nearest node and prepare for the second
					for (i = 0; i < nNodes; i++) {
						n_i = nodes[i];

						// Calculate distance
						n_i.sqrDist = (n_i.x - discreteSignalsX[j]) *
								(n_i.x - discreteSignalsX[j]) +
								(n_i.y - discreteSignalsY[j]) *
								(n_i.y - discreteSignalsY[j]);

						// Calculate node with best distance and prepare for second
						if (n_i.sqrDist < bestDistLBG) {
							curr2nd = curr1st;
							curr1stIdx = i;
							curr1st = n_i;
							nextBestDistLBG = bestDistLBG;
							bestDistLBG = n_i.sqrDist;
						}
					}

					// Store distance to the nearest codebook vector (LBG-U)
					discreteSignalsD1[j] = bestDistLBG;
					errorAct += bestDistLBG;

					// Add signal index to the winning codebook vector
					curr1st.addSignal(j);

					// Find node with second best distance
					if (curr1st == curr2nd)
						nextBestDistLBG = Float.MAX_VALUE;

					for (i = curr1stIdx + 1; i < nNodes; i++) {
						n_i = nodes[i];

						if (n_i.sqrDist < nextBestDistLBG) {
							curr2nd = n_i;
							nextBestDistLBG = n_i.sqrDist;
						}
					}

					// Store distance to the second nearest codebook vector (LBG-U)
					discreteSignalsD2[j] = nextBestDistLBG;


					valueGraph += bestDistLBG;
				}

				minUtilityLBG = Float.MAX_VALUE;
				maxErrorLBG = 0.0f;
				// Adapt selected nodes
				for (l = 0; l < nNodes; l++) {
					n_i = nodes[l];
					tmpFP = new FPoint(n_i.x, n_i.y);
					utility = 0.0f;
					error = 0.0f;

					noOfSignals = n_i.numSignals();

					if (noOfSignals > 0) {
						for (m = 0; m < noOfSignals; m++) {
							sig = n_i.removeSignal();
							dx = discreteSignalsX[sig];
							dy = discreteSignalsY[sig];
							n_i.adapt(dx, dy);

							// calculate utility
							utility += (discreteSignalsD2[sig] -
									discreteSignalsD1[sig]);
							// calculate error
							error += discreteSignalsD1[sig];

						}
						n_i.x /= (noOfSignals + 1.0f);
						n_i.y /= (noOfSignals + 1.0f);

						// nodes moved?
						if (!tmpFP.equal(n_i.x, n_i.y)) {
							n_i.hasMoved = true;
							readyLBG_B = false;
						} else {
							n_i.hasMoved = false;
						}

						// determine minimum utility
						if (utility < minUtilityLBG) {
							wa = l;
							minUtilityLBG = utility;
						}
						// determine maximum error
						if (error > maxErrorLBG) {
							wb = l;
							maxErrorLBG = error;
						}
					}
				}

				if (LBG_U_B) {
					if (DEBUG)
						log("Act: " + errorAct
								+ ", Best: " + errorBestLBG_U);
					if (readyLBG_B && (errorAct < errorBestLBG_U) ) {
						// Save old positions
						for (i = 0; i < nNodes; i++) {
							Cbest[i] = new FPoint(nodes[i].x,
									nodes[i].y);
						}
						readyLBG_B = false;
						errorBestLBG_U = errorAct;
						// move node from wa to wb with a small offset (LBG-U)
						// IMPORTANT: This works only for image data space!!!
						dx = nodes[wb].x - nodes[wa].x;
						dy = nodes[wb].y + 1 - nodes[wa].y;
						nodes[wa].adapt(dx, dy);
						//nodes[wa].x = nodes[wb].x;
						//nodes[wa].y = nodes[wb].y + 1;
					} else if (readyLBG_B && (errorAct > errorBestLBG_U) ) {
						// Restore old positions
						for (i = 0; i < nNodes; i++) {
							nodes[i].x = Cbest[i].x;
							nodes[i].y = Cbest[i].y;
						}
					}
				}
				if (readyLBG_B && autoStopB) {
					graph.stop();
					repaint();
				}

			} // if (algo.isDiscrete())
			if (stopB==true)
				break;
			
		} // loop over stepSize

//		if (errorGraphB) {
//			// Calculate mean square error
//			if (algo.isDiscrete())
//				valueGraph = valueGraph / numDiscreteSignals;
//
//			valueGraph = (float) Math.sqrt( valueGraph / (stepSize) );
//		}
	}



	/**
	 * The mouse-selected node.
	 */
	protected NodeGNG pick;
	/**
	 * The flag for mouse-selected node.
	 */
	protected boolean pickfixed;
	Image offscreen;
	Dimension offscreensize;
	Graphics g;

	/**
	 * The color of input signals
	 */
	protected final Color signalsColor = Color.red;
	/**
	 * The color of the 1-D SOM torus.
	 */
	protected final Color torusColor = Color.cyan;
	/**
	 * The color of SOM polgons.
	 */
	protected final Color somColor = Color.yellow;
	/**
	 * The color of the unused nodes (usage=true).
	 */
	protected final Color unusedColor = Color.black;
	/**
	 * The color of the winner node (teach-mode).
	 */
	protected final Color winnerColor = Color.red;
	/**
	 * The color of the second node (teach-mode).
	 */
	protected final Color secondColor = Color.orange;
	/**
	 * The color of the last moved nodes (teach-mode).
	 */
	protected final Color movedColor = Color.yellow;
	/**
	 * The color of the last inserted node (teach-mode).
	 */
	protected final Color insertedColor = Color.blue;
	/**
	 * The color of the shown signal (teach-mode).
	 */
	protected final Color signalColor = Color.black;
	/**
	 * The color of the mouse-selected node.
	 */
	protected final Color selectColor = Color.pink;
	/**
	 * The color of the edges.
	 */
	protected final Color edgeColor = Color.black;
	/**
	 * The color of the Voronoi diagram.
	 */
	protected final Color voronoiColor = Color.red;
	/**
	 * The color of the Delaunay diagram.
	 */
	protected final Color delaunayColor = new Color(205,155,29);
	/**
	 * The color of the nodes.
	 */
	protected final Color nodeColor = Color.green;
	/**
	 * The color for debug information.
	 */
	protected final Color debugColor = Color.gray;
	/**
	 * The color of the distribution.
	 */
	protected final Color distribColor = new Color(203, 205, 252);
	/**
	 * The color of the low density distribution.
	 */
	protected final Color lowDistribColor = new Color(203, 205, 252);
	/**
	 * The color of the high density distribution.
	 */
	protected final Color highDistribColor = new Color(152, 161, 250);

	public Color mixColor(Color a, Color b, float aa){
		log(String.valueOf(aa));
		Color x;
		float bb=(1.0f-aa)/2;
		aa = aa/2;
		x = new Color(
				(int) Math.min(a.getRed()*aa+b.getRed()*bb,255), 
				(int) Math.min((int)a.getGreen()*aa+b.getGreen()*bb,255),
				(int) Math.min((int)a.getBlue()*aa+b.getBlue()*bb,255));
		return x;
	}
	/**
	 * Paint a node.
	 * 
	 * @param g            The graphic context
	 * @param n            The node
	 */
	public void paintNode(Graphics g, NodeGNG n) {
		int RADIUS = 10;
		Color col = nodeColor;

		if (teachB && (!algo.isDiscrete()) ) {
			if (n.isWinner) {
				RADIUS += 5;
				col = winnerColor;
			} else if (n.isSecond) {
				RADIUS += 3;
				col = secondColor;
			} 
//			else if (n.hasMoved) {
//				RADIUS += 2;
//				col = movedColor;
//			}
		}

		if (algo.isSOMType() && usageB){
			if (n.tau < 1.0){
				//Color c = nodeColor;
				//col = new Color(c.getRed(),c.getGreen(),c.getBlue(),(int)(100*n.tau));
				//col = mixColor(nodeColor,Color.black,n.tau);
				col=unusedColor;
			}
		}
		if (n.isMostRecentlyInserted)  {
			RADIUS += 2;
			col = insertedColor;
		}

		if ( (algo.isDiscrete()) && (!n.hasMoved) ) {
			RADIUS += 4;
			col = movedColor;
		}

		g.setColor(col);
		if (mapSpaceGGB && (algo==Algo.GG||algo==Algo.GR) || mapSpaceSOMB && algo==Algo.SOM) {
			g.fillOval((int)(gx2x((int)n.x_grid) - (RADIUS/2)), (int)(gy2y((int)n.y_grid) - (RADIUS/2)), RADIUS, RADIUS);
		} else {
			g.fillOval((int)n.x - (RADIUS/2), (int)n.y - (RADIUS/2), RADIUS, RADIUS);
		}
		g.setColor(Color.black);
		if (mapSpaceGGB && (algo==Algo.GG||algo==Algo.GR) || mapSpaceSOMB && algo==Algo.SOM) {
			g.drawOval((int)gx2x(n.x_grid) - (RADIUS/2), (int)gy2y(n.y_grid) - (RADIUS/2), RADIUS, RADIUS);
		} else {
			g.drawOval((int)n.x - (RADIUS/2), (int)n.y - (RADIUS/2), RADIUS, RADIUS);
		}

	}

// repaint --> update() ---> paintComponent()
	/**
	 * Update the drawing area.
	 * 
	 * @param g          The graphic context
	 */
	static int paintCouter=0; 
	static int prevSigs=0; 
	
	// helper function to compute mapspace coordinates for SOM-like networks
	public int gx2x(int gx){
		Dimension d = getSize();
		return (int) (gx *d.width*0.9f/(gridWidth-1)+0.05*d.width);
	}
	public int gy2y(int gy){
		Dimension d = getSize();
		return (int) (gy *d.height*0.9f/(gridHeight-1)+0.05*d.height);
	}
	public synchronized void paintComponent(Graphics g) {

		//log("paintComponent() CGNG " + String.valueOf(paintCouter)+" signals:"+String.valueOf(sigs) + "delta-sig:"+String.valueOf(sigs - prevSigs));
		paintCouter+=1;
		prevSigs = sigs;
		Dimension d = getSize();
		int ll,lr,r2,l2;
		int xA[] = new int[MAX_COMPLEX];
		int yA[] = new int[MAX_COMPLEX];
		int w;
		int h;
		int mindim;
		int ringRadius;
		int i, x, y;
        mindim = (d.width < d.height) ? d.width : d.height;

		if (whiteB)
			g.setColor(Color.white);
		else
			g.setColor(getBackground());

		g.fillRect(0, 0, d.width, d.height);

		// recompute Delaunay/Voronoi
		if ((delaunayB || voronoiB) && nodesMovedB) {
			nlines = 0;
			voro.computeVoronoi();// TODO: analyze
		}
		nodesMovedB = false;
		//
		// draw probability distribution .....
		// changes need to be reflected in the distribution itself
		//

		// Set color for distribution
		if (!algo.isDiscrete()) 
			g.setColor(distribColor);

		switch (pd) {
		case Rectangle: // Rectangle
			ll = d.width/20;
			lr = d.height/20;
			r2 = d.width*9/10;
			l2 = d.height*9/10;
			g.fillRect(ll, lr, r2, l2);
			break;
		case Circle: // Circle

			l2 = mindim*9/10; // Diameter is proportional to the smallest panel dimension 

			ll = d.width/2 -l2/2;
			lr = d.height/2 -l2/2;

			g.fillOval(ll, lr, l2, l2);
			break;
		case TwoCircles: // Circle
            // circle space circle (3x1)
			if (d.width/3 < d.height){
				// limiting dimension is width
				l2=(int) (d.width/2.6);
			} else {
				// limiting dimension is height
				l2=(int) (d.height*0.95);
			}
			
			ll = d.width/2 -l2*5/4;
			lr = d.height/2 -l2/2;
			g.fillOval(ll, lr, l2, l2);

			ll = d.width/2 +l2/4;
			g.fillOval(ll, lr, l2, l2);

			break;
		case Ring: // Ring
			int cx = d.width/2; // horizontal center of panel
			int cy = d.height/2;// vertical center of panel
			l2 = (cx < cy) ? cx : cy; // Diameter

			ll = cx - l2;
			lr = cy - l2;
			ringRadius = (int) (l2 * RING_FACTOR);

			g.fillOval(ll, lr, 2*l2, 2*l2);
			if (whiteB)
				g.setColor(Color.white);
			else
				g.setColor(getBackground());
			g.fillOval(ll + ringRadius,
					lr+ringRadius,
					2*l2-2*ringRadius,
					2*l2-2*ringRadius);
			break;
		case UNI: // Complex (1)
			w = d.width/9;
			h = d.height/5;
			xA[0] = w;
			yA[0] = h;
			xA[1] = w;
			yA[1] = 2*h;
			xA[2] = w;
			yA[2] = 3*h;
			xA[3] = 2*w;
			yA[3] = 3*h;
			xA[4] = 3*w;
			yA[4] = 3*h;
			xA[5] = 3*w;
			yA[5] = 2*h;
			xA[6] = 3*w;
			yA[6] = h;
			xA[7] = 4*w;
			yA[7] = h;
			xA[8] = 5*w;
			yA[8] = h;
			xA[9] = 5*w;
			yA[9] = 2*h;
			xA[10] = 5*w;
			yA[10] = 3*h;
			xA[11] = 7*w;
			yA[11] = h;
			xA[12] = 7*w;
			yA[12] = 2*h;
			xA[13] = 7*w;
			yA[13] = 3*h;

			for (i = 0; i < 14; i++)
				g.fillRect(xA[i], yA[i], w, h);
			break;
		case SmallSpirals: // Complex (2)
			w = d.width/9;
			h = d.height/7;
			xA[0] = w;
			yA[0] = 5*h;
			xA[1] = w;
			yA[1] = 4*h;
			xA[2] = w;
			yA[2] = 3*h;
			xA[3] = w;
			yA[3] = 2*h;
			xA[4] = 1*w;
			yA[4] = h;
			xA[5] = 2*w;
			yA[5] = h;
			xA[6] = 3*w;
			yA[6] = h;
			xA[7] = 4*w;
			yA[7] = h;
			xA[8] = 5*w;
			yA[8] = 1*h;
			xA[9] = 5*w;
			yA[9] = 2*h;
			xA[10] = 5*w;
			yA[10] = 3*h;
			xA[11] = 3*w;
			yA[11] = 3*h;
			xA[12] = 3*w;
			yA[12] = 4*h;
			xA[13] = 3*w;
			yA[13] = 5*h;
			xA[14] = 4*w;
			yA[14] = 5*h;
			xA[15] = 5*w;
			yA[15] = 5*h;
			xA[16] = 6*w;
			yA[16] = 5*h;
			xA[17] = 7*w;
			yA[17] = 5*h;
			xA[18] = 7*w;
			yA[18] = 4*h;
			xA[19] = 7*w;
			yA[19] = 3*h;
			xA[20] = 7*w;
			yA[20] = 2*h;
			xA[21] = 7*w;
			yA[21] = 1*h;

			for (i = 0; i < 22; i++)
				g.fillRect(xA[i], yA[i], w, h);
			break;
		case LargeSpirals: // Complex (3)
			w = d.width/13;
			h = d.height/11;
			xA[0] = w;
			yA[0] = h;
			xA[1] = w;
			yA[1] = 2*h;
			xA[2] = w;
			yA[2] = 3*h;
			xA[3] = w;
			yA[3] = 4*h;
			xA[4] = 1*w;
			yA[4] = 5*h;
			xA[5] = 1*w;
			yA[5] = 6*h;
			xA[6] = 1*w;
			yA[6] = 7*h;
			xA[7] = 1*w;
			yA[7] = 8*h;
			xA[8] = 1*w;
			yA[8] = 9*h;
			xA[9] = 2*w;
			yA[9] = 1*h;
			xA[10] = 3*w;
			yA[10] = 1*h;
			xA[11] = 4*w;
			yA[11] = 1*h;
			xA[12] = 5*w;
			yA[12] = 1*h;
			xA[13] = 6*w;
			yA[13] = 1*h;
			xA[14] = 7*w;
			yA[14] = 1*h;
			xA[15] = 8*w;
			yA[15] = 1*h;
			xA[16] = 9*w;
			yA[16] = 1*h;
			xA[17] = 9*w;
			yA[17] = 2*h;
			xA[18] = 9*w;
			yA[18] = 3*h;
			xA[19] = 9*w;
			yA[19] = 4*h;
			xA[20] = 9*w;
			yA[20] = 5*h;
			xA[21] = 9*w;
			yA[21] = 6*h;
			xA[22] = 9*w;
			yA[22] = 7*h;
			xA[23] = 8*w;
			yA[23] = 7*h;
			xA[24] = 7*w;
			yA[24] = 7*h;
			xA[25] = 6*w;
			yA[25] = 7*h;
			xA[26] = 5*w;
			yA[26] = 7*h;
			xA[27] = 5*w;
			yA[27] = 6*h;
			xA[28] = 5*w;
			yA[28] = 5*h;
			xA[29] = 3*w;
			yA[29] = 3*h;
			xA[30] = 3*w;
			yA[30] = 4*h;
			xA[31] = 3*w;
			yA[31] = 5*h;
			xA[32] = 3*w;
			yA[32] = 6*h;
			xA[33] = 3*w;
			yA[33] = 7*h;
			xA[34] = 3*w;
			yA[34] = 8*h;
			xA[35] = 3*w;
			yA[35] = 9*h;
			xA[36] = 4*w;
			yA[36] = 3*h;
			xA[37] = 5*w;
			yA[37] = 3*h;
			xA[38] = 6*w;
			yA[38] = 3*h;
			xA[39] = 7*w;
			yA[39] = 3*h;
			xA[40] = 7*w;
			yA[40] = 4*h;
			xA[41] = 7*w;
			yA[41] = 5*h;
			xA[42] = 4*w;
			yA[42] = 9*h;
			xA[43] = 5*w;
			yA[43] = 9*h;
			xA[44] = 6*w;
			yA[44] = 9*h;
			xA[45] = 7*w;
			yA[45] = 9*h;
			xA[46] = 8*w;
			yA[46] = 9*h;
			xA[47] = 9*w;
			yA[47] = 9*h;
			xA[48] =10*w;
			yA[48] = 9*h;
			xA[49] =11*w;
			yA[49] = 9*h;
			xA[50] =11*w;
			yA[50] = 8*h;
			xA[51] =11*w;
			yA[51] = 7*h;
			xA[52] =11*w;
			yA[52] = 6*h;
			xA[53] =11*w;
			yA[53] = 5*h;
			xA[54] =11*w;
			yA[54] = 4*h;
			xA[55] =11*w;
			yA[55] = 3*h;
			xA[56] =11*w;
			yA[56] = 2*h;
			xA[57] =11*w;
			yA[57] = 1*h;

			for (i = 0; i < 58; i++)
				g.fillRect(xA[i], yA[i], w, h);
			break;

		case HiLoDensity: // HiLo-Density
			w = d.width/10;
			h = d.height/10;
			xA[0] = 2 * w;
			yA[0] = 4 * h;
			xA[1] = 5 * w;
			yA[1] = 1 * h;

			if (!algo.isDiscrete())
				g.setColor(highDistribColor);
			g.fillRect(xA[0], yA[0], w, h);
			if (!algo.isDiscrete())
				g.setColor(lowDistribColor);
			g.fillRect(xA[1], yA[1], 4 * w, 8 * h);
			break;

		case DiscreteMixture: // discrete
			//int RADIUS = 2;
			for (i = 0; i < numDiscreteSignals; i++) {
				x = Math.round(discreteSignalsX[i]);
				y = Math.round(discreteSignalsY[i]);

				g.setColor(distribColor);
				g.fillOval(x - 1, y - 1, 2, 2);
				g.setColor(Color.black);
				g.drawOval(x - 1, y - 1, 2, 2);
			}
			break;

		case UNIT: // Complex (4)
			w = d.width/17;
			h = d.height/8;
			xA[0] = w;
			yA[0] = 2*h;
			xA[1] = w;
			yA[1] = 3*h;
			xA[2] = w;
			yA[2] = 4*h;
			xA[3] = w;
			yA[3] = 5*h;
			xA[4] = 2*w;
			yA[4] = 5*h;
			xA[5] = 3*w;
			yA[5] = 5*h;
			xA[6] = 3*w;
			yA[6] = 4*h;
			xA[7] = 3*w;
			yA[7] = 3*h;
			xA[8] = 3*w;
			yA[8] = 2*h;
			xA[9] = 4*w;
			yA[9] = 2*h;
			xA[10] = 5*w;
			yA[10] = 2*h;
			xA[11] = 6*w;
			yA[11] = 2*h;
			xA[12] = 7*w;
			yA[12] = 2*h;
			xA[13] = 7*w;
			yA[13] = 3*h;
			xA[14] = 7*w;
			yA[14] = 4*h;
			xA[15] = 7*w;
			yA[15] = 5*h;
			xA[16] = 8*w;
			yA[16] = 5*h;
			xA[17] = 9*w;
			yA[17] = 5*h;
			xA[18] = 10*w;
			yA[18] = 5*h;
			xA[19] = 11*w;
			yA[19] = 5*h;
			xA[20] = 11*w;
			yA[20] = 4*h;
			xA[21] = 11*w;
			yA[21] = 3*h;
			xA[22] = 11*w;
			yA[22] = 2*h;
			xA[23] = 14*w;
			yA[23] = 2*h;
			xA[24] = 15*w;
			yA[24] = 2*h;
			xA[25] = 15*w;
			yA[25] = 3*h;
			xA[26] = 15*w;
			yA[26] = 4*h;
			xA[27] = 15*w;
			yA[27] = 5*h;

			for (i = 0; i < 28; i++)
				g.fillRect(xA[i], yA[i], w, h);
			break;
		case MoveJump: // Moving and Jumping Rectangle
			r2 = d.width/4;
			l2 = d.height/4;
			ll = (int) (0.75 * (d.width/2 +
					Math.IEEEremainder(0.2 * sigs,(d.width))));
			lr = (int) (0.75 * (d.height/2 +
					Math.IEEEremainder(0.2 * sigs,(d.height))));

			g.fillRect(ll, lr, r2, l2);
			break;
		case Move: // Moving Rectangle
			r2 = d.width/4;
			l2 = d.height/4;
			ll = (int) (0.75 * (d.width/2 +
					bounceX * Math.IEEEremainder(0.2 * sigs,
							(d.width))));
			lr = (int) (0.75 * (d.height/2 +
					bounceY * Math.IEEEremainder(0.2 * sigs,
							(d.height))));

			g.fillRect(ll, lr, r2, l2);
			break;

		case Jump: // Jumping Rectangle
			r2 = d.width/4;
			l2 = d.height/4;

			g.fillRect(jumpX, jumpY, r2, l2);
			break;

		case RightMouseB: // R.Mouse Rectangle
			r2 = d.width/4;
			l2 = d.height/4;

			g.fillRect(jumpX, jumpY, r2, l2);
			break;
		}      

		// Draw the edges
		if (edgesB) {
			int x1, y1, x2, y2;
			EdgeGNG e;
			for (i = 0 ; i < nEdges ; i++) {
				e = edges[i];
				if (mapSpaceGGB && (algo==Algo.GG||algo==Algo.GR) || mapSpaceSOMB && algo==Algo.SOM) {
					x1 = gx2x(nodes[e.from].x_grid);
					y1 = gy2y(nodes[e.from].y_grid);
					x2 = gx2x(nodes[e.to].x_grid);
					y2 = gy2y(nodes[e.to].y_grid);
				} else {
					x1 = (int)nodes[e.from].x;
					y1 = (int)nodes[e.from].y;
					x2 = (int)nodes[e.to].x;
					y2 = (int)nodes[e.to].y;
				}
				g.setColor(edgeColor);
				g.drawLine(x1, y1, x2, y2);
			}
		} else if (algo.isSOMType()) {
			g.setColor(edgeColor);
			// draw the outer edges, i.e. where for *both* endpoints holds:
			// gridx=0 or gridy=0 or gridx = width-1 or grid y= height-1
			for (i = 0; i < gridWidth-1; i++) {
				NodeGNG n1 = grid[i][0].node;
				NodeGNG n2 = grid[i+1][0].node;
				g.drawLine((int)n1.x, (int)n1.y, (int)n2.x, (int)n2.y);
				n1 = grid[i][gridHeight-1].node;
				n2 = grid[i+1][gridHeight-1].node;
				g.drawLine((int)n1.x, (int)n1.y, (int)n2.x, (int)n2.y);
			}
			for (i = 0; i < gridHeight-1; i++) {
				NodeGNG n1 = grid[0][i].node;
				NodeGNG n2 = grid[0][i+1].node;
				g.drawLine((int)n1.x, (int)n1.y, (int)n2.x, (int)n2.y);
				n1 = grid[gridWidth-1][i].node;
				n2 = grid[gridWidth-1][i+1].node;
				g.drawLine((int)n1.x, (int)n1.y, (int)n2.x, (int)n2.y);
			}
		}
		
		// draw the filled polygons of fixed-dimensional networks (SOM, GG. eventually GCS) 
		if (algo.isSOMType()){
			Color c = somColor;
			Color cT = new Color(c.getRed(),c.getGreen(),c.getBlue(),80);
			//Color dd = Color.black;
			//Color dT = new Color(dd.getRed(),dd.getGreen(),dd.getBlue(),80);
			int j;
			int xPoints[] = new int[5];
			int yPoints[] = new int[5];
			// draw the rectangles (as closed polygons)
			if (gridHeight>1){
				for (i = 0; i < gridWidth-1; i++) {
					for (j = 0; j < gridHeight-1; j++) {
						// draw polygon i,j;i+1,j;i+1,j+1;i,j+1;i,j
						if (mapSpaceGGB && (algo==Algo.GG||algo==Algo.GR) || mapSpaceSOMB && algo==Algo.SOM) {
							xPoints[0]=gx2x(i);
							xPoints[1]=gx2x(i+1);
							xPoints[2]=gx2x(i+1);
							xPoints[3]=gx2x(i);
							xPoints[4]=gx2x(i);

							yPoints[0]=gy2y(j);
							yPoints[1]=gy2y(j);
							yPoints[2]=gy2y(j+1);
							yPoints[3]=gy2y(j+1);
							yPoints[4]=gy2y(j);

						} else {
							xPoints[0]=(int)grid[i][j].node.x;
							xPoints[1]=(int)grid[i+1][j].node.x;
							xPoints[2]=(int)grid[i+1][j+1].node.x;
							xPoints[3]=(int)grid[i][j+1].node.x;
							xPoints[4]=(int)grid[i][j].node.x;

							yPoints[0]=(int)grid[i][j].node.y;
							yPoints[1]=(int)grid[i+1][j].node.y;
							yPoints[2]=(int)grid[i+1][j+1].node.y;
							yPoints[3]=(int)grid[i][j+1].node.y;
							yPoints[4]=(int)grid[i][j].node.y;
						}
						g.setColor(cT);
						g.fillPolygon(xPoints,yPoints,5);
						//g.setColor(dT);
						//g.drawPolygon(xPoints,yPoints,5);
					}
				}
			} else if ((algo==Algo.SOM && torusSOMB) || ((algo==Algo.GG||algo==Algo.GR) && torusGGB)){
				c = torusColor;
				cT = new Color(c.getRed(),c.getGreen(),c.getBlue(),80);
				g.setColor(cT);
				Polygon p = new Polygon();
				for (i = 0; i < gridWidth-1; i++) {
					p.addPoint((int)grid[i][0].node.x,(int)grid[i][0].node.y);
				}
				g.fillPolygon(p);
			}
		}

		// Draw the Voronoi or Delaunay diagram
		if (voronoiB || delaunayB) {
			LineGNG l;
			for (i = 0; i < nlines; i++) {
				l = lines[i];
				if (vd[i])
					// voronoi
					g.setColor(voronoiColor);
				else
					// delaunay
					g.setColor(delaunayColor);
				g.drawLine(l.x1, l.y1, l.x2, l.y2);
			}
		}

		// Draw the nodes
		if (nodesB)
			for (i = 0; i < nNodes; i++)
				paintNode(g, nodes[i]);
		
		// draw the tau values
		if ((algo==Algo.GG||algo==Algo.GR) && tauB){
			g.setColor(Color.black);
			int j;
			for (i = 0; i < gridWidth; i++) {
				for (j = 0; j < gridHeight; j++) {
					GridNodeGNG xx = grid[i][j];
					if (i==0 && j==0){
						if (mapSpaceGGB) {
							g.drawString("x"+String.valueOf((int)xx.node.tau)+"x", gx2x(xx.node.x_grid), gy2y(xx.node.y_grid));
							
						} else {
							g.drawString("x"+String.valueOf((int)xx.node.tau)+"x", (int)xx.node.x, (int)xx.node.y);
						}
					} else {
						if (mapSpaceGGB) {
							g.drawString(String.valueOf((int)xx.node.tau),  gx2x(xx.node.x_grid), gy2y(xx.node.y_grid));
						} else {
							g.drawString(String.valueOf((int)xx.node.tau), (int)xx.node.x, (int)xx.node.y);
							
						}
					}
				}
			}
			
		}
		
		// Draw the motion traces
		if (tracesB && !algo.isLBGType()){ // traces not working for LBG for some reason
			g.setColor(Color.black);
			for (i = 0; i < nNodes; i++) {
				Vector<Float> tr = nodes[i].getTrace();
				if (tr.size()<4)
					break;
				int x1,y1,x2,y2;
				x1=(int) Math.round(tr.get(0));
				y1=(int) Math.round(tr.get(1));
				for (int j=2;j<tr.size();j+=2){
					x2=(int) Math.round(tr.get(j));
					y2=(int) Math.round(tr.get(j+1));
					g.drawLine(x1, y1, x2, y2);
					x1=x2;
					y1=y2;
				}
			}
		}

		
        //Draw teach mode info	
		if ( teachB ) {
			int r = 6;
			int offset_x = 12;
			int offset2_x = offset_x + 5;
			int offset_y = d.height/4;

			if (algo.isDiscrete()) {
				// Draw legend
				g.setColor(Color.black);
				g.drawString("Legend:", 	2, offset_y); offset_y += 15;

				g.setColor(movedColor);
				g.fillOval(offset_x - r, offset_y - r, r, r);
				g.setColor(Color.black);
				g.drawString("Not moved", offset2_x,
						offset_y); offset_y += 15;
			} else {
				g.setColor(signalColor);
				g.fillOval(SignalX - r/2, SignalY - r/2, r, r);

				// Draw legend
				g.setColor(Color.black);
				g.drawString("Legend:", 	2, offset_y); offset_y += 15;

				g.setColor(winnerColor);
				g.fillOval(offset_x - r, offset_y - r, r, r);
				g.setColor(Color.black);
				g.drawString("Winner", offset2_x, offset_y); offset_y += 15;

				if (algo != Algo.HCL) {
					g.setColor(secondColor);
					g.fillOval(offset_x - r, offset_y - r, r, r);
					g.setColor(Color.black);
					g.drawString("Second", offset2_x, offset_y); offset_y += 15;
				}

				if (algo == Algo.GNG) {
					g.setColor(movedColor);
					g.fillOval(offset_x - r, offset_y - r, r, r);
					g.setColor(Color.black);
					g.drawString("Neighbors", offset2_x, offset_y); offset_y += 15;

					g.setColor(insertedColor);
					g.fillOval(offset_x - r, offset_y - r, r, r);
					g.setColor(Color.black);
					g.drawString("Last inserted", offset2_x, offset_y); offset_y += 15;
				}

				g.setColor(signalColor);
				g.fillOval(offset_x - r, offset_y - r, r, r);
				g.setColor(Color.black);
				g.drawString("Signal", offset2_x, offset_y); offset_y += 15;
			}
		}

		g.setColor(Color.black);
		g.drawString("Signals: "+String.valueOf(sigs), 10, 10);
		if (maxNodes == 1)
			g.drawString(String.valueOf(nNodes) + " node",
					10, d.height - 10);
		else
			g.drawString("Nodes: "+String.valueOf(nNodes),
					10, d.height - 10);

		g.drawString("DemoGNG "+DGNG_VERSION, d.width - 130, 10);
		if ( readyLBG_B && (algo.isLBGType()) ) {
			g.drawString("READY!", d.width-50, d.height-10);
		}
		if ( fineTuningB && (algo==Algo.GG||algo==Algo.GR) )
			g.drawString(fineTuningS, d.width-130, d.height-10);

		//
		// draw signals
		//
		if ( signalsB && (!algo.isDiscrete()) ) {
			for (i = 0; i < stepSize; i++) {
				x = (int) (lastSignalsX[i]);
				y = (int) (lastSignalsY[i]);

				//g.setColor(Color.green);
				//g.fillOval(x - 1, y - 1, 2, 2);
				g.setColor(signalsColor);
				g.fillOval(x - 2, y - 2, 3, 3);
				g.drawOval(x - 2, y - 2, 3, 3);
			}
		}


		//
		// play insert sound
		//
		if ( insertedSoundB && soundB ) {
			//log("beep!!!");
			//log(graph.getCodeBase().toString());
			graph.play(graph.getCodeBase(), "audio/drip.au");
			insertedSoundB = false;
		}

		if (algo.isDiscrete()) {
			for (i = 0; i < numDiscreteSignals; i++) {
				x = Math.round(discreteSignalsX[i]);
				y = Math.round(discreteSignalsY[i]);

				g.setColor(distribColor);
				g.fillOval(x - 1, y - 1, 2, 2);
				g.setColor(Color.black);
				g.drawOval(x - 1, y - 1, 2, 2);
			}
		}

		// Show error graph or not
		if (errorGraph != null) {
			if (errorGraphB)
				errorGraph.setVisible(true);
			else
				errorGraph.setVisible(false);
		}

	}

	@Override
		public void mousePressed(MouseEvent evt) {
		int x = evt.getX();
		int y = evt.getY();
		if ((evt.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK) {
			if (pd == PD.RightMouseB)  {
				Dimension d = getSize();

				jumpX = x;
				jumpY = y;
				// Draw distribution only inside the visible region
				if (jumpX > (0.75 * d.width))
					jumpX = (int) (0.75 * d.width);

				if (jumpY > (0.75 * d.height))
					jumpY = (int) (0.75 * d.height);

				repaint();
				return;// true;
			} else return;
		} else if ((evt.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {

			float bestDist = Float.MAX_VALUE;
			NodeGNG n;
			float dist;

			for (int i = 0 ; i < nNodes ; i++) {
				n = nodes[i];
				dist = (n.x - x) * (n.x - x) + (n.y - y) * (n.y - y);
				if (dist <= bestDist) {
					pick = n;
					bestDist = dist;
				}
			}
			pickfixed = pick.isMouseSelected;
			pick.isMouseSelected = true;
			pick.x = x;
			pick.y = y;

			if (algo.isDiscrete())
				pick.hasMoved = true;

			nodesMovedB = true;
			repaint();
		}
	}

	@Override
	public void mouseDragged(MouseEvent evt) {
		int x = evt.getX();
		int y = evt.getY();
		Dimension d = getSize();
        
        if ((evt.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK) {
        	if (pd == PD.RightMouseB)  {
        		jumpX = x;
        		jumpY = y;

        		// Draw distribution only inside the visible region
        		if (jumpX < 0)
        			jumpX = 0;
        		else if (jumpX > (0.75 * d.width))
        			jumpX = (int) (0.75 * d.width);

        		if (jumpY < 0)
        			jumpY = 0;
        		else if (jumpY > (0.75 * d.height))
        			jumpY = (int) (0.75 * d.height);

        		repaint();
        	} 
        } else if ((evt.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {

        	pick.x = x;
        	pick.y = y;

        	// Draw nodes only inside the visible region
        	if (pick.x < 0)
        		pick.x = 0;
        	else if (pick.x > d.width)
        		pick.x = d.width;

        	if (pick.y < 0)
        		pick.y = 0;
        	else if (pick.y > d.height)
        		pick.y = d.height;

        	nodesMovedB = true;
        	repaint();
        }
	}

	@Override
	public void mouseReleased(MouseEvent evt) {
		Dimension d = getSize();
		int x = evt.getX();
		int y = evt.getY();

		if ((evt.getButton()==java.awt.event.MouseEvent.BUTTON3) && (pd == PD.RightMouseB) ) {
			jumpX = x;
			jumpY = y;

			// Draw distribution only inside the visible region
			if (jumpX < 0)
				jumpX = 0;
			else if (jumpX > (0.75 * d.width))
				jumpX = (int) (0.75 * d.width);

			if (jumpY < 0)
				jumpY = 0;
			else if (jumpY > (0.75 * d.height))
				jumpY = (int) (0.75 * d.height);

			repaint();
		} else if (evt.getButton()==java.awt.event.MouseEvent.BUTTON1) {

			pick.x = x;
			pick.y = y;
			pick.isMouseSelected = pickfixed;

			// Draw nodes only inside the visible region
			if (pick.x < 0)
				pick.x = 0;
			else if (pick.x > d.width)
				pick.x = d.width;

			if (pick.y < 0)
				pick.y = 0;
			else if (pick.y > d.height)
				pick.y = d.height;

			pick = null;

			nodesMovedB = true;
			repaint();
		}
	}

	public void start() {
		stopB = false;
		log("start() ......");
		relaxer = new Thread(this);
		relaxer.start();

		if ( errorGraphB  && (errorGraph != null) )
			errorGraph.setVisible(true);
	}

	public void stop(){
		log("stop() ......");
		stopB =true;
		if (relaxer != null) {
			relaxer.interrupt(); //!!!!
			relaxer = null;
			log("set relaxer to 0");
		}
		if ( errorGraphB  && (errorGraph != null) )
			errorGraph.setVisible(false);
	}

	public void destroy() {
		if ( errorGraphB  && (errorGraph != null) ) {
			errorGraph.dispose();
			errorGraph = null;
		}
	}

	public void graphClose() {
		if ( errorGraphB  && (errorGraph != null) ) {
			errorGraph.dispose();
			errorGraph = null;
		}
	}
//	@Override
//	public void mouseDragged(MouseEvent e) {
//		// TODO Auto-generated method stub
//		//log("mouseDragged");
//		
//	}
	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		//log("mouseMoved");
		
	}
	
	// mouse listener
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
//	@Override
//	public void mousePressed(MouseEvent e) {
//		// TODO Auto-generated method stub
//		log("mouseMoved");
//		log(e.toString());
//	
//	}
//	@Override
//	public void mouseReleased(MouseEvent e) {
//		// TODO Auto-generated method stub
//		
//	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub
		log("....................... resized!!!!!!!! ..............");
		nodesMovedB=true;
		//wasResized();	
	}
	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		log("......................... SH O W N ..................");
		
	}
	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
	    JSlider source = (JSlider)e.getSource();
	    if (true/*!source.getValueIsAdjusting()*/) {
	        delay = (50-(int)source.getValue())*10;
	    }
	        

		
	}

}
