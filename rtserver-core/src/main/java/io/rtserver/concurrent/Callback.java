package io.rtserver.concurrent;

public interface Callback<T> {
	
	public void execute(T arg, Throwable t);
}
