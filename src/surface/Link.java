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
	protected List<Spring> _springs = new LinkedList<Spring>();
	
	protected List<Integer> _arriving = new LinkedList<Integer>(); 

	/**
	 * TODO
	 */
	protected double _snap;
	
	public Link()
	{
	}
	
	/**
	 * Add spring at specific position in case position indexing is used
	 * @param pos
	 * @param spring
	 */
	public void addSpring( int pos, Spring spring )
	{
		this._springs.add(pos, spring);
	}

	public void addSpring( Spring spring )
	{
		this._springs.add(spring);
	}
	
	public List<Spring> getSprings( )
	{
		return _springs;
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
//		
		Body aBody = (Body) a.get(AspectRef.agentBody);
		Body bBody = (Body) b.get(AspectRef.agentBody);
		Body cBody = (Body) c.get(AspectRef.agentBody);
		Double linkerStifness = (double) b.getOr( 
				AspectRef.linkerStifness, 100000.0);
		/* FIXME placeholder default function */
		Expression springFun = (Expression) b.getOr( 
				AspectRef.filialLinker, new Expression( 
						"stiffness * dif * dif * 1000" ));

		Point[] points = new Point[] { aBody.getPoints().get(0), 
				bBody.getPoints().get(0), cBody.getPoints().get(0) };
		
		Spring spring = new TorsionSpring(linkerStifness, points, springFun,
				3.14159265359);
		link.addSpring(spring);

	}
	
	public static void linLink(Agent a, Agent b)
	{
		Body momBody = (Body) a.get(AspectRef.agentBody);
		Body daughterBody = (Body) b.get(AspectRef.agentBody);
		Link link = new Link();
		link(a, b, new Link());
		link.addMember(0, a);
		link.addMember(1, b);
		momBody.addLink(link);
		daughterBody.addLink(link);
	}
	
	public static void link(Agent a, Agent b, Link link)
	{
		if(a == null || b == null)
			return;
		Body momBody = (Body) a.get(AspectRef.agentBody);
		Body daughterBody = (Body) b.get(AspectRef.agentBody);
		
//		if( !link._members.isEmpty())
//		{
//			for( Link l : daughterBody.getLinks() )
//			{
//				if( l._members.size() == 2 && l._members.contains(a) && l._members.contains(b))
//				{
//					daughterBody.unLink(l);
//					daughterBody.addLink(link);
//				}
//			}
//		}
//		
		Double linkerStifness = (double) a.getOr( 
				AspectRef.linkerStifness, 10.0);
		/* FIXME placeholder default function */
		Expression springFun = (Expression) a.getOr( 
				AspectRef.filialLinker, new Expression( 
						"stiffness * dh * dh * 1000000.0 )" ));

		Point[] points = new Point[] { momBody.getPoints().get(0), 
				daughterBody.getPoints().get(0) };
		
		Spring spring = new LinearSpring(linkerStifness, points, springFun,
				a.getDouble(AspectRef.bodyRadius) + 
				b.getDouble(AspectRef.bodyRadius));
		link.addSpring(spring);
	}
	
	
	public void update(int pos, Point point)
	{
		for(Spring s : _springs)
			s.setPoint(pos, point);
	}
	
	public void update()
	{
		if( !this._arriving.isEmpty() )
		{
			for (int i = 0; i< this._arriving.size(); i++) 
			{
				AspectInterface m = Idynomics.simulator.findAgent( 
						Integer.valueOf( this._arriving.get(i)) );
				if( m != null )
				{
					this._members.add( i,  m);
				}
				else
				{
					Log.out("unkown agent " +i);
				}
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
//			Collection<Element> srpingNodes =
//			XmlHandler.getAllSubChild(xmlElement, XmlRef.spring);
//			for (Element e : srpingNodes) 
//			{
//				String type = e.getAttribute(XmlRef.typeAttribute);
//				this._springs.add((Spring) Instance.getNew(type, null) );
//			}
			
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
