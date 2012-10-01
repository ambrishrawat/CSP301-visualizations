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

public class FinalControlListener extends ControlAdapter implements Control
{
	
	private JPopupMenu jpub;
	
	public void itemEntered(VisualItem item, MouseEvent e)
	{
		//if(item instanceof NodeItem)
		//{
		if(item.isInGroup("data"))
		{
			String label = ((String) item.get("Constituency"));
			String id = ((String)item.get("MP name"));
			String party = ((String)item.get("Political party"));
			String state = ((String)item.get("State"));
			jpub = new JPopupMenu();
			
			
			double x = item.getX();
			double y = item.getY();
			jpub.add("MP Name: " + id);
			jpub.add("Political Party: " + party);
			jpub.add("Constituency: " + label);
			jpub.add("State: " + state);
			//System.out.println(label);
			jpub.show(e.getComponent(),(int)x,(int)y);
		}	
	
		//}
	}
	
	@Override 
	public void itemExited(VisualItem item, MouseEvent e)
	{
		if(item.isInGroup("data"))
		{
			jpub.setVisible(false);
		}
	}
	
}
