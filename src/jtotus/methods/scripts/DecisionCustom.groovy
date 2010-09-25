/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.rulebase

import jtotus.methods.PotentialWithIn;



PotentialWithIn pot = new PotentialWithIn();
Thread th = new Thread(pot);
th.start();
