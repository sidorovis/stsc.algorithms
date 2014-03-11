package stsc.storage;

import org.joda.time.LocalDate;

import stsc.algorithms.EodSignal;
import junit.framework.TestCase;

public class SignalsStorageTest extends TestCase {

	class TestSignal extends EodSignal {
		public final int id;

		public TestSignal(int id) {
			this.id = id;
		}
	}

	public void testSignalsStorage() throws BadSignalException {
		SignalsStorage signalsStorage = new SignalsStorage();
		signalsStorage.registerEodSignalsType("e1", TestSignal.class);
		signalsStorage.addEodSignal("e1", new LocalDate(2010, 10, 20).toDate(), new TestSignal(12));
		TestSignal ts = (TestSignal)signalsStorage.getEodSignal("e1", new LocalDate(2010, 10, 20).toDate());
		assertEquals(12, ts.id);
	}
}
