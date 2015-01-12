package org.lov.objects;

import java.io.Serializable;

/**
 * Represents statistics associated to a Language
 * 
 * @author Pierre-Yves Vandenbussche
 *
 */
public class StatLang implements Serializable{

	private static final long serialVersionUID = 4832292865404789112L;
	private String label;
	int nbOccurrences=0;
	
	public StatLang(){super();}
	
	public StatLang(String label){
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
        if(o instanceof StatLang){
        	StatLang toCompare = (StatLang) o;
            return label.equals(toCompare.getLabel());
        }
        return false;
    }
}
