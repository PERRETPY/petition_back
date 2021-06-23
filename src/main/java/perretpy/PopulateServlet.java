package perretpy;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
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
		String [] tagList = {"#Animaux", "#Environnement", "#JusticeEconomique", "#Politique", "#Sante", "#PresDeVous", "#DroitsDesFemmes",
				"#DroitsDesMigrants", "#JusticePenale", "#Education", "#Handicap", "#DroitsHumains", "#Famille", "#Patrimoine", "#Autre"};

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
			ZoneId defaultZoneId = ZoneId.systemDefault();


			Date date = Date.from(petitionCreation.atStartOfDay(defaultZoneId).toInstant());
			Long millis = date.getTime();


			Long max = 999999999999999L;
			millis = max - millis;

			String key = millis + ":" + j;

			String reverseDatePetitionCreation = new StringBuilder(petitionCreation.toString()).reverse().toString();
			//Force the petition number to be on two digits to facilitate sort by title
			String title = "titrePetition";
			if (j<10) { title += "0"; }; 
			
			
			//Add key to tab for random signature and to add owner
			allPetitions.add(key);
			
			
			//Petition creation
			Entity p = new Entity("Petition", key);
			p.setProperty("title", title + j);
			p.setProperty("description", "Ceci est la description de la pétition " + j);
			HashSet<String> fset = new HashSet<String>();
			while (fset.size() < nbSignatoriesR.nextInt(3)) {
				fset.add(tagList[r.nextInt(tagList.length)]);
			}
			p.setProperty("tag", fset);
			p.setProperty("nbSignature", (long)0);
			
			
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
			String key = "first" + i + ".last" + i + "@exemple.com";
			allUsers.add(key);
			
			//User creation
			Entity u = new Entity("User", key);
			u.setProperty("firstName", "first" + i);
			u.setProperty("lastName", "last" + i);
		    
			
			//Put user into data store
			datastore.put(u);
			response.getWriter().print("<li> created user:" + u.getKey() + "<br>");
			
			
			//Signatures creation
			Entity signatureBloc = new Entity("Signatures", u.getKey());
			HashSet<String> fset = new HashSet<String>();
			while (fset.size() < nbSignatoriesR.nextInt(20)) {
				String petitionKey = allPetitions.get(r.nextInt(allPetitions.size())).toString();
				fset.add(petitionKey);
				
				Entity e=new Entity("Petition", petitionKey);
				try {
					Entity e1=datastore.get(e.getKey());
					Long nbSignature = (Long) e1.getProperty("nbSignature");
					nbSignature = nbSignature + 1;
					e1.setProperty("nbSignature", nbSignature++);
					response.getWriter().print("Pet : " + petitionKey + "nbSiganture : " + nbSignature);
					datastore.put(e1);
				} catch (EntityNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	
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