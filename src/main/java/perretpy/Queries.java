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

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		
	//Q1 : Existence of User0

		response.getWriter().print("<h1> Friends Queries </h1>");;

		response.getWriter().print("<h2> is f0 exist ? </h2>");

		
		Entity e=new Entity("User","u0");
		try {
			Entity e1=datastore.get(e.getKey());
			response.getWriter().print("<li> Get F0:" + e1.getProperty("firstName"));
		} catch (EntityNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	//Q2 : All users with firstName projection	

		response.getWriter().print("<h2> Q2: All users with firstName projection </h2>");	
		
		long t1=System.currentTimeMillis();
		
		Query q = new Query("User");
		q.addProjection(new PropertyProjection("firstName",String.class));
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withDefaults());

		response.getWriter().print("<li> result:" + result.size() + "<br>");
		for (Entity entity : result) {
		    response.getWriter().print(entity.getProperty("firstName")+"<br>");
		}
		
		
	//Q3 : All petitions with title and tag projection
		
		response.getWriter().print("<h2> Q3: All petitions with title and tag projection </h2>");	
		
		long t2=System.currentTimeMillis();
		
		Query qpet = new Query("Petition");
		qpet.addProjection(new PropertyProjection("titre",String.class));
		qpet.addProjection(new PropertyProjection("tag",String.class));
		qpet.addSort("titre");
		PreparedQuery pqpet = datastore.prepare(qpet);
		List<Entity> resultpet = pqpet.asList(FetchOptions.Builder.withDefaults());

		response.getWriter().print("<li> result:" + resultpet.size() + "<br>");
		for (Entity entity : resultpet) {
		    response.getWriter().print(entity.getProperty("titre")+" : "+entity.getProperty("tag")+"<br>");
		}
		long t3=System.currentTimeMillis();
		
		
	//Print timing of Q2 and Q3
		
		response.getWriter().print("<h2> Timer </h2>");	
		
		response.getWriter().print("<li> Q2 : "+(t2-t1)+"ms");
		
		response.getWriter().print("<li> Q3 : "+(t3-t2)+"ms");
		
		
	//Q4 : Petitions with "Environnement" title projection
		
		response.getWriter().print("<h2> Q4: now just print petitions with tag Environnement (titre and tag projected) </h2>");
		
		Query qpetTag = new Query("Petition");
		qpetTag.setFilter(new FilterPredicate("tag", FilterOperator.EQUAL, "Environnement"));
		//qpetTag.addProjection(new PropertyProjection("titre", String.class));
		
		PreparedQuery pqpetTag = datastore.prepare(qpetTag);
		List<Entity> resultpetTag = pqpetTag.asList(FetchOptions.Builder.withDefaults());
		
		response.getWriter().print("<li> result:" + resultpetTag.size() + "<br>");
		for (Entity entity : resultpetTag) {
			response.getWriter().print(entity.getProperty("titre")+" : "+entity.getProperty("tag")+"<br>");
		}
		
	
	//Q5 : Petitions signed by User0
		
		response.getWriter().print("<h2> Q5: Petitions signed by User0 </h2>");
		
		Query qpetUser = new Query("Petition");
		qpetUser.setFilter(new FilterPredicate("signatories", FilterOperator.EQUAL, "u0"));
		qpetUser.addProjection(new PropertyProjection("titre", String.class));
		
		PreparedQuery pqpetUser = datastore.prepare(qpetUser);
		List<Entity> resultpetUser = pqpetUser.asList(FetchOptions.Builder.withDefaults());
		
		response.getWriter().print("<li> result:" + resultpetUser.size() + "<br>");
		for (Entity entity : resultpetUser) {
			response.getWriter().print(entity.getProperty("titre") + " <br>");
		}
		
		
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
