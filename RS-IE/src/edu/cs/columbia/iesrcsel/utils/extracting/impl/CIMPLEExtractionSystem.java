package edu.cs.columbia.iesrcsel.utils.extracting.impl;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import edu.cs.columbia.iesrcsel.model.impl.Tuple;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

import prototype.CIMPLE.execution.OperatorNode;

public class CIMPLEExtractionSystem {
	private OperatorNode executionPlan;
	
	public CIMPLEExtractionSystem(String executionPlan) throws IllegalArgumentException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException{
		this.executionPlan = (OperatorNode) SerializationHelper.deserialize(executionPlan);
	}

	public List<Tuple> extractTuplesFrom(String docContent) {
		executionPlan.resetNode();
		
		List<prototype.CIMPLE.datamodel.Tuple> resultsCIMPLE = executionPlan.execute();
		List<Tuple> results = new ArrayList<Tuple>();
		for(prototype.CIMPLE.datamodel.Tuple t : resultsCIMPLE){
			Tuple newTuple = new Tuple();
			for(int i=0;i<t.getSize();i++){
				prototype.CIMPLE.datamodel.Span cimpleSpan = (prototype.CIMPLE.datamodel.Span)t.getData(i);
				newTuple.addFieldValue("attribute" + i, cimpleSpan.getValue());
			}
			results.add(newTuple);
		}
		return results;
	}

	public String getPlanString() {
		return executionPlan.toString();
	}

}
