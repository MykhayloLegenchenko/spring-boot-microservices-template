INSERT INTO `role` (`name`)
VALUES ('TEST_ROLE_1'),
       ('TEST_ROLE_2'),
       ('TEST_ROLE_3');

INSERT INTO `user` (`uuid`, `email`, `enabled`, `first_name`, `last_name`, `password`, created_at, deleted_at)
VALUES (X'100151ACB1DE4D77A3FA26AD61A1C6BE', 'john@example.com', 1, 'John', 'Doe',
        '$2a$10$PbK1rW7xgNAIsLfc/t.4b.Wg0dqfCnFDfRRG3prxac6rzfIosewji', '2023-11-21 13:55:34.388428',
        '1000-01-01 00:00:00.000000');
