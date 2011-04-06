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

package org.jtotus.engine;

import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Evgeni Kappinen
 */
public class StartUpLoader {
    protected static StartUpLoader loader = null;
    private ScriptEngineManager mgr = null;
    private ScriptEngine engine = null;

    protected StartUpLoader() {
        
    }


    public synchronized static StartUpLoader getInstance() {
        if (loader==null) {
            loader = new StartUpLoader();

            BasicConfigurator.configure();
            Logger.getRootLogger().setLevel(Level.INFO);
            //DOMConfigurator.configure("log4j.xml");
        }

        return loader;
    }


    // js/JavaScript/Groovy
    public ScriptEngine load(String name) {
        if (mgr == null) {
            mgr = new ScriptEngineManager();
        }
        
        engine = mgr.getEngineByName(name);
        return engine;
    }

    public ScriptEngine getLoadedEngine() {
        return engine;
    }
    
    public ScriptEngineManager getLoadedScriptManager() {
        return mgr;
    }



    void scriptEngineInfo (ScriptEngineManager mgr) {
        List<ScriptEngineFactory> factories =
                mgr.getEngineFactories();
        for (ScriptEngineFactory factory : factories) {
            System.out.println("ScriptEngineFactory Info");
            String engName = factory.getEngineName();
            String engVersion = factory.getEngineVersion();
            String langName = factory.getLanguageName();
            String langVersion = factory.getLanguageVersion();
            System.out.printf("\tScript Engine: %s (%s)\n",
                    engName, engVersion);
            List<String> engNames = factory.getNames();
            for (String name : engNames) {
                System.out.printf("\tEngine Alias: %s\n", name);
            }
            System.out.printf("\tLanguage: %s (%s)\n",
                    langName, langVersion);
        }

    }
    
}
