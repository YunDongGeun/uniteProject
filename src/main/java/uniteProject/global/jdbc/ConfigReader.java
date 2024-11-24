package uniteProject.global.jdbc;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private final Properties properties;

    public ConfigReader() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config/db.yml")) {
            if (input == null) {
                System.out.println("config.properties not found");
                return;
            }
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

//    public static void main(String[] args) {
//        ConfigReader config = new ConfigReader();
//        System.out.println("JDBC URL: " + config.getProperty("DB_CONNECTION_URL"));
//        System.out.println("JDBC Username: " + config.getProperty("DB_USER"));
//        System.out.println("JDBC Password: " + config.getProperty("DB_PWD"));
//    }
}
