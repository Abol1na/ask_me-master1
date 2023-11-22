import java.util.Observable;
import java.util.Observer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Strategy Pattern: WeatherDataCollectionStrategy
interface WeatherDataCollectionStrategy {
    void collectData(WeatherData weatherData);
}

class WeatherAPI implements WeatherDataCollectionStrategy {
    public void collectData(WeatherData weatherData) {
        double temperatureCelsius = Math.random() * 50 - 10;
        double humidity = Math.random() * 50 + 50;
        double pressure = Math.random() * 10 + 1013;
        weatherData.setMeasurements(temperatureCelsius, humidity, pressure);
    }
}

class WeatherSensor implements WeatherDataCollectionStrategy {
    public void collectData(WeatherData weatherData) {
        double temperatureCelsius = Math.random() * 50 - 10;
        double humidity = Math.random() * 50 + 50;
        double pressure = Math.random() * 10 + 1013;
        weatherData.setMeasurements(temperatureCelsius, humidity, pressure);
    }
}

// Observer Pattern: WeatherObserver
class WeatherObserver implements Observer {
    private WeatherData weatherData;
    private double temperatureThreshold;

    public WeatherObserver(WeatherData weatherData, double temperatureThreshold) {
        this.weatherData = weatherData;
        this.temperatureThreshold = temperatureThreshold;
        weatherData.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof WeatherData) {
            WeatherData weatherData = (WeatherData) o;
            double temperature = weatherData.getTemperatureCelsius();

            if (temperature < temperatureThreshold) {
                System.out.println("Temperature is below " + temperatureThreshold + "°C. Warning!");
            }
        }
    }
}

// Decorator Pattern: WeatherReportDecorator
abstract class WeatherReportDecorator {
    protected WeatherData weatherData;

    public WeatherReportDecorator(WeatherData weatherData) {
        this.weatherData = weatherData;
    }

    public abstract void display();
}

class TemperatureDecorator extends WeatherReportDecorator {
    private String temperatureScale;

    public TemperatureDecorator(WeatherData weatherData, String temperatureScale) {
        super(weatherData);
        this.temperatureScale = temperatureScale;
    }

    @Override
    public void display() {
        String scaleLabel = "";
        double temperature = 0.0;

        if (temperatureScale.equalsIgnoreCase("Celsius")) {
            scaleLabel = "°C";
            temperature = weatherData.getTemperatureCelsius();
        } else if (temperatureScale.equalsIgnoreCase("Fahrenheit")) {
            scaleLabel = "°F";
            temperature = weatherData.getTemperatureFahrenheit();
        } else if (temperatureScale.equalsIgnoreCase("Kelvin")) {
            scaleLabel = "K";
            temperature = weatherData.getTemperatureKelvin();
        } else {
            System.out.println("Invalid temperature scale. Using the default scale (Celsius).");
            scaleLabel = "°C";
            temperature = weatherData.getTemperatureCelsius();
        }

        System.out.println("Temperature (" + temperatureScale + "): " + temperature + scaleLabel);
    }
}

// State Pattern: WeatherConditionState
interface WeatherConditionState {
    void display();
}

class SunnyState implements WeatherConditionState {
    public void display() {
        System.out.println("Weather: Sunny");
    }
}

class RainyState implements WeatherConditionState {
    public void display() {
        System.out.println("Weather: Rainy");
    }
}

class CloudyState implements WeatherConditionState {
    public void display() {
        System.out.println("Weather: Cloudy");
    }
}

class SnowyState implements WeatherConditionState {
    public void display() {
        System.out.println("Weather: Snowy");
    }
}

// Singleton Pattern: WeatherData
class WeatherData extends Observable {
    private static WeatherData instance;
    private static final String URL = "jdbc:postgresql://localhost:5432/tutdb";
    private static final String USER = "postgres";
    private static final String PASSWORD = "presented01";
    public WeatherConditionState conditionState;
    public void setConditionState(WeatherConditionState conditionState) {
        this.conditionState = conditionState;
    }
    private double temperatureCelsius;
    private double humidity;
    private double pressure;


    private WeatherData() {
        // Private constructor to prevent external instantiation.
    }

    public static WeatherData getInstance() {
        if (instance == null) {
            instance = new WeatherData();
        }
        return instance;
    }

