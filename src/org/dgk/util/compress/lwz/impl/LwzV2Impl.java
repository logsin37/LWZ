package org.dgk.util.compress.lwz.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.dgk.util.compress.lwz.Lwz;
import org.dgk.util.compress.lwz.io.ComposedDataInputStream;
import org.dgk.util.compress.lwz.io.ComposedDataOutputStream;

/**
 * LWZ compress/decompress util default implement
 * 
 * @author Dai Zong 2018年7月1日
 */
public class LwzV2Impl extends LwzImpl implements Lwz {
	
	public static final byte VERSION = (byte) 2;
	
	@Override
	public void encode(String srcFilePath, String destFolderPath) throws IOException {
		if(srcFilePath == null || srcFilePath.length() == 0) {
			throw new IllegalArgumentException("src file path should not be null");
		}
		if(destFolderPath == null || destFolderPath.length() == 0) {
			throw new IllegalArgumentException("dest folder path should not be null");
		}
		File srcFile = new File(srcFilePath);
		if(!srcFile.isFile()) {
			throw new IllegalArgumentException("src is not a file");
		}
		int[] indexs = this.encode(srcFile);
		String srcFileName = srcFile.getName();
		String destFileName = srcFileName.split("\\.")[0] + ".lwz2";
		
		try(ComposedDataOutputStream outputStream = new ComposedDataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(destFolderPath + File.separator + destFileName))))) {
			// first byte : version
			outputStream.write(VERSION);
			byte[] srcFileNameBytes = srcFileName.getBytes(CHAR_SET);
			// next four bytes : src file name length
			outputStream.writeInt(srcFileNameBytes.length);
			// next srcFileNameBytes.length bytes : src file name
			outputStream.write(srcFileNameBytes);
			// next four bytes : data length
			outputStream.writeInt(indexs.length * 4);
			// other bytes : data
			for(int index : indexs) {
				outputStream.writeInt(index);
			}
		}
	}
	
	@Override
	public void decode(String srcFilePath, String destFolderPath) throws IOException {
		if(srcFilePath == null || srcFilePath.length() == 0) {
			throw new IllegalArgumentException("src file path should not be null");
		}
		if(destFolderPath == null || destFolderPath.length() == 0) {
			throw new IllegalArgumentException("dest folder path should not be null");
		}
		File srcFile = new File(srcFilePath);
		if(!srcFile.isFile()) {
			throw new IllegalArgumentException("src is not a file");
		}
		
		OutputStream outputStream = null;
		try(ComposedDataInputStream inputStream = new ComposedDataInputStream(new BufferedInputStream(new FileInputStream(srcFile)))) {
			// first byte : version
			byte version = inputStream.readByte();
			if(version != VERSION) {
				throw new IllegalArgumentException("file version error: need " + VERSION + " , get " + version);
			}
			// next four bytes : src file name length
			int originFileNameLength = inputStream.readInt();
			// next srcFileNameBytes.length bytes : src file name
			byte[] originFileNameBytes = new byte[originFileNameLength];
			inputStream.read(originFileNameBytes);
			String originFileName = new String(originFileNameBytes, CHAR_SET);
			// next four bytes : data length
			int dataLength = inputStream.readInt();
			// other bytes : data
			int[] indexs = new int[dataLength / 4];
			for(int i=0 ; i<indexs.length ; i++) {
				indexs[i] = inputStream.readInt();
			}
			byte[] decodedBytes = this.decode(indexs);
			outputStream = new BufferedOutputStream(new FileOutputStream(new File(destFolderPath + File.separator + originFileName)));
			outputStream.write(decodedBytes);
		}finally {
			if(outputStream != null) {
				outputStream.close();
			}
		}
	}
	
}

