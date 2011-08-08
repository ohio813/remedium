/*
 * Create a graph that can be used on web pages.
 */

package utils;



import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author Nuno Brito, 9th of June 2011 in Darmstadt, Germany.
 */
public class Graph {

    private DefaultCategoryDataset data = new DefaultCategoryDataset();


    private String
            //WindowTitle,
            Title,
            titleX,
            titleY;

    public void addValue(int value, String valueX, String valueY){
        data.addValue(value, valueX, valueY);
    }

    public Graph(
            //String newWindowTitle,
            String newTitle,
            String newTitleX,
            String newTitleY){
            //WindowTitle = newWindowTitle;
            Title = newTitle;
            titleX = newTitleX;
            titleY = newTitleY;
    }


    /**
     * do the graph onto a given panel
     */
    public void output(String folderName, String fileName,
            int sizeX, int sizeY){

        // create a chart...
        JFreeChart chart = ChartFactory.createBarChart3D(
                Title,       // chart title
                titleX,                  // domain axis label
                titleY,                     // range axis label
                data,                     // data
                PlotOrientation.VERTICAL,  // orientation
                false,                        // include legend
                true,                        // tooltips
                false                        // urls
                );

        // x = domain
        // y = range

        // remove long decimal and use only integer values
        NumberAxis rangeAxis = (NumberAxis) chart.getCategoryPlot().getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());


        CategoryAxis a = chart.getCategoryPlot().getDomainAxis();

        a.setAxisLineVisible(false);

        File file = new File(folderName, fileName);


        
        ChartUtilities.applyCurrentTheme(chart);
        try {
            ChartUtilities.saveChartAsPNG(file, chart, sizeX, sizeY);
        } catch (IOException ex) {
            Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
        }


    }


}
