package cmu.isr.arunkaly.pypi;

import cmu.isr.arunkaly.miners.IssueCommentsMiner;
import cmu.isr.arunkaly.miners.IssuesMiner;

public class TestIssueMining {

	//Major Error: Github APi does not let us page through all issues thre seems to be a random page limit
	//https://api.github.com/repos/scipy/scipy/issues?client_id=9249116ee8e4a73edc52&client_secret=bb2abc93bdaf6b5ce2008b848cd07d1e203a5e32&page=31
	//Increasing per_page does not make any difference
	public static void main(String[] args) {
		FileOutputLayer outputController = new FileOutputLayer(".");
		IssueCommentsMiner im = new IssueCommentsMiner("scipy", "scipy", false);
		im.invokeAndWrite(outputController);
	}
}
