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

package jtotus.gui.graph;

import java.awt.Color;
import java.awt.Container;
import java.text.SimpleDateFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
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

            XYPlot plot = chart.getXYPlot();

            chart.setBackgroundPaint(Color.white);
            plot.setBackgroundPaint(Color.lightGray);


            //Set Render
            XYLineAndShapeRenderer renderer = getDefaultLine();
            plot.setRenderer(renderer);

            DateAxis axis = (DateAxis) plot.getDomainAxis();

            axis.setDateFormatOverride(new SimpleDateFormat("dd-MM-yyyy"));


            return chart;

         }
    

    private XYLineAndShapeRenderer getDefaultLine(){
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

            renderer.setSeriesLinesVisible(1, true);
            renderer.setSeriesShapesVisible(1, false);

            return renderer;
      }


    private XYDataset createDataset() {

        if (mainDataset == null) {
            mainDataset = new TimeSeriesCollection();
        }

        return mainDataset;

    }
    


    public Container getContainer() {
        return mainPanel;
    }

    public void testDraw(){
        TimeSeries s1 = new TimeSeries("L&G European Index Trust");
        s1.add(new Day(2,3,2001), 181.8);
        s1.add(new Day(3,3,2001), 167.3);
        s1.add(new Day(4,3,2001), 153.8);
        s1.add(new Day(5,3,2001), 167.6);
        s1.add(new Day(6,3,2001), 158.8);


        mainDataset.addSeries(s1);


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
