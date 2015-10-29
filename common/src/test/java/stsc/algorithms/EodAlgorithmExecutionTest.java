package stsc.algorithms;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import stsc.algorithms.primitive.eod.TestingEodAlgorithm;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.EodExecution;
import stsc.common.algorithms.MutatingAlgorithmConfiguration;

public final class EodAlgorithmExecutionTest {

	@Test
	public void testEodAlgorithmExecutionConstructor() {
		boolean exception = false;
		try {
			new EodExecution("execution1", "algorithm1", Mockito.mock(MutatingAlgorithmConfiguration.class));
		} catch (BadAlgorithmException e) {
			exception = true;
		}
		Assert.assertTrue(exception);
	}

	@Test
	public void testNameInstallingMethod() throws BadAlgorithmException {
		final EodExecution eae = new EodExecution("e1", "stsc.algorithms.primitive.eod.TestingEodAlgorithm", Mockito.mock(MutatingAlgorithmConfiguration.class));
		Assert.assertEquals("stsc.algorithms.primitive.eod.TestingEodAlgorithm", eae.getAlgorithmName());
	}

	@Test
	public void testExecution() throws BadAlgorithmException {
		EodExecution e3 = new EodExecution("e1", TestingEodAlgorithm.class.getName(), Mockito.mock(MutatingAlgorithmConfiguration.class));
		Assert.assertEquals(TestingEodAlgorithm.class.getName(), e3.getAlgorithmName());
		Assert.assertEquals("e1", e3.getExecutionName());
	}
}
