package org.dgk.util.compress.lwz.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.dgk.util.compress.lwz.Lwz;
import org.dgk.util.compress.lwz.Lwz.LwzDictionary;
import org.dgk.util.compress.lwz.Lwz.LwzDictionary.DictionaryPage;

/**
 * LWZ compress/decompress util default implement
 * 
 * @author Dai Zong 2018年7月1日
 */
public class LwzImpl implements Lwz {
	
	public static final byte VERSION = (byte) 1;
	
	public static final Charset CHAR_SET = Charset.forName("UTF-8");
	
	/**
	 * original dictionary (byte 0~255)
	 */
	private static final LwzDictionary  ORIGINAL_DICT;
	private static final int TWO = 2;
	
	/**
	 * inti original dictionary
	 */
	static {
		ORIGINAL_DICT = new LwzDictionaryImpl();
		byte temp;
		byte[] tempArr;
		for(int i=0 ; i<=Constants.MAX_UNSIGND_BYTE ; i++) {
			temp = (byte)i;
			tempArr = new byte[1];
			tempArr[0] = temp;
			ORIGINAL_DICT.add(new DictionaryPageImpl(i, tempArr));
		}
	}

	@Override
	public int[] encode(byte[] src) {
		if(src == null || src.length == 0) {
			throw new IllegalArgumentException("empty source");
		}
		long startTime = System.nanoTime();
		int[] res = new int[512];
		int ptr = 0;
		int percentCount = src.length / 100 ;
		// step 1
		LwzDictionary dict = new LwzDictionaryImpl(ORIGINAL_DICT);
		byte[] p = new byte[0],c = new byte[0];
		byte[] pc;
		int i = 0,ii;
		while(i < src.length) {
			if(i % percentCount == 0) {
				System.out.println("encode processing... " + String.valueOf((i / percentCount) > 100 ? 100 : (i / percentCount)) + '%');
			}
			// step 2
			c = new byte[] {src[i]};
			pc = this.byteArrayJoin(p, c);
			// step 3
			if(dict.containsMeaning(pc)) {
				// step 3.q
				p = pc;
			}else {
				ii = dict.getIndexByMeaning(p);
				// step 3.b
				res = this.intArrayAdd(res, ptr++, ii);
				dict.add(pc);
				p = c;
			}
			if(i == src.length - 1) {
				res = this.intArrayAdd(res, ptr++, dict.getIndexByMeaning(p));
			}
			i++;
			// step 4
		}
		
		int[] accurateSizeArray = new int[ptr];
		System.arraycopy(res, 0, accurateSizeArray, 0, ptr);
		long endTime = System.nanoTime();
		System.out.println("\ncompress rate : " + ((double)accurateSizeArray.length/src.length) * 100  + "%");
		System.out.println("cost " + (endTime - startTime)/ 10E6 + "ms\n");
		return accurateSizeArray;
	}
	
