import java.time.LocalDateTime;

public class DataPoint {
    private LocalDateTime datetime;
    private double temperature;
    private double humidity;

    public DataPoint(LocalDateTime datetime, double temperature, double humidity) {
        this.datetime = datetime;
        this.temperature = temperature;
        this.humidity = humidity;
    }

    // Getters and setters
    public LocalDateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

}
