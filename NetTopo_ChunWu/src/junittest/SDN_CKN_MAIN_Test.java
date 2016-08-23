/**
 * 
 */
package junittest;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.deri.nettopo.algorithm.ckn.function.SDN_CKN_MAIN;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tony
 *
 */
public class SDN_CKN_MAIN_Test {

	private static Logger logger=Logger.getLogger(SDN_CKN_MAIN_Test.class);
	public static SDN_CKN_MAIN sdn = new SDN_CKN_MAIN();
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		sdn.run();
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		
	}

	/**
	 * Test method for {@link org.deri.nettopo.algorithm.ckn.function.SDN_CKN_MAIN#SDN_CKN_MAIN(org.deri.nettopo.algorithm.Algorithm)}.
	 */
	@Test
	public void testSDN_CKN_MAINAlgorithm() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.deri.nettopo.algorithm.ckn.function.SDN_CKN_MAIN#SDN_CKN_MAIN()}.
	 */
	@Test
	public void testSDN_CKN_MAIN() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.deri.nettopo.algorithm.ckn.function.SDN_CKN_MAIN#run()}.
	 */
	@Test
	public void testRun() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.deri.nettopo.algorithm.ckn.function.SDN_CKN_MAIN#runForStatistics()}.
	 */
	@Test
	public void testRunForStatistics() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.deri.nettopo.algorithm.ckn.function.SDN_CKN_MAIN#getAlgorithm()}.
	 */
	@Test
	public void testGetAlgorithm() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.deri.nettopo.algorithm.ckn.function.SDN_CKN_MAIN#isNeedInitialization()}.
	 */
	@Test
	public void testIsNeedInitialization() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.deri.nettopo.algorithm.ckn.function.SDN_CKN_MAIN#setNeedInitialization(boolean)}.
	 */
	@Test
	public void testSetNeedInitialization() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.deri.nettopo.algorithm.ckn.function.SDN_CKN_MAIN#getK()}.
	 */
	@Test
	public void testGetK() {
		assertEquals(2, sdn.getK());
	}

	/**
	 * Test method for {@link org.deri.nettopo.algorithm.ckn.function.SDN_CKN_MAIN#setK(int)}.
	 */
	@Test
	public void testSetK() {
		fail("Not yet implemented");
	}

}
