package pl.coderslab.sports_betting.Service.Security.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.coderslab.sports_betting.Entity.Football.FootballTeam;
import pl.coderslab.sports_betting.Entity.Lol.LolTeam;
import pl.coderslab.sports_betting.Entity.Role;
import pl.coderslab.sports_betting.Entity.User;
import pl.coderslab.sports_betting.Repository.Fotball.FootballBetRepository;
import pl.coderslab.sports_betting.Repository.Fotball.FootballTeamRepository;
import pl.coderslab.sports_betting.Repository.Lol.LolBetRepository;
import pl.coderslab.sports_betting.Repository.Lol.LolTeamRepository;
import pl.coderslab.sports_betting.Repository.General.UserRepository;
import pl.coderslab.sports_betting.Service.Security.Service.RoleService;
import pl.coderslab.sports_betting.Service.Security.Service.UserService;


import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    FootballTeamRepository footballTeamRepository;
    @Autowired
    LolTeamRepository lolTeamRepository;
    @Autowired
    FootballBetRepository footballBetRepository;
    @Autowired
    LolBetRepository lolBetRepository;

    private static final String DEFAULT_USER_ROLE_NAME = "ROLE_USER";
    private static final String ADMIN = "ROLE_ADMIN";

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final BCryptPasswordEncoder passwordEncoder;


    public UserServiceImpl(UserRepository userRepository,
                           RoleService roleService,
                           BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    /**
     * Method is geting number of all users
     * @return number of users in database
     */
    @Override
    public long getNumUsers() {
        return userRepository.count();
    }

    /**
     * Method is looking for all users
     * @return list of all users
     */
    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Method is looking for all users without actual user (looged one)
     * @return list of all users without actual one
     */
    @Override
    public List<User> findAllWithoutActiveUser() {
        List<User> list = findAll();
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        list.remove(user);
        return list;
    }

    /**
     * Method is looking for single user by username (in this case this is e-mail)
     * @param username username
     * @return object user
     */
    @Override
    public User findByUserName(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Method is saving user into databse
     * First password in encoded by BcCrypt
     * variable enabled is setted by true
     * money is setted by 100 (initial bonus)
     * role is setted to default - user
     * roles are saved into user and user is saved into db
     * @param user Object user
     */
    @Override
    public void saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setMoney(BigDecimal.valueOf(100));

        Role role = roleService.getOrCreate(DEFAULT_USER_ROLE_NAME);
        Set<Role> roles = new HashSet<>(Collections.singletonList(role));
        user.setRoles(roles);
        userRepository.save(user);
    }

    /**
     * Method is saving admin into databse
     * First password in encoded by BcCrypt
     * variable enabled is setted by true
     * money is setted by 9999999
     * role is setted to default - user
     * roles are saved into user and user(ADMIN) is saved into db
     * @param user
     */
    @Override
    public void saveAdmin(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setMoney(BigDecimal.valueOf(99999999));
        Role role = roleService.getOrCreate(ADMIN);
        Set<Role> roles = new HashSet<>(Collections.singletonList(role));
        user.setRoles(roles);
        userRepository.save(user);
    }

    /**
     * This method allows to change several variables of object User,
     * password is encrypted by BcCrypt
     * @param username username (in this case it is email)
     * @param firstName String firstname
     * @param lastName String lastname
     * @param nick String nick
     * @param password String password
     */

    @Override
    public void editUser(String username, String firstName, String lastName, String nick, String password){
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setNick(nick);
        user.setPassword(password);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    /**
     * Method is saving first name, last name and nick of user into session
     * to make access to these variables easy
     * @param httpSession
     */
    public void userDetailsToSession (HttpSession httpSession){
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        httpSession.setAttribute("firstName", user.getFirstName());
        httpSession.setAttribute("lastName", user.getLastName());
        httpSession.setAttribute("nick", user.getNick());
    }

    /**
     * Method deletes user by Id
     * @param id id of selected user
     */
    @Override
    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }

    /**
     * Methods adds football favorites into user account
     * First is looking for active user then is getting user favorite football teams
     * Then is getting football team by id (new favorite team for user)
     * if team is not actually in favorites then set it into users and save databae
     * @param teamId Id of team to save into user favorites
     */
    @Override
    public void addToFootballFavorites(Long teamId){
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        List<FootballTeam> footballTeams = user.getFavoriteFootballTeams();
        FootballTeam footballTeam = footballTeamRepository.getOne(teamId);
        if (!(footballTeams.contains(footballTeam))){
            footballTeams.add(footballTeam);
            user.setFavoriteFootballTeams(footballTeams);
            userRepository.save(user);
        }
    }

    /**
     * Methods adds lol favorites into user account
     * First is looking for active user then is getting user favorite lol teams
     * Then is getting lol team by id (new favorite team for user)
     * if team is not actually in favorites then set it into users and save databae
     * @param teamId Id of team to save into user favorites
     */
    public void addToLolFavorites(Long teamId){
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        List<LolTeam> lolTeams = user.getFavoriteLolTeams();
        LolTeam lolTeam = lolTeamRepository.getOne(teamId);
        if (!(lolTeams.contains(lolTeam))){
            lolTeams.add(lolTeam);
            user.setFavoriteLolTeams(lolTeams);
            userRepository.save(user);
        }
    }


    @Override
    public void removeFromFavorite(Long id){
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        List<FootballTeam> footballTeams = user.getFavoriteFootballTeams();
        FootballTeam footballTeam = footballTeamRepository.getOne(id);
        footballTeams.remove(footballTeam);
        user.setFavoriteFootballTeams(footballTeams);
        userRepository.save(user);
    }

    public void addToFriends(Long id){
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        List<User> friends = user.getFriends();
        User friend = userRepository.getOne(id);
        if (!(friends.contains(friend))){
            friends.add(friend);
            user.setFriends(friends);
            userRepository.save(user);
        }
    }

    public List<User> getAllUserFriends(){
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        List<User> friends = user.getFriends();
        List<User> friendOf = user.getFriendOf();
        List<User> all = new ArrayList<>();
        for (User load: friends) {
            if (!(all.contains(load))){
                all.add(load);
            } }

        for (User load: friendOf) {
            if (!(all.contains(load))){
                all.add(load);
        } }
        return all;
    }

    public String checkFavorite(){
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        int footballBetsSize = footballBetRepository.findAll().size();
        int lolBetsSize = lolBetRepository.findAll().size();
        String favorite;
        if (footballBetsSize > lolBetsSize){
            favorite = "football";
        } else if (lolBetsSize > footballBetsSize){
            favorite = "lol";
        } else {
            favorite = "no";
        }
        return  favorite;
    }

    public User findById(Long id){
        return userRepository.getOne(id);
    }

}
