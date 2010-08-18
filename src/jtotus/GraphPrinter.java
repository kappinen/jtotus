/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JInternalFrame;
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
        // Frame, where the chart will be printed
        JInternalFrame mainFrame = null;

        LinkedList<TimeSeries> listOfSeries = null;
        HashMap <String,TimeSeries>seriesMap = null;

        // Create a chart
        private JFreeChart mainChart;
        private ChartPanel mainPanel;


        public GraphPrinter(JInternalFrame tmp) {
            mainFrame = tmp;


            //Create Linked list for series
            listOfSeries = new LinkedList<TimeSeries>();
            seriesMap = new HashMap<String,TimeSeries>();

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


        TimeSeriesCollection dataset = new TimeSeriesCollection();

        Iterator <TimeSeries>timeIter = listOfSeries.iterator();
        while (timeIter.hasNext()) {
            dataset.addSeries(timeIter.next());
        }

        Iterator <String>iterator = seriesMap.keySet().iterator();
        while(iterator.hasNext()){

        }
        
        dataset.setDomainIsPointsInTime(true);

        return dataset;

    }
    
    public void draw() {


        XYDataset dataset = createDataset();

        mainChart = createTimeLine(dataset,"Test");
        if (mainChart == null){
            return;
        }

        mainPanel = new ChartPanel(mainChart, false);
        mainPanel.setMouseZoomable(true, false);
        mainFrame.setContentPane(mainPanel);

    }



    public void testDraw(){
        TimeSeries s1 = new TimeSeries("L&G European Index Trust");
        s1.add(new Day(2,3,2001), 181.8);
        s1.add(new Day(3,3,2001), 167.3);
        s1.add(new Day(4,3,2001), 153.8);
        s1.add(new Day(5,3,2001), 167.6);
        s1.add(new Day(6,3,2001), 158.8);

        listOfSeries.add(s1);

        draw();



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

       draw();
    }

}
