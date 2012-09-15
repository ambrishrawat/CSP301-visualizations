import java.awt.Font;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.LocationAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.AxisLayout;
import prefuse.activity.Activity;
import prefuse.activity.ActivityAdapter;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.PanControl;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.expression.FunctionExpression;
import prefuse.data.expression.FunctionTable;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.io.DataIOException;
import prefuse.data.io.DelimitedTextTableReader;
import prefuse.data.io.CSVTableReader;
import prefuse.data.query.SearchQueryBinding;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.demos.ZipDecode;
import prefuse.demos.ZipDecode.StateLookupFunction;
import prefuse.demos.ZipDecode.ZipColorAction;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.ShapeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.util.ui.JSearchPanel;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;

public class DataVisulization extends Display implements Constants {

    public static final String ZIPCODES = "./MPTrack-15_latest2.csv";
    public static final String STATES = "./MPTrack-15_latest.csv";
    
    // data groups
    private static final String DATA = "data";
    private static final String LABELS = "labels";
    private static final String FOCUS = Visualization.FOCUS_ITEMS;
    
    public static class StateLookupFunction extends FunctionExpression {
        private static Table s_states;
        static {
            try {
                s_states = new CSVTableReader().readTable(STATES);
            } catch ( Exception e ) { e.printStackTrace(); }
        }
        
