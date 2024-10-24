import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    public static List<DataPoint> readCSV(String filePath) {
        List<DataPoint> dataPoints = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
}