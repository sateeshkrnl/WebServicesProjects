package adages3;

import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.annotation.Resource;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.http.HTTPException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

@WebServiceProvider
@ServiceMode(Mode.MESSAGE)
@BindingType(HTTPBinding.HTTP_BINDING)
public class AdagesProvider implements Provider<Source> {
	@Resource
	private WebServiceContext webServiceCtx;

	public AdagesProvider() {
	}

	@Override
	public Source invoke(Source request) {
		Source response;
		if (webServiceCtx == null)
			throw new RuntimeException("Injection failed on webservice context");
		MessageContext msgCtx = webServiceCtx.getMessageContext();
		String httpVerb = (String) msgCtx.get(MessageContext.HTTP_REQUEST_METHOD);
		switch (httpVerb) {
		case "GET":
			response = this.doGet(msgCtx);
			break;
		case "POST":
			response = this.doPost(request);
			break;
		case "PUT":
			response = this.doPut(request);
			break;
		case "DELETE":
			response = this.doDelete(msgCtx);
			break;
		default:
			throw new HTTPException(405);
		}
		return response;
	}

	private Source doGet(MessageContext msgCtx) {
		String qs = (String) msgCtx.get(MessageContext.QUERY_STRING);
		if (qs == null)
			return adages2Xml();
		else {
			int id = getId(qs);
			if (id < 0)
				throw new HTTPException(400); // bad request
			Adage adage = Adages.find(id);
			if (adage == null)
				throw new HTTPException(404); // not found
			return adage2Xml(adage);
		}
	}

	private Source doPost(Source request) {
		if (request == null)
			throw new HTTPException(400); // bad request
		InputSource in = toInputSource(request);
		String pattern = "//words/text()"; // find the Adage's "words"
		String words = findElement(pattern, in);
		if (words == null)
			throw new HTTPException(400); // bad request
		Adages.add(words);
		String msg = "The adage '" + words + "' has been created.";
		return toSource(toXml(msg));
	}

	private Source doPut(Source request) {
		if (request == null)
			throw new HTTPException(400); // bad request
		InputSource in = toInputSource(request);
		String pattern = "//words/text()"; // find the Adage's "words"
		String words = findElement(pattern, in);
		if (words == null)
			throw new HTTPException(400); // bad request
		// Format in XML is: <words>!<id>
		String[] parts = words.split("!");
		if (parts[0].length() < 1 || parts[1].length() < 1)
			throw new HTTPException(400); // bad request
		int id = -1;
		try {
			id = Integer.parseInt(parts[1].trim());
		} catch (Exception e) {
			throw new HTTPException(400);
		} // bad request
		// Find and edit.
		Adage adage = Adages.find(id);
		if (adage == null)
			throw new HTTPException(404); // not found
		adage.setWords(parts[0]);
		String msg = "Adage " + adage.getId() + " has been updated.";
		return toSource(toXml(msg));
	}

	private Source doDelete(MessageContext mctx) {
		String qs = (String) mctx.get(MessageContext.QUERY_STRING);
		// Disallow the deletion of all teams at once.
		if (qs == null)
			throw new HTTPException(403); // illegal operation
		else {
			int id = getId(qs);
			if (id < 0)
				throw new HTTPException(400); // bad request
			Adage adage = Adages.find(id);
			if (adage == null)
				throw new HTTPException(404); // not found
			Adages.remove(adage);
			String msg = "Adage " + id + " removed.";
			return toSource(toXml(msg));
		}
	}
	
	private int getId(String qs) {
		int badId = -1; // bad ID
		String[] parts = qs.split("=");
		if (!parts[0].toLowerCase().trim().equals("id"))
			return badId;
		int goodId = badId; // for now
		try {
			goodId = Integer.parseInt(parts[1].trim());
		} catch (Exception e) {
			return badId;
		}
		return goodId;
	}
	
	private StreamSource adages2Xml() {
		String str = toXml(Adages.getListAsArray());
		return toSource(str);
	}

	private StreamSource adage2Xml(Adage adage) {
		String str = toXml(adage);
		return toSource(str);
	}

	private String toXml(Object obj) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLEncoder enc = new XMLEncoder(out);
		enc.writeObject(obj);
		enc.close();
		return out.toString();
	}
	
	private StreamSource toSource(String str) {
		return new StreamSource(new StringReader(str));
	}
	
	private InputSource toInputSource(Source source) {
		InputSource input = null;
		try {
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(bos);
			trans.transform(source, result);
			input = new InputSource(new ByteArrayInputStream(bos.toByteArray()));
		} catch (Exception e) {
			throw new HTTPException(500);
		} // internal server error
		return input;
	}

	private String findElement(String expression, InputSource source) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		String retval = null;
		try {
			retval = (String) xpath.evaluate(expression, source, XPathConstants.STRING);
		} catch (Exception e) {
			throw new HTTPException(400);
		} // bad request
		return retval;
	}
}
