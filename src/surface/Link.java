package surface;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;

import agent.Agent;
import agent.Body;
import aspect.AspectInterface;
import dataIO.Log;
import dataIO.XmlHandler;
import expression.Expression;
import idynomics.Idynomics;
import instantiable.Instantiable;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import utility.Helper;

/**
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Link implements Instantiable, Settable  {

	protected List<AspectInterface> _members = new LinkedList<AspectInterface>();
	/**
	 * 
	 */
	protected Spring _spring;
	
	protected List<Integer> _arriving = new LinkedList<Integer>(); 

	/**
	 * TODO
	 */
	protected double _snap;
	
	public Link()
	{
	}


	public void setSpring( Spring spring )
	{
		this._spring = spring;
	}
	
	public Spring getSpring( )
	{
		return _spring;
	}
	
	public void addMember(  int pos, AspectInterface member )
	{
		if( member == null )
			System.out.print("wow");
		if( this._members.size() > pos)
			this._members.remove(pos);
		this._members.add(pos, member);
	}
	
	public List<AspectInterface> getMembers()
	{
		return this._members;
	}

	public static void torLink(Agent a, Agent b, Agent c)
	{
		Link link = new Link();
		torLink(a,b,c, link);
		link.addMember(0, a);
		link.addMember(1, b);
		link.addMember(2, c);
		((Body) b.get(AspectRef.agentBody)).addLink(link);
	}
	
	public static void torLink(Agent a, Agent b, Agent c, Link link)
	{
//		if(a == null || b == null || c == null)
//			return;

		Body aBody = (Body) a.get(AspectRef.agentBody);
		Body bBody = (Body) b.get(AspectRef.agentBody);
		Body cBody = (Body) c.get(AspectRef.agentBody);
		Double linkerStifness = (double) b.getOr( 
				AspectRef.torsionStifness, 100000.0);
		/* FIXME placeholder default function */
		Expression springFun = (Expression) b.getOr( 
				AspectRef.filialLinker, new Expression( 
						"stiffness * dif * dif * 1000" ));

		Point[] points = null;
		if( a != b && b != c)
		{
			/* b must be coccoid */
		points = new Point[] { aBody.getClosePoint(bBody.getCenter()), 
				bBody.getPoints().get(0), cBody.getClosePoint(bBody.getCenter()) };
		}
		else if ( a == b)
		{
			/* b is rod with a */
			points = new Point[] { aBody.getFurthesPoint(cBody.getCenter()), 
					bBody.getClosePoint(cBody.getCenter()), cBody.getClosePoint(bBody.getCenter())};
		}
		else
		{
			/* b is rod with c */
			points = new Point[] { aBody.getClosePoint(bBody.getCenter()), 
					bBody.getClosePoint(aBody.getCenter()), cBody.getFurthesPoint(aBody.getCenter())};
		}
		
		Spring spring = new TorsionSpring(linkerStifness, points, springFun,
				3.14159265359);
		link.setSpring(spring);

	}
	
	public static void linLink(Agent a, Agent b)
	{
		Body momBody = (Body) a.get(AspectRef.agentBody);
		Body daughterBody = (Body) b.get(AspectRef.agentBody);
		Link link = new Link();
		link(a, b, link);
		link.addMember(0, a);
		link.addMember(1, b);
		momBody.addLink(link);
		daughterBody.addLink(link); // to keep consistent with xml out make this a copy
	}
	
	public static void link(Agent a, Agent b, Link link)
	{
		if(a == null || b == null)
			return;
		Body momBody = (Body) a.get(AspectRef.agentBody);
		Body daughterBody = (Body) b.get(AspectRef.agentBody);
		
		if( !link._members.isEmpty())
		{
//			for( Link l : daughterBody.getLinks() )
//			{
//				if( l._members.size() == 2 && l._members.contains(a) && l._members.contains(b))
//				{
//					daughterBody.unLink(l);
//					daughterBody.addLink(link);
//				}
//			}
		}

		Double linkerStifness = (double) a.getOr( 
				AspectRef.linearStifness, 1000000.0);
		/* FIXME placeholder default function */
		Expression springFun = (Expression) a.getOr( 
				AspectRef.filialLinker, new Expression( 
						"stiffness * dh * 10.0 )" ));

		Point[] points = new Point[] { momBody.getClosePoint(
				daughterBody.getCenter()), 
				daughterBody.getClosePoint(
				momBody.getCenter()) };
		
		double restlength;
		if(a != b )
			restlength = a.getDouble(AspectRef.bodyRadius) + 
			b.getDouble(AspectRef.bodyRadius);
		else
			{
			restlength = 1;
			}
		Spring spring = new LinearSpring(linkerStifness, points, springFun,
				restlength);
		
		link.setSpring(spring);
	}
	
	public void update()
	{
		if( getMembers().size() == 2 && 
				getMembers().get(0) != getMembers().get(1)
				)
		{
			getSpring().setRestValue( getMembers().get(0).getDouble(AspectRef.bodyRadius)
					+  getMembers().get(1).getDouble(AspectRef.bodyRadius));
		}
		if( this._spring instanceof LinearSpring)
		{
			boolean c = false, d = false;
			LinearSpring s = (LinearSpring) this._spring;
			for( AspectInterface member : getMembers())
			{
				Body b = (Body) member.getValue(AspectRef.agentBody);
				if ( b.getPoints().contains(s._a))
					c = true;
				if ( b.getPoints().contains(s._b))
					d = true;
			}
			if( !c || !d)
			{
				System.out.println("mismatch");
			}
		}
	}
	
	public void setPoint(int pos, Point point, boolean tempDuplicate)
	{
		this._spring.setPoint(pos, point, tempDuplicate);
	}
	
	public void initiate()
	{
		if( !this._arriving.isEmpty() )
		{
			for (int i = 0; i< this._arriving.size(); i++) 
			{
				AspectInterface m = Idynomics.simulator.findAgent( 
						Integer.valueOf( this._arriving.get(i)) );
				if( m != null )
					this._members.add( i,  m);
				else
					Log.out("unkown agent " +i+ " in " + 
							this.getClass().getSimpleName());
			}
			if( this._members.size() == 2)
			{
				link((Agent) this._members.get(0), 
						(Agent) this._members.get(1),this);
			}
			else if ( this._members.size() == 3)
			{
				torLink((Agent) this._members.get(0), 
						(Agent) this._members.get(1),
						(Agent) this._members.get(2),this);
			}
			this._arriving.clear();
		}
	}

	@Override
	public Module getModule() {
		Module modelNode = new Module(XmlRef.link, this);
		modelNode.setRequirements(Requirements.ZERO_TO_FEW);
//		
//		for (Spring p : this._springs )
//			modelNode.add(p.getModule() );

		for (int i = 0; i < this._members.size(); i++)
		{
			Module mem = new Module(XmlRef.member, this);
			mem.add(new Attribute(XmlRef.identity, 
					String.valueOf(((Agent) this._members.get(i)).identity()), null, false));
			modelNode.add(mem);
		}
		return modelNode;
	}

	@Override
	public String defaultXmlTag() {
		return XmlRef.link;
	}

	@Override
	public void setParent(Settable parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Settable getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void instantiate(Element xmlElement, Settable parent) 
	{
		if( !Helper.isNullOrEmpty( xmlElement ))
		{			
			/* find member agents and add them to the member list. */
			Collection<Element> memberNodes =
			XmlHandler.getAllSubChild(xmlElement, XmlRef.member);
			for (Element e : memberNodes) 
			{
				this._arriving.add( 
						Integer.valueOf(e.getAttribute(XmlRef.identity) ) );
			}
		
		}
	}
}
