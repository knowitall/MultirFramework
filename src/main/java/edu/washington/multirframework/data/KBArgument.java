package edu.washington.multirframework.data;

/**
 * Class that represents and Argument with
 * a KnowledgeBase id
 * @author jgilme1
 *
 */
public class KBArgument extends Argument {
	private String kbid;
	
	public String getKbId(){return kbid;}
	
	public KBArgument(Argument arg, String kbid){
		super(arg);
		this.kbid = kbid;
	}
}
