# timetray
TimeTray is a very simple program that I originally hacked on one day for a former colleague of mine many years ago.
It displays the calender week in a system tray - a feature that Windows still lacks in 2016. Since TimeTray is written in Java, it can run on other operating systems as well, e.g. Linux or MacOS.

If you don't use the "Download ZIP" option but only want to download TimeTray.jar, don't right-click it in the list (!) but left-click on it and get the "RAW" version!

Just make sure that you're running a Java Runtime Engine (e. g. the JRE from [Oracle](http://www.java.com/en/download/ "Oracle")), and put TimeTray.jar into your autostart folder, crontab, whatever...

## to be done
TimeTray is totally working - I hope ;-) I cannot test it on Windows because I don't use Windows. Anyway, allowing to set some parameters would be useful:

* the tray icon's background color
* the tray icon's font color
* the tray icon's font
* an optional offset of -1 or +1 if you're running a locale version of your OS that doesn't match your local calendar customs

I might as well play with a wrapper like [launch4j](http://launch4j.sourceforge.net/ "launch4j") to create a Windows native executable, but that's not my priority. So, you might do that on your own as well ;-)

When will that be done? When it's done.

## license
TimeTray is licensed under the DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE.
