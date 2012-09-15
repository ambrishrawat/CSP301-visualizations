import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import prefuse.Constants;
import prefuse.render.AbstractShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
public class FinalRenderer extends AbstractShapeRenderer
{
	private int m_baseSize = 10;
	protected Ellipse2D m_box = new Ellipse2D.Double();
	
	@Override
	protected Shape getRawShape(VisualItem item)
	{
		int stype = item.getShape();
        double x = item.getX();
        if ( Double.isNaN(x) || Double.isInfinite(x) )
            x = 0;
        double y = item.getY();
        if ( Double.isNaN(y) || Double.isInfinite(y) )
            y = 0;
        double width = m_baseSize*item.getSize();
        double height = m_baseSize*item.getSize();
        
        // Center the shape around the specified x and y
        if ( width > 1 ) {
            x = x-width/2;
            y = y-width/2;
        }
        
        //if(item instanceof NodeItem)
        //{
        	width = (500)/(((Integer)item.get("Age")).intValue());
        	height = (500)/(((Integer)item.get("Age")).intValue());
        	//width = 5;
        	//height = 5;
        //}
        
        
        
       m_box.setFrame(x, y, width, height);
       return m_box;
       
	}
	
}
