package com.seatecnologia.todo.util;

import org.junit.jupiter.api.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitarios para TodoUtil — funcoes puras, sem dependencia do Liferay.
 */
class TodoUtilTest {

	// ── sanitize ────────────────────────────────────────────────────────────

	@Test
	void testSanitize_Null_ReturnsEmpty() {
		assertEquals("", TodoUtil.sanitize(null, 100));
	}

	@Test
	void testSanitize_TrimAndRemoveControlChars() {
		// \u0000 removed; tab/CrLf at end also removed by String.trim()
		assertEquals("Hello World",
			TodoUtil.sanitize("  Hello \u0000World\t\r\n  ", 100));
	}

	@Test
	void testSanitize_PreserveCRLFInMiddle() {
		// Tab + CrLf in the middle of text are preserved
		assertEquals("Hello\t\r\nWorld",
			TodoUtil.sanitize("Hello\t\r\nWorld", 100));
	}

	@Test
	void testSanitize_TruncateOverLimit() {
		String input = "ABCDEFGHIJ";
		String result = TodoUtil.sanitize(input, 5);
		assertEquals("ABCDE", result);
	}

	@Test
	void testSanitize_ExactlyAtLimit() {
		String input = "ABC";
		String result = TodoUtil.sanitize(input, 3);
		assertEquals("ABC", result);
	}

	@Test
	void testSanitize_LongTitle_TitleField() {
		String input = "A".repeat(250);
		String result = TodoUtil.sanitize(input, 200);
		assertEquals(200, result.length());
	}

	// ── isValidImageMagic ─────────────────────────────────────────────────────

	@Test
	void testIsValidImageMagic_Null_ReturnsFalse() {
		assertFalse(TodoUtil.isValidImageMagic(null));
	}

	@Test
	void testIsValidImageMagic_NonExistent_ReturnsFalse() {
		assertFalse(TodoUtil.isValidImageMagic(new File("/tmp/nonexistent.jpg")));
	}

	@Test
	void testIsValidImageMagic_JPEG_Pass() throws IOException {
		File f = tempFile(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00});
		assertTrue(TodoUtil.isValidImageMagic(f));
		f.delete();
	}

	@Test
	void testIsValidImageMagic_PNG_Pass() throws IOException {
		File f = tempFile(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});
		assertTrue(TodoUtil.isValidImageMagic(f));
		f.delete();
	}

	@Test
	void testIsValidImageMagic_GIF_Pass() throws IOException {
		File f = tempFile(new byte[]{0x47, 0x49, 0x46, 0x38});
		assertTrue(TodoUtil.isValidImageMagic(f));
		f.delete();
	}

	@Test
	void testIsValidImageMagic_Only2Bytes_False() throws IOException {
		File f = tempFile(new byte[]{0x01, 0x02});
		assertFalse(TodoUtil.isValidImageMagic(f));
		f.delete();
	}

	@Test
	void testIsValidImageMagic_TextFile_False() throws IOException {
		File f = tempFile(new byte[]{0x00, 0x00, 0x00, 0x00}); // null bytes
		assertFalse(TodoUtil.isValidImageMagic(f));
		f.delete();
	}

	// ── helpers ───────────────────────────────────────────────────────────────

	private File tempFile(byte[] content) throws IOException {
		File f = File.createTempFile("test-", ".bin");
		try (FileOutputStream fos = new FileOutputStream(f)) {
			fos.write(content);
		}
		return f;
	}
}
