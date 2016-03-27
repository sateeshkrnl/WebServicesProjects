package adages3;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "adage")
public class Adage {
	private String words;
	private int wordCount;
	private int id;
	
	public Adage() {
	}

	@Override
	public String toString() {
		return String.format("%2d: ", id) + words + " -- " + wordCount + " words";
	}

	public void setWords(String words) {
		this.words = words;
		this.wordCount = words.trim().split("\\s+").length;
	}

	public String getWords() {
		return this.words;
	}

	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}

	public int getWordCount() {
		return this.wordCount;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public boolean equals(Object obj) {		
		return this.id == ((Adage)obj).getId();
	}
}