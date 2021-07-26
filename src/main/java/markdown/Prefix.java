package markdown;

import lombok.Getter;
import lombok.Setter;

public class Prefix {
	@Getter
	@Setter
	private Text text;

	public Prefix(String text) {
		this.text = new Text(text);
	}
}
