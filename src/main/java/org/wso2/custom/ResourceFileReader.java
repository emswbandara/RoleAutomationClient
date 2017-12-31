package org.wso2.custom;

import org.apache.log4j.Logger;
import org.wso2.custom.beans.Automation;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ResourceFileReader {

    private static ResourceFileReader fileReader = new ResourceFileReader();
    private Properties properties;
    private Logger log = Logger.getLogger(ResourceFileReader.class);

    public static ResourceFileReader getInstance() {

        return fileReader;
    }

    private ResourceFileReader() {

        try (InputStream in = new FileInputStream(new File("conf/automation.properties"))) {
            properties = new Properties();
            properties.load(in);
        } catch (IOException ex) {
            log.error("Error occurred while reading property file, " + ex.getMessage(), ex);
        }
    }

    public Automation readXML(String path) throws JAXBException {

        log.info("Reading automation configuration file...");
        JAXBContext jc = JAXBContext.newInstance(Automation.class);

        File xml = new File(path);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        Automation automation = (Automation) unmarshaller.unmarshal(xml);

        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        log.info("Read successful.");
        return automation;
    }

    public Properties getProperties() {

        return properties;
    }

}
