package org.xiaoxu;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import lombok.extern.slf4j.Slf4j;

/**
 * Unit test for simple App.
 */
@Slf4j
public class AppTest extends TestCase
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

        new Thread(()->{
            Thread.currentThread().setName("线程1");
            log.info("{}执行了", Thread.currentThread().getName());
        }).start();
    }
}
