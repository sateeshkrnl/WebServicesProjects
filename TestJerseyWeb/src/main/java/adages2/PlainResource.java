package adages2;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class PlainResource extends ServerResource {

	@Get
	public Representation toPlan(){
		String s = Adages.toPlain();
		return new StringRepresentation(s);
	}
}
