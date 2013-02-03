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
import java.awt.Dimension;


/**
 * Compute Voronoi diagram.
 * A sweepline algorithm is implemented (Steven Fortune, 1987).
 * It computes the Voronoi diagram/Delaunay triangulation of n sites
 * in time O(n log n) and space usage O(n).
 * Input: nodes[], Output: lines[] (global).
 * 
 */
public class Voronoi {
	ComputeGNG cgng;
	Voronoi() {
		;
	}
	
	Voronoi(ComputeGNG cgng) {
		this.cgng = cgng;
		vsites = new SiteVoronoi[cgng.MAX_NODES + 1];
	}
	/**
	 * This array of sites is sorted by y-coordinate (2nd y-coordinate).
	 * vsites[1] is the index of the bottom node.
	 */
	protected SiteVoronoi vsites[];// = new SiteVoronoi[cgng.MAX_NODES + 1];
	// Vars for Voronoi diagram (start).
	int xmin, ymin, xmax, ymax;
	int deltax, deltay;
	int siteidx, nsites, sqrt_nsites;
	int nvertices, nvedges;
	int PQcount, PQmin;
	SiteVoronoi bottomsite;
	final int LE = 0;
	final int RE = 1;
	final int DELETED = -2;
	ListGNG list, pq;
	boolean debug = true;
	HalfEdgeVoronoi ELleftend, ELrightend;
	float pxmin, pymin, pxmax, pymax;
	// Vars for Voronoi diagram (end).
	protected void sortSites(int n) {
		SiteVoronoi exchange;
		int i;

		// Initialize the sorted site array
		for (i = 1; i <= n; i++) {
			NodeGNG nd = cgng.nodes[i-1];
			SiteVoronoi sv = new SiteVoronoi();
			sv.coord.x = nd.x;
			sv.coord.y = nd.y;
			sv.sitenbr = i-1;
			sv.refcnt = 0;
			vsites[i] = sv;
		}
		cgng.nNodesChangedB = false;

		// Build a maximum heap
		for (i = n/2; i > 0; i--)
			reheapVoronoi(i, n);

		// Switch elements 1 and i then reheap
		for (i = n; i > 1; i--) {
			exchange = vsites[1];
			vsites[1] = vsites[i];
			vsites[i] = exchange;
			reheapVoronoi(1, i-1);
		}
	}

	/**
	 * Build a maximum-heap. The result is in the <TT> vsite</TT>-array.
	 * 
	 * @param i          The start of the intervall
	 * @param k          The end of the intervall
	 * @see ComputeGNG#vsites
	 * @see ComputeGNG#sortSites
	 */
	protected void reheapVoronoi(int i, int k) {
		int j = i;
		int son;

		while (2*j <= k) {
			if (2*j+1 <= k)
				if ( (vsites[2*j].coord.y > vsites[2*j+1].coord.y) ||
						(vsites[2*j].coord.y == vsites[2*j+1].coord.y &&
						vsites[2*j].coord.x > vsites[2*j+1].coord.x) )
					son = 2*j;
				else
					son = 2*j + 1;
			else
				son = 2*j;

			if ( (vsites[j].coord.y < vsites[son].coord.y) ||
					(vsites[j].coord.y == vsites[son].coord.y &&
					vsites[j].coord.x < vsites[son].coord.x) ) {
				SiteVoronoi exchange = vsites[j];
				vsites[j] = vsites[son];
				vsites[son] = exchange;
				j = son;
			} else
				return;
		}
	}