    public void setMeasurements(double temperatureCelsius, double humidity, double pressure) {
        this.temperatureCelsius = temperatureCelsius;
        this.humidity = humidity;
        this.pressure = pressure;

        if (temperatureCelsius > 25.0) {
            conditionState = new SunnyState();
        } else if (temperatureCelsius > 15.0) {
            conditionState = new CloudyState();
        } else if( temperatureCelsius > 0.0) {
            conditionState = new RainyState();
        } else {
            conditionState = new SnowyState();
        }

        measurementsChanged();
    }

    public void measurementsChanged() {
        setChanged();
        notifyObservers();
    }

    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public double getTemperatureFahrenheit() {
        return (temperatureCelsius * 9 / 5) + 32;
    }

    public double getTemperatureKelvin() {
        return temperatureCelsius + 273.15;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getPressure() {
        return pressure;
    }

    public void displayWeatherCondition() {
        conditionState.display();
    }

    public void setWeatherDataInDatabase(String time) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            String query = "INSERT INTO weather_data (time, temperature, humidity, pressure) VALUES (?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, time);
            preparedStatement.setDouble(2, temperatureCelsius);
            preparedStatement.setDouble(3, humidity);
            preparedStatement.setDouble(4, pressure);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public WeatherData retrieveDataFromDatabase(String time) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        WeatherData retrievedData = new WeatherData();

        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            String query = "SELECT * FROM weather_data WHERE time = ?";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, time);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                double temperature = resultSet.getDouble("temperature");
                double humidity = resultSet.getDouble("humidity");
                double pressure = resultSet.getDouble("pressure");
                retrievedData.setMeasurements(temperature, humidity, pressure);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return retrievedData;
    }
}

class WeatherDataMemento {
    private double temperatureCelsius;
    private double humidity;
    private double pressure;
    private WeatherConditionState conditionState;

    public WeatherDataMemento(double temperatureCelsius, double humidity, double pressure, WeatherConditionState conditionState) {
        this.temperatureCelsius = temperatureCelsius;
        this.humidity = humidity;
        this.pressure = pressure;
        this.conditionState = conditionState;
    }

    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getPressure() {
        return pressure;
    }

    public WeatherConditionState getConditionState() {
        return conditionState;
    }
}

class DataSourceChange {
    private WeatherDataCollectionStrategy dataCollectionStrategy;
    private WeatherDataMemento weatherDataMemento;

    public DataSourceChange(WeatherDataCollectionStrategy strategy, WeatherDataMemento memento) {
        this.dataCollectionStrategy = strategy;
        this.weatherDataMemento = memento;
    }

    public WeatherDataCollectionStrategy getStrategy() {
        return dataCollectionStrategy;
    }

    public WeatherDataMemento getMemento() {
        return weatherDataMemento;
    }
}


