package markdown;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class Link {
	@Getter
	@Setter
	private String title;
	@Getter
	@Setter
	private String cacheId;
	@Getter
	@Setter
	private String inheritId;

	@Override
	public String toString() {
		if (!StringUtils.isBlank(cacheId)) {
			// Format: [タイトル](cache/キャッシュID/元継承ID)
			return String.format("[%s](cache/%s/%s)", title, cacheId, inheritId);
		}
		if (!StringUtils.isBlank(inheritId)) {
			// Format: [タイトル](element/元継承ID)
			return String.format("[%s](element/%s)", title, inheritId);
		}

		return String.format("[%s](element/)", title);
	}
}
