package uniteProject.persistence;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class PooledDataSource {
    private static BasicDataSource basicDS;
    static {
        try {
            basicDS = new BasicDataSource();
            Properties properties = new Properties();
            Class<PooledDataSource> pooledDataSourceClass = PooledDataSource.class;
            BufferedInputStream bis = new BufferedInputStream(Objects.requireNonNull(pooledDataSourceClass.getClassLoader().getResourceAsStream("config/db.yml")));
            properties.load(bis);
            basicDS.setDriverClassName(properties.getProperty("DRIVER_CLASS")); //loads the jdbc driver
            basicDS.setUrl(properties.getProperty("DB_CONNECTION_URL"));
            basicDS.setUsername(properties.getProperty("DB_USER"));
            basicDS.setPassword(properties.getProperty("DB_PWD"));
            // Parameters for connection pooling
            basicDS.setInitialSize(15);
            basicDS.setMaxTotal(15);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static DataSource getDataSource() {
        return basicDS;
    }
}