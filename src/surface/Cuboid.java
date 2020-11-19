package surface;

import java.util.List;

import org.w3c.dom.Element;

import generalInterfaces.HasBoundingBox;
import settable.Module;
import shape.Shape;
import utility.Helper;
import utility.StandardizedImportMethods;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Tim Foster @Secondus2 (trf896@student.bham.ac.uk) 
 */
public class Cuboid extends Surface implements HasBoundingBox {
	
    public Point[] _points;

    public double _height;

    public Cuboid(List<Point> points)
    {
    	Point[] pointsArray = (Point[]) points.toArray();
    	this._points = pointsArray;
    }

	/*
	 * FIXME rod bounding box is broken since it receives periodic point
	 * positions but cannot correct it's bounding box for it
	 */
    private BoundingBox boundingBox = new BoundingBox();
    
    public Cuboid(Point[] points)
    {
    	this._points = points;
    }

	public Cuboid(Point pointA, Point pointB)
	{
		this._points = new Point[] { pointA , pointB };
    }
	
	public Cuboid(double[] pointA, double[] pointB)
	{
		this._points = new Point[] { new Point(pointA), new Point(pointB)};
	}
	
	public Cuboid(Cuboid cuboid) 
	{
		this._points = new Point[] {(Point) cuboid._points[0].copy(), 
				(Point) cuboid._points[1].copy()};
	}
	
	public Cuboid(Element xmlElem)
	{
		if( !Helper.isNullOrEmpty( xmlElem ))
		{
			this._points = StandardizedImportMethods.
					pointImport(xmlElem, this, 2);
		}
	}


	public Type type() {
		return Surface.Type.CUBOID;
	}

	
	public double[][] pointMatrix(Shape shape)
	{
		double[][] p = new double[_points.length][_points[0].nDim()];
		for(int i = 0; i < _points.length; i++)
			if ( i < 1)
				p[i] = _points[i].getPosition();
			else
				p[i] = shape.getNearestShadowPoint( _points[i].getPosition(), 
						p[i-1]);
		return p;
	}
	
	@Override
	public int dimensions() 
	{
		return this._points[0].nDim();
	}
	



	public BoundingBox boundingBox(double margin, Shape shape)
	{
		double[] corner1 = _points[0].getPosition();
		double[] corner2 = _points[1].getPosition();
		if (corner1[0] > corner2[0]) 
		{
			for (int i = 0; i < corner1.length; i++) {
				corner1[i] += margin;
			}
			
			for (int i = 0; i < corner2.length; i++) {
				corner2[i] -= margin;
			}

			return boundingBox.get(corner2, corner1);
		}
		
		else {
			
			for (int i = 0; i < corner1.length; i++) {
				corner2[i] += margin;
			}
			
			for (int i = 0; i < corner2.length; i++) {
				corner1[i] -= margin;
			}
			
			return boundingBox.get(corner1, corner2);
		}
	
	}

	public BoundingBox boundingBox(Shape shape)
	{
		//Temporary fix while I work out importance of HasBoundingBox
		return this.boundingBox();
	}
	
	public BoundingBox boundingBox() {
		double[] corner1 = _points[0].getPosition();
		double[] corner2 = _points[1].getPosition();
		if (corner1[0] > corner2[0]) {
	
			return boundingBox.get(corner2, corner1);
				
		}

		else {
	
			return boundingBox.get(corner1, corner2);
		}
	}
	
	public Module appendToModule(Module modelNode) 
	{
		for (Point p : _points )
			modelNode.add(p.getModule() );
		
		return modelNode;
	}
}