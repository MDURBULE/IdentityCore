package com.IdentityCore;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

import org.junit.jupiter.api.Test;

public class DatabaseMigrationRunner {

    @Test
    void applySchemaAndProcedures() throws Exception {
        Properties dbProps = new Properties();
        try (InputStream in = java.nio.file.Files.newInputStream(java.nio.file.Paths.get("config/DBConfig.cfg"))) {
            dbProps.load(in);
        }

        String url = dbProps.getProperty("DB_URL");
        String user = dbProps.getProperty("DB_USERNAME");
        String pass = dbProps.getProperty("DB_PASSWORD");

        System.out.println("Connecting to database: " + url.replaceAll(":.*@", ":***@"));
        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected to Neon DB successfully!");

            String tablesSql = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/main/resources/db/tables.sql")), StandardCharsets.UTF_8);
            try (Statement st = con.createStatement()) {
                st.execute(tablesSql);
                System.out.println("tables.sql executed successfully!");
            }

            String proceduresSql = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/main/resources/db/procedures.sql")), StandardCharsets.UTF_8);
            try (Statement st = con.createStatement()) {
                st.execute(proceduresSql);
                System.out.println("procedures.sql executed successfully!");
            }
        }
    }
}
