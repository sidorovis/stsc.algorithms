package stsc.algorithms;

import org.junit.Assert;
import org.junit.Test;

import stsc.common.Settings;
import stsc.common.algorithms.AlgorithmSetting;
import stsc.common.algorithms.BadAlgorithmException;

public final class AlgorithmSettingsTest {

	@Test
	public void testAlgorithmsSettings() {
		final AlgorithmSettingsImpl as = new AlgorithmSettingsImpl();
		Assert.assertNull(as.getString("a"));
		Assert.assertNotNull(as.setDouble("a", new Double(14.05)));
		Assert.assertNotNull(as.setDouble("b", 14.05));

		Assert.assertEquals(as.getDouble("b"), as.getDouble("a"), Settings.doubleEpsilon);

		final AlgorithmSettingImpl<Double> asd = new AlgorithmSettingImpl<Double>(0.0);
		asd.setValue(as.getDouble("a"));
		Assert.assertEquals(14.05, asd.getValue(), Settings.doubleEpsilon);
	}

	@Test
	public void testGetIntegerDoubleTypes() throws BadAlgorithmException {
		final AlgorithmSettingsImpl as = new AlgorithmSettingsImpl();
		as.setInteger("asd", Integer.valueOf(15));
		as.setInteger("4asd", Integer.valueOf(1231));
		as.setDouble("param", Double.valueOf(1231.0));
		as.setDouble("para3m", Double.valueOf(125.454));
		Assert.assertEquals(Integer.valueOf(15), as.getInteger("asd"));
		Assert.assertEquals(Integer.valueOf(1231), as.getInteger("4asd"));
		Assert.assertEquals(Double.valueOf(1231.0), as.getDouble("param"));
		Assert.assertEquals(Double.valueOf(125.454), as.getDouble("para3m"));

		as.setDouble("kill", 15.343);
		final AlgorithmSetting<Double> d = as.getDoubleSetting("kill", 0.0);
		Assert.assertEquals(15.343, d.getValue(), Settings.doubleEpsilon);
	}
}
