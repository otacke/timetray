/*
 * TimeTray
 *
 * TimeTray is a tool that adds a symbol to an operating system's icon tray
 * showing the current calender week. Further information about date and time
 * can be obtained by hovering over the symbol with the mouse pointer.
 *
 * @author Oliver Tacke, Armin Schöning
 * @version 1.1, Jan 2016
 */

//import neccessary Classes
import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
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

public class TimeTray extends TimerTask implements ActionListener {

	// default background color for the tray icon
	private final Color DEFAULT_BACKGROUND_COLOR =
			new Color( 221, 221, 221, 0 );

	// default font color for the tray icon
	private final Color DEFAULT_FONT_COLOR = Color.white;

	// in general, locale settings probably match the local customs
	private final Integer DEFAULT_OFFSET = 0;

	// default format for displaying date information
	private final String DEFAULT_SDF_FORMAT =
			"'week' w, EEEE, MMMM dd, yyyy, HH:mm";

	// TrayIcon to be used in system tray
	private TrayIcon trayIcon;

	// dimension of the TrayIcon
	private Dimension iconSize;

	// image to be shown within TrayIcon
	private BufferedImage image;

	// internal Java calendar for getting date information
	private Calendar calendar;

	// SimpleDateFormat for use in connection with the ToolTip text
	private SimpleDateFormat sdf;

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
	private Integer offset;

	// popupMenu to be used in connection with the TrayIcon
	private PopupMenu menu = this.createMenu();

	/**
	 * TimeTray Constructor
	 *
	 * @author Oliver Tacke
	 * @version 1.0, May 2007
	 */
	public TimeTray()  {

		// retrieve iconSize of SystemTray
		SystemTray systemTray = SystemTray.getSystemTray();
		iconSize = systemTray.getTrayIconSize();

		// set presets
		this.setPresets( iconSize.height );

		// run thread and set timer to update every second
		run();
                
		try {
			systemTray.add( trayIcon );
		} catch ( AWTException ex ) {
            ex.printStackTrace();
		}
              
		Timer timer = new Timer();
		timer.schedule( this, 60000, 60000 );
	}

	/**
	 * run method
	 *
	 * @author Oliver Tacke, Armin Schöning
	 * @version 1.1, January 2016
	 */
	
	// TODO: refactor
	public void run() {
            
            // create TrayIcon according to iconSize
            image = new BufferedImage(
					iconSize.width,
					iconSize.height,
					BufferedImage.TYPE_INT_ARGB );
            trayIcon = new TrayIcon( image, "TimeTray", menu );
            
            // get current date and set ToolTipText accordingly
            calendar = Calendar.getInstance();
            trayIcon.setToolTip( sdf.format( calendar.getTime() ) );

            // draw background image
            Graphics2D g2 = image.createGraphics();
            g2.setColor( backgroundColor );
            g2.fillRect( 0, 0, iconSize.width, iconSize.height );

			// font
            g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON ); 
            g2.setColor( fontColor );
            g2.setFont( font );
            FontMetrics fm = g2.getFontMetrics( font );
            int fontWidth = fm.stringWidth(
					String.valueOf( calendar.get( Calendar.WEEK_OF_YEAR ) ) );
            g2.drawString(
					String.valueOf(
							calendar.get( Calendar.WEEK_OF_YEAR ) + offset ),
					( iconSize.width-fontWidth ) / 2,
					iconSize.height - 3 );

            // show drawn image
            trayIcon.setImage( image );

            g2.dispose();
	}

	/**
	 * main method
	 *
	 * @param args arguments
	 *
	 * @author Oliver Tacke
	 * @version 1.0, May 2007
	 */
	public static void main(String[] args) {
		// create new TrayIcon if a SystemTray is supported by the OS
		if ( SystemTray.isSupported() ) {
			new TimeTray();
		}
	}

	/**
	 * ActionListener for the TrayIcon
	 *
	 * @param e the event triggering the ActionListener
	 *
	 * @author Oliver Tacke
	 * @version 1.0, May 2007
	 */
	public void actionPerformed(ActionEvent e) {
		// user chose to exit TimeTray
		if ( e.getActionCommand() == "quit" ) {
			System.exit( 0 );
		}
		// user requests information about TimeTray
		if ( e.getActionCommand() == "settings" ) {
			// TODO: implement settings menu
			trayIcon.displayMessage(
					"Coming soon...", "Coming soon: settings for TimeTray!", TrayIcon.MessageType.INFO );
		}
	}

	/**
	 * creates a PopUp menu for the TrayIcon
	 *
	 * @return PopUp menu for the TrayIcon
	 *
	 * @author Oliver Tacke
	 * @version 1.0, May 2007
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
		menu.add(menuExit);

		menu.addActionListener( this );

		return menu;
	}
	
	/**
	 * sets individual settings or sets default values (not yet)
	 *
	 * @param fontHeight the height of the TrayIcon for determining the font
	 *
	 * @author Oliver Tacke
	 * @version 1.0, Jan 2016
	 */
	private void setPresets( Integer trayHeight ) {
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
		
		// TODO: implement loading settings
	}	
}