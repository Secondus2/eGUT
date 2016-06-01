package glRender;

import java.util.LinkedList;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
//import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLU;

import agent.Agent;
import idynomics.AgentContainer;
import idynomics.NameRef;
import linearAlgebra.Vector;
import shape.Shape;
import surface.Ball;
import surface.Point;
import surface.Rod;
import surface.Surface;


/**
 * Agent mediator, draws agents and plane / cube indication the computational
 * domain
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */
public class AgentMediator implements CommandMediator {
	protected AgentContainer _agents;
	protected Shape _shape;
	private String pigment;
	private float[] rgba;
	private GLU glu = new GLU();
	public float kickback;
	private GL2 gl;

	/**
	 * used to set up the open gl camera
	 */
	@Override
	public float kickback() {
		return 2f * kickback;
	}
	
	/**
	 * assign agent container via the constructor
	 * @param agents
	 */
	public AgentMediator(AgentContainer agents)
	{
		this._agents = agents;
		this._shape = agents.getShape();
	}

	/**
	 * draw the the relevant objects in 3d
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void draw(GLAutoDrawable drawable) {
		gl = drawable.getGL().getGL2();

		/* get the domain lengths to draw itself and scaling */
		double[] domainLengths = _agents.getShape().getDimensionLengths();
		double[] domain = new double[]{ domainLengths[0], domainLengths[1],
				(domainLengths.length > 2 ? domainLengths[2] : 0.0)};
		
		kickback = (float) Math.max(domain[0], Math.max(domain[1], domain[2]));
		
        if(domain[2] != 0.0f)
        	domainCube(drawable,domain);
        
		/* get the surfaces from the agents */
		for ( Agent a : this._agents.getAllLocatedAgents() )
		{

			for ( Surface s : (List<Surface>) (a.isAspect(
					NameRef.surfaceList) ? a.get(NameRef.surfaceList) :
					new LinkedList<Surface>()))
			{
				pigment = a.getString("pigment");
				switch (pigment)
				{
				case "GREEN" :
					  rgba = new float[] {0.1f, 1f, 0.1f};
					  break;
				case "RED" :
					  rgba = new float[] {1f, 0.1f, 0.1f};
					  break;
				case "BLUE" :
					  rgba = new float[] {0.1f, 0.1f, 1f};
					  break;
				case "PURPLE" :
					  rgba = new float[] {1.0f, 0.0f, 1.0f};
					  break;
				case "ORANGE" :
					  rgba = new float[] {1f, 0.6f, 0.1f};
					  break;
				case "BLACK" :
					  rgba = new float[] {0.0f, 0.0f, 0.0f};
					  break;
				default :
					  rgba = new float[] {1f, 1f, 1f};
					  break;
				}
				
				if(s instanceof Ball)
				{
					Ball ball = (Ball) s;
					sphere(drawable, domain, ball._point.getPosition(), 
	        		ball._radius);
				} 
				else if ( s instanceof Rod )
				{
					Rod rod = (Rod) s;
					sphere(drawable, domain, rod._points[0].getPosition(), rod._radius);
					sphere(drawable, domain, rod._points[1].getPosition(), rod._radius);
					
					// TODO cylinder
//					rgba = new float[] {0.1f, 1f, 0.1f};
					cylinder(drawable, domain, rod._points[0].getPosition(),
							rod._points[1].getPosition(), rod._radius);
//					sphere(drawable, domain, Vector.midPoint(
//							rod._points[0].getPosition(),
//							rod._points[1].getPosition()), rod._radius);
				}
			}
		}
		