        public StateLookupFunction() { super(1); }
        public String getName() { return "STATE"; }
        public Class getType(Schema s) { return String.class; }
        public Object get(Tuple t) {
            int code = s_states.index("code").get(param(0).getInt(t));
            return s_states.getString(code, "alpha");
        }
    }
    // add state function to the FunctionTable
    static { FunctionTable.addFunction("STATE", StateLookupFunction.class); }
    
    
    public DataVisulization(final Table t) {
        super(new Visualization());
        
        VisualTable vt = m_vis.addTable(DATA, t, getDataSchema());
        /*
         // this predicate makes sure only the continental states are included
        Predicate filter = (Predicate)ExpressionParser.parse(
                "state >= 1 && state <= 56 && state != 2 && state != 15");
        VisualTable vt = m_vis.addTable(DATA, t, filter, getDataSchema());
        // zip codes are loaded in as integers, so lets create a derived
        // column that has correctly-formatted 5 digit strings
        //vt.addColumn("zipstr", "LPAD(zip,5,'0')");
        // now add a formatted label to show within the visualization
        //vt.addColumn("label", "CONCAT(CAP(city),', ',STATE(state),' ',zipstr)");
        vt.addConstantColumn("zipstr", String.class, "");
        vt.addConstantColumn("label", String.class, "");
        // create a filter controlling label appearance
        Predicate loneResult = (Predicate)ExpressionParser.parse(
                "INGROUP('_search_') AND GROUPSIZE('_search_')=1 AND " +
                "LENGTH(QUERY('_search_'))=5");
        
        
        // add a table of visible city,state,zip labels
        // this is a derived table, overriding only the fields that need to
        // have unique values and inheriting all other data values from the
        // data table. in particular, we want to inherit the x,y coordinates.
        m_vis.addDerivedTable(LABELS, DATA, loneResult, getLabelSchema());
        */
        
        
        
        // -- renderers -------------------------------------------------------
        /*
        DefaultRendererFactory rf = new DefaultRendererFactory();
        rf.setDefaultRenderer(new ShapeRenderer(5)); // 1 pixel rectangles
        rf.add("INGROUP('labels')", new LabelRenderer("label") {
            public Shape getShape(VisualItem item) {
                // set horizontal alignment based on x-coordinate position
                //setHorizontalAlignment(item.getX()>getWidth()/2 ? RIGHT:LEFT);
                // now return shape as usual
                return super.getShape(item);
            }
        });
        */
        FinalRenderer rf = new FinalRenderer();
        m_vis.setRendererFactory(new DefaultRendererFactory(rf));
        
        // -- actions ---------------------------------------------------------
        ActionList config = new ActionList();
        DataColorAction fill = new DataColorAction(DATA, "Political party",Constants.NOMINAL,VisualItem.FILLCOLOR);
		fill.add(VisualItem.FIXED, ColorLib.rgba(200, 200, 255,200));
		config.add(fill);
		
        ActionList layout = new ActionList();
        layout.add(new AxisLayout(DATA, "Latitude", Y_AXIS));
        layout.add(new AxisLayout(DATA, "Longitude", X_AXIS));
        
        m_vis.putAction("layout", layout);
        m_vis.putAction("config", config);
        // the update list updates the colors of data points and sets the visual
        // properties for any labels. Color updating is limited only to the
        // current focus items, ensuring faster performance.
        final Action update = new ZipColorAction(FOCUS);
        m_vis.putAction("update", update);
        
        // animate a change in color in the interface. this animation is quite
        // short, only 200ms, so that it does not impede with interaction.
        // color animation of data points looks only at the focus items,
        // ensuring faster performance.
        ActionList animate = new ActionList(200);
        animate.add(new ColorAnimator(FOCUS, VisualItem.FILLCOLOR));
        animate.add(new ColorAnimator(LABELS, VisualItem.TEXTCOLOR));
        animate.add(new RepaintAction());
        animate.addActivityListener(new ActivityAdapter() {
            public void activityCancelled(Activity a) {
                // if animation is canceled, set colors to final state
                update.run(1.0);
            }
        });
        m_vis.putAction("animate", animate);
        
        // update items after a resize of the display, animating them to their
        // new locations. this animates all data points, so is noticeably slow.
        ActionList resize = new ActionList(1500);
        resize.setPacingFunction(new SlowInSlowOutPacer());
        resize.add(new LocationAnimator(DATA));
        resize.add(new LocationAnimator(LABELS));
        resize.add(new RepaintAction());
        m_vis.putAction("resize", resize);
        
        // -- display ---------------------------------------------------------
        
        setSize(581,581);
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        //setBackground(ColorLib.getGrayscale(50));
        setBackgroundImage("./India.jpg", true, false);
        setFocusable(false);
        
        // -- search ----------------------------------------------------------
        /*
        // zipcode text search is performed using a prefix based search,
        // provided by a search dynamic query. to make this application run
        // more efficiently, we optimize data handling by taking all search
        // results (both added and removed) and shuttling them into a
        // focus set. we work with this reduced set for updating and
        // animating color changes in the action definitions above.
        
        // create a final reference to the focus set, so that the following
        // search listener can access it.
        final TupleSet focus = m_vis.getFocusGroup(FOCUS);
        
        // create the search query binding
        SearchQueryBinding searchQ = new SearchQueryBinding(vt, "zipstr");
        final SearchTupleSet search = searchQ.getSearchSet(); 
        
        // create the listener that collects search results into a focus set
        search.addTupleSetListener(new TupleSetListener() {
            public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
                m_vis.cancel("animate");
                
                // invalidate changed tuples, add them all to the focus set
                focus.clear();
                for ( int i=0; i<add.length; ++i ) {
                    ((VisualItem)add[i]).setValidated(false);
                    focus.addTuple(add[i]);
                }
                for ( int i=0; i<rem.length; ++i ) {
                    ((VisualItem)rem[i]).setValidated(false);
                    focus.addTuple(rem[i]);
                }
                
                m_vis.run("update");
                m_vis.run("animate");
            }
        });
        m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, search);
        
        // create and parameterize a search panel for searching on zip code
        final JSearchPanel searcher = searchQ.createSearchPanel();
        searcher.setLabelText("zip>"); // the search box label
        searcher.setShowCancel(false); // don't show the cancel query button
        searcher.setShowBorder(false); // don't show the search box border
        searcher.setFont(FontLib.getFont("Georgia", Font.PLAIN, 22));
        searcher.setBackground(ColorLib.getGrayscale(50));
        searcher.setForeground(ColorLib.getColor(100,100,75));
        add(searcher); // add the search box as a sub-component of the display
        searcher.setBounds(10, getHeight()-40, 120, 30);
        */
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                m_vis.run("layout");
                m_vis.run("config");
                m_vis.run("update");
                m_vis.run("resize");
                //searcher.setBounds(10, getHeight()-40, 120, 30);
                invalidate();
            }
        });
        addControlListener(new FinalControlListener());
        addControlListener(new PanControl()); 
        // -- launch ----------------------------------------------------------
        
        m_vis.run("layout");
        m_vis.run("config");
        m_vis.run("animate");
    }
    
    private static Schema getDataSchema() {
        Schema s = PrefuseLib.getVisualItemSchema();
        s.setDefault(VisualItem.INTERACTIVE, false);
        s.setDefault(VisualItem.FILLCOLOR, ColorLib.rgb(100,100,75));
        return s;
    }
    
    private static Schema getLabelSchema() {
        Schema s = PrefuseLib.getMinimalVisualSchema();
        s.setDefault(VisualItem.INTERACTIVE, false);
        
        // default font is 16 point Georgia
        s.addInterpolatedColumn(
                VisualItem.FONT, Font.class, FontLib.getFont("Georgia",16));
        
        // default fill color should be invisible
        s.addInterpolatedColumn(VisualItem.FILLCOLOR, int.class);
        s.setInterpolatedDefault(VisualItem.FILLCOLOR, 0);
        
        s.addInterpolatedColumn(VisualItem.TEXTCOLOR, int.class);
        // default text color is white
        s.setInterpolatedDefault(VisualItem.TEXTCOLOR, ColorLib.gray(255));
        // default start text color is fully transparent
        s.setDefault(VisualItem.STARTTEXTCOLOR, ColorLib.gray(255,0));
        return s;
    }
    
    // ------------------------------------------------------------------------
    
    public static void main(String[] args) {
        String datafile = ZIPCODES;
        if ( args.length > 0 )
            datafile = args[0];
        
        try {
            JFrame frame = demo(datafile);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public static JFrame demo() {
        try {
            return demo(ZIPCODES);
        } catch ( Exception e ) {
            return null;
        }
    }
    
    public static JFrame demo(String table) throws DataIOException {
        CSVTableReader tr = new CSVTableReader();
        Table t = tr.readTable(table);        
        DataVisulization zd = new DataVisulization(t);
        
        JFrame frame = new JFrame("p r e f u s e  |  l o k s a b h a");
        frame.getContentPane().add(zd);
        frame.pack();
        return frame;
    }
    
    public static class ZipColorAction extends ColorAction {
        public ZipColorAction(String group) {
            super(group, VisualItem.FILLCOLOR);
        }
        
        public int getColor(VisualItem item) {
            if ( item.isInGroup(Visualization.SEARCH_ITEMS) ) {
                return ColorLib.gray(255);
            } else {
                return ColorLib.rgb(100,100,75);
            }
        }
    }
    
} // end of class ZipDecode
