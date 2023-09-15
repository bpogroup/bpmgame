package nl.tue.bpmgame.bpmgamegui.controllers;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import nl.tue.bpmgame.dataaccess.model.GameGroup;
import nl.tue.bpmgame.dataaccess.model.Role;
import nl.tue.bpmgame.dataaccess.model.User;

import nl.tue.bpmgame.bpmgamegui.dao.GameGroupDAO;
import nl.tue.bpmgame.bpmgamegui.dao.RoleDAO;
import nl.tue.bpmgame.bpmgamegui.dao.UserDAO;
import nl.tue.bpmgame.bpmgamegui.services.ConstantProviderService;
import nl.tue.bpmgame.bpmgamegui.services.EmailService;
import nl.tue.bpmgame.bpmgamegui.viewmodels.Email;

@Controller
public class UserController {

	@Autowired
	private UserDAO userDao;
	@Autowired
	private RoleDAO roleDao;
	@Autowired
	private GameGroupDAO gameGroupDao;
	@Autowired
	private EmailService emailService;
	
	/**
	 * Displays the registration page (also see the POST version of this method)
	 * 
	 * @return a webpage
	 */
	@RequestMapping(value=ConstantProviderService.userRegister, method=RequestMethod.GET)
	public String register(){
		return "register";
	}

	/**
	 * Processes the registration request. There are the following options:
	 * - user not in the same timezone => reject registration message
	 * - user already registered => reject registration message
	 * - user is blocked => reject registration message
	 * - user has not yet confirmed e-mail => resend confirmation request and reject registration message
	 * - user can be registered => register the user, send confirmation request and success registration message 
	 * 
	 * @param request the HTTP request
	 * @param name details of the registration
	 * @param email details of the registration
	 * @param password details of the registration
	 * @param captcha a captcha (basically this is a honeypot)
	 * @return a message displaying the result of the registration
	 */
	@RequestMapping(value=ConstantProviderService.userRegister, method=RequestMethod.POST)
	public ModelAndView register(HttpServletRequest request, 
			@RequestParam("name") String name, 
			@RequestParam("email") String email, 
			@RequestParam("password") String password){
		ModelAndView mav = new ModelAndView();
		mav.setViewName("response");

		if (!(email.endsWith("tue.nl") || email.endsWith("sap.com"))){
			mav.addObject("alertType","alert-danger");
			mav.addObject("message", "You must register with an e-mail address of your organization. Your organization must be registered to use the BPM Game.");
			return mav;			
		}
		
		User user = userDao.findByEmail(email);
		if (user != null){
			if (user.isConfirmed()){
				mav.addObject("alertType","alert-warning");
				mav.addObject("message", "You are already registered.");
				return mav;
			}else{				
				emailService.sendEmail(Email.registrationEmail(user, request));
				mav.addObject("alertType","alert-warning");
				mav.addObject("message", "You are already registered, but your e-mail has not yet been confirmed. We have resent the request for confirmation.");
				return mav;				
			}
		}

		Role r = roleDao.findRoleByName(Role.ROLE_USER);
		if (r == null) {
			r = new Role();
			r.setName(Role.ROLE_USER);
			roleDao.create(r);
		}

		user = new User();
		userDao.create(user);
		long id = user.getId();
		user.setName(name);
		user.setEmail(email);
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		user.setPassword(passwordEncoder.encode(password));
		user.setUid(new BigInteger(130, new SecureRandom()).toString(32)+"_"+Long.toString(id));
		user.setConfirmed(false);
		user.getRoles().add(r);
		userDao.update(user);

		emailService.sendEmail(Email.registrationEmail(user, request));

		mav.addObject("alertType","alert-success");
		mav.addObject("message", "You are now registered. Please check your e-mail to confirm your account.");
		return mav;			
	}

	/**
	 * Confirm a users e-mail (also see register POST method). There are a few possibilities:
	 * - UID has is unknown => error
	 * - user with UID is blocked => error
	 * - e-mail is confirmed => success 
	 * 
	 * @param uid the UID hash for whom to confirm the e-mail address
	 * @param request the HTTP request
	 * @return an error or success message
	 */
	@RequestMapping(ConstantProviderService.userConfirm + "/{uid}")
	public ModelAndView confirmEmail(@PathVariable("uid") String uid, HttpServletRequest request){
		ModelAndView mav = new ModelAndView();
		mav.setViewName("response");

		User user = userDao.findByUid(uid);
		if (user == null){
			mav.addObject("alertType","alert-danger");
			mav.addObject("message", "Your registration was not recognized.");
			return mav;							
		}
		user.setConfirmed(true);
		userDao.update(user);

		mav.addObject("alertType","alert-success");
		mav.addObject("message", "Your e-mail address was confirmed. You can now log in.");
		return mav;			
	}
	
