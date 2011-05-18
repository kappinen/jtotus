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
package brokerwatcher.indicators;

import brokerwatcher.eventtypes.StockTick;
import brokerwatcher.ranalyzer.Rexecutor;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RserveException;

/**
 *
 * @author Evgeni Kappinen
 */
public class SimpleTechnicalIndicators extends StockIndicator<StockTick> {

    public static DataTypes dataTypes;
    public enum DataTypes {
        VOLUME,
        LATESTPRICE,
        LATESTBUY,
        LATESTSELL,
        LATESTHIGH,
        LATESTLOW,
        REVENUE
    }

    public SimpleTechnicalIndicators() {
        super();
    }

    public double VOLUME(int i) {
        return super.getTick(i).getVolume();
    }

    public double LATESTPRICE(int i) {
        return super.getTick(i).getLatestPrice();
    }

    public double LATESTHIGH(int i) {
        return super.getTick(i).getLatestHighest();
    }

    public double LATESTLOW(int i) {
        return super.getTick(i).getLatestLowest();
    }

    // Volume Rate of Change (VROC)
    // Source: http://www.mysmp.com/technical-analysis/volume-rate-of-change.html
    public double vroc(int iIndex, int n) {
        if (iIndex - n <= 0) {
            return 0;
        }

        return ((VOLUME(iIndex) - VOLUME(iIndex - n)) / VOLUME(iIndex - n)) * 100;
    }

    public double vrocMultPrice(int iIndex, int n) {
        if (iIndex - n <= 0) {
            return 0;
        }

        double volume = ((VOLUME(iIndex) - VOLUME(iIndex - n)) / VOLUME(iIndex - n)) * 100;
        double curPrice = ((LATESTPRICE(iIndex) - LATESTPRICE(iIndex - n)) / LATESTPRICE(iIndex - n)) * 100;
        return (volume * curPrice);
    }

    public static double vrocVolume(double ithVoluome, double iNthVoluome) {
        return ((ithVoluome - iNthVoluome) / iNthVoluome) * 100;
    }

    // Price and Volume Trend, for dayily data
    // Source: http://en.wikipedia.org/wiki/Volume_Price_Trend
    public double vptRecursive(int ithIndex) {
        if (ithIndex - 1 < 0) {
            return 0.0d;
        }
        return ((LATESTPRICE(ithIndex) - LATESTPRICE(ithIndex - 1)) / LATESTPRICE(ithIndex - 1)) / VOLUME(ithIndex) + vptRecursive(ithIndex - 1);
    }

    //Accumulation/distribution index
    //Source: http://en.wikipedia.org/wiki/Accumulation/distribution_index
    public double accdistIndexRecursive(int ithIndex) {
        if (ithIndex - 1 < 0) {
            return 0.0d;
        }

        double clv = ((LATESTPRICE(ithIndex) - LATESTLOW(ithIndex))
                      - (LATESTHIGH(ithIndex) - LATESTPRICE(ithIndex)))
                      / (LATESTHIGH(ithIndex) - LATESTLOW(ithIndex));

        double accdist = accdistIndexRecursive(ithIndex - 1) + VOLUME(ithIndex)*clv;
        
        return accdist;
    }

    public double accdistIndexRecursiveVroc(int ithIndex, int n) {
        if (ithIndex - 1 < 0) {
            return 0.0d;
        }

        double clv = ((LATESTPRICE(ithIndex) - LATESTLOW(ithIndex))
                      - (LATESTHIGH(ithIndex) - LATESTPRICE(ithIndex)))
                      / (LATESTHIGH(ithIndex) - LATESTLOW(ithIndex));

        double accdist = accdistIndexRecursiveVroc(ithIndex - 1, n) + (vrocMultPrice(ithIndex, n))*clv;

        return accdist;
    }

    public double[] RSI(double[] input,
                        MInteger outBegIdxDec,
                        MInteger outNbElementDec,
                        int decRSIPeriod) {

        int intput_size = input.length - 1;
        final Core core = new Core();
        final int allocationSizeDecision = intput_size - core.rsiLookback(decRSIPeriod);


        if (allocationSizeDecision <= 0) {
            System.err.printf("No data for period (%d)\n", allocationSizeDecision);
            return null;
        }

        double[] outputDec = new double[allocationSizeDecision];


        RetCode decCode = core.rsi(0, intput_size - 1,
                input, decRSIPeriod,
                outBegIdxDec, outNbElementDec,
                outputDec);

        if (decCode.compareTo(RetCode.Success) != 0) {
            //Error return empty method results
            throw new java.lang.IllegalStateException("RSI failed:" + decRSIPeriod
                    + " Begin:" + outBegIdxDec.value
                    + " NumElem:" + outNbElementDec.value + "\n");
        }

        return outputDec;
    }



