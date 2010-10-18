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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBubbleRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author kappiev
 */
public class GraphPrinter {
    // Create a chart

    private JFreeChart mainChart = null;
    private ChartPanel mainPanel = null;
    private TimeSeriesCollection mainDataset = null;
    private XYPlot mainPlot = null;



    public GraphPrinter(String reviewTarget) {
        XYDataset dataset = createDataset();

        mainChart = createTimeLine(dataset, reviewTarget);
        if (mainChart == null){
            return;
        }

        mainPanel = new ChartPanel(mainChart, false);
        mainPanel.setMouseZoomable(true, false);
        mainPanel.setFillZoomRectangle(true);
        mainPanel.setMouseWheelEnabled(true);
        
    }
   
    
    private JFreeChart createTimeLine(XYDataset dataset,
                                      String title)
      {
            JFreeChart chart = ChartFactory.createTimeSeriesChart(title,
                                                                  "Date",
                                                                  "Price",
                                                                  dataset,
                                                                  true,
                                                                  true,
                                                                  false);

            mainPlot = chart.getXYPlot();

            chart.setBackgroundPaint(Color.white);
            mainPlot.setBackgroundPaint(Color.lightGray);

            mainPlot.setRangePannable(false);
            mainPlot.setDomainGridlinesVisible(true);
            mainPlot.setDomainCrosshairLockedOnData(true);
            mainPlot.setOutlineVisible(true);
            
            //Set Render
            XYItemRenderer renderer = this.getDefaultLine();
            mainPlot.setRenderer(renderer);

            DateAxis axis = (DateAxis) mainPlot.getDomainAxis();

            axis.setDateFormatOverride(new SimpleDateFormat("dd-MM-yyyy"));


            return chart;

         }
    

    private XYItemRenderer getDefaultLine(){
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

            renderer.setSeriesLinesVisible(1, true);
            renderer.setSeriesShapesVisible(1, true);
            return renderer;
      }
    
    private XYItemRenderer getdDefualtBuble(){
            XYBubbleRenderer renderer = new XYBubbleRenderer();
            renderer.setAutoPopulateSeriesOutlineStroke(false);
            return renderer;
      }


    private XYDataset createDataset() {

        if (mainDataset == null) {
            mainDataset = new TimeSeriesCollection();
        }
        return mainDataset;
    }
    
    public void setRenderer(int series, GraphSeriesType type) {

        if (type == null) {
            return;
        }
        
        switch(type) {
            case SIMPLELINE:
                mainPlot.setRenderer(series, this.getDefaultLine());
                break;
            case SIMPLEBUBLE:
                mainPlot.setRenderer(series, this.getdDefualtBuble());
                break;
            default:
                mainPlot.setRenderer(series, this.getDefaultLine());
                break;
        }

    }

    public Container getContainer() {
        return mainPanel;
    }

   
    public synchronized void drawSeries(TimeSeries series) {
        mainDataset.addSeries(series);
        return;
    }

    public synchronized void cleanChart() {
        //TODO: dummy remove
        //mainDataset.removeAllSeries();
        return;
    }

}
