package adages3;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.namespace.QName;

import com.fasterxml.jackson.databind.ObjectMapper;


public class Adages {
	private static CopyOnWriteArrayList<Adage> adages;
	private static AtomicInteger id;
	static {
		String[ ] aphorisms =
		{"What can be shown cannot be said.",
		"If a lion could talk, we could not understand him.",
		"Philosophy is a battle against the bewitchment of our intelligence by means of language.",
		"Ambition is the death of thought.",
		"The limits of my language mean the limits of my world."};
		adages = new CopyOnWriteArrayList<>();
		id = new AtomicInteger();
		for(String str: aphorisms){
			add(str);
		}
	}
	
	public static CopyOnWriteArrayList<Adage> getList() { return adages; }
	public static Adage[] getListAsArray() {
		return adages.toArray(new Adage[0]); 
	}
	public static void add(String words) {
		int localid = id.incrementAndGet();
		Adage adage = new Adage();
		adage.setWords(words);
		adage.setId(localid);
		adages.add(adage);
	}
	
	public static Adage find(int id){
		Adage adage = null;
		for(Adage obj: adages){
			if(obj.getId() == id){
				adage = obj;
				break;
			}
		}
		return adage;
	}
	public static void remove(Adage adage){
		adages.remove(adage);
	} 
	// Java Adage --> XML document
	@XmlElementDecl(namespace = "http://aphorism.adage", name = "adage")
	private JAXBElement<Adage> toXml(Adage adage) {
		return new JAXBElement<Adage>(new QName("adage"), Adage.class, adage);
	}

	// Java Adage --> JSON document
	// Jersey provides automatic conversion to JSON using the Jackson
	// libraries. In this example, the conversion is done manually
	// with the Jackson libraries just to indicate how straightforward it is.
	private String toJson(Adage adage) {
		String json = "If you see this, there's a problem.";
		try {
			json = new ObjectMapper().writeValueAsString(adage);
		} catch (Exception e) {
		}
		return json;
	}
	
	public static String toPlain() {
		String retval = "";
		int i = 1;
		for (Adage adage : adages)
			retval += adage.toString() + "\n";
		return retval;
	}
}