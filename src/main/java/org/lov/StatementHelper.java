package org.lov;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Ease the creation of statements in jena
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class StatementHelper {
	private Model model;
	
	public StatementHelper(Model model) {
		this.model = model;
	}
	
	/**
	 * 
	 * @param resourceSubject
	 * @param property
	 * @param literalValue
	 * @param datatype e.g. XSDDatatype.XSDint
	 * @param lang
	 */
	public void addLiteralStatement(String resourceSubject, String property, String literalValue, RDFDatatype datatype, String lang){
		if(literalValue!=null && literalValue.length()>0){
			addLiteralStatement(ResourceFactory.createResource(resourceSubject),property,literalValue,datatype,lang);
		}
	}
	public void addLiteralStatement(Resource resourceSubject, String property, String literalValue, RDFDatatype datatype, String lang){
		if(literalValue!=null && literalValue.length()>0){
			if(datatype!=null){
				model.addLiteral(
						resourceSubject, 
						ResourceFactory.createProperty(property),
						ResourceFactory.createTypedLiteral(literalValue, datatype));
			}else{
				model.addLiteral(
						resourceSubject, 
						ResourceFactory.createProperty(property),
						((lang!=null)?ResourceFactory.createLangLiteral(literalValue, lang) : ResourceFactory.createPlainLiteral(literalValue)));
			}
		}
	}
	
	public void addResourceStatement(String resourceSubject, String property, String resourceObject){
		if(resourceObject!=null && resourceObject.length()>0){
			addResourceStatement(
					ResourceFactory.createResource(resourceSubject), 
					ResourceFactory.createProperty(property),
					ResourceFactory.createResource(resourceObject));
		}
	}
	public void addResourceStatement(String resourceSubject, String property, Resource resourceObject){
		if(resourceObject!=null){
			addResourceStatement(
					ResourceFactory.createResource(resourceSubject), 
					ResourceFactory.createProperty(property),
					resourceObject);
		}
	}
	public void addResourceStatement(Resource resourceSubject, String property, String resourceObject){
		if(resourceObject!=null){
			addResourceStatement(
					resourceSubject, 
					ResourceFactory.createProperty(property),
					ResourceFactory.createResource(resourceObject));
		}
	}
	public void addResourceStatement(Resource resourceSubject, Property property, Resource resourceObject){
		if(resourceObject!=null){
			model.add(resourceSubject, property,resourceObject);
		}
	}
	
}
