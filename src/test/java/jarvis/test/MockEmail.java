package jarvis.test;

import java.util.Map;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

public class MockEmail extends Email{

	public MockEmail() {
		this.hostName = null;
		this.ssl = true;
	}
	
	@Override
	public Email setMsg(String msg) throws EmailException {
		return null;
	}

	//No getter for Email's header map exists, but it is protected, so it is accessible from here
	public Map<String, String> getHeaders(){
		return this.headers;
	}
}
