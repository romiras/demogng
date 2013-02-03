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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
/**
 * A class for drawing the GUI and interacting with the user.
 *
 */
@SuppressWarnings("serial")
public class DemoGNG extends JApplet {
	public DemoGNG(){
		super();
	}
	static int shown=0;
	private boolean needReset = false;

	void log(String prefix, String txt) {
		System.out.println(timeStamp()+" D: "+prefix+txt);
	}
	void log(String txt) {
		log("####### ",txt);
	}
	void syslog(String txt) {
		log("####### ", txt);
	}
	class MyPanel extends JPanel {

		MyPanel() {
			super();
			//setBackground(new Color(50+(int)( Math.random()*200),50+(int)( Math.random()*200),50+(int)( Math.random()*200)));
		}
	}
	final static private SimpleDateFormat format
	= new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");
	public String timeStamp() {
	    long lDateTime = new Date().getTime();
		String str;
		str = format.format(new java.util.Date())+"."+String.format("%03d",lDateTime%1000);
		return str;
	}
	
	// Slioder listener
	class ALSlider implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			// TODO Auto-generated method stub
		    JSlider source = (JSlider)e.getSource();
		    if (true/*!source.getValueIsAdjusting()*/) {
		        compute.delay = (50-(int)source.getValue())*10;
		    }
		}
	}
	// CheckBox Listener
	class ALCheckBox implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt)
	    {
			//
			// A MyCheckBox event?
			//
	        Object ob = evt.getSource();
			if (ob instanceof MyCheckBox) {
				MyCheckBox cb = (MyCheckBox) ob;
				Boolean val = cb.isSelected();


				if (cb.getText().equals(SIGNALS)) {
					compute.signalsB = val;
				} else if (cb.equals(noNewNodesGNG_cb)) {
					compute.noNewNodesGNGB = val;
				}  if (cb.equals(noNewNodesGG_cb)) {
					compute.noNewNodesGGB = val;
				}  if (cb.equals(mapSpaceGG_cb)) {
					compute.mapSpaceGGB = val;
				}  if (cb.equals(mapSpaceSOM_cb)) {
					compute.mapSpaceSOMB = val;
				}  if (cb.equals(tau_cb)) {
					compute.tauB = val;
				}  if (cb.equals(torusGG_cb)) {
					compute.torusGGB = val;
				}  if (cb.equals(torusSOM_cb)) {
					compute.torusSOMB = val;
				}  if (cb.equals(usage_cb)) {
					compute.usageB = val;
				}  else if (cb.getText().equals(UTILITY_GNG)) {
					compute.GNG_U_B = val;
				} else if (cb.getText().equals(LBG_U)) {
					compute.LBG_U_B = val;
				} else if (cb.getText().equals(SOUND)) {
					compute.soundB = val;
				} else if (cb.getText().equals(AUTOSTOP)) {
					compute.autoStopB = val;
				} else if (cb.getText().equals(RNDINIT)) {
					compute.rndInitB = val;
				} else if (cb.getText().equals(NODES)) {
					compute.nodesB = val;
				} else if (cb.getText().equals(TRACES)) {
					compute.tracesB = val;
				} else if (cb.getText().equals(EDGES)) {
					compute.edgesB = val;
				} else if (cb.getText().equals(ERRORGRAPH)) {
					compute.errorGraphB = val;
				} else if (cb.getText().equals(VORONOI)) {
					compute.voronoiB = val;
					compute.nodesMovedB = true;
				} else if (cb.getText().equals(DELAUNAY)) {
					compute.delaunayB = val;
					compute.nodesMovedB = true;
				} else if (cb.getText().equals(TEACH)) {
					compute.teachB = val;
				} else if (cb.getText().equals(VARIABLE)) {
					compute.variableB = val;
					if (compute.variableB) {
						epsilonHCL_lbl.setEnabled(false);
						epsilonHCL_choice.setEnabled(false);

						epsiloniHCL_lbl.setEnabled(true);
						epsilonfHCL_lbl.setEnabled(true);
						//tmaxHCL_lbl.setEnabled(true);

						epsiloniHCL_choice.setEnabled(true);
						epsilonfHCL_choice.setEnabled(true);
						//tmaxHCL_choice.setEnabled(true);
					} else {
						epsilonHCL_lbl.setEnabled(true);
						epsilonHCL_choice.setEnabled(true);

						epsiloniHCL_lbl.setEnabled(false);
						epsilonfHCL_lbl.setEnabled(false);
						//tmaxHCL_lbl.setEnabled(false);

						epsiloniHCL_choice.setEnabled(false);
						epsilonfHCL_choice.setEnabled(false);
						//tmaxHCL_choice.setEnabled(false);
					}
				}
				compute.repaint(); // added after removing the general repaint()
			}
	    }
	}
	
	// Button Listener
	class ALButton implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt)
	    {
			//
			// A Button event?
			//
			if (evt.getSource() instanceof JButton) {
	
				// Demo
				if (random_b.equals(evt.getSource())) {
					stop();
					randomSim();
				}
				// Start
				if (start_b.equals(evt.getSource())) {
					start();
				}
				// Stop
				else if (stop_b.equals(evt.getSource())) {
					stop();
				}
				// Reset
				else if (reset_b.equals(evt.getSource())) {
					reset();
				}
				// Restart
				else if (restart_b.equals(evt.getSource())) {
					reset();
					start();
				}
			}
	    }
	}
	public void prepareAlgo(Algo aNew){
		log("PREPARE ALGO");
		if (!isGuiInitialized()){
			log("prepareAlgo: gui not yet initialized ....");
			return;
		}
		Dimension d = compute.getSize();
		int i;
		// Reset values
		compute.sigs = 0;
		compute.nNodes = 0;
		compute.nEdges = 0;
		compute.nodesMovedB = true;
		if (aNew.equals(Algo.LBGU)) {
			// use LBG card for LBG-U as well
			((CardLayout)cards.getLayout()).show(cards,Algo.LBG.getName());				
		} else if (aNew.equals(Algo.GNGU)) {
			// use GNG card for GNG-U as well
			((CardLayout)cards.getLayout()).show(cards,Algo.GNG.getName());				
		} else if (aNew.equals(Algo.GR)) {
			// use GG card for GR as well
			((CardLayout)cards.getLayout()).show(cards,Algo.GG.getName());				
		} else{
			// use specific card
			((CardLayout)cards.getLayout()).show(cards,aNew.getName());
		}
		maxNodes_lbl.setEnabled(true);
		maxNodes_choice.setEnabled(true);
		maxNodes_lbl.setVisible(true);
		maxNodes_choice.setVisible(true);

		// set algo to use
		compute.algo = aNew; // is altered below in only some cases
		if (aNew.isDiscrete()){
			compute.stepSize = stepSize_Ai[1]; //1
			stepSize_choice.setSelectedIndex(1);

			//int maxIndex = indexWithContent(maxNodes_Ai,4);
			setChoice(maxNodes_Ai,maxNodes_choice,5);
			//compute.maxNodes = maxNodes_Ai[maxIndex]; //5
			//maxNodes_choice.setSelectedIndex(maxIndex);
		} else {
			compute.stepSize = stepSize_Ai[0]; // 50
			stepSize_choice.setSelectedIndex(0);

			if (aNew.equals(Algo.GR)) {
				setChoice(maxNodes_Ai,maxNodes_choice,500);
			} else {
				setChoice(maxNodes_Ai,maxNodes_choice,100);
			}
			//compute.maxNodes = maxNodes_Ai[0];  //100
			//maxNodes_choice.setSelectedIndex(0);
		}
		if (!aNew.equals(Algo.GR)) {
			nodes_cb.setSelected(true);
			compute.nodesB = true;
			torusGG_cb.setSelected(false);
			compute.torusGGB = false;
			//setChoice
		}
		// Set specific algorithm parameters
		if (aNew.equals(Algo.GNG) || aNew.equals(Algo.GNGU)) {
			if(aNew.equals(Algo.GNGU)){
				compute.GNG_U_B=true;
			} else {
				compute.GNG_U_B=false;						
			}
			gng_u_cb.setSelected(compute.GNG_U_B);
			// set algo to GNG in both cases!
			compute.algo = Algo.GNG;

			// Set default values
			compute.addNode(d);
			setChoice(epsilonGNG1_Af,epsilonGNG1_choice,0.05f);
			setChoice(epsilonGNG2_Af,epsilonGNG2_choice,6.0E-4f);
			setChoice(alphaGNG_Af,alphaGNG_choice,0.5f);
			setChoice(utilityGNG_Af,utilityGNG_choice,3.0f);
			setChoice(maxEdgeAgeGNG_Ai,delEdgeGNG_choice,88);
			setChoice(lambdaGNG_Ai,newNodeGNG_choice,600);
			setChoice(betaGNG_Af,betaGNG_choice,5.0E-4f);

			if (compute.maxNodes != 1)
				compute.addNode(d);
			//maxNodes_lbl.setText("max. nodes:");
			maxNodes_lbl.setText("# nodes:");
		} else if (aNew.equals(Algo.HCL)) {
			// Set default values
			setChoice(epsilonHCL_Af,epsilonHCL_choice,0.1f);
			setChoice(epsiloniHCL_Af,epsiloniHCL_choice,0.1f);
			setChoice(epsilonfHCL_Af,epsilonfHCL_choice,0.005f);
			setChoice(tmaxHCL_Af,tmaxHCL_choice,30000);
			
			maxNodes_lbl.setText("# nodes:");

			// Generate some nodes
			for (i = 0; i < compute.maxNodes; i++)
				compute.addNode(d);
		} else if (aNew.equals(Algo.NG)) {
			// Set default values
			maxNodes_lbl.setText("# nodes:");
			setChoice(lambdaiNG_Af,lambdaiNG_choice,30f);
			setChoice(lambdafNG_Af,lambdafNG_choice,0.01f);
			setChoice(epsiloniNG_Af,epsiloniNG_choice,0.3f);
			setChoice(epsilonfNG_Af,epsilonfNG_choice,0.05f);
			setChoice(tmaxNG_Af,tmaxNG_choice,40000f);
//			compute.l_i = lambdaiNG_Af[0];
//			compute.l_f = lambdafNG_Af[0];
//			compute.e_i = epsiloniNG_Af[0];
//			compute.e_f = epsilonfNG_Af[0];
//			compute.t_max = tmaxNG_Af[0];
//
//			lambdaiNG_choice.setSelectedIndex(0);
//			lambdafNG_choice.setSelectedIndex(0);
//			epsiloniNG_choice.setSelectedIndex(0);
//			epsilonfNG_choice.setSelectedIndex(0);
//			tmaxNG_choice.setSelectedIndex(0);

			// Generate some nodes
			for (i = 0; i < compute.maxNodes; i++)
				compute.addNode(d);
		} else if (aNew.equals(Algo.NGCHL)) {
			// Set default values
			setChoice(lambdaiNGCHL_Af,lambdaiNGCHL_choice,30);
			setChoice(lambdafNGCHL_Af,lambdafNGCHL_choice,0.01f);
			setChoice(epsiloniNGCHL_Af,epsiloniNGCHL_choice,0.3f);
			setChoice(epsilonfNGCHL_Af,epsilonfNGCHL_choice,0.05f);
			setChoice(tmaxNGCHL_Af,tmaxNGCHL_choice,40000f);
			setChoice(edgeiNGCHL_Ai,edgeiNGCHL_choice,20);
			setChoice(edgefNGCHL_Ai,edgefNGCHL_choice,200);
//			compute.l_i = lambdaiNGCHL_Af[0];
//			compute.l_f = lambdafNGCHL_Af[0];
//			compute.e_i = epsiloniNGCHL_Af[0];
//			compute.e_f = epsilonfNGCHL_Af[0];
//			compute.t_max = tmaxNGCHL_Af[0];
//			compute.delEdge_i = edgeiNGCHL_Ai[0];
//			compute.delEdge_f = edgefNGCHL_Ai[0];

			maxNodes_lbl.setText("# nodes:");
//			lambdaiNGCHL_choice.setSelectedIndex(0);
//			lambdafNGCHL_choice.setSelectedIndex(0);
//			epsiloniNGCHL_choice.setSelectedIndex(0);
//			epsilonfNGCHL_choice.setSelectedIndex(0);
//			tmaxNGCHL_choice.setSelectedIndex(0);
//			edgeiNGCHL_choice.setSelectedIndex(0);
//			edgefNGCHL_choice.setSelectedIndex(0);

			// Generate some nodes
			for (i = 0; i < compute.maxNodes; i++)
				compute.addNode(d);
		} else if (aNew.equals(Algo.CHL)) {
			// Set default values
			maxNodes_lbl.setText("# nodes:");

			// Generate some nodes
			for (i = 0; i < compute.maxNodes; i++)
				compute.addNode(d);
		} else if (aNew.equals(Algo.LBG) || 
				aNew.equals(Algo.LBGU)) 
		{
			// set algo to LBG in both cases!
			compute.algo = Algo.LBG;
			if(aNew.equals(Algo.LBGU)){
				compute.LBG_U_B=true;
			} else {
				compute.LBG_U_B=false;						
			}
			lbg_u_cb.setSelected(compute.LBG_U_B);

			// Set default values
			compute.numDiscreteSignals = numDiscreteSignalsLBG_Ai[0];
			compute.readyLBG_B = false;
			compute.errorBestLBG_U = Float.MAX_VALUE;

			numDiscreteSignalsLBG_choice.setSelectedIndex(0);
			maxNodes_lbl.setText("# nodes:");

			// Initialize discrete signals
			compute.initDiscreteSignals(compute.pd);

			// Generate some nodes
			int z = (int) (compute.numDiscreteSignals * Math.random());
			for (i = 0; i < compute.maxNodes; i++)
				compute.addNode(Math.round(compute.discreteSignalsX[(z+i)%compute.numDiscreteSignals]),
						Math.round(compute.discreteSignalsY[(z+i)%compute.numDiscreteSignals]));
		} else if (aNew.equals(Algo.GG)) {
			// Set default values
			compute.fineTuningB = false;
			compute.initGrid(2, 2, d);
			compute.decayFactor = 1.0f - betaGNG_Af[0];
			//maxNodes_lbl.setText("max. nodes:");
			maxNodes_lbl.setText("# nodes:");
			
			setChoice(lambdagGG_Af,lambdagGG_choice,30);
			setChoice(lambdafGG_Af,lambdafGG_choice,100);
			setChoice(epsiloniGG_Af,epsiloniGG_choice,0.1f);
			setChoice(epsilonfGG_Af,epsilonfGG_choice,0.0050f);
			setChoice(sigmaGG_Af,sigmaGG_choice,0.9f);
			setChoice(maxYGG_Ai,maxYGG_choice,0);
		} else if (aNew.equals(Algo.GR)) {
			// Set default values
			compute.fineTuningB = false;
			compute.initGrid(2, 1, d);
			compute.decayFactor = 1.0f - betaGNG_Af[0];
			//maxNodes_lbl.setText("max. nodes:");
			maxNodes_lbl.setText("# nodes:");
			
			setChoice(lambdagGG_Af,lambdagGG_choice,30);
			setChoice(lambdafGG_Af,lambdafGG_choice,100);
			setChoice(epsiloniGG_Af,epsiloniGG_choice,0.1f);
			setChoice(epsilonfGG_Af,epsilonfGG_choice,0.0050f);
			setChoice(sigmaGG_Af,sigmaGG_choice,2.0f);
			setChoice(maxYGG_Ai,maxYGG_choice,1);
			torusGG_cb.setSelected(true);
			compute.torusGGB = true;
			nodes_cb.setSelected(false);
			compute.nodesB = false;
		} else if (aNew.equals(Algo.SOM)) {
			// Set default values
			setChoice(epsiloniSOM_Af,epsiloniSOM_choice,0.1f);
			setChoice(epsilonfSOM_Af,epsilonfSOM_choice,0.005f);
			setChoice(sigmaiSOM_Af,sigmaiSOM_choice,5.0f);
			setChoice(sigmafSOM_Af,sigmafSOM_choice,0.2f);
			setChoice(tmaxSOM_Af,tmaxSOM_choice,40000f);
//			compute.e_i = epsiloniSOM_Af[0];
//			compute.e_f = epsilonfSOM_Af[0];
//			compute.sigma_i = sigmaiSOM_Af[0];
//			compute.sigma_f = sigmafSOM_Af[0];
//			compute.t_max = tmaxSOM_Af[0];

			compute.initGrid(sizeSOM_Ai[0][0],
					sizeSOM_Ai[0][1], d);
			maxNodes_lbl.setEnabled(false);
			maxNodes_choice.setEnabled(false);
//          maxNodes_lbl.setVisible(false);
//          maxNodes_choice.setVisible(false);
//			epsiloniSOM_choice.setSelectedIndex(0);
//			epsilonfSOM_choice.setSelectedIndex(0);
//			sigmaiSOM_choice.setSelectedIndex(0);
//			sigmafSOM_choice.setSelectedIndex(0);
//			tmaxSOM_choice.setSelectedIndex(0);
		}
		
	}
	
	// ComboBox Listener
	class ALComboBox implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent evt)
		{
//			if (shown==0){
//				log("******* ", "ALComboBox==ActionListener: GUI not yet shown, leaving.....");
//				log(evt.getSource().toString());
//				return;
//			}
			if (!isGuiInitialized()){
				log("ALComboBox: gui not yet initialized ....");
				return;
			}
			//
			// A MyComboBox event?
			//
			Object ob = evt.getSource();
			if (ob instanceof MyComboBox) {
				MyComboBox mc = (MyComboBox) ob;
				// algorithm
				if (algo_choice.equals(ob)) {
					log("ComboBox ALGO");

					String myArg = (String) mc.getSelectedItem();
					// which algo?
					Algo aNew = null; // any value as initializer;
					for (Algo a1:Algo.values()){
						if (myArg.equals(a1.getName())){
							log(">>> Network Model: \""+a1.getName()+"\"");
							aNew = a1;
							break;
						}
					}
					
					prepareAlgo(aNew);
					
				} 
				// distribution
				else if (distrib_choice.equals(mc)) {
//					if (compute.soundB) {
//						play(getCodeBase(), "audio/drummer.au");
//					}
					// default
					compute.pd=PD.Rectangle;
					for (PD p:PD.values()) {
						if (p.ordinal() == distrib_choice.getSelectedIndex()) {
							compute.pd = p;
							log(">>> Distribution: "+p.getName());
							break;
						}
					}
					// Initialize discrete signals
					compute.initDiscreteSignals(compute.pd);
					compute.errorBestLBG_U = Float.MAX_VALUE;		  
				} 
				// stepsize
				else if (stepSize_choice.equals(mc)) {
					compute.stepSize = stepSize_Ai[stepSize_choice.getSelectedIndex()];
				} 
				// machine speed
				else if (speed_choice.equals(mc)) {
					compute.tSleep = speed_Ai[speed_choice.getSelectedIndex()];
				} 
				// max. nodes
				else if (maxNodes_choice.equals(mc)) {
					compute.maxNodes = maxNodes_Ai[maxNodes_choice.getSelectedIndex()];
					compute.fineTuningB = false;
				} 
				// insert new node
				else if (newNodeGNG_choice.equals(mc)) {
					compute.lambdaGNG = lambdaGNG_Ai[newNodeGNG_choice.getSelectedIndex()];
				} 
				// max. edges
				else if (delEdgeGNG_choice.equals(mc)) {
					compute.MAX_EDGE_AGE = maxEdgeAgeGNG_Ai[delEdgeGNG_choice.getSelectedIndex()];
				} 
				// epsilon HCL
				else if (epsilonHCL_choice.equals(mc)) {
					compute.epsilon = epsilonHCL_Af[epsilonHCL_choice.getSelectedIndex()];
				} 
				// epsilon_i HCL
				else if (epsiloniHCL_choice.equals(mc)) {
					compute.e_i = epsiloniHCL_Af[epsiloniHCL_choice.getSelectedIndex()];
				} 
				// epsilon_f HCL
				else if (epsilonfHCL_choice.equals(mc)) {
					compute.e_f = epsilonfHCL_Af[epsilonfHCL_choice.getSelectedIndex()];
				} 
				// t_max HCL
				else if (tmaxHCL_choice.equals(mc)) {
					compute.t_max = tmaxHCL_Af[tmaxHCL_choice.getSelectedIndex()];
				} 
				// epsilon winner GNG
				else if (epsilonGNG1_choice.equals(mc)) {
					compute.epsilonGNG = epsilonGNG1_Af[epsilonGNG1_choice.getSelectedIndex()];
				} 
				// epsilon neighbors GNG
				else if (epsilonGNG2_choice.equals(mc)) {
					compute.epsilonGNG2 = epsilonGNG2_Af[epsilonGNG2_choice.getSelectedIndex()];
				} 
				// alpha GNG
				else if (alphaGNG_choice.equals(mc)) {
					compute.alphaGNG = alphaGNG_Af[alphaGNG_choice.getSelectedIndex()];
				} 
				// beta GNG
				else if (betaGNG_choice.equals(mc)) {
					compute.decayFactor = 1.0f - betaGNG_Af[betaGNG_choice.getSelectedIndex()];
					compute.decayFactorUtility = 1.0f - betaGNG_Af[betaGNG_choice.getSelectedIndex()];
				} 
				// utility GNG
				else if (utilityGNG_choice.equals(mc)) {
					compute.utilityGNG = utilityGNG_Af[utilityGNG_choice.getSelectedIndex()];
				} 
				// lambda_i NG
				else if (lambdaiNG_choice.equals(mc)) {
					compute.l_i = lambdaiNG_Af[lambdaiNG_choice.getSelectedIndex()];
				} 
				// lambda_f NG
				else if (lambdafNG_choice.equals(mc)) {
					compute.l_f = lambdafNG_Af[lambdafNG_choice.getSelectedIndex()];
				} 
				// epsilon_i NG
				else if (epsiloniNG_choice.equals(mc)) {
					compute.e_i = epsiloniNG_Af[epsiloniNG_choice.getSelectedIndex()];
				} 
				// epsilon_f NG
				else if (epsilonfNG_choice.equals(mc)) {
					compute.e_f = epsilonfNG_Af[epsilonfNG_choice.getSelectedIndex()];
				} 
				// t_max NG
				else if (tmaxNG_choice.equals(mc)) {
					compute.t_max = tmaxNG_Af[tmaxNG_choice.getSelectedIndex()];
				} 
				// lambda_g GG
				else if (lambdagGG_choice.equals(mc)) {
					compute.l_i = lambdagGG_Af[lambdagGG_choice.getSelectedIndex()];
				} 
				// maxXGG
				else if (maxYGG_choice.equals(mc)) {
					compute.maxYGG = (int) (maxYGG_Ai[maxYGG_choice.getSelectedIndex()]);
					log("maxXGG="+Integer.toString(compute.maxYGG));
				} 
				// lambda_f GG
				else if (lambdafGG_choice.equals(mc)) {
					compute.l_f = lambdafGG_Af[lambdafGG_choice.getSelectedIndex()];
				} 
				// epsilon_i GG
				else if (epsiloniGG_choice.equals(mc)) {
					compute.e_i = epsiloniGG_Af[epsiloniGG_choice.getSelectedIndex()];
				} 
				// epsilon_f GG
				else if (epsilonfGG_choice.equals(mc)) {
					compute.e_f = epsilonfGG_Af[epsilonfGG_choice.getSelectedIndex()];
				} 
				// sigma GG
				else if (sigmaGG_choice.equals(mc)) {
					compute.sigma = sigmaGG_Af[sigmaGG_choice.getSelectedIndex()];
				} 
				// grid size SOM
				else if (sizeSOM_choice.equals(mc)) {
					sizeSOM_index = sizeSOM_choice.getSelectedIndex();
				} 
				// epsilon_i SOM
				else if (epsiloniSOM_choice.equals(mc)) {
					compute.e_i = epsiloniSOM_Af[epsiloniSOM_choice.getSelectedIndex()];
				} 
				// epsilon_f SOM
				else if (epsilonfSOM_choice.equals(mc)) {
					compute.e_f = epsilonfSOM_Af[epsilonfSOM_choice.getSelectedIndex()];
				} 
				// sigma_i SOM
				else if (sigmaiSOM_choice.equals(mc)) {
					compute.sigma_i = sigmaiSOM_Af[sigmaiSOM_choice.getSelectedIndex()];
				} 
				// sigma_f SOM
				else if (sigmafSOM_choice.equals(mc)) {
					compute.sigma_f = sigmafSOM_Af[sigmafSOM_choice.getSelectedIndex()];
				} 
				// t_max SOM
				else if (tmaxSOM_choice.equals(mc)) {
					compute.t_max = tmaxSOM_Af[tmaxSOM_choice.getSelectedIndex()];
				} 
				// lambda_i CHL
				else if (lambdaiNGCHL_choice.equals(mc)) {
					compute.l_i = lambdaiNGCHL_Af[lambdaiNGCHL_choice.getSelectedIndex()];
				} 
				// lambda_f CHL
				else if (lambdafNGCHL_choice.equals(mc)) {
					compute.l_f = lambdafNGCHL_Af[lambdafNGCHL_choice.getSelectedIndex()];
				} 
				// epsilon_i CHL
				else if (epsiloniNGCHL_choice.equals(mc)) {
					compute.e_i = epsiloniNGCHL_Af[epsiloniNGCHL_choice.getSelectedIndex()];
				} 
				// epsilon_f CHL
				else if (epsilonfNGCHL_choice.equals(mc)) {
					compute.e_f = epsilonfNGCHL_Af[epsilonfNGCHL_choice.getSelectedIndex()];
				} 
				// t_max CHL
				else if (tmaxNGCHL_choice.equals(mc)) {
					compute.t_max = tmaxNGCHL_Af[tmaxNGCHL_choice.getSelectedIndex()];
				} 
				// edge_i CHL
				else if (edgeiNGCHL_choice.equals(mc)) {
					compute.delEdge_i = edgeiNGCHL_Ai[edgeiNGCHL_choice.getSelectedIndex()];
				} 
				// edge_f CHL
				else if (edgefNGCHL_choice.equals(mc)) {
					compute.delEdge_f = edgefNGCHL_Ai[edgefNGCHL_choice.getSelectedIndex()];
				} 
				// numDiscreteSignals LBG
				else if (numDiscreteSignalsLBG_choice.equals(mc)) {
					// Initialize discrete signals
					compute.initDiscreteSignals(compute.pd);
					// Set number of discrete signals
					if (compute.pd.equals(PD.DiscreteMixture)) {
						compute.numDiscreteSignals = 500; // TODO: define constant
					} else {
						compute.numDiscreteSignals = numDiscreteSignalsLBG_Ai[numDiscreteSignalsLBG_choice.getSelectedIndex()];
					}
					compute.errorBestLBG_U = Float.MAX_VALUE;		  
				} 
				compute.repaint(); // added after removing the general repaint()
			}
		}
	}
	class MyLabel extends JLabel {
		MyLabel(String label, int orientation) {
			super(label,orientation);
		}
		MyLabel(String label, int orientation, String helptext) {
			super(label,orientation);
			this.setToolTipText(helptext);
		}
		MyLabel(String label, String helptext) {
			super(label);
			this.setToolTipText(helptext);
		}
		MyLabel(String label) {
			super(label);
		}
	}
	// Button which adds special actionlistener
	class MyButton extends JButton {
		MyButton(String label, String helptext) {
			super(label);
			this.addActionListener(new ALButton());
			this.setToolTipText(helptext);
			this.setMargin(new Insets(2, 2, 2, 2));

		}
	}
	class MyCheckBox extends JCheckBox {
		MyCheckBox(String label, Boolean initial) {
			super(label, initial);
			this.addActionListener(new ALCheckBox());
		}
		MyCheckBox(String label, Boolean initial, String helptext) {
			super(label, initial);
			this.addActionListener(new ALCheckBox());
			this.setToolTipText(helptext);
		}
	}
	class MyComboBox extends JComboBox {
		String helptext;
		MyComboBox() {
			
			super();
			this.addActionListener(new ALComboBox());
		}
		MyComboBox(String helptext) {
			
			super();
			this.addActionListener(new ALComboBox());
			this.setToolTipText(helptext);
			this.helptext=helptext;
		}
	}
	ComponentListener comp = new ComponentListener() {
		@Override
		public void componentHidden(ComponentEvent e) {
			dumpEvent("Hidden", e);
		}

		@Override
		public void componentMoved(ComponentEvent e) {
			dumpEvent("Moved", e);
		}

		@Override
		public void componentResized(ComponentEvent e) {
			dumpEvent("Resized", e);
		}

		@Override
		public void componentShown(ComponentEvent e) {
			dumpEvent("Shown", e);
			shown=1;
			if (needReset) {
				compute.algo = Algo.GNG; // hack
				algo_choice.setSelectedIndex(Algo.GNG.ordinal());
				reset();
				needReset =false;
				log("componentShown(): reset performed ....");
				start();
			}
		}

		private void dumpEvent(String type, ComponentEvent e) {
			log(e.getComponent().getName() + " : " + type);
			log("width="+Integer.toString(e.getComponent().getWidth()) +
					"height = " + Integer.toString(e.getComponent().getHeight())+" : ");
		}
	};

	/**
	 * The name of the demo button.
	 */
	protected final static String BUTTON_Random = "Random";
	/**
	 * The name of the start button.
	 */
	protected final static String BUTTON_Start = "Start";
	/**
	 * The name of the stop button.
	 */
	protected final static String BUTTON_Stop = "Stop";
	/**
	 * The name of the reset button.
	 */
	protected final static String BUTTON_Reset = "Reset";
	/**
	 * The name of the reset button.
	 */
	protected final static String BUTTON_Restart = "Restart";
	/**
	 * The name of the signal checkbox.
	 */
	protected final static String SIGNALS    	= " signals";
	/**
	 * The name of the no_new_nodes checkbox.
	 */
	protected final static String NO_NEW_NODES 	= " no new nodes";
	/**
	 * The name of the utility checkbox.
	 */
	protected final static String UTILITY_GNG 	= " Utility   UtilityFactor:";
	/**
	 * The name of the LBG-U checkbox.
	 */
	protected final static String LBG_U 	= " LBG-U";
	/**
	 * The name of the sound checkbox.
	 */
	protected final static String SOUND      	= " sound";
	/**
	 * The name of the hardcopy checkbox.
	 */
	protected final static String AUTOSTOP      	= " autoStop";
	/**
	 * The name of the hardcopy checkbox.
	 */
	protected final static String WHITE      	= " White";
	/**
	 * The name of the random-init checkbox.
	 */
	protected final static String RNDINIT    	= " random init";
	/**
	 * The name of the teach checkbox.
	 */
	protected final static String TEACH      	= " teach";
	/**
	 * The name of the variable checkbox (HCL).
	 */
	protected final static String VARIABLE   	= " variable";
	/**
	 * The name of the edges checkbox.
	 */
	protected final static String EDGES   	= " edges";
	/**
	 * The name of the nodes checkbox.
	 */
	protected final static String NODES   	= " nodes";
	/**
	 * The name of the nodes checkbox.
	 */
	protected final static String TRACES   	= " traces";
	/**
	 * The name of the error graph checkbox.
	 */
	protected final static String ERRORGRAPH   = " error graph";
	/**
	 * The name of the Voronoi checkbox.
	 */
	protected final static String VORONOI   	= " Voronoi";
	/**
	 * The name of the Delaunay checkbox.
	 */
	protected final static String DELAUNAY   	= " Delaunay";
	private volatile boolean guiInitialized = false;
	boolean virgin = true;
	ComputeGNG compute;
	MyPanel cards;
	MyPanel pan_GNG;
	MyPanel p71;
	MyLabel epsilonHCL_lbl;
	MyLabel epsiloniHCL_lbl;
	MyLabel epsilonfHCL_lbl;
	MyLabel tmaxHCL_lbl;
	MyLabel maxNodes_lbl;
	MyCheckBox noNewNodesGNG_cb;
	MyCheckBox noNewNodesGG_cb;
	MyCheckBox mapSpaceGG_cb;
	MyCheckBox mapSpaceSOM_cb;
	MyCheckBox tau_cb;
	MyCheckBox nodes_cb;
	MyCheckBox torusGG_cb;
	MyCheckBox torusSOM_cb;
	MyCheckBox usage_cb;
	//MyCheckBox noNodes_cb2;
	MyCheckBox gng_u_cb;
	MyCheckBox lbg_u_cb;
	MyCheckBox variable_cb;
	MyCheckBox errorGraph_cb;
	MyComboBox algo_choice;
	MyComboBox distrib_choice;
	MyComboBox stepSize_choice;
	MyComboBox speed_choice;
	MyComboBox maxNodes_choice;
	MyComboBox newNodeGNG_choice;
	MyComboBox delEdgeGNG_choice;
	MyComboBox epsilonGNG1_choice;
	MyComboBox epsilonGNG2_choice;
	MyComboBox alphaGNG_choice;
	MyComboBox betaGNG_choice;
	MyComboBox utilityGNG_choice;
	MyComboBox epsilonHCL_choice;
	MyComboBox epsiloniHCL_choice;
	MyComboBox epsilonfHCL_choice;
	MyComboBox tmaxHCL_choice;
	MyComboBox lambdaiNG_choice;
	MyComboBox lambdafNG_choice;
	MyComboBox epsiloniNG_choice;
	MyComboBox epsilonfNG_choice;
	MyComboBox tmaxNG_choice;
	MyComboBox lambdaiNGCHL_choice;
	MyComboBox lambdafNGCHL_choice;
	MyComboBox epsiloniNGCHL_choice;
	MyComboBox epsilonfNGCHL_choice;
	MyComboBox tmaxNGCHL_choice;
	MyComboBox edgeiNGCHL_choice;
	MyComboBox edgefNGCHL_choice;
	MyComboBox numDiscreteSignalsLBG_choice;
	MyComboBox lambdagGG_choice;
	MyComboBox maxYGG_choice;
	MyComboBox lambdafGG_choice;
	MyComboBox epsiloniGG_choice;
	MyComboBox epsilonfGG_choice;
	MyComboBox sigmaGG_choice;
	MyComboBox epsiloniSOM_choice;
	MyComboBox epsilonfSOM_choice;
	MyComboBox sigmaiSOM_choice;
	MyComboBox sigmafSOM_choice;
	MyComboBox tmaxSOM_choice;
	MyComboBox sizeSOM_choice;
	int sizeSOM_index = 0;
	MyButton random_b;
	MyButton start_b;
	MyButton stop_b;
	MyButton reset_b;
	MyButton restart_b;
	/**
	 * The array for the stepsize.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected int stepSize_Ai[] = {50, 1, 2, 5, 10, 20, 40, 80, 100, 150, 200};
	/**
	 * The array for the machine speed.
	 *  To add or delete values to the choice, change this array and speed_As[].
	 */
	protected int speed_Ai[] = {10,  50,  200, 400,1000};
	/**
	 * The array for the machine speed names.
	 *  To add or delete values to the choice, change this array and speed_Ai.
	 */
	protected String speed_As[] = {"maximal", "fast", "medium","slow","very slow"};
	/**
	 * The array for the maximum number of nodes.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected int maxNodes_Ai[] = {100, 1, 2, 3, 4, 5, 10, 20, 50, 150,
			200, 250,500,1000,2000,5000,10000};
	/**
	 * The array for the maximum age of an edge.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected int maxEdgeAgeGNG_Ai[] = {88, 10, 20, 50, 100, 200, 400, 800};
	/**
	 * The array for the number of runs to insert a new node (parameter lambda).
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected int lambdaGNG_Ai[] = {600, 10, 50, 100, 200, 300,
			400, 500, 800, 1000, 2000};
	/**
	 * The array for the value epsilon initial of the HCL algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float epsiloniHCL_Af[] = {0.1f, 0.05f, 0.2f, 0.3f, 0.4f, 0.5f,
			0.6f, 0.7f, 0.8f, 0.9f, 1.0f};
	/**
	 * The array for the value epsilon final of the HCL algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float epsilonfHCL_Af[] = {0.005f, 0.0001f, 0.001f, 0.008f, 0.01f,
			0.05f, 0.1f, 0.2f, 0.5f, 1.0f};
	/**
	 * The array for the value t_max of the HCL algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float tmaxHCL_Af[] = {1000, 5000, 10000, 20000, 30000, 50000, 100000,200000,500000,1000000};
	/**
	 * The array for the value epsilon of the HCL algorithm.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float epsilonHCL_Af[] = {0.1f, 0.05f, 0.2f, 0.3f, 0.4f, 0.5f,
			0.6f, 0.7f, 0.8f, 0.9f, 1.0f};
	/**
	 * The array for the value epsilon of the GNG algorithm (winner).
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float epsilonGNG1_Af[] = {0.05f, 0.0f, 0.001f, 0.005f, 0.01f,
			0.1f, 0.2f, 0.5f, 1.0f};
	/**
	 * The array for the value epsilon of the GNG algorithm (second).
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float epsilonGNG2_Af[] = {0.0006f, 0.0f, 0.0001f, 0.001f, 0.005f,
			0.01f, 0.05f, 0.1f, 0.15f, 0.2f};
	/**
	 * The array for the value alpha of the GNG algorithm.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float alphaGNG_Af[] = {0.5f, 0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f};
	/**
	 * The array for the value beta of the GNG algorithm.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float betaGNG_Af[] = {0.0005f, 0.0f, 0.00001f, 0.00005f, 0.0001f,
			0.001f, 0.005f, 0.01f, 0.05f, 0.1f, 0.5f,
			1.0f};
	/**
	 * The array for the utility factor of the GNG-U algorithm.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float utilityGNG_Af[] =  {3.0f, 1.0f, 1.5f, 2.0f, 2.5f,
			3.5f, 4.0f, 4.5f, 5.0f, 5.5f, 6.0f, 6.5f,
			7.0f, 7.5f, 8.0f};
	/**
	 * The array for the value lambda initial of the NG algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float lambdaiNG_Af[] = {30, 10, 20, 40, 60, 80, 100};
	/**
	 * The array for the value lambda final of the NG algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float lambdafNG_Af[] = {0.01f, 0.0f, 0.001f, 0.005f, 0.05f,
			0.1f, 0.2f, 0.5f, 1.0f};
	/**
	 * The array for the value epsilon initial of the NG algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float epsiloniNG_Af[] = {0.3f, 0.1f, 0.2f, 0.4f, 0.5f, 0.6f,
			0.7f, 0.8f, 0.9f, 1.0f};
	/**
	 * The array for the value epsilon final of the NG algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float epsilonfNG_Af[] = {0.05f, 0.0f, 0.001f, 0.005f, 0.01f,
			0.05f, 0.1f, 0.2f, 0.5f, 1.0f};
	/**
	 * The array for the value t_max of the NG algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float tmaxNG_Af[] = {40000, 1000, 5000, 10000, 20000, 30000};
	/**
	 * The array for the value lambda initial of the NGCHL algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float lambdaiNGCHL_Af[] = {30, 10, 20, 40, 60, 80, 100};
	/**
	 * The array for the value lambda final of the NGCHL algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float lambdafNGCHL_Af[] = {0.01f, 0.0f, 0.001f, 0.005f, 0.05f,
			0.1f, 0.2f, 0.5f, 1.0f};
	/**
	 * The array for the value epsilon initial of the NGCHL algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float epsiloniNGCHL_Af[] ={0.3f, 0.1f, 0.2f, 0.4f, 0.5f, 0.6f, 0.7f,
			0.8f, 0.9f, 1.0f};
	/**
	 * The array for the value epsilon final of the NGCHL algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float epsilonfNGCHL_Af[] = {0.05f, 0.0f, 0.001f, 0.005f, 0.01f,
			0.05f, 0.1f, 0.2f, 0.5f, 1.0f};
	/**
	 * The array for the value t_max of the NGCHL algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float tmaxNGCHL_Af[] = {40000, 1000, 5000, 10000, 20000, 30000, 50000,100000, 200000, 1000000};
	/**
	 * The array for the value delete edge initial of the NGCHL algorithm.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected int edgeiNGCHL_Ai[] = {20, 10, 30, 40, 50, 60, 70, 80, 90, 100};
	/**
	 * The array for the value delete edge final of the NGCHL algorithm.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected int edgefNGCHL_Ai[] = {200, 100, 120, 140, 180, 250, 300, 400, 500};
	/**
	 * The array for the number of discrete signals.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected int numDiscreteSignalsLBG_Ai[] = {500, 5,10, 20, 50, 100, 200,
			1000, 2000, 5000, 10000};
	/**
	 * The array for the value lambda growing of the GG algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float lambdagGG_Af[] = {10, 20, 30, 40, 60, 80, 100};
	protected int maxYGG_Ai[] = {0,1,2,3,4,5,6,7,8,9,10,20};
	/**
	 * The array for the value lambda fine tuning of the GG algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float lambdafGG_Af[] = {60, 100, 150, 200};
	/**
	 * The array for the value epsilon initial of the GG algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float epsiloniGG_Af[] = {0.05f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f,
			0.6f, 0.7f, 0.8f, 0.9f, 1.0f};
	/**
	 * The array for the value epsilon final of the GG algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float epsilonfGG_Af[] = {0.0001f, 0.001f, 0.005f, 0.008f, 0.01f,
			0.05f, 0.1f, 0.2f, 0.5f, 1.0f};
	/**
	 * The array for the value sigma of the GG algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float sigmaGG_Af[] = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f,
			0.6f, 0.7f, 0.8f, 0.9f, 1.0f, 1.2f, 1.4f,
			1.6f, 1.8f, 2.0f};
	/**
	 * The array for the value epsilon initial of the SOM algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float epsiloniSOM_Af[] = {0.1f, 0.05f, 0.2f, 0.3f, 0.4f, 0.5f,
			0.6f, 0.7f, 0.8f, 0.9f, 1.0f};
	/**
	 * The array for the value epsilon final of the SOM algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float epsilonfSOM_Af[] = {0.005f, 0.0001f, 0.001f, 0.008f, 0.01f,
			0.05f, 0.1f, 0.2f, 0.5f, 1.0f};
	/**
	 * The array for the value sigma of the SOM algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float sigmaiSOM_Af[] = {5.0f, 1.0f, 1.5f, 2.5f, 10.0f,
			20.0f, 50.0f, 100.0f, 200.0f, 500.0f, 1000.0f};
	/**
	 * The array for the value sigma of the SOM algorithms.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float sigmafSOM_Af[] = {0.2f, 0.01f, 0.05f, 0.1f, 0.3f, 0.4f,
			0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f};
	/**
	 * The array for the value t_max of the SOM algorithm.
	 *  To add or delete values to the choice, only this array must be changed.
	 */
	protected float tmaxSOM_Af[] =  {40000, 1000, 5000, 10000, 20000, 30000, 50000,100000, 200000, 1000000};
	/**
	 * The array for the grid size of the SOM algorithm.
	 *  To add or delete values to the choice, change this array and
	 *  sizeSOM_As[]. Larger value should be first due to different max values for x and y
	 */
	protected int sizeSOM_Ai[][] = {{10, 10}, {5, 5},  {15, 15}, {100,100}, 
			{100, 1}, {1000, 1}, {10000, 1},
			{100, 2}, {100, 3},  {100, 10},
			{15, 10}, {20, 10},
			{50,5},{100,5},{1000,10}};
	/**
	 * The array for the grid size of the SOM algorithm.
	 *  To add or delete values to the choice, change this array and sizeSOM_Ai.
	 */
	protected String sizeSOM_As[] = {"10x10", "5x5", "15x15", "100x100", 
			"1x100", "1000x1", "10000x1", 
			"2x100", "3x100", "10x100",
			"10x15", "10x20",
			"5x50","5x100","10x1000"};

	public void reset() {
		syslog("reset() algo = " + compute.algo.getMnemo());
		syslog("reset() maxNodes = "+String.format("%d",compute.maxNodes));
		int i;
		Dimension d = compute.getSize();
		// Reset values
		compute.sigs = 0;
		compute.nNodes = 0;
		compute.nEdges = 0;
		compute.noNewNodesGNGB = noNewNodesGNG_cb.isSelected();
		compute.GNG_U_B = gng_u_cb.isSelected();
		compute.nodesMovedB = true;

		// Set specific algorithm parameters
		if (compute.algo.isGNGType()) { // GNG
			compute.addNode(d); // node 1
			if (compute.maxNodes != 1)
				compute.addNode(d); // node 2
		} else if (compute.algo.isLBGType()) { // LBG / LBG-U
			// Initialize discrete signals
			log("reset()init discrete distri ");
			compute.initDiscreteSignals(compute.pd); // need finite number of signals here
			compute.readyLBG_B = false;
			compute.errorBestLBG_U = Float.MAX_VALUE;		  
			compute.LBG_U_B = lbg_u_cb.isSelected();

			// Generate some nodes and put them onto the discrete signal positions
			int z = (int) (compute.numDiscreteSignals * Math.random());
			for (i = 0; i < compute.maxNodes; i++)
				compute.addNode(Math.round(compute.discreteSignalsX[(z+i)%compute.numDiscreteSignals]),
						Math.round(compute.discreteSignalsY[(z+i)%compute.numDiscreteSignals]));
		} else if (compute.algo.equals(Algo.GG) || compute.algo.equals(Algo.GR)) { // GG
			// Set default values
			compute.fineTuningB = false;
			if (compute.maxNodes < 4) {
				compute.maxNodes = maxNodes_Ai[0];
				maxNodes_choice.setSelectedIndex(0);
			}
			if (compute.algo.equals(Algo.GR)) {
				setChoice(maxYGG_Ai, maxYGG_choice, 1);
			}
//			else {
//				setChoice(maxYGG_Ai, maxYGG_choice, 0);
//			}
			// Generate some nodes
			if (compute.maxYGG==1){
				compute.initGrid(2, 1, d);
			} else {
				compute.initGrid(2, 2, d);
			}

		} else if (compute.algo.equals(Algo.SOM)) { // SOM
			// Set default values

			// Generate some nodes
			compute.initGrid(sizeSOM_Ai[sizeSOM_index][0],
					sizeSOM_Ai[sizeSOM_index][1], d);

		} else {
			// Generate some nodes (default)
			for (i = 0; i < compute.maxNodes; i++)
				compute.addNode(d);
		}
		ComputeGNG.paintCouter = 0;
		compute.repaint(); // reset, added after removing the general repaint()

	}

	@Override
	public void paint(Graphics g) {
		syslog("paint() ....");
        super.paint(g);
		setBackground( Color.red );
        setForeground( Color.black );
    }
	public void setDist(PD pd){
		compute.pd = pd;
		distrib_choice.setSelectedIndex(pd.ordinal());
	}
	void setAlgo(Algo x){
		compute.algo = x;
		algo_choice.setSelectedIndex(x.ordinal());
	}

	/* (non-Javadoc)
	 * @see java.applet.Applet#init()
	 * (builds up the GUI)
	 * 
	 */
	void createGUI() {
		MyComboBox curcb;
		syslog("createGUI() start");
		this.addComponentListener(comp);
		resize(new Dimension(700,700));
		
		int i;
		Font font = getFont();
		Font plainFont = new Font(font.getName(), Font.PLAIN, font.getSize()+2);
		Font boldFont = new Font(font.getName(), Font.BOLD, font.getSize()+2);

		// Set the layout-style
		setLayout(new BorderLayout());

		// - [N] cp_algo
		// - [C] compute
		// - [S] pSouth
		//      [N] pAll
		//          [N] pAllN // start, stop, reset
		//          [S] pAllS // errorgraph, nodes, edges, random init, sound
		//      [C] pDS
		//      [S] cards
		//
		//
		//
		
		// Create the GNG-MyPanel and center it
		compute = new ComputeGNG(this);
		add("Center", compute);

		// Create a MyPanel for the Buttons

		// Put the MyComboBox in a MyPanel to get a nicer look.
		MyPanel cp_distrib = new MyPanel();
		MyPanel cp_displayInterval = new MyPanel();
		MyPanel cp_speed = new MyPanel();
		MyPanel cp_nodes = new MyPanel(); // for nodes checkboxes
		MyPanel cp_algo = new MyPanel(); // for algo ComboBox
		// Create a menu of algorithms and add it to the MyPanel.
		algo_choice = new MyComboBox("algorithm to use");
		algo_choice.setFont(boldFont);
		for (Algo a:Algo.values()){
			algo_choice.addItem(a.getName());			
		}
		MyLabel tmp = new MyLabel("Network Model:", SwingConstants.RIGHT);
		tmp.setFont(plainFont);
		cp_algo.add(tmp);
		cp_algo.add(algo_choice);
		add("North", cp_algo);

		// Create new panel, set the layout-style and add it to the MyPanel
		MyPanel pSouth = new MyPanel();
		pSouth.setLayout(new BorderLayout());
		add("South", pSouth);
		MyPanel pAll = new MyPanel();

		MyPanel pSouthI = new MyPanel();
		pSouthI.setLayout(new BorderLayout());
		pSouthI.setBorder(BorderFactory.createTitledBorder("general"));
		pAll.setLayout(new BorderLayout());
		MyPanel pAllN = new MyPanel();
		MyPanel pAllS = new MyPanel();
	    random_b = new MyButton(BUTTON_Random,"simulate with randomly chosen model and distribution");
		start_b = new MyButton(BUTTON_Start,"start the simulation");
		stop_b = new MyButton(BUTTON_Stop,"stop the simulation");
		reset_b = new MyButton(BUTTON_Reset,"reset and randomize vectors");
		restart_b = new MyButton(BUTTON_Restart,"reset and start current model");
		start_b.setEnabled(false);
		reset_b.setEnabled(false);
		String orientation = BorderLayout.WEST;
		pAllN.add(random_b,orientation);
		pAllN.add(start_b,orientation);
		pAllN.add(stop_b,orientation);
		pAllN.add(reset_b,orientation);
		pAllN.add(restart_b,orientation);
		pAllN.add(new MyLabel("    "),orientation);
		
		pAllN.add(new MyCheckBox(TEACH,false,"instructive mode: show signals, winner and neighbors"),orientation);
		pAllN.add(new MyCheckBox(SIGNALS,false,"show input signals"),orientation);
		pAllN.add(new MyCheckBox(VORONOI,false,"display Voronoi diagram"),orientation);
		pAllN.add(new MyCheckBox(DELAUNAY,false,"display Delaunay triangulation"),orientation);

		errorGraph_cb = new MyCheckBox(ERRORGRAPH, compute.errorGraphB);
		pAllS.add(errorGraph_cb);
		pAllS.add(nodes_cb = new MyCheckBox(NODES, compute.nodesB, "display reference vectors as green circles"));
		pAllS.add(new MyCheckBox(TRACES, compute.tracesB, "display (motion) traces"));
		pAllS.add(new MyCheckBox(EDGES, compute.edgesB,"display neighborhood edges"));
		pAllS.add(new MyCheckBox(RNDINIT, compute.rndInitB,"init ref. vectors from rectangular distribution instead of chosen one"));
		//pAllS.add(new MyCheckBox(WHITE, compute.whiteB));
		pAllS.add(new MyCheckBox(SOUND, compute.soundB,"play sounds e.g. at insertion"));
		pAllS.add(new MyCheckBox(AUTOSTOP, compute.autoStopB,"stop algo automatically (model-dependent)"));
		pAll.add("North", pAllN);
		pAll.add("South", pAllS);
		pSouth.add("North",pSouthI);
		pSouthI.add("North", pAll);

		MyPanel pDS = new MyPanel();

		// Create a menu of distributions and add it to the MyPanel.
		curcb = distrib_choice = new MyComboBox("input signal probability distribution");
		for (PD pd:PD.values()) {
			distrib_choice.addItem(pd.getName());
		}
		cp_distrib.add(new MyLabel("prob. Distrib.:", SwingConstants.RIGHT,curcb.getToolTipText()));
		cp_distrib.add(distrib_choice);
		pDS.add(cp_distrib);

		// Create a menu of node-numbers and add it to the MyPanel.
		maxNodes_choice = new MyComboBox("maximum number of nodes");
		for (i = 0; i < maxNodes_Ai.length; i++)
			maxNodes_choice.addItem(String.valueOf(maxNodes_Ai[i]));

		maxNodes_lbl = new MyLabel("max. nodes:", SwingConstants.RIGHT,maxNodes_choice.getToolTipText());
		cp_nodes.add(maxNodes_lbl);
		cp_nodes.add(maxNodes_choice);
		pDS.add(cp_nodes);
		// Create a menu of step sizes and add it to the MyPanel.
		curcb = stepSize_choice = new MyComboBox("number of input signals after which a redraw should happen");
		for (i = 0; i < stepSize_Ai.length; i++)
			stepSize_choice.addItem(String.valueOf(stepSize_Ai[i]));

		cp_displayInterval.add(new MyLabel("display:", SwingConstants.RIGHT,curcb.getToolTipText()));
		cp_displayInterval.add(stepSize_choice);
		pDS.add(cp_displayInterval);
		pSouthI.add("Center", pDS);

		// Create a menu of machine speeds and add it to the MyPanel.
		curcb=speed_choice = new MyComboBox("parameter to slow down the simulation via 'sleep()' calls for better observation");
		for (i = 0; i < speed_As.length; i++)
			speed_choice.addItem(speed_As[i]);

//		cp_speed.add(new MyLabel("speed:", SwingConstants.RIGHT,curcb.getToolTipText()));
//		JSlider sped = new JSlider(JSlider.HORIZONTAL,
//				0, 50, 10);
//		sped.addChangeListener(new ALSlider());
//		cp_speed.add(sped);
		cp_speed.add(speed_choice);
		pDS.add(cp_speed);
		pSouthI.add("Center", pDS);

		cards = new MyPanel();
		cards.setLayout(new CardLayout());
		cards.setBorder(BorderFactory.createTitledBorder("model-specific"));


		MyPanel p1 = new MyPanel();
		//p1.setBackground(new Color(255,0,0));
		p1.setLayout(new BorderLayout());

		MyPanel p10 = new MyPanel();
		noNewNodesGNG_cb = new MyCheckBox(NO_NEW_NODES,false,"forbid creation of additional nodes");
		p10.add(noNewNodesGNG_cb);
		gng_u_cb = new MyCheckBox(UTILITY_GNG, compute.GNG_U_B,"use 'utility criterion to remove unused nodes");
		p10.add(gng_u_cb);
		utilityGNG_choice = new MyComboBox("utility factor");
		for (i = 0; i < utilityGNG_Af.length; i++)
			utilityGNG_choice.addItem(String.valueOf(utilityGNG_Af[i]));
		p10.add(utilityGNG_choice);

		pan_GNG = new MyPanel();
		pan_GNG.setLayout(new GridLayout(2,6));
		curcb = newNodeGNG_choice = new MyComboBox("after that many signals a new unit is created");
		for (i = 0; i < lambdaGNG_Ai.length; i++)
			newNodeGNG_choice.addItem(String.valueOf(lambdaGNG_Ai[i]));
		pan_GNG.add(new MyLabel("lambda",curcb.getToolTipText()));

		curcb = delEdgeGNG_choice = new MyComboBox("if an edge has 'locally aged' so high, it is removed");
		for (i = 0; i < maxEdgeAgeGNG_Ai.length; i++)
			delEdgeGNG_choice.addItem(String.valueOf(maxEdgeAgeGNG_Ai[i]));
		pan_GNG.add(new MyLabel("max. edge age",curcb.getToolTipText()));

		curcb = epsilonGNG1_choice = new MyComboBox("adaptation factor for winner");
		for (i = 0; i < epsilonGNG1_Af.length; i++)
			epsilonGNG1_choice.addItem(String.valueOf(epsilonGNG1_Af[i]));
		pan_GNG.add(new MyLabel("epsilon winner",curcb.getToolTipText()));

		curcb = epsilonGNG2_choice = new MyComboBox("adaptation factor for neighbors");
		for (i = 0; i < epsilonGNG2_Af.length; i++)
			epsilonGNG2_choice.addItem(String.valueOf(epsilonGNG2_Af[i]));
		pan_GNG.add(new MyLabel("epsilon neighbor",curcb.getToolTipText()));

		curcb = alphaGNG_choice = new MyComboBox("factor for error redistribution after an insertion");
		for (i = 0; i < alphaGNG_Af.length; i++)
			alphaGNG_choice.addItem(String.valueOf(alphaGNG_Af[i]));
		pan_GNG.add(new MyLabel("   alpha",curcb.getToolTipText()));

		curcb = betaGNG_choice = new MyComboBox("decay factor for accumulated error/resource values");
		for (i = 0; i < betaGNG_Af.length; i++)
			betaGNG_choice.addItem(String.valueOf(betaGNG_Af[i]));
		pan_GNG.add(new MyLabel("beta",curcb.getToolTipText()));

		pan_GNG.add(newNodeGNG_choice);
		pan_GNG.add(delEdgeGNG_choice);
		pan_GNG.add(epsilonGNG1_choice);
		pan_GNG.add(epsilonGNG2_choice);
		pan_GNG.add(alphaGNG_choice);
		pan_GNG.add(betaGNG_choice);
		
		MyPanel p1x = new MyPanel();
		p1x.setLayout(new FlowLayout());
		p1x.add(pan_GNG);
		p1.add("Center", p1x);

		p1.add("North", p10);
		//p1.add("Center", pan_GNG);
		p1.add("East", new MyPanel().add(new MyLabel("     ")));
		p1.add("West", new MyPanel().add(new MyLabel("     ")));


		MyPanel pan_HCL = new MyPanel();
		// Create a menu of epsilon sizes
		epsilonHCL_choice = new MyComboBox("constant adaptation factor");
		for (i = 0; i < epsilonHCL_Af.length; i++)
			epsilonHCL_choice.addItem(String.valueOf(epsilonHCL_Af[i]));
		
		epsiloniHCL_choice = new MyComboBox("initial adaptation factor for decaying adaptation");
		for (i = 0; i < epsiloniHCL_Af.length; i++)
			epsiloniHCL_choice.addItem(String.valueOf(epsiloniHCL_Af[i]));
		
		epsilonfHCL_choice = new MyComboBox("final adaptation factor for decaying adaptation");
		for (i = 0; i < epsilonfHCL_Af.length; i++)
			epsilonfHCL_choice.addItem(String.valueOf(epsilonfHCL_Af[i]));
		
		tmaxHCL_choice = new MyComboBox("time (number of signals) to use for decay process");
		for (i = 0; i < tmaxHCL_Af.length; i++)
			tmaxHCL_choice.addItem(String.valueOf(tmaxHCL_Af[i]));

		// Create two labels
		epsilonHCL_lbl = new MyLabel("epsilon =", SwingConstants.RIGHT,epsilonHCL_choice.getToolTipText());
		epsiloniHCL_lbl = new MyLabel("epsilon_i =", SwingConstants.RIGHT,epsiloniHCL_choice.getToolTipText());
		epsilonfHCL_lbl = new MyLabel("epsilon_f =", SwingConstants.RIGHT,epsilonfHCL_choice.getToolTipText());
		tmaxHCL_lbl = new MyLabel("t_max =", SwingConstants.RIGHT,tmaxHCL_choice.getToolTipText());

		pan_HCL.add(new MyCheckBox(VARIABLE, compute.variableB, "have a variable adaptation rate"));
		pan_HCL.add(epsilonHCL_lbl);
		pan_HCL.add(epsilonHCL_choice);

		pan_HCL.add(epsiloniHCL_lbl);
		pan_HCL.add(epsiloniHCL_choice);
		pan_HCL.add(epsilonfHCL_lbl);
		pan_HCL.add(epsilonfHCL_choice);
		pan_HCL.add(tmaxHCL_lbl);
		pan_HCL.add(tmaxHCL_choice);

		if (!compute.variableB) {
			epsiloniHCL_lbl.setEnabled(false);
			epsilonfHCL_lbl.setEnabled(false);
			//tmaxHCL_lbl.setEnabled(false);

			epsiloniHCL_choice.setEnabled(false);
			epsilonfHCL_choice.setEnabled(false);
			//tmaxHCL_choice.setEnabled(false);
		}

		MyPanel p3 = new MyPanel();
		p3.setLayout(new BorderLayout());

		MyPanel p30 = new MyPanel();
		p30.add(new MyPanel().add(new MyLabel("     ")));
		MyPanel pan_NG = new MyPanel();
		pan_NG.setLayout(new GridLayout(2,5));
		lambdaiNG_choice = new MyComboBox("initial neighborhood range lambda (beware: different definition then lambda for GNG)");
		for (i = 0; i < lambdaiNG_Af.length; i++)
			lambdaiNG_choice.addItem(String.valueOf(lambdaiNG_Af[i]));
		lambdafNG_choice = new MyComboBox("final neighborhood range lambda (beware: different definition then lambda for GNG)");
		for (i = 0; i < lambdafNG_Af.length; i++)
			lambdafNG_choice.addItem(String.valueOf(lambdafNG_Af[i]));
		epsiloniNG_choice = new MyComboBox("initial adaption rate");
		for (i = 0; i < epsiloniNG_Af.length; i++)
			epsiloniNG_choice.addItem(String.valueOf(epsiloniNG_Af[i]));
		epsilonfNG_choice = new MyComboBox("final adaptation rate");
		for (i = 0; i < epsilonfNG_Af.length; i++)
			epsilonfNG_choice.addItem(String.valueOf(epsilonfNG_Af[i]));
		tmaxNG_choice = new MyComboBox("number of signals used for parameter decay");
		for (i = 0; i < tmaxNG_Af.length; i++)
			tmaxNG_choice.addItem(String.valueOf(tmaxNG_Af[i]));

		pan_NG.add(new MyLabel("lambda_i",lambdaiNG_choice.getToolTipText()));
		pan_NG.add(new MyLabel("lambda_f",lambdafNG_choice.getToolTipText()));
		pan_NG.add(new MyLabel("epsilon_i",epsiloniNG_choice.getToolTipText()));
		pan_NG.add(new MyLabel("epsilon_f",epsilonfNG_choice.getToolTipText()));
		pan_NG.add(new MyLabel("t_max",tmaxNG_choice.getToolTipText()));
		pan_NG.add(lambdaiNG_choice);
		pan_NG.add(lambdafNG_choice);
		pan_NG.add(epsiloniNG_choice);
		pan_NG.add(epsilonfNG_choice);
		pan_NG.add(tmaxNG_choice);
		
		MyPanel p3x = new MyPanel();
		p3x.setLayout(new FlowLayout());
		p3x.add(pan_NG);
		p3.add("Center", p3x);
		
		p3.add("East", new MyPanel().add(new MyLabel("     ")));
		p3.add("West", new MyPanel().add(new MyLabel("     ")));
		p3.add("South", p30);


		MyPanel p4 = new MyPanel();

		p4.setLayout(new BorderLayout());

		MyPanel p40 = new MyPanel();
		p40.add(new MyPanel().add(new MyLabel("     ")));
		MyPanel pan_CHL = new MyPanel();
		pan_CHL.setLayout(new GridLayout(2,7));
		lambdaiNGCHL_choice = new MyComboBox(lambdaiNG_choice.getToolTipText());
		for (i = 0; i < lambdaiNGCHL_Af.length; i++)
			lambdaiNGCHL_choice.addItem(String.valueOf(lambdaiNGCHL_Af[i]));
		lambdafNGCHL_choice = new MyComboBox(lambdafNG_choice.getToolTipText());
		for (i = 0; i < lambdafNGCHL_Af.length; i++)
			lambdafNGCHL_choice.addItem(String.valueOf(lambdafNGCHL_Af[i]));
		epsiloniNGCHL_choice = new MyComboBox(epsiloniNG_choice.getToolTipText());
		for (i = 0; i < epsiloniNGCHL_Af.length; i++)
			epsiloniNGCHL_choice.addItem(String.valueOf(epsiloniNGCHL_Af[i]));
		epsilonfNGCHL_choice = new MyComboBox(epsilonfNG_choice.getToolTipText());
		for (i = 0; i < epsilonfNGCHL_Af.length; i++)
			epsilonfNGCHL_choice.addItem(String.valueOf(epsilonfNGCHL_Af[i]));
		tmaxNGCHL_choice = new MyComboBox(tmaxNG_choice.getToolTipText());
		for (i = 0; i < tmaxNGCHL_Af.length; i++)
			tmaxNGCHL_choice.addItem(String.valueOf(tmaxNGCHL_Af[i]));
		edgeiNGCHL_choice = new MyComboBox("initial edge age for emoval");
		for (i = 0; i < edgeiNGCHL_Ai.length; i++)
			edgeiNGCHL_choice.addItem(String.valueOf(edgeiNGCHL_Ai[i]));
		edgefNGCHL_choice = new MyComboBox("final edge age for removal");
		for (i = 0; i < edgefNGCHL_Ai.length; i++)
			edgefNGCHL_choice.addItem(String.valueOf(edgefNGCHL_Ai[i]));

		pan_CHL.add(new MyLabel("lambda_i"));
		pan_CHL.add(new MyLabel("lambda_f"));
		pan_CHL.add(new MyLabel("epsilon_i"));
		pan_CHL.add(new MyLabel("epsilon_f"));
		pan_CHL.add(new MyLabel("t_max"));
		pan_CHL.add(new MyLabel("edge_i"));
		pan_CHL.add(new MyLabel("edge_f"));
		pan_CHL.add(lambdaiNGCHL_choice);
		pan_CHL.add(lambdafNGCHL_choice);
		pan_CHL.add(epsiloniNGCHL_choice);
		pan_CHL.add(epsilonfNGCHL_choice);
		pan_CHL.add(tmaxNGCHL_choice);
		pan_CHL.add(edgeiNGCHL_choice);
		pan_CHL.add(edgefNGCHL_choice);
		
		MyPanel p4x = new MyPanel();
		p4x.setLayout(new FlowLayout());
		p4x.add(pan_CHL);
		p4.add("Center", p4x);
		
		p4.add("East", new MyPanel().add(new MyLabel("     ")));
		p4.add("West", new MyPanel().add(new MyLabel("     ")));
		p4.add("South", p40);

		MyPanel p5 = new MyPanel();

		p5.setLayout(new BorderLayout());

		MyPanel p6 = new MyPanel();

		p6.setLayout(new BorderLayout());

		MyPanel p60 = new MyPanel();
		p60.add(new MyPanel().add(new MyLabel("     ")));
		lbg_u_cb = new MyCheckBox(LBG_U, compute.LBG_U_B, "use utility criterion to identify and remove nodes contributing only little to error reduction");
		p60.add(lbg_u_cb);
		MyPanel p61 = new MyPanel();
		p61.setLayout(new GridLayout(2,1));
		numDiscreteSignalsLBG_choice = new MyComboBox("size of discrete signal set");
		for (i = 0; i < numDiscreteSignalsLBG_Ai.length; i++)
			numDiscreteSignalsLBG_choice.addItem(String.valueOf(numDiscreteSignalsLBG_Ai[i]));

		p61.add(new MyLabel("Number of Signals"));
		p61.add(numDiscreteSignalsLBG_choice);

		
		MyPanel p6x = new MyPanel();
		p6x.setLayout(new FlowLayout());
		p6x.add(p61);
		p6.add("Center", p6x);
		
		p6.add("North", p60);
		//p6.add("Center", p61);
		p6.add("East", new MyPanel().add(new MyLabel("                     ")));
		p6.add("West", new MyPanel().add(new MyLabel("                     ")));

		MyPanel p7 = new MyPanel();
		p7.setLayout(new BorderLayout());

		MyPanel p70 = new MyPanel();
		noNewNodesGG_cb = new MyCheckBox(NO_NEW_NODES,false,"disallow the insertion of new node layers (rows and columns in 2D)");
		p70.add(noNewNodesGG_cb);
		maxYGG_choice = new MyComboBox("maximum gridheight (if set to non-zero value, it limits the growth in this direction)");
		p70.add(new JLabel("max Y-width"));
		for (i = 0; i < maxYGG_Ai.length; i++)
			maxYGG_choice.addItem(String.valueOf(maxYGG_Ai[i]));
		p70.add(maxYGG_choice);
		mapSpaceGG_cb = new MyCheckBox("mapSpace",false,"show 2D map space (not signal space)");
		p70.add(mapSpaceGG_cb);
		torusGG_cb = new MyCheckBox("torus",false,"close growing grid to a torus by connecting x=0 and x=gridWidth");
		p70.add(torusGG_cb);
		tau_cb = new MyCheckBox("tau",false,"show tau values");
		//p70.add(tau_cb);
		usage_cb = new MyCheckBox("usage",false,"paint unused nodes black");
		p70.add(usage_cb);

		MyPanel pan_GG = new MyPanel();
		pan_GG.setLayout(new GridLayout(2,5));
		lambdagGG_choice = new MyComboBox("insertion parameter for growth phase");
		for (i = 0; i < lambdagGG_Af.length; i++)
			lambdagGG_choice.addItem(String.valueOf(lambdagGG_Af[i]));
		lambdafGG_choice = new MyComboBox("time parameter for fine-tuning phase");
		for (i = 0; i < lambdafGG_Af.length; i++)
			lambdafGG_choice.addItem(String.valueOf(lambdafGG_Af[i]));
		epsiloniGG_choice = new MyComboBox("adaptation parameter for growth phase and initial adaptation parameter for fine-tuning phase");
		for (i = 0; i < epsiloniGG_Af.length; i++)
			epsiloniGG_choice.addItem(String.valueOf(epsiloniGG_Af[i]));
		epsilonfGG_choice = new MyComboBox("final adaptation parameter");
		for (i = 0; i < epsilonfGG_Af.length; i++)
			epsilonfGG_choice.addItem(String.valueOf(epsilonfGG_Af[i]));
		sigmaGG_choice = new MyComboBox("neighborhood parameter");
		for (i = 0; i < sigmaGG_Af.length; i++)
			sigmaGG_choice.addItem(String.valueOf(sigmaGG_Af[i]));

		pan_GG.add(new MyLabel("lambda_g",lambdagGG_choice.getToolTipText()));
		pan_GG.add(new MyLabel("lambda_f",lambdafGG_choice.getToolTipText()));
		pan_GG.add(new MyLabel("epsilon_i",epsiloniGG_choice.getToolTipText()));
		pan_GG.add(new MyLabel("epsilon_f",epsilonfGG_choice.getToolTipText()));
		pan_GG.add(new MyLabel("sigma",sigmaGG_choice.getToolTipText()));
		pan_GG.add(lambdagGG_choice);
		pan_GG.add(lambdafGG_choice);
		pan_GG.add(epsiloniGG_choice);
		pan_GG.add(epsilonfGG_choice);
		pan_GG.add(sigmaGG_choice);

		MyPanel p7x = new MyPanel();
		p7x.setLayout(new FlowLayout());
		p7x.add(pan_GG);
		p7.add("Center", p7x);
		
		p7.add("North", p70);
		p7.add("East", new MyPanel().add(new MyLabel("     ")));
		p7.add("West", new MyPanel().add(new MyLabel("     ")));

		MyPanel p8 = new MyPanel();
		p8.setLayout(new BorderLayout());

		MyPanel p80 = new MyPanel();
		//p80.add(new MyPanel().add(new MyLabel("     ")));
		mapSpaceSOM_cb = new MyCheckBox("mapSpace",false,"show 2D map space (not signal space)");
		p80.add(new MyPanel().add(mapSpaceSOM_cb));
		torusSOM_cb = new MyCheckBox("torus",false,"close SOM to a torus by connecting x=0 and x=gridWidth");
		p80.add(new MyPanel().add(torusSOM_cb));

		MyPanel pan_SOM = new MyPanel();
		pan_SOM.setLayout(new GridLayout(2,6));
		sizeSOM_choice = new MyComboBox("SOM size");
		for (i = 0; i < sizeSOM_As.length; i++)
			sizeSOM_choice.addItem(sizeSOM_As[i]);
		epsiloniSOM_choice = new MyComboBox("initial adaptation parameter");
		for (i = 0; i < epsiloniSOM_Af.length; i++)
			epsiloniSOM_choice.addItem(String.valueOf(epsiloniSOM_Af[i]));
		epsilonfSOM_choice = new MyComboBox("final adaptation parameter");
		for (i = 0; i < epsilonfSOM_Af.length; i++)
			epsilonfSOM_choice.addItem(String.valueOf(epsilonfSOM_Af[i]));
		sigmaiSOM_choice = new MyComboBox("initial neigborhood parameter");
		for (i = 0; i < sigmaiSOM_Af.length; i++)
			sigmaiSOM_choice.addItem(String.valueOf(sigmaiSOM_Af[i]));
		sigmafSOM_choice = new MyComboBox("final neighborhood parameter");
		for (i = 0; i < sigmafSOM_Af.length; i++)
			sigmafSOM_choice.addItem(String.valueOf(sigmafSOM_Af[i]));
		tmaxSOM_choice = new MyComboBox("number of input signals to use for adaptation");
		for (i = 0; i < tmaxSOM_Af.length; i++)
			tmaxSOM_choice.addItem(String.valueOf(tmaxSOM_Af[i]));

		pan_SOM.add(new MyLabel("Grid size",sizeSOM_choice.getToolTipText()));
		pan_SOM.add(new MyLabel("epsilon_i",epsiloniSOM_choice.getToolTipText()));
		pan_SOM.add(new MyLabel("epsilon_f",epsilonfSOM_choice.getToolTipText()));
		pan_SOM.add(new MyLabel("sigma_i",sigmaiSOM_choice.getToolTipText()));
		pan_SOM.add(new MyLabel("sigma_f",sigmafSOM_choice.getToolTipText()));
		pan_SOM.add(new MyLabel("t_max",tmaxSOM_choice.getToolTipText()));
		pan_SOM.add(sizeSOM_choice);
		pan_SOM.add(epsiloniSOM_choice);
		pan_SOM.add(epsilonfSOM_choice);
		pan_SOM.add(sigmaiSOM_choice);
		pan_SOM.add(sigmafSOM_choice);
		pan_SOM.add(tmaxSOM_choice);
		
		MyPanel p8x = new MyPanel();
		p8x.setLayout(new FlowLayout());
		p8x.add(pan_SOM);
		p8.add("Center", p8x);
		
		p8.add("North", p80);
		p8.add("East", new MyPanel().add(new MyLabel("     ")));
		p8.add("West", new MyPanel().add(new MyLabel("     ")));


		cards.add(Algo.GNG.getName(), p1); log("cards add "+Algo.GNG.getName());
		cards.add(Algo.HCL.getName(), pan_HCL);
		cards.add(Algo.NG.getName(), p3);
		cards.add(Algo.NGCHL.getName(), p4);
		cards.add(Algo.CHL.getName(), p5);
		cards.add(Algo.LBG.getName(), p6);
		cards.add(Algo.GG.getName(), p7);
		cards.add(Algo.SOM.getName(), p8);
		pSouth.add("South", cards);

		// some initial value, will be overridden
		compute.algo = null;

		// Get the parameter from the html-page
		String algorithmParam = getParameter("algorithm");
		String distributionParam = getParameter("distribution");
		log("******** algorithmParam: "+algorithmParam+" distributionParam:"+ distributionParam+ "*********\n");
		if (algorithmParam==null){
			needReset=false;		
			algorithmParam = Algo.NG.getMnemo();
			distributionParam = "UNIT";
			algo_choice.setSelectedIndex(Algo.NG.ordinal());
			log("******** algorithmParam: "+algorithmParam+" distributionParam:"+ distributionParam+ " now *********\n");
		} else {
			needReset=true;		
		}

		
		if (distributionParam != null) {
			for (PD pd:PD.values()) {
				if (distributionParam.equals(pd.getName())) {
					setDist(pd);
					break;
				}
				// default
				setDist(PD.Rectangle);
			}
		}

		if (algorithmParam != null) {
			log("createGUI() algoparam <> null");
			// Init for Hard Competitive Learning (HCL)
			if (algorithmParam.equals(Algo.HCL.getMnemo())) {
				compute.algo = Algo.HCL;
				algo_choice.setSelectedIndex(Algo.HCL.ordinal());
				((CardLayout)cards.getLayout()).show(cards, Algo.HCL.getName());
				maxNodes_lbl.setText("# nodes:");

				// Generate some nodes (HCL)
				for (i = 0; i < compute.maxNodes; i++)
					compute.addNode(new Dimension(compute.panelWidth, compute.panelHeight));
			}
			// Init for Neural Gas (NG)
			else if (algorithmParam.equals(Algo.NG.getMnemo())) {
				compute.algo = Algo.NG;
				algo_choice.setSelectedIndex(Algo.NG.ordinal());
				((CardLayout)cards.getLayout()).show(cards, Algo.NG.getName());
				maxNodes_lbl.setText("# nodes:");
//				compute.stepSize = stepSize_Ai[3];
//				stepSize_choice.setSelectedIndex(3);

				// Generate some nodes (NG)
				for (i = 0; i < compute.maxNodes; i++)
					compute.addNode(new Dimension(compute.panelWidth, compute.panelHeight));
			}
			// Init for Neural Gas with Competitive Hebbian Learning (NGCHL)
			else if (algorithmParam.equals(Algo.NGCHL.getMnemo())) {
				compute.algo = Algo.NGCHL;
				algo_choice.setSelectedIndex(Algo.NGCHL.ordinal());
				((CardLayout)cards.getLayout()).show(cards, Algo.NGCHL.getName());
				maxNodes_lbl.setText("# nodes:");
//				compute.stepSize = stepSize_Ai[3];
//				stepSize_choice.setSelectedIndex(3);

				// Generate some nodes (NGCHL)
				for (i = 0; i < compute.maxNodes; i++)
					compute.addNode(new Dimension(compute.panelWidth, compute.panelHeight));
			}
			// Init for Competitive Hebbian Learning (CHL)
			else if (algorithmParam.equals(Algo.CHL.getMnemo())) {
				compute.algo = Algo.CHL;
				algo_choice.setSelectedIndex(Algo.CHL.ordinal());
				((CardLayout)cards.getLayout()).show(cards, Algo.CHL.getName());
				maxNodes_lbl.setText("# nodes:");

				// Generate some nodes (CHL)
				for (i = 0; i < compute.maxNodes; i++)
					compute.addNode(new Dimension(compute.panelWidth, compute.panelHeight));
			}
			// Init for LBG (LBG)
			else if (algorithmParam.equals(Algo.LBG.getMnemo()) ||
					algorithmParam.equals(Algo.LBGU.getMnemo())) {
				if (algorithmParam.equals(Algo.LBGU.getMnemo())) {
					compute.LBG_U_B = true;
					lbg_u_cb.setSelected(compute.LBG_U_B);
					compute.algo = Algo.LBGU;
				} else {
					compute.LBG_U_B = false;
					lbg_u_cb.setSelected(compute.LBG_U_B);
					compute.algo = Algo.LBG;	
				}
				algo_choice.setSelectedIndex(compute.algo.ordinal());
				((CardLayout)cards.getLayout()).show(cards, Algo.LBG.getName());
				maxNodes_lbl.setText("# nodes:");
				compute.errorBestLBG_U = Float.MAX_VALUE;		  
			}
			// Init for Growing Grid (GG)
			else if (algorithmParam.equals(Algo.GG.getMnemo())) {
				compute.algo = Algo.GG;
				algo_choice.setSelectedIndex(Algo.GG.ordinal());
				((CardLayout)cards.getLayout()).show(cards, Algo.GG.getName());

				// Generate some nodes (GG)
				compute.initGrid(2, 2,
						new Dimension(compute.panelWidth, compute.panelHeight));
			}			// Init for Growing Grid (GG)
			else if (algorithmParam.equals(Algo.GR.getMnemo())) {
				compute.algo = Algo.GR;
				algo_choice.setSelectedIndex(Algo.GR.ordinal());
				((CardLayout)cards.getLayout()).show(cards, Algo.GG.getName());

				// Generate some nodes (GG)
				compute.initGrid(2, 1,
						new Dimension(compute.panelWidth, compute.panelHeight));
			}

			// Init for Self-Organizing Map (SOM)
			else if (algorithmParam.equals(Algo.SOM.getMnemo())) {
				compute.algo = Algo.SOM;
				algo_choice.setSelectedIndex(Algo.SOM.ordinal());
				((CardLayout)cards.getLayout()).show(cards, Algo.SOM.getName());
				
				maxNodes_lbl.setEnabled(false);
				maxNodes_choice.setEnabled(false);

				// Generate some nodes (SOM)
				compute.initGrid(sizeSOM_Ai[0][0], sizeSOM_Ai[0][1],
						new Dimension(compute.panelWidth, compute.panelHeight));
			}
			// Init for Growing Neural Gas (GNG) and GNG-U
			else { // default case: GNG
				if (algorithmParam.equals(Algo.GNGU.getMnemo())) {
					algo_choice.setSelectedIndex(Algo.GNGU.ordinal());
					compute.GNG_U_B = true;
					gng_u_cb.setSelected(true);
					compute.algo = Algo.GNGU;
				} else {
					algo_choice.setSelectedIndex(Algo.GNG.ordinal());
					compute.algo = Algo.GNG;			
					gng_u_cb.setSelected(false);
					compute.algo = Algo.GNG;
				}
				
				// Generate some nodes (GNG)
				compute.addNode(new Dimension(compute.panelWidth, compute.panelHeight));
				if (compute.maxNodes != 1)
					compute.addNode(new Dimension(compute.panelWidth, compute.panelHeight));
			}
		}
		compute.errorGraph = new GraphGNG(this);
		compute.errorGraph.graph.startNewTrace();
		setGuiInitialized(true);
		syslog("createGUI() end XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
	}
	public void randomizeSimulation() {
		setAlgo(selectRandomAlgo());
		do {
			setDist(selectRandomPD());
		} while (compute.pd == PD.DiscreteMixture  
				|| compute.pd == PD.Jump
				|| compute.pd == PD.RightMouseB
				|| compute.pd == PD.Move
				|| compute.pd == PD.MoveJump);
		//setAlgo(Algo.GG);
		log("randomize: algo="+compute.algo.getName());
		log("randomize:   PD="+compute.pd.getName());
		prepareAlgo(compute.algo);
		reset();
	}
	@Override
	public void init() {
		syslog("init()");
		Font font = getFont();
		Font buttonfont = new Font(font.getName(), Font.PLAIN, font.getSize()+1 /*font.getSize()*/);
		Font checkboxfont = new Font(font.getName(), Font.PLAIN, font.getSize());
		Font comboboxfont = checkboxfont;
		Font labelfont = checkboxfont;
		//		this.setFont(myfont);
		UIManager.put("Button.font", buttonfont);
//		UIManager.put("ToggleButton.font", /* font of your liking */);
//		UIManager.put("RadioButton.font", /* font of your liking */);
		UIManager.put("CheckBox.font", checkboxfont);
//		UIManager.put("ColorChooser.font", /* font of your liking */);
		UIManager.put("ComboBox.font", comboboxfont);
		UIManager.put("Label.font", labelfont);
//		UIManager.put("List.font", /* font of your liking */);
//		UIManager.put("MenuBar.font", /* font of your liking */);
//		UIManager.put("MenuItem.font", /* font of your liking */);
//		UIManager.put("RadioButtonMenuItem.font", /* font of your liking */);
//		UIManager.put("CheckBoxMenuItem.font", /* font of your liking */);
//		UIManager.put("Menu.font", /* font of your liking */);
//		UIManager.put("PopupMenu.font", /* font of your liking */);
//		UIManager.put("OptionPane.font", /* font of your liking */);
//		UIManager.put("Panel.font", /* font of your liking */);
//		UIManager.put("ProgressBar.font", /* font of your liking */);
//		UIManager.put("ScrollPane.font", /* font of your liking */);
//		UIManager.put("Viewport.font", /* font of your liking */);
//		UIManager.put("TabbedPane.font", /* font of your liking */);
//		UIManager.put("Table.font", /* font of your liking */);
//		UIManager.put("TableHeader.font", /* font of your liking */);
//		UIManager.put("TextField.font", /* font of your liking */);
//		UIManager.put("PasswordField.font", /* font of your liking */);
//		UIManager.put("TextArea.font", /* font of your liking */);
//		UIManager.put("TextPane.font", /* font of your liking */);
//		UIManager.put("EditorPane.font", /* font of your liking */);
//		UIManager.put("TitledBorder.font", /* font of your liking */);
//		UIManager.put("ToolBar.font", /* font of your liking */);
//		UIManager.put("ToolTip.font", /* font of your liking */);
//		UIManager.put("Tree.font", /* font of your liking */);
		createGUI();
//		try {
//			log("invokeandwait");
//			SwingUtilities.invokeAndWait(new GuiThread());
//		} catch (InterruptedException e) {
//			log("InterruptedException");
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			log("InvocationTargetException");
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	class RandomSimThread implements Runnable {
		@Override
		public void run() {
    		log("T H R E A D ! ! ! RandomSimThread run()");
    		stop();
    		randomizeSimulation();
    		startNow();
    		log("T H R E A D ! ! ! RandomSimThread done");
		}
	}
	public void randomSim() {
		syslog("randomSim() ****************************************************************");
		SwingUtilities.invokeLater(new RandomSimThread());
	}
	public PD selectRandomPD() {
		Random random = new Random();  
        return PD.values()[random.nextInt(PD.values().length-1)];  //-1 to prevent RightMouseButton PD
	}
	public Algo selectRandomAlgo() {
		Random random = new Random();  
        return Algo.values()[random.nextInt(Algo.values().length)];  
	}
	
	//class StartThread implements Runnable {
    class StartThread extends Thread {
		@Override
		public void run() {
			log("T H R E A D ! ! ! StartThread run()");
			if (virgin) {
				do {
					randomizeSimulation();
				} while (compute.algo.isLBGType());
				virgin = false;
			}
			//start2();
			start_b.setEnabled(false);
			//random_b.setEnabled(false);
			restart_b.setEnabled(false);
			
			stop_b.setEnabled(true);
	    	reset_b.setEnabled(false);
			compute.readyLBG_B = false;
			compute.start();
			log("T H R E A D ! ! ! StartThread done");
		}
	}
	
	@Override
	public void start() {
		syslog("start()");
		syslog(String.format("tspeed = %d", compute.tSleep));
		//for (int i =1;i<10;i++)
		StartThread t = new StartThread();
		//t.start();
		SwingUtilities.invokeLater(t);
		try {
			t.join();
			log("Joined!!!!!!!");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startNow() {
		syslog("startNow()");
		(new StartThread()).run();
	    
	}
	

	@Override
	public void stop() {
		syslog("stop() after "+String.format("%d",compute.sigs)+" input signals");
		start_b.setEnabled(true);
		random_b.setEnabled(true);
		restart_b.setEnabled(true);
		stop_b.setEnabled(false);
		reset_b.setEnabled(true);
		compute.stop();
	}

	@Override
	public void destroy() {
		compute.destroy();
		log("............... destroyed .....................");
	}

	public void graphClose() {
		compute.errorGraphB = false;
		errorGraph_cb.setSelected(false);
		compute.graphClose();
	}

//	private void setCheckBox(MyCheckBox cb, boolean x){
//		cb.setSelected(x);
//	}
	private void setChoice(float[] arr,MyComboBox cb,float x){
		int i = indexWithContent(arr, x);
		cb.setSelectedIndex(i);
	}
	private void setChoice(int[] arr,MyComboBox cb,int x){
		int i = indexWithContent(arr, x);
		cb.setSelectedIndex(i);
	}
	//
	// find index in array with the value equal to x
	//
	private int indexWithContent(float[] array,float x){
		for(int i=0;i<array.length;i++){
			// handle zero
			if (x<0.000001 && array[i]<0.000001)
				return i;
			else
				if (Math.abs(array[i]-x)/x<0.01) {
					return i;
				}
		}
		throw new EmptyStackException();
	}
	private int indexWithContent(int[] array,int x){
		for(int i=0;i<array.length;i++){
			if (array[i]==x) {
				return i;
			}
		}
		throw new EmptyStackException();
	}

	@Override
	public String[][] getParameterInfo() {
		String[][] info = {
				// Arrays of arrays of strings describing each parameter.
				{"algorithm\t\t",
					"The abbreviation of an algorithm (GNG, GNG-U, HCL, NG, NGCHL, " +
							"CHL, LBG, LBG-U, GG, SOM)",
				"The starting algorithm"},
				{"distribution\t",
					"The name of a distribution (Rectangle, Ring, Circle, UNI, " +
							"Small Spirals, Large Spirals, HiLo Density, Discrete, UNIT, " +
							"Move & Jump, Move, Jump, Right MouseB)",
				"The initial distribution"}
		};
		return info;
	}

	/**
	 * Where to find beta releases and additional information.
	 */
	static final String myHomepage =
			"http://www.demogng.de";



	@Override
	public String getAppletInfo() {
		String versionInfo = "DemoGNG " + compute.DGNG_VERSION +
				". Written by Hartmut S. Loos (Copyright 1996-1998)" +
				"\n\nand  Bernd Fritzke (Copyright 2012-2013)\n\n under the terms of the GNU General Public License." +
				"\n\nFor updates look at " + myHomepage;
		return versionInfo;
	}
	boolean isGuiInitialized() {
		return guiInitialized;
	}
	void setGuiInitialized(boolean guiInitialized) {
		this.guiInitialized = guiInitialized;
	}

}

