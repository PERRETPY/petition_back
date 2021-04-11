package perretpy;

import java.util.List;

import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
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
	public List<Entity> top100(@Nullable @Named("tag1") String tag1) {
		Query q = new Query("Petition");
		if (tag1 != null) {
			q.setFilter(new FilterPredicate("tag", FilterOperator.EQUAL, tag1));
		}
		System.out.println("QUERY = "+q.toString());
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
		return result;
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
	
	
	@ApiMethod(path = "auth", name = "auth", httpMethod = HttpMethod.GET)
	public List<Entity> auth(User user) throws UnauthorizedException {

		if (user == null) {
			throw new UnauthorizedException("Invalid credentials");
		}
		
		Query q = new Query("Petition");
		System.out.println("QUERY = "+q.toString());
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		PreparedQuery pq = datastore.prepare(q);
		List<Entity> result = pq.asList(FetchOptions.Builder.withLimit(100));
		return result;
	}
	
	
	
	
	
	
	
}
