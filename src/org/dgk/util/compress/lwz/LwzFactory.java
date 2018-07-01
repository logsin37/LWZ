package org.dgk.util.compress.lwz;

/**
 * LWZ Util Factory
 * @author Dai Zong 2018年7月1日
 */
public class LwzFactory {
	
	private static final Lwz INSTANCE = new LwzImpl();
	
	/**
	 * get a singleton instance LWZ Util
	 * 
	 * @author Dai Zong
	 * @return LWZ Util
	 */
	public static Lwz getInstance() {
		return INSTANCE;
	}

}
