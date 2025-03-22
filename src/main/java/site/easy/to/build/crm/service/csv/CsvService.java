package site.easy.to.build.crm.service.csv;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.util.CsvUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CsvService {
    private JdbcTemplate jdbcTemplate;
    private ApplicationContext applicationContext;
    private EntityManager entityManager;

    public CsvService(JdbcTemplate jdbcTemplate,ApplicationContext applicationContext,EntityManager entityManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.applicationContext = applicationContext;
        this.entityManager = entityManager;
    }
    public static String getTableName(Class<?> entityClass) {
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        if (tableAnnotation != null && !tableAnnotation.name().isEmpty()) {
            return tableAnnotation.name();
        }
        return entityClass.getSimpleName().toLowerCase(Locale.ROOT);
    }
    public static String getColumnName(Field field){
        if (field.isAnnotationPresent(Column.class)) {
            Column columnAnnotation=field.getAnnotation(Column.class);
            return columnAnnotation.name();
        }
        else if (field.isAnnotationPresent(JoinColumn.class)) {
            JoinColumn columnAnnotation=field.getAnnotation(JoinColumn.class);
            return columnAnnotation.name();
        }
        return field.getName().toLowerCase();
    }
    public List<String> getEntityColumns(Class<?> entityClass) {
        List<String> columns = new ArrayList<>();
        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column columnAnnotation = field.getAnnotation(Column.class);
                columns.add(columnAnnotation.name());
            } else if (field.isAnnotationPresent(JoinColumn.class)) {
                JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
                columns.add(joinColumnAnnotation.name());
            }
        }

        return columns;
    }
    public static String getTempTableName(String tableName){
        return tableName+"_temp";
    }
    public String getCreateTableScript(String tableName) {
        String sql = String.format("SHOW CREATE TABLE %s", tableName);
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> rs.getString(2));
    }
    public void createTempTable(String originalTableName) {
        String tempTableName=getTempTableName(originalTableName);
        String sql=getCreateTableScript(originalTableName);
        String tempTableScript = sql
                .replace("CREATE TABLE", "CREATE TABLE")
                .replace(originalTableName, tempTableName);
        jdbcTemplate.execute(tempTableScript);
    }
    public void deleteTempTable(String tempTableName) {
        String sql = "DROP TABLE IF EXISTS " + tempTableName;
        jdbcTemplate.execute(sql);
    }
    private Object getIdFromEntity(Object entity) throws Exception {
        if (entity == null) return null;

        List<Method> getters = new ArrayList<>();

        // Récupère toutes les méthodes déclarées de la classe de l'entité
        for (Method method : entity.getClass().getDeclaredMethods()) {
            // Ajoute les méthodes getters (méthodes qui commencent par 'get')
            if (method.getName().startsWith("get") && method.getParameterCount() == 0) {
                method.setAccessible(true); // Pour accéder aux méthodes privées
                getters.add(method);
            }
        }

        // Cherche la méthode getter correspondant à l'ID
        for (Method method : getters) {
            // Vérifie si le nom de la méthode se termine par "Id"
            if (method.getName().endsWith("Id")) {
                return method.invoke(entity); // Appelle la méthode et retourne la valeur de l'ID
            }
        }

        return null; // Si aucune méthode n'a été trouvée, retourne null
    }

    public void insertDataTempTable(String tempTableName, List<?> listes) throws Exception {
        if (listes.isEmpty()) return;

        try {

            // Récupérer les colonnes de l'entité
            List<String> columns = getEntityColumns(listes.get(0).getClass());

            for (Object obj : listes) {
                // Filtrer les colonnes avec des valeurs non nulles
                List<String> nonNullColumns = new ArrayList<>();
                List<Object> nonNullValues = new ArrayList<>();

                Field[] fields = obj.getClass().getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    Object value = null;
                    try {
                        value = field.get(obj);

                        // Si la valeur n'est pas nulle, ajouter la colonne et sa valeur
                        if (value != null) {
                            nonNullColumns.add(getColumnName(field));
                            nonNullValues.add(value);
                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Error accessing field: " + field.getName(), e);
                    }
                }

                // Construire dynamiquement la requête avec les colonnes et valeurs non nulles
                String columnNames = String.join(", ", nonNullColumns);
                String placeholders = nonNullColumns.stream().map(col -> "?").collect(Collectors.joining(", "));
                String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tempTableName, columnNames, placeholders);

                // Exécuter la requête
                jdbcTemplate.update(sql, ps -> {
                    int index = 1;
                    for (Object value : nonNullValues) {
                        // Si l'objet est non-primitive, obtenir l'ID ou la valeur à insérer
                        if (value != null && !value.getClass().isPrimitive()) {
                            try {
                                value = getIdFromEntity(value);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        ps.setObject(index++, value);
                    }
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during batch insert", e);
        }
    }





    public void insertIntoRealTable(String tableName,String tempTableName) throws Exception{
        try{
            String insertIntoRealTable = "INSERT INTO " + tableName + " SELECT * FROM " + tempTableName;
            jdbcTemplate.update(insertIntoRealTable);
        }
        catch (Exception e){
            throw e;
        }
    }

    @Transactional
    public void insertData(MultipartFile file, Class<?> clazz, char separator) throws Exception {
        String tableName=getTableName(clazz);
        String tempTableName=getTempTableName(tableName);
        try {
            CSVParser csvParser = CsvUtil.readCsv(file, separator);
            List<?>listes=CsvUtil.parseCSV(csvParser,clazz,applicationContext,entityManager);
            for (int i = 0; i < listes.size(); i++) {
                System.out.println(listes.get(i));
            }
            deleteTempTable(tempTableName);
            createTempTable(tableName);
            insertDataTempTable(tempTableName,listes);
            insertIntoRealTable(tableName,tempTableName);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'insertion des données depuis le fichier CSV", e);
        }
        finally {
            deleteTempTable(tempTableName);
        }
    }

}
