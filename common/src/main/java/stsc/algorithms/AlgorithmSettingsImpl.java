package stsc.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import stsc.common.FromToPeriod;
import stsc.common.algorithms.AlgorithmSetting;
import stsc.common.algorithms.AlgorithmSettings;
import stsc.common.algorithms.EodAlgorithmInit;
import stsc.common.algorithms.MutatingAlgorithmSettings;
import stsc.common.algorithms.StockAlgorithmInit;

/**
 * This is an implementation for collection of {@link AlgorithmSetting} for typed elements. Supported / expected types are: <br/>
 * 1. integer; <br/>
 * 2. double; <br/>
 * 3. string; <br/>
 * 4. sub-execution (string like but store order). <br/>
 * Also implements writeExternal / read (external).
 */
public final class AlgorithmSettingsImpl implements AlgorithmSettings, MutatingAlgorithmSettings {

	private final FromToPeriod period;
	private final HashMap<String, Integer> integers;
	private final HashMap<String, Double> doubles;
	private final HashMap<String, String> strings;
	private final ArrayList<String> subExecutions;

	public AlgorithmSettingsImpl(final StockAlgorithmInit init) {
		this(init.getSettings().getPeriod());
	}

	public AlgorithmSettingsImpl(final EodAlgorithmInit init) {
		this(init.getSettings().getPeriod());
	}

	public AlgorithmSettingsImpl(final FromToPeriod period) {
		this.period = period;
		this.integers = new HashMap<>();
		this.doubles = new HashMap<>();
		this.strings = new HashMap<>();
		this.subExecutions = new ArrayList<>();
	}

	private AlgorithmSettingsImpl(final AlgorithmSettingsImpl cloneFrom) {
		this.period = cloneFrom.period;
		this.integers = new HashMap<String, Integer>(cloneFrom.integers);
		this.doubles = new HashMap<String, Double>(cloneFrom.doubles);
		this.strings = new HashMap<String, String>(cloneFrom.strings);
		this.subExecutions = new ArrayList<String>(cloneFrom.subExecutions);
	}

	public AlgorithmSettingsImpl setString(final String key, final String value) {
		strings.put(key, value);
		return this;
	}

	public AlgorithmSettingsImpl setInteger(final String key, final Integer value) {
		integers.put(key, value);
		return this;
	}

	public AlgorithmSettingsImpl setDouble(final String key, final Double value) {
		doubles.put(key, value);
		return this;
	}

	public AlgorithmSettingsImpl addSubExecutionName(final String subExecutionName) {
		subExecutions.add(subExecutionName);
		return this;
	}

	@Override
	public Map<String, Integer> getIntegers() {
		return integers;
	}

	@Override
	public Map<String, Double> getDoubles() {
		return doubles;
	}

	@Override
	public Map<String, String> getStrings() {
		return strings;
	}

	@Override
	public Integer getInteger(final String key) {
		return integers.get(key);
	}

	@Override
	public Double getDouble(final String key) {
		return doubles.get(key);
	}

	@Override
	public String getString(final String key) {
		return strings.get(key);
	}

	@Override
	public List<String> getSubExecutions() {
		return subExecutions;
	}

	@Override
	public AlgorithmSetting<Integer> getIntegerSetting(final String key, final Integer defaultValue) {
		final Integer value = integers.get(key);
		if (value == null) {
			return new AlgorithmSettingImpl<Integer>(defaultValue);
		}
		return new AlgorithmSettingImpl<Integer>(value);
	}

	@Override
	public AlgorithmSetting<Double> getDoubleSetting(final String key, final Double defaultValue) {
		final Double value = doubles.get(key);
		if (value == null) {
			return new AlgorithmSettingImpl<Double>(defaultValue);
		}
		return new AlgorithmSettingImpl<Double>(value);
	}

	@Override
	public AlgorithmSetting<String> getStringSetting(final String key, final String defaultValue) {
		final String value = strings.get(key);
		if (value == null) {
			return new AlgorithmSettingImpl<String>(defaultValue);
		}
		return new AlgorithmSettingImpl<String>(value);
	}

	@Override
	public AlgorithmSettingsImpl clone() {
		return new AlgorithmSettingsImpl(this);
	}

	@Override
	public FromToPeriod getPeriod() {
		return period;
	}

	public void stringHashCode(StringBuilder sb) {
		for (Map.Entry<String, Integer> i : integers.entrySet()) {
			sb.append(i.getKey()).append(i.getValue());
		}
		for (Map.Entry<String, Double> i : doubles.entrySet()) {
			sb.append(i.getKey()).append(i.getValue());
		}
		for (Map.Entry<String, String> i : strings.entrySet()) {
			sb.append(i.getKey()).append(i.getValue());
		}
		for (String string : subExecutions) {
			sb.append(string);
		}
	}

	// Mutating methods

	@Override
	public void mutate(String name, Integer mutatedValue) {
		integers.put(name, mutatedValue);
	}

	@Override
	public void mutate(String name, Double mutatedValue) {
		doubles.put(name, mutatedValue);
	}

	@Override
	public void mutate(String name, String mutatedValue) {
		strings.put(name, mutatedValue);
	}

	@Override
	public void mutateSubExecution(int index, String value) {
		subExecutions.set(index, value);
	}

	// common methods

	@Override
	public String toString() {
		String result = "";
		for (int i = 0; i < subExecutions.size(); ++i) {
			result += subExecutions.get(i);
			if (i + 1 < subExecutions.size()) {
				result += ", ";
			}
		}
		result = addParameters(result, integers, "I");
		result = addParameters(result, doubles, "D");
		result = addParameters(result, strings, "");

		return result;
	}

	private <T> String addParameters(String result, HashMap<String, T> data, String postfix) {
		if (!result.isEmpty() && !data.isEmpty())
			result += ", ";
		final Iterator<Entry<String, T>> i = data.entrySet().iterator();
		while (i.hasNext()) {
			final Entry<String, T> e = i.next();
			result += e.getKey() + " = " + String.valueOf(e.getValue()) + postfix;
			if (i.hasNext())
				result += ", ";
		}
		return result;
	}

}
