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
		setUpVisualization();
		setUpRenderers();
		setUpActions();
		setUpDisplay();
		JFrame frame = new JFrame("Graph Visualisation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(d);
        frame.pack();           
        frame.setVisible(true); 
        vis.run("config");
        vis.run("layout");
        DegreePlot();		
		setRandomGraphs();
		setRandomGraphsClusteringCoeff();
        return;
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
		BufferedReader in = new BufferedReader(new FileReader("./polblogs/polblogs.gml"));
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
	
		while(!(str.equals("edge")||str.equals("edge [")))
		{
			str = in.readLine();
			str = str.trim();
			if(str.equals("["))
			{
					str = in.readLine();
			}
			nodes.addRow();
			row_counter++;
			while(!str.equals("]"))
			{
				str = str.trim();
				String[] sarray = str.split("\\s+",2);
				if(sarray[1].matches("\".+\""))
					sarray[1] = sarray[1].split("\"")[1];
				nodes.set(row_counter, sarray[0], sarray[1]);
				str = in.readLine();
				str = str.trim();
				
			}
			str = in.readLine();
			str = str.trim();
		}

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
        vis = new Visualization();
        vis.add("graph", graph);
        
	}
	
    // -- 3. the renderers and renderer factory ---------------------------
	public static void setUpRenderers()
	{
        FinalRenderer r = new FinalRenderer();
        EdgeRenderer e = new EdgeRenderer(Constants.EDGE_TYPE_LINE, Constants.EDGE_ARROW_NONE);
        if(graph.isDirected())
        {
        	e.setArrowType(Constants.EDGE_ARROW_FORWARD);
        	e.setArrowHeadSize(7, 7);
        }
        vis.setRendererFactory(new DefaultRendererFactory(r,e));
	}
	
	public static void setUpActions()
	{
		
        // -- 4. the processing actions ---------------------------------------
        
        int[] palette = {ColorLib.rgba(200, 0, 0,135), ColorLib.rgba(0, 0, 200,135), ColorLib.rgba(0, 200, 0,135)};
		DataColorAction fill = new DataColorAction("graph.nodes", "value",Constants.NOMINAL,VisualItem.FILLCOLOR,palette);
		fill.add(VisualItem.FIXED, ColorLib.rgba(200, 200, 255,200));
        fill.add(VisualItem.HIGHLIGHT, ColorLib.rgba(150, 150, 150,200));
		ShapeAction shape = new ShapeAction("graph.nodes", Constants.SHAPE_ELLIPSE);
        ColorAction edges = new ColorAction("graph.edges", VisualItem.STROKECOLOR, ColorLib.gray(200));
        ActionList config = new ActionList();
        config.add(fill);
        config.add(edges);
        config.add(shape);
        if(graph.isDirected())
        	config.add(new ColorAction("graph.edges", VisualItem.FILLCOLOR, ColorLib.gray(200)));
        ActionList layout = new ActionList(Activity.INFINITY);
        ForceDirectedLayout force = new ForceDirectedLayout("graph", true);
        layout.add(force);
        layout.add(fill);
        layout.add(new RepaintAction());
        vis.putAction("config", config);
        vis.putAction("layout", layout);
        
	}
	
	public static void setUpDisplay()
	{
        // -- 5. the display and interactive controls -------------------------
        
        d = new Display(vis);
        d.setSize(1300, 700);
        d.pan(650, 350);
        d.addControlListener(new DragControl());
        d.addControlListener(new PanControl()); 
        d.addControlListener(new ZoomControl());
        d.addControlListener(new NeighborHighlightControl());
        d.addControlListener(new FinalControlListener());
	}
	
	public static void DegreePlot() throws Exception
	{
		BufferedWriter out = new BufferedWriter(new FileWriter("./DegreePlot.dat"));
		for(int i = 0;i<graph.getNodeCount();i++)
		{
			out.write(graph.getDegree(i)+"\t"+i);
			out.newLine();
		}
		out.close();
		System.out.println("File Created Successfully");
		
	}
	
	public static double CalcEdgeRatio(Graph rangraph)
	{
		double counter=0.0;
		for(int i=0;i<rangraph.getEdgeCount();i++)
		{
			if(rangraph.getEdge(i).getSourceNode().get("value").equals(rangraph.getEdge(i).getTargetNode().get("value")))
			{
				counter+=1;
			}
		}
		counter = counter/rangraph.getEdgeCount();
		return counter;
	}
	
	public static void setRandomGraphs() throws Exception
	{
		BufferedWriter out = new BufferedWriter(new FileWriter("./EdgeRatioPlot.dat"));
		out.write(((Double)CalcEdgeRatio(graph)).toString());
		out.newLine();
		for(int j=2; j<=100; j++)
		{
			Graph g = new Graph(graph.getNodeTable(),graph.isDirected());
			int edge_count = 0;
			for(int i = 0;i <graph.getEdgeCount();i++)
			{
				Random r = new Random();
				int n = r.nextInt(graph.getNodeCount());
				int m = r.nextInt(graph.getNodeCount());
				if(m==0)
				{
					m++;
				}
				if(n==0)
					n++;				
				
				g.addEdge(n, m);
			}
			out.write(((Double)CalcEdgeRatio(g)).toString());
			out.newLine();
		}
		out.close();
		System.out.println("EdgeRatioFile Created Successfully");
	}
	
	public static double CalcClusteringCoeff(Graph rangraph)
	{
		double accumulator=0.0;
		
		for(int i=0;i<rangraph.getNodeCount();i++)
		{
			double counter=0.0;
			Iterator<Node> current = rangraph.getNode(i).neighbors(); 
			double c = 0.0;
			while(current.hasNext())
			{
				Iterator<Node> temp = rangraph.getNode(i).neighbors();
				Integer x = ((Integer)(current.next().get("id")));
				c++;
				while(temp.hasNext())
				{
					Node tempNext = temp.next();
					Iterator<Node> ineigh = rangraph.getNode(x.intValue()).neighbors();
					do
					{
						Node ineighNext = (Node)ineigh.next();
						if(((Integer)((ineighNext).get("id"))).equals(((Integer)(tempNext.get("id")))))
						{
							counter +=1;
						}
					}
					while((ineigh.hasNext()&&temp.hasNext()));
					
				}
				
			}
			//System.out.println(c);
			if(c>1)
			{
				counter = (counter)/(c*(c-1));
				//System.out.println(counter);
			}
			if(rangraph.isDirected())
				counter/=2;
			accumulator+=counter;
			
			
		}
		
		accumulator/=( rangraph.getNodeCount());
		return accumulator;
	}
	
	public static void setRandomGraphsClusteringCoeff() throws Exception
	{
		BufferedWriter out = new BufferedWriter(new FileWriter("./ClusterCoeff.dat"));
		out.write(((Double)CalcClusteringCoeff(graph)).toString());
		out.newLine();
		
		for(int j=2; j<=100; j++)
		{
			System.out.println(j+"??????????????????????????????????????????????");
			Graph g = new Graph(graph.getNodeTable(),graph.isDirected());
			int edge_count = 0;
			for(int i = 0;i <graph.getEdgeCount();i++)
			{
				Random r = new Random();
				int n = r.nextInt(graph.getNodeCount());
				int m = r.nextInt(graph.getNodeCount());
				if(m==0)
					m++;
				if(n==0)
					n++;				
				
				g.addEdge(n, m);
			}
			out.write(((Double)CalcClusteringCoeff(g)).toString());
			out.newLine();
		}
		
		out.close();
		System.out.println("ClusterCoeffFile Created Successfully");
	}
    
}
