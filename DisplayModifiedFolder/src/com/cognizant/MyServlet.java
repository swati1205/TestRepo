package com.cognizant;


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * Servlet implementation class MyServlet
 */
public class MyServlet extends HttpServlet {
	Set<String> set = new LinkedHashSet<String>();
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MyServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session=request.getSession();
		response.setContentType("text/html");  
		String app=request.getParameter("app");
		if(app.equalsIgnoreCase("CAFE")){
			DAVRepositoryFactory.setup( );

			
			String url = "https://PC248463.cts.com/svn/CAFE/";
		    String name = "anamika";
		    String password = "anamika";
			long startRevision = 1;
		    long endRevision; 
		    String date="07/15/2016 12:30:00";
		    
		    SVNRepository repository = null;
		    try {
		        repository = SVNRepositoryFactory.create( SVNURL.parseURIEncoded( url ) );
		        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( name, password );
		        repository.setAuthenticationManager( authManager );
		        
		        SVNDirEntry entry = repository.info(".", -1);
		        System.out.println("Latest Rev: " + entry.getRevision()); 
		        endRevision=entry.getRevision();
		        
		        
		        Collection logEntries = null;

		        logEntries = repository.log( new String[] { "" } , null , startRevision , endRevision , true , true );

		        for ( Iterator entries = logEntries.iterator( ); entries.hasNext( ); ) {
		            SVNLogEntry logEntry = ( SVNLogEntry ) entries.next( );
		            System.out.println (String.format("revision: %d, date %s", logEntry.getRevision( ), logEntry.getDate()));
		            
		            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		            Date specifiedDate = (Date)formatter.parse(date);
		            //System.out.println(specifiedDate);
		            
		            if(logEntry.getDate().compareTo(specifiedDate)>0)
		            {
		            	Map changedSetMap = logEntry.getChangedPaths();
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
		    System.out.println("Set of branches: "+set);
		    request.setAttribute("set", set);
		    RequestDispatcher rd = request.getRequestDispatcher("Index.jsp");
		    rd.include(request,response);

		}
		
		else{
			System.out.println("Application is not CAFE");
		}
		 
	}

}
