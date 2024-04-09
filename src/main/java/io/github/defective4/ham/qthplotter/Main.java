package io.github.defective4.ham.qthplotter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class Main {
    private static MapPlotter plotter = new MapPlotter();

    private static BufferedImage locationPoint;

    static {
        try (InputStream is = Main.class.getResourceAsStream("/location.png")) {
            locationPoint = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Main() {
    }

    public static void main(String[] args) {
        JFrame win = new JFrame("FT8 Plotter (BETA)");
        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(17, 2, 2, 1));

        JLabel mapFile = new JLabel();
        JLabel logFile = new JLabel();

        FileChooserButton mapChooser = new FileChooserButton(mapFile);
        mapChooser.setListener(file -> {
            if (file != null) {
                try {
                    plotter.setBaseMap(ImageIO.read(file));
                } catch (Exception e) {
                    e.printStackTrace();
                    displayError(e, win);
                }
            }
        });
        FileChooserButton logChooser = new FileChooserButton(logFile);
        JComboBox<MapPlotter.ColorMode> colorModeChooser = new JComboBox<>();
        JComboBox<MapPlotter.LocatorMode> locatorModeChooser = new JComboBox<>();
        ColorChooserButton staticColor = new ColorChooserButton(Color.RED);
        for (MapPlotter.ColorMode colorMode : MapPlotter.ColorMode.values())
            colorModeChooser.addItem(colorMode);
        for (MapPlotter.LocatorMode mode : MapPlotter.LocatorMode.values())
            locatorModeChooser.addItem(mode);
        JButton ok = new JButton("Process");
        JSpinner locatorSize = new JSpinner(new SpinnerNumberModel(32, 1, Integer.MAX_VALUE, 1));
        JCheckBox overlay = new JCheckBox("Map overlay");

        mainPanel.add(new Label("Choose a base map:"));
        mainPanel.add(new Label(" "));
        mainPanel.add(mapChooser);
        mainPanel.add(mapFile);
        mainPanel.add(new Label(" "));
        mainPanel.add(new Label(" "));
        mainPanel.add(new Label("Choose a log file:"));
        mainPanel.add(new Label(" "));
        mainPanel.add(logChooser);
        mainPanel.add(logFile);
        mainPanel.add(new Label("Square color mode:"));
        mainPanel.add(new Label(" "));
        mainPanel.add(colorModeChooser);
        mainPanel.add(new Label(" "));
        mainPanel.add(new Label("Static square color:"));
        mainPanel.add(new Label(" "));
        mainPanel.add(staticColor);
        mainPanel.add(new Label(" "));
        mainPanel.add(new Label("Locator mode:"));
        mainPanel.add(new Label(" "));
        mainPanel.add(locatorModeChooser);
        mainPanel.add(new Label(" "));
        mainPanel.add(new Label("Locator size:"));
        mainPanel.add(new Label(" "));
        mainPanel.add(locatorSize);
        mainPanel.add(new Label(" "));
        mainPanel.add(new Label(" "));
        mainPanel.add(new Label(" "));
        mainPanel.add(overlay);
        mainPanel.add(new Label(" "));
        mainPanel.add(ok);

        ok.addActionListener(e -> {
            File lf = logChooser.getFile();
            if (lf == null || plotter.getBaseMap() == null) return;
            plotter.setColorMode((MapPlotter.ColorMode) colorModeChooser.getSelectedItem());
            plotter.setStaticColor(staticColor.getColor());
            plotter.setLocatorMode((MapPlotter.LocatorMode) locatorModeChooser.getSelectedItem());
            plotter.setLocatorSymbol(adjustLocator((int) locatorSize.getValue()));
            plotter.setOverlayMode(overlay.isSelected());

            try {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select output folder");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setApproveButtonText("Save");
                if (chooser.showSaveDialog(win) == JFileChooser.APPROVE_OPTION) {
                    File dir = chooser.getSelectedFile();
                    WSJTXParser.WSJTEntry[] entries = WSJTXParser.parse(lf);
                    List<StationLocator> stations = new ArrayList<>();
                    List<StationLocator> stations2 = new ArrayList<>();
                    try (PrintWriter pw = new PrintWriter(Files.newOutputStream(new File(dir,
                                                                                         "stations.csv").toPath()))) {
                        pw.println("Callsign, Signal (db), Latitude, Longitude, Locator");
                        for (WSJTXParser.WSJTEntry entry : entries) {
                            WSJTXParser.WSJTData data = entry.getData();
                            if (data != null) {
                                Maidenhead.Locator locator = data.getLocator();
                                if (locator != null) {
                                    boolean duplicateLocator = false;
                                    boolean duplicateCS = false;
                                    for (StationLocator el : stations)
                                        if (locator.equals(el.getLocator())) {
                                            duplicateLocator = true;
                                            break;
                                        }

                                    for (StationLocator el : stations2)
                                        if (data.getFrom().equals(el.getCallsign())) {
                                            duplicateCS = true;
                                            break;
                                        }
                                    StationLocator station = new StationLocator(locator,
                                                                                data.getFrom(),
                                                                                entry.getRXSignal());
                                    if (!duplicateCS) {
                                        pw.println(station.getCallsign() + ", " + station.getSignal() + ", " + station.getLocator()
                                                                                                                      .getLatitude() + ", " + station.getLocator()
                                                                                                                                                     .getLongtitude() + ", " + station.getLocator()
                                                                                                                                                                                      .getOrigin());
                                        stations2.add(station);
                                    }
                                    if (!duplicateLocator) stations.add(station);
                                }
                            }
                        }
                    }
                    plotter.plot(stations);
                    JDialog dial = new JDialog();
                    dial.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                    dial.setContentPane(new JOptionPane("Processing..."));
                    dial.pack();
                    dial.setResizable(false);
                    dial.setVisible(true);
                    DateFormat fmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                    if (dir != null && dir.isDirectory()) {
                        dir.mkdirs();
                        ImageIO.write(plotter.getPlottedMap(), "png", new File(dir, "plotted.png"));
                        try (PrintWriter pw = new PrintWriter(Files.newOutputStream(new File(dir,
                                                                                             "calls.csv").toPath()))) {
                            pw.println("Time, Mode, RX Signal, From, To, Reported Signal, Latitude, Longitude, Locator");
                            for (WSJTXParser.WSJTEntry entry : entries) {
                                WSJTXParser.WSJTData data = entry.getData();
                                pw.println(String.join(", ",
                                                       new String[]{fmt.format(new Date(entry.getTime())),
                                                                    entry.getMode(),
                                                                    Integer.toString(entry.getRXSignal()),
                                                                    data.getFrom(),
                                                                    data.getTo() == null ? "" : data.getTo(),
                                                                    data.getType() == WSJTXParser.WSJTData.FTType.REPORT ? Integer.toString(
                                                                            data.getSignal()) : "",
                                                                    data.getLocator() == null ? "n/a, n/a" : data.getLocator()
                                                                                                                 .getLatitude() + ", " + data.getLocator()
                                                                                                                                             .getLatitude() + ", " + data.getLocator()
                                                                                                                                                                         .getOrigin()}));
                            }
                        }
                    }
                    dial.dispose();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });

        win.setContentPane(mainPanel);
        win.pack();
        win.setResizable(false);
        win.setVisible(true);
    }

    private static void displayError(Exception e, JFrame parent) {
        JOptionPane.showMessageDialog(parent, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static BufferedImage adjustLocator(int size) {
        BufferedImage newLocator = new BufferedImage(size, size, locationPoint.getType());
        Graphics2D g2 = newLocator.createGraphics();
        g2.drawImage(locationPoint, 0, 0, size, size, null);
        return newLocator;
    }
}
