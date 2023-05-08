import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.plot.CategoryMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
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
import java.io.ByteArrayOutputStream;
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
    public static Instant postCovid = Instant.parse("2021-01-01T01:00:00.00Z");
    public static LocalDate postCovidDate = LocalDate.parse("2021-01-01");
    public static LocalDate seasonStart = LocalDate.now().minusMonths(7);
    public static void main(String[] args) {
        try {
            //rankGraph("dev", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static BufferedImage merger() {
        return null;
    }

    public static void generatePlayerCard(String player, JsonObject p, File f) throws IOException, FontFormatException {
        File fontFile = new File("fonts/FuturaPTBold.otf");
        Font futuraBold = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(72f);
        
        BufferedImage template = ImageIO.read(new File("template.png"));
        Graphics2D g2d = template.createGraphics();
        JsonElement bio = p.get("bio");
        int width = template.getWidth();
        int height = template.getHeight();
        int textWidth = width - 100;
        System.out.println(textWidth);
        Color red = (new Color(191, 30, 46));
        Color blue = (new Color(21, 13, 39));
        g2d.setFont(futuraBold);
        g2d.setColor(red);
        int lineHeight = g2d.getFontMetrics().getHeight() / 2;
        FontMetrics metrics = g2d.getFontMetrics();
        int startPlayerName = 307 - metrics.stringWidth(player) / 2;
        g2d.drawString(player, startPlayerName, lineHeight + 10);
        g2d.setFont(futuraBold.deriveFont(40f));
        metrics = g2d.getFontMetrics();
        lineHeight = g2d.getFontMetrics().getHeight();
        String[] lines = new String[0];
        if (bio != null) {
            lines = splitTextIntoLines(bio.getAsString(), metrics, textWidth);
            int x = 55;
            int y = 500 + lineHeight;
            for (int i = 0; i < lines.length && y < height - 25; ++i) {
                g2d.drawString(lines[i], x, y);
                y += lineHeight;
            }
        }
        g2d.setColor(blue);
        g2d.drawString(p.get("tier").getAsString(), 15, lineHeight);
        g2d.setFont(futuraBold.deriveFont(30f));
        g2d.drawString(p.get("town").getAsString(), 467, 780);
        ImageIO.write(template, "png", f);
    }

    private static String[] splitTextIntoLines(String text, FontMetrics metrics, int maxWidth) {
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<String>();

        for (String word : words) {
            if (metrics.stringWidth(line + " " + word) <= maxWidth) {
                line.append(" ").append(word);
            } else {
                lines.add(line.toString());
                line = new StringBuilder(word);
            }
        }
        lines.add(line.toString());

        return lines.toArray(new String[0]);
    }

    static boolean rankGraph(String player, boolean local) throws ParseException {
        StringBuilder p = new StringBuilder(player);
        JsonObject pJson = PlayerBuilder.getPlayer(p);
        JsonArray elos;
        if (local){
            elos = pJson.get("elos").getAsJsonArray();
        }
        else {
            elos = pJson.get("state elos").getAsJsonArray();
        }
		JsonArray times = pJson.get("times").getAsJsonArray();
        double eloStart = elos.get(0).getAsDouble();
        boolean wonAMatch = false;
        TimeSeries series = new TimeSeries(p.toString());
        
        Date cur;
        for(int i = 0; i < elos.size(); ++i){
            cur = df.parse(times.get(i).getAsString());
            RegularTimePeriod rtp = RegularTimePeriod.createInstance(Day.class, cur, TimeZone.getDefault(), Locale.US);
            //System.out.println(cur.toInstant().toString());
            
            if (postCovid.isBefore(cur.toInstant())) {
                double elo = elos.get(i).getAsDouble();
                if (elo <= eloStart) eloStart = elo;
                else wonAMatch = true;
                if (wonAMatch) series.addOrUpdate(rtp, elo);
            }
        }
        TimeSeriesCollection line_chart_dataset = new TimeSeriesCollection(series);
        String localString = local ? "Local" : "State";
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
            p.toString() + " " + localString + " Elo Graph",
            "Date",
            "local Elo",
            line_chart_dataset
        );
        chart.removeLegend();
        chart.setBackgroundPaint(new Color(244, 255, 252));
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
        Font georgia = new Font("Georgia", Font.ROMAN_BASELINE, 24);
        Font georgia16 = georgia.deriveFont(16f);
        yAxis.setLabelFont(georgia16);
        xAxis.setLabelFont(georgia16);
        chart.getTitle().setFont(georgia);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(230, 255, 255));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        
        double value = 1000;
        String label = "Average elo";
        Color color = Color.RED;

        ValueMarker marker = new ValueMarker(value, color, new BasicStroke(2.0f));
        marker.setLabel(label);
        marker.setLabelFont(georgia16);
        marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        marker.setLabelTextAnchor(TextAnchor.BASELINE_RIGHT);

        plot.addRangeMarker(marker, Layer.BACKGROUND);

        // Customize the renderer of the plot
        plot.getRenderer().setSeriesPaint(0, Color.blue);
        plot.getRenderer().setSeriesStroke(0, new BasicStroke(2.0f));
        try {
            OutputStream out = new FileOutputStream("playergraph.png");
            int width = 600; 
            int height = 400;
            ChartUtils.writeScaledChartAsPNG(out, chart, width, height, 4, 4);
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        
    }
}
