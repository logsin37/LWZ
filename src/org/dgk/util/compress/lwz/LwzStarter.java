package org.dgk.util.compress.lwz;

import java.io.IOException;

/**
 * LWZ Util Starter
 * 
 * @author Dai Zong 2018年7月1日
 */
public class LwzStarter {
	
	private static final String MODE_ENCODE = "e";
	private static final String MODE_DECODE = "d";
	private static final String MODE_HELP = "h";
	
	public static void main(String[] args) throws IOException {
		
		if(args == null || args.length < 1) {
			throw new IllegalArgumentException("wrong arguments count, user java -jar LwzStarter.jar h to get help info");
		}
		switch (args[0]) {
			case MODE_HELP: {
				printHelpInfo();
				break;
			}
			case MODE_ENCODE: {
				if(args == null || args.length < 3) {
					throw new IllegalArgumentException("wrong arguments count, user java -jar LwzStarter.jar h to get help info");
				}
				LwzFactory.getInstance().encode(args[1], args[2]);
				break;
			}
			case MODE_DECODE: {
				if(args == null || args.length < 3) {
					throw new IllegalArgumentException("wrong arguments count, user java -jar LwzStarter.jar h to get help info");
				}
				LwzFactory.getInstance().decode(args[1], args[2]);
				break;
			}
			default:{
				throw new IllegalArgumentException("wrong mode code, user java -jar LwzStarter.jar h to get help info");
			}
		}
	}
	
	public static void printHelpInfo() {
		System.out.println("this is a java program to compress or decompression data with lwz algorithm");
		System.out.println("user java -jar LwzStarter.jar h to get help info");
		System.out.println("user java -jar LwzStarter.jar e srcFilePath destFolderPath to compress source file's data to destination folder, the destination file's name is same as source file and suffix with .lwz1");
		System.out.println("user java -jar LwzStarter.jar d srcFilePath destFolderPath to decompression source file's data to destination folder");
	}
	
}
