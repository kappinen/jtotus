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
 * http://tutorials.jenkov.com/java-collections/navigableset.html
 */
package org.jtotus.gui.graph;

import java.awt.Color;
import java.awt.Container;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBubbleRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 *
 * @author Evgeni Kappinen
 */
public class GraphPrinter {
    // Create a chart

    private JFreeChart mainChart = null;
    private ChartPanel mainPanel = null;
    private CombinedDomainXYPlot mainPlot = null;

    private HashMap<String, TimeSeriesCollection> seriesMap = null;

    public GraphPrinter(String reviewTarget) {

        seriesMap = new HashMap<String, TimeSeriesCollection>();

        
        mainChart = this.createChart(reviewTarget);
        if (mainChart == null) {
            return;
        }

        mainPanel = new ChartPanel(mainChart, false);
        mainPanel.setMouseZoomable(true, false);
        mainPanel.setFillZoomRectangle(true);
        mainPanel.setMouseWheelEnabled(true);
    }

    private JFreeChart createChart(String title) {


       // valueAxis.setAutoRangeMinimumSize(1);
        DateAxis domain = new DateAxis("Date");
        domain.setDateFormatOverride(new SimpleDateFormat("dd-MM-yyyy"));

        mainPlot  = new CombinedDomainXYPlot(domain);
        mainPlot.setGap(4.0);
        //mainPlot.setOrientation(PlotOrientation.HORIZONTAL);



        JFreeChart chart = new JFreeChart(title, 
                                JFreeChart.DEFAULT_TITLE_FONT,
                                mainPlot, false);
        
        chart.setBackgroundPaint(Color.white);
        mainPlot.setBackgroundPaint(Color.lightGray);

        mainPlot.setRangePannable(true);
        mainPlot.setDomainGridlinesVisible(true);
        mainPlot.setOutlineVisible(true);
        mainPlot.setDomainCrosshairVisible(true);
        mainPlot.setRangeMinorGridlinesVisible(true);

        return chart;

    }

    private XYItemRenderer getDefaultLine() {
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        renderer.setSeriesLinesVisible(1, true);
        renderer.setSeriesShapesVisible(1, true);
        return renderer;
    }

    private XYItemRenderer getdDefualtBuble() {
        XYBubbleRenderer renderer = new XYBubbleRenderer();
        renderer.setAutoPopulateSeriesOutlineStroke(false);
        return renderer;
    }

    private XYItemRenderer getdDefualtDot() {
        XYDotRenderer renderer = new XYDotRenderer();
        return renderer;
    }

    private XYItemRenderer getDefualtCandleStick() {
        XYStepRenderer renderer = new XYStepRenderer();
        
        return renderer;
    }

    public XYItemRenderer getRenderer(GraphSeriesType type) {
        if (type == null) {
            return this.getDefaultLine();
        }

        
        switch (type) {
            case SIMPLELINE:
                return this.getDefaultLine();
            case SIMPLEBUBLE:
                return this.getdDefualtBuble();
             case SIMPLECANDLESTICK:
                 return this.getDefualtCandleStick();
             case SIMPLEDOT:
                 return this.getdDefualtDot();
            default:
                return this.getDefaultLine();
        }
    }

    public Container getContainer() {
        return mainPanel;
    }

    public synchronized void cleanChart() {
        //TODO: dummy remove
        //mainDataset.removeAllSeries();
        return;
    }


    public TimeSeries createSubPlot(String title, GraphSeriesType type) {
        TimeSeries newSeries = new TimeSeries(title);

        if (type == null) {
            TimeSeriesCollection collection=null;
            
            if(!seriesMap.isEmpty()) {

                Set<String> set = seriesMap.keySet();
                Iterator<String> iter = set.iterator();

                collection = seriesMap.get(iter.next());
                collection.addSeries(newSeries);

                seriesMap.put(title, collection);
                return newSeries;
            }
        }


        final TimeSeriesCollection collection = new TimeSeriesCollection();
        
        collection.addSeries(newSeries);
        
        seriesMap.put(title, collection);

        final NumberAxis range = new NumberAxis("Value");
        final XYPlot newSubPlot = new XYPlot(collection,
                                             null,
                                             range,
                                             this.getRenderer(type));
        

        newSubPlot.setDomainCrosshairVisible(true);
        newSubPlot.setRangeCrosshairVisible(true);

        
        mainPlot.add(newSubPlot);
        return newSeries;
    }



    public void drawSeries(GraphPacket packet) {

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(packet.date);

            Day tmpDay = new Day(cal.get(Calendar.DATE),
                                 cal.get(Calendar.MONTH) + 1,
                                 cal.get(Calendar.YEAR));

            // add point to seriesMap
            if (seriesMap.containsKey(packet.seriesTitle)) { //Series Exists
                TimeSeries hashSeries = seriesMap
                                        .get(packet.seriesTitle)
                                        .getSeries(packet.seriesTitle);
                
                //update if already exists
                if (hashSeries.addOrUpdate(tmpDay, packet.result) != null) {
                    System.err.printf("Warning overwritting existent value in time series:%s :%f :%s\n",
                            packet.seriesTitle, packet.result, tmpDay.toString());
                }

            } else {  // New series

                this.createSubPlot(packet.seriesTitle, packet.type)
                    .add(tmpDay, packet.result);
            }
    }








}
