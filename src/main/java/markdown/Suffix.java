package markdown;

import lombok.Getter;
import lombok.Setter;

public class Suffix {
	@Getter
	@Setter
	private Link link;
	@Getter
	@Setter
	private Text text;
	@Getter
	@Setter
	private SuffixType type;

	public Suffix(SuffixType type, Link link) {
		this.type = type;
		this.link = link;
	}

	public Suffix(String t) {
		this.text = new Text(t);
		this.type = SuffixType.TEXT;
	}

	@Override
	public String toString() {
		if (this.type.equals(SuffixType.TEXT)) {
			return text.toString();
		}
		return link.toString();
	}

}
