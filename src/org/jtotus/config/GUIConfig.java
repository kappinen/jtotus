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

package org.jtotus.config;

import java.util.LinkedList;
import org.jtotus.common.StockNames;
import org.jtotus.crypt.JtotusCrypt;
import org.jtotus.engine.Engine;
import org.jtotus.methods.MethodEntry;

/**
 *
 * @author Evgeni Kappinen
 */
public class GUIConfig {
    public StockNames names = null;
    public String []stockNames = null;

    public String gmailLogin = null;
    public String gmailPassword = null;
    
    public String brokerLogin = null;
    public String brokerPassword = null;

    public String keyRingPassword = null;
    
    public int day_period = 5;
    private boolean debug = false;

    public GUIConfig(){
        if (names==null) {
            names = new StockNames();
        }
        
        stockNames = names.getNames();
    }

    public String []fetchStockNames() {
        if (names==null)
            names = new StockNames();
        
        return names.getNames();
    }

    public LinkedList <MethodEntry> getSupportedMethodsList() {
        Engine engine = Engine.getInstance();
        return  engine.getMethods();
    }


    private JtotusCrypt keyRing() {
      JtotusCrypt keyRing = new JtotusCrypt();

      if (keyRingPassword == null) {
           keyRingPassword = keyRing.createKeyRing();
      }
      return keyRing;
    }

    public String getBrokerLogin() {
        return this.keyRing().decryptWithKeyRing(brokerLogin, keyRingPassword);
    }

    public String getBrokerPassword() {
        return this.keyRing().decryptWithKeyRing(brokerPassword, keyRingPassword);
    }

    public String getGmailLogin() {
        return this.keyRing().decryptWithKeyRing(gmailLogin, keyRingPassword);
    }

    public String getGmailPassword() {
        return this.keyRing().decryptWithKeyRing(gmailPassword, keyRingPassword);
    }

    public void setBrokerLogin(String brokerLogin) {
        this.brokerLogin = this.keyRing().encryptWithKeyRing(brokerLogin, keyRingPassword);
    }

    public void setBrokerPassword(String brokerPassword) {
        this.brokerPassword = this.keyRing().encryptWithKeyRing(brokerPassword, keyRingPassword);

    }

    public void setGmailLogin(String gmailLogin) {
        this.gmailLogin = this.keyRing().encryptWithKeyRing(gmailLogin, keyRingPassword);
        if (debug) {
            System.out.printf("Gmail login before:%s :after %s", this.gmailLogin, gmailLogin);
        }
    }

    public void setGmailPassword(String gmailPassword) {
        this.gmailPassword = this.keyRing().encryptWithKeyRing(gmailPassword, keyRingPassword);
        if (debug) {
            System.out.printf("Gmail pass before:%s :after :%s", gmailPassword, this.gmailPassword);
        }
    }
}

