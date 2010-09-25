/*
    This file is part of jTotus.

    jTotus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    jTotus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with jTotus.  If not, see <http://www.gnu.org/licenses/>.
*/


package jtotus.gui;




import jtotus.engine.Engine;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;


/**
 * The main class of the application.
 */
public class JtotusApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        JtotusView mainWindow=new JtotusView(this);
        Engine mainEngine=Engine.getInstance();

        mainWindow.setListener(mainEngine);
        mainEngine.setGUI(mainWindow);

        mainEngine.run();
        show(mainWindow);
    }


    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of JtotusApp
     */
    public static JtotusApp getApplication() {
        return Application.getInstance(JtotusApp.class);
    }

    /**
     * Main method launching the application.
     * @param args
     * @param engine 
     */
    public static void main(String[] args) {

        launch(JtotusApp.class, args);
        
    }

}
