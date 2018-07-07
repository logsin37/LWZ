package org.dgk.util.compress.lwz;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LWZ Util Factory 默认实现类
 * @author Dai Zong 2018年7月7日
 */
public class LwzFactoryImpl implements LwzFactory {
	
	private static final Map<LwzVersion, Class<? extends Lwz>> LWZ_VERSION_LABELS = new HashMap<>();
	private static LwzVersion LatestVersion;
	private volatile Map<LwzVersion, Lwz> lwzClinets;
	
	@Override
	public synchronized boolean registerVersion(LwzVersion version, Class<? extends Lwz> clazz) {
		if(version == null) {
			throw new NullPointerException("lwz version should not be null");
		}
		if(LWZ_VERSION_LABELS.containsKey(version)) {
			return false;
		}
		LWZ_VERSION_LABELS.put(version, clazz);
		if(LatestVersion == null) {
			LatestVersion = version;
		}else if(LatestVersion.ordinal() < version.ordinal()) {
			LatestVersion = version;
		}
		return true;
	}
	
	@Override
	public Lwz getLwzByVersion(LwzVersion version) {
		if(LatestVersion == null) {
			throw new IllegalStateException("no client is registed in this factory");
		}
		if(version == null) {
			throw new NullPointerException("lwz version should not be null");
		}
		return lazyLoad(version);
	}
	
	/**
	 * lazy load Lwz client by version
	 * @author Dai Zong
	 * 2018年7月7日
	 * 
	 * @param version
	 * @return
	 */
	private Lwz lazyLoad(LwzVersion version) {
		if(this.lwzClinets == null) {
			synchronized (this) {
				if(this.lwzClinets == null) {
					this.lwzClinets = new ConcurrentHashMap<>(8);
				}
			}
		}
		Lwz lwzClient = this.lwzClinets.get(version);
		if(lwzClient == null) {
			synchronized (this) {
				lwzClient = this.lwzClinets.get(version);
				if(lwzClient == null) {
					Class<? extends Lwz> clazz = LWZ_VERSION_LABELS.get(version);
					if(clazz == null) {
						throw new IllegalArgumentException("version " + version.name() + " is not registed");
					}
					try {
						lwzClient = clazz.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return lwzClient;
		
	}
	
	@Override
	public Lwz getInstance() {
		return this.lazyLoad(LatestVersion);
	}
	
}
