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
			String label = ((String) item.get("Constituency"));
			String id = ((String)item.get("MP name"));
			jpub = new JPopupMenu();
			jpub.add("Constituency: " + label);
			jpub.add("MP Name: " + id);
			//System.out.println(label);
			jpub.show(e.getComponent(),0,570);
			
	
		//}
	}
	
	@Override 
	public void itemExited(VisualItem item, MouseEvent e)
	{
		//if(item instanceof NodeItem)
		//{
			jpub.setVisible(false);
		//}
	}
	
}