	@Override
	public byte[] decode(int[] indexs) {
		if(indexs == null || indexs.length == 0) {
			throw new IllegalArgumentException("empty indexs");
		}
		long startTime = System.nanoTime();
		byte[] res = new byte[indexs.length * 2];
		int ptr = 0;
		byte[] p = new byte[0],c = new byte[0];
		int pw = 0,cw = 0;
		int percentCount = indexs.length / 100 ;
		byte[] t;
		// step 1
		LwzDictionary dict = new LwzDictionaryImpl(ORIGINAL_DICT);
		int i = 0;
		// step 2
		cw = indexs[i++];
		res = this.byteArrayAdd(res, ptr++, dict.getMeaningByIndex(cw));
		// step 3
		pw = cw;
		while(i < indexs.length) {
			if(i % percentCount == 0) {
				System.out.println("decode processing... " + String.valueOf((i / percentCount) > 100 ? 100 : (i / percentCount)) + '%');
			}
			// step 4
			cw = indexs[i++];
			// step 5
			if(cw < dict.size()) {
				// step 5.a
				t = dict.getMeaningByIndex(cw);
				res = this.byteArrayAdd(res, ptr, t);
				ptr+=t.length;
				p = dict.getMeaningByIndex(pw);
				c = new byte[1];
				c[0] = t[0];
				dict.add(this.byteArrayJoin(p, c));
			}else {
				// step 5.b
				p = dict.getMeaningByIndex(pw);
				c = new byte[1];
				c[0] = p[0];
				t = this.byteArrayJoin(p, c);
				cw = dict.size();
				res = this.byteArrayAdd(res, ptr, t);
				ptr+=t.length;
				dict.add(new DictionaryPageImpl(cw, t));
			}
			// step 6
			pw = cw;
		}
		
		byte[] accurateSizeArray = new byte[ptr];
		System.arraycopy(res, 0, accurateSizeArray, 0, ptr);
		long endTime = System.nanoTime();
		System.out.println("\ncost " + (endTime - startTime)/ 10E6 + "ms\n");
		return accurateSizeArray;
	}
	
	@Override
	public int[] encode(File file) throws IOException {
		if(file == null) {
			throw new NullPointerException("file is null");
		}
		if(file.length() > Integer.MAX_VALUE) {
			throw new ArrayIndexOutOfBoundsException("file is too large");
		}
		byte[] bytes = new byte[(int) file.length()];
		try(InputStream inputStream = new FileInputStream(file)) {
			inputStream.read(bytes);
			return this.encode(bytes);
		} catch (IOException e) {
			System.out.println("oops , file error : " + e.getMessage());
			throw e;
		}
	}

	@Override
	public byte[] decode(File file) throws IOException {
		if(file == null) {
			throw new NullPointerException("file is null");
		}
		if(file.length() > Integer.MAX_VALUE / TWO) {
			throw new ArrayIndexOutOfBoundsException("file is too large");
		}
		
		try(DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			// first byte : version
			byte version = inputStream.readByte();
			if(version != VERSION) {
				throw new IllegalArgumentException("file version error: need " + VERSION + " , get " + version);
			}
			// next four bytes : src file name length
			int originFileNameLength = inputStream.readInt();
			// next srcFileNameBytes.length bytes : src file name
			inputStream.skipBytes(originFileNameLength);
			// next four bytes : data length
			int dataLength = inputStream.readInt();
			// other bytes : data
			int[] indexs = new int[dataLength / 4];
			for(int i=0 ; i<indexs.length ; i++) {
				indexs[i] = inputStream.readInt();
			}
			return this.decode(indexs);
		}
	}
	
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
		String destFileName = srcFileName.split("\\.")[0] + ".lwz1";
		
		try(DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(destFolderPath + File.separator + destFileName))))) {
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
		try(DataInputStream inputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(srcFile)))) {
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

	/**
	 * add a byte array to aimPtr in destination array<br/>
	 * if there is no space in destination array, this function while auto double the space
	 * 
	 * @author Dai Zong
	 * @param dest destination array
	 * @param aimPtr aim position
	 * @param obj add aim
	 * @return the array(may be a new one)
	 */
	private byte[] byteArrayAdd(byte[] dest, int aimPtr, byte[] obj) {
		if(aimPtr + obj.length < dest.length) {
			System.arraycopy(obj, 0, dest, aimPtr, obj.length);
			return dest;
		}else {
			byte[] res = new byte[2 * dest.length];
			System.arraycopy(dest, 0, res, 0, dest.length);
			System.arraycopy(obj, 0, res, aimPtr, obj.length);
			return res;
		}
	}

	/**
	 * connect two byte array to one
	 * 
	 * @author Dai Zong
	 * @param a one byte array
	 * @param b another byte array
	 * @return the connected result
	 */
	private byte[] byteArrayJoin(byte[] a, byte[] b) {
		byte[] res = new byte[a.length + b.length];
		System.arraycopy(a, 0, res, 0, a.length);
		System.arraycopy(b, 0, res, a.length, b.length);
		return res;
	}
	
	/**
	 * add a int to aimPtr in destination array<br/>
	 * if there is no space in destination array, this function while auto double the space
	 * 
	 * @author Dai Zong
	 * @param dest destination array
	 * @param aimPtr aim position
	 * @param obj add aim
	 * @return the array(may be a new one)
	 */
	private int[] intArrayAdd(int[] dest, int aimPtr, int obj) {
		if(aimPtr < dest.length) {
			dest[aimPtr] = obj;
			return dest;
		}else {
			int[] res = new int[2 * dest.length];
			System.arraycopy(dest, 0, res, 0, dest.length);
			res[aimPtr] = obj;
			return res;
		}
	}
	
}

