package edu.cs.columbia.iesrcsel.utils;

public class PairUnordered<A, B> {

	private A first;
	private B second;

	public A getFirst() {
		return first;
	}

	public B getSecond() {
		return second;
	}

	
	public PairUnordered(A first, B second){
		this.first = first;
		this.second = second;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PairUnordered){
			PairUnordered p = (PairUnordered) obj;
			return (p.first.equals(first) && p.second.equals(second))
				|| (p.first.equals(second) && p.second.equals(p.first));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return 31 * (first.hashCode() + second.hashCode()) / 2;
	}
}
