import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;
import java.io.*;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JPanel;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.CircleLayout;
import prefuse.action.layout.RandomLayout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.data.io.TableReader;

public class GetPollGraph {

	private static Graph graph;
	private static Random rand;
	private static Visualization vis;
	private static Display d;
	
    public static void main(String[] argv) throws Exception
	{
    	
		setUpData();
		
		setUpVisualization();
		setUpRenderers();
		setUpActions();
		setUpDisplay();

        // launch the visualization -------------------------------------
        
        // The following is standard java.awt.
        // A JFrame is the basic window element in awt. 
        // It has a menu (minimize, maximize, close) and can hold
        // other gui elements. 
        
        // Create a new window to hold the visualization.  
        // We pass the text value to be displayed in the menubar to the constructor.
        JFrame frame = new JFrame("prefuse example");
        
        // Ensure application exits when window is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // The Display object (d) is a subclass of JComponent, which
        // can be added to JFrames with the add method.
        frame.add(d);
        
        // Prepares the window.
        frame.pack();           
        
        // Shows the window.
        frame.setVisible(true); 
        
        // We have to start the ActionLists that we added to the visualization
        vis.run("color");
        vis.run("layout");
	}
     
    // -- 1. load the data ------------------------------------------------
	public static void setUpData() throws Exception
	{	
    	// Here we are manually creating the data structures.  100 nodes are
    	// added to the Graph structure.  100 edges are made randomly 
    	// between the nodes.
		int row_counter = -1;
		int edge_counter = -1;
		Table nodes = new Table();
		Table edges = new Table();
		edges.addColumn("source", Integer.TYPE, 1);
		edges.addColumn("target", Integer.TYPE, 1);
		nodes.addColumn("id", Integer.TYPE);
		String type = new String();
		BufferedReader in = new BufferedReader(new FileReader("./blogs/polblogs.gml"));
		
		String str = new String();
		str = in.readLine();
		Integer id;
		String label, value;
		
		str = in.readLine();
		str = in.readLine();
		str = str.trim();
		if(str.equals("["))
		{
			str = in.readLine();
		}
		boolean direct = (str=="directed 0")?false:true;
		str = in.readLine();
		//System.out.println("tag "+str);
		while(!(str.equals("edge")||str.equals("edge [")))
		{
			str = in.readLine();
			str = str.trim();
			//System.out.println("Line 100" + str);
			if(str.equals("["))
			{
					str = in.readLine();
			}
			if(str.equals("id 1"))
			{
				nodes.addRow();
				row_counter++;
			}
			nodes.addRow();
			row_counter++;
			//System.out.println("Row count = "+ row_counter);
			//System.out.println("line118 "+str);
			while(!str.equals("]"))
			{
				str = str.trim();
				//System.out.println("Line 122" + str);
				String[] sarray = str.split("\\s+",2);
				if(sarray[1].matches("\".+\""))
					sarray[1] = sarray[1].split("\"")[1];
				int i = 0;
				while(i< nodes.getColumnCount())
				{
					//System.out.println(nodes.getColumnName(i));
					if(!nodes.getColumnName(i).equals(sarray[0]))
						i++;
					else
						break;
				}
				if(i==nodes.getColumnCount())
				{
				//	System.out.println("Count"+nodes.getColumnCount()+" "+i);
					nodes.addColumn(sarray[0],type.getClass());
				}
				nodes.set(row_counter, sarray[0], sarray[1]);
				//System.out.println(" Line 138"+" "+str.length()+" "+str);
				str = in.readLine();
				str = str.trim();
				
			}
			str = in.readLine();
			str = str.trim();
		}
			
		while(!str.equals("]"))
//		for(int z = 0; z<9304; z++)
		{
			str = in.readLine();
			str = str.trim();
			if(str.equals("["))
					str = in.readLine();
			edges.addRow();
			edge_counter+=1;
			while(!str.equals("]"))
			{
				str = str.trim();
			//	System.out.println("Line 122" + str);
				String[] sarray = str.split("\\s+",2);
				int i = 0;
				while(i< edges.getColumnCount())
				{
			//		System.out.println(edges.getColumnName(i));
					if(!edges.getColumnName(i).equals(sarray[0]))
						i++;
					else
						break;
				}
				if(i==edges.getColumnCount())
				{
				//	System.out.println("Count"+edges.getColumnCount()+" "+i);
					edges.addColumn(sarray[0],type.getClass());
				}
				System.out.println(" Line 188"+" "+sarray[0]+" "+sarray[1]+" int: "+Integer.parseInt(sarray[1]));
				if(sarray[0].equals("target")||sarray[0].equals("source"))
					edges.set(edge_counter, sarray[0], Integer.parseInt(sarray[1]));
				else
					edges.set(edge_counter, sarray[0], sarray[1]);
				//System.out.println(" Line 138"+" "+str.length()+" "+str);
				str = in.readLine();
				str = str.trim();
				
			}
			str = in.readLine();
			str = str.trim();
		}
		in.close();
		System.out.println(edges.getRowCount());
	/*	Table sortedNodes = new Table();
		for(int m=0; m<nodes.getColumnCount(); m++)
		{
			System.out.println(nodes.getColumnName(m) + nodes.getColumnType(m));
			sortedNodes.addColumn(nodes.getColumnName(m), nodes.getColumnType(m));
		}
		Iterator it = nodes.rowsSortedBy("id", true);
		System.out.println("No. of Columns: "+ nodes.getColumnCount());
		for(int n=0; n<=row_counter; n++)
		{
			sortedNodes.addRow();
		}
		int l= 0;
		while (it.hasNext())
		{
			Object itNext = it.next();
			//System.out.println("id = " + nodes.getString(l, 0));
			for(int k=0; k<nodes.getColumnCount(); k++)
			{
				sortedNodes.set(l, k, itNext.);
			}
			l++;
		}*/
		graph = new Graph(nodes, edges, direct);

		System.out.println("Graph made!"); 
       
	}
	
