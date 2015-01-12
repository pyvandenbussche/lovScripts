package org.lov.objects;

import java.io.Serializable;

/**
 * Represents statistics associated to a Tag
 * 
 * @author Pierre-Yves Vandenbussche
 *
 */
public class StatTag implements Serializable{

	private static final long serialVersionUID = 5590977759956582738L;
	private String label;
	int nbOccurrences=0;
	
	public StatTag(){super();}
	
	public StatTag(String label){
		super();
		this.label=label;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getNbOccurrences() {
		return nbOccurrences;
	}

	public void setNbOccurrences(int nbOccurrences) {
		this.nbOccurrences = nbOccurrences;
	}

	public void addOccurrence(){
		nbOccurrences++;
	}
	
	@Override
    public boolean equals(Object o){
        if(o instanceof StatTag){
        	StatTag toCompare = (StatTag) o;
            return label.equals(toCompare.getLabel());
        }
        return false;
    }
}
