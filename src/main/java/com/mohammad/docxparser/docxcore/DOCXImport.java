package com.mohammad.docxparser.docxcore;

import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.ICell;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import markdown.Markdown;
import markdown.MarkdownType;
import markdown.Prefix;
import markdown.Suffix;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class DOCXImport {
	// private final static String UNTITLED = "無題";

	public String importDocument(InputStream stream, String docTitle) {
		try (XWPFDocument document = new XWPFDocument(stream)) {
			// handleImageElements(document);
			// elements = new ArrayList<>();
			processBodyElements(document.getBodyElements());
		} catch (IOException e) {
			// var msg = "Document couldn't be instantiated.";
			e.printStackTrace();
		}
		// return elements;
		// TODO: change returning value
		return "";
	}

	//
	private void processBodyElements(List<IBodyElement> bodyElements) {
		List<Markdown> markdown = new ArrayList<>();
		boolean isStartProcessing = false;
		for (var bodyElement : bodyElements) {
			// Looking for the first header
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

		/*
		 * if (isTableTitle(p)) { System.out.println("Table title"); }
		 */

		// processing paragraph
		List<Markdown> runs = new ArrayList<>();
		for (var r : p.getRuns()) {
			runs.addAll(processRun(r));
		}
		return runs;
	}

	/*
	 * private boolean isTableTitle(XWPFParagraph p) { // スタイルをつけてない場合、０を返す if
	 * (StringUtils.isBlank(p.getStyle())) { return false; }
	 * 
	 * // スタイル名を取得 final String styleName = p.getStyle().toLowerCase();
	 * 
	 * // スタイル名は"heading"や"見出し"なら、見出しのレベルを返す if (styleName.contains("表題") ||
	 * styleName.contains("図表番号")) { return true; }
	 * 
	 * // ドキュメントからスタイルを取得 XWPFStyle style =
	 * p.getDocument().getStyles().getStyle(styleName);
	 * 
	 * // スタイルはNullなら、０を返す if (Objects.isNull(style)) { return false; }
	 * 
	 * var pStyleName = style.getName().toLowerCase(); //
	 * スタイル名は"表題"や"図表番号"なら、trueを返す if (!StringUtils.isBlank(pStyleName) &&
	 * (pStyleName.contains("表題") || pStyleName.contains("図表番号"))) { return true; }
	 * 
	 * return false; }
	 */

	// returns the heading level
	private int getHeadingLevel(XWPFParagraph p) {
		// return 0, if style is not present
		if (StringUtils.isBlank(p.getStyle())) {
			return 0;
		}

		final String styleName = p.getStyle().toLowerCase();

		// スタイル名は"heading"や"見出し"なら、見出しのレベルを返す
		if (styleName.contains("heading") || styleName.contains("見出し")) {
			return styleName.charAt(styleName.length() - 1) - '0';
		}

		// スタイル名は数ならの対応
		// if (StringUtils.isNumeric(styleName)) {

		// ドキュメントからスタイルを取得
		XWPFStyle style = p.getDocument().getStyles().getStyle(styleName);

		// スタイルはNullなら、０を返す
		if (Objects.isNull(style)) {
			return 0;
		}

		var pStyleName = style.getName().toLowerCase();
		// スタイル名は"heading"や"見出し"なら、見出しのレベルを返す
		if (!StringUtils.isBlank(pStyleName) && (pStyleName.contains("heading") || pStyleName.contains("見出し"))) {
			return pStyleName.charAt(pStyleName.length() - 1) - '0';
		}

		return 0;
	}

	// 見出しを処理し、返す
	private Markdown processHeading(XWPFParagraph p, int lvl) {
		return new Markdown(new Prefix("#".repeat(lvl)), new Suffix(p.getText()), MarkdownType.PLAINTEXT);
		// return String.format("%s %s", "#".repeat(lvl), p.getText());
	}

	// runの処理
	private List<Markdown> processRun(XWPFRun run) {
		if (run.getEmbeddedPictures().size() > 0) {
			// return processImageRun(run);
		}
		return List.of(processTextRun(run));
	}

	private Markdown processTextRun(XWPFRun run) {
		List<String> text = new ArrayList<>();
		// ボロドーの対応
		if (run.isBold()) {
			text.add("**");
		}

		// イタリックの対応
		if (run.isItalic()) {
			text.add("*");
		}

		// 取り消し線の対応
		if (run.isStrikeThrough() || run.isDoubleStrikeThrough()) {
			text.add("~~");
		}

		// アンダーラインの対応
		if (!run.getUnderline().equals(UnderlinePatterns.NONE)) {
			text.add("<u>");
		}

		// スタイルつけてない場合
		if (text.size() == 0) {
			return new Markdown(new Prefix(""), new Suffix(run.text()), MarkdownType.PLAINTEXT);
			// return run.text();
		}

		// スタイルを連結
		final var prefix = text.stream().collect(joining());
		// スタイルを逆にし、連結
		final var suffix = text.stream().collect(Collector.of(ArrayDeque<String>::new, (deq, t) -> {
			// "<u>"の場合、"</u>"で置き換える
			deq.addFirst(t.contains("<u>") ? "</u>" : t);
		}, (d1, d2) -> {
			d2.addAll(d1);
			return d2;
		})).stream().collect(joining());

		// テキストにスタイルをつけて返す
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
				// 最初の段落だけを処理すれば良い
				break;
			}
			alignment.append("|");
		}

		return alignment.toString();
	}

	/*
	 * private List<Markdown> processImageRun(XWPFRun run){ for (var p :
	 * run.getEmbeddedPictures()) { // final Link l = new
	 * Link().title(p.getPictureData().getFileName()); createImageElement(p);
	 * run.getParent(); final Link l = new Link().title(UNTITLED); final Markdown m
	 * = new Markdown(new Prefix(""), new Suffix(SuffixType.LINK, l),
	 * MarkdownType.IMAGELINK); }
	 * 
	 * return run.getEmbeddedPictures().stream().map(p -> { // final Link l = new
	 * Link().title(p.getPictureData().getFileName()); final Link l = new
	 * Link().title(UNTITLED); final Markdown m = new Markdown(new Prefix(""), new
	 * Suffix(SuffixType.LINK, l), MarkdownType.IMAGELINK); return m;
	 * }).collect(toList()); }
	 */

	// 画像エレメントを作成
	/*
	 * private Element createImageElement(XWPFPicture p) throws AxldsErrorException,
	 * AxldsFatalException { // 空エレメントを作成 Element e = ElementModel.createElement();
	 * // 画像の拡張子を取得 final String extension =
	 * p.getPictureData().suggestFileExtension().toLowerCase(); // ソースマップを作成
	 * Map<String, String> source = new HashMap<>();
	 * 
	 * // MIMETypeを設定する e.setMimetype(getMimeType(extension)); // 要素タイプを設定する
	 * e.setElementType(getElementType(extension));
	 * 
	 * // XWPFImageオブジェクトを作成し、拡張子とデータを設定する XWPFImage image =
	 * getXWPFImageInstance(p);
	 * 
	 * return e; }
	 */

	// XWPFImageインスタンス作成し、返す
	/*
	 * private XWPFImage getXWPFImageInstance(XWPFPicture pic) throws
	 * AxldsErrorException, AxldsFatalException { // 画像の拡張子を取得 final String ext =
	 * pic.getPictureData().suggestFileExtension().toLowerCase(); //
	 * XWPFImageインスタンス化 XWPFImage image = new
	 * XWPFImage().data(Base64.getEncoder().encode(pic.getPictureData().getData()))
	 * .extension(ext);
	 * 
	 * switch (ext) { case "png": // TODO: 本当にSVGかどうか確認する break; case "emf":
	 * image.convData(new String(ImageUtils.convertEmfToSvg(image.data()))); break;
	 * case "jpeg": case "jpg": break; }
	 * 
	 * return image; }
	 */

	/*
	 * private Map<String, String> setImageSource(Element element, XWPFImage image,
	 * Map<String, String> source){ switch (element.getMimetype()) { case IMAGE_BMP:
	 * source.put("data", new String(Base64.getEncoder().encode(image.data())));
	 * break; case IMAGE_SVG_XML: if
	 * (element.getElementType().equals(ElementType.FIGURE_EMF)) {
	 * source.put("data", new String(Base64.getEncoder().encode(image.data()))); }
	 * else if (element.getElementType().equals(ElementType.FIGURE_IMAGE)) {
	 * source.put("data", image.convData()); } break; default: break; }
	 * 
	 * return source; }
	 */

	// sets the mime type of the image element
	/*
	 * private MimeType getMimeType(String extension) { switch (extension) { case
	 * "svg": case "emf": return MimeType.IMAGE_SVG_XML; case "jpeg": return
	 * MimeType.IMAGE_JPEG; case "bmp": return MimeType.IMAGE_BMP; default: return
	 * MimeType.IMAGE_PNG; } }
	 */

	/*
	 * private ElementType getElementType(String extension) { switch (extension) {
	 * case "emf": return ElementType.FIGURE_EMF; case "png": case "jpeg": case
	 * "jpg": case "svg": break; }
	 * 
	 * return ElementType.FIGURE_IMAGE; }
	 */

	/*
	 * private String getImageProperties(ImageProperties props) throws
	 * AxldsFatalException { Map<String, String> properties = new HashMap<>();
	 * properties.put("SizeHeight", props.sizeHeight()); properties.put("SizeWidth",
	 * props.sizeWidth()); return Utils.convToJSON(properties); }
	 */

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
