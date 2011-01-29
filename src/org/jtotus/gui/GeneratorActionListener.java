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

package org.jtotus.gui;

import brokerwatcher.generators.TickInterface;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.AbstractButton;

/**
 *
 * @author Evgeni Kappinen
 */

//http://www.java2s.com/Tutorial/Java/0240__Swing/ListeningtoJCheckBoxMenuItemEventswithanActionListener.htm
public class GeneratorActionListener implements  ActionListener{
    HashMap<String, TickInterface> listeners = null;
    public GeneratorActionListener(HashMap<String, TickInterface> listeners) {
        this.listeners = listeners;
    }

    private void addListeners() {
        Iterator <String> stmts = listeners.keySet().iterator();
           while(stmts.hasNext()) {
               String stringStmt = stmts.next();
               TickInterface ticker = listeners.get(stringStmt);
               ticker.statementForEvents(stringStmt);
               //ticker.subscribeForTicks();
           }
    }

    public void actionPerformed(ActionEvent event) {
        AbstractButton aButton = (AbstractButton) event.getSource();
        boolean selected = aButton.getModel().isSelected();
        
        if (selected) {
            addListeners();
            aButton.setSelected(true);
        } else {
            aButton.setSelected(false);
        }
    }
}
