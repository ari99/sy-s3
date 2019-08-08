package sf.sf;

import org.springframework.util.AntPathMatcher;

public class TestPatternMatcher {
    public static void main( String[] args )
    {	
		AntPathMatcher matcher = new AntPathMatcher();
		boolean res = matcher.match("**/*1.jpg", "aaa/bb/00271.jpg") ;
    	System.out.println("res: "+res);
    }
}
