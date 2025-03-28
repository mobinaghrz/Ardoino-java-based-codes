import com.fazecast.jSerialComm.SerialPort;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;

public class EarthQuickDetector {

    private static final int WINDOW_SIZE = 100;  // Larger window for seismic analysis
    private static final double DETECTION_THRESHOLD = 0.5;  // Minimum g-force to trigger alert
    private static final int CALIBRATION_SAMPLES = 5;
    private static final Color ALERT_COLOR = new Color(255, 50, 50);

    static class SeismographPanel extends JPanel {
        private final Queue<Double> readings = new LinkedList<>();
        private double baseline = 0;
        private boolean calibrated = false;
        private boolean alertActive = false;
        private double maxRecentMagnitude = 0;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Set background based on alert status
            g2d.setColor(alertActive ? new Color(30, 0, 0) : new Color(10, 10, 30));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            int width = getWidth();
            int height = getHeight();
            int margin = 50;

            drawSeismicGrid(g2d, width, height, margin);

            if (!readings.isEmpty()) {
                drawSeismicActivity(g2d, width, height, margin);
            }

            drawStatusInfo(g2d, width, height);
        }

        private void drawSeismicGrid(Graphics2D g2d, int width, int height, int margin) {
            g2d.setColor(new Color(80, 80, 120));
            g2d.setStroke(new BasicStroke(1f));

            // Horizontal center line
            g2d.drawLine(margin, height / 2, width - margin, height / 2);

            // Vertical grid lines (time markers)
            for (int i = 0; i <= 10; i++) {
                int x = margin + (i * (width - 2 * margin) / 10);
                g2d.drawLine(x, margin, x, height - margin);
            }

            // Horizontal grid lines (magnitude markers)
            for (int i = 1; i <= 3; i++) {
                int y = height / 2 - (int) (i * (height - 2 * margin) / 6);
                g2d.drawLine(margin, y, width - margin, y);
                g2d.setColor(Color.WHITE);
                g2d.drawString(i + "g", margin - 40, y + 5);
            }
        }

        private void drawSeismicActivity(Graphics2D g2d, int width, int height, int margin) {
            Path2D path = new Path2D.Double();
            double xScale = (width - 2 * margin) / (double) WINDOW_SIZE;
            double yScale = (height - 2 * margin) / 6.0;  // 0-3g range

            // Start path at first point
            Double[] readingsArray = readings.toArray(new Double[0]);
            path.moveTo(margin, height / 2 - (readingsArray[0] - baseline) * yScale);

            // Draw seismic waveform
            for (int i = 1; i < readingsArray.length; i++) {
                double x = margin + (i * xScale);
                double y = height / 2 - (readingsArray[i] - baseline) * yScale;
                path.lineTo(x, y);

                // Highlight peaks above threshold
                if (Math.abs(readingsArray[i] - baseline) > DETECTION_THRESHOLD) {
                    g2d.setColor(ALERT_COLOR);
                    g2d.fillOval((int) x - 3, (int) y - 3, 6, 6);
                }
            }

            // Draw the main trace
            g2d.setStroke(new BasicStroke(2f));
            g2d.setColor(alertActive ? ALERT_COLOR : new Color(100, 180, 255));
            g2d.draw(path);
        }

        private void drawStatusInfo(Graphics2D g2d, int width, int height) {
            g2d.setFont(new Font("Arial", Font.BOLD, 16));

            // Display calibration status
            String status = calibrated ?
                    String.format("Monitoring | Max: %.2fg", maxRecentMagnitude) :
                    "Calibrating... (Keep sensor still)";
            g2d.setColor(calibrated ? Color.WHITE : Color.YELLOW);
            g2d.drawString(status, 20, 30);

            // Display alert if active
            if (alertActive) {
                g2d.setFont(new Font("Arial", Font.BOLD, 36));
                g2d.setColor(ALERT_COLOR);
                g2d.drawString("EARTHQUAKE DETECTED!", width / 2 - 150, 50);
            }
        }

        public void addReading(double value) {
            if (!calibrated) {
                // Calibration phase
                readings.add(value);

                if (readings.size() >= CALIBRATION_SAMPLES) {
                    baseline = calculateAverage(readings);
                    calibrated = true;
                    readings.clear();
                }
            } else {
                // Monitoring phase
                double adjustedValue = value - baseline;
                readings.add(value);

                // Track maximum magnitude
                double currentMagnitude = Math.abs(adjustedValue);
                if (currentMagnitude > maxRecentMagnitude) {
                    maxRecentMagnitude = currentMagnitude;
                }

                // Update alert status
                alertActive = currentMagnitude > DETECTION_THRESHOLD;

                // Maintain window size
                if (readings.size() > WINDOW_SIZE) {
                    readings.remove();
                    // Reset max magnitude periodically
                    if (readings.size() % 50 == 0) {
                        maxRecentMagnitude = 0;
                    }
                }
            }
            repaint();
        }

        private double calculateAverage(Queue<Double> values) {
            return values.stream().mapToDouble(d -> d).average().orElse(0.0);
        }
    }

    public static void main(String[] args) {
        SerialPort comPort = SerialPort.getCommPort("COM3");
        comPort.setBaudRate(9600);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (!comPort.openPort()) {
            System.out.println("Failed to open serial port.");
            return;
        }

        // Create GUI
        JFrame frame = new JFrame("Earthquake Detector");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 600);

        SeismographPanel seismograph = new SeismographPanel();
        frame.add(seismograph);

        JLabel magnitudeLabel = new JLabel("Current: 0.00g | Max: 0.00g", SwingConstants.CENTER);
        magnitudeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        magnitudeLabel.setForeground(Color.WHITE);
        magnitudeLabel.setPreferredSize(new Dimension(1200, 30));
        frame.add(magnitudeLabel, BorderLayout.SOUTH);

        frame.setVisible(true);

        // Data processing loop

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(comPort.getInputStream()))) {
            System.out.println("Earthquake detector initialized. Waiting for data...");

            while (true) {
                String line = reader.readLine();
                if (line == null) continue;

                line = line.trim();
                try {
                    double magnitude = Double.parseDouble(line);
                    seismograph.addReading(magnitude);

                    // Update label
                    double adjustedMagnitude = magnitude - seismograph.baseline;
                    magnitudeLabel.setText(String.format(
                            "Current: %.2fg | Max: %.2fg | Baseline: %.2fg",
                            adjustedMagnitude, seismograph.maxRecentMagnitude, seismograph.baseline
                    ));

                    // LED Control (NEW CODE)
                    if (seismograph.alertActive) {
                        magnitudeLabel.setForeground(ALERT_COLOR);
                        Toolkit.getDefaultToolkit().beep();
                        comPort.writeBytes("A".getBytes(), 1);  // Send 'A' for ALERT
                    } else {
                        magnitudeLabel.setForeground(Color.WHITE);
                        comPort.writeBytes("N".getBytes(), 1);  // Send 'N' for NORMAL
                    }

                } catch (NumberFormatException e) {
                    System.err.println("Invalid data: " + line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            comPort.closePort();
        }
    }
}