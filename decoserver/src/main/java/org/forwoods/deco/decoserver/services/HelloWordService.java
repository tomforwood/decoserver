package org.forwoods.deco.decoserver.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/HelloWorld")
public class HelloWordService {
	
	@GET
	@Path("/sayHello")
	public String sayHello() {
		return "Hello2";
	}

}
