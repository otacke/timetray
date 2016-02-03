/*
 * TimeTray
 *
 * TimeTray is a tool that adds a symbol to an operating system's icon tray
 * showing the current calender week. Further information about date and time
 * can be obtained by hovering over the symbol with the mouse pointer.
 *
 * TODO: open one presets window only
 * TODO: repaint the TrayIcon when a new week begins
 * TODO: load and save methods for the presets
 * TODO: color chooser settings
 * TODO: font settings (maybe)
 * TODO: refactoring
 *
 * @author Oliver Tacke, Armin Schöning
 * @version 1.2, Feb 2016
 */

//import neccessary Classes
import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JSlider;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class TimeTray extends TimerTask implements ActionListener {

	// TrayIcon to be used in system tray
	private TrayIcon trayIcon;

	// dimension of the TrayIcon
	private Dimension iconSize;

	// internal Java calendar for getting date information
	private Calendar calendar;

	// popupMenu to be used in connection with the TrayIcon
	private PopupMenu menu = this.createMenu();
		
	// Presets for the TrayIcon
	private Presets presets = null;

	// indicate if the TrayIcon needs repainting
	private boolean repaint = false;

	/**
	 * main method
	 *
	 * @param args arguments
	 */
	public static void main(String[] args) {
		// create new TrayIcon if a SystemTray is supported by the OS
		if ( SystemTray.isSupported() ) {
			new TimeTray();
		} else {
			// TODO: Display a "Sorry" message
		}
	}

	/**
	 * TimeTray Constructor
	 */
	public TimeTray()  {

		// retrieve iconSize of SystemTray
		SystemTray systemTray = SystemTray.getSystemTray();
		iconSize = systemTray.getTrayIconSize();

		// set presets
		presets  = new Presets( iconSize.height );
		calendar = Calendar.getInstance();

		// create TrayIcon according to iconSize
		trayIcon = new TrayIcon( getTrayImage(), "TimeTray", menu );
		try {
			systemTray.add( trayIcon );
		} catch ( AWTException ex ) {
            ex.printStackTrace();
		}

		// run thread and set timer to update every second (if neccessary)
		run();
                   
		Timer timer = new Timer();
		timer.schedule( this, 1000, 1000 );
	}

	/**
	 * run method
	 */
	public void run() {
		if ( repaint ) {
            // get current date and set ToolTipText accordingly
            trayIcon.setToolTip( presets.sdf.format( calendar.getTime() ) );

            // show drawn image
            trayIcon.setImage( getTrayImage() );
			
			this.repaint = false;
		}
	}

	/**
	 * create the image for the TrayIcon
	 *
	 * @returns BufferedImage the image for the TrayIcon
	 */
	private BufferedImage getTrayImage() {
			BufferedImage image = new BufferedImage(
				this.iconSize.width,
				this.iconSize.height,
				BufferedImage.TYPE_INT_ARGB );

            // draw background image
            Graphics2D g2 = image.createGraphics();
            g2.setColor( presets.backgroundColor );
            g2.fillRect( 0, 0, iconSize.width, iconSize.height );

			// draw number
            g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON ); 
            g2.setColor( presets.fontColor );
            g2.setFont( presets.font );
            FontMetrics fm = g2.getFontMetrics( presets.font );
            int fontWidth = fm.stringWidth(
					String.valueOf( calendar.get( Calendar.WEEK_OF_YEAR ) ) );
            g2.drawString(
					String.valueOf(
							calendar.get( Calendar.WEEK_OF_YEAR ) + presets.offset ),
					( iconSize.width-fontWidth ) / 2,
					iconSize.height - 3 );

            return image;
	}

	/**
	 * ActionListener for the TrayIcon
	 *
	 * @param e the event triggering the ActionListener
	 */
	public void actionPerformed( ActionEvent ev ) {
		// user chose to exit TimeTray
		if ( ev.getActionCommand() == "quit" ) {
			System.exit( 0 );
		}
		// user requests information about TimeTray
		if ( ev.getActionCommand() == "settings" ) {
			SettingsWindow settingsWindow = new SettingsWindow( this );
			settingsWindow.setVisible( true );
		}
	}

	/**
	 * creates a PopUp menu for the TrayIcon
	 *
	 * @return PopUp menu for the TrayIcon
	 */
	private PopupMenu createMenu() {
		PopupMenu menu = new PopupMenu( "TimeTray" );

		// about item
		MenuItem menuSettings = new MenuItem( "Settings" );
		menuSettings.setActionCommand( "settings" );
		menu.add( menuSettings );

		// separator
		menu.addSeparator();

		// exit item
		MenuItem menuExit = new MenuItem( "Quit" );
		menuExit.setActionCommand( "quit" );
		menu.add( menuExit );

		menu.addActionListener( this );

		return menu;
	}
	
	/**
	 * individual settings or sets default values (not yet)
	 *
	 * TODO: setters/getters
	 */
	private class Presets {
		// default background color for the tray icon
		private final Color DEFAULT_BACKGROUND_COLOR =
				new Color( 221, 221, 221, 0 );

		// default font color for the tray icon
		private final Color DEFAULT_FONT_COLOR = Color.white;

		// in general, locale settings probably match the local customs
		private static final int DEFAULT_OFFSET = 0;

		// default format for displaying date information
		private static final String DEFAULT_SDF_FORMAT =
				"'week' w, EEEE, MMMM dd, yyyy, HH:mm";		
		
		// background color for the tray icon
		private Color backgroundColor;
			
		// font color for the tray icon
		private Color fontColor;

		// font to be used
		private Font font;

		/*
		 * used to correct the calendar week by +1 or -1 if neccessary
		 * 
		 * this may come in handy, because Java determines the start of a week
		 * depending on the locale settings of your operating system that can
		 * differ from the customs of your region.
		 */
		private int offset;

		// SimpleDateFormat for use in connection with the ToolTip text
		private SimpleDateFormat sdf;
		
		private Presets( int trayHeight ) {
			/*
			 * I use "SansSerif" instead of "Arial" because this tool might run
			 * anywhere, not only Windows
			 */
			final Font DEFAULT_FONT =
					new Font( "SansSerif", Font.PLAIN, trayHeight );
			
			backgroundColor = DEFAULT_BACKGROUND_COLOR;
			fontColor       = DEFAULT_FONT_COLOR;
			font            = DEFAULT_FONT;
			offset          = DEFAULT_OFFSET;
			sdf				= new SimpleDateFormat( DEFAULT_SDF_FORMAT );			
		}

		/**
		 * Load presets (from a JSON file)
		 *
		 * @returns Presets the presets
		 */
		private Presets loadPresets() {
			return null;
		}

		/**
		 * Save presets (to a JSON file)
		 */		
		private void savePresets() {
		}
		
	}

	/**
	 * Window for changing the settings
	 */	
	private class SettingsWindow extends JFrame implements ChangeListener {
		// Tray
		private TimeTray parent;		
		
		// Slider for setting the offset
		private JSlider offsetSlider;

		/**
		 * Constructor for SettingsWindow
		 *
		 * @param TimeTray parent Tray opening the window
		 */		
		private SettingsWindow( TimeTray parent ) {
			this.parent = parent;
			this.getContentPane().setLayout( new BoxLayout( this.getContentPane(), BoxLayout.PAGE_AXIS ) );
			this.initWindow();
		}
		
		/**
		 * Build the window
		 */
		private void initWindow() {
			JLabel sliderLabel = new JLabel( "Offset (not saved yet)" );
			sliderLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
			
			offsetSlider = createOffsetSlider( this );

			this.setTitle( "Presets (not finished)" );
			this.getContentPane().add( sliderLabel );
			this.getContentPane().add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
			this.getContentPane().add( offsetSlider );
			this.pack();
		}
		
		/**
		 * Build the slider for setting the offset
		 *
		 * @param SettingsWindow window the window that implements the Change Listener
		 * @return JSlider the slider for setting the offset
		 */		
		private JSlider createOffsetSlider( SettingsWindow window ) {
			JSlider offsetSlider = new JSlider( JSlider.HORIZONTAL, -1, 1, window.parent.presets.offset );
			offsetSlider.setAlignmentX( Component.LEFT_ALIGNMENT );
			offsetSlider.setMajorTickSpacing( 1 );
			offsetSlider.setPaintTicks( true );
			offsetSlider.addChangeListener( window );
			offsetSlider.setPaintLabels( true );
			
			return offsetSlider;
		}

		/**
		 * register change events in the window
		 *
		 * @param ChangeEvent an event
		 */
		public void stateChanged( ChangeEvent ev ) {
			JSlider source = (JSlider)ev.getSource();
			this.parent.presets.offset = source.getValue();
			this.parent.repaint = true;
		}
		
	}
}