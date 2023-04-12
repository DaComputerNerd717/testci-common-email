package jarvis.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.EmailException;
import org.junit.Before;
import org.junit.Test;

public class EmailTest {

	Email mail;
	
	@Before
	public void setup() {
		mail = new MockEmail();
	}
	
	@Test
	public void addBccTest() {
		//make a list of the strings. Type doesn't matter with assertIterableEquals
		String[] strings = new String[]{"test1@gmail.com", "test2@msn.com", "test3@hotmail.com", "test4@gmail.com"};
		try {
			//Add the same strings
			Email withBcc = mail.addBcc(strings[0], strings[1], strings[2], strings[3]);
			//Get the string representations of the addresses back to match with the strings we gave
			String[] actuals = new String[4];
			for(int i = 0; i < 4; i++) //get each string representation of the addresses
				actuals[i] = withBcc.getBccAddresses().get(i).getAddress();
			assertArrayEquals("BCCs don't match", strings, actuals);
		} catch (EmailException e) { 
			System.err.println("EmailException thrown by addBcc(String...)");
			e.printStackTrace();
		}
	}
	
	@Test(expected=EmailException.class)
	public void addBccInvalidTest() throws EmailException {
		mail.addBcc((String[]) null);
	}
	
	@Test
	public void AddCcTest() {
		String addr = "test@hotmail.com";
		Email withCC;
		try {
			//Set the address to our test string
			withCC = mail.addCc(addr);
			//Get the first sstring address back from the CC list
			String actual = withCC.getCcAddresses().get(0).toString();
			assertTrue("CCs don't match", addr.equals(actual));
		} catch (EmailException e) {
			System.err.println("EmailException thrown by addCc(String)");
			e.printStackTrace();
		}
	}
	
