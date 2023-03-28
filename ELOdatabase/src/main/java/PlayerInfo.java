import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.jfree.chart.JFreeChart; 
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
public class PlayerInfo {
    
    public static void main(String[] args) {
        rankGraph();
    }

    static BufferedImage merger() {
        return null;
    }

    static void rankGraph() {
        int[] Elos = {1400, 1050, 1120, 1100, 1040, 1200, 1600};
        int[] times = {-10, -9, -8, -7, -6, -5, -1};
        XYSeriesCollection line_chart_dataset = new XYSeriesCollection();
        String name = "player1";
        XYSeries series = new XYSeries(name);
        for(int i = 0; i < 7; ++i){
            series.add(times[i], Elos[i]);
        }
        line_chart_dataset.addSeries(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Elo graph",
            "Days ago",
            "Elo",
            line_chart_dataset
            );
        //ChartPanel panel = new ChartPanel(chart);
        try {
            OutputStream out = new FileOutputStream("playergraph.png");
            /* Dimension d = panel.getSize();
            BufferedImage image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            panel.print( g2d );
            g2d.dispose(); */
            int width = 600; 
            int height = 400;
            //panel.setPreferredSize(new Dimension(width, height));
            ChartUtils.writeChartAsPNG(out, chart, width, height);
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}
