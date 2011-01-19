/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author house
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
               ticker.subscribeForTicks();

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
