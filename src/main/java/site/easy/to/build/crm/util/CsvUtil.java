package site.easy.to.build.crm.util;

import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.JoinColumn;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.context.ApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class CsvUtil {

    private static String DATE_PATTERN = "yyyy-MM-dd"; // Format de date par défaut

    public static void setDatePattern(String pattern) {
        DATE_PATTERN = pattern;
    }

    public static CSVParser readCsv(MultipartFile file, char separator) throws Exception {
        Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
        CSVFormat format = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withDelimiter(separator);
        return new CSVParser(reader, format);
    }

    public static <T> List<T> parseCSV(CSVParser csvParser, Class<T> clazz, ApplicationContext applicationContext, EntityManager entityManager) throws Exception {
        List<T> resultList = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();

        for (CSVRecord record : csvParser) {
            T obj = clazz.getDeclaredConstructor().newInstance();

            for (Field field : fields) {
                field.setAccessible(true);
                String columnName = getColumnName(field);
                if (columnName!=null){
                    if (record.isMapped(columnName)) {
                        String value = record.get(columnName);
                        if (value != null && !value.isEmpty()) {
                            Object convertedValue = convertValue(value, field.getType());
                            if (convertedValue == null) {
                                convertedValue = entityManager.find(field.getType(),Integer.parseInt(value));
                            }
                            field.set(obj, convertedValue);
                        }
                    }
                }

            }
            resultList.add(obj);
        }
        return resultList;
    }

    private static String getColumnName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            return field.getAnnotation(Column.class).name();
        }
        if (field.isAnnotationPresent(JoinColumn.class)) {
            return field.getAnnotation(JoinColumn.class).name();
        }
        return null;
    }

    private static Object convertValue(String value, Class<?> fieldType) {
        if (value==null) return null;
        try {
            if (fieldType == int.class || fieldType == Integer.class) {
                return Integer.parseInt(value);
            } else if (fieldType == long.class || fieldType == Long.class) {
                return Long.parseLong(value);
            } else if (fieldType == double.class || fieldType == Double.class) {
                return Double.parseDouble(value);
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                return Boolean.parseBoolean(value);
            } else if (fieldType == String.class) {
                return value;
            } else if (fieldType == Date.class) {
                return parseDate(value);
            } else if (fieldType == Timestamp.class) {
                return Timestamp.valueOf(value);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private static Date parseDate(String value) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
            return dateFormat.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Format de date invalide: " + value + " (Attendu: " + DATE_PATTERN + ")");
        }
    }

    private static Object findEntityById(Class<?> fieldType, String id, ApplicationContext applicationContext) {
        String serviceName = fieldType.getSimpleName() + "Service";
        Map<String, ?> serviceBeans = applicationContext.getBeansWithAnnotation(org.springframework.stereotype.Service.class);

        for (Object serviceBean : serviceBeans.values()) {
            if (serviceBean.getClass().getSimpleName().equalsIgnoreCase(serviceName)) {
                try {
                    Method method = findIdMethod(serviceBean.getClass());
                    if (method != null) {
                        return method.invoke(serviceBean, id);
                    }
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    private static Method findIdMethod(Class<?> serviceClass) {
        for (Method method : serviceClass.getDeclaredMethods()) {
            if (method.getName().startsWith("findBy") && method.getName().endsWith("Id")) {
                System.out.println(method.getName());
                return method;
            }
        }
        return null;
    }
}
