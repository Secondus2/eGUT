package surface;


public class OrientedCuboidSurface extends CuboidSurface {
	
	private Point[] _apicalFace;
	
	private double[] _apicalNormal;
	
	
	public OrientedCuboidSurface(Point[] points, double[] apicalNormal) {
		super(points);
		this._apicalNormal = apicalNormal;
		this.calculateApicalFace();
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

}