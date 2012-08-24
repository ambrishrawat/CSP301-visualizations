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
	
	
	public void itemEntered(VisualItem item, MouseEvent e)
	{
		if(item instanceof NodeItem)
		{
			String label = ((String) item.get("label"));
			int id = (Integer) item.get("id");
			
			//DataSizeAction size = new DataSizeAction("graph.nodes","degree");
			//double s = size.getSize(item);
			
			JPopupMenu jpub = new JPopupMenu();
			jpub.add("label: " + label);
			jpub.add("Id: " + id);
			//jpub.add("Size: " + size);
			jpub.show(e.getComponent(),(int) item.getX(),(int) item.getY());
			
	
		}
	}
	
	
}
