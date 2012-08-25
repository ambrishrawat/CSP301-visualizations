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
import prefuse.action.assignment.*;
import prefuse.action.layout.CircleLayout;
import prefuse.action.layout.RandomLayout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.FruchtermanReingoldLayout;
import prefuse.activity.Activity;
import prefuse.controls.DragControl;
import prefuse.controls.NeighborHighlightControl;
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
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.data.io.TableReader;
import prefuse.*;
public class GetPollGraph {

	private static Graph graph;
	private static Random rand;
	private static Visualization vis;
	private static Display d;
	
    public static void main(String[] argv) throws Exception
	{
    	
		setUpData();
		DegreePlot();		
			
		setUpVisualization();
		setUpRenderers();
		setUpActions();
		setUpDisplay();

        // launch the visualization -------------------------------------
        
        // Create a new window to hold the visualization.  
        // We pass the text value to be displayed in the menubar to the constructor.
        JFrame frame = new JFrame("Graph Visualisation");
        
        // Ensure application exits when window is closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(d);
        
        // Prepares the window.
        frame.pack();           
        
        // Shows the window.
        frame.setVisible(true); 
        vis.run("config");
        vis.run("layout");
	}
     
    // -- 1. load the data ------------------------------------------------
	public static void setUpData() throws Exception
	{	
    	// Here we are parsing the given .gml file to create two tables named
		// 'nodes' and 'edges', which we will use to construct the graph.
    	int row_counter = -1;
		int edge_counter = -1;
		Table nodes = new Table();
		Table edges = new Table();
		edges.addColumn("source", Integer.TYPE, 1);
		edges.addColumn("target", Integer.TYPE, 1);
		nodes.addColumn("id", Integer.TYPE);
		String type = new String();
		//BufferedReader in = new BufferedReader(new FileReader("./polbooks/polbooks.gml"));
		BufferedReader in = new BufferedReader(new FileReader("./blogs/polblogs.gml"));
		String str = new String();
		str = in.readLine();
		str = in.readLine();
		str = in.readLine();
		str = str.trim();
		if(str.equals("["))
		{
			str = in.readLine();
		}
		str = str.trim();
		boolean direct = (str.equals("directed 0"))?false:true;
		str = in.readLine();
		str = str.trim();
		while(!(str.equals("edge")||str.equals("edge [")))
		{
			str = in.readLine();
			str = str.trim();
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
			while(!str.equals("]"))
			{
				str = str.trim();
				String[] sarray = str.split("\\s+",2);
				if(sarray[1].matches("\".+\""))
					sarray[1] = sarray[1].split("\"")[1];
				int i = 0;
				while(i< nodes.getColumnCount())
				{
					if(!nodes.getColumnName(i).equals(sarray[0]))
						i++;
					else
						break;
				}
				if(i==nodes.getColumnCount())
				{
					nodes.addColumn(sarray[0],type.getClass());
				}
				nodes.set(row_counter, sarray[0], sarray[1]);
				str = in.readLine();
				str = str.trim();
				
			}
			str = in.readLine();
			str = str.trim();
		}
			
		while(!str.equals("]"))
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
				String[] sarray = str.split("\\s+",2);
				int i = 0;
				while(i< edges.getColumnCount())
				{
					if(!edges.getColumnName(i).equals(sarray[0]))
						i++;
					else
						break;
				}
				if(i==edges.getColumnCount())
				{
					edges.addColumn(sarray[0],type.getClass());
				}
				edges.set(edge_counter, sarray[0], sarray[1]);
				str = in.readLine();
				str = str.trim();
				
			}
			str = in.readLine();
			str = str.trim();
		}
		in.close();
		
		nodes.addColumn("indegree", Integer.TYPE);
		nodes.addColumn("outdegree", Integer.TYPE);
		nodes.addColumn("degree", Integer.TYPE);
		
		graph = new Graph(nodes, edges, direct);
		
		for(int k = 0;k< nodes.getRowCount();k++)
		{
			nodes.set(k,"degree",graph.getDegree(k));
			nodes.set(k,"indegree",graph.getInDegree(k));
			nodes.set(k,"outdegree",graph.getOutDegree(k));
		}
		
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
        FinalRenderer r = new FinalRenderer();
        EdgeRenderer e = new EdgeRenderer(Constants.EDGE_TYPE_LINE, Constants.EDGE_ARROW_NONE);
        if(graph.isDirected())
        {
        	e.setArrowType(Constants.EDGE_ARROW_FORWARD);
        	e.setArrowHeadSize(7, 7);
        }
        // create a new DefaultRendererFactory
        // This Factory will use the ShapeRenderer for all nodes.
        vis.setRendererFactory(new DefaultRendererFactory(r,e));
        System.out.println(graph.isDirected());
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
		int[] palette = {ColorLib.rgba(200, 0, 0,135), ColorLib.rgba(0, 0, 200,135), ColorLib.rgba(0, 200, 0,135)};
		DataColorAction fill = new DataColorAction("graph.nodes", "value",Constants.NOMINAL,VisualItem.FILLCOLOR,palette);
		fill.add(VisualItem.FIXED, ColorLib.rgba(200, 200, 255,200));
        fill.add(VisualItem.HIGHLIGHT, ColorLib.rgba(150, 150, 150,200));
		ShapeAction shape = new ShapeAction("graph.nodes", Constants.SHAPE_ELLIPSE);
        // Similarly to the node coloring, we use a ColorAction for the 
        // edges
        ColorAction edges = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray(200));
        
        //DataSizeAction size = new DataSizeAction("graph.nodes","degree",Constants.CONTINUOUS,Constants.SQRT_SCALE);
        //size.setMaximumSize(50);
        // Create an action list containing all color assignments
        // ActionLists are used for actions that will be executed
        // at the same time.  
        ActionList config = new ActionList();
        config.add(fill);
        config.add(edges);
        config.add(shape);
        if(graph.isDirected())
        	config.add(new ColorAction("graph.edges", VisualItem.FILLCOLOR, ColorLib.gray(200)));
        //config.add(size);
        // The layout ActionList recalculates 
        // the positions of the nodes.
        ActionList layout = new ActionList(Activity.INFINITY);
        
        // We add the layout to the layout ActionList, and tell it
        // to operate on the "graph".
        //FruchtermanReingoldLayout force = new FruchtermanReingoldLayout("graph");
        ForceDirectedLayout force = new ForceDirectedLayout("graph", true);
        //force.setMargin(1000, 1000, 1000, 1000);
        layout.add(force);
        layout.add(fill);
        //layout.add(new RandomLayout("graph"));
        
        // We add a RepaintAction so that every time the layout is 
        // changed, the Visualization updates it's screen.
        layout.add(new RepaintAction());
        
        // add the actions to the visualization
        vis.putAction("config", config);
        vis.putAction("layout", layout);
        
	}
	
	public static void setUpDisplay()
	{
        // -- 5. the display and interactive controls -------------------------
        
        // Create the Display object, and pass it the visualization that it 
        // will hold.
		d = new Display(vis);
        
        // Set the size of the display.
        d.setSize(1300, 700);
        d.pan(650, 350);
        
        //d.setBackgroundImage("./vader.jpg", true, true);
        // We use the addControlListener method to set up interaction.
        
        // The DragControl is a built in class for manually moving
        // nodes with the mouse. 
        d.addControlListener(new DragControl());
        // Pan with left-click drag on background
        d.addControlListener(new PanControl()); 
        // Zoom with right-click drag
        d.addControlListener(new ZoomControl());
        d.addControlListener(new NeighborHighlightControl());
        
        d.addControlListener(new FinalControlListener());




	}
	
	public static void DegreePlot() throws Exception
	{
		//int a[] = new int[graph.getNodeCount()];
		BufferedWriter out = new BufferedWriter(new FileWriter("./DegreePlot.dat"));
		//out.write("#File");
		//out.newLine();
		//out.write("\t#NodeId\tDegree");
		//out.newLine();
		for(int i = 0;i<graph.getNodeCount();i++)
		{
			out.write(graph.getDegree(i)+"\t"+i);
			//out.write(graph.getDegree(i));
			//a[i] = graph.getDegree(i);
			out.newLine();
			
		}
		
		
		out.close();
		System.out.println("File Created Successfully");
		
	}
    
}