	/**
	 * Displays the sign in page. The actual sign in is handled by Spring and this method may be called by Spring with a failure reason.
	 * If the e-mail or password is invalid, the method returns an error.
	 * If the user is blocked, the method returns an error.
	 * If the users e-mail is not confirmed, the confirmation request is re-send and an error is returned.
	 *  
	 * @param request the HTTP request
	 * @param failure the reason for which a possible previous sign in failed
	 * 
	 * @return a webpage
	 */
	@RequestMapping(value=ConstantProviderService.userSignin, method=RequestMethod.GET)
	public ModelAndView signIn(HttpServletRequest request,  @RequestParam(value = "failure", required = false) String failure){
		ModelAndView model = new ModelAndView();
		model.setViewName("signin");
		if (failure != null) {
		    String userName = (String) request.getSession().getAttribute(UserNameCachingAuthenticationFailureHandler.LAST_USERNAME_KEY);
		    User user = userDao.findByEmail(userName);
			if (user == null){
				model.addObject("failure", "Invalid e-mail.");				
			}else if (!user.isConfirmed()){
				emailService.sendEmail(Email.registrationEmail(user, request));
				model.addObject("failure", "You are already registered, but your e-mail address is not yet confirmed. We sent another confirmation request to your e-mail address.");
			}else{
				model.addObject("failure", "Invalid password.");
			}
		}

		return model;
	}

	/**
	 * Signs the user out and returns to the home page.
	 * 
	 * @return the home page
	 */
	@RequestMapping(value=ConstantProviderService.userSignout, method=RequestMethod.GET)
	public ModelAndView signOut(){
		ModelAndView model = new ModelAndView();
		model.setViewName("home");  
		return model;
	}

	/**
	 * Displays the change password page (also see the POST for this method)
	 * 
	 * @return a webpage
	 */
	@RequestMapping(value=ConstantProviderService.userChangePassword, method=RequestMethod.GET)
	public String changePassword(){
		return "changepassword";
	}

	/**
	 * Changes the password (also see the GET for this method)
	 * 
	 * @param request the HTTP request
	 * @param oldPassword the old password of the user
	 * @param password the new password of the user
	 * @return a confirmation of the password change or an error if the original password was not corect
	 */
	@RequestMapping(value=ConstantProviderService.userChangePassword, method=RequestMethod.POST)
	public ModelAndView changePassword(HttpServletRequest request, 
			@RequestParam("oldpassword") String oldPassword,
			@RequestParam("password") String password){
		ModelAndView mav = new ModelAndView();
		mav.setViewName("response");

		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		
		User u = userDao.findByEmail(request.getUserPrincipal().getName());
		if (!passwordEncoder.matches(oldPassword, u.getPassword())){
			mav.addObject("alertType","alert-danger");
			mav.addObject("message", "The original password that you provided is incorrect.");
			return mav;										
		}
		if (u.getName().startsWith("Reviewer")){
			mav.addObject("alertType","alert-danger");
			mav.addObject("message", "You cannot change your password.");
			return mav;										
		}
		
		u.setPassword(passwordEncoder.encode(password));
		userDao.update(u);

		mav.addObject("alertType","alert-success");
		mav.addObject("message", "You password was changed.");
		return mav;			
	}

	/**
	 * Displays the 'password forgotten' page (also see the POST for this method).
	 *  
	 * @return a webpage
	 */
	@RequestMapping(value=ConstantProviderService.userPassword, method=RequestMethod.GET)
	public String password(){
		return "password";
	}

