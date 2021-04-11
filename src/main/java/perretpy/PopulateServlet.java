package perretpy;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

@SuppressWarnings("serial")
@WebServlet(name = "PopulateServlet", urlPatterns = { "/populate" })
public class PopulateServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		int nbUsers = 100;
		int nbPetitions = 50;
		LocalDate startDate = LocalDate.of(2010, 1, 1); //start date for date of user creation
		LocalDate endDate = LocalDate.now(); //end date for date of user creation

		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		
		//tag from change.org web site
		String [] tagList = {"Animaux", "Environnement", "Justice économique", "Politique", "Santé", "Près de vous", "Droits des femmes",
				"Droits des migrants", "Justice pénale", "Education", "Handicap", "Droits humains", "Famille", "Patrimoine", "Autre"};

		Random r = new Random();
		Random nbSignatoriesR = new Random();
		ArrayList<String> allPetitions = new ArrayList<String>();
		ArrayList<String> allUsers = new ArrayList<String>();
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		
		//Delete all the datastore
		Query petitions = new Query("Petition");
		PreparedQuery ppetitions = datastore.prepare(petitions);
		List<Entity> resultPetitions = ppetitions.asList(FetchOptions.Builder.withDefaults());
		for (Entity entity : resultPetitions) {
			datastore.delete(entity.getKey());			
		}
		Query users = new Query("User");
		PreparedQuery pusers = datastore.prepare(users);
		List<Entity> resultUsers = pusers.asList(FetchOptions.Builder.withDefaults());
		for (Entity entity : resultUsers) {
			datastore.delete(entity.getKey());			
		}
		Query signatures = new Query("Signatures");
		PreparedQuery psignatures = datastore.prepare(signatures);
		List<Entity> resultSignatures = psignatures.asList(FetchOptions.Builder.withDefaults());
		for (Entity entity : resultSignatures) {
			datastore.delete(entity.getKey());			
		}
		
		
		//Create [nbPetitions] petitions
		for (int j=0 ; j<nbPetitions ; j++) {
			//Creation of a random petition creation date for entity key to facilitate sort petition by creation date 
			LocalDate petitionCreation = randomDateBetween(startDate, endDate);
			String reverseDatePetitionCreation = new StringBuilder(petitionCreation.toString()).reverse().toString();
			//Force the petition number to be on two digits to facilitate sort by title
			String title = "titrePetition";
			if (j<10) { title += "0"; }; 
			
			
			//Add key to tab for random signature and to add owner
			String key = reverseDatePetitionCreation + "p" + j;
			allPetitions.add(key);
			
			
			//Petition creation
			Entity p = new Entity("Petition", key);
			p.setProperty("title", title + j);
			p.setProperty("description", "Ceci est la description de la pétition " + j);
			p.setProperty("tag", tagList[r.nextInt(tagList.length)]);
			
			
			//Put petition into data store
			datastore.put(p);
			response.getWriter().print("<li> created petition:" + p.getKey() + "Signataire : " + p.getProperty("signatories") + "<br>");
		}
		
		
		//Create [nbUsers] user
		for (int i=0 ; i<nbUsers ; i++) {
			//Creation of a random user creation date for entity key to facilitate sort users by creation date 
			LocalDate userCreation = randomDateBetween(startDate, endDate);
			String reverseDateUserCreation = new StringBuilder(userCreation.toString()).reverse().toString();
			
			
			//Add key to tab for random signature and to add owner
			String key = reverseDateUserCreation + "u" + i;
			allUsers.add(key);
			
			//User creation
			Entity u = new Entity("User", key);
			u.setProperty("firstName", "first" + i);
			u.setProperty("lastName", "last" + i);
			u.setProperty("mail", "first" + i + ".last" + i + "@exemple.com");
		    
			
			//Put user into data store
			datastore.put(u);
			response.getWriter().print("<li> created user:" + u.getKey() + "<br>");
			
			
			//Signatures creation
			Entity signatureBloc = new Entity("Signatures", u.getKey());
			HashSet<String> fset = new HashSet<String>();
			while (fset.size() < nbSignatoriesR.nextInt(20)) {
				fset.add(allPetitions.get(r.nextInt(allPetitions.size())).toString());
			}
			signatureBloc.setProperty("petitions", fset);
			
			
			//Put signature bloc into data store
			datastore.put(signatureBloc);
		}
		
		
		//Add owner to all petitions
		for (int i=0 ; i<allPetitions.size(); i++) {
			Entity petition = new Entity("Petition", allPetitions.get(i));
			String owner = allUsers.get(r.nextInt(allUsers.size()));
			
			try {
				Entity petitionDS = datastore.get(petition.getKey());
				petitionDS.setProperty("owner", owner);
				datastore.put(petitionDS);
			} catch (EntityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	* Return an random date between startDate and endDate 
	*
	* @param  startDate	LocalDate 
	* @param  endDate	LocalDate
	* @return LocalDate between startDate and endDate
	*/
	public LocalDate randomDateBetween(LocalDate startDate, LocalDate endDate){
		long start = startDate.toEpochDay();
		long end = endDate.toEpochDay();
		long randomEpochDay = ThreadLocalRandom.current().longs(start, end).findAny().getAsLong();
		return LocalDate.ofEpochDay(randomEpochDay);
	}
	
}