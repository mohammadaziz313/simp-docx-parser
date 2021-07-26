package markdown;

import lombok.Getter;
import lombok.Setter;

public class Text {
	@Getter
	@Setter
	private String value;

	Text(String value) {
		this.value = value;
	}
}
