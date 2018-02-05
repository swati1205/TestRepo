import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
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


public class ListModifiedFiles {
	
	
	public static void main(String[] args) {
		 DAVRepositoryFactory.setup( );

		Set<String> set = new LinkedHashSet<String>();
		String url = "https://PC248463.cts.com/svn/CAFE/";
	    String name = "anamika";
	    String password = "anamika";
		long startRevision = 1;
	    long endRevision; 
	    String date="Tue 08/04/2016";
	    
	    SVNRepository repository = null;
	    try {
	        repository = SVNRepositoryFactory.create( SVNURL.parseURIEncoded( url ) );
	        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( name, password );
	        repository.setAuthenticationManager( authManager );
	        
	        SVNDirEntry entry = repository.info(".", -1);
	        System.out.println("Latest Rev: " + entry.getRevision()); 
	        endRevision=entry.getRevision();
	        //System.out.println(LocalDate.now());
	        
	        
	        Collection logEntries = null;

	        logEntries = repository.log( new String[] { "" } , null , startRevision , endRevision , true , true );

	        for ( Iterator entries = logEntries.iterator( ); entries.hasNext( ); ) {
	            SVNLogEntry logEntry = ( SVNLogEntry ) entries.next( );
	            System.out.println (String.format("revision: %d, date %s", logEntry.getRevision( ), logEntry.getDate()));
	            //System.out.println(logEntry.getChangedPaths());
	            /*Map changedSetMap = logEntry.getChangedPaths();
	            Iterator it = changedSetMap.entrySet().iterator();
	            while (it.hasNext()) {
	                Map.Entry pair = (Map.Entry)it.next();
	                //System.out.println(pair.getKey() + " = " + pair.getValue());
	                String modifiedFilePath=pair.getKey().toString();
	                //System.out.println(modifiedFilePath);
	                String[] parts = modifiedFilePath.split("/");
	                //String part1 = parts[1];
	                //System.out.println(part1);
	                String part2 = parts[2];
	                System.out.println(part2);
	                set.add(part2);
	                
	                //it.remove(); // avoids a ConcurrentModificationException
	            }*/
	            // MM/dd/yyyy HH:mm:ss
	            
	            DateFormat formatter = new SimpleDateFormat("EEE MM/dd/yyyy");
	            Date specifiedDate = (Date)formatter.parse(date);
	            //System.out.println(specifiedDate);
	            
	            if(logEntry.getDate().compareTo(specifiedDate)>0)
	            		//equals(Calendar.getInstance().getTimeInMillis()));
	            //if(logEntry.getDate()==specifiedDate)
	            {
	            	Map changedSetMap = logEntry.getChangedPaths();
	            	System.out.println(logEntry.getRevision());
	            	System.out.println(changedSetMap);
	            	System.out.println(logEntry.getAuthor());
		            Iterator it = changedSetMap.entrySet().iterator();
		            while (it.hasNext()) {
		                Map.Entry pair = (Map.Entry)it.next();
		                String modifiedFilePath=pair.getKey().toString();
		                String[] parts = modifiedFilePath.split("/");
		                String part2 = parts[2];
		                //System.out.println(part2);
		                set.add(part2);
		            }
	            }
	            
	            
	        }
	    } catch (Exception e){ 
	    	System.out.println(e);

	    }
	    
	    //SVNDiffClient diffClient = clientManager.getDiffClient();
	    System.out.println("Set of branches: "+set);

	}
	
	 

}
