/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *
 * TODO: result from scripts
 * http://groovy.codehaus.org/Embedding+Groovy
 */

package jtotus.engine;


import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jtotus.threads.VoterThread;
import org.codehaus.groovy.control.CompilationFailedException;

/**
 *
 * @author kappiev
 */
public class DecisionScript implements VoterThread{

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


        } catch (InstantiationException ex) {
            Logger.getLogger(DecisionScript.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (IllegalAccessException ex) {
            Logger.getLogger(DecisionScript.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (CompilationFailedException ex) {
            Logger.getLogger(DecisionScript.class.getName()).log(Level.SEVERE, null, ex);
            return;
        } catch (IOException ex) {
            Logger.getLogger(DecisionScript.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        Object[] args = {};
        groovyObject.invokeMethod("run", args);
    }

    public String getMethName() {
      // return this.getClass().getName();
      int dot = path_to_script.lastIndexOf(".");
      int sep = path_to_script.lastIndexOf("/");
      return path_to_script.substring(sep + 1, dot);
    }



}