public class WeatherAppWithGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherAppWithGUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Weather Monitoring and Alert System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);

        WeatherData weatherData = WeatherData.getInstance();
        final String[] temperatureScale = {"Celsius"};

        JLabel titleLabel = new JLabel("Weather Monitoring and Alert System");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JComboBox<String> dataSourceComboBox = new JComboBox<>(new String[]{"API", "Sensor"});
        dataSourceComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                temperatureScale[0] = (String) dataSourceComboBox.getSelectedItem();
            }
        });
        dataSourceComboBox.setSelectedItem("API"); // Set a default value

        JButton collectDataButton = new JButton("Collect Weather Data");
        collectDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WeatherDataCollectionStrategy dataCollectionStrategy;

                // Check which data collection strategy is selected
                String selectedStrategy = (String) dataSourceComboBox.getSelectedItem();
                if ("API".equals(selectedStrategy)) {
                    dataCollectionStrategy = new WeatherAPI();
                } else if ("Sensor".equals(selectedStrategy)) {
                    dataCollectionStrategy = new WeatherSensor();
                } else {
                    // Default to the API if an invalid strategy is selected
                    dataCollectionStrategy = new WeatherAPI();
                }

                // Collect data using the selected strategy
                dataCollectionStrategy.collectData(weatherData);

                // Get the current time for saving to the database
                String currentTime = getCurrentTime();

                // Save the data to the database
                weatherData.setWeatherDataInDatabase(currentTime);

                // Check if the temperature is below 0°C and display a warning in a new window
                if (weatherData.getTemperatureCelsius() < 0) {
                    // Show a warning in a new window
                    JOptionPane.showMessageDialog(null, "Temperature is below 0°C. Warning!",
                            "Temperature Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });


        JButton setThresholdButton = new JButton("Set Temperature Threshold");
        setThresholdButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String thresholdInput = JOptionPane.showInputDialog(frame, "Enter a new temperature threshold (" + temperatureScale[0] + "):");
                double threshold = convertTemperature(Double.parseDouble(thresholdInput), temperatureScale[0], "Celsius");
                WeatherObserver temperatureObserver = new WeatherObserver(weatherData, threshold);
            }
        });

        JButton displayDataButton = new JButton("Display Weather Data");
        displayDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WeatherReportDecorator temperatureDecorator = new TemperatureDecorator(weatherData, temperatureScale[0]);
                temperatureDecorator.display();
                double temperature;
                String scaleLabel;

                if (temperatureScale[0].equals("Celsius")) {
                    temperature = weatherData.getTemperatureCelsius();
                    scaleLabel = "°C";
                } else if (temperatureScale[0].equals("Fahrenheit")) {
                    temperature = weatherData.getTemperatureFahrenheit();
                    scaleLabel = "°F";
                } else if (temperatureScale[0].equals("Kelvin")) {
                    temperature = weatherData.getTemperatureKelvin();
                    scaleLabel = "K";
                } else {
                    // Default to Celsius if an invalid scale is selected
                    temperature = weatherData.getTemperatureCelsius();
                    scaleLabel = "°C";
                }

                JOptionPane.showMessageDialog(frame,
                        "Temperature (" + temperatureScale[0] + "): " + temperature + scaleLabel + "\n" +
                                "Humidity: " + weatherData.getHumidity() + "%" + "\n" +
                                "Pressure: " + weatherData.getPressure() + " hPa",
                        "Weather Data",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton displayConditionButton = new JButton("Display Weather Condition");
        displayConditionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WeatherReportDecorator temperatureDecorator = new TemperatureDecorator(weatherData, temperatureScale[0]);
                temperatureDecorator.display();
                double temperature = weatherData.getTemperatureCelsius();
                String weatherCondition = getWeatherCondition(temperature);

                JOptionPane.showMessageDialog(frame,
                        "Weather Condition: " + weatherCondition,
                        "Weather Condition",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JComboBox<String> temperatureScaleComboBox = new JComboBox<>(new String[]{"Celsius", "Fahrenheit", "Kelvin"});
        temperatureScaleComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                temperatureScale[0] = (String) temperatureScaleComboBox.getSelectedItem();
            }
        });
        temperatureScaleComboBox.setSelectedItem("Celsius");

        // Create panels
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 1));
        mainPanel.add(titleLabel);
        mainPanel.add(dataSourceComboBox);
        mainPanel.add(collectDataButton);
        mainPanel.add(setThresholdButton);
        mainPanel.add(temperatureScaleComboBox);
        mainPanel.add(displayDataButton);
        mainPanel.add(displayConditionButton);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private static double convertTemperature(double value, String fromScale, String toScale) {
        if (fromScale.equals(toScale)) {
            return value;
        }
        if (fromScale.equals("Celsius")) {
            if (toScale.equals("Fahrenheit")) {
                return (value * 9 / 5) + 32;
            } else if (toScale.equals("Kelvin")) {
                return value + 273.15;
            }
        } else if (fromScale.equals("Fahrenheit")) {
            if (toScale.equals("Celsius")) {
                return (value - 32) * 5 / 9;
            } else if (toScale.equals("Kelvin")) {
                return (value + 459.67) * 5 / 9;
            }
        } else if (fromScale.equals("Kelvin")) {
            if (toScale.equals("Celsius")) {
                return value - 273.15;
            } else if (toScale.equals("Fahrenheit")) {
                return (value * 9 / 5) - 459.67;
            }
        }
        return value;
    }

    private static String getWeatherCondition(double temperature) {
        if (temperature > 25.0) {
            return "Sunny";
        } else if (temperature > 15.0) {
            return "Cloudy";
        } else if (temperature > 0.0) {
            return "Rainy";
        } else {
            return "Snowy";
        }
    }

    private static String getCurrentTime() {
        // Implement a method to get the current time in your preferred format (e.g., "yyyy-MM-dd HH:mm:ss")
        return "2023-11-07 12:00:00";
    }
}