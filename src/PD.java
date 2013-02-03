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
 * @author B. Fritzke
 * enum for all probability distributions
 */
public enum PD {
	Rectangle("Rectangle"),
	Circle("Circle"),
	TwoCircles("Two Circles"),
	Ring("Ring"),
	UNI("UNI"),
	SmallSpirals("Small Spirals"),
	LargeSpirals("Large Spirals"),
	HiLoDensity("HiLo Density"),
	DiscreteMixture("DiscreteMixture"),
	UNIT("UNIT"),
	MoveJump("Move & Jump"),
	Move("Move"),
	Jump("Jump"),
	RightMouseB("Right Mouse-Btn");
	private String name;
	public String getName() {
		return name;
	}
	private PD(String name){
		this.name=name;
	}

}
