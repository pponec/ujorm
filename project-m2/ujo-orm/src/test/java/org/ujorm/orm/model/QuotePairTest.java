package org.ujorm.orm.model;



import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuotePairTest {

    @Test
    void testToQuotesForSymmetricPair() {
        assertEquals("\"", QuotePair.ofDefault().toQuotes());
        assertEquals("`", QuotePair.ofMySql().toQuotes());
    }

    @Test
    void testToQuotesForAsymmetricPair() {
        assertEquals("[]", QuotePair.ofMsSqlServer().toQuotes());
    }

    @Test
    void testToQuotesForNonePair() {
        assertEquals("", QuotePair.ofNone().toQuotes());
    }
}
