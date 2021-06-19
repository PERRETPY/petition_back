package perretpy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import com.google.appengine.repackaged.com.google.datastore.v1.CompositeFilter;
import com.google.appengine.repackaged.com.google.datastore.v1.Projection;
import com.google.appengine.repackaged.com.google.datastore.v1.PropertyFilter;

@WebServlet(name = "Queries", urlPatterns = { "/queries" })
public class Queries extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		
		if (request.getUserPrincipal() != null) {
			response.getWriter().print(request.getUserPrincipal().getName());
		}
		
		

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		
	//Q1 : Existence of User0
		
		
	//Q2 : All users with firstName projection	

		response.getWriter().print("<h2> Q2: TOP 100 All users with firstName projection </h2>");	
		
		long t1=System.currentTimeMillis();
		
		Query q = new Query("User");
		System.out.println("Q2 : " + q.toString());
		q.addProjection(new PropertyProjection("firstName",String.class));
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));

		response.getWriter().print("<li> result:" + result.size() + "<br>");
		for (Entity entity : result) {
		    response.getWriter().print(entity.getProperty("firstName")+"<br>");
		}
		
		
	//Q3 : All petitions with title and tag projection
		
		response.getWriter().print("<h2> Q3: TOP 100 All petitions with title and tag projection </h2>");	
		
		long t2=System.currentTimeMillis();
		
		Query qpet = new Query("Petition");
		System.out.println("Q3 : " + qpet.toString());
		//qpet.addProjection(new PropertyProjection("titre",String.class));
		//qpet.addSort("titre");
		PreparedQuery pqpet = datastore.prepare(qpet);
		List<Entity> resultpet = pqpet.asList(FetchOptions.Builder.withLimit(100));

		response.getWriter().print("<li> result:" + resultpet.size() + "<br>");
		for (Entity entity : resultpet) {
		    response.getWriter().print(entity.getProperty("title")+ " | " + entity.getProperty("nbSignature") +"<br>");
		}
		long t3=System.currentTimeMillis();
		
		
	
		
		
	//Q4 : Petitions with "Environnement" title projection
		
		response.getWriter().print("<h2> Q4: now just print petitions with tag Environnement (titre and tag projected) </h2>");
		
		Query qpetTag = new Query("Petition");
		System.out.println("Q4 : " + qpetTag.toString());
		qpetTag.setFilter(new FilterPredicate("tag", FilterOperator.EQUAL, "Environnement"));
		//qpetTag.addProjection(new PropertyProjection("titre", String.class));
		
		PreparedQuery pqpetTag = datastore.prepare(qpetTag);
		List<Entity> resultpetTag = pqpetTag.asList(FetchOptions.Builder.withDefaults());
		
		response.getWriter().print("<li> result:" + resultpetTag.size() + "<br>");
		for (Entity entity : resultpetTag) {
			response.getWriter().print(entity.getProperty("title")+" : "+entity.getProperty("tag")+"<br>");
		}
		long t4=System.currentTimeMillis();
	
	//Q5 : Petitions signed by User0
		
		Query qTemp = new Query("User");
		System.out.println("Q5 : " + qTemp.toString());
		PreparedQuery pqTemp = datastore.prepare(qTemp);
		List<Entity> tempUser = pqTemp.asList(FetchOptions.Builder.withLimit(1));
		
		long t5=System.currentTimeMillis();
		
		response.getWriter().print("<h2> Q5: Petitions signed by " + tempUser.get(0).getProperty("firstName") + "</h2>");
		
		Query signatures = new Query("Signatures");
		signatures.setAncestor(tempUser.get(0).getKey());
		System.out.println("Q5 bis : " + signatures.toString());
		
		PreparedQuery psignatures = datastore.prepare(signatures);
		List<Entity> resultsignatures = psignatures.asList(FetchOptions.Builder.withDefaults());
		//List<Entity> petitionSigned;
		Entity searchEntity;
		Entity resultEntity;
		
		for (Entity entity : resultsignatures) {
			ArrayList petitionSignedList = (ArrayList) entity.getProperty("petitions");
			for (Object petitionSigned : petitionSignedList) {
				
				searchEntity = new Entity("Petition",petitionSigned.toString());
				try {
					resultEntity = datastore.get(searchEntity.getKey());
					response.getWriter().print(resultEntity.getProperty("title") + "<br>");
				} catch (EntityNotFoundException EntityNotFound) {
					// TODO Auto-generated catch block
					EntityNotFound.printStackTrace();
				}
				
				System.out.println(petitionSigned.toString());
				
			}
		}
		long t5bis=System.currentTimeMillis();
		
	//Print timing 
	
			response.getWriter().print("<h2> Timer </h2>");	
			
			response.getWriter().print("<li> Q2 : "+(t2-t1)+"ms");
			
			response.getWriter().print("<li> Q3 : "+(t3-t2)+"ms");
			
			response.getWriter().print("<li> Q4 : "+(t4-t3)+"ms");
			
			response.getWriter().print("<li> Q5 : "+(t5bis-t5)+"ms");
		
	//10 Petitions more signed
		
		/*response.getWriter().print("<h2> Q6: 10 Petitions more signed (NOT WORK) </h2>");	
		
		Query qpetMoreSigned = new Query("Petition");
		qpetMoreSigned.addProjection(new PropertyProjection("titre",String.class));
		//qpetMoreSigned.addSort("signatories", null);
		//qpetMoreSigned.setOrderBy(OrderBy.desc("priority"));
		
		PreparedQuery pqpetMoreSigned = datastore.prepare(qpetMoreSigned);
		List<Entity> resultpetMoreSigned = pqpetMoreSigned.asList(FetchOptions.Builder.withDefaults());

		response.getWriter().print("<li> result:" + resultpetMoreSigned.size() + "<br>");
		for (Entity entity : resultpetMoreSigned) {
		    response.getWriter().print(entity.getProperty("titre")+"."+entity.getProperty("tag")+"/");
		}*/	
	}
}
