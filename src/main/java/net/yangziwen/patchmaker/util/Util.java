package net.yangziwen.patchmaker.util;

public class Util {

	private Util() {}
	
	public static Counter counter() {
		return counter(0);
	}
	
	public static Counter counter(int n) {
		return new Counter(n);
	}
	
	public static class Counter {
		int cnt;
		Counter(int n) {
			this.cnt = n;
		}
		
		public int get() {
			return cnt;
		}
		
		public int getAndIncr() {
			return getAndIncr(1);
		}
		
		public int getAndIncr(int n) {
			int old = cnt;
			cnt += n;
			return old;
		}
		
		public int incrAndGet() {
			return incrAndGet(1);
		}
		
		public int incrAndGet(int n) {
			return cnt += n;
		}
	}
	
}
