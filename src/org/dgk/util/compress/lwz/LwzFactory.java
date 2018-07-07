package org.dgk.util.compress.lwz;

/**
 * Lwz抽象工厂
 * @author Dai Zong 2018年7月7日
 *
 */
public interface LwzFactory {
	
	/**
	 * register a Lwz implement
	 * @author Dai Zong
	 * 2018年7月7日
	 * 
	 * @param version version
	 * @param clazz Lwz implement class
	 * @return success flag;
	 */
	boolean registerVersion(LwzVersion version, Class<? extends Lwz> clazz);
	
	/**
	 * get Lwz client by version
	 * @author Dai Zong
	 * 2018年7月7日
	 * 
	 * @param version
	 * @return
	 */
	public Lwz getLwzByVersion(LwzVersion version);
	
	/**
	 * get Lwz client by latest version
	 * @author Dai Zong
	 * 2018年7月7日
	 * 
	 * @return
	 */
	public Lwz getInstance();
	
	/**
	 * Lwz版本
	 * @author Dai Zong 2018年7月7日
	 *
	 */
	enum LwzVersion{
		/**
		 * 版本1
		 */
		V1(1),
		/**
		 * 版本2
		 */
		V2(2);
		private LwzVersion(int version) {
			this.version = version;
		}
		private int version;
		
		public int getVersion() {
			return version;
		}
	}

}
