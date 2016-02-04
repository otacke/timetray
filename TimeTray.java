/*
 * TimeTray
 *
 * TimeTray is a tool that adds a symbol to an operating system's icon tray
 * showing the current calender week. Further information about date and time
 * can be obtained by hovering over the symbol with the mouse pointer.
 *
 * TODO: open one presets window only
 * TODO: positioning of the presets window
 * TODO: color chooser settings
 * TODO: font settings (maybe)
 * TODO: beautify the settings window
 * TODO: improve or remove the tooltip stuff
 * TODO: refactoring, e.g. packaging
 *
 * @author Oliver Tacke, Armin Schöning
 * @version 1.4, Feb 2016
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
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
	// version string
	private final String VERSION = "1.4";

	// TrayIcon to be used in system tray
	private TrayIcon trayIcon = null;

	// dimension of the TrayIcon
	private Dimension iconSize = null;

	// internal Java calendar for getting date information
	private Calendar calendar = null;
	
	// current week number
	private int weeknumber = 0;
	
	// popupMenu to be used in connection with the TrayIcon
	private PopupMenu menu = this.createMenu();
		
	// Presets for the TrayIcon
	private Presets presets = null;

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

		// run thread and set timer tooltip to update every second
		run();
                   
		Timer timer = new Timer();
		timer.schedule( this, 1000, 1000 );
	}

	/**
	 * run method
	 */
	public void run() {
		// get current date and time and set ToolTipText accordingly
        calendar = Calendar.getInstance();
	
		trayIcon.setToolTip(
				"week " + 
				getWeekNumber() + ", " + 
				presets.sdf.format( calendar.getTime() ) );
		
		// The computer might be running when a new week starts...
		if ( weeknumber != getWeekNumber() ) {
			weeknumber = getWeekNumber();
			repaintTrayIcon();
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
					String.valueOf( getWeekNumber() ) );
            g2.drawString(
					String.valueOf( getWeekNumber() ),
					( iconSize.width-fontWidth ) / 2,
					iconSize.height - 3 );

            return image;
	}

	private void repaintTrayIcon() {
		this.trayIcon.setImage( this.getTrayImage() );
	}
	
	/**
	 * calculate the current week number
	 *
	 * @return int the current week number
	 */
	private int getWeekNumber() {
		return this.calendar.get( Calendar.WEEK_OF_YEAR ) + this.presets.offset;
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
		PopupMenu menu = new PopupMenu( "TimeTray " + this.VERSION );

		// about item
		MenuItem menuSettings = new MenuItem( "Settings" );
		menuSettings.setActionCommand( "settings" );
		menu.add( menuSettings );

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
		
		// path + file name for saving settings in the home directory
		private final String FILENAME = System.getProperty( "user.home" ) +
				File.separator +
				".timetray";
		
		// default background color for the tray icon
		private final Color DEFAULT_BACKGROUND_COLOR =
				new Color( 221, 221, 221, 0 );

		// default font color for the tray icon
		private final Color DEFAULT_FONT_COLOR = Color.white;

		// in general, locale settings probably match the local customs
		private static final int DEFAULT_OFFSET = 0;

		// default format for displaying date information
		private static final String DEFAULT_SDF_FORMAT =
				"EEEE, MMMM dd, yyyy, HH:mm";		
		
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
			
			backgroundColor = this.DEFAULT_BACKGROUND_COLOR;
			fontColor       = this.DEFAULT_FONT_COLOR;
			font            = DEFAULT_FONT;
			offset          = this.DEFAULT_OFFSET;
			sdf				= new SimpleDateFormat( DEFAULT_SDF_FORMAT );
			
			File file = new File( this.FILENAME );
			if ( file.exists() ) {
				loadPresets( trayHeight );
			} else {
				savePresets();
			}
			
		}

		/**
		 * Load presets
		 *
		 * This load/save method is very ugly, I know. No beauty and not
		 * failsafe. It might be replaced by something using JSON, but I want
		 * to avoid using full blown libraries for a plain settings file.
		 *
		 * @param int trayHeight the height of the TrayIcon
		 */
		private void loadPresets( int trayHeight ) {
			String line;
			try (
				InputStream fis = new FileInputStream( this.FILENAME );
				InputStreamReader isr = new InputStreamReader(
						fis,
						Charset.forName( "UTF-8" ) );
				BufferedReader br = new BufferedReader( isr );
			) {
				int red   = Integer.parseInt( br.readLine() );
				int green = Integer.parseInt( br.readLine() );
				int blue  = Integer.parseInt( br.readLine() );
				int alpha = Integer.parseInt( br.readLine() );
				this.backgroundColor = new Color ( red, green, blue, alpha );
				
				red   = Integer.parseInt( br.readLine() );
				green = Integer.parseInt( br.readLine() );
				blue  = Integer.parseInt( br.readLine() );
				alpha = Integer.parseInt( br.readLine() );
				this.fontColor = new Color ( red, green, blue, alpha );

				this.offset = Integer.parseInt( br.readLine() );
				
				String family = br.readLine();
				int style     = Integer.parseInt( br.readLine() );
				this.font = new Font( family, style, trayHeight );
			
				this.sdf = new SimpleDateFormat( br.readLine() );

			} catch ( IOException ex ) {
				ex.printStackTrace();
			}
		}

		/**
		 * Save presets
		 *
		 * This load/save method is very ugly, I know. No beauty and not
		 * failsafe. It might be replaced by something using JSON, but I want
		 * to avoid using full blown libraries for a plain settings file.
		 */		
		private void savePresets() {
			try ( PrintStream out = new PrintStream(
						new FileOutputStream( this.FILENAME ) ) ) {
				out.println( this.backgroundColor.getRed() );
				out.println( this.backgroundColor.getGreen() );
				out.println( this.backgroundColor.getBlue() );
				out.println( this.backgroundColor.getAlpha() );
				
				out.println( this.fontColor.getRed() );
				out.println( this.fontColor.getGreen() );
				out.println( this.fontColor.getBlue() );
				out.println( this.fontColor.getAlpha() );
				
				out.println( this.offset );
				
				out.println( this.font.getFamily() );
				out.println( this.font.getStyle() );
				
				out.println( this.sdf.toPattern() );
				
			}  catch ( FileNotFoundException ex ) {
				ex.printStackTrace();
			}
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
			this.getContentPane().setLayout( new BoxLayout(
					this.getContentPane(),
					BoxLayout.PAGE_AXIS ) );
			this.initWindow();
		}
		
		/**
		 * Build the window
		 */
		private void initWindow() {
			JLabel sliderLabel = new JLabel( "Offset" );
			sliderLabel.setAlignmentX( Component.LEFT_ALIGNMENT );
			
			offsetSlider = createOffsetSlider( this );

			this.setTitle( "Presets (not finished)" );
			this.getContentPane().add( sliderLabel );
			this.getContentPane().add( Box.createRigidArea(
					new Dimension( 0, 5 ) ) );
			this.getContentPane().add( offsetSlider );
			this.pack();
		}
		
		/**
		 * Build the slider for setting the offset
		 *
		 * @param SettingsWindow window the window that implements the Change
		 *        Listener
		 * @return JSlider the slider for setting the offset
		 */		
		private JSlider createOffsetSlider( SettingsWindow window ) {
			JSlider offsetSlider = new JSlider(
					JSlider.HORIZONTAL,
					-1,
					1,
					window.parent.presets.offset );
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
			
			//update the TrayIcon image
			this.parent.repaintTrayIcon();
		
			// save changed presets
			this.parent.presets.savePresets();
		}
		
	}
}