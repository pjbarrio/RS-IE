package edu.cs.columbia.iesrcsel.execution.logger;

public abstract class LogCollisionSaver {

	public abstract void log(String header, String...values);

	public abstract void dump();

}
