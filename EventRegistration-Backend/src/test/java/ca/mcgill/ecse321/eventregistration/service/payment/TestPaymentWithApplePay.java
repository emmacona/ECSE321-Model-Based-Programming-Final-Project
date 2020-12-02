package ca.mcgill.ecse321.eventregistration.service.payment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import ca.mcgill.ecse321.eventregistration.dao.ApplePayRepository;
import ca.mcgill.ecse321.eventregistration.dao.EventRepository;
import ca.mcgill.ecse321.eventregistration.dao.PersonRepository;
import ca.mcgill.ecse321.eventregistration.dao.RegistrationRepository;
import ca.mcgill.ecse321.eventregistration.model.ApplePay;
import ca.mcgill.ecse321.eventregistration.model.Event;
import ca.mcgill.ecse321.eventregistration.model.Person;
import ca.mcgill.ecse321.eventregistration.model.Registration;
import ca.mcgill.ecse321.eventregistration.service.EventRegistrationService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestPaymentWithApplePay {
	@Autowired
	private EventRegistrationService service;

	@Autowired
	private PersonRepository personRepository;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private RegistrationRepository registrationRepository;
	@Autowired
	private ApplePayRepository applePayRepository;

	@After
	public void clearDatabase() {
		// Fisrt, we clear registrations to avoid exceptions due to inconsistencies
		registrationRepository.deleteAll();
		// Then we can clear the other tables
		personRepository.deleteAll();
		eventRepository.deleteAll();
		applePayRepository.deleteAll();
	}

	@Test
	public void test_01_testPayWithApplePay() {
		try {
			Person person = TestUtils.setupPerson(service, TestPaymentWithApplePayData.TEST01_PERSON_NAME);
			Event event = TestUtils.setupEvent(service, TestPaymentWithApplePayData.TEST01_EVENT_NAME);
			Registration r = TestUtils.register(service, person, event);
			ApplePay ap = service.createApplePay(TestPaymentWithApplePayData.TEST01_VALID_ID,
					TestPaymentWithApplePayData.TEST01_VALID_AMOUNT);
			service.pay(r, ap);
			List<Registration> allRs = service.getAllRegistrations();
			assertEquals(allRs.size(), 1);
			applePayAsserts(ap, allRs.get(0).getApplePay());

		} catch (IllegalArgumentException e) {
			fail();
		}
	}

	@Test
	public void test_04_testMultiplePaysBreakNegative() {
		int breakIndex = TestPaymentWithApplePayData.TEST04_BREAK_INDEX;
		String[] ids = TestPaymentWithApplePayData.TEST04_VALID_IDS;
		int[] amounts = TestPaymentWithApplePayData.TEST04_PARTIAL_BREAK_AMOUNTS;
		String[] names = TestPaymentWithApplePayData.TEST04_PERSON_NAMES;
		String[] events = TestPaymentWithApplePayData.TEST04_EVENT_NAMES;
		int length = ids.length;
		ApplePay[] pays = new ApplePay[length];

		try {
			for (int i = 0; i < length; i++) {
				Person person = TestUtils.setupPerson(service, names[i]);
				Event event = TestUtils.setupEvent(service, events[i]);
				Registration r = TestUtils.register(service, person, event);
				pays[i] = service.createApplePay(ids[i], amounts[i]);
				service.pay(r, pays[i]);
			}
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(TestPaymentWithApplePayData.AMOUNT_NEGATIVE_ERROR, e.getMessage());
			List<Registration> allRs = service.getAllRegistrations();
			assertEquals(allRs.size(), breakIndex + 1);
			for (int i = 0; i < breakIndex; i++) {
				if (!contains(allRs, pays[i])) {
					fail();
				}
			}
		}
	}

	@Test
	public void test_05_testUpdatePay() {
		try {
			Person person = TestUtils.setupPerson(service, TestPaymentWithApplePayData.TEST05_PERSON_NAME);
			Event event = TestUtils.setupEvent(service, TestPaymentWithApplePayData.TEST05_EVENT_NAME);
			Registration r = TestUtils.register(service, person, event);
			ApplePay ap1 = service.createApplePay(TestPaymentWithApplePayData.TEST05_INITIAL_ID,
					TestPaymentWithApplePayData.TEST05_INITIAL_AMOUNT);
			ApplePay ap2 = service.createApplePay(TestPaymentWithApplePayData.TEST05_AFTER_ID,
					TestPaymentWithApplePayData.TEST05_AFTER_AMOUNT);
			service.pay(r, ap1);
			List<Registration> allRs1 = service.getAllRegistrations();
			assertEquals(allRs1.size(), 1);
			applePayAsserts(ap1, allRs1.get(0).getApplePay());
			service.pay(r, ap2);
			List<Registration> allRs2 = service.getAllRegistrations();
			assertEquals(allRs2.size(), 1);
			applePayAsserts(ap2, allRs2.get(0).getApplePay());
		} catch (IllegalArgumentException e) {
			fail();
		}
	}

	@Test
	public void test_06_testCreateApplePay() {
		try {
			ApplePay ap = service.createApplePay(TestPaymentWithApplePayData.TEST06_VALID_ID,
					TestPaymentWithApplePayData.TEST06_VALID_AMOUNT);
			assertEquals(1, applePayRepository.count());
			for (ApplePay pay : applePayRepository.findAll()) {
				applePayAsserts(ap, pay);
			}
		} catch (IllegalArgumentException e) {
			fail();
		}
	}

	@Test
	public void test_08_testPayWithRegistrationNull() {
		try {
			Person person = TestUtils.setupPerson(service, TestPaymentWithApplePayData.TEST08_PERSON_NAME);
			Event event = TestUtils.setupEvent(service, TestPaymentWithApplePayData.TEST08_EVENT_NAME);
			Registration r = TestUtils.register(service, person, event);
			ApplePay ap = null;
			service.pay(r, ap);
			fail();

		} catch (IllegalArgumentException e) {
			assertEquals(TestPaymentWithApplePayData.PAY_WITH_NULL_ERROR, e.getMessage());
		}
	}

	@Test
	public void test_09_testCreateApplePayWrongFormat() {
		try {
			service.createApplePay(TestPaymentWithApplePayData.TEST09_WRONG_ID,
					TestPaymentWithApplePayData.TEST09_VALID_AMOUNT);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(TestPaymentWithApplePayData.ID_FORMAT_ERROR, e.getMessage());
		}
	}

	@Test
	public void test_10_testCreateApplePayLongFormat() {
		try {
			service.createApplePay(TestPaymentWithApplePayData.TEST10_WRONG_ID,
					TestPaymentWithApplePayData.TEST10_VALID_AMOUNT);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(TestPaymentWithApplePayData.ID_FORMAT_ERROR, e.getMessage());
		}
	}

	@Test
	public void test_11_testCreateApplePayNull() {
		try {
			service.createApplePay(TestPaymentWithApplePayData.TEST11_WRONG_ID,
					TestPaymentWithApplePayData.TEST11_VALID_AMOUNT);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(TestPaymentWithApplePayData.ID_FORMAT_ERROR, e.getMessage());
		}

	}

	@Test
	public void test_12_testCreateApplePayEmpty() {
		try {
			service.createApplePay(TestPaymentWithApplePayData.TEST12_WRONG_ID,
					TestPaymentWithApplePayData.TEST12_VALID_AMOUNT);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(TestPaymentWithApplePayData.ID_FORMAT_ERROR, e.getMessage());
		}
	}

	@Test
	public void test_13_testCreateApplePaySpace() {
		try {
			service.createApplePay(TestPaymentWithApplePayData.TEST13_WRONG_ID,
					TestPaymentWithApplePayData.TEST13_VALID_AMOUNT);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(TestPaymentWithApplePayData.ID_FORMAT_ERROR, e.getMessage());
		}
	}

	@Test
	public void test_14testCreateApplePayZero() {
		try {
			ApplePay ap = service.createApplePay(TestPaymentWithApplePayData.TEST14_VALID_ID,
					TestPaymentWithApplePayData.TEST14_VALID_AMOUNT);
			assertEquals(1, applePayRepository.count());
			for (ApplePay pay : applePayRepository.findAll()) {
				applePayAsserts(ap, pay);
			}
		} catch (IllegalArgumentException e) {
			fail();
		}
	}

	// Utility Methods, no test
	public void applePayAsserts(ApplePay expected, ApplePay actual) {
		assertNotEquals(null, actual);
		assertEquals(expected.getAmount(), actual.getAmount());
		assertEquals(expected.getDeviceID(), actual.getDeviceID());
	}

	public boolean applePayEquals(ApplePay pay1, ApplePay pay2) {
		return pay2 != null && pay2.getAmount() == pay1.getAmount() && pay2.getDeviceID().equals(pay1.getDeviceID());
	}

	public boolean contains(List<Registration> rs, ApplePay pay) {
		for (Registration r : rs) {
			if (applePayEquals(pay, r.getApplePay())) {
				return true;
			}
		}
		return false;
	}
}
