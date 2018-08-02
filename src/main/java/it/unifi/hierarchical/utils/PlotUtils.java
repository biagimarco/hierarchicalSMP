package it.unifi.hierarchical.utils;

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import it.unifi.hierarchical.analysis.NumericalValues;


public class PlotUtils extends ApplicationFrame {
	private static final long serialVersionUID = 1109735922230651555L;

	private PlotUtils( String applicationTitle , String chartTitle, String xLabel, String yLabel, XYDataset dataset)
	   {
	      super(applicationTitle);
	      JFreeChart xylineChart = ChartFactory.createXYLineChart(
	         chartTitle,
	         xLabel, 
	         yLabel,
	         dataset,
	         PlotOrientation.VERTICAL,
	         true,true,false);
	         
	      ChartPanel chartPanel = new ChartPanel( xylineChart );
	      //chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
	      final XYPlot plot = xylineChart.getXYPlot( );
	      XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
	      renderer.setSeriesPaint( 0 , Color.BLUE );
	      renderer.setSeriesStroke( 0 , new BasicStroke( 1.0f ) );
	      renderer.setSeriesShapesVisible(0, false);
	      plot.setRenderer( renderer ); 
	      setContentPane( chartPanel ); 
	   }

	 private static XYDataset createDataset(double x[], double y[], String seriesName)
	   {
	      final XYSeries series = new XYSeries( seriesName ); 
	      for(int i =0; i<x.length;i++){
	    	  series.add( x[i] , y[i] );	    	  
	      }
          	                
	      final XYSeriesCollection dataset = new XYSeriesCollection( );          
	      dataset.addSeries( series );          
	      return dataset;
	   }

	public static void plot(String applicationTitle , String chartTitle, String xLabel, String yLabel, double x[], double y[], String seriesName) {
		PlotUtils chart = new PlotUtils(applicationTitle, chartTitle, xLabel, yLabel, createDataset(x, y, seriesName));

		chart.pack();
		RefineryUtilities.centerFrameOnScreen(chart);
		chart.setVisible(true);
	}

	public static void plotNumericalValues(String chartTitle, NumericalValues values) {
	    double t[] = new double[values.getValues().length];
	    double p[] = new double[values.getValues().length];
	    for(int time = 0; time < t.length; time++) {
	        double timePoint = time * values.getStep();
	        t[time]= timePoint;
	        p[time] = values.getValues()[time];
	    }
	    plot(chartTitle, chartTitle, "t", "prob", t, p, "");
	}
	
}
