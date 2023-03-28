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

import com.google.gson.JsonObject;

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

    static boolean rankGraph(String player) {
        StringBuilder p = new StringBuilder(player);
        JsonObject pJson = PlayerBuilder.getPlayer(p);

        int[][] timesElos = PlayerBuilder.getHistory(pJson);
        XYSeriesCollection line_chart_dataset = new XYSeriesCollection();
        String name = "player1";
        XYSeries series = new XYSeries(name);
        for(int i = 0; i < timesElos.length; ++i){
            series.add(timesElos[0][i], timesElos[1][i]);
        }
        line_chart_dataset.addSeries(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Elo graph",
            "Days ago",
            "Elo",
            line_chart_dataset
            );

        try {
            OutputStream out = new FileOutputStream("playergraph.png");
            int width = 600; 
            int height = 400;
            ChartUtils.writeChartAsPNG(out, chart, width, height);
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
    }
}