	/**
	 * Compute Voronoi diagram.
	 * A sweepline algorithm is implemented (Steven Fortune, 1987).
	 * It computes the Voronoi diagram/Delaunay triangulation of n sites
	 * in time O(n log n) and space usage O(n).
	 * Input: nodes[], Output: lines[] (global).
	 * 
	 */
	public boolean computeVoronoi() {
		Dimension d = cgng.getSize();
		int i;
		xmin = 0;
		ymin = 0;
		xmax = deltax = d.width;
		ymax = deltay = d.height;
		siteidx = 0;
		nsites = cgng.nNodes;
		nvertices = 0;
		nvedges = 0;
		sqrt_nsites = (int) Math.sqrt(nsites + 4);
		PQcount = 0;
		PQmin = 0;

		// Copy nodes[] to vsites[] and sort them
		sortSites(cgng.nNodes);

		if ( (cgng.nNodes == 0) ||
				( (cgng.nNodes != cgng.maxNodes) && (cgng.algo != Algo.GNG) && (cgng.algo != Algo.GG) ) )
			return true;

		xmin = (int) vsites[1].coord.x; 
		xmax = (int) vsites[1].coord.x;
		for(i = 2; i <= nsites; i++) {
			if (vsites[i].coord.x < xmin)
				xmin = (int) vsites[i].coord.x;
			if (vsites[i].coord.x > xmax)
				xmax = (int) vsites[i].coord.x;
		}
		ymin = (int) vsites[1].coord.y;
		ymax = (int) vsites[nsites].coord.y;

		doVoronoi();
		return false;
	}

	/**
	 * Compute Voronoi diagram (2).
	 * A sweepline algorithm is implemented (Steven Fortune, 1987).
	 * Input: nodes[], Output: lines[] (global).
	 * 
	 * @see ComputeGNG#computeVoronoi
	 */
	public void doVoronoi() {
		SiteVoronoi newsite, bot, top, temp, p, v;
		FPoint newintstar = new FPoint();
		int pm;
		HalfEdgeVoronoi lbnd, rbnd, llbnd, rrbnd, bisector;
		EdgeVoronoi e;

		pq = new ListGNG();
		bottomsite = nextsite();
		ELinitialize();
		newsite = nextsite();

		while(true) {
			if (!pq.empty())
				newintstar = pq.PQ_min();

			if ( (newsite != null) &&
					( pq.empty() ||
							(newsite.coord.y < newintstar.y) ||
							( (newsite.coord.y == newintstar.y) &&
									(newsite.coord.x < newintstar.x) )
							) ) {
				lbnd = ELleftbnd(newsite.coord);
				rbnd = lbnd.ELright;
				bot = rightreg(lbnd);
				e = bisect(bot, newsite);
				bisector = new HalfEdgeVoronoi(e, LE);
				ELinsert(lbnd, bisector);
				if ( (p = intersect(lbnd, bisector)) != null ) {
					PQdelete(lbnd);
					PQinsert(lbnd, p, dist(p,newsite));
				}
				lbnd = bisector;
				bisector = new HalfEdgeVoronoi(e, RE);
				ELinsert(lbnd, bisector);
				if ( (p = intersect(bisector, rbnd)) != null ) {
					PQinsert(bisector, p, dist(p,newsite));	
				}

				newsite = nextsite();

			} else if ( !pq.empty() ) {
				// intersection is smallest
				PQcount--;
				lbnd = pq.PQextractmin();
				llbnd = lbnd.ELleft;
				rbnd = lbnd.ELright;
				rrbnd = rbnd.ELright;
				bot = leftreg(lbnd);
				top = rightreg(rbnd);
				v = lbnd.vertex;
				makevertex(v);
				endpoint(lbnd.ELedge, lbnd.ELpm, v);
				endpoint(rbnd.ELedge, rbnd.ELpm, v);
				ELdelete(lbnd); 
				PQdelete(rbnd);
				ELdelete(rbnd); 
				pm = LE;
				if (bot.coord.y > top.coord.y) {
					temp = bot;
					bot = top;
					top = temp;
					pm = RE;
				}
				e = bisect(bot, top);
				bisector = new HalfEdgeVoronoi(e, pm);
				ELinsert(llbnd, bisector);
				endpoint(e, RE-pm, v);
				deref(v);
				if ( (p = intersect(llbnd, bisector)) != null ) {
					PQdelete(llbnd);
					PQinsert(llbnd, p, dist(p, bot) );
				}
				if ( (p = intersect(bisector, rrbnd)) != null )
					PQinsert(bisector, p, dist(p, bot) );
			} else
				break;
		}

		if (cgng.voronoiB) {
			for(lbnd = ELleftend.ELright;
					lbnd != ELrightend;
					lbnd = lbnd.ELright) {
				e = lbnd.ELedge;
				out_ep(e);
			}
		}
	}

