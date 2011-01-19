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
public interface TickInterface extends UpdateListener{

    public void subscribeForTicks();
    public String getName();

}
