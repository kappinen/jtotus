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
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
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
import org.joda.time.LocalDate;
import org.jtotus.common.StockUnit;

/**
 *
 * @author Evgeni Kappinen
 */
public class GraphPrinter {
    // Create a chart

    private String mainPlotName = "MainPlot";
    private JFreeChart mainChart = null;
    private ChartPanel mainPanel = null;
    private CombinedDomainXYPlot mainPlot = null;
    private HashMap<String, TimeSeries> seriesMap = null;
    private HashMap<String, XYPlot> plotMap = null;

    public GraphPrinter(String reviewTarget) {

        seriesMap = new HashMap<String, TimeSeries>();
        plotMap = new HashMap<String, XYPlot>();

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

        mainPlot = new CombinedDomainXYPlot(domain);
        mainPlot.setGap(4.0);
        //mainPlot.setOrientation(PlotOrientation.HORIZONTAL);
        mainPlot.setBackgroundPaint(Color.lightGray);
        mainPlot.setRangePannable(true);
        mainPlot.setDomainGridlinesVisible(true);
        mainPlot.setOutlineVisible(true);
        mainPlot.setDomainCrosshairVisible(true);
        mainPlot.setRangeMinorGridlinesVisible(true);


        JFreeChart chart = new JFreeChart(title,
                                         JFreeChart.DEFAULT_TITLE_FONT,
                                         mainPlot,
                                         true);

        chart.setBackgroundPaint(Color.white);


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

        Iterator<TimeSeries> serIter = seriesMap.values().iterator();
        while (serIter.hasNext()) {
            serIter.next().clear();
        }

        return;
    }

    private XYPlot createSubPlot(String plotName, GraphSeriesType type) {

        final TimeSeriesCollection collection = new TimeSeriesCollection();
        final NumberAxis range = new NumberAxis("Value");
        final XYPlot newSubPlot = new XYPlot(collection,
                                            null,
                                            range,
                                            this.getRenderer(type));


        newSubPlot.setDomainCrosshairVisible(true);
        newSubPlot.setRangeCrosshairVisible(true);
        newSubPlot.setDomainGridlinesVisible(true);

        if(!plotMap.containsKey(plotName)) {
            plotMap.put(plotName, newSubPlot);
            mainPlot.add(newSubPlot);
            return newSubPlot;
        }

        return null;
    }

    private XYPlot fetchPlot(String name) {
        if (name == null) {
            name = this.mainPlotName;
        }
        return plotMap.get(name);
    }

    private TimeSeries createTimeSeries(GraphPacket packet) {
        XYPlot addSeriesToPlot = null;
        //TimeSeries with packet.seriesTitle is not found.

        //Create MainPlot for the rest of the packets.
        //This plot will be used if in packet.plotName is null
        if (plotMap.isEmpty()) {
            addSeriesToPlot = this.createSubPlot(this.mainPlotName, GraphSeriesType.SIMPLELINE);
        }

        //If packet.plotName is null, lets use MainPlot
        if (packet.plotName != null)  {
            addSeriesToPlot = plotMap.get(packet.plotName);
            if (addSeriesToPlot == null) {
                addSeriesToPlot = this.createSubPlot(packet.plotName, packet.type);
            }
        }

        //If we are in this function, this means that
        //unique name of timeseries is not found in seriesMap.
        TimeSeries newSeries = new TimeSeries(packet.seriesTitle);
        seriesMap.put(packet.seriesTitle, newSeries);

        return newSeries;
    }

    private Day localDateToDay(LocalDate date) {
        Day tmpDay = new Day(date.getDayOfMonth(),
                date.getMonthOfYear(),
                date.getYear());

        return tmpDay;
    }


    /*
     * Creates new plot if neeed and adds values to time series
     *
     * @param packet Packet Object from blocking queus
     */
    public void drawSeries(GraphPacket packet) {
        TimeSeries series = null;

        if (seriesMap.containsKey(packet.seriesTitle)) { //Series Exists
            series = seriesMap.get(packet.seriesTitle);
        }else {
            series = this.createTimeSeries(packet);
            //FIXME: is it prefereable to add series after points are added ?
            TimeSeriesCollection collection = (TimeSeriesCollection) this.fetchPlot(packet.plotName).getDataset();
            collection.addSeries(series);
        }

        Iterator<StockUnit> iter = packet.results.iterator();
            while (iter.hasNext()) {
                //update if already existing series
                StockUnit unit = iter.next();
                if(unit.annotation != null) {
                    final XYTextAnnotation annotation = new XYTextAnnotation(unit.annotation,
                                                                             this.localDateToDay(unit.date).getMiddleMillisecond(),
                                                                             unit.value);
                    annotation.setFont(new Font("SansSerif", Font.PLAIN, 9));
                    this.fetchPlot(packet.plotName).addAnnotation(annotation);
                }

                series.addOrUpdate(this.localDateToDay(unit.date),
                                   unit.value);
        }
    }
}
