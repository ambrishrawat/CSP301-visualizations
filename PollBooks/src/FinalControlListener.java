import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JPopupMenu;
import prefuse.controls.ControlAdapter;
import prefuse.controls.Control;
import prefuse.util.ColorLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class FinalControlListener extends ControlAdapter implements Control
{
	
	
	public void itemEntered(VisualItem item, MouseEvent e)
	{
		if(item instanceof NodeItem)
		{
			String label = ((String) item.get("label"));
			int id = (Integer) item.get("id");
			JPopupMenu jpub = new JPopupMenu();
			jpub.add("label: " + label);
			jpub.add("Id: " + id);
			jpub.show(e.getComponent(),(int) item.getX(),(int) item.getY());
	
	
		}
	}
	
	
}
