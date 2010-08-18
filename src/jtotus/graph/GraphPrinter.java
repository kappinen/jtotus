/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.graph;

import java.awt.Color;
import java.awt.Container;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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

        LinkedList<TimeSeries> listOfSeries = null;
        HashMap <String,TimeSeries>seriesMap = null;

        // Create a chart
        private JFreeChart mainChart = null;
        private ChartPanel mainPanel = null;
        private TimeSeriesCollection mainDataset = null;

        public GraphPrinter() {

            //Create Linked list for series
            listOfSeries = new LinkedList<TimeSeries>();
            seriesMap = new HashMap<String,TimeSeries>();
            initialize();

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


        mainDataset = new TimeSeriesCollection();

        Iterator <TimeSeries>timeIter = listOfSeries.iterator();
        while (timeIter.hasNext()) {
            mainDataset.addSeries(timeIter.next());
        }

        Iterator <String>iterator = seriesMap.keySet().iterator();
        while(iterator.hasNext()){

        }
        
        mainDataset.setDomainIsPointsInTime(true);

        return mainDataset;

    }
    
    private void initialize() {


        XYDataset dataset = createDataset();

        mainChart = createTimeLine(dataset,"Test");
        if (mainChart == null){
            return;
        }

        mainPanel = new ChartPanel(mainChart, false);
        mainPanel.setMouseZoomable(true, false);
        mainPanel.setFillZoomRectangle(true);
        mainPanel.setMouseWheelEnabled(true);
        


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

        listOfSeries.add(s1);

        mainDataset.addSeries(s1);


    }

   public void addPoint(String seriesTitle, SimpleDateFormat date, float value)
    {

       TimeSeries tmp = seriesMap.get(seriesTitle);

       if (tmp == null) {
       // Key is not found
           TimeSeries tmpSeries = new TimeSeries(seriesTitle);
           Calendar tmpCalendar = date.getCalendar();
           Day tmpDay = new Day(date.DATE_FIELD,
                                date.MONTH_FIELD,
                                date.YEAR_FIELD);
           tmpSeries.add(tmpDay,value);
           
           seriesMap.put(seriesTitle, tmpSeries);
           
       } else {

           Calendar tmpCalendar = date.getCalendar();
           Day tmpDay = new Day(date.DATE_FIELD,
                                date.MONTH_FIELD,
                                date.YEAR_FIELD);
           tmp.add(tmpDay,value);
           
       }

      
    }

}