		/* draw the domain square */
		plane(drawable,domain);
		
	}
	
	/**
	 * draw a scaled sphere positioned relative to the domain
	 * @param drawable
	 * @param domain
	 * @param pos
	 * @param radius
	 */
	private void sphere(GLAutoDrawable drawable, double[] domain, double[] pos, 
			double radius) 
	{
		double[] p = new double[]{ pos[0], pos[1], 
				(pos.length > 2 ? pos[2] : 0.0)};
		
		int i, j;
		final int lats = 16;
		final int longs = 16;
		for(i = 0; i <= lats; i++) 
		{
	  		double lat0 = Math.PI * (-0.5 + (double) (i - 1) / lats);
	  		double z0  = Math.sin(lat0);
	  		double zr0 =  Math.cos(lat0);
	
	  		double lat1 = Math.PI * (-0.5 + (double) i / lats);
	     	double z1 = Math.sin(lat1);
	     	double zr1 = Math.cos(lat1);

	     	gl.glLoadIdentity();
			gl.glTranslated(p[0] - domain[0] * 0.5, p[1] - domain[1] * 0.5, 
					p[2] - domain[2] * 0.5);
	     	gl.glScaled(radius, radius, radius);
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
			gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.1f);
			gl.glColor3f(rgba[0], rgba[1], rgba[2]);
			gl.glBegin(GL2.GL_QUAD_STRIP);
			for(j = 0; j <= longs; j++) 
			{
				double lng = 2 * Math.PI * (double) (j - 1) / longs;
				double x = Math.cos(lng);
				double y = Math.sin(lng);
				
				gl.glNormal3d(x * zr0, y * zr0, z0);
				gl.glVertex3d(x * zr0, y * zr0, z0);
				gl.glNormal3d(x * zr1, y * zr1, z1);
				gl.glVertex3d(x * zr1, y * zr1, z1);
			}
			gl.glEnd();
		}
	
	}
	
	/**
	 * draw gl cylinder... FIXME ok I am lost this class needs additional work
	 * @param drawable
	 * @param domain
	 * @param pos
	 * @param posb
	 * @param radius
	 */
	private void cylinder(GLAutoDrawable drawable, double[] domain, double[] pos, 
			double[] posb, double radius) 
	{
		/* fineness of the cylinder */
		double slices = 16;
		// FIXME think of something more robust
		/*
		 * find the closest distance between the two mass points of the rod
		 * agent and assumes this is the correct length, preventing rods being
		 * stretched out over the entire domain
		 */
		List<double[]> cyclicPoints = _shape.getCyclicPoints(pos);
		double[] c = cyclicPoints.get(0);
		
		/* distance between the two mass points */
		double dist = Vector.distanceEuclid(posb, c);
		double dDist;
		/* 
		 * find the closest 'shadow' point, use the original point if all
		 * alternative point are further.
		 */
		for ( double[] d : cyclicPoints )
		{
			dDist = Vector.distanceEuclid( posb, d);
			if ( dDist < dist)
			{
				c = d;
				dist = dDist;
			}
		}
		
		/* use the middle point to place the cylinder */
		pos = Vector.midPoint(c, posb);
		
		// FIXME by lack of a better way for now draw a 3th sphere in the middle
		sphere(drawable, domain, pos, radius);
		
		// FIXME the following part is in the good direction but still problems
		// with proper rotating and scaling
//		
//		/* distance between the mass points equals the lenght of the cylinder */
//		double l = Vector.distanceEuclid(pos, posb);
//		/* set 3th dimension to 0.0 for 2d simulations */
//		double[] p = new double[]{ pos[0], pos[1], 
//				(pos.length > 2 ? pos[2] : 0.0)};
//		
//		/* start openGL object description */
//		gl.glLoadIdentity();
//		
//		/* correct position for the domain */
//		gl.glTranslated(p[0] - domain[0] * 0.5, p[1] - domain[1] * 0.5, 
//				p[2] - domain[2] * 0.5);
//		
//		/* scale the cylinder */
//     	gl.glScaled(l, radius, radius);
//     	
//     	/* rotate the cylinder */
//     	gl.glRotated(Math.toDegrees(Vector.cosAngle(pos, posb)), 0.0, 0.0, 1.0);
//     	
//     	/* lighting */
//		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
//		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
//		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.1f);
//		
//		/* color */
//		gl.glColor3f(rgba[0], rgba[1], rgba[2]);
//		
//		/* begin quad description: vertexes normals etc */
//		gl.glBegin(GL2.GL_QUAD_STRIP);
//
//		/* iterate through every vertex of the cylinder */
//		double s0, s1, c0, c1;
//		for (int i = 0; (i <= slices); i++)
//        {
//			s0 = Math.sin(Math.PI / (slices/2) * i);
//			s1 = Math.sin(Math.PI / (slices/2) * (i + 1));
//			c0 = Math.cos(Math.PI / (slices/2) * i);
//			c1 = Math.cos(Math.PI / (slices/2) * (i + 1));
//
//			gl.glNormal3d(1, s0, c0);
//			gl.glVertex3d(1, s0, c0);
//			gl.glNormal3d(1, s1, c1);
//			gl.glVertex3d(1, s1, c1);
//			gl.glNormal3d(-1, s0, c0);
//			gl.glVertex3d(-1, s0, c0);
//			gl.glNormal3d(-1, s1, c1);
//			gl.glVertex3d(-1, s1, c1);
//        }
//		
//		/* finalise object */
//		gl.glEnd();
		
	}
	
	/**
	 * draw a domain plane
	 * @param drawable
	 * @param domain
	 */
	private void plane(GLAutoDrawable drawable, double[] domain) 
	{
		rgba = new float[] {0.3f, 0.3f, 0.3f};
		plane(drawable, domain, Vector.zeros(domain), Vector.onesDbl(domain.length), rgba, false);
	}
	
	/**
	 * draw a standard plane positioned and scaled relative to the domain
	 * @param drawable
	 * @param domain
	 * @param origin
	 * @param lengths
	 * @param color
	 * @param lighting
	 */
	private void plane(GLAutoDrawable drawable, double[] domain, double[] origin
			, double[] lengths ,float[] color, boolean lighting)
	{
		gl.glLoadIdentity();
		gl.glTranslated(origin[0], origin[1], origin[2]);
		gl.glScaled(domain[0]*0.5, domain[1]*0.5, domain[2]*0.5);
		gl.glScaled(lengths[0], lengths[1], lengths[2]);
		if (lighting)
		{
	        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, color, 0);
	        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, color, 0);
	        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.1f);
		}
		else
		{
			gl.glDisable(GL2.GL_LIGHTING);
		}
		gl.glBegin(GL2.GL_QUADS);             
		gl.glColor3f(color[0],color[1],color[2]);    
			gl.glVertex3d(-1.0, 1.0, -1.0); 
		    gl.glVertex3d( 1.0, 1.0, -1.0);
		    gl.glVertex3d( 1.0, -1.0, -1.0);  
		    gl.glVertex3d(-1.0, -1.0, -1.0); 
		gl.glEnd();
		if (lighting)
		{
			
		}
		else
		{
			gl.glEnable(GL2.GL_LIGHTING);
		}
	}
	
	/**
	 * draw a alpha blend domain cube (for 3D simulations)
	 * @param drawable
	 * @param domain
	 */
	private void domainCube(GLAutoDrawable drawable, double[] domain) 
	{
		
		gl.glLoadIdentity();
		gl.glEnable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_DEPTH_TEST);
		gl.glScaled(0.5f*domain[0], 0.5f*domain[1], 0.5f*domain[2]);
		
		rgba = new float[] {0.1f, 0.1f, 1f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.1f);
        gl.glColor3f(rgba[0],rgba[1],rgba[2]);
		gl.glBegin(GL2.GL_QUADS);                  // Start Drawing The Cube
		
		gl.glVertex3f( 1.0f, 1.0f,-1.0f);          // Top Right Of The Quad (Top)
		gl.glVertex3f(-1.0f, 1.0f,-1.0f);          // Top Left Of The Quad (Top)
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);          // Bottom Left Of The Quad (Top)
		gl.glVertex3f( 1.0f, 1.0f, 1.0f);          // Bottom Right Of The Quad (Top)
		
		gl.glVertex3f( 1.0f,-1.0f, 1.0f);          // Top Right Of The Quad (Bottom)
		gl.glVertex3f(-1.0f,-1.0f, 1.0f);          // Top Left Of The Quad (Bottom)
		gl.glVertex3f(-1.0f,-1.0f,-1.0f);          // Bottom Left Of The Quad (Bottom)
		gl.glVertex3f( 1.0f,-1.0f,-1.0f);          // Bottom Right Of The Quad (Bottom)

		gl.glVertex3f( 1.0f, 1.0f, 1.0f);          // Top Right Of The Quad (Front)
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);          // Top Left Of The Quad (Front)
		gl.glVertex3f(-1.0f,-1.0f, 1.0f);          // Bottom Left Of The Quad (Front)
		gl.glVertex3f( 1.0f,-1.0f, 1.0f);          // Bottom Right Of The Quad (Front)

		gl.glVertex3f( 1.0f,-1.0f,-1.0f);          // Bottom Left Of The Quad (Back)
		gl.glVertex3f(-1.0f,-1.0f,-1.0f);          // Bottom Right Of The Quad (Back)
		gl.glVertex3f(-1.0f, 1.0f,-1.0f);          // Top Right Of The Quad (Back)
		gl.glVertex3f( 1.0f, 1.0f,-1.0f);          // Top Left Of The Quad (Back)
		
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);          // Top Right Of The Quad (Left)
		gl.glVertex3f(-1.0f, 1.0f,-1.0f);          // Top Left Of The Quad (Left)
		gl.glVertex3f(-1.0f,-1.0f,-1.0f);          // Bottom Left Of The Quad (Left)
		gl.glVertex3f(-1.0f,-1.0f, 1.0f);          // Bottom Right Of The Quad (Left)

        gl.glVertex3f( 1.0f, 1.0f,-1.0f);          // Top Right Of The Quad (Right)
        gl.glVertex3f( 1.0f, 1.0f, 1.0f);          // Top Left Of The Quad (Right)
        gl.glVertex3f( 1.0f,-1.0f, 1.0f);          // Bottom Left Of The Quad (Right)
        gl.glVertex3f( 1.0f,-1.0f,-1.0f);          // Bottom Right Of The Quad (Right)
	    gl.glEnd();                        // Done Drawing The Quad
	    
	    gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_BLEND);
	}

}
