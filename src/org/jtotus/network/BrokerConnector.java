/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jtotus.network;

/**
 *
 * @author house
 */
public interface BrokerConnector {
    public ConnectorState state = ConnectorState.INITIAL;
    
    public String getPage(String pattern, Object... arg);

    public String authenticate(String _LOGIN_URL_,
                               String userToken,
                               String user,
                               String passToken,
                               String encryptedPassword);

    public void close();

    public enum ConnectorState {
                INITIAL,
                CONNECTION_FAILURE,
                CONNECTED,
                AUTHENTICATED
    };

}
