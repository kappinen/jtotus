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

/**
 *
 * @author Evgeni Kappinen
 */
public class ConfTickGenerator {

    public int sleepBetweenTicks = 1;


    //Start and end time to generate ticks
    public String timeZone = "Europe/Helsinki";
    public int start_hour = 10;
    public int start_minute = 00;
    public int end_hour = 18;
    public int end_minute = 30;


}
