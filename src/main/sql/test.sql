DELIMITER //

CREATE PROCEDURE GenerateRandomData(IN nombreDeLigne INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE max_users INT;
    DECLARE max_customers INT;
    DECLARE max_leads INT;
    DECLARE max_contracts INT;
    DECLARE max_templates INT;
    DECLARE random_user_id INT;
    DECLARE random_customer_id INT;
    DECLARE random_lead_id INT;
    DECLARE random_contract_id INT;
    DECLARE random_template_id INT;

    -- Compter le nombre d'enregistrements dans les tables de référence
SELECT COUNT(*) INTO max_users FROM users;
SELECT COUNT(*) INTO max_customers FROM customer;
SELECT COUNT(*) INTO max_leads FROM trigger_lead;
SELECT COUNT(*) INTO max_contracts FROM trigger_contract;
SELECT COUNT(*) INTO max_templates FROM email_template;

-- Générer des données pour `trigger_lead`
SET i = 0;
    WHILE i < nombreDeLigne DO
        SET random_customer_id = FLOOR(1 + RAND() * max_customers);
        SET random_user_id = FLOOR(1 + RAND() * max_users);

INSERT INTO trigger_lead (customer_id, user_id, name, phone, status, created_at)
VALUES (
           random_customer_id,
           random_user_id,
           CONCAT('LeadName', i),
           CONCAT('+123456789', FLOOR(RAND() * 100)),
           IF(RAND() > 0.5, 'open', 'closed'),
           NOW() - INTERVAL FLOOR(RAND() * 30) DAY
       );
SET i = i + 1;
END WHILE;

    -- Générer des données pour `trigger_contract`
    SET i = 0;
    WHILE i < nombreDeLigne DO
        SET random_lead_id = FLOOR(1 + RAND() * max_leads);
        SET random_user_id = FLOOR(1 + RAND() * max_users);
        SET random_customer_id = FLOOR(1 + RAND() * max_customers);

INSERT INTO trigger_contract (subject, status, description, start_date, end_date, amount, lead_id, user_id, customer_id, created_at)
VALUES (
           CONCAT('ContractSubject', i),
           IF(RAND() > 0.5, 'active', 'inactive'),
           CONCAT('Description for contract ', i),
           NOW() - INTERVAL FLOOR(RAND() * 365) DAY,
           NOW() + INTERVAL FLOOR(RAND() * 365) DAY,
           ROUND(1000 + RAND() * 9000, 2),
           random_lead_id,
           random_user_id,
           random_customer_id,
           NOW() - INTERVAL FLOOR(RAND() * 30) DAY
       );
SET i = i + 1;
END WHILE;

    -- Générer des données pour `trigger_ticket`
    SET i = 0;
    WHILE i < nombreDeLigne DO
        SET random_customer_id = FLOOR(1 + RAND() * max_customers);
        SET random_user_id = FLOOR(1 + RAND() * max_users);

INSERT INTO trigger_ticket (subject, description, status, priority, customer_id, manager_id, employee_id, created_at)
VALUES (
           CONCAT('TicketSubject', i),
           CONCAT('Description for ticket ', i),
           IF(RAND() > 0.5, 'open', 'closed'),
           IF(RAND() > 0.5, 'high', 'low'),
           random_customer_id,
           random_user_id,
           random_user_id,
           NOW() - INTERVAL FLOOR(RAND() * 30) DAY
       );
SET i = i + 1;
END WHILE;

    -- Générer des données pour `contract_settings`
    SET i = 0;
    WHILE i < nombreDeLigne DO
        SET random_user_id = FLOOR(1 + RAND() * max_users);
        SET random_template_id = FLOOR(1 + RAND() * max_templates);
        SET random_customer_id = FLOOR(1 + RAND() * max_customers);

INSERT INTO contract_settings (amount, subject, description, end_date, start_date, status, user_id, status_email_template, amount_email_template, subject_email_template, description_email_template, start_email_template, end_email_template, customer_id)
VALUES (
           IF(RAND() > 0.5, 1, 0),
           IF(RAND() > 0.5, 1, 0),
           IF(RAND() > 0.5, 1, 0),
           IF(RAND() > 0.5, 1, 0),
           IF(RAND() > 0.5, 1, 0),
           IF(RAND() > 0.5, 1, 0),
           random_user_id,
           random_template_id,
           random_template_id,
           random_template_id,
           random_template_id,
           random_template_id,
           random_template_id,
           random_customer_id
       );
SET i = i + 1;
END WHILE;

    -- Générer des données pour `lead_settings`
    SET i = 0;
    WHILE i < nombreDeLigne DO
        SET random_user_id = FLOOR(1 + RAND() * max_users);
        SET random_template_id = FLOOR(1 + RAND() * max_templates);
        SET random_customer_id = FLOOR(1 + RAND() * max_customers);

INSERT INTO lead_settings (status, meeting, phone, name, user_id, status_email_template, phone_email_template, meeting_email_template, name_email_template, customer_id)
VALUES (
           IF(RAND() > 0.5, 1, 0),
           IF(RAND() > 0.5, 1, 0),
           IF(RAND() > 0.5, 1, 0),
           IF(RAND() > 0.5, 1, 0),
           random_user_id,
           random_template_id,
           random_template_id,
           random_template_id,
           random_template_id,
           random_customer_id
       );
SET i = i + 1;
END WHILE;

    -- Générer des données pour `ticket_settings`
    SET i = 0;
    WHILE i < nombreDeLigne DO
        SET random_user_id = FLOOR(1 + RAND() * max_users);
        SET random_template_id = FLOOR(1 + RAND() * max_templates);
        SET random_customer_id = FLOOR(1 + RAND() * max_customers);

INSERT INTO ticket_settings (priority, subject, description, status, user_id, status_email_template, subject_email_template, priority_email_template, description_email_template, customer_id)
VALUES (
           IF(RAND() > 0.5, 1, 0),
           IF(RAND() > 0.5, 1, 0),
           IF(RAND() > 0.5, 1, 0),
           IF(RAND() > 0.5, 1, 0),
           random_user_id,
           random_template_id,
           random_template_id,
           random_template_id,
           random_template_id,
           random_customer_id
       );
SET i = i + 1;
END WHILE;

    -- Générer des données pour `file`
    SET i = 0;
    WHILE i < nombreDeLigne DO
        SET random_lead_id = FLOOR(1 + RAND() * max_leads);
        SET random_contract_id = FLOOR(1 + RAND() * max_contracts);

INSERT INTO file (file_name, file_type, lead_id, contract_id)
VALUES (
           CONCAT('file', i, '.txt'),
           IF(RAND() > 0.5, 'text/plain', 'application/pdf'),
           IF(RAND() > 0.5, random_lead_id, NULL),
           IF(RAND() > 0.5, random_contract_id, NULL)
       );
SET i = i + 1;
END WHILE;

    -- Générer des données pour `google_drive_file`
    SET i = 0;
    WHILE i < nombreDeLigne DO
        SET random_lead_id = FLOOR(1 + RAND() * max_leads);
        SET random_contract_id = FLOOR(1 + RAND() * max_contracts);

INSERT INTO google_drive_file (drive_file_id, drive_folder_id, lead_id, contract_id)
VALUES (
           UUID(),
           UUID(),
           IF(RAND() > 0.5, random_lead_id, NULL),
           IF(RAND() > 0.5, random_contract_id, NULL)
       );
SET i = i + 1;
END WHILE;

    -- Générer des données pour `lead_action`
    SET i = 0;
    WHILE i < nombreDeLigne DO
        SET random_lead_id = FLOOR(1 + RAND() * max_leads);

INSERT INTO lead_action (lead_id, action, date_time)
VALUES (
           random_lead_id,
           CONCAT('Action', i),
           NOW() - INTERVAL FLOOR(RAND() * 30) DAY
       );
SET i = i + 1;
END WHILE;
END //

DELIMITER ;


CREATE TABLE budget(
                       budget_id INT NOT NULL AUTO_INCREMENT,
                       valeur DECIMAL(10,2) NOT NULL,
                       date_budget datetime NOT NULL,
                       customer_id INT UNSIGNED NOT NULL,
                       PRIMARY KEY(budget_id),
                       FOREIGN KEY(customer_id) REFERENCES customer(customer_id)
);


CREATE TABLE notification(
                             notification_id INT NOT NULL AUTO_INCREMENT,
                             message VARCHAR(250) NOT NULL,
                             date_notification DATETIME NOT NULL,
                             etat INT DEFAULT NULL,
                             customer_id INT UNSIGNED NOT NULL,
                             PRIMARY KEY(notification_id),
                             FOREIGN KEY(customer_id) REFERENCES customer(customer_id)
);

CREATE TABLE depense(
                        depense_id INT NOT NULL AUTO_INCREMENT,
                        valeur_depense DECIMAL(10,2) NOT NULL,
                        date_depense DATETIME NOT NULL,
                        etat INT NOT NULL,
                        lead_id INT UNSIGNED DEFAULT NULL,
                        ticket_id INT UNSIGNED DEFAULT NULL,
                        PRIMARY KEY(depense_id),
                        FOREIGN KEY(lead_id) REFERENCES trigger_lead(lead_id),
                        FOREIGN KEY(ticket_id) REFERENCES trigger_ticket(ticket_id)
);

CREATE TABLE seuil(
                      seuil_id INT NOT NULL AUTO_INCREMENT,
                      taux DECIMAL(10,2) NOT NULL,
                      date_seuil DATETIME NOT NULL,
                      PRIMARY KEY(seuil_id)
);