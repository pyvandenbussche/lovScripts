package org.lov.dif.describers;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class ResourceChangeDescriber implements Comparable<ResourceChangeDescriber> {
	
	private Resource res = null;
	private List<Statement> poAddList = new ArrayList<Statement>();
	private List<Statement> poDeleteList = new ArrayList<Statement>();
	
	private List<Statement> spAddList = new ArrayList<Statement>();
	private List<Statement> spDeleteList = new ArrayList<Statement>();
	
	public ResourceChangeDescriber(Resource res){
		this.res=res;
	}

	public List<Statement> getPoAddList() {
		return poAddList;
	}

	public void setPoAddList(List<Statement> poAddList) {
		this.poAddList = poAddList;
	}

	public List<Statement> getPoDeleteList() {
		return poDeleteList;
	}

	public void setPoDeleteList(List<Statement> poDeleteList) {
		this.poDeleteList = poDeleteList;
	}

	public List<Statement> getSpAddList() {
		return spAddList;
	}

	public void setSpAddList(List<Statement> spAddList) {
		this.spAddList = spAddList;
	}

	public List<Statement> getSpDeleteList() {
		return spDeleteList;
	}

	public void setSpDeleteList(List<Statement> spDeleteList) {
		this.spDeleteList = spDeleteList;
	}

	public Resource getRes() {
		return res;
	}





	@Override
	public int compareTo(ResourceChangeDescriber o) {
		if(res!=null){
			return res.toString().compareTo(o.getRes().toString());
		}
		else return -1;
	}
	@Override
    public boolean equals(Object o){
        if(o instanceof ResourceChangeDescriber){
            return this.res.toString().equals( ((ResourceChangeDescriber)o).getRes().toString() );
        }
        return false;
    }


}
