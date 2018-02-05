package com.cognizant.subversion;
import java.io.ByteArrayOutputStream;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnDiff;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;


public class DoDiffMerge {

	public static void main(String[] args) throws SVNException {
		//URL url1 = new URL("");
		String url1 = "https://PC248463.cts.com/svn/Jpetstore/trunk";
		String url2 ="https://PC248463.cts.com/svn/Jpetstore/branches/b1";
	   // String name = "anamika";
	   // String password = "anamika";
	    SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		//final SVNClientManager clientManager = SVNClientManager.newInstance();
		try {
			
			SVNRepository repository1 = SVNRepositoryFactory.create( SVNURL.parseURIEncoded( url1) );
			SVNRepository repository2 = SVNRepositoryFactory.create( SVNURL.parseURIEncoded( url2 ) );
			//ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( name, password );
			
			svnOperationFactory.setAuthenticationManager(repository1.getAuthenticationManager());
			svnOperationFactory.setAuthenticationManager(repository2.getAuthenticationManager());
			svnOperationFactory.setOptions(new DefaultSVNOptions());  
	        //repository1.setAuthenticationManager( authManager );
	        //repository2.setAuthenticationManager( authManager );
		    /*final SVNDiffClient diffClient = clientManager.getDiffClient();

		    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    //final OutputStream outputStream= new OutputStream();
		    diffClient.doDiff(SVNURL.parseURIEncoded( url1) , SVNRevision.HEAD,SVNURL.parseURIEncoded( url2 ), SVNRevision.HEAD, SVNDepth.INFINITY, false, outputStream);//(url1, SVNRevision.HEAD, url2, SVNRevision.HEAD, SVNDepth.INFINITY, false, outputStream, null);
		    System.out.println(outputStream);*/
	        
	        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

	        final SvnDiff diff = svnOperationFactory.createDiff();
	        diff.setSources(SvnTarget.fromURL(SVNURL.parseURIEncoded( url1), SVNRevision.HEAD), SvnTarget.fromURL(SVNURL.parseURIEncoded( url2), SVNRevision.HEAD));
	        diff.setOutput(byteArrayOutputStream);
	        diff.run();
	        System.out.println(byteArrayOutputStream);
		} finally {
		    //clientManager.dispose();
			svnOperationFactory.dispose();
		}


	}

}
