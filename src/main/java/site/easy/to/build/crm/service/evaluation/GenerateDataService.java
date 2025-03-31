package site.easy.to.build.crm.service.evaluation;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

@Service
public class GenerateDataService {

    private static final Logger logger = LoggerFactory.getLogger(GenerateDataService.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenerateDataService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Génère des données aléatoires dans la base de données CRM
     *
     * @param nombreLigneCustomerInfoLoginEtCustomer Nombre de clients à créer
     * @param nombreLigneTriggerLead Nombre de leads à créer
     * @param nombreLigneTriggerTicket Nombre de tickets à créer
     * @param nombreLigneDepense Nombre de dépenses à créer
     * @param nombreLigneBudget Nombre de budgets à créer
     * @return boolean - true si l'opération a réussi, false sinon
     */
    public boolean generateRandomData(int nombreLigneCustomerInfoLoginEtCustomer,
                                      int nombreLigneTriggerLead,
                                      int nombreLigneTriggerTicket,
                                      int nombreLigneDepense,
                                      int nombreLigneBudget) {
        try {
            logger.info("Début de la génération de données aléatoires...");
            logger.debug("Paramètres: Clients={}, Leads={}, Tickets={}, Dépenses={}, Budgets={}",
                    nombreLigneCustomerInfoLoginEtCustomer, nombreLigneTriggerLead,
                    nombreLigneTriggerTicket, nombreLigneDepense, nombreLigneBudget);

            // Validation des paramètres
            if (nombreLigneCustomerInfoLoginEtCustomer < 0 || nombreLigneTriggerLead < 0 ||
                    nombreLigneTriggerTicket < 0 || nombreLigneDepense < 0 || nombreLigneBudget < 0) {
                throw new IllegalArgumentException("Les paramètres ne peuvent pas être négatifs");
            }

            String sql = "CALL GenerateRandomData(?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, nombreLigneCustomerInfoLoginEtCustomer, nombreLigneTriggerLead,
                    nombreLigneTriggerTicket, nombreLigneDepense, nombreLigneBudget);

            logger.info("Génération de données terminée avec succès");
            return true;

        } catch (DataAccessException e) {
            logger.error("Erreur d'accès à la base de données lors de la génération de données", e);
            return false;
        } catch (IllegalArgumentException e) {
            logger.error("Paramètres invalides pour la génération de données", e);
            return false;
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de la génération de données", e);
            return false;
        }
    }

    /**
     * Version alternative avec valeurs par défaut
     */

}