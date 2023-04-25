import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;

import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.time.*;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.awt.Color;
import java.awt.BasicStroke;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
public class PlayerInfo {
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private static int dpi = 144;
    public static void main(String[] args) {
        try {
            rankGraph("rock");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    static BufferedImage merger() {
        return null;
    }

    static boolean rankGraph(String player) throws ParseException {
        StringBuilder p = new StringBuilder(player);
        JsonObject pJson = PlayerBuilder.getPlayer(p);

        JsonArray elos = pJson.get("elos").getAsJsonArray();
		JsonArray times = pJson.get("times").getAsJsonArray();
        
        TimeSeries series = new TimeSeries(p.toString());
        Instant now = ZonedDateTime.now().minusYears(1).toInstant();
        Date cur;
        for(int i = 0; i < elos.size(); ++i){
            cur = df.parse(times.get(i).getAsString());
            RegularTimePeriod rtp = RegularTimePeriod.createInstance(Day.class, cur, TimeZone.getDefault(), Locale.US);
            //System.out.println(cur.toInstant().toString());
            if (true | now.isBefore(cur.toInstant())) {
                series.addOrUpdate(rtp, elos.get(i).getAsDouble());
            }
        }
        TimeSeriesCollection line_chart_dataset = new TimeSeriesCollection(series);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            p.toString() + " Local Elo Graph",
            "Date",
            "local Elo",
            line_chart_dataset
        );
        chart.removeLegend();
        chart.setBackgroundPaint(Color.white);
        ValueAxis yAxis = chart.getXYPlot().getRangeAxis();
        DateAxis xAxis = (DateAxis) chart.getXYPlot().getDomainAxis(); // get the x-axis
        xAxis.setDateFormatOverride(new SimpleDateFormat("MMM yy"));
        double lower = yAxis.getLowerBound();
        double higher = yAxis.getUpperBound();
        if (lower > 1000) {
            yAxis.setRangeWithMargins(1000, higher);
        } else if (higher < 1000) {
            yAxis.setRangeWithMargins(lower, 1000);
        }
        chart.getTitle().setFont(new Font("Georgia", Font.ROMAN_BASELINE, 24));
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(230, 255, 255));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));

        // Customize the renderer of the plot
        plot.getRenderer().setSeriesPaint(0, Color.blue);
        plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));
        try {
            int width = 600; 
            int height = 400;
            
            BufferedImage image = chart.createBufferedImage(width, height, BufferedImage.TYPE_INT_ARGB, null);
            ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
            File outputfile = new File("playergraph.png");
            ImageOutputStream ios = ImageIO.createImageOutputStream(outputfile);
            writer.setOutput(ios);
            IIOMetadata metadata = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(image), null);
            IIOMetadataNode physNode = new IIOMetadataNode("javax_imageio_1.0");
            physNode.setAttribute("dpiWidth", Integer.toString(dpi));
            physNode.setAttribute("dpiHeight", Integer.toString(dpi));
            IIOMetadataNode metaNode = new IIOMetadataNode("javax_imageio_1.0");
            metaNode.appendChild(physNode);
            metadata.mergeTree("javax_imageio_1.0", metaNode);

            // Write the image with metadata to the output file
            writer.write(metadata, new IIOImage(image, null, metadata), null);

            // Close the streams
            ios.close();
            writer.dispose();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
    }
}
