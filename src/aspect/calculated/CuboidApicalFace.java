package aspect.calculated;

import java.util.List;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import aspect.Calculated;
import referenceLibrary.AspectRef;
import surface.Point;

public class CuboidApicalFace extends Calculated {
	
	String ORIENTATION = AspectRef.cuboidOrientation;
	String BODY = AspectRef.agentBody;
	private Point[] _apicalFace;
	private Point[] corners;
	
	@Override
	public Object get(AspectInterface aspectOwner)
	{
		double[] orientation = (double[]) aspectOwner.getValue(ORIENTATION);
		Body body = (Body) ((Agent)aspectOwner).get(BODY);
		List<Point> pointsList = body.getPoints();
		this.corners = new Point[pointsList.size()];
		corners = pointsList.toArray(corners);
		calculateApicalFace(orientation);
		
		
		
		return _apicalFace;
	}
	
	public void calculateApicalFace(double[] orientation)
	{
		this._apicalFace = new Point[2];
		for (int i = 0; i < orientation.length; i++) {
			//If the apical normal is negative, it is "facing downwards", and 
			//the bottom corner of the cell is part of its apical face.
			if (orientation[i] == -1.0)
			{
				if (corners[0].getPosition()[i] < 
						corners[1].getPosition()[i])
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
			if (orientation[i] == 1.0)
			{
				if (corners[0].getPosition()[i] < 
						corners[1].getPosition()[i])
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
		this._apicalFace[0] = corners[1];
		Point partnerPoint = (Point) corners[0].copy();
		double [] partnerPointPosition = partnerPoint.getPosition();
		partnerPointPosition[i] = corners[1].getPosition()[i];
		this._apicalFace[1] = partnerPoint;
	}
	
	private void shiftPoint1ToFace(int i)
	{
		this._apicalFace[0] = corners[0];
		Point partnerPoint = (Point) corners[1].copy();
		double [] partnerPointPosition = partnerPoint.getPosition();
		partnerPointPosition[i] = corners[0].getPosition()[i];
		this._apicalFace[1] = partnerPoint;
	}

}
