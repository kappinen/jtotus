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
 *
 * 
 * TODO: result from scripts
 * http://groovy.codehaus.org/Embedding+Groovy
 */

package org.jtotus.methods;


import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.control.CompilationFailedException;
import org.jtotus.common.MethodResults;


/**
 *
 * @author Evgeni Kappinen
 */
public class DecisionScript implements MethodEntry {

    private String path_to_script = null;
    

    public DecisionScript(String tmp) {
        path_to_script = tmp;
    }


    public void run() {

       File file = new File (path_to_script);
     
     
       if(!file.isFile() || !file.canRead()) {
              return;
          }

       runGroovyScripts(file);
    }


    
    public void runGroovyScripts(File path_to_script){
        ClassLoader parent = getClass().getClassLoader();
        GroovyClassLoader loader = new GroovyClassLoader(parent);
        GroovyObject groovyObject = null;
        Class groovyClass = null;


        try {
            groovyClass = loader.parseClass(path_to_script);
            // let's call some method on an instance
            groovyObject = (GroovyObject) groovyClass.newInstance();


        }
        catch (InstantiationException ex) {
            Logger.getLogger(DecisionScript.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(DecisionScript.class.getName()).log(Level.SEVERE, null, ex);
        }        catch (CompilationFailedException ex) {
            Logger.getLogger(DecisionScript.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DecisionScript.class.getName()).log(Level.SEVERE, null, ex);
        }

        
        Object[] args = {};
        groovyObject.invokeMethod("run", args);
    }

    public String getMethName() {
      // return this.getClass().getName();
      int dot = path_to_script.lastIndexOf(".");
      int sep = path_to_script.lastIndexOf(File.separator);
      return path_to_script.substring(sep + 1, dot);
    }

    public boolean isCallable() {
        return false;
    }

    public MethodResults call() throws Exception {
        //TOD:implement
        throw new UnsupportedOperationException("Not supported yet.");
    }




}
