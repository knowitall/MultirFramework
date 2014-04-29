package edu.washington.multirframework.corpus;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class SentOffsetInformation implements SentInformationI{

	public static final class SentStartOffset implements CoreAnnotation<Integer>{
		@Override
		public Class<Integer> getType() {
			return Integer.class;
		}
	}
	
	public static final class SentEndOffset implements CoreAnnotation<Integer>{
		@Override
		public Class<Integer> getType() {
			return Integer.class;
		}
	}
	
	@Override
	public void read(String s, CoreMap c) {
		String[] values = s.split("\\s+");
		if(values.length == 2){
			Integer startOffset = Integer.parseInt(values[0]);
			Integer endOffset = Integer.parseInt(values[1]);
			c.set(SentStartOffset.class, startOffset);
			c.set(SentEndOffset.class,endOffset);
		}
		else{
			c.set(SentStartOffset.class,null);
			c.set(SentEndOffset.class, null);
		}
	}

	@Override
	public String write(CoreMap c) {
			Integer start = c.get(SentStartOffset.class);
			Integer end = c.get(SentEndOffset.class);
			if(start == null || end == null){
				return "";
			}
			else{
				StringBuilder sb = new StringBuilder();
				sb.append(String.valueOf(start));
				sb.append(" ");
				sb.append(String.valueOf(end));
				return sb.toString();
			}
	}
	@Override
	public String name() {
		return this.getClass().getSimpleName().toUpperCase();
	}
}
