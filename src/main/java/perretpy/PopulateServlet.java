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
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

@WebServlet(name = "PopulateServlet", urlPatterns = { "/populate" })
public class PopulateServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		int nbUsers = 100;
		int nbPetitions = 50;

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		
		//tag from change.org web site
		String [] tagList = {"Animaux", "Environnement", "Justice économique", "Politique", "Santé", "Près de vous", "Droits des femmes",
				"Droits des migrants", "Justice pénale", "Education", "Handicap", "Droits humains", "Famille", "Patrimoine", "Autre"};

		Random r = new Random();
		Random nbSignatoriesR = new Random();
		int nbSignatories;
		
		
		//Create 1000 user
		for (int i=0 ; i<nbUsers ; i++) {
			Entity u = new Entity("User", "u" + i);
			u.setProperty("firstName", "first" + i);
			u.setProperty("lastName", "last" + i);
			
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			datastore.put(u);

			response.getWriter().print("<li> created user:" + u.getKey() + "<br>");
		}
		
		
		
		//Create [nbPetitions] petitions
		for (int j=0 ; j<nbPetitions ; j++) {
			Entity p = new Entity("Petition", "p" + j);
			String titre = "titrePetition";
			if (j<10) { titre += "0"; } //facilite le tri sur le titre
			p.setProperty("titre", titre + j);
			p.setProperty("description", "Ceci est la description de la pétition " + j);
			p.setProperty("tag", tagList[r.nextInt(14)]);
			
			nbSignatories = nbSignatoriesR.nextInt(19) + 1;
			
			HashSet<String> fset = new HashSet<String>();
			while (fset.size() < nbSignatories) {
				fset.add("u" + r.nextInt(nbUsers));
			}
			p.setProperty("signatories", fset);
			
			
			
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			datastore.put(p);

			response.getWriter().print("<li> created petition:" + p.getKey() + "Signataire : " + p.getProperty("signatories") + "<br>");
		}
		
		
		
		
		
		
		
		
		
		
	}
}