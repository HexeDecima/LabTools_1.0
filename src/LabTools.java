import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static javafx.application.Application.launch;

public class LabTools extends Application {
    // Class-level variables
    private BorderPane root; // The main layout
    private List<DataPoint> dataPoints; // The list of data points
    private DatePicker startDatePicker; // Starting date picker

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("LabTools - Vizualizace teploty a vlhkosti");

        // Test
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        System.out.println("Classpath: " + System.getProperty("java.class.path"));

        // Set the application icon
        InputStream iconStream = getClass().getResourceAsStream("/production/LabTools/resources/LabTools.png");
        if (iconStream != null) {
            primaryStage.getIcons().add(new Image(iconStream));
        } else {
            System.err.println("Icon resource not found: /production/LabTools/resources/LabTools.png");
        }

        // Create the ArduinoData directory if it doesn't exist
        createArduinoDataDirectory();

        // Create the root layout
        root = new BorderPane();

        // Add the date picker and range buttons
        addStartDatePicker(root);
        addRangeButtons(root);

        // Load data
        String filePath = Paths.get(System.getProperty("user.home"), "ArduinoData", "arduino_data.csv").toString();
        dataPoints = loadDataFromCSV(filePath);

        // Display the initial chart
        updateChartWithRange(Period.ofDays(7)); // Default to last week

        // Create the scene
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createArduinoDataDirectory() {
        // Get the path to the home directory and append "ArduinoData"
        String homeDir = System.getProperty("user.home");
        java.nio.file.Path arduinoDataPath = Paths.get(homeDir, "ArduinoData");

        // Check if the directory already exists, and if not, create it
        try {
            if (!Files.exists(arduinoDataPath)) {
                Files.createDirectories(arduinoDataPath);
                System.out.println("Directory created: " + arduinoDataPath);
            } else {
                System.out.println("Directory already exists: " + arduinoDataPath);
            }
        } catch (IOException e) {
            System.err.println("Failed to create directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<DataPoint> loadDataFromCSV(String filePath) {
        List<DataPoint> dataPoints = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Adjust the pattern if needed

        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            for (String line : lines.subList(1, lines.size())) { // Skip the header line
                String[] fields = line.split(",");
                LocalDateTime datetime = LocalDateTime.parse(fields[0], formatter);
                double temperature = Double.parseDouble(fields[1]);
                double humidity = Double.parseDouble(fields[2]);
                dataPoints.add(new DataPoint(datetime, temperature, humidity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataPoints;
    }

    private void addStartDatePicker(BorderPane root) {
        // Create the starting date picker
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Od"); // "From" in Czech
        startDatePicker.setValue(LocalDate.now().minusDays(7)); // Default to last week

        // Add listener to update the chart when the date is changed
        startDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateChartWithRange(Period.ofDays(7)); // Default to last week if the range buttons haven't been used
        });

        // Add the date picker to an HBox
        HBox datePickerBox = new HBox(10, new Label("Od:"), startDatePicker);
        datePickerBox.setAlignment(Pos.CENTER);
        datePickerBox.setPadding(new Insets(10));

        // Add the date picker to the top of the BorderPane
        root.setTop(datePickerBox);
    }

    private void addRangeButtons(BorderPane root) {
        Button hourButton = new Button("Hodina");
        Button dayButton = new Button("Den");
        Button weekButton = new Button("Týden");
        Button monthButton = new Button("Měsíc");
        Button threeMonthsButton = new Button("3 měsíce");
        Button sixMonthsButton = new Button("6 měsíců");
        Button yearButton = new Button("Rok");

        // Set button actions to trigger the chart update with the correct range
        hourButton.setOnAction(e -> updateChartWithRange(Duration.ofHours(1)));
        dayButton.setOnAction(e -> updateChartWithRange(Duration.ofDays(1)));
        weekButton.setOnAction(e -> updateChartWithRange(Period.ofWeeks(1)));
        monthButton.setOnAction(e -> updateChartWithRange(Period.ofMonths(1)));
        threeMonthsButton.setOnAction(e -> updateChartWithRange(Period.ofMonths(3)));
        sixMonthsButton.setOnAction(e -> updateChartWithRange(Period.ofMonths(6)));
        yearButton.setOnAction(e -> updateChartWithRange(Period.ofYears(1)));

        // Add the buttons to an HBox
        HBox rangeButtonsBox = new HBox(10, hourButton, dayButton, weekButton, monthButton, threeMonthsButton, sixMonthsButton, yearButton);
        rangeButtonsBox.setAlignment(Pos.CENTER);
        rangeButtonsBox.setPadding(new Insets(10));

        // Add the buttons to the bottom of the BorderPane
        root.setBottom(rangeButtonsBox);
    }

    private LineChart<Number, Number> createChart(List<DataPoint> originalDataPoints, DateTimeFormatter formatter) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Čas"); // Time in Czech
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Hodnota"); // Value in Czech

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Teplota a vlhkost v průběhu času");

        XYChart.Series<Number, Number> temperatureSeries = new XYChart.Series<>();
        temperatureSeries.setName("Teplota");
        XYChart.Series<Number, Number> humiditySeries = new XYChart.Series<>();
        humiditySeries.setName("Vlhkost");

        if (originalDataPoints.isEmpty()) {
            System.out.println("No data points were selected after filtering.");
            return lineChart;
        }

        long minEpochMilli = Long.MAX_VALUE;
        long maxEpochMilli = Long.MIN_VALUE;

        for (DataPoint dataPoint : originalDataPoints) {
            long epochMilli = dataPoint.getDatetime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            temperatureSeries.getData().add(new XYChart.Data<>(epochMilli, dataPoint.getTemperature()));
            humiditySeries.getData().add(new XYChart.Data<>(epochMilli, dataPoint.getHumidity()));

            if (epochMilli < minEpochMilli) {
                minEpochMilli = epochMilli;
            }
            if (epochMilli > maxEpochMilli) {
                maxEpochMilli = epochMilli;
            }
        }

        // Set the x-axis bounds
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(minEpochMilli);
        xAxis.setUpperBound(maxEpochMilli);
        double tickUnit = (maxEpochMilli - minEpochMilli) / (double) Math.max(1, originalDataPoints.size() - 1);
        xAxis.setTickUnit(tickUnit);
        xAxis.setTickLabelRotation(45);
        xAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(xAxis) {
            @Override
            public String toString(Number object) {
                return formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(object.longValue()), ZoneId.systemDefault()));
            }
        });

