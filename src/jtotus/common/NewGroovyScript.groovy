/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *
 *
 *
 * http://blogs.sun.com/geertjan/entry/netbeans_groovy_editor_in_a
 *
 */

package jtotus.common

import jtotus.common.StockName;

//Example 1
class Callee {
  void hello() {
     println "hello, world"
  }
}

c = new Callee()
c.hello()


//Example 2
def name='Example 2'
println "Hello $name!"


//Example 3
def cl = Class.forName("jtotus.common.StockName").newInstance();
name = new jtotus.common.StockName("Fortum Oyj")
println name.getHexName();

