-- liquibase formatted sql
-- changeset misha:initial
CREATE TABLE `user`
(
    `id`         bigint       NOT NULL AUTO_INCREMENT,
    `uuid`       binary(16)   NOT NULL,
    `email`      varchar(100) NOT NULL,
    `enabled`    tinyint(1)   NOT NULL,
    `first_name` varchar(50)  NOT NULL,
    `last_name`  varchar(50)  NOT NULL,
    `password`   varchar(60)  NOT NULL,
    `created_at` datetime(6)  NOT NULL,
    `deleted_at` datetime(6)  NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `user_uuid_uk` (`uuid`),
    UNIQUE KEY `user_email_uk` (`email`, `deleted_at`)
);

INSERT INTO `user`
VALUES (1, X'1001A9B7B4BA46D69CF41FEDC06439EC', 'admin@example.com', 1, 'System', 'Admin',
        '$2a$10$av3LcZg.Js0qL1p.ebofm.qhlKf9cNQM4WTs4lYNz68K69VzR5Gta', '2023-11-17 10:48:44.262405',
        '1000-01-01 00:00:00.000000');

CREATE TABLE `role`
(
    `id`   bigint                          NOT NULL AUTO_INCREMENT,
    `name` varchar(50) CHARACTER SET ascii NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `role_name_uk` (`name`)
);

INSERT INTO `role`
VALUES (1, 'ADMIN'),
       (2, 'SUPER');

CREATE TABLE `user_role`
(
    `user_id` bigint NOT NULL,
    `role_id` bigint NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    CONSTRAINT `user_role_user_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `user_role_role_fk` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
);

INSERT INTO `user_role`
VALUES (1, 1),
       (1, 2);

-- rollback DROP TABLE `user_role`;
-- rollback DROP TABLE `role`;
-- rollback DROP TABLE `user`;