/**
 * LWZ Dictionary's implement, based on {@link ArrayList}
 *
 * @author Dai Zong 2018年7月1日
 */
class LwzDictionaryImpl extends ArrayList<Lwz.LwzDictionary.DictionaryPage> implements LwzDictionary {
	private static final long serialVersionUID = 8955471333073162919L;
	
	public LwzDictionaryImpl() {
		super();
	}
	
	public LwzDictionaryImpl(LwzDictionary lwzDictionary) {
		super(lwzDictionary);
	}
	
	@Override
	public boolean add(byte[] meaning) {
		return this.add(new DictionaryPageImpl(this.size(), meaning));
	}
	
	@Override
	public boolean containsMeaning(byte[] meaning) {
		return this.getIndexByMeaning(meaning) != -1;
	}
	
	@Override
	public int getIndexByMeaning(byte[] meaning) {
		if(meaning.length < 1) {
			throw new IllegalArgumentException("meaning must contain at least 1 byte");
		}
		DictionaryPage pair;
		Iterator<DictionaryPage> iterator = this.iterator();
		while (iterator.hasNext()) {
			pair = iterator.next();
			if(pair.meaningEquals(meaning)) {
				return pair.getIndex();
			}
		}
		return -1;
	}
	
	@Override
	public byte[] getMeaningByIndex(int index) {
		if(index > this.size()) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return this.get(index).getMeaning();
	}
	
}

/**
 * Dictionary Page implement, contains a pair of index(int) and data(byte[])
 * 
 * @author Dai Zong 2018年7月1日
 */
class DictionaryPageImpl implements DictionaryPage{
	
	/**
	 * lwz index
	 */
	private int index;
	
	/**
	 * lwz meaning
	 */
	private byte[] meaning;
	
	public DictionaryPageImpl(int index, byte[] meaning) {
		if(meaning.length < 1) {
			throw new IllegalArgumentException("meaning must contain at least 1 byte");
		}
		this.index = index;
		this.meaning = meaning;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(obj instanceof DictionaryPageImpl) {
			DictionaryPageImpl pair = (DictionaryPageImpl) obj;
			return this.indexEquals(pair.index) && this.meaningEquals(pair.meaning);
		}
		return super.equals(obj);
	}

	@Override
	public int getIndex() {
		return index;
	}
	
	@Override
	public byte[] getMeaning() {
		return meaning;
	}
	
	@Override
	public int hashCode() {
		int code = this.meaning[0];
		for(int i=1 ; i< this.meaning.length ; i++) {
			code ^= this.meaning[i];
		}
		return code ^ this.index;
	}
	
	@Override
	public boolean indexEquals(int index) {
		return this.index == index;
	}
	
	@Override
	public boolean meaningEquals(byte[] meaning) {
		if(this.meaning.length != meaning.length) {
			return false;
		}
		for(int i=0 ; i<meaning.length ; i++) {
			if(this.meaning[i] != meaning[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return "DictionaryPairImpl [index=" + index + ", meaning=" + Arrays.toString(meaning) + "]";
	}
	
}
