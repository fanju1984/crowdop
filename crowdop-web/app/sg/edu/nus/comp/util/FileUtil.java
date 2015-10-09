package sg.edu.nus.comp.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Returns the content of a file as string
 * 
 * @param path
 *            the file path
 * 
 * @return the file content as string
 * 
 * @throws Exception
 */
public class FileUtil {

	static public String file2String(String path) throws Exception {
		File file = new File(path);
		FileReader fileReader = new FileReader(file);
		char[] caContent = new char[(int) file.length()];
		fileReader.read(caContent);

		String strContent = new String(caContent);
		fileReader.close();
		
		// ��ֹ��ASCII�ַ��ĸ��ţ��ý���ַ���ĩβ����һЩ'\0'�ַ���
		int first0index = strContent.indexOf(0); 
		if(first0index != -1) {
			return strContent.substring(0, first0index);
		}
		return strContent;
	}

	
	/**
	 * Returns the content of a file as string. The character encoding can be
	 * defined
	 * 
	 * @param path
	 *            the file path
	 * 
	 * @return the file content as string
	 * 
	 * @throws Exception
	 */
	static public String file2String(String path, String charEncoding)
			throws Exception {

		File file = new File(path);
		FileInputStream fileInputStream = new FileInputStream(file);

		Charset charset = Charset.forName(charEncoding);
		InputStreamReader inputStreamReader = new InputStreamReader(
				fileInputStream, charset);
		char[] caContent = new char[(int) file.length()];
		inputStreamReader.read(caContent);

		String strContent = new String(caContent);

		inputStreamReader.close();
		fileInputStream.close();

		// ��ֹ��ASCII�ַ��ĸ��ţ��ý���ַ���ĩβ����һЩ'\0'�ַ���
		int first0index = strContent.indexOf(0); 
		if(first0index != -1) {
			return strContent.substring(0, first0index);
		}
		return strContent;
	}
	
	/**
	 * Writes a string into a file
	 * 
	 * @param strContent
	 *            the new file content as String
	 * @param strFileName
	 *            the path of the (new) file
	 * 
	 * @throws Exception
	 */
	static public void string2File(String strContent, String strFileName)
			throws Exception {
		FileWriter fileWriter = new FileWriter(strFileName);

		fileWriter.write(strContent);
		fileWriter.flush();
		fileWriter.close();
	}

}