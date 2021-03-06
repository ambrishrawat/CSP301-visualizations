import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataShapeAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.ToolTipControl;
import prefuse.data.Table;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DelimitedTextTableReader;
import prefuse.data.query.ObjectRangeModel;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.AxisRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.VisiblePredicate;

/**
 * A simple scatter plot visualization that allows visual encodings to
 * be changed at runtime.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class ScatterPlot extends Display {

    private static final String group = "data";
    private Rectangle2D m_ylabB = new Rectangle2D.Double();
    private Rectangle2D m_xlabB = new Rectangle2D.Double();
    private Rectangle2D m_dataB = new Rectangle2D.Double();
    //private ShapeRenderer m_shapeR = new ShapeRenderer();
    
    
    public ScatterPlot(Table t, String xfield, String yfield) {
        this(t, xfield, yfield, null);
    }
    
    public ScatterPlot(Table t, String xfield, String yfield, String sfield) {
        super(new Visualization());
        
        // --------------------------------------------------------------------
        // STEP 1: setup the visualized data
        
        m_vis.addTable(group, t);
       // m_shapeR.ellipse(x, y, width, height)
        FinalRenderer r = new FinalRenderer();
        DefaultRendererFactory rf = new DefaultRendererFactory(r);
       // m_vis.setRendererFactory(rf);
        m_vis.setRendererFactory(new RendererFactory() {
        //    AbstractShapeRenderer sr = new ShapeRenderer();
        	FinalRenderer r = new FinalRenderer();
            Renderer arY = new AxisRenderer();
            Renderer arX = new AxisRenderer();
            
            public Renderer getRenderer(VisualItem item) {
                return item.isInGroup("ylab") ? arY :
                       item.isInGroup("xlab") ? arX : r;
            }
        });
        // --------------------------------------------------------------------
        // STEP 2: create actions to process the visual data
        
        // set up the actions
        AxisLayout x_axis = new AxisLayout(group, xfield, 
                Constants.X_AXIS, VisiblePredicate.TRUE);
        m_vis.putAction("x", x_axis);
        
        AxisLayout y_axis = new AxisLayout(group, yfield, 
                Constants.Y_AXIS, VisiblePredicate.TRUE);
        m_vis.putAction("y", y_axis);
        
        
        
        AxisLabelLayout xlabels = new AxisLabelLayout("xlab", x_axis);
        m_vis.putAction("xlabels", xlabels);
        
        AxisLabelLayout ylabels = new AxisLabelLayout("ylab", y_axis);
        m_vis.putAction("ylabels", ylabels);
        
        ColorAction color = new ColorAction(group, 
                VisualItem.FILLCOLOR, ColorLib.rgba(100,100,255,100));
        m_vis.putAction("color", color);
        
        //DataShapeAction shape = new DataShapeAction(group, sfield);
        //m_vis.putAction("shape", shape);
        
        DataSizeAction size = new DataSizeAction(group, sfield,Constants.CONTINUOUS,Constants.SQRT_SCALE);
        m_vis.putAction("size", size);
        ActionList draw = new ActionList();
        draw.add(x_axis);
        draw.add(y_axis);
        draw.add(xlabels);
        draw.add(ylabels);
        if ( sfield != null )
            {
        	//draw.add(shape);
        	draw.add(size);
            }
        draw.add(color);
        draw.add(new RepaintAction());
        m_vis.putAction("draw", draw);
        
        // --------------------------------------------------------------------
        // STEP 3: set up a display and ui components to show the visualization

        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setSize(700,450);
        setHighQuality(true);
        
        addControlListener(new FinalControlListener());
        //m_vis.run("xlabels");
        m_vis.run("update");
        m_vis.run("xlabels");
        ToolTipControl ttc = new ToolTipControl(new String[] {xfield,yfield});
        addControlListener(ttc);
        
        
        // --------------------------------------------------------------------        
        // STEP 4: launching the visualization
                
        m_vis.run("draw");

    }
    /*
    public int getPointSize() {
        return m_shapeR.getBaseSize();
    }
    
    public void setPointSize(int size) {
        m_shapeR.setBaseSize(size);
        repaint();
    }
    */
    // ------------------------------------------------------------------------
    
    public static void main(String[] argv) {
        String data = "./MPTrack-15_latest2.csv";
        String xfield = "State";
        String yfield = "Political party";
        String sfield = "Age";
        String sortxfield = "Age";
        String sortyfield = "Age";
        if ( argv.length >= 3 ) {
            data = argv[0];
            xfield = argv[1];
            yfield = argv[2];
            sfield = ( argv.length > 3 ? argv[3] : null );
        }
        
        final ScatterPlot sp = demo(data, xfield, yfield, sfield);
        JToolBar toolbar = getEncodingToolbar(sp, xfield, yfield, sfield, sortxfield,sortyfield);
        
        
        
        JFrame frame = new JFrame("p r e f u s e  |  s c a t t e r");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(toolbar, BorderLayout.NORTH);
        frame.getContentPane().add(sp, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
    
    public static ScatterPlot demo(String data, String xfield, String yfield) {
        return demo(data, xfield, yfield, null);
    }
    
    public static ScatterPlot demo(String data, String xfield,
                                   String yfield, String sfield)
    {
        Table table = null;
        try {
            table = new CSVTableReader().readTable(data);
        } catch ( Exception e ) {
            e.printStackTrace();
            return null;
        }
        ScatterPlot scatter = new ScatterPlot(table, xfield, yfield, sfield);
       // scatter.setPointSize(10);
        return scatter;
    }
    
    private static JToolBar getEncodingToolbar(final ScatterPlot sp,
            final String xfield, final String yfield, final String sfield,final String sortxfield,final String sortyfield)
    {
        int spacing = 10;
        
        // create list of column names
        Table t = (Table)sp.getVisualization().getSourceData(group);
        String[] colnames = new String[t.getColumnCount()];
        for ( int i=0; i<colnames.length; ++i )
            colnames[i] = t.getColumnName(i);
        
        // create toolbar that allows visual mappings to be changed
        JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.add(Box.createHorizontalStrut(spacing));
        
        String[] xcolumns = new String[15];
        xcolumns[0] = "State";
        xcolumns[1] = "Political party";
        xcolumns[2] = "Gender";
        xcolumns[3] = "Education qualifications";
        xcolumns[4] = "Age";
        xcolumns[5] = "Debates";
        xcolumns[6] = "Private Member Bills";
        xcolumns[7] = "Questions";
        xcolumns[8] = "Attendance";
        xcolumns[9] = "State's Debates average";
        xcolumns[10] = "State's Private Member Bills  average";
        xcolumns[11] = "State's Questions average";
        xcolumns[12] = "State's Attendance average";
        xcolumns[13] = "Latitude";
        xcolumns[14] = "Longitude";
        final JComboBox xcb = new JComboBox(xcolumns);
        xcb.setSelectedItem(xfield);
        xcb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Visualization vis = sp.getVisualization();
                AxisLayout xaxis = (AxisLayout)vis.getAction("x");
                xaxis.setDataField((String)xcb.getSelectedItem());
                xaxis.setDataType(Constants.ORDINAL);
                String []o = new String[100];
                for(int i =0;i<100;i++)
                	o[i]=" ";
                ObjectRangeModel om = new ObjectRangeModel(o);
                AxisLabelLayout xlabels = (AxisLabelLayout)vis.getAction("xlabels");
                //xlabels.setRangeModel(
                //xlabels.setRangeModel(om);
                xlabels.setRangeModel(xaxis.getRangeModel());
                //ObjectRangeModel disp = (ObjectRangeModel)xaxis.getRangeModel();
            
                //AxisLabelLayout xnew = new AxisLabelLayout("xlab", xaxis);
                //xlabels = xnew;
                //vis.putAction("xlabels", xlabels);
                //draw.add(xlabels);
                vis.run("draw");
                //vis.run("update");
            }
        });
        toolbar.add(new JLabel("X: "));
        toolbar.add(xcb);
        toolbar.add(Box.createHorizontalStrut(2*spacing));
        
        
        
        
        final JComboBox ycb = new JComboBox(xcolumns);
        ycb.setSelectedItem(yfield);
        ycb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Visualization vis = sp.getVisualization();
                AxisLayout yaxis = (AxisLayout)vis.getAction("y");
                yaxis.setDataField((String)ycb.getSelectedItem());
                //yaxis.setDataType(Constants.UNKNOWN);
                AxisLabelLayout ylabels = (AxisLabelLayout)vis.getAction("ylabels");
                ylabels.setRangeModel(yaxis.getRangeModel());
                
                vis.run("draw");
            }
        });
        toolbar.add(new JLabel("Y: "));
        toolbar.add(ycb);
        toolbar.add(Box.createHorizontalStrut(2*spacing));
        
        String[] scolumns = new String[5];
        scolumns[0] = "age";
        scolumns[1] = "debates";
        scolumns[2] = "private member bills";
        scolumns[3] = "questions";
        scolumns[4] = "attendance";
        
        final JComboBox scb = new JComboBox(scolumns);
        scb.setSelectedItem(sfield);
        scb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Visualization vis = sp.getVisualization();
                DataSizeAction s = (DataSizeAction)vis.getAction("size");
                s.setDataField((String)scb.getSelectedItem());
                //s.setScale(Constants.LOG_SCALE);
                vis.run("draw");
            }
        });
        toolbar.add(new JLabel("Size: "));
        toolbar.add(scb);
        toolbar.add(Box.createHorizontalStrut(spacing));
        toolbar.add(Box.createHorizontalGlue());
        
        return toolbar;
    }
    
} // end of class ScatterPlot
