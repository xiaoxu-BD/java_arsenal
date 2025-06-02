package org.xiaoxu;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.xiaoxu.enums.Week;

/**
 * Unit test for simple App.
 */
@Slf4j
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    public void testApp2()
    {
        log.info("testApp2");
    }

    public void testApp3()
    {
        Week[] values = Week.values();
        for (Week value : values) {
            log.info("value.nums: {}",value.getNums());
            log.info("value.names: {}",value.getName());
        }
    }

    public void testApp4(){

     log.info("testApp4 {}", Week.MONDAY.getName());
    }



    public void testStringUtil(){
        StringUtils.isBlank(" ");
        log.info("testStringUtil {} ",StringUtils.isBlank(""));

        log.trace("TRACE: Starting the logging demo...");
        log.debug("DEBUG: Checking if string is blank: {}", StringUtils.isBlank(" "));
        log.info("INFO: Joining strings: {}", StringUtils.join(new String[]{"apple", "banana"}, ", "));
        log.warn("WARN: This is a warning message!");
        log.error("ERROR: Something went wrong!");
    }
}
