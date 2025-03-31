package site.easy.to.build.crm.util;

import jakarta.persistence.Entity;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.service.customer.CustomerService;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CopieUtil {
    CustomerService customerService;

    public CopieUtil(CustomerService customerService) {
        this.customerService = customerService;
    }

    public static String getCopieName(String original){
        return "copie_"+original;
    }

    public static String escapeCsvField(Object field) {
        if (field == null) {
            return "";
        }
        String str = field.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }
    public static void writeToFile(String content, String filename) throws IOException {
        try (FileWriter fw = new FileWriter(filename)) {
            fw.write(content);
        }
    }
    public static LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {

            }
        }

        System.err.println("Erreur parse: " + dateStr);
        return null;
    }
}
