package org.ujorm.sample;

import junit.framework.TestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.ujorm.hotels.gui.HomePage;
import org.ujorm.hotels.gui.WicketApplication;
import org.ujorm.orm.OrmHandlerProvider;

/**
 * Simple test using the WicketTester
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:org/ujorm/hotels/config/ApplicationContext.xml"})
public class TestHomePage extends TestCase {

    @Autowired
    private OrmHandlerProvider ormProvider;
    private WicketTester tester;

    @Override
    public void setUp() {
        tester = new WicketTester(new WicketApplication(ormProvider));
    }

    public void testRenderMyPage() {
        //start and render the test page
        tester.startPage(HomePage.class);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
    }
}