    private static Rexecutor rexec = null;
    public static double correlation(double a[], double b[]) {

        if (rexec==null) {
            rexec = new Rexecutor();
        }
        
        double cor = 0.0;
        try {

            rexec.getConnection().assign("a", a);
            rexec.getConnection().assign("b", b);

            RList list = rexec.getConnection()
                            .eval("cor.test(log(a), log(b), method=\"spearman\")")
                            .asList();


            cor = list.at("estimate").asDouble();
//            cor = Math.pow(cor, 2);
        } catch (REXPMismatchException ex) {
            Logger.getLogger(SimpleTechnicalIndicators.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REngineException ex) {
            Logger.getLogger(SimpleTechnicalIndicators.class.getName()).log(Level.SEVERE, null, ex);
        }

        return cor;
    }

    public static double[] crossCorrelation(double a[], double b[], String aName, String bName) {
        int maxLag;
        if (rexec == null) {
            rexec = new Rexecutor();

        }

        double []cor = new double[2];
        try {

            rexec.getConnection().assign("a", a);
            rexec.getConnection().assign("b", b);
            maxLag = Math.max(30 , a.length / 5 );

            //estimate
            cor[0] = rexec.getConnection().eval("jpeg('crossCor"+aName+"_"+bName+".jpg');"
                                        + "ab.ccf <- ccf(diff(a),diff(b), lag.max = "+maxLag+", main=\""+aName+" and "+bName+"\");"
                                        + "dev.off();"
                                        + "max(ab.ccf$acf)").asDouble();

            //lag
            cor[1] = rexec.getConnection().eval("ab.indx <- which(ab.ccf$acf==max(ab.ccf$acf));"
                                                    + "ab.ccf$lag[ab.indx]").asDouble();

            System.out.printf("Cross-Correlation estimate:%f with lag:%f\n", cor[0], cor[1]);

        } catch (REXPMismatchException ex) {
            Logger.getLogger(SimpleTechnicalIndicators.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REngineException ex) {
            Logger.getLogger(SimpleTechnicalIndicators.class.getName()).log(Level.SEVERE, null, ex);
        }

        return cor;
    }

    //TODO:
    //http://en.wikipedia.org/wiki/Rate_of_change_%28technical_analysis%29
    //http://tadoc.org/indicator/ADOSC.htm
    //http://stockcharts.com/help/doku.php?id=chart_school:technical_indicators:force_index

    public static void plotAB(double a[], double b[]) {

        if (rexec == null) {
            rexec = new Rexecutor();

        }
        
        try {

            rexec.getConnection().assign("a", a);
            rexec.getConnection().assign("b", b);

            rexec.getConnection().eval("plot(scale(a), type=\"l\", col=1, xlim=c(0,"+a.length+"), ylim=c(-5,5));");
            rexec.getConnection().eval("lines(scale(b), col=2);");
//                                        + "lines(b, col=\"red\");");
        } catch (REngineException ex) {
            Logger.getLogger(SimpleTechnicalIndicators.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void plot(double a[], double b[]) {

        if (rexec == null) {
            rexec = new Rexecutor();

        }

        try {

            rexec.getConnection().assign("a", a);
            rexec.getConnection().assign("b", b);

            rexec.getConnection().eval("plot(a, b, type=\"l\", col=1);");
//                                        + "lines(b, col=\"red\");");
        } catch (REngineException ex) {
            Logger.getLogger(SimpleTechnicalIndicators.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static double[] autocorrelation(double a[]) {

        if (rexec == null) {
            rexec = new Rexecutor();
        }

        double []cor = null;
        try {
            cor[0] = execute("a<-%d; a.ccf<-acf(a, lag=100);max(a.ccf$acf)", a).asDouble();

            //lag
            cor[1] = execute("a.indx <- which(a.ccf$acf==max(a.ccf$acf));"
                                                    + "a.ccf$lag[a.indx]").asDouble();

            System.out.printf("Cross-Correlation estimate:%f with lag:%f\n", cor[0], cor[1]);

                //            cor = Math.pow(cor, 2);
        } catch (REXPMismatchException ex) {
                Logger.getLogger(SimpleTechnicalIndicators.class.getName()).log(Level.SEVERE, null, ex);
        } catch (REngineException ex) {
            Logger.getLogger(SimpleTechnicalIndicators.class.getName()).log(Level.SEVERE, null, ex);
        }

        return cor;
    }

    //FIXME: (y <- c(3,3,)) assigments
    //FIXME: y = c(1,2);
    private static final Pattern patternForAssign = Pattern.compile("([\\w]+)[\\s]*<-[\\s]*%d[\\s]*;");
    private static String [] paramParser(String cmds) {
        ArrayList<String> listOfCmds = new ArrayList<String>();
        StringBuilder cmd = new StringBuilder();
        boolean startToSkip = false;
        char []cmdsAsChar = cmds.toCharArray();

        for (char c : cmdsAsChar) {

            if (c == '\'') {
                startToSkip = !startToSkip;
                cmd.append(c);
            } else if (c == ';') {
                if (startToSkip) {
                    cmd.append(c);
                } else {
                    cmd.append(c);
                    listOfCmds.add(cmd.toString());
                    cmd = new StringBuilder();
                }
            } else {
                cmd.append(c);
            }
        }
        listOfCmds.add(cmd.toString());
        
        return (String[]) listOfCmds.toArray(new String[listOfCmds.size()]);
    }

    public static REXP execute(final String command, double[]...b) throws REngineException, REXPMismatchException {
        REXP retValue = null;
        int counter = 0;
        
        if (rexec == null) {
            rexec = new Rexecutor();
        }


        String[] cmds = paramParser(command);
            for (String cmd : cmds) {
                Matcher m = patternForAssign.matcher(cmd);
                if (m.find()) {
                    String name = m.group(1);
                    System.out.printf("Assigning:%s\n", name);
                    rexec.getConnection().assign(name, b[counter++]);
                } else {
                    retValue = rexec.getConnection().eval(cmd);
                }
            }

        return retValue;
    }

    //TODO:
    //http://en.wikipedia.org/wiki/Rate_of_change_%28technical_analysis%29
    //http://tadoc.org/indicator/ADOSC.htm
    //http://stockcharts.com/help/doku.php?id=chart_school:technical_indicators:force_index
}
