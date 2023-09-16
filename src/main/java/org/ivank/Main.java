package org.ivank;

import org.ivank.db.DbManager;
import org.ivank.gen.ReportGenerator;

import java.text.DecimalFormat;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        Properties properties = Properties.getInstance(args[0]);
        DbManager.connect(properties);
        if (properties.getFormat().contains("xml")) {
            Logger.info("Writing to xml has started.");
            Date start = new Date();
            ReportGenerator.writeToXML(properties.getCatalog(), DbManager.loadDbData(), properties.getOutput());
            Double time = (new Date().getTime() - start.getTime()) * 0.001;
            Logger.info(String.format("Writing to xml has finished. Took %s seconds.", format(time)));
        }
        DbManager.close();
    }

    private static String format(Double value) {
        DecimalFormat format = new DecimalFormat("0.#");
        return format.format(value);
    }
}