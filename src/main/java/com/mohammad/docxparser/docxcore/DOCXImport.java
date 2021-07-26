package com.mohammad.docxparser.docxcore;

import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import markdown.Markdown;
import markdown.MarkdownType;
import markdown.Prefix;
import markdown.Suffix;

public class DOCXImport {

	public String importDocument(InputStream stream, String docTitle) {
		try (XWPFDocument document = new XWPFDocument(stream)) {
			processBodyElements(document.getBodyElements());
		} catch (IOException e) {
			// var msg = "Document couldn't be instantiated.";
			e.printStackTrace();
		}
		// TODO: change returning value to a valid markdown
		return "";
	}

	// Processes all the elements in the DOCX file
	private void processBodyElements(List<IBodyElement> bodyElements) {
		List<Markdown> markdown = new ArrayList<>();
		boolean isStartProcessing = false;
		for (var bodyElement : bodyElements) {
			// looking for the first header
			if (!isStartProcessing) {
				if (!isProcessDocument(bodyElement)) {
					continue;
				}
				isStartProcessing = true;
			}
			switch (bodyElement.getElementType()) {
			case CONTENTCONTROL:
				// TODO: needs to be implemented
				break;
			case PARAGRAPH:
				markdown.addAll(processParagraph((XWPFParagraph) bodyElement));
				break;
			case TABLE:
				processTable((XWPFTable) bodyElement);
				break;
			}
		}
	}

	// Determines whether document processing should start or not
	private boolean isProcessDocument(IBodyElement element) {
		if (element instanceof XWPFParagraph) {
			// checking whether it is a header or not
			if (getHeadingLevel((XWPFParagraph) element) > 0) {
				return true;
			}
		}
		return false;
	}

	// Handles paragraphs, headings and inner paragraph elements(such as images)
	private List<Markdown> processParagraph(XWPFParagraph p) {
		int lvl = getHeadingLevel(p);
		// processing heading
		if (lvl != 0) {
			return List.of(processHeading(p, lvl));
		}

		// processing paragraph
		List<Markdown> runs = new ArrayList<>();
		for (var r : p.getRuns()) {
			runs.addAll(processRun(r));
		}
		return runs;
	}

	// Returns the heading level
	private int getHeadingLevel(XWPFParagraph p) {
		// return 0, if style is not present
		if (StringUtils.isBlank(p.getStyle())) {
			return 0;
		}

		final String styleName = p.getStyle().toLowerCase();

		// checking whether the style name is heading or 見出し
		if (styleName.contains("heading") || styleName.contains("見出し")) {
			return styleName.charAt(styleName.length() - 1) - '0';
		}

		// getting the style from the document
		XWPFStyle style = p.getDocument().getStyles().getStyle(styleName);

		// return 0 in case style is not present
		if (Objects.isNull(style)) {
			return 0;
		}

		var pStyleName = style.getName().toLowerCase();
		// checking whether the style name is heading or 見出し
		if (!StringUtils.isBlank(pStyleName) && (pStyleName.contains("heading") || pStyleName.contains("見出し"))) {
			return pStyleName.charAt(pStyleName.length() - 1) - '0';
		}

		return 0;
	}

	// Processing heading and returning level
	private Markdown processHeading(XWPFParagraph p, int lvl) {
		return new Markdown(new Prefix("#".repeat(lvl)), new Suffix(p.getText()), MarkdownType.PLAINTEXT);
		// return String.format("%s %s", "#".repeat(lvl), p.getText());
	}

	// Processing the text run
	private List<Markdown> processRun(XWPFRun run) {
		if (run.getEmbeddedPictures().size() > 0) {
			// TODO: implement image processing
		}
		return List.of(processTextRun(run));
	}

	// Processing the text and adding styling to it
	private Markdown processTextRun(XWPFRun run) {
		List<String> text = new ArrayList<>();
		// support for bold
		if (run.isBold()) {
			text.add("**");
		}

		// support for italics
		if (run.isItalic()) {
			text.add("*");
		}

		// support for strike through
		if (run.isStrikeThrough() || run.isDoubleStrikeThrough()) {
			text.add("~~");
		}

		// support for underline
		if (!run.getUnderline().equals(UnderlinePatterns.NONE)) {
			text.add("<u>");
		}

		// in case there is no styling to be attached
		if (text.size() == 0) {
			return new Markdown(new Prefix(""), new Suffix(run.text()), MarkdownType.PLAINTEXT);
			// return run.text();
		}

		// concatenating the styling
		final var prefix = text.stream().collect(joining());
		// reversing the style and concatenating it
		final var suffix = text.stream().collect(Collector.of(ArrayDeque<String>::new, (deq, t) -> {
			// in case of "<u>"、replace it with "</u>"
			deq.addFirst(t.contains("<u>") ? "</u>" : t);
		}, (d1, d2) -> {
			d2.addAll(d1);
			return d2;
		})).stream().collect(joining());

		// Append style and return the text
		return new Markdown(new Prefix(""), new Suffix(prefix + run.text() + suffix), MarkdownType.PLAINTEXT);
		// return prefix + run.text() + suffix;
	}

	// Handles tables
	private void processTable(XWPFTable t) {
		StringBuilder tableText = new StringBuilder();
		boolean isHeading = true;
		for (var row : t.getRows()) {
			tableText.append("|");
			for (var cell : row.getTableCells()) {
				boolean isRepeatParagraph = false;
				for (var p : cell.getParagraphs()) {
					if (isRepeatParagraph) {
						tableText.append("\n");
					}
					tableText.append(StringUtils.isBlank(p.getText()) ? "" : p.getText());
					isRepeatParagraph = true;
				}
				tableText.append("|");
			}
			tableText.append("\n");
			if (isHeading) {
				tableText.append(getTableAlignment(row) + "\n");
				isHeading = false;
			}
		}
	}

	// returns table alignment
	private String getTableAlignment(XWPFTableRow row) {
		StringBuilder alignment = new StringBuilder();
		// various markdown table alignment patterns
		final String leftAlign = "----";
		final String rightAlign = "---:";
		final String centerAlign = ":--:";

		alignment.append("|");
		for (var cell : row.getTableCells()) {
			for (var p : cell.getParagraphs()) {
				switch (p.getAlignment()) {
				case RIGHT:
				case END:
					alignment.append(rightAlign);
					break;
				case CENTER:
					alignment.append(centerAlign);
					break;
				case START:
				case BOTH:
				case DISTRIBUTE:
				case HIGH_KASHIDA:
				case LEFT:
				case LOW_KASHIDA:
				case MEDIUM_KASHIDA:
				case NUM_TAB:
				case THAI_DISTRIBUTE:
				default:
					alignment.append(leftAlign);
					break;
				}
				// it is enough to process the first paragraph only
				break;
			}
			alignment.append("|");
		}

		return alignment.toString();
	}

	@Accessors(fluent = true)
	private class ImageProperties {
		@Getter
		@Setter
		private String sizeHeight;
		@Getter
		@Setter
		private String sizeWidth;
	}
}