        // Add the series to the chart
        lineChart.getData().addAll(temperatureSeries, humiditySeries);

        // Apply the style to the humidity series after the chart is rendered
        Platform.runLater(() -> {
            if (humiditySeries.getNode() != null) {
                // Change the line color for the humidity series
                humiditySeries.getNode().setStyle("-fx-stroke: blue;"); // Change to your desired line color

                // Change the color of the dots (symbols) for each data point
                for (XYChart.Data<Number, Number> data : humiditySeries.getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-background-color: blue, white;"); // Set the dot color
                    }
                }

                // Update the legend color for the humidity series
                Node legendSymbol = lineChart.lookup(".chart-legend-item-symbol.default-color1"); // Assuming series1 is for humidity
                if (legendSymbol != null) {
                    legendSymbol.setStyle("-fx-background-color: blue, white;"); // Match the series color
                }
            }
        });

        return lineChart;
    }

    private List<DataPoint> downsampleData(List<DataPoint> dataPoints, int targetPoints) {
        if (dataPoints.size() <= targetPoints) {
            return dataPoints; // No need to downsample if the data is already within the target range
        }

        List<DataPoint> downsampledData = new ArrayList<>();
        double step = (double) dataPoints.size() / targetPoints;

        for (int i = 0; i < targetPoints; i++) {
            int index = (int) (i * step);
            downsampledData.add(dataPoints.get(index));
        }

        return downsampledData;
    }

    private void updateChartWithRange(TemporalAmount range) {
        LocalDate startDate = startDatePicker.getValue();
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = startDateTime.plus(range);

        // Filter data based on the date range
        List<DataPoint> filteredDataPoints = dataPoints.stream()
                .filter(dp -> !dp.getDatetime().isBefore(startDateTime) && !dp.getDatetime().isAfter(endDateTime))
                .collect(Collectors.toList());

        // Determine the number of points and date format based on the selected range
        int targetPoints;
        DateTimeFormatter formatter;
        if (range.equals(Duration.ofHours(1))) {
            targetPoints = 60; // 60 points for an hour
            formatter = DateTimeFormatter.ofPattern("HH:mm");
        } else if (range.equals(Duration.ofDays(1))) {
            targetPoints = 24; // 24 points for a day
            formatter = DateTimeFormatter.ofPattern("dd.MM., HH:mm");
        } else if (range.equals(Period.ofWeeks(1))) {
            targetPoints = 14; // 14 points for a week
            formatter = DateTimeFormatter.ofPattern("dd.MM., HH:mm");
        } else if (range.equals(Period.ofMonths(1))) {
            targetPoints = 30; // 30 points for a month
            formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        } else if (range.equals(Period.ofMonths(3))) {
            targetPoints = 12; // 12 points for 3 months
            formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        } else if (range.equals(Period.ofMonths(6))) {
            targetPoints = 36; // 36 points for 6 months
            formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        } else if (range.equals(Period.ofYears(1))) {
            targetPoints = 12; // 12 points for a year
            formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        } else {
            // Default case
            targetPoints = 100; // Fallback if range is unspecified
            formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        }

        // Downsample the data based on the determined target points
        List<DataPoint> downsampledData = downsampleData(filteredDataPoints, targetPoints);

        // Update the chart with the filtered and downsampled data
        LineChart<Number, Number> lineChart = createChart(downsampledData, formatter);
        root.setCenter(lineChart);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

