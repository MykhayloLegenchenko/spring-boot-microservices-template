package com.example.test.data.h2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class StatementProxyTests {

  @Test
  @SuppressWarnings("SqlResolve")
  void testMultiOptionsAlterTableExecute() throws SQLException {
    var mockStatement = mock(Statement.class);
    var mockConnection = mock(Connection.class);
    var proxy = StatementProxy.create(mockStatement, mockConnection);
    var options = new ArrayList<String>();

    when(mockStatement.execute(any(String.class)))
        .then(
            invocation -> {
              options.add(invocation.getArgument(0));
              return false;
            });

    proxy.execute(
"""
                \t \n ALTER \t \n  TABLE \t  \n  `user`
                    ADD COLUMN name VARCHAR(100) DEFAULT 'O\\'Connor, "User"',
                    ADD COLUMN "status" VARCHAR(20) DEFAULT "active,enabled, \\\\\\"",
                    DROP COLUMN old_col,
                    ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id),
                    ADD UNIQUE KEY `uk_email` (email),
                    ADD PRIMARY KEY (id),
                    DROP CONSTRAINT `old_constraint`,
                    ADD INDEX idx_status (status),
                    ADD INDEX idx_composite (last_name, first_name),
                    DROP INDEX `idx_old`
""");

    assertThat(options)
        .isEqualTo(
            List.of(
                "ALTER TABLE `user` ADD COLUMN name VARCHAR(100) DEFAULT 'O\\'Connor, \"User\"'",
                "ALTER TABLE `user` ADD COLUMN \"status\" VARCHAR(20) DEFAULT \"active,enabled, \\\\\\\"\"",
                "ALTER TABLE `user` DROP COLUMN old_col",
                "ALTER TABLE `user` ADD CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)",
                "ALTER TABLE `user` ADD UNIQUE KEY `uk_email` (email)",
                "ALTER TABLE `user` ADD PRIMARY KEY (id)",
                "ALTER TABLE `user` DROP CONSTRAINT `old_constraint`",
                "ALTER TABLE `user` ADD INDEX idx_status (status)",
                "ALTER TABLE `user` ADD INDEX idx_composite (last_name, first_name)",
                "ALTER TABLE `user` DROP INDEX `idx_old`"));
  }
}