	public void out_bisector(EdgeVoronoi e) {
		line(e.reg[0].coord.x, e.reg[0].coord.y, 
				e.reg[1].coord.x, e.reg[1].coord.y, false);

	}

	public void out_ep(EdgeVoronoi e) {
		SiteVoronoi s1, s2;
		float x1, x2, y1, y2;
		Dimension dim = cgng.getSize();

		pxmin = 0.0f;
		pymin = 0.0f;
		pxmax = dim.width;
		pymax = dim.height;

		if ( (e.a == 1.0f) && (e.b >= 0.0f) ) {
			s1 = e.ep[1];
			s2 = e.ep[0];
		} else {
			s1 = e.ep[0];
			s2 = e.ep[1];
		}

		if (e.a == 1.0) {
			y1 = pymin;
			if ( (s1 != null) && (s1.coord.y > pymin) )
				y1 = s1.coord.y;
			if (y1 > pymax)
				return;
			x1 = e.c - e.b * y1;
			y2 = pymax;
			if ( (s2 != null) && (s2.coord.y < pymax) ) 
				y2 = s2.coord.y;
			if (y2 < pymin)
				return;
			x2 = e.c - e.b * y2;
			if ( (x1 > pxmax & x2 > pxmax) | (x1 < pxmin & x2 < pxmin) )
				return;
			if (x1 > pxmax) {
				x1 = pxmax;
				y1 = (e.c - x1)/e.b;
			}
			if (x1 < pxmin) {
				x1 = pxmin;
				y1 = (e.c - x1)/e.b;
			}
			if (x2 > pxmax) {
				x2 = pxmax;
				y2 = (e.c - x2)/e.b;
			}
			if (x2 < pxmin) {
				x2 = pxmin;
				y2 = (e.c - x2)/e.b;
			}
		} else {
			x1 = pxmin;
			if ( (s1 != null) && (s1.coord.x > pxmin) ) 
				x1 = s1.coord.x;
			if (x1 > pxmax)
				return;
			y1 = e.c - e.a * x1;
			x2 = pxmax;
			if ( (s2 != null) && (s2.coord.x < pxmax) )
				x2 = s2.coord.x;
			if (x2 < pxmin)
				return;
			y2 = e.c - e.a * x2;
			if ( (y1 > pymax & y2 > pymax) | ( y1 < pymin & y2 < pymin) )
				return;
			if (y1 > pymax) {
				y1 = pymax;
				x1 = (e.c - y1)/e.a;
			}
			if(y1 < pymin) {
				y1 = pymin;
				x1 = (e.c - y1)/e.a;
			}
			if(y2 > pymax) {
				y2 = pymax;
				x2 = (e.c - y2)/e.a;
			}
			if(y2 < pymin) {
				y2 = pymin;
				x2 = (e.c - y2)/e.a;
			}
		}
		line(x1, y1, x2, y2, true);
	}

	public void line(float x1, float y1, float x2, float y2, boolean vdB) {
		LineGNG l = new LineGNG((int) x1, (int) y1, (int) x2, (int) y2);
		cgng.lines[cgng.nlines] = l;
		cgng.vd[cgng.nlines] = vdB;
		cgng.nlines++;
	}

	public void PQinsert(HalfEdgeVoronoi he, SiteVoronoi v, float offset) {
		he.vertex = v;
		v.refcnt++;
		he.ystar = v.coord.y + offset;

		pq.PQinsert(he);
		PQcount++;
	}

