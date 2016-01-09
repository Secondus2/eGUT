package test;

import java.util.Arrays;
import java.util.Scanner;

import javax.management.RuntimeErrorException;

import boundary.Boundary;
import grid.CartesianGrid;
import grid.CylindricalGrid;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import grid.SphericalGrid;
import idynomics.Compartment.BoundarySide;
import linearAlgebra.Vector;
import test.plotting.PolarGridPlot3D;

public class PolarGridTest {
	public static Scanner keyboard = new Scanner(System.in);

	public static void main(String[] args) {
		
		// choose array type here
//		SphericalGrid gridp = new SphericalGrid(
//				new int[]{200,360,180},new double[]{1,1,1});
		
	    CylindricalGrid gridp = new CylindricalGrid(
				new int[]{4,360,1},new double[]{1,1,1});
		
//	    CartesianGrid gridp = new CartesianGrid(new int[]{100,100,4000},1);
		
		// create an array
		ArrayType type=ArrayType.CONCN;
		gridp.newArray(type, 0);
		
		// add boundaries
		for (BoundarySide bs : BoundarySide.values()){
			gridp.addBoundary(bs, Boundary.constantDirichlet(0.0));
		}
		
		long t_start = System.currentTimeMillis();
		
		/**********************************************************************/
		/*************** uncomment to test memory usage of grid ***************/
		/**********************************************************************/
		
	    long mem_start = (Runtime.getRuntime().totalMemory() 
	    		- Runtime.getRuntime().freeMemory());
	    		
		System.out.println("time needed to create grid: "
				+(System.currentTimeMillis()-t_start)
				+" ms");
		
		System.out.println("Memory usage of grid array: "+
				((Runtime.getRuntime().totalMemory() 
						- Runtime.getRuntime().freeMemory()
				)-mem_start)/1e6 + " MB");
		System.out.println("number of grid elements: "+gridp.length());
		
//		System.out.println(gridp.arrayAsText(type));
//		System.out.println();
		
		/**********************************************************************/
		/**************** uncomment to test iterator's speed  *****************/
		/**********************************************************************/
		
		t_start = System.currentTimeMillis();
		
		int[] current;
		for ( current = gridp.resetIterator(); gridp.isIteratorValid();
				current = gridp.iteratorNext())
		{
			
			/****************** uncomment to test iterator ********************/			
			
//			System.out.println("current: "+Arrays.toString(current)+
//			"\torigin: "+Arrays.toString(
//				gridp.getVoxelOrigin(Vector.copy(current)))
//			+"\tcoord: "+Arrays.toString(
//				gridp.getCoords(Vector.copy(
//				gridp.getVoxelOrigin(Vector.copy(current))))));
//
//			System.out.println("current: "+Arrays.toString(current)
//				+"\tindex: "+gridp.coord2idx(current)
//			+"\tcoord: "+Arrays.toString(
//				gridp.idx2coord(gridp.coord2idx(current),null)));
//			
//			System.out.println();
			
			/******* uncomment to test iterator and neighborhood iterator *****/
		
//			System.out.println("grid size: "
//						+Arrays.toString(gridp.getNumVoxels()));
//			int[] nbh;
//			for ( current = gridp.resetIterator(); gridp.isIteratorValid();
//					  current = gridp.iteratorNext())
//			{
//				System.out.println("current: "+Arrays.toString(current));
//				for ( nbh = gridp.resetNbhIterator(); 
//						gridp.isNbhIteratorValid(); 
//							nbh = gridp.nbhIteratorNext() )
//				{
//					System.out.println("\tnbh: "+Arrays.toString(nbh));
//				}
//			}
//			System.out.println();
//			int[] coords=gridp.getCoords(
//								gridp.getVoxelOrigin(new int[]{3,41,7}));
//			System.out.println(coords[0]+" "+coords[1]+" "+coords[2]);
		}
		
		System.out.println("time needed to iterate through grid: "
			+(System.currentTimeMillis()-t_start)+" ms");	
		
		/**********************************************************************/
		/************ uncomment to create graphical representation ************/
		/**********************************************************************/
		
		PolarGridPlot3D plot = new PolarGridPlot3D(gridp,true,true);
		System.out.println("press enter to start iterator");
		keyboard.nextLine();
        plot.startIterator();  // manual step
//        plot.runIterator();      // running automatically
        keyboard.close();
	}

}
