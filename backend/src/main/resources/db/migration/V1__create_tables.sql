CREATE TABLE `users`
(
    `id`         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `email`      VARCHAR(255) NOT NULL,
    `password`   VARCHAR(255) NOT NULL,
    `nickname`   VARCHAR(50)  NOT NULL,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at` DATETIME              NULL,
    UNIQUE KEY `uq_users_email` (`email`)
) ENGINE = InnoDB
  CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `categories`
(
    `id`          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `code`        VARCHAR(20) NOT NULL,
    `code_number` VARCHAR(3)  NOT NULL,
    `name`        VARCHAR(30) NOT NULL,
    `type`        TINYINT(1)  NOT NULL,
    UNIQUE KEY `uq_categories_code_type` (`code`, `type`),
    UNIQUE KEY `uq_categories_code_number` (`code_number`)
) ENGINE = InnoDB
  CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `transactions`
(
    `id`               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `user_id`          BIGINT UNSIGNED NOT NULL,
    `category_id`      BIGINT UNSIGNED NOT NULL,
    `type`             TINYINT(1)      NOT NULL,
    `amount`           DECIMAL(12, 0)  NOT NULL CHECK (`amount` > 0),
    `memo`             VARCHAR(255)             NULL,
    `transaction_date` DATE            NOT NULL,
    `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at`       DATETIME                 NULL,
    CONSTRAINT `fk_transactions_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_transactions_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
    INDEX `idx_transactions_user_date` (`user_id`, `transaction_date`),
    INDEX `idx_transactions_user_category` (`user_id`, `category_id`)
) ENGINE = InnoDB
  CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `recurring_items`
(
    `id`          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `user_id`     BIGINT UNSIGNED  NOT NULL,
    `category_id` BIGINT UNSIGNED  NOT NULL,
    `type`        TINYINT(1)       NOT NULL,
    `name`        VARCHAR(100)     NOT NULL,
    `amount`      DECIMAL(12, 0)   NOT NULL CHECK (`amount` > 0),
    `billing_day` TINYINT UNSIGNED NOT NULL CHECK (`billing_day` BETWEEN 1 AND 31),
    `is_active`   TINYINT(1)       NOT NULL DEFAULT 1,
    `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted_at`  DATETIME                  NULL,
    CONSTRAINT `fk_recurring_items_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `fk_recurring_items_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE = InnoDB
  CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `budgets`
(
    `id`         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `user_id`    BIGINT UNSIGNED NOT NULL,
    `year_month` CHAR(7)         NOT NULL,
    `amount`     DECIMAL(12, 0)  NOT NULL CHECK (`amount` > 0),
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_budgets_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    UNIQUE KEY `uq_budgets_user_year_month` (`user_id`, `year_month`)
) ENGINE = InnoDB
  CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
