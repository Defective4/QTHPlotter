package io.github.defective4.ham.qthplotter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class WSJTXParser {
    public static class WSJTData {

        public enum FTType {
            CQ, REPORT, LOCATOR, MISC
        }

        private final String[] raw;

        public WSJTData(String raw) {
            if (raw.split(" ").length != 3) throw new IllegalStateException();
            this.raw = raw.split(" ");
        }

        public FTType getType() {
            FTType type = FTType.MISC;
            if (raw[0].equals("CQ")) {
                return FTType.CQ;
            } else if (Maidenhead.isLocator(raw[2])) {
                return FTType.LOCATOR;
            } else {
                try {
                    if (Integer.parseInt(raw[2]) != 73) return FTType.REPORT;
                } catch (Exception ignored) {
                }
            }
            return type;
        }

        public String getFrom() {
            return (getType() == FTType.CQ ? raw[1] : raw[0]).replace("<", "").replace(">", "");
        }

        public String getTo() {
            return (getType() == FTType.CQ ? null : raw[1]).replace("<", "").replace(">", "");
        }

        public int getSignal() {
            if (getType() != FTType.REPORT) throw new IllegalStateException("Data type must be REPORT!");
            return Integer.parseInt(raw[2]);
        }

        public Maidenhead.Locator getLocator() {
            return Maidenhead.isLocator(raw[2]) ? Maidenhead.decode(raw[2]) : null;
        }

        public String[] getRaw() {
            return raw;
        }

        @Override
        public String toString() {
            return "WSJTData{" + "raw='" + Arrays.toString(raw) + '\'' + '}';
        }
    }

    public static class WSJTEntry {
        private final long time;
        private final int signal;
        private final String mode;
        private final WSJTData data;

        public WSJTEntry(long time, int signal, String mode, String rawData) {
            this.time = time;
            this.signal = signal;
            this.mode = mode;
            this.data = new WSJTData(rawData);

            if (getData().getFrom().equals("<...>")) throw new IllegalStateException();
        }

        @Override
        public String toString() {
            return "WSJTEntry{" + "time=" + time + ", signal=" + signal + ", mode='" + mode + '\'' + ", data=" + data + '}';
        }

        public long getTime() {
            return time;
        }

        public int getRXSignal() {
            return signal;
        }

        public String getMode() {
            return mode;
        }

        public WSJTData getData() {
            return data;
        }
    }

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyMMdd_HHmmss");

    private WSJTXParser() {
    }

    public static WSJTEntry[] parse(File logFile) throws IOException {
        List<WSJTEntry> entries = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(logFile.toPath())) {
            String line;
            while ((line = reader.readLine()) != null) {
                while (line.contains("  ")) line = line.replace("  ", " ");
                String[] split = line.split(" ");
                if (split.length >= 8) {
                    long time;
                    try {
                        time = DATE_FORMAT.parse(split[0]).getTime();
                    } catch (Exception e) {
                        time = -1;
                    }
                    String mode = split[2];
                    int signal;
                    try {
                        signal = Integer.parseInt(split[4]);
                    } catch (Exception e) {
                        signal = -100;
                    }

                    String data = String.join(" ", Arrays.copyOfRange(split, 7, split.length));
                    try {
                        entries.add(new WSJTEntry(time, signal, mode, data));
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return entries.toArray(new WSJTEntry[0]);
    }
}