	@Test
	public void addHeaderTest() {
		mail.addHeader("testKey", "testValue");
		//Unfortunately, there is no getter for the protected header map, so one was added to MockEmail
		Map<String, String> headers = ((MockEmail) mail).getHeaders();
		assertTrue("Header missing key", headers.containsKey("testKey"));
		assertTrue("Header missing value", headers.containsValue("testValue"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void addHeaderNullKeyTest() {
		mail.addHeader(null, "test");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void addHeaderNullValueTest() {
		mail.addHeader("test", null);
	}
	
	@Test
	public void addReplyToTest() {
		try {
			Email withReply = mail.addReplyTo("test@gmail.com");
			//Get first reply address
			List<InternetAddress> addrs = withReply.getReplyToAddresses();
			InternetAddress addr = addrs.get(0);
			String replyTo = addr.getAddress();
			assertTrue("Reply address doesn't match", replyTo.equals("test@gmail.com"));
		} catch (EmailException e) {
			System.err.println("EmailException thrown by addReplyTo(String)");
			e.printStackTrace();
		}
	}
	
	@Test
	public void buildMimeMessageTest() {
		try {
			mail.setFrom("test@gmail.com");
			List<InternetAddress> to = new LinkedList<>();
			to.add(new InternetAddress("test2@gmail.com"));
			mail.setTo(to);
			mail.setHostName("testname");
			mail.setContent(new MimeMultipart());
			mail.buildMimeMessage();
			mail.setCharset(Charset.defaultCharset().name());
			MimeMessage msg = mail.getMimeMessage();
			Address addr = msg.getAllRecipients()[0];
			assertTrue("Address is not an InternetAddress", addr instanceof InternetAddress);
			InternetAddress iAddr = (InternetAddress) addr;
			assertTrue("To address is wrong", iAddr.getAddress().equals("test2@gmail.com"));
			Address addrFrom = msg.getFrom()[0];
			assertTrue("Address is not an InternetAddress", addrFrom instanceof InternetAddress);
			InternetAddress iAddrFrom = (InternetAddress) addrFrom;
			assertTrue("From address is wrong", iAddrFrom.getAddress().equals("test@gmail.com"));
		} catch (EmailException e) {
			System.err.println("EmailException thrown in buildMimeMessageTest()");
			e.printStackTrace();
		} catch (AddressException e) {
			System.err.println("AddressException thrown in buildMimeMessageTest()");
			e.printStackTrace();
		} catch (MessagingException e) {
			System.err.println("MessagingException thrown in buildMimeMessageTest()");
			e.printStackTrace();
		}
	}
	
	@Test
	public void buildMimeMessageTest2() {
		try {
			mail.setSubject("test");
			List<InternetAddress> to = new LinkedList<>();
			to.add(new InternetAddress("test2@gmail.com"));
			mail.setTo(to);
			mail.setFrom("test4@test.test");
			mail.addBcc("test@test.test");
			mail.addCc("test2@test.test");
			mail.addHeader("testKey", "testValue");
			mail.setHostName("testname");
			mail.setContent("test content", EmailConstants.TEXT_PLAIN);
			mail.buildMimeMessage();
			MimeMessage msg = mail.getMimeMessage();
			Address addr = msg.getAllRecipients()[0];
			assertTrue("Address is not an InternetAddress", addr instanceof InternetAddress);
			InternetAddress iAddr = (InternetAddress) addr;
			assertTrue("To address is wrong", iAddr.getAddress().equals("test2@gmail.com"));
			Address addrFrom = msg.getFrom()[0];
			assertTrue("Address is not an InternetAddress", addrFrom instanceof InternetAddress);
			InternetAddress iAddrFrom = (InternetAddress) addrFrom;
			assertTrue("From address is wrong", iAddrFrom.getAddress().equals("test4@test.test"));
		} catch (EmailException e) {
			System.err.println("EmailException thrown in buildMimeMessageTest()");
			e.printStackTrace();
		} catch (AddressException e) {
			System.err.println("AddressException thrown in buildMimeMessageTest()");
			e.printStackTrace();
		} catch (MessagingException e) {
			System.err.println("MessagingException thrown in buildMimeMessageTest()");
			e.printStackTrace();
		}
	}
	
	@Test(expected=EmailException.class)
	public void buildMimeMessageTestInvalid() throws EmailException, AddressException {
		mail.setSubject("test");
		List<InternetAddress> to = new LinkedList<>();
		to.add(new InternetAddress("test2@gmail.com"));
		mail.setTo(to);
		mail.addBcc("test@test.test");
		mail.addCc("test2@test.test");
		mail.addHeader("testKey", "testValue");
		mail.setHostName("testname");
		mail.setContent("test content", EmailConstants.TEXT_PLAIN);
		mail.buildMimeMessage();
	}
	
	@Test
	public void getHostNameTest() {
		String hostTest = "testHostPleaseIgnore";
		mail.setHostName(hostTest);
		assertTrue(mail.getHostName().equals(hostTest));
	}
	
	@Test
	public void getHostNameNullTest() {
		assertEquals(null, mail.getHostName());
	}
	
	@Test
	public void getHostNameFromSessionTest() {
		Properties prop = new Properties();
		prop.setProperty(EmailConstants.MAIL_SMTP_AUTH, "false");
		prop.setProperty(EmailConstants.MAIL_HOST, "testhost");
		Session session = Session.getDefaultInstance(prop);
		mail.setMailSession(session);
		assertEquals("testhost", mail.getHostName());
	}
	
	@Test
	public void getMailSessionTest() {
		Properties prop = new Properties();
		prop.setProperty(EmailConstants.MAIL_SMTP_AUTH, "false");
		Session session = Session.getDefaultInstance(prop);
		mail.setMailSession(session);
		try {
			Session sessBack = mail.getMailSession();
			assertSame("Sessions don't match", sessBack, session);
		} catch (EmailException e) {
			System.err.println("EmailException thrown by getMailSession()");
			e.printStackTrace();
		}
	}
	
	@Test
	public void getMailSessionSSLTest() {
		Properties prop = new Properties();
		prop.setProperty(EmailConstants.MAIL_SMTP_PASSWORD, "password123");
		prop.setProperty(EmailConstants.MAIL_SMTP_USER, "test");
		prop.setProperty(EmailConstants.MAIL_SMTP_AUTH, "true");
		prop.setProperty(EmailConstants.MAIL_HOST, "testhost");
		mail.setHostName(null);
		Email mail2 = mail.setSendPartial(true).setSSLOnConnect(true);
		Session session = Session.getInstance(prop);
		mail2.setMailSession(session);
		try {
			Session sessBack = mail2.getMailSession();
			assertNotEquals("Session not initialized", null, session);
		} catch (EmailException e) {
			System.err.println("EmailException thrown by getMailSession()");
			e.printStackTrace();
		}
	}
	
	@Test
	public void getSentDateTest() {
		Date date = Date.from(Instant.now());
		mail.setSentDate(date);
		Date dateBack = mail.getSentDate();
		assertEquals("Dates don't match", date.getTime(), dateBack.getTime());
	}
	
	@Test
	public void getSocketConnectionTimeoutTest() {
		int millis = 1050;
		mail.setSocketConnectionTimeout(millis);
		assertEquals("Socket connection timeout times not equal", millis, mail.getSocketConnectionTimeout());
	}
	
	@Test
	public void setFromTest() {
		String addr = "test@gmail.com";
		try {
			mail.setFrom(addr);
			String addrBack = mail.getFromAddress().getAddress();
			assertTrue(addr.equals(addrBack));
		} catch (EmailException e) {
			System.err.println("EmailException thrown by setFrom()");
			e.printStackTrace();
		}
	}
}
