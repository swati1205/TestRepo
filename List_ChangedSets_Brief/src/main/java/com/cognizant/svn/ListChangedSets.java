package com.cognizant.svn;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
* 
* Java utility to list brief change sets of a specific repository url in SVN after a specific date. It makes use
* of SVNKIT. 
*
* @author  Cognizant GTO SEA Labs
* @version 1.0
*/

public class ListChangedSets {

	public static void main(String[] args) {
		 DAVRepositoryFactory.setup( );

			Set<String> set = new LinkedHashSet<String>();
			String url = args[2];
		    String name = args[0];
		    String password = args[1];
			long startRevision = 1;
		    long endRevision; 
		    String date=args[3];
		    
		    SVNRepository repository = null;
		    try {
		        repository = SVNRepositoryFactory.create( SVNURL.parseURIEncoded( url ) );
		        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( name, password );
		        repository.setAuthenticationManager( authManager );
		        
		        SVNDirEntry entry = repository.info(".", -1);
		        endRevision=entry.getRevision();
		        Collection logEntries = null;

		        logEntries = repository.log( new String[] { "" } , null , startRevision , endRevision , true , true );

		        for ( Iterator entries = logEntries.iterator( ); entries.hasNext( ); ) {
		            SVNLogEntry logEntry = ( SVNLogEntry ) entries.next( );
		            //System.out.println (String.format("revision: %d, date %s", logEntry.getRevision( ), logEntry.getDate()));
		            
		            DateFormat formatter = new SimpleDateFormat("EEE MM/dd/yyyy hh:mm:ss");
		            Date specifiedDate = (Date)formatter.parse(date+" 00:30:00");
		            if(logEntry.getDate().compareTo(specifiedDate)>0)
		            {
		            	Map changedSetMap = logEntry.getChangedPaths();
		            	System.out.println("Revision Number: "+logEntry.getRevision());
		            	System.out.println("Author: "+logEntry.getAuthor());
		            	System.out.println("Date: "+logEntry.getDate());
		            	System.out.println("Message: "+logEntry.getMessage());
		            	//System.out.println(changedSetMap);
			            Iterator it = changedSetMap.entrySet().iterator();
			            System.out.println("\n");
			            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------");
		            }
		            
		            
		        }
		    } catch (Exception e){ 
		    	System.out.println(e);

		    }
		    

	}

}
