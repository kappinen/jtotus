package org.jtotus.config;

import java.util.Calendar;

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
* Date: 5/2/11
* Time: 9:55 PM
*/

/*
*
* Time line for train manager
*
* [---IndicatorPeriod---][[---TrainPeriod---][---Cross-Validation/BootstrappingPeriod--]][--TestPeriod---]
*
* IndicatorPeriod, the period, which LongTermIndicators will use.
* TrainPeriod, the period, which will be used to train model
* Cross-Validation: the period for crossValidation
* TestPeriod: the period for estimating residual
*
* Indicator Period will grow up end of the TrainPeriod
*
* if CrossValidationRange.length != 1, considered as points in TrainPeriod
* Times are in days
*
* */

public class ConfTrainWithLongTermIndicators {

    public final int testPeriodLength = 3;
    public final int trainPeriodLength = 100 + testPeriodLength;
    public final int indicatorPeriodLength = 300 + trainPeriodLength;

    public final int []CrossValidationRange = new int []{15};
    public final float maxLost = 0.985f;
    public final float maxWin = 1.012f;


}