	public void PQdelete(HalfEdgeVoronoi he) {
		if(he.vertex != null) {
			pq.PQdelete(he);
			PQcount--;
			deref(he.vertex);
			he.vertex = null;
		}
	}

	public float dist(SiteVoronoi s, SiteVoronoi t)
	{
		float dx,dy;
		dx = s.coord.x - t.coord.x;
		dy = s.coord.y - t.coord.y;
		return( (float) Math.sqrt(dx*dx + dy*dy) );
	}

	public SiteVoronoi intersect(HalfEdgeVoronoi el1, HalfEdgeVoronoi el2) {
		EdgeVoronoi e1, e2, e;
		HalfEdgeVoronoi el;
		float d, xint, yint;
		boolean right_of_site;
		SiteVoronoi v;

		e1 = el1.ELedge;
		e2 = el2.ELedge;
		if ( (e1 == null) || (e2 == null) ) 
			return(null);
		if (e1.reg[1] == e2.reg[1])
			return(null);

		d = e1.a * e2.b - e1.b * e2.a;
		if ( (-1.0e-10 < d) && (d < 1.0e-10) )
			return(null);

		xint = (e1.c * e2.b - e2.c * e1.b)/d;
		yint = (e2.c * e1.a - e1.c * e2.a)/d;

		if ( (e1.reg[1].coord.y < e2.reg[1].coord.y) ||
				( (e1.reg[1].coord.y == e2.reg[1].coord.y) &&
						(e1.reg[1].coord.x < e2.reg[1].coord.x) ) ) {
			el = el1;
			e = e1;
		} else {
			el = el2;
			e = e2;
		}
		right_of_site = (xint >= e.reg[1].coord.x);
		if ( (right_of_site && el.ELpm == LE) ||
				(!right_of_site && el.ELpm == RE) )
			return(null);

		v = new SiteVoronoi();
		v.refcnt = 0;
		v.coord.x = xint;
		v.coord.y = yint;
		return(v);
	}

	public void ELinsert(HalfEdgeVoronoi lb, HalfEdgeVoronoi henew) {
		henew.ELleft = lb;
		henew.ELright = lb.ELright;
		(lb.ELright).ELleft = henew;
		lb.ELright = henew;
	}

	public void deref(SiteVoronoi v) {
		v.refcnt--;
		if (v.refcnt == 0 )
			v = null;
	}

	public EdgeVoronoi bisect(SiteVoronoi s1, SiteVoronoi s2) {
		float dx, dy, adx, ady;
		EdgeVoronoi newedge;

		newedge = new EdgeVoronoi();

		newedge.reg[0] = s1;
		newedge.reg[1] = s2;
		s1.refcnt++; 
		s2.refcnt++;
		newedge.ep[0] = null;
		newedge.ep[1] = null;

		dx = s2.coord.x - s1.coord.x;
		dy = s2.coord.y - s1.coord.y;
		adx = (dx > 0) ? dx : -dx;
		ady = (dy > 0) ? dy : -dy;
		newedge.c = s1.coord.x * dx + s1.coord.y * dy + (dx*dx + dy*dy) * 0.5f;
		if (adx > ady) {
			newedge.a = 1.0f;
			newedge.b = dy/dx;
			newedge.c /= dx;
		} else {
			newedge.b = 1.0f;
			newedge.a = dx/dy;
			newedge.c /= dy;
		}

		newedge.edgenbr = nvedges;
		if (cgng.delaunayB)
			out_bisector(newedge);
		nvedges++;
		return(newedge);
	}

	public void endpoint(EdgeVoronoi e, int lr, SiteVoronoi s) {
		e.ep[lr] = s;
		s.refcnt++;;
		if (e.ep[RE-lr] == null)
			return;
		if (cgng.voronoiB)
			out_ep(e);
		deref(e.reg[LE]);
		deref(e.reg[RE]);
		e = null;
	}

	public void makevertex(SiteVoronoi v) {
		v.sitenbr = nvertices;
		nvertices++;
	}

