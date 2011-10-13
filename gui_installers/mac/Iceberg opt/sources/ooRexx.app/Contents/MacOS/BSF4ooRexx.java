/* Name: BSF4ooRexx.java
   Purpose: Menu bar for ooRexx with BSF4ooRexx
   Author: Manuel Paar, Juergen Hesse
   Version: 1.0

   ------------------- Apache Version 2.0 license --------------------
      Copyright (C) 2011 Manuel Paar, Juergen Hesse

      Licensed under the Apache License, Version 2.0 (the "License");
      you may not use this file except in compliance with the License.
      You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
      implied.
      See the License for the specific language governing permissions and
      limitations under the License.
   -------------------------------------------------------------------
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.imageio.*;

public class BSF4ooRexx implements Runnable, ActionListener
{
  private JFrame frame;
  private JMenuBar menuBar;
  private JMenu ooRexxMenu;
  private JMenu bsfMenu;
  private JMenu installMenu;
  private JMenuItem licenceMenuItem;
  private JMenuItem ooRexxMenuItem;
  private JMenuItem docMenuItem;
  private JMenuItem ooRexxSamplesMenuItem;
  private JMenuItem infoMenuItem;
  private JMenuItem downloadMenuItem;
  private JMenuItem uninstallMenuItem;
  private JMenuItem reinstallMenuItem;
  private JMenuItem samplesMenuItem;
  private JMenuItem utilMenuItem;
  private JMenuItem ooRexxTryMenuItem;

  private ImageIcon reinstallIcon = null;
  private ImageIcon uninstallIcon = null;

  public static void main(String[] args)
  {
    // Enable the Apple-style  
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "ooRexx Menu Bar");
    SwingUtilities.invokeLater(new BSF4ooRexx());
  }

  public void run()
  {
    try {
      // Set the dock icon
      com.apple.eawt.Application.getApplication().setDockIconImage(
         ImageIO.read(this.getClass().getClassLoader().getResource("oorexx.png")));
      // Set the uninstall and reinstall icons
      reinstallIcon = new ImageIcon(this.getClass().getClassLoader().getResource("BSF4RexxReinstall.png"));
      uninstallIcon = new ImageIcon(this.getClass().getClassLoader().getResource("BSF4RexxUninstall.png"));
    } catch (java.io.IOException e) {
        System.out.println(e.getMessage());
    }
    
    frame = new JFrame("ooRexx");
    menuBar = new JMenuBar();

    ooRexxMenu = new JMenu("Open Object Rexx");

    // Create the License menu item
    licenceMenuItem = new JMenuItem("Licence");
    licenceMenuItem.setAccelerator(KeyStroke.getKeyStroke('L', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    licenceMenuItem.addActionListener(this);
    ooRexxMenu.add(licenceMenuItem);

    // Create the ooRexx Homepage menu item
    ooRexxMenuItem = new JMenuItem("ooRexx Home Page");
    ooRexxMenuItem.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    ooRexxMenuItem.addActionListener(this);
    ooRexxMenu.add(ooRexxMenuItem);

    // Create the ooRexxTry menu item
    ooRexxTryMenuItem = new JMenuItem("ooRexxTry");
    ooRexxTryMenuItem.setAccelerator(KeyStroke.getKeyStroke('T', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    ooRexxTryMenuItem.addActionListener(this);
    ooRexxMenu.add(ooRexxTryMenuItem);

    // Create the Documentation menu item
    docMenuItem = new JMenuItem("Documentation");
    docMenuItem.setAccelerator(KeyStroke.getKeyStroke('D', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    docMenuItem.addActionListener(this);
    ooRexxMenu.add(docMenuItem);

    // Create the ooRexx Samples menu item
    ooRexxSamplesMenuItem = new JMenuItem("ooRexx Samples");
    ooRexxSamplesMenuItem.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    ooRexxSamplesMenuItem.addActionListener(this);
    ooRexxMenu.add(ooRexxSamplesMenuItem);


    bsfMenu = new JMenu("BSF4ooRexx");

    // Create the Information menu item
    infoMenuItem = new JMenuItem("Information");
    infoMenuItem.setAccelerator(KeyStroke.getKeyStroke('I', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    infoMenuItem.addActionListener(this);
    bsfMenu.add(infoMenuItem);

    // Create the BSF4ooRexx Download Page menu item
    downloadMenuItem = new JMenuItem("BSF4ooRexx Download Page");
    downloadMenuItem.setAccelerator(KeyStroke.getKeyStroke('B', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    downloadMenuItem.addActionListener(this);
    bsfMenu.add(downloadMenuItem);

    // Create the Utilities menu item
    utilMenuItem = new JMenuItem("Utilities");
    utilMenuItem.setAccelerator(KeyStroke.getKeyStroke('U', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    utilMenuItem.addActionListener(this);
    bsfMenu.add(utilMenuItem);

    // Create the BSF4ooRexx Samples menu item
    samplesMenuItem = new JMenuItem("BSF4ooRexx Samples");
    samplesMenuItem.setAccelerator(KeyStroke.getKeyStroke('E', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    samplesMenuItem.addActionListener(this);
    bsfMenu.add(samplesMenuItem);


    installMenu = new JMenu("Installation");

    // Create the Uninstall menu item
    uninstallMenuItem = new JMenuItem("Uninstall ooRexx with BSF4ooRexx");
    uninstallMenuItem.setAccelerator(KeyStroke.getKeyStroke('X', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    uninstallMenuItem.addActionListener(this);
    
    // Create the Reinstall menu item
    reinstallMenuItem = new JMenuItem("Reinstall ooRexx with BSF4ooRexx");
    reinstallMenuItem.setAccelerator(KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    reinstallMenuItem.addActionListener(this);
    installMenu.add(uninstallMenuItem);
    installMenu.add(reinstallMenuItem);
    
    menuBar.add(ooRexxMenu);
    menuBar.add(bsfMenu);
    menuBar.add(installMenu);
    
    frame.setJMenuBar(menuBar);

    // Show only the menu bar and no window
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(0, 0);
    frame.setLocation(Short.MIN_VALUE, Short.MIN_VALUE);
    frame.setUndecorated(true);
    frame.setVisible(true);
  }
  
  public void actionPerformed(ActionEvent ev)
  {
    Object source = ev.getSource();
  
    try {
        // On-Click events for the menu items
	if (source == licenceMenuItem)
    		Runtime.getRuntime().exec("open /opt/ooRexx/CPLv1.0.txt");
	else if (source == ooRexxMenuItem)
    		Runtime.getRuntime().exec("open http://www.oorexx.org/");
        else if (source == ooRexxTryMenuItem)
                Runtime.getRuntime().exec("/Applications/ooRexx.app/Contents/MacOS/run_ooRexxTry.sh");
	else if (source == docMenuItem)
    		Runtime.getRuntime().exec("open /Applications/ooRexx.app/Contents/documentation/ooRexx/");
	else if (source == ooRexxSamplesMenuItem)
    		Runtime.getRuntime().exec("open /Applications/ooRexx.app/Contents/samples/ooRexx/");
	else if (source == infoMenuItem)
    		Runtime.getRuntime().exec("open /Applications/ooRexx.app/Contents/documentation/BSF4ooRexx/");
    	else if (source == downloadMenuItem)
    		Runtime.getRuntime().exec("open http://sourceforge.net/projects/bsf4oorexx/"); 
    	else if (source == utilMenuItem)
    		Runtime.getRuntime().exec("open /Applications/ooRexx.app/Contents/utilities/");
        else if (source == samplesMenuItem)
    		Runtime.getRuntime().exec("open /Applications/ooRexx.app/Contents/samples/BSF4ooRexx/");
    	else if (source == uninstallMenuItem) {
    		if (JOptionPane.showConfirmDialog(null, "Do you really want to uninstall?", "Uninstall ooRexx with BSF4ooRexx", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, uninstallIcon) == 0) {
			Runtime.getRuntime().exec("sh /Applications/ooRexx.app/Contents/Resources/uninstall.sh uninstall");
			System.exit(0);
		}
	}
        else if (source == reinstallMenuItem) {
    		if (JOptionPane.showConfirmDialog(null, "Do you really want to reinstall?", "Reinstall ooRexx with BSF4ooRexx", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, reinstallIcon) == 0) {
			Runtime.getRuntime().exec("sh /Applications/ooRexx.app/Contents/Resources/uninstall.sh reinstall");
			System.exit(0);
		}
	}

    } catch (java.io.IOException e) {
    	System.out.println(e.getMessage());
    }
  }
}
