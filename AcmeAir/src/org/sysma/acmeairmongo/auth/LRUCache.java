package org.sysma.acmeairmongo.auth;

import java.util.HashMap;
import java.util.function.BiFunction;

import org.sysma.schedulerExecutor.Communication;

public class LRUCache<K,V> {
	private class Node {
		public final K key;
		public final V val;
		public Node fresher;
		public Node staler;
		public Node(K key, V val, LRUCache<K, V>.Node fresher, LRUCache<K, V>.Node staler) {
			super();
			this.key = key;
			this.val = val;
			this.fresher = fresher;
			this.staler = staler;
		}
	}
	
	private final int size;
	private final HashMap<K, Node> cachePtr;
	private Node fresh = null;
	private Node stale = null;
	private final BiFunction<K,Communication,V> function;
	
	public LRUCache(int size, BiFunction<K,Communication,V> function) {
		super();
		this.size = size;
		this.cachePtr = new HashMap<>();
		this.function = function;
	}
	
	public synchronized V query(K key, Communication comm) {
		if(cachePtr.containsKey(key)) {
			Node nd = cachePtr.get(key);
			if(nd.fresher != null)
				nd.fresher.staler = nd.staler;
			if(nd.staler != null)
				nd.staler.fresher = nd.fresher;
			nd.fresher = null;
			nd.staler = fresh;
			fresh = nd;
			return nd.val;
		}
		V ans = function.apply(key, comm);
		if(size == 0)
			return ans;
		
		if(cachePtr.size() >= size) {
			cachePtr.remove(stale.key);
			stale = stale.fresher;
			stale.staler = null;
		}
		Node nAns = new Node(key, ans, null, fresh);
		if(fresh != null) {
			fresh.fresher = nAns;
		}
		fresh = nAns;
		if(stale == null) {
			stale = nAns;
		}
		return ans;
	}
}
