package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mohammad.docxparser.docxcore.DOCXImport;

public class DOCXImportTest {
	private static DOCXImport docxImport;
	
	@BeforeAll
	public static void setUp() throws Exception{
		docxImport = new DOCXImport();
	}
	
	@Test
	@DisplayName("Simple docx import should work")
	public void textSimpleDocumentImport() {
		assertEquals("", docxImport.importDocument(null,""));
	}
	
	
	
	
}