    // -- 2. the visualization --------------------------------------------
	public static void setUpVisualization()
	{
        // Create the Visualization object.
		vis = new Visualization();
        
        // Now we add our previously created Graph object to the visualization.
        // The graph gets a textual label so that we can refer to it later on.
        vis.add("graph", graph);
        
	}
	
    // -- 3. the renderers and renderer factory ---------------------------
	public static void setUpRenderers()
	{
        // Create a default ShapeRenderer
        ShapeRenderer r = new ShapeRenderer();
        
        // create a new DefaultRendererFactory
        // This Factory will use the ShapeRenderer for all nodes.
        vis.setRendererFactory(new DefaultRendererFactory(r));
	}
	
	public static void setUpActions()
	{
		
        // -- 4. the processing actions ---------------------------------------
        
        // We must color the nodes of the graph.  
        // Notice that we refer to the nodes using the text label for the graph,
        // and then appending ".nodes".  The same will work for ".edges" when we
        // only want to access those items.
        // The ColorAction must know what to color, what aspect of those 
        // items to color, and the color that should be used.
        ColorAction fill = new ColorAction("graph.nodes", VisualItem.FILLCOLOR, ColorLib.rgb(0, 200, 0));
       
        // Similarly to the node coloring, we use a ColorAction for the 
        // edges
        ColorAction edges = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray(200));
        
        // Create an action list containing all color assignments
        // ActionLists are used for actions that will be executed
        // at the same time.  
        ActionList color = new ActionList();
        color.add(fill);
        color.add(edges);
        
        // The layout ActionList recalculates 
        // the positions of the nodes.
        ActionList layout = new ActionList(Activity.INFINITY);
        
        // We add the layout to the layout ActionList, and tell it
        // to operate on the "graph".
        layout.add(new ForceDirectedLayout("graph",true));
        
        // We add a RepaintAction so that every time the layout is 
        // changed, the Visualization updates it's screen.
        layout.add(new RepaintAction());
        
        // add the actions to the visualization
        vis.putAction("color", color);
        vis.putAction("layout", layout);
        
	}
	
	public static void setUpDisplay()
	{
        // -- 5. the display and interactive controls -------------------------
        
        // Create the Display object, and pass it the visualization that it 
        // will hold.
		d = new Display(vis);
        
        // Set the size of the display.
        d.setSize(720, 500); 
        
        // We use the addControlListener method to set up interaction.
        
        // The DragControl is a built in class for manually moving
        // nodes with the mouse. 
        d.addControlListener(new DragControl());
        // Pan with left-click drag on background
        d.addControlListener(new PanControl()); 
        // Zoom with right-click drag
        d.addControlListener(new ZoomControl());
	}
    
}