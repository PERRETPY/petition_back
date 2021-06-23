package perretpy;

import com.google.api.server.spi.config.*;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.User;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;

//import com.google.api.server.spi.auth.common.User;
//import com.google.appengine.api.datastore.Query.PropertyFilter;
//import com.google.appengine.repackaged.com.google.datastore.v1.CompositeFilter;
//import com.google.appengine.repackaged.com.google.datastore.v1.CompositeFilter;


@Api(name = "petitionEndpoint",
	version = "v1",
	audiences = "872392969523-in5sir0l4md25uv570creil2d1th3ks7.apps.googleusercontent.com",
	clientIds = "872392969523-in5sir0l4md25uv570creil2d1th3ks7.apps.googleusercontent.com",
	namespace =
	@ApiNamespace(
			ownerDomain = "helloworld.example.com",
			ownerName = "helloworld.example.com",
			packagePath = "")
	)

public class PetitionEndpoint {
	
	@ApiMethod(name = "top100", httpMethod = HttpMethod.GET)
	public CollectionResponse<Entity> top100(@Nullable @Named("tag1") String tag1, @Nullable @Named("next") String cursorString) {
		System.out.println("Hello from top100");

		Query q = new Query("Petition");
		if (tag1 != null) {
			q.setFilter(new FilterPredicate("tag", FilterOperator.EQUAL, tag1));
		}

		q.addSort("nbSignature", Query.SortDirection.DESCENDING);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		
		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(4);
		
		if (cursorString != null) {
			fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
		}
		
		QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);
		
		cursorString = results.getCursor().toWebSafeString();
		
