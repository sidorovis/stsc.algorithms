package stsc.algorithms;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import stsc.algorithms.primitive.eod.TestingEodAlgorithm;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.EodExecutionInstance;
import stsc.common.algorithms.MutableAlgorithmConfiguration;

public final class EodAlgorithmExecutionTest {

	@Test
	public void testEodAlgorithmExecutionConstructor() {
		boolean exception = false;
		try {
			new EodExecutionInstance("execution1", "algorithm1", Mockito.mock(MutableAlgorithmConfiguration.class));
		} catch (BadAlgorithmException e) {
			exception = true;
		}
		Assert.assertTrue(exception);
	}

	@Test
	public void testNameInstallingMethod() throws BadAlgorithmException {
		final EodExecutionInstance eae = new EodExecutionInstance("e1", "stsc.algorithms.primitive.eod.TestingEodAlgorithm", Mockito.mock(MutableAlgorithmConfiguration.class));
		Assert.assertEquals("stsc.algorithms.primitive.eod.TestingEodAlgorithm", eae.getAlgorithmName());
	}

	@Test
	public void testExecution() throws BadAlgorithmException {
		EodExecutionInstance e3 = new EodExecutionInstance("e1", TestingEodAlgorithm.class.getName(), Mockito.mock(MutableAlgorithmConfiguration.class));
		Assert.assertEquals(TestingEodAlgorithm.class.getName(), e3.getAlgorithmName());
		Assert.assertEquals("e1", e3.getExecutionName());
	}
}
