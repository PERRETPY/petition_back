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
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

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
		
		System.out.println("Voila le token que j'ai reçu : " + cursorString);
		
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
	
	
	@ApiMethod(path = "petitionSigned", name = "auth", httpMethod = HttpMethod.GET)
	public List<Entity> auth(User user) throws UnauthorizedException {

		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		String key = "first41.last41@exemple.com";
		Entity searchEntityUser=new Entity("Petition",key);
		List<Entity> result = new ArrayList();
		
		// SELECT * FROM Signatures WHERE __key__ HAS ANCESTOR Key(User, '01-20-1102u41')
		
		Query signatures = new Query("Signatures");
		signatures.setAncestor(searchEntityUser.getKey());
		System.out.println("Q5 bis : " + signatures.toString());
		
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
		return petitionSignedList;
	}
	
	@ApiMethod(path="signedPetition", name="signedPetition", httpMethod=HttpMethod.GET)
	public Entity signedPetition(User user) throws UnauthorizedException {

		if (user  == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Entity searchEntity = new Entity("Signature", user.getEmail());
		Entity resultEntity = null;
		try {
			resultEntity = datastore.get(searchEntity.getKey());
		} catch (EntityNotFoundException EntityNotFound) {
			// TODO Auto-generated catch block
			EntityNotFound.printStackTrace();
		}
		
		return resultEntity;
	}
	
	
	@ApiMethod(path="signPetition/{petitionId}", name="signPetition", httpMethod=HttpMethod.PUT)
	public Entity signPetition(@Named("petitionId") String petitionId, User user) throws UnauthorizedException, EntityNotFoundException, ConcurrentModificationException {
		
		System.out.println("petition ID : " + petitionId);
		
		if (user  == null) {
			throw new UnauthorizedException("Invalid credentials");
		}	
		
		int retries = 3;
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity resultPetition = null;
		Key petitionKey = KeyFactory.createKey("MessageBoard", petitionId);
		Entity signedPetition = signedPetition(user);
		
		while (true) {
			TransactionOptions options = TransactionOptions.Builder.withXG(true);
			Transaction txn = datastore.beginTransaction(options);
			
			try {
				resultPetition = datastore.get(petitionKey);
				long nbSignatures = (long) resultPetition.getProperty("nbSignatures");
				++nbSignatures;
				resultPetition.setProperty("nbSignatures", nbSignatures);
				datastore.put(txn, resultPetition);
				
				
				HashSet<String> petitions = (HashSet<String>) signedPetition.getProperty("petitions");
				petitions.add(petitionId);
				signedPetition.setProperty("petitions", petitions);
				datastore.put(txn, signedPetition);
				
	
				txn.commit();
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
		return signedPetition;
	}
	
	
	
	@ApiMethod(path="petition", name="postPetition", httpMethod=HttpMethod.POST)
	public Entity postPetition(Entity petition, User user) throws UnauthorizedException {
		
		if (user  == null) {
			throw new UnauthorizedException("Invalid credentials");
		}	
		
		petition.setProperty("owner", user.getEmail());
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		datastore.put(petition);
		
		return petition;
	}
}
