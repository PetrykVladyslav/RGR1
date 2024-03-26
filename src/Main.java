import org.mariuszgromada.math.mxparser.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    private static JPanel chartPanel;
    private static JLabel statusLabel;
    private static ArrayList<Double> xValuesFromFile = new ArrayList<>();
    public static void main(String[] args) {
        License.iConfirmNonCommercialUse("KarazinUniver");
        JFrame frame = new JFrame("Побудова графіків");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 880);
        JPanel panel = new JPanel();
        frame.add(panel);
        JTextField functionInput = new JTextField(20);
        functionInput.setToolTipText("Введіть функцію тут");
        JTextField startInput = new JTextField(10);
        startInput.setToolTipText("Старт");
        JTextField stopInput = new JTextField(10);
        stopInput.setToolTipText("Кінець");
        JTextField stepInput = new JTextField(10);
        stepInput.setToolTipText("Крок");
        JButton drawButton = new JButton("Побудувати графік");
        JButton fileButton = new JButton("Файл");
        JButton clearButton = new JButton("Очистити");
        statusLabel = new JLabel("Введіть функцію та параметри для побудови графіка");
        panel.add(statusLabel);
        panel.add(functionInput);
        panel.add(new JLabel("Старт:"));
        panel.add(startInput);
        panel.add(new JLabel("Кінець:"));
        panel.add(stopInput);
        panel.add(new JLabel("Крок:"));
        panel.add(stepInput);
        panel.add(drawButton);
        panel.add(fileButton);
        panel.add(clearButton);
        chartPanel = new JPanel();
        panel.add(chartPanel);
        drawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!xValuesFromFile.isEmpty()) {
                    plotGraphFromFile(frame, panel, functionInput, xValuesFromFile);
                } else {
                    plotGraph(frame, panel, startInput, stopInput, stepInput, functionInput);
                }
            }
        });
        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] values = line.split(",");
                            for (String value : values) {
                                value = value.trim();
                                value = value.replaceAll("^\"|\"$", "");
                                if (!value.isEmpty()) {
                                    try {
                                        xValuesFromFile.add(Double.parseDouble(value));
                                    } catch (NumberFormatException ex) {
                                        JOptionPane.showMessageDialog(frame, "Некоректне значення у файлі: " + value);
                                    }
                                }
                            }
                        }
                        JOptionPane.showMessageDialog(frame, "Файл успішно завантажено. Кількість значень: " + xValuesFromFile.size());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Помилка при читанні файла: " + ex.getMessage());
                    }
                }
            }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                functionInput.setText("");
                startInput.setText("");
                stopInput.setText("");
                stepInput.setText("");
                xValuesFromFile.clear();
                XYSeriesCollection dataset = new XYSeriesCollection();
                JFreeChart chart = ChartFactory.createXYLineChart(
                        "Графік функції та її похідної",
                        "x",
                        "y",
                        dataset
                );
                ChartPanel chartPanelComponent = new ChartPanel(chart);
                panel.remove(chartPanel);
                chartPanel = chartPanelComponent;
                panel.add(chartPanel);
                panel.revalidate();
                panel.repaint();
            }
        });

        frame.setVisible(true);
    }
    private static void plotGraph(JFrame frame, JPanel panel, JTextField startInput, JTextField stopInput, JTextField stepInput, JTextField functionInput) {
        String startText = startInput.getText();
        String stopText = stopInput.getText();
        String stepText = stepInput.getText();
        String functionText = functionInput.getText();
        if (startText.isEmpty() || stopText.isEmpty() || stepText.isEmpty() || functionText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Будь ласка, заповніть усі поля введення (Функція, Початок, Кінець, Крок) або завантажте дані з файлу");
            return;
        }
        try {
            double minX = Double.parseDouble(startInput.getText());
            double maxX = Double.parseDouble(stopInput.getText());
            double step = Double.parseDouble(stepInput.getText());
            String filename = "differentiation_result_" + System.currentTimeMillis() + ".txt";
            FileWriter writer = new FileWriter(filename);
            XYSeries functionSeries = new XYSeries("Функція");
            XYSeries derivativeSeries = new XYSeries("Похідна");
            for (double x = minX; x <= maxX; x += step) {
                double y = evalFunction(functionText, x);
                functionSeries.add(x, y);

                double yDerivative = evalDerivative(functionText, x, step);
                derivativeSeries.add(x, yDerivative);
                writer.write(String.format("%.3f, %.3f, %.3f\n", x, y, yDerivative));
            }
            writer.close();
            XYSeriesCollection dataset = new XYSeriesCollection();
            dataset.addSeries(functionSeries);
            dataset.addSeries(derivativeSeries);
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Графік функції та її похідної",
                    "x",
                    "y",
                    dataset
            );
            ChartPanel chartPanelComponent = new ChartPanel(chart);
            panel.remove(chartPanel);
            chartPanel = chartPanelComponent;
            panel.add(chartPanel);
            panel.revalidate();
            panel.repaint();
            statusLabel.setText("Графіки для функції " + functionText);
        } catch (NumberFormatException | IOException e) {
            JOptionPane.showMessageDialog(frame, "Помилка: " + e.getMessage());
        }
    }
    private static void plotGraphFromFile(JFrame frame, JPanel panel, JTextField functionInput, ArrayList<Double> xValues) {
        String functionText = functionInput.getText();
        if (functionText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Будь ласка, введіть функцію");
            return;
        }
        try {
            String filename = "differentiation_result_" + System.currentTimeMillis() + ".txt";
            FileWriter writer = new FileWriter(filename);
            XYSeries functionSeries = new XYSeries("Функція");
            XYSeries derivativeSeries = new XYSeries("Похідна");
            for (double x : xValues) {
                double y = evalFunctionFromTable(functionText, x, xValues);
                functionSeries.add(x, y);
                double step = (xValues.get(1) - xValues.get(0));
                double yDerivative = evalDerivative(functionText, x, step);
                derivativeSeries.add(x, yDerivative);
                writer.write(String.format("%.3f, %.3f, %.3f\n", x, y, yDerivative));
            }
            writer.close();
            XYSeriesCollection dataset = new XYSeriesCollection();
            dataset.addSeries(functionSeries);
            dataset.addSeries(derivativeSeries);
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Графік функції та її похідної",
                    "x",
                    "y",
                    dataset
            );
            ChartPanel chartPanelComponent = new ChartPanel(chart);
            panel.remove(chartPanel);
            chartPanel = chartPanelComponent;
            panel.add(chartPanel);
            panel.revalidate();
            panel.repaint();
            statusLabel.setText("Графіки для функції " + functionText);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Помилка: " + e.getMessage());
        }
    }
    private static double evalFunction(String functionText, double x) {
        Function function = new Function("f(x) = " + functionText);
        Expression e = new Expression("f(" + x + ")", function);
        return e.calculate();
    }
    private static double evalFunctionFromTable(String functionText, double x, ArrayList<Double> xValues) {
        int index = (int) ((x - xValues.get(0)) / (xValues.get(1) - xValues.get(0)));
        if (index < 0 || index >= xValues.size() - 1) {
            return Double.NaN;
        }
        double y0 = evalFunction(functionText, xValues.get(index));
        double y1 = evalFunction(functionText, xValues.get(index + 1));
        double t = (x - xValues.get(index)) / (xValues.get(index + 1) - xValues.get(index));
        return y0 + t * (y1 - y0);
    }
    private static double evalDerivative(String functionText, double x, double h) {
        double f_x1 = evalFunction(functionText, x + h);
        double f_x2 = evalFunction(functionText, x - h);
        return (f_x1 - f_x2) / (2 * h);
    }
}