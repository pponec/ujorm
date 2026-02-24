package org.ujorm.core.csv;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class CsvManagerTest {

    @Test
    void convertToBeanByOrder() {
        var csvManager = CsvManager.of(UserBean.class);
        var result = csvManager.convertByOrder(source()).toList();

        assertNotNull(result);
        assertEquals(10, result.size());
        assertEquals(10, result.size());

        for (int i = 0; i < 10; i++) {
            var user = result.get(i);
            assertEquals(i, user.getId());
            switch (i) {
                case 0 -> {
                    assertTrue(user.active);
                    assertEquals("James", user.firstName);
                    assertEquals("Smith", user.lastName);
                    assertEquals(50000, user.salary);
                    assertEquals(new BigDecimal("1500.50"), user.balance);
                }
                case 9 -> {
                    assertFalse(user.active);
                    assertEquals("", user.firstName);
                    assertEquals("", user.lastName);
                    assertEquals(0, user.salary);
                    assertNull(user.balance);
                }
            }
        }
    }


    @Test
    void convertToRecordByOrder() {
        var csvManager = CsvManager.of(UserRecord.class);
        var result = csvManager.convertByOrder(source()).toList();

        assertNotNull(result);
        assertEquals(10, result.size());
        assertEquals(10, result.size());

        for (int i = 0; i < 10; i++) {
            var user = result.get(i);
            assertEquals(i, user.id());
            switch (i) {
                case 0 -> {
                    assertTrue(user.active);
                    assertEquals("James", user.firstName);
                    assertEquals("Smith", user.lastName);
                    assertEquals(50000, user.salary);
                    assertEquals(new BigDecimal("1500.50"), user.balance);
                }
                case 9 -> {
                    assertFalse(user.active);
                    assertEquals("", user.firstName);
                    assertEquals("", user.lastName);
                    assertEquals(0, user.salary);
                    assertNull(user.balance);
                }
            }
        }
    }

    private Stream<String> source() {
        return """
                0;true;25;1;50000;4.5;1000.0;M;James;Smith;1500.50;2026-01-01T10:00:00
                1;false;30;2;60000;3.8;500.0;F;Mary;Johnson;2500.75;2026-01-02T11:15:00
                2;true;22;3;45000;4.2;1200.0;M;Robert;Williams;3000.00;2026-01-03T12:30:00
                3;true;45;1;85000;5.0;2000.0;F;Patricia;Brown;4200.20;2026-01-04T08:45:00
                4;false;28;4;52000;3.5;0.0;M;Michael;Jones;1100.10;2026-01-05T09:00:00
                5;true;35;2;70000;4.8;1500.0;F;Jennifer;Garcia;5500.00;2026-01-06T14:20:00
                6;true;50;1;95000;4.9;3000.0;M;William;Miller;8900.50;2026-01-07T16:10:00
                7;false;19;5;30000;2.1;100.0;F;Linda;Davis;500.25;2026-01-08T17:55:00
                8;true;42;2;78000;4.4;1800.0;M;David;Rodriguez;6700.80;2026-01-09T20:00:00
                9;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
                """
                .lines();
    }

    @Getter
    @Setter
    public static class UserBean {
        // Primitive types
        private int id;
        private boolean active;
        private byte age;
        private short rank;
        private long salary;
        private float rating;
        private double bonus;
        private char gender;

        // More objects
        private String firstName;
        private String lastName;
        private java.math.BigDecimal balance;
        private java.time.LocalDateTime createdAt;
    }

    public record UserRecord (
        // Primitive types
        int id,
        boolean active,
        byte age,
        short rank,
        long salary,
        float rating,
        double bonus,
        char gender,

        // More objects
        String firstName,
        String lastName,
        java.math.BigDecimal balance,
        java.time.LocalDateTime createdAt
        ){}
}