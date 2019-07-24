package surface;

import surface.Surface.Type;

/**
 * An Oriented Cuboid is a cuboid surface with only one face that interacts
 * with other agents. In the case of epithelial cells, this is the "apical face"
 * which faces into the lumen.
 * 
 * @author trf896
 *
 */
public class OrientedCuboid extends Cuboid {
	
	private Point[] _apicalFace;
	
	private double[] _apicalNormal;
	
	
	public OrientedCuboid(Point[] points, double[] apicalNormal) {
		super(points);
		this._apicalNormal = apicalNormal;
		this.calculateApicalFace();
	}
	
	public OrientedCuboid(Point[] points) {
		super(points);
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
				if (this._points[0].getPosition()[i] < 
						this._points[1].getPosition()[i])
				{
					shiftPoint1ToFace(i);
				}
				else
				{
					shiftPoint0ToFace(i);
				}
			}
			//If the apical normal is positive, it is "facing upwards", and 
			//the top corner of the cell is part of its apical face.
			if (this._apicalNormal[i] == 1.0)
			{
				if (this._points[0].getPosition()[i] < 
						this._points[1].getPosition()[i])
				{
					shiftPoint0ToFace(i);
				}
				else
				{
					shiftPoint1ToFace(i);
				}
			}
		}
	}
	
	private void shiftPoint0ToFace (int i)
	{
		this._apicalFace[0] = this._points[1];
		Point partnerPoint = (Point) this._points[0].copy();
		double [] partnerPointPosition = partnerPoint.getPosition();
		partnerPointPosition[i] = this._points[1].getPosition()[i];
		this._apicalFace[1] = partnerPoint;
	}
	
	private void shiftPoint1ToFace(int i)
	{
		this._apicalFace[0] = this._points[0];
		Point partnerPoint = (Point) this._points[1].copy();
		double [] partnerPointPosition = partnerPoint.getPosition();
		partnerPointPosition[i] = this._points[0].getPosition()[i];
		this._apicalFace[1] = partnerPoint;
	}

	@Override
	public Type type() {
		return Surface.Type.ORIENTEDCUBOID;
	}
	
	public Point[] getApicalFace() 
	{
		return this._apicalFace;
	}
	
	public double[] getNormal ()
	{
		return this._apicalNormal;
	}
	
	public void setNormal (double[] normal)
	{
		this._apicalNormal = normal;
		this.calculateApicalFace();
	}

}
