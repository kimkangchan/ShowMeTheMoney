ALTER TABLE `users`
    ADD COLUMN `username` VARCHAR(50) NOT NULL DEFAULT '' AFTER `id`,
    ADD UNIQUE KEY `uq_users_username` (`username`);

UPDATE `users` SET `username` = `email` WHERE `username` = '';

ALTER TABLE `users` ALTER COLUMN `username` DROP DEFAULT;
