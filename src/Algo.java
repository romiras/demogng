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
 * enum for all learning methods
 */
public enum Algo {
	LBG("LBG (Linde,Buzo,Gray)","LBG"),
	LBGU("LBG-U (Fritzke)","LBGU"),
	HCL("Hard Competitive Learning","HCL"), 
	CHL("Competitive Hebbian Learning  (Martinetz)","CHL"),
	NG("Neural Gas  (Martinetz)","NG"), 
	NGCHL("Neural Gas with CHL  (Martinetz)","NGwCHL"),
	GNG("Growing Neural Gas (Fritzke)","GNG"),
	GNGU("Growing Neural Gas with Utility (Fritzke)","GNGU"),
	SOM("The Self-Organizing Map  (Kohonen)","SOM"),
	GG("Growing Grid  (Fritzke)","GG"),
	GR("Growing Ring  (Fritzke)","GR");
	private String name;
	public String getName() {
		return name;
	}
	public String getMnemo() {
		return mnemo;
	}
	public boolean isLBGType(){
		if (this==LBG | this == LBGU)
			return true;
		else
			return false;
	
	}
	public boolean isGNGType(){
		if (this==GNG | this == GNGU)
			return true;
		else
			return false;
	}
	public boolean isSOMType(){
		if (this==SOM | this == GG | this == GR)
			return true;
		else
			return false;
	}
	public boolean isDiscrete(){
		if (ordinal()==0 || ordinal() ==1)
			return true;
		else
			return false;
	}
	private String mnemo;
	private Algo(String name,String mnemo){
		this.name=name;
		this.mnemo=mnemo;
	}
}

