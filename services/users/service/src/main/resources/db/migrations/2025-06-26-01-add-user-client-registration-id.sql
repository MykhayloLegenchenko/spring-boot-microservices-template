-- liquibase formatted sql
-- changeset legenchenko:add-user-client-registration-id
ALTER TABLE `user`
    ADD COLUMN `client_registration_id` varchar(10) AFTER `email`;
ALTER TABLE `user`
    ADD COLUMN `subject_id` varchar(50) AFTER `client_registration_id`;
ALTER TABLE `user`
    ADD UNIQUE KEY `user_subject_uk` (`subject_id`, `client_registration_id`);

-- rollback ALTER TABLE `user` DROP KEY `user_subject_uk`, DROP COLUMN `subject_id`, DROP COLUMN `client_registration_id`;
