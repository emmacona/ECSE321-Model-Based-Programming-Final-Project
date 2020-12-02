package ca.mcgill.ecse321.eventregistration.service.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import ca.mcgill.ecse321.eventregistration.dao.*;
import ca.mcgill.ecse321.eventregistration.service.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestCircus {

    @Autowired
    private EventRegistrationService service;

    @Autowired
    private CircusRepository circusRepository;

    @After
    public void clearDatabase() {
        circusRepository.deleteAll();
    }

    @Test
    public void test_01_CreateCircus() {
        assertEquals(0, service.getAllCircuses().size());

        String name = "Cirque du Soleil";
        String company = "Alegria";

        Calendar c = Calendar.getInstance();
        c.set(2019, Calendar.JANUARY, 18);
        Date circusDate = new Date(c.getTimeInMillis());

        LocalTime startTime = LocalTime.parse("09:00");
        LocalTime endTime = LocalTime.parse("18:00");

        try {
            service.createCircus(name, circusDate, Time.valueOf(startTime) , Time.valueOf(endTime), company);
        } catch (IllegalArgumentException e) {
            fail();
        }

        checkResultCircus(name, circusDate, startTime, endTime, company);
    }

    private void checkResultCircus(String name, Date circusDate, LocalTime startTime, LocalTime endTime, String company) {
        assertEquals(0, service.getAllPersons().size());
        assertEquals(1, service.getAllCircuses().size());
        assertEquals(name, service.getAllCircuses().get(0).getName());
        assertEquals(circusDate.toString(), service.getAllCircuses().get(0).getDate().toString());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        assertEquals(startTime.format(formatter).toString(), service.getAllCircuses().get(0).getStartTime().toString());
        assertEquals(endTime.format(formatter).toString(), service.getAllCircuses().get(0).getEndTime().toString());
        assertEquals(company, service.getAllCircuses().get(0).getCompany());
        assertEquals(0, service.getAllRegistrations().size());
    }

    @Test
    public void test_02_CreateCircusNull() {
        assertEquals(0, service.getAllRegistrations().size());

        String name = null;
        String company = null;
        Date circusDate = null;
        Time startTime = null;
        Time endTime = null;

        String error = null;
        try {
            service.createCircus(name, circusDate, startTime, endTime, company);
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        }

        // Check error
        assertTrue(error.contains("Event name cannot be empty!"));
        assertTrue(error.contains("Event date cannot be empty!"));
        assertTrue(error.contains("Event start time cannot be empty!"));
        assertTrue(error.contains("Event end time cannot be empty!"));
        // Check model in memory
        assertEquals(0, service.getAllCircuses().size());
    }

    @Test
    public void test_03_CreateCircusNameEmpty() {
        assertEquals(0, service.getAllCircuses().size());

        String name = "";
        String company = "Alegria";

        Calendar c = Calendar.getInstance();
        c.set(2019, Calendar.JANUARY, 18);
        Date circusDate = new Date(c.getTimeInMillis());

        LocalTime startTime = LocalTime.parse("09:00");
        LocalTime endTime = LocalTime.parse("18:00");

        String error = null;
        try {
            service.createCircus(name, circusDate, Time.valueOf(startTime), Time.valueOf(endTime), company);
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        }

        // Check error
        assertEquals("Event name cannot be empty!", error);
        // Check model in memory
        assertEquals(0, service.getAllCircuses().size());
    }

    @Test
    public void test_05_CreateCircusEndTimeBeforeStartTime() {
        assertEquals(0, service.getAllCircuses().size());

        String name = "Luzia";
        String company = "Alegria";

        Calendar c = Calendar.getInstance();
        c.set(2019, Calendar.MARCH, 7);
        Date circusDate = new Date(c.getTimeInMillis());

        LocalTime startTime = LocalTime.parse("18:00");
        LocalTime endTime = LocalTime.parse("09:00");

        String error = null;
        try {
            service.createCircus(name, circusDate, Time.valueOf(startTime), Time.valueOf(endTime), company);
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        }

        // Check error
        assertEquals("Event end time cannot be before event start time!", error);

        // Check model in memory
        assertEquals(0, service.getAllCircuses().size());
    }

    @Test
    public void test_07_CreateCircusCompanySpaces() {
        assertEquals(0, service.getAllCircuses().size());

        String name = "Volta";
        String company = " ";

        Calendar c = Calendar.getInstance();
        c.set(2019, Calendar.NOVEMBER, 22);
        Date circusDate = new Date(c.getTimeInMillis());

        LocalTime startTime = LocalTime.parse("09:00");
        LocalTime endTime = LocalTime.parse("18:00");

        String error = null;
        try {
            service.createCircus(name, circusDate, Time.valueOf(startTime), Time.valueOf(endTime), company);
        } catch (IllegalArgumentException e) {
            error = e.getMessage();
        }

        // Check error
        assertEquals("Circus company cannot be empty!", error);
        // Check model in memory
        assertEquals(0, service.getAllCircuses().size());
    }
}
