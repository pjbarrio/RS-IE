package edu.cs.columbia.iesrcsel.model;

import java.util.List;
import java.util.Set;

import Jama.Matrix;

import edu.cs.columbia.iesrcsel.model.impl.Category;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public class DatabasesHierarchy {

	public Category getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isLeaf(Category c) {
		// TODO Auto-generated method stub
		return false;
	}

	public Set<Category> getSubcategories(Category c) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Query> getQueries(Category category) {
		// TODO Auto-generated method stub
		return null;
	}

	public Matrix getInvertedConfusionMatrix(Category c) {
		// TODO The inverse of double[][] m2 = {{0.6,0.04,0.125},{0.10,0.8,0.09375},{0.05,0.08,0.625}}; 
		return null;
	}

	public List<TextCollection> getDatabasesExhaustive(Category category) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Category> getHigherCategories(TextCollection textCollection) {
		// TODO return just a list. Databases that are classified under multiple categories are different textCollections. (see footnote)

		return null;
	}

	//Includes the dummy class
	
}
