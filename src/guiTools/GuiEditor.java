package guiTools;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner; // to be implemented
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import dataIO.XmlLabel;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;


/**
 * tabbed interface that allows the user to change parameters of a simulator
 * allows the creation of new simulators or loading from file.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class GuiEditor
{
	/**
	 * Hashmap of all gui TextAreas associated with their ModelAttribute
	 */
	private static HashMap<ModelAttribute,JTextArea> _attributes = 
			new HashMap<ModelAttribute,JTextArea>();
	
	/**
	 * Hashmap of all gui TextAreas associated with their ModelAttribute
	 */
	@SuppressWarnings("rawtypes")
	private static HashMap<ModelAttribute,JComboBox> _attributeSelectors = 
			new HashMap<ModelAttribute,JComboBox>();
	
	/**
	 * Obtain all attribute textarea values and set them in the modelAttribute
	 * objects.
	 */
	public static void setAttributes()
	{
		for ( ModelAttribute a : _attributes.keySet())
			a.value = _attributes.get(a).getText();
		
		for ( ModelAttribute a : _attributeSelectors.keySet())
			a.value = (String) _attributeSelectors.get(a).getSelectedItem();
	}
	
	/*
	 * The JComponent set in the gui
	 */
	public static void addComponent(ModelNode node, JComponent parent) {
		
		JTabbedPane tabs = GuiComponent.newPane();
		JPanel component = new JPanel();
		tabs.addTab(node.tag, component);
		component.setLayout(new WrapLayout(FlowLayout.LEFT, 0, 0));
		JPanel attr = new JPanel();
		attr.setLayout(new WrapLayout(FlowLayout.LEFT, 5, 5));
		
		/* loop trough child constructors */
		for(NodeConstructor c : node.childConstructors.keySet())
		{
			/* add child to interface if exactly one is required and the node
			 * is not present yet */
			if(node.childConstructors.get(c) == Requirements.EXACTLY_ONE && 
					node.getChildNodes(c.defaultXmlTag()).isEmpty())
			{
				NodeConstructor newNode = c.newBlank();
				node.add(newNode.getNode());
				node.add(newNode);
			}
			else if(node.childConstructors.get(c) == Requirements.EXACTLY_ONE)
			{
				// required unique childNode is already present: do nothing
			}
			else
			{
				/* add button for optional childnode(s) */
				attr.add(GuiComponent.actionButton(c.defaultXmlTag(), 
						new JButton("add"), new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent event)
					{
						NodeConstructor newNode = c.newBlank();
						node.add(newNode.getNode());
						addComponent(newNode.getNode(), component);
						node.add(newNode);
					}
				}
				));
			}
		}
		
		/* add textareas for this ModelNode's attributes */
		for(ModelAttribute a : node.attributes)
		{
			if ( a.value == null &&  a.options == null )
			{
				/* input field */
				JTextArea input = new JTextArea();
				input.setEditable(a.editable);
				if (! a.editable)
					input.setForeground(Color.gray);
				attr.add(GuiComponent.inputPanel(a.tag, input));
				_attributes.put(a, input);
			}
			else if ( a.options == null && a.value.length() < 60)
			{
				/* input field */
				JTextArea input = new JTextArea();
				input.setText(a.value);
				input.setEditable(a.editable);
				if (! a.editable)
					input.setForeground(Color.gray);
				attr.add(GuiComponent.inputPanel(a.tag, input));
				_attributes.put(a, input);
			}
			else if ( a.options == null )
			{
				/* input field */
				JTextArea input = new JTextArea();
				input.setText(a.value);
				input.setEditable(a.editable);
				if (! a.editable)
					input.setForeground(Color.gray);
				attr.add(GuiComponent.inputPanelLarge(a.tag, input));
				_attributes.put(a, input);
			}
			else
			{
				/* options box */
				JComboBox<String> input = new JComboBox<String>(a.options);
				input.setSelectedItem(a.value);
				input.setEditable(a.editable);
				attr.add(GuiComponent.selectPanel(a.tag, input));
				_attributeSelectors.put(a, input);
			}
		}
		component.add(attr);
		
		/* placement of this ModelNode in the gui */
		if(node.tag == XmlLabel.speciesLibrary  )
		{
			/* exception for speciesLib add component as tab next to the
			 * parent tab (simulation) */
			GuiComponent.addTab((JTabbedPane) parent.getParent().getParent(), 
					node.tag , tabs, "");
		}
		else if( node.tag == XmlLabel.compartment )
		{
			/* exception for compartments add component as tab next to the
			 * parent tab (simulation) */
			GuiComponent.addTab((JTabbedPane) parent.getParent().getParent(), 
					node.tag + " " + node.title, tabs, "");
		} 
		else if(node.tag == XmlLabel.agents || node.tag == XmlLabel.solutes ||
				node.tag == XmlLabel.processManagers )
		{
			GuiComponent.addTab((JTabbedPane) parent.getParent(), 
					node.tag, tabs, "");
		}
		else if(node.tag == XmlLabel.aspect || node.tag == XmlLabel.solute )
		{
			GuiComponent.addTab((JTabbedPane) parent.getParent(), 
					node.tag + " " + node.title, tabs, "");
		}
		else if( node.tag == XmlLabel.shapeDimension || node.tag == XmlLabel.point ||
				node.tag == XmlLabel.stoichiometry || node.tag == XmlLabel.constant ||
				node.tag == XmlLabel.speciesModule )
		{
			parent.add(component, null);
			parent.revalidate();
		} 
		else if( node.requirement.maxOne() && parent != GuiMain.tabbedPane )
		{
			/* exactly one: append this component to the parent component */
			parent.add(component, null);
			parent.revalidate();
		}
		else if( node.requirement == Requirements.ZERO_TO_MANY)
		{
			/* species, agents, TODO: changes to spinner */
			GuiComponent.addTab((JTabbedPane) parent.getParent(), 
					node.tag + " " + node.title, tabs, "");
		} 
		else
		{
			/* else add component as Child tab of parent */
			GuiComponent.addTab((JTabbedPane) parent, node.tag, tabs, "");
		}
		
		/* add childnodes of this component to the gui */
		for(ModelNode n : node.childNodes)
			addComponent(n, component);
	}
}
