package io.github.defective4.ham.locresolver;

import io.github.defective4.ham.ui.FileChooserButton;

import javax.swing.*;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {

        CountryResolver resolver;
        try {
            resolver = new CountryResolver();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFrame win = new JFrame("Country name decorator");
        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(4, 2, 2, 1));

        JLabel stationsFile = new JLabel();

        FileChooserButton logChooser = new FileChooserButton(stationsFile);
        JButton process = new JButton("Process");

        process.addActionListener(e -> {
            File target = logChooser.getFile();
            if (target.isFile()) {
                try (BufferedReader reader = Files.newBufferedReader(target.toPath())) {
                    String header = reader.readLine();
                    if (!header.replace(", ", ",")
                               .equalsIgnoreCase("Callsign,Signal (db),Latitude,Longitude,Locator")) {
                        throw new IOException("Not a valid stations.csv file!");
                    }
                    List<String> newLines = new ArrayList<>();
                    newLines.add("Callsign, Signal (db), Latitude, Longitude, Locator, Country");
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] split = line.split(", ");
                        if (split.length != 5) throw new IOException("Invalid line length!");
                        String callsign = split[0];
                        String country = resolver.resolve(callsign);
                        if (country == null) country = "Unknown";
                        newLines.add(line + ", " + country);
                    }

                    try (PrintWriter pw = new PrintWriter(new File(target.getParentFile(), "stations_countries.csv"))) {
                        newLines.forEach(pw::println);
                    }

                    JOptionPane.showMessageDialog(win,
                                                  "Saved to stations_countries.csv!",
                                                  "Done!",
                                                  JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(win, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        mainPanel.add(new JLabel("Choose a stations.csv file"));
        mainPanel.add(new JLabel(" "));
        mainPanel.add(logChooser);
        mainPanel.add(stationsFile);
        mainPanel.add(new JLabel(" "));
        mainPanel.add(new JLabel(" "));
        mainPanel.add(process);
        mainPanel.add(new JLabel(" "));

        win.setContentPane(mainPanel);
        win.setResizable(false);
        win.pack();
        win.setVisible(true);
    }
}
