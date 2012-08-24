import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JPopupMenu;

import prefuse.Constants;
import prefuse.controls.ControlAdapter;
import prefuse.controls.Control;
import prefuse.util.ColorLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.action.assignment.*;
//import prefuse.data.ass

public class FinalControlListener extends ControlAdapter implements Control
{
	
	private JPopupMenu jpub;
	private int temp_color;
	
	public void itemEntered(VisualItem item, MouseEvent e)
	{
		if(item instanceof NodeItem)
		{
			String label = ((String) item.get("label"));
			int id = (Integer) item.get("id");
			temp_color  = item.getFillColor();
			item.setFillColor(ColorLib.rgb(10, 10, 10));
			jpub = new JPopupMenu();
			jpub.add("label: " + label);
			jpub.add("Id: " + id);
			jpub.show(e.getComponent(),(int) item.getX(),(int) item.getY());
			
	
		}
	}
	
	@Override 
	public void itemExited(VisualItem item, MouseEvent e)
	{
		if(item instanceof NodeItem)
		{
			jpub.setVisible(false);
			item.setFillColor(temp_color);
		}
	}
	
}
