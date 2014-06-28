package stsc.general.simulator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.XMLConfigurationFactory;

import stsc.common.BadSignalException;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.general.statistic.Statistics;
import stsc.general.statistic.StatisticsCalculationException;
import stsc.general.trading.TradeProcessor;
import stsc.general.trading.TradeProcessorInit;

public class Simulator {

	static {
		System.setProperty(XMLConfigurationFactory.CONFIGURATION_FILE_PROPERTY, "./config/simulator.log4j2.xml");
	}

	private static Logger logger = LogManager.getLogger("Simulator");

	private final Statistics statistics;

	public Simulator(final SimulatorSettings settings) throws BadAlgorithmException, StatisticsCalculationException, BadSignalException {
		logger.info("Simulator starting");
		final TradeProcessor tradeProcessor = new TradeProcessor(settings.getInit());
		statistics = tradeProcessor.simulate(settings.getInit().getPeriod());
		logger.info("Simulated finished");
	}

	public static Simulator fromFile(final String filePath) throws BadAlgorithmException, StatisticsCalculationException, BadSignalException, Exception {
		return new Simulator(new SimulatorSettings(0, new TradeProcessorInit(filePath)));
	}

	public Statistics getStatistics() {
		return statistics;
	}

}