	public void ELdelete(HalfEdgeVoronoi he) {
		(he.ELleft).ELright = he.ELright;
		(he.ELright).ELleft = he.ELleft;
		he.ELedge = null;
	}

	public SiteVoronoi rightreg(HalfEdgeVoronoi he) {
		if(he.ELedge == null)
			return(bottomsite);
		return( he.ELpm == LE ? 
				he.ELedge.reg[RE] :
					he.ELedge.reg[LE] );
	}

	public SiteVoronoi leftreg(HalfEdgeVoronoi he) {
		if (he.ELedge == null)
			return(bottomsite);
		return( he.ELpm == LE ? 
				he.ELedge.reg[LE] :
					he.ELedge.reg[RE] );
	}

	public void ELinitialize() {
		list = new ListGNG();
		ELleftend = new HalfEdgeVoronoi(null, 0);
		ELrightend = new HalfEdgeVoronoi(null, 0);
		ELleftend.ELleft = null;
		ELleftend.ELright = ELrightend;
		ELrightend.ELleft = ELleftend;
		ELrightend.ELright = null;
		list.insert(ELleftend, list.head);
		list.insert(ELrightend, list.last());
	}

	public HalfEdgeVoronoi ELleftbnd(FPoint p) {
		HalfEdgeVoronoi he;
		he = (list.first()).elem;
		// Now search linear list of halfedges for the correct one
		if ( he == ELleftend  || (he != ELrightend && right_of(he,p)) ) {
			do {
				he = he.ELright;
			} while ( (he != ELrightend) && right_of(he,p) );
			he = he.ELleft;
		} else { 
			do {
				he = he.ELleft;
			} while ( he != ELleftend && !right_of(he,p) );
		}
		return(he);
	}

	// returns true if p is to right of halfedge e
	public boolean right_of(HalfEdgeVoronoi el, FPoint p) {
		EdgeVoronoi e;
		SiteVoronoi topsite;
		boolean right_of_site, above, fast;
		float dxp, dyp, dxs, t1, t2, t3, yl;

		e = el.ELedge;
		topsite = e.reg[1];
		right_of_site = p.x > topsite.coord.x;
		if(right_of_site && (el.ELpm == LE) )
			return(true);
		if(!right_of_site && (el.ELpm == RE) )
			return (false);

		if (e.a == 1.0) {
			dyp = p.y - topsite.coord.y;
			dxp = p.x - topsite.coord.x;
			fast = false;
			if ( (!right_of_site & e.b < 0.0) | (right_of_site & e.b >= 0.0) ) {
				above = (dyp >= e.b * dxp);
				fast = above;
			}
			else {
				above = (p.x + p.y * e.b) > e.c;
				if(e.b < 0.0)
					above = !above;
				if (!above)
					fast = true;
			}
			if (!fast) {
				dxs = topsite.coord.x - (e.reg[0]).coord.x;
				above = (e.b * (dxp*dxp - dyp*dyp)) <
						(dxs * dyp * (1.0 + 2.0 * dxp/dxs + e.b * e.b));
				if(e.b < 0.0)
					above = !above;
			}
		} else {  // e.b == 1.0
				yl = e.c - e.a * p.x;
				t1 = p.y - yl;
				t2 = p.x - topsite.coord.x;
				t3 = yl - topsite.coord.y;
				above = t1*t1 > t2*t2 + t3*t3;
		}
		return (el.ELpm == LE ? above : !above);
	}

	public SiteVoronoi nextsite() {
		siteidx++;
		if (siteidx > nsites)
			return(null);
		return(vsites[siteidx]);
	}
	/**
	 * Sort the array of nodes. The result is in the <TT> vsite</TT>-array.
	 *  The implemented sorting algorithm is heapsort.
	 * 
	 * @param n          The number of nodes to be sorted
	 * @see ComputeGNG#vsites
	 * @see ComputeGNG#reheapVoronoi
	 */


}
