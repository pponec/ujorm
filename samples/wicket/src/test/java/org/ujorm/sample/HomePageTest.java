package org.ujorm.sample;

import junit.framework.TestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.ujorm.hotels.config.SpringContext;
import org.ujorm.hotels.gui.HomePage;
import org.ujorm.hotels.gui.MainApplication;

/**
 * Simple test using the WicketTester
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringContext.class)
public class HomePageTest extends TestCase {

    private WicketTester tester;

    @Override
    public void setUp() {
        tester = new WicketTester(new MainApplication());
    }

    /** Empty test */
    @Test
    public void testDummy() {
    }

    /** Run application */
    public void XXX_testRenderMyPage() {
        //start and render the test page
        tester.startPage(HomePage.class);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
    }
}
