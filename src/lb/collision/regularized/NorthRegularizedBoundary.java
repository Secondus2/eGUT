/*  Lattice Boltzmann sample, written in Java
 *
 *  Main author: Jean-Luc Falcone
 *  Co-author: Jonas Latt
 *  Copyright (C) 2006 University of Geneva
 *  Address: Jean-Luc Falcone, Rue General Dufour 24,
 *           1211 Geneva 4, Switzerland 
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public 
 *  License along with this program; if not, write to the Free 
 *  Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 *  Boston, MA  02110-1301, USA.
*/

package lb.collision.regularized;

import lb.collision.D2Q9RegularizedBoundary;

/** A second order accurate boundary condition for straight boundaries
 *  aligned with the north wall
 */
public class NorthRegularizedBoundary implements PiComputer {
	
	private final int[] freeLinks;
	public final double[] fNeq;
	
	public NorthRegularizedBoundary() {
		freeLinks = new int[] {1,2,3,5,6};
		fNeq = new double[9];
	}

	public void computeNeqPi(double rho, double[] f, double[] neqPi, 
			D2Q9RegularizedBoundary collOp)
	{
		for(int i: freeLinks) {
			fNeq[i] = f[i]-collOp.fEq(rho,i,f);
		}
		neqPi[XX] = fNeq[1] + fNeq[3] + 2.*(fNeq[5]+fNeq[6]);
		neqPi[YY] = 2.*(fNeq[6]+fNeq[2]+fNeq[5]);
		neqPi[XY] = 2.*(fNeq[5] - fNeq[6]);
	}

}
