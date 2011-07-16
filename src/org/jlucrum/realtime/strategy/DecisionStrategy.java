package org.jlucrum.realtime.strategy;

import org.jlucrum.realtime.eventtypes.MarketSignal;
import org.jtotus.common.MethodResults;

import java.util.HashMap;

/**
 * This file is part of JTotus.
 * <p/>
 * jTotus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * jTotus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with jTotus.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
* Created by IntelliJ IDEA.
* Author: Evgeni Kappinen
* Date: 5/3/11
* Time: 7:48 PM
*/
public interface DecisionStrategy {
    MarketSignal makeDecision(HashMap<String,MethodResults> inputs);
}
