package org.ujorm.sample;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.ujorm.hotels.config.SpringContext;
import org.ujorm.hotels.gui.HomePage;
import org.ujorm.hotels.gui.MainApplication;

/**
 * Simple test using the WicketTester
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = SpringContext.class)
//@WebAppConfiguration
public class HomePageTest {

    private WicketTester tester;

//    @BeforeEach
//    public void setUp() {
//        tester = new WicketTester(new MainApplication());
//    }

    /** Empty test */
    @Test
    public void testDummy() {
        Assertions.assertTrue(true);
    }

    /** Run application */
    @Test
    @Disabled
    public void XXX_testRenderMyPage() {
        //start and render the test page
        tester.startPage(HomePage.class);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
    }
}
