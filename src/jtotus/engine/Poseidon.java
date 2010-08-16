/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *
 *
 *
 *
 *
 *
 * http://stackoverflow.com/questions/482633/in-java-is-it-possible-to-know-whether-a-class-has-already-been-loaded
 * http://www.javadb.com/list-methods-of-a-class-using-reflection
 */

package jtotus.engine;

/**
 *
 * @author kappiev
 */
public class Poseidon extends ClassLoader {





    public boolean classNameFound(String name){



        Object classLoaded = findLoadedClass(name);
        if (classLoaded != null) {
            return true;
        }

        return false;
    }


}