	/**
	 * Creates a temporary password for the user and sends that password by e-mail (also see the GET for this method).
	 * If the user is blocked, gives an error
	 * If the user does not exit, gives an error
	 * 
	 * @param request the HTTP request
	 * @param email the e-mail address of the user for whom to create a new password
	 * @return a success or failure message depending on the result
	 */
	@RequestMapping(value=ConstantProviderService.userPassword, method=RequestMethod.POST)
	public ModelAndView password(HttpServletRequest request, 
			@RequestParam("email") String email){
		ModelAndView mav = new ModelAndView();
		mav.setViewName("response");

		User user = userDao.findByEmail(email);
		if (user == null){
			mav.addObject("alertType","alert-danger");
			mav.addObject("message", "We do not have a record of a user with the provided e-mail address.");
			return mav;										
		}
		if (user.getName().startsWith("Reviewer")){
			mav.addObject("alertType","alert-danger");
			mav.addObject("message", "You cannot change your password.");
			return mav;										
		}

		String newPassword = getRandomPass();
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		user.setPassword(passwordEncoder.encode(newPassword));
		userDao.update(user);
		emailService.sendEmail(Email.passwordEmail(user, newPassword));

		mav.addObject("alertType","alert-success");
		mav.addObject("message", "We sent a new password to your e-mail address.");
		return mav;			
	}
	
	private String getRandomPass() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random(System.currentTimeMillis());
        while (salt.length() < 10) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
	
	private ModelAndView populateGroupView(GameGroup group) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("group");
		if (group == null) {
			mav.addObject("groupName", null);
			mav.addObject("groupId", null);
			mav.addObject("groupMembers", null);
		} else {
			mav.addObject("groupName", group.getName());
			mav.addObject("groupId", group.getId());
			String memberNames[] = new String[group.getUsers().size()];
			int i = 0;
			for (User member: group.getUsers()) {
				memberNames[i] = member.getName();
				i++;
			}
			mav.addObject("groupMembers", memberNames);			
		}
		return mav;
	}
	
	@RequestMapping(value=ConstantProviderService.groupManage, method=RequestMethod.GET)	
	public ModelAndView manageGroup(HttpServletRequest request){
		User u = userDao.findByEmail(request.getUserPrincipal().getName());		
		ModelAndView mav = populateGroupView(u.getGroup());
		return mav;
	}
	
	@RequestMapping(value=ConstantProviderService.groupCreate, method=RequestMethod.POST)
	public ModelAndView createGroup(HttpServletRequest request, 
			@RequestParam("groupName") String groupName){
		User u = userDao.findByEmail(request.getUserPrincipal().getName());
		
		GameGroup group = gameGroupDao.findByName(groupName);
		if (group != null) {
			ModelAndView mav = populateGroupView(u.getGroup());
			mav.addObject("failure", "A group by that name already exists.");
			return mav;
		}
		
		group = new GameGroup();
		group.setName(groupName);
		gameGroupDao.create(group);
		
		u.setGroup(group);
		userDao.update(u);
		group.addUser(u);
		gameGroupDao.update(group);

		return populateGroupView(u.getGroup());
	}
	
	@RequestMapping(value=ConstantProviderService.groupLeave, method=RequestMethod.GET)
	public ModelAndView leaveGroup(HttpServletRequest request){
		User u = userDao.findByEmail(request.getUserPrincipal().getName());
		
		GameGroup group = u.getGroup();
		if (group != null) {			
			group.removeUser(u);
			gameGroupDao.update(group);			
			u.setGroup(null);
			userDao.update(u);
		}
		
		ModelAndView mav = populateGroupView(u.getGroup());
		return mav;
	}
	
	@RequestMapping(value=ConstantProviderService.groupAdd, method=RequestMethod.POST)
	public ModelAndView addToGroup(HttpServletRequest request, 
			@RequestParam("email") String email){
		User u = userDao.findByEmail(request.getUserPrincipal().getName());		

		GameGroup group = u.getGroup();
		if (group != null) {
			User newMember = userDao.findByEmail(email);
			if (newMember == null) {
				ModelAndView mav = populateGroupView(u.getGroup());
				mav.addObject("failure", "No user found with that e-mail address.");
				return mav;
			}else if (newMember.getGroup() != null) {
				ModelAndView mav = populateGroupView(u.getGroup());
				mav.addObject("failure", "The user cannot be added to your group, because it is already a member of a group.");
				return mav;
			}else {
				newMember.setGroup(group);
				userDao.update(newMember);
				group.addUser(newMember);
				gameGroupDao.update(group);				
			}			
		}
		
		return populateGroupView(u.getGroup());
	}
	
}
