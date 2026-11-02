package bms.infrastructure;
import bms.domain.User;
import bms.domain.Role;
import bms.application.UserRepository;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Map;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

// Stores and retrieves user data in memory; this is a component responsible for storing data.
@Repository
public class InMemoryUserRepository implements UserRepository {

    // Create Memory Storage Space
    private final Map<Long, User> users = new ConcurrentHashMap<>();
    
    public InMemoryUserRepository() {
        User defaultUser = new User(//Create the specified initial user

            1234L,           

            "NUIG",            

            "UNIG@UNIG.com",    

            Role.SUPER_ADMIN//Role: Super Administrator

        );
        save(defaultUser);
    }

    @Override
    public void save(User user) {

        users.put(user.getId(), user);// user.getId() retrieves the user ID
                                    // users.put() saves the user to the Map
    }

    //Get All Users
    @Override
    public List<User> findAll() {

        List<User> result = new ArrayList<>(users.values());//Retrieve all users from the Map and convert them to an ArrayList
        
        result.sort(Comparator.comparing(User::getId));//Sort from smallest to largest

        return result;
    }

    //Find By ID
    @Override
    public Optional<User> findById(Long id) {

        return Optional.ofNullable(users.get(id));//Retrieve a user by user ID
    }

    @Override
    public boolean existsById(Long id) {

        return users.containsKey(id);//Does the specified user ID exist.true,The ID already exists; false, The ID does not exist
    }

    //Check for duplicate email addresses
    @Override
    public boolean existsByEmail(String email) {
        for (User user : users.values()) {

            //equalsIgnoreCase() Ignores case when comparing
            if (user.getEmail().equalsIgnoreCase(email)) {

                return true;
            }
        }
        return false;
    }
}
