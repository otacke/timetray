/*
 * TimeTray
 *
 * TimeTray is a tool that adds a symbol to an operating system's icon tray
 * showing the current calender week. Further information about date and time
 * can be obtained by hovering over the symbol with the mouse pointer.
 *
 * Cleaning up the code may be useful.
 *
 * @author Oliver Tacke
 * @version 1.0, May 2007
 */

/*
 * import neccessary Classes
 */
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.MenuItem;
import java.awt.PopupMenu;
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

	/* TrayIcon to be used in system tray */
	private TrayIcon trayIcon;

	/* dimension of the TrayIcon */
	private Dimension iconSize;

	/* image to be shown within TrayIcon */
	private BufferedImage image;

	/* string to be shown as ToolTip text */
	private String dateString;

	/* internal Java calendar for getting date information */
	private Calendar calendar;

	/* SimpleDateFormat for use in connection with the ToolTip text*/
	private SimpleDateFormat sdf;

	/* font to be used */
	private Font font;

	/* text shown as information about the program */
	private static String aboutText =
		"Dieses gaaanz tolle Programm wurde programmiert von Oliver Tacke am 01.05.2007!";

	/* string for setting the SimpleDateFormat */
	private static String sdfString = "w, EEEE, dd. MMMM yyyy, HH:mm:ss";


	/* popupMenu to be used in connection with the TrayIcon */
	private PopupMenu menu = this.createMenu();

	/**
	 * TimeTray Constructor
	 *
	 * @author Oliver Tacke
	 * @version 1.0, May 2007
	 */
	public TimeTray()  {

		/* retrieve iconSize of SystemTray */
		SystemTray systemTray = SystemTray.getSystemTray();
		iconSize = systemTray.getTrayIconSize();

		/* set ToolTipText related stuff, e.g. date format */
		font = new Font("SansSerif", Font.BOLD, iconSize.height-2);
		sdf = new SimpleDateFormat(sdfString);

		/* create TrayIcon according to iconSize */
		image = new BufferedImage(
				iconSize.width,
				iconSize.height,
				BufferedImage.TYPE_INT_RGB);
		trayIcon = new TrayIcon(image, "TimeTray", menu);

		/* run thread and set timer to update every second */
		run();
		try {
			systemTray.add(trayIcon);
		} catch (AWTException ex) {
			ex.printStackTrace();
		}
		Timer timer = new Timer();
		timer.schedule(this, 1000, 1000);
	}

	/**
	 * run method
	 *
	 * @author Oliver Tacke
	 * @version 1.0, May 2007
	 */
	public void run() {

		/* get current date and set ToolTipText accordingly */
		calendar = Calendar.getInstance();
		dateString = "KW " + sdf.format(calendar.getTime());
		trayIcon.setToolTip(dateString);

		/* draw background image including border */
		Graphics g = image.getGraphics();
		g.setColor(new Color(221, 221, 221));
		g.fillRect(0, 0, iconSize.width, iconSize.height);
		g.setColor(new Color(245, 245, 245));
		g.drawLine(0, 0, 0, iconSize.height);
		g.drawLine(0, 0, iconSize.width, 0);
		g.setColor(new Color(197, 197, 197));
		g.drawLine(iconSize.width-1, 0, iconSize.width-1, iconSize.height);
		g.drawLine(0, iconSize.height-1, iconSize.width, iconSize.height-1);

		/* draw number of calendar week centered to the image*/
		g.setColor(Color.black);
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics(font);
		int fontWidth = fm.stringWidth(
				String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR)));
		g.drawString(String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR)),
				(iconSize.width-fontWidth)/2,
				iconSize.height-3);

		/* show drawn image */
		trayIcon.setImage(image);

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

		/* create new TrayIcon if a SystemTray is supported by the OS */
		if (SystemTray.isSupported()) {
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
	private void actionPerformed(ActionEvent e) {

		/* user chose to exit TimeTray */
		if (e.getActionCommand() == "exit") {
			System.exit(0);
		}
		/* user requests information about TimeTray */
		if (e.getActionCommand() == "about") {
			trayIcon.displayMessage(
					"Über TimeTray...",
					aboutText,
					TrayIcon.MessageType.INFO);
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

		PopupMenu menu = new PopupMenu("TimeTray");

		/* about item */
		MenuItem m_about = new MenuItem("über");
		m_about.setActionCommand("about");
		menu.add(m_about);

		/* separator */
		menu.addSeparator();

		/* exit item */
		MenuItem m_exit = new MenuItem("beenden");
		m_exit.setActionCommand("exit");
		menu.add(m_exit);

		menu.addActionListener(this);

		return menu;
	}
}