		return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();
	}
	
	@ApiMethod(path = "petition/{id}", name = "getPetition", httpMethod = HttpMethod.GET)
	public Entity getPetition(@Named("id") String id){
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity searchEntity=new Entity("Petition",id);
		Entity resultEntity;
		try {
			resultEntity = datastore.get(searchEntity.getKey());
		} catch (EntityNotFoundException EntityNotFound) {
			// TODO Auto-generated catch block
			EntityNotFound.printStackTrace();
			resultEntity = null;
		}
		return resultEntity;
	}

	//Return petition signed by user
	@ApiMethod(path = "petitionSigned", name = "auth", httpMethod = HttpMethod.GET)
	public List<Entity> petitionSigned(User user) throws UnauthorizedException {

		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}

		Key userKey = KeyFactory.createKey("User", user.getEmail());
		List<Entity> result = new ArrayList();

		Query signatures = new Query("Signatures");
		signatures.setAncestor(userKey);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery psignatures = datastore.prepare(signatures);
		
		List<Entity> resultsignatures = psignatures.asList(FetchOptions.Builder.withDefaults());
		Entity searchEntity;
		Entity resultEntity;
		ArrayList petitionSignedList = new ArrayList();
		
		for (Entity entity : resultsignatures) {
			petitionSignedList = (ArrayList) entity.getProperty("petitions");
			for (Object petitionSigned : petitionSignedList) {
				searchEntity = new Entity("Petition",petitionSigned.toString());
				try {
					resultEntity = datastore.get(searchEntity.getKey());
					result.add(resultEntity);
				} catch (EntityNotFoundException EntityNotFound) {
					// TODO Auto-generated catch block
					EntityNotFound.printStackTrace();
				}
				
				System.out.println(petitionSigned.toString());
				
			}
		}
		return result;
	}

	@ApiMethod(path= "myPetitions", name = "myPetitions", httpMethod = HttpMethod.GET)
	public List<Entity> myPetitions(User user, @Nullable @Named("next") String cursorString) throws UnauthorizedException {

		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}

		Query myPetitions = new Query("Petition");
		myPetitions.setFilter(new FilterPredicate("owner", FilterOperator.EQUAL, user.getEmail()));

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(myPetitions);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
		return result;
	}

	//Return list of petitions key signed by user
	//@ApiMethod(path="signedPetition", name="signedPetition", httpMethod=HttpMethod.GET)
	public List signedPetition(User user, @Named("petitionId") String petitionId) throws UnauthorizedException {

		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}

		Key userKey = KeyFactory.createKey("User", user.getEmail());

		Query signatures = new Query("Signatures");
		signatures.setAncestor(userKey);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery psignatures = datastore.prepare(signatures);

		List<Entity> resultsignatures = psignatures.asList(FetchOptions.Builder.withDefaults());

		List petitionsKey = new ArrayList<>();

		for (Entity entity : resultsignatures){
			petitionsKey.add(entity.getProperty("petitions").toString());
		}
		System.out.println("PETITIONS_KEY = " + petitionsKey);
		return petitionsKey;
	}
	
	@ApiMethod(path="signPetition/{petitionId}", name="signPetition", httpMethod=HttpMethod.PUT)
	public void signPetition(@Named("petitionId") String petitionId, User user) throws UnauthorizedException, EntityNotFoundException, ConcurrentModificationException {
		
		System.out.println("petition ID : " + petitionId);
		
		if (user  == null) {
			throw new UnauthorizedException("Invalid credentials");
		}

		Key userKeyV = KeyFactory.createKey("User", user.getEmail());

		Query signaturesV = new Query("Signatures");
		signaturesV.setAncestor(userKeyV);
		signaturesV.setFilter(new FilterPredicate("petitions", FilterOperator.EQUAL, petitionId));

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery psignaturesV = datastore.prepare(signaturesV);

		List<Entity> resultsignaturesV = psignaturesV.asList(FetchOptions.Builder.withDefaults());
		if (resultsignaturesV.size() != 0 ) {
			throw new UnauthorizedException("Petition already signed");
		}



		System.out.println("USER ID : " + user.getEmail());


		TransactionOptions options = TransactionOptions.Builder.withXG(true);
		Transaction txn = datastore.beginTransaction(options);

		int retries = 3;
		Entity resultPetition = null;
		Key petitionKey = KeyFactory.createKey("Petition", petitionId);

		while (true) {

			try {
				//Add 1 to nbSignature in petition entity
				resultPetition = datastore.get(petitionKey);
				long nbSignatures = (long) resultPetition.getProperty("nbSignature");
				nbSignatures += 1;
				resultPetition.setProperty("nbSignature", nbSignatures);
				datastore.put(txn, resultPetition);

				System.out.println("1 OK");

				//Add petition key to Signatures of the user

				List petitionList = signedPetition(user, petitionId);
				HashSet<String> petitionHashSet = new HashSet<String>();
				for (Object petition : petitionList) {
					petitionHashSet.add(petition.toString());
				}

				//HashSet<String> petitionList = (HashSet<String>) signedPetition(user);

				petitionHashSet.add(petitionId);

				Key userKey = KeyFactory.createKey("User", user.getEmail());

				Query signatures = new Query("Signatures");
				signatures.setAncestor(userKey);

				PreparedQuery psignatures = datastore.prepare(signatures);

				List<Entity> resultsignatures = psignatures.asList(FetchOptions.Builder.withDefaults());


				resultsignatures.get(0).setProperty("petitions", petitionHashSet);

				datastore.put(txn, resultsignatures.get(0));

				txn.commit();
				System.out.println("Petition " + petitionId + " signed by " + user.getEmail());
				break;
				
			} catch (ConcurrentModificationException ConcurrentModification) {
				if (retries == 0) {
					ConcurrentModification.printStackTrace();
					throw ConcurrentModification;
				}
				--retries;
				
			} catch (EntityNotFoundException EntityNotFound) {
				EntityNotFound.printStackTrace();
				throw EntityNotFound;
				
			} finally {
			    if (txn.isActive()) {
			        txn.rollback();
			    }
			}
			
		}
		//return signedPetition;
	}

	@ApiMethod(path="signatory/{petitionId}", name="getSignatory", httpMethod = HttpMethod.GET)
	public List<Entity> getSignatory(@Named("petitionId") String petitionId) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Query qSignatory = new Query("Signatures");
		qSignatory.setFilter(new FilterPredicate("petitions", FilterOperator.EQUAL, petitionId));
		System.out.println(" : " + qSignatory.toString());



		PreparedQuery pqSignatory = datastore.prepare(qSignatory);
		List<Entity> resultSignatory = pqSignatory.asList(FetchOptions.Builder.withDefaults());
		List<Entity> userKey = new ArrayList<>();
		for (Entity entity : resultSignatory) {
			System.out.println(entity.getParent());
			Entity resultEntity;
			try {
				resultEntity = datastore.get(entity.getParent());
				System.out.println(resultEntity.getKey());
			} catch (EntityNotFoundException EntityNotFound) {
				// TODO Auto-generated catch block
				EntityNotFound.printStackTrace();
				resultEntity = null;
			}
			userKey.add(resultEntity);
		}
		return userKey;
	}
	
	@ApiMethod(path="petition", name="postPetition", httpMethod=HttpMethod.POST)
	public Entity postPetition(Petition petition, User user)  {
		
		System.out.println("Hello from postpetition");
		Long max = 999999999999999L;
		Long millis = max - System.currentTimeMillis();

		String key = millis + ":" + user.getEmail();

		Entity newPetition = new Entity("Petition", key);
		newPetition.setProperty("title", petition.getTitle());
		newPetition.setProperty("description", petition.getDescription());
		newPetition.setProperty("tag", petition.getTags());
		newPetition.setProperty("owner", user.getEmail());
		newPetition.setProperty("nbSignature", 0);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		datastore.put(newPetition);
		
		return newPetition;
	}

	@ApiMethod(path="search", name="search", httpMethod = HttpMethod.GET)
	public CollectionResponse<Entity> search(@Named("query") String query, @Nullable @Named("next") String cursorString) {

		String SEPARATEUR = " ";

		String mots[] = query.split(SEPARATEUR);
		ArrayList<String> words= new ArrayList<>();
		String title = "";

		Query q = new Query("Petition");
		Boolean firstWord = true;

		ArrayList<FilterPredicate> filters = new ArrayList<FilterPredicate>();

		for (String mot : mots) {
			if(mot.charAt(0) == '#') {
				filters.add(new FilterPredicate("tag", FilterOperator.EQUAL, mot));
			}else {
				if (firstWord) { title += mot; }else {title += mot + " "; };
				firstWord = false;
			}
		}

		if(!firstWord) { filters.add(new FilterPredicate("title", FilterOperator.EQUAL, title)); }

		FilterPredicate[] tabs = filters.toArray(new FilterPredicate[0]);

		if (filters.size() > 1) {
			q.setFilter(CompositeFilterOperator.and(tabs));
		} else {
			q.setFilter(filters.get(0));
		}



		System.out.println(q.toString());

		//q.addSort("nbSignature", Query.SortDirection.DESCENDING);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);

		FetchOptions fetchOptions = FetchOptions.Builder.withLimit(4);

		if (cursorString != null) {
			fetchOptions.startCursor(Cursor.fromWebSafeString(cursorString));
		}

		QueryResultList<Entity> results = pq.asQueryResultList(fetchOptions);

		cursorString = results.getCursor().toWebSafeString();

		return CollectionResponse.<Entity>builder().setItems(results).setNextPageToken(cursorString).build();
	}

	@ApiMethod(path="newConnection", name="newConnection", httpMethod = HttpMethod.GET)
	public Entity newConnection(User user) throws UnauthorizedException {
		System.out.println("Hello from newConnection");
		if (user  == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity searchEntity=new Entity("User", user.getEmail());
		Entity resultEntity = null;
		try {
			resultEntity = datastore.get(searchEntity.getKey());
		} catch (EntityNotFoundException EntityNotFound) {
			Entity newSignature  = new Entity("Signatures", searchEntity.getKey());
			resultEntity = searchEntity;
			datastore.put(searchEntity);
			datastore.put(newSignature);
		}
		return resultEntity;
	}
}
