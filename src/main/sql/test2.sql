DELIMITER //

CREATE PROCEDURE GenerateRandomData(
    IN nombreDeLigneCustomer_info_login_et_Customer INT,
    IN nombreLigne_trigger_lead INT,
    IN nombreLigne_trigger_ticket INT,
    IN nombreDeLigne_depense INT,
    IN nombreDeLigne_budget INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE max_users INT;
    DECLARE random_user_id INT;
    DECLARE random_email VARCHAR(255);
    DECLARE hashed_password VARCHAR(255);
    DECLARE generated_token VARCHAR(255);
    DECLARE random_login_id INT;
    DECLARE random_customer_id INT;
    DECLARE random_lead_id INT;
    DECLARE random_ticket_id INT;

    -- Désactiver temporairement les contraintes de clé étrangère
    SET FOREIGN_KEY_CHECKS = 0;

    -- Récupération du nombre d'utilisateurs disponibles
SELECT COUNT(*) INTO max_users FROM users;

-- Insertion des clients et leurs informations de connexion
WHILE i < nombreDeLigneCustomer_info_login_et_Customer DO
        -- Sélection aléatoire d'un utilisateur existant ou NULL
        IF max_users > 0 THEN
SELECT id INTO random_user_id FROM users ORDER BY RAND() LIMIT 1;
ELSE
            SET random_user_id = NULL;
END IF;

        SET random_email = CONCAT('user', i, '@example.com');
        SET hashed_password = SHA2(CONCAT('1234', i), 256); -- SHA-256
        SET generated_token = UUID();

        -- Insertion dans customer_login_info
INSERT INTO customer_login_info (password, username, token, password_set)
VALUES (hashed_password, random_email, generated_token, 1);
SET random_login_id = LAST_INSERT_ID();

        -- Insertion dans customer
INSERT INTO customer (name, phone, address, country, email, description, profile_id, user_id)
VALUES (
           CONCAT('Client ', i),
           CONCAT('+12345678', LPAD(i, 2, '0')),
           CONCAT('Address ', i),
           'USA',
           random_email,
           'Generated customer',
           random_login_id,
           random_user_id
       );
SET random_customer_id = LAST_INSERT_ID();

        SET i = i + 1;
END WHILE;

    -- Insertion des leads
    SET i = 0;
    WHILE i < nombreLigne_trigger_lead DO
        -- Vérifier qu'il y a des clients disponibles
        IF EXISTS (SELECT 1 FROM customer) THEN
            INSERT INTO trigger_lead (customer_id, employee_id, user_id, name, phone, status, created_at)
            VALUES (
                (SELECT customer_id FROM customer ORDER BY RAND() LIMIT 1),
                (SELECT id FROM users ORDER BY RAND() LIMIT 1),
                (SELECT id FROM users ORDER BY RAND() LIMIT 1),
                CONCAT('LeadName', i),
                CONCAT('+123456789', FLOOR(RAND() * 100)),
                CASE FLOOR(RAND() * 4)
                    WHEN 0 THEN 'meeting-to-schedule'
                    WHEN 1 THEN 'assign-to-sales'
                    WHEN 2 THEN 'archived'
                    ELSE 'success'
                END,
                NOW() - INTERVAL FLOOR(RAND() * 30) DAY
            );
            SET i = i + 1;
ELSE
            -- Si aucun client n'est disponible, sortir de la boucle
            SET i = nombreLigne_trigger_lead;
END IF;
END WHILE;

    -- Insertion des tickets
    SET i = 0;
    WHILE i < nombreLigne_trigger_ticket DO
        -- Vérifier qu'il y a des clients disponibles
        IF EXISTS (SELECT 1 FROM customer) THEN
            INSERT INTO trigger_ticket (customer_id, manager_id, employee_id, subject, description, status, priority, created_at)
            VALUES (
                (SELECT customer_id FROM customer ORDER BY RAND() LIMIT 1),
                (SELECT id FROM users ORDER BY RAND() LIMIT 1),
                (SELECT id FROM users ORDER BY RAND() LIMIT 1),
                CONCAT('TicketSubject', i),
                CONCAT('Description ', i),
                CASE FLOOR(RAND() * 9)
                    WHEN 0 THEN 'open'
                    WHEN 1 THEN 'assigned'
                    WHEN 2 THEN 'on-hold'
                    WHEN 3 THEN 'in-progress'
                    WHEN 4 THEN 'resolved'
                    WHEN 5 THEN 'closed'
                    WHEN 6 THEN 'reopened'
                    WHEN 7 THEN 'pending-customer-response'
                    ELSE 'escalated'
                END,
                CASE FLOOR(RAND() * 6)
                    WHEN 0 THEN 'low'
                    WHEN 1 THEN 'medium'
                    WHEN 2 THEN 'high'
                    WHEN 3 THEN 'closed'
                    WHEN 4 THEN 'urgent'
                    ELSE 'critical'
                END,
                NOW() - INTERVAL FLOOR(RAND() * 30) DAY
            );
            SET i = i + 1;
ELSE
            -- Si aucun client n'est disponible, sortir de la boucle
            SET i = nombreLigne_trigger_ticket;
END IF;
END WHILE;

    -- Insertion des budgets
    SET i = 0;
    WHILE i < nombreDeLigne_budget DO
        -- Vérifier qu'il y a des clients disponibles
        IF EXISTS (SELECT 1 FROM customer) THEN
            INSERT INTO budget (customer_id, valeur, date_budget)
            VALUES (
                (SELECT customer_id FROM customer ORDER BY RAND() LIMIT 1),
                ROUND(RAND() * 1000, 2),
                NOW() - INTERVAL FLOOR(RAND() * 30) DAY
            );
            SET i = i + 1;
ELSE
            -- Si aucun client n'est disponible, sortir de la boucle
            SET i = nombreDeLigne_budget;
END IF;
END WHILE;

    -- Insertion des dépenses
    -- Insertion des dépenses
SET i = 0;
WHILE i < nombreDeLigne_depense DO
    -- Vérifier s'il y a des leads OU des tickets disponibles
    IF EXISTS (SELECT 1 FROM trigger_lead) OR EXISTS (SELECT 1 FROM trigger_ticket) THEN
        -- Décider si on utilise un lead ou un ticket (priorité aléatoire)
        SET @use_lead = CASE
            WHEN NOT EXISTS (SELECT 1 FROM trigger_lead) THEN 0
            WHEN NOT EXISTS (SELECT 1 FROM trigger_ticket) THEN 1
            ELSE RAND() < 0.5 END;

        IF @use_lead = 1 THEN
            -- Utiliser un lead existant
            INSERT INTO depense (valeur_depense, date_depense, etat, lead_id, ticket_id)
            VALUES (
                ROUND(RAND() * 500, 2),
                NOW() - INTERVAL FLOOR(RAND() * 30) DAY,
                FLOOR(RAND() * 3),
                (SELECT lead_id FROM trigger_lead ORDER BY RAND() LIMIT 1),
                NULL
            );
ELSE
            -- Utiliser un ticket existant
            INSERT INTO depense (valeur_depense, date_depense, etat, lead_id, ticket_id)
            VALUES (
                ROUND(RAND() * 500, 2),
                NOW() - INTERVAL FLOOR(RAND() * 30) DAY,
                FLOOR(RAND() * 3),
                NULL,
                (SELECT ticket_id FROM trigger_ticket ORDER BY RAND() LIMIT 1)
            );
END IF;
        SET i = i + 1;
ELSE
        -- Si ni leads ni tickets ne sont disponibles, sortir de la boucle
        SET i = nombreDeLigne_depense;
END IF;
END WHILE;

    -- Réactiver les contraintes de clé étrangère
    SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Données générées avec succès' AS result;
END //

DELIMITER ;