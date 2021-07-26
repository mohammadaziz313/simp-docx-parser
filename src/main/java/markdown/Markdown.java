package markdown;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

public class Markdown {
	@Getter
	@Setter
	private Prefix prefix;
	@Getter
	@Setter
	private Suffix suffix;
	@Getter
	@Setter
	private MarkdownType type;

	// POC purpose
	private static final String cacheLinkRegex = "\\[(.*)\\]\\(cache\\/([0-9]*)\\/(.*)\\)";
	// POC purpose
	private static final Pattern cacheLinkPattern = Pattern.compile(cacheLinkRegex);
	// POC purpose
	private static final String linkRegex = "(?<!\\\\)\\[*(?<!\\\\)\\]\\(element\\/([0-9]+)\\)";
	// POC purpose
	private static final Pattern linkPattern = Pattern.compile(linkRegex);

	public Markdown(Prefix prefix, Suffix suffix, MarkdownType type) {
		this.suffix = suffix;
		this.prefix = prefix;
		this.type = type;
	}

	@Override
	public String toString() {
		if (StringUtils.isBlank(prefix.toString())) {
			return suffix.toString();
		}
		return String.format("%s %s", prefix.toString(), suffix.toString());
	}

	// POC purpose
	public static List<String[]> splitMarkdown(String data) {
		String[] splitData = data.split("\n");
		List<String[]> markdownInfo = new ArrayList<>();

		for (String line : splitData) {
			Matcher matcher = linkPattern.matcher(line);

			if (matcher.find()) {
				// Support for extracting cache data
				// markdownInfo.add(new String[] { matcher.group(1), matcher.group(2),
				// matcher.group(3) });
				// Support for extracting markdown link info
				markdownInfo.add(new String[] { matcher.group(1) });
			}
		}

		return markdownInfo;
	}

	// POC purpose
	public static List<String[]> splitMarkdownCache(String data) {
		String[] splitData = data.split("\n");
		List<String[]> markdownInfo = new ArrayList<>();

		for (String line : splitData) {
			Matcher matcher = cacheLinkPattern.matcher(line);

			if (matcher.find()) {
				markdownInfo.add(new String[] { matcher.group(1), matcher.group(2), matcher.group(3) });
			}
		}

		return markdownInfo;
	}
}
