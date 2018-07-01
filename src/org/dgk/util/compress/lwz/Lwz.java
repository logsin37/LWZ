package org.dgk.util.compress.lwz;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dgk.util.compress.lwz.Lwz.LwzDictionary.DictionaryPage;

/**
 * LWZ compress/decompress util
 * 
 * @author Dai Zong 2018年7月1日
 */
public interface Lwz {
	
	/**
	 * 根据给定数据源进行压缩
	 * <br/>
	 * <br/>
	 * <b>LWZ压缩算法:</b><br/>
	 * 1. 初始状态，字典里只有所有的默认项，例如0->a，1->b，2->c。此时P和C都是空的。<br/>
	 * 2. 读入新的字符C，与P合并形成字符串P+C。<br/>
	 * 3. 在字典里查找P+C，如果:<br/>
	 * &emsp;&emsp;a. P+C在字典里，P=P+C。<br/>
	 * &emsp;&emsp;b. P+C不在字典里，将P的记号输出；在字典中为P+C建立一个记号映射；更新P=C。<br/>
	 * 4. 返回步骤2重复，直至读完原字符串中所有字符。<br/>
	 * 
	 * @author Dai Zong
	 * @param src 数据源
	 * @return 索引集
	 */
	public int[] encode(byte[] src);
	
	/**
	 * encode a file
	 * 
	 * @author Dai Zong
	 * @param file 数据源
	 * @return 索引集
	 * @throws IOException 
	 * @see {@link Lwz#encode(byte[])}
	 */
	public int[] encode(File file) throws IOException;
	
	/**
	 * encode a file to destination folder
	 * 
	 * @author Dai Zong
	 * @param srcFilePath 源文件路径
	 * @param destFolderPath 输出目标文件夹路径
	 * @throws IOException 
	 * @see {@link Lwz#encode(byte[])}
	 */
	public void encode(String srcFilePath, String destFolderPath) throws IOException;
	
	/**
	 * 根据给定索引集进行解压缩
	 * <br/>
	 * <br/>
	 * <b>LWZ解压算法:</b><br/>
	 * 1. 初始状态，字典里只有所有的默认项，例如0->a，1->b，2->c。此时pW和cW都是空的。<br/>
	 * 2. 读入第一个的符号cW，解码输出。注意第一个cW肯定是能直接解码的，而且一定是单个字符。<br/>
	 * 3. 赋值pW=cW。<br/>
	 * 4. 读入下一个符号cW。<br/>
	 * 5. 在字典里查找cW，如果:<br/>
	 * &emsp;&emsp;a. cW在字典里：<br/>
	 * &emsp;&emsp;&emsp;&emsp;(1) 解码cW，即输出 Str(cW)。<br/>
	 * &emsp;&emsp;&emsp;&emsp;(2) 令P=Str(pW)，C=Str(cW)的**第一个字符**。<br/>
	 * &emsp;&emsp;&emsp;&emsp;(3) 在字典中为P+C添加新的记号映射。<br/>
	 * &emsp;&emsp;b. cW不在字典里:<br/>
	 * &emsp;&emsp;&emsp;&emsp;(1) 令P=Str(pW)，C=Str(pW)的**第一个字符**。<br/>
	 * &emsp;&emsp;&emsp;&emsp;(2) 在字典中为P+C添加新的记号映射，这个新的记号一定就是cW。<br/>
	 * &emsp;&emsp;&emsp;&emsp;(3) 输出P+C。<br/>
	 * 6. 返回步骤3重复，直至读完所有记号。<br/>
	 * 
	 * @author Dai Zong
	 * @param indexs 索引集
	 * @return 原文
	 */
	public byte[] decode(int[] indexs);
	
	/**
	 * decode a file
	 * 
	 * @author Dai Zong
	 * @param file 索引集
	 * @return 原文
	 * @throws IOException 
	 * @see {@link Lwz#decode(byte[])}
	 */
	public byte[] decode(File file) throws IOException;
	
	/**
	 * decode a file to destination folder
	 * 
	 * @author Dai Zong
	 * @param srcFilePath 源文件路径
	 * @param destFolderPath 输出目标文件夹路径
	 * @throws IOException 
	 * @see {@link Lwz#decode(int[])}
	 */
	public void decode(String srcFilePath, String destFolderPath) throws IOException;
	
	/**
	 * LWZ Dictionary
	 * 
	 * @author Dai Zong 2018年7月1日
	 */
	interface LwzDictionary extends List<DictionaryPage> {
		
		/**
		 * <p>get meaning by a index</p>
		 * <p>index must lease than dictionary's size</p>
		 * 
		 * @author Dai Zong
		 * @param index index
		 * @return meaning
		 */
		byte[] getMeaningByIndex(int index);

		/**
		 * <p>get index by a meaning</p>
		 * <p>if this dictionary contains this meaning, return its index</p>
		 * <p>else return -1</p>
		 * 
		 * @author Dai Zong
		 * @param meaning meaning
		 * @return index
		 */
		int getIndexByMeaning(byte[] meaning);

		/**
		 * check if a meaing is contained by this dictionary
		 * 
		 * @author Dai Zong
		 * @param meaning meaning
		 * @return if is contained
		 */
		boolean containsMeaning(byte[] meaning);

		/**
		 * add a DictionaryPair at the last of this dictionary
		 * 
		 * @author Dai Zong
		 * @param meaning DictionaryPair meaning
		 * @return if add successed
		 */
		boolean add(byte[] meaning);
		
		/**
		 * Dictionary Page in LWZ Dictionary
		 * 
		 * @author Dai Zong 2018年7月1日
		 */
		interface DictionaryPage{
			
			/**
			 * get this page's index
			 * 
			 * @author Dai Zong
			 * @return lwz index
			 */
			public int getIndex();

			/**
			 * get this page's data
			 * 
			 * @author Dai Zong
			 * @return lwz meaning
			 */
			public byte[] getMeaning();
			
			/**
			 * decide if this pair's index is equal to a specific index
			 * 
			 * @author Dai Zong
			 * @param index a specific index
			 * @return if index is equal
			 */
			boolean indexEquals(int index);
			
			/**
			 * decide if this pair's meaning is equal to a specific meaning
			 * 
			 * @author Dai Zong
			 * @param meaning index a specific meaning
			 * @return if meaning is equal
			 */
			boolean meaningEquals(byte[] meaning);
			
		}

	}
	
}
