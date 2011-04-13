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

package org.jtotus.threads;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jtotus.common.MethodResults;

/**
 *
 * @author Evgeni Kappinen
 */
public class MethodFuture <T> extends FutureTask<T>{
    private Callable<T> callable = null;
    private InterfaceMethodListner method = null;

    public MethodFuture(Callable<T> task){
        super(task);
        callable = task;
    }


    public void addListener(InterfaceMethodListner listener){
        method = listener;
    }

    @Override
    protected void done(){
        System.out.printf("Callable finished.\n");
        try {
            if (super.isDone() && !super.isCancelled()) {
                T methodResults = super.get();
                //FIXME:dangerous
                if (methodResults != null) {
                    method.putResults((MethodResults) methodResults);
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(MethodFuture.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(MethodFuture.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
