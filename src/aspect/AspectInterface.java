package aspect;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dataIO.Log;
import dataIO.ObjectFactory;
import dataIO.Log.Tier;
import referenceLibrary.XmlRef;

/**
 * The aspect interface is implemented by classes with an aspect registry,
 * allows for direct interaction with the aspect registry and easy loading of 
 * aspects from xml.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public abstract interface AspectInterface
{
	/**
	 * \brief TODO
	 * @return
	 */
	public AspectReg reg();	
	
	/**
	 * \brief Load all aspects from xmlNode into anything that implements the
	 * StateObject interface.
	 * 
	 * @param aspectReg
	 * @param xmlNode
	 */
	public default void loadAspects(Node xmlNode)
	{
		if (xmlNode != null)
		{
			Element e = (Element) xmlNode;
			AspectReg aspectReg = (AspectReg) reg();
			String  key;
			NodeList stateNodes = e.getElementsByTagName( XmlRef.aspect );
			for (int j = 0; j < stateNodes.getLength(); j++) 
			{
				Element s = (Element) stateNodes.item(j);
				key = s.getAttribute( XmlRef.nameAttribute );
				aspectReg.add( key, ObjectFactory.loadObject( s ) );
				Log.out(Tier.BULK, "Aspects loaded for \""+key+"\"");
			}
		}
	}
	
	
	/**************************************************************************
	 * Quick getter methods, making life easy and code readable, expand as new
	 * objects are implemented in the aspect interface
	 * NOTE: there may be more efficient ways of doing this, check
	 */
	
	/**
	 * check for local existence of aspect
	 * @param aspect
	 * @return
	 */
	default boolean isLocalAspect(String aspect)
	{
		return this.reg().isLocalAspect(aspect);
	}
	
	/**
	 * check for global existence of aspect
	 * @param aspect
	 * @return
	 */
	default boolean isAspect(String aspect)
	{
		return this.reg().isGlobalAspect(aspect);
	}
	
	/**
	 * 
	 * @param key
	 * @param aspect
	 */
	public default void set(String key, Object aspect)
	{
		this.reg().set(key, aspect);
	}
	
	/**
	 * get value or use default if the aspect is not set
	 * @param aspect
	 * @param defaultValue
	 * @return
	 */
	public default Object getOr(String aspect, Object defaultValue)
	{
		return ( this.isAspect(aspect) ? 
				this.getValue(aspect) : defaultValue );
	}
	
	/**
	 * Getting raw aspect object
	 * @param aspect
	 * @return
	 */
	public default Object getValue(String aspect)
	{
		return this.reg().getValue(this, aspect);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Double getDouble(String aspect)
	{
		return (Double) this.reg().getValue(this, aspect);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Double[] getDoubleA(String aspect)
	{
		return (Double[]) this.reg().getValue(this, aspect);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default String getString(String aspect)
	{
		return (String) this.reg().getValue(this, aspect);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default String[] getStringA(String aspect)
	{
		return (String[]) this.reg().getValue(this, aspect);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Integer getInt(String aspect)
	{
		return (Integer) this.reg().getValue(this, aspect);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Integer[] getIntA(String aspect)
	{
		return (Integer[]) this.reg().getValue(this, aspect);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null.
	 * NOTE: floats are not used within iDynoMiCS, yet available to combine with
	 * external packages / models that require floats.
	 * @param aspect
	 * @return
	 */
	public default Float getFloat(String aspect)
	{
		return (Float) this.reg().getValue(this, aspect);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null.
	 * NOTE: floats are not used within iDynoMiCS, yet available to combine with
	 * external packages / models that require floats.
	 * @param aspect
	 * @return
	 */
	public default Float[] getFloatA(String aspect)
	{
		return (Float[]) this.reg().getValue(this, aspect);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Boolean getBoolean(String aspect)
	{
		return (Boolean) this.reg().getValue(this, aspect);
	}
	
	/**
	 * check, cast and return aspect, return null if the aspect does not exist
	 * or is equal to null
	 * @param aspect
	 * @return
	 */
	public default Boolean[] getBooleanA(String aspect)
	{
		return (Boolean[]) this.reg().getValue(this, aspect);
	}

}
