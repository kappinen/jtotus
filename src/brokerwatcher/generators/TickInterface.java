/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package brokerwatcher.generators;

import com.espertech.esper.client.UpdateListener;

/**
 *
 * @author house
 */
public interface TickInterface extends UpdateListener {

    public boolean subscribeForTicks();
    public boolean unsubscribeForTicks();
    public void statementForEvents(String stmt);
    
    public String getName();
    public String getListnerInfo();

}
