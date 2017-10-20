package com.polyglot.lucene.chp01;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author Rajiv Singla . Creation Date: 10/20/2017.
 */
class AppMainTest {

    private static final String[] args = {"-d", "C:\\Users\\rs153v\\Documents\\Personal", "-i", "C:\\tmp\\index"};

    @Test
    @DisplayName("Test Main method with valid arguments")
    void testMain() throws Exception {
        AppMain.main(args);
    }

}
