package surface;

import agent.Body;
import generalInterfaces.HasBoundingBox;
import shape.Shape;
import surface.BoundingBox;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Tim Foster @Secondus2 (trf896@student.bham.ac.uk) 
 */
public class CuboidSurface extends Surface implements HasBoundingBox {
	
    public Point[] _points;
	
    
    private Point[] _apicalFace;

    
    public double _height;


	private double[] _apicalNormal;
	
    
    public CuboidSurface(Point[] points)
    {
    	this._points = points;
    }

	public CuboidSurface(Point pointA, Point pointB)
	{
		this._points = new Point[] { pointA , pointB };
    }
	
	public CuboidSurface(double[] pointA, double[] pointB)
	{
		this._points = new Point[] { new Point(pointA), new Point(pointB)};
	}
	
	public CuboidSurface(CuboidSurface cuboid) 
	{
		this._points = new Point[] {(Point) cuboid._points[0].copy(), 
				(Point) cuboid._points[1].copy()};
	}


	public CuboidSurface(Point[] points, double[] apicalNormal) {
		this._points = points;
		this._apicalNormal = apicalNormal;
		this.calculateApicalFace();
	}

	public Type type() {
		return Surface.Type.CUBOID;
	}


	//This will calculate the two corners of the apical face of a cell. In 2D,
	//these will be two corners on a line (describing an edge). In 3D, they will
	//be two corners of a square or rectangle.
	public void calculateApicalFace()
	{
		this._apicalFace = new Point[2];
		for (int i = 0; i < this._apicalNormal.length; i++) {
			//If the apical normal is negative, it is "facing downwards", and 
			//the bottom corner of the cell is part of its apical face.
			if (this._apicalNormal[i] == -1.0)
			{
				this._apicalFace[0] = this._points[0];
				Point partnerPoint = (Point) this._points[1].copy();
				double [] partnerPointPosition = partnerPoint.getPosition();
				partnerPointPosition[i] = this._points[0].getPosition()[i];
				this._apicalFace[1] = partnerPoint;
			}
			//If the apical normal is positive, it is "facing upwards", and 
			//the top corner of the cell is part of its apical face.
			if (this._apicalNormal[i] == 1.0)
			{
				this._apicalFace[0] = this._points[1];
				Point partnerPoint = (Point) this._points[0].copy();
				double [] partnerPointPosition = partnerPoint.getPosition();
				partnerPointPosition[i] = this._points[1].getPosition()[i];
				this._apicalFace[1] = partnerPoint;
			}
		}
	}

	public Point[] getApicalFace() {
		return this._apicalFace;
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
	

	protected BoundingBox boundingBox = new BoundingBox();
	
	 
	public BoundingBox boundingBox(double margin, Shape shape)
	{
		double[] corner1 = _points[0].getPosition();
		double[] corner2 = _points[1].getPosition();
		if (corner1[0] > corner2[0]) {
			for (int i = 0; i < corner1.length; i++) {
				corner1[i] += margin;
			}
			
			for (int i = 0; i < corner2.length; i++) {
				corner2[i] -= margin;
			}

			return boundingBox.get(corner2, corner1, true);
		}
		
		else {
			
			for (int i = 0; i < corner1.length; i++) {
				corner2[i] += margin;
			}
			
			for (int i = 0; i < corner2.length; i++) {
				corner1[i] -= margin;
			}
			
			return boundingBox.get(corner1, corner2, true);
		}
	
	}

	public BoundingBox boundingBox(Shape shape)
	{
		//Temporary fix while I work out importance of HasBoundingBox
		return this.boundingBox();
	}
	
	public BoundingBox boundingBox() {
			return boundingBox.get(this._points[0].getPosition(), 
					this._points[1].getPosition(), true);
	}
}