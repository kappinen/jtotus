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

package brokerwatcher.ranalyzer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Evgeni Kappinen
 */
public class Rexecutor {
    private static boolean serverStarted = false;
    private final static Log log = LogFactory.getLog(Rexecutor.class);
    private final static String os = System.getProperty("os.name").toLowerCase();
    private RConnection connection = null;

    /**
     * Starts RServe daemon
     *
     * @return false on failure
     */
    private synchronized static boolean startRServe() {
        BufferedReader stderr = null;

        if (serverStarted) {
            return serverStarted;
        }

        try {
            String rserverStartUp = null;
            log.info("Starting RServe");
            if (os.startsWith("windows")) {
                // lower cased
                rserverStartUp = System.getenv("R_HOME") + File.separator + "library" + File.separator + "Rserve" + File.separator + "Rserve.exe";
            } else {
                rserverStartUp = "/usr/bin/R CMD Rserve --no-save";
            }
            ProcessBuilder builder = new ProcessBuilder(rserverStartUp.split(" "));
            Process process = builder.start();
            stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            int ret = process.waitFor();
            if (ret == 0) {
                log.info("RServe is started");
                serverStarted = true;
            } else {
                String line;
                while ((line = stderr.readLine()) != null) {
                    System.err.println(line);
                    System.err.flush();
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Rexecutor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Rexecutor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (stderr != null) {
                try {
                    stderr.close();
                } catch (IOException ex) {
                    Logger.getLogger(Rexecutor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return serverStarted;
    }


    /**
     * @return the connection
     */
    public RConnection getConnection() throws RserveException {
        if (connection == null || !connection.isConnected()) {
            startRServe();
            connection = new RConnection();
        }

        return connection;
    }

    /**
     * @param connection the connection to set
     */
    public void setConnection(RConnection connection) {
        this.connection = connection;
    }

    public REXP eval(String pattern, Object... arguments) {

        try {
            String cmd = String.format(pattern, arguments);
            return getConnection().eval(cmd);
        } catch (RserveException ex) {
            log.error("Failure in getting connection to RServer");
        } finally {
            //TODO: close/shutdown connection ?
        }

        return new REXP();
    }


}
