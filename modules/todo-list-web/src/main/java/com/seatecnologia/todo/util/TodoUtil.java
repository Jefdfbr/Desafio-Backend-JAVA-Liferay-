package com.seatecnologia.todo.util;

import java.io.File;
import java.io.FileInputStream;

/**
 * Utilitarios do Todo List — metodos puramente funcionais, sem dependencia do Liferay.
 * Extraidos para facilitar testes unitarios.
 */
public class TodoUtil {

	/**
	 * Valida magic bytes de arquivo de imagem.
	 * Suporta: JPEG, PNG, GIF.
	 */
	public static boolean isValidImageMagic(File file) {
		if (file == null || !file.exists()) return false;
		byte[] magic = new byte[4];
		try (FileInputStream fis = new FileInputStream(file)) {
			if (fis.read(magic) < 3) return false;
		} catch (Exception e) {
			return false;
		}
		// JPEG: FF D8 FF
		if ((magic[0] & 0xFF) == 0xFF && (magic[1] & 0xFF) == 0xD8 && (magic[2] & 0xFF) == 0xFF) return true;
		// PNG: 89 50 4E 47
		if ((magic[0] & 0xFF) == 0x89 && (magic[1] & 0xFF) == 0x50 && (magic[2] & 0xFF) == 0x4E && (magic[3] & 0xFF) == 0x47) return true;
		// GIF: 47 49 46 38
		if ((magic[0] & 0xFF) == 0x47 && (magic[1] & 0xFF) == 0x49 && (magic[2] & 0xFF) == 0x46 && (magic[3] & 0xFF) == 0x38) return true;
		return false;
	}

	/**
	 * Sanitiza string: remove caracteres de controle (exceto \r\n\t), trim, limita tamanho.
	 */
	public static String sanitize(String input, int maxLength) {
		if (input == null) return "";
		input = input.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "").trim();
		return input.length() > maxLength ? input.substring(0, maxLength) : input;
	}
}
