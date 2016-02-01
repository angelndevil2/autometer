package kr.blogspot.andmemories.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author k, Created on 16. 1. 30.
 */
@Slf4j
public class PropertiesUtil {

    public static final String LogbackConfig = "logback.xml";
    public static final String AppProperties = "autometer.properties";
    public static final String JmeterProperties = "jmeter.properties";
    @Setter @Getter
    private static String baseDir;
    @Setter @Getter
    private static String confDir;
    @Setter @Getter
    private static String binDir;
    @Getter
    private static final Properties properties = new Properties();

    private static void loadProperties() throws IOException {
        properties.load(new FileInputStream(confDir + File.separator + AppProperties));
    }

    private static void loadLogbackConfiguration() {
        System.setProperty("logback.configurationFile", confDir+File.separator+LogbackConfig);
    }

    public static String getJMeterPropertiesFile() { return confDir+File.separator+JmeterProperties; }

    /**
     * <ol>
     *  <li>set directory structure for autometer</li>
     *  <li>load autometer propertes</li>
     *  <li>if logback.use = true, load logback configuration, default file is conf/logback.xml</li>
     * </ol>
     *
     */
    private static void setDirs() throws IOException {
        if (baseDir == null) baseDir = ".";
        confDir = baseDir+File.separator+File.separator+"conf";
        binDir = baseDir+File.separator+File.separator+"bin";

        loadProperties();
        if (Boolean.valueOf(properties.getProperty("logback.use"))) loadLogbackConfiguration();

    }

    public static void setDirs(@NonNull String bd) throws IOException {
        baseDir = bd;
        setDirs();
    }
}
