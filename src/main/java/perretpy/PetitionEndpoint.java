package perretpy;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

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

		/*if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}*/

		String key = "first41.last41@exemple.com";
		Key userKey = KeyFactory.createKey("User", key);
		List<Entity> result = new ArrayList();

		Query signatures = new Query("Signatures");
		signatures.setAncestor(userKey);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery psignatures = datastore.prepare(signatures);
		
		List<Entity> resultsignatures = psignatures.asList(FetchOptions.Builder.withDefaults());
		System.out.println("Result Signature" + resultsignatures.toString());
		Entity searchEntity;
		Entity resultEntity;
		ArrayList petitionSignedList = new ArrayList();
		
		for (Entity entity : resultsignatures) {
			petitionSignedList = (ArrayList) entity.getProperty("petitions");
			for (Object petitionSigned : petitionSignedList) {
				System.out.println(petitionSigned.toString());
				
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


	//Return list of petitions key signed by user
	@ApiMethod(path="signedPetition", name="signedPetition", httpMethod=HttpMethod.GET)
	public List signedPetition(User user) throws UnauthorizedException {

		/*if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}*/

		String key = "first22.last22@exemple.com";
		Key userKey = KeyFactory.createKey("User", key);
		List<Entity> result = new ArrayList();

		Query signatures = new Query("Signatures");
		signatures.setAncestor(userKey);

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery psignatures = datastore.prepare(signatures);

		List<Entity> resultsignatures = psignatures.asList(FetchOptions.Builder.withDefaults());

		List petitionsKey = new ArrayList<>();

		for (Entity entity : resultsignatures){
			petitionsKey.add(entity.getProperty("petitions").toString());
		}
		
		return petitionsKey;
	}
	
	
	@ApiMethod(path="signPetition/{petitionId}", name="signPetition", httpMethod=HttpMethod.PUT)
	public void signPetition(@Named("petitionId") String petitionId, User user) throws UnauthorizedException, EntityNotFoundException, ConcurrentModificationException {
		
		System.out.println("petition ID : " + petitionId);
		
		if (user  == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

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

				//Add petition key to Signatures of the user



				List petitionList = signedPetition(user);

				petitionList.add(petitionId);




				Key userKey = KeyFactory.createKey("User", "first22.last22@exemple.com");

				Query signatures = new Query("Signatures");
				signatures.setAncestor(userKey);

				PreparedQuery psignatures = datastore.prepare(signatures);

				List<Entity> resultsignatures = psignatures.asList(FetchOptions.Builder.withDefaults());

				resultsignatures.get(0).setProperty("petitions", petitionList);

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
	public List<Key> getSignatory(@Named("petitionId") String petitionId) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Query qSignatory = new Query("Signatures");
		qSignatory.setFilter(new FilterPredicate("petitions", FilterOperator.EQUAL, petitionId));
		System.out.println(" : " + qSignatory.toString());



		PreparedQuery pqSignatory = datastore.prepare(qSignatory);
		List<Entity> resultSignatory = pqSignatory.asList(FetchOptions.Builder.withDefaults());
		List<Key> userKey = new ArrayList<>();
		for (Entity entity : resultSignatory) {
			userKey.add(entity.getParent());
		}
		return userKey;
	}
	
	
	
	/*@ApiMethod(path="petition", name="postPetition", httpMethod=HttpMethod.POST)
	public Entity postPetition(Entity petition, User user) throws UnauthorizedException {
		
		if (user  == null) {
			throw new UnauthorizedException("Invalid credentials");
		}	
		
		petition.setProperty("owner", user.getEmail());
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		datastore.put(petition);
		
		return petition;
	}*/
}
