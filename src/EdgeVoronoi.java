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

/**
 * A class representing an edge in the Voronoi diagram
 *
 */
class EdgeVoronoi {
  /**
   * One part of line equation.
   * (equation: ax + by = c)
   */
  public float a;
  /**
   * One part of line equation.
   * (equation: ax + by = c)
   */
  public float b;
  /**
   * One part of line equation.
   * (equation: ax + by = c)
   */
  public float c;
  /**
   * The number of the edge
   */
  public int edgenbr = 0;

  /**
   * The sites of the edge
   */
  public SiteVoronoi ep[] = new SiteVoronoi[2];
  /**
   * The next region
   */
  public SiteVoronoi reg[] = new SiteVoronoi[2];

}
