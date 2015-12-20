package edu.cs.columbia.iesrcsel.execution.logger.impl;

import java.io.PrintStream;

import edu.cs.columbia.iesrcsel.execution.logger.LogCollisionSaver;

public class PrintStreamCollisionLogger extends LogCollisionSaver{

	private PrintStream out;
	private StringBuilder sb;

	public PrintStreamCollisionLogger(PrintStream out) {
		this.out = out;
		sb = new StringBuilder();
	}

	@Override
	public void log(String header, String... values) {
		
		out.append(header);
		
		for (int i = 0; i < values.length; i++) {
			
			out.append("," + values[i]);
			
		}
		
		out.append("\n");
		
	}

	@Override
	public void dump() {
		out.print(sb.toString());
		sb.delete(0, sb.length());
	}

}
