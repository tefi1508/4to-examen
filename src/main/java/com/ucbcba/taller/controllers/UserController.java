package com.ucbcba.taller.controllers;



import com.ucbcba.taller.entities.Comment;
import com.ucbcba.taller.entities.User;
import com.ucbcba.taller.services.SecurityService;
import com.ucbcba.taller.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    //@Autowired
    //private UserValidator userValidator;

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String registrationInit(Model model) {
        model.addAttribute("user", new User());

        return "registration";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registration(@ModelAttribute("user") User user, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "registration";
        }

        userService.save(user);
        securityService.autologin(user.getUsername(), user.getPasswordConfirm());
        return "redirect:/bienvenidos";
    }


    @RequestMapping(value = "/saveEdit", method = RequestMethod.POST)
    public String editUser(User user) {

        userService.save(user);
        return "redirect:/showUserAccount";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model, String error, String logout) {
        if (error != null)
            model.addAttribute("error", "Your username and password is invalid.");

        if (logout != null)
            model.addAttribute("message", "You have been logged out successfully.");

        return "login";
    }

    @RequestMapping("/showUserAccount")
    String showUser(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        model.addAttribute("user", user);
        if (user.isAdmin()){
            return "showAdmin";
        }
        else {
            return "showUser";
        }
    }

    @RequestMapping("/showUserFromComment/{username}")
    String showUserFC(@PathVariable String username,Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        User use=userService.findByUsername(username);
        if(user.getId()==use.getId()){
            return "redirect:/showUserAccount";
        }
        else{
            model.addAttribute("user", use);
            return "showOtherUser";
        }
    }

    @RequestMapping("/showUserFromCommentPublic/{username}")
    String showUserFCP(@PathVariable String username,Model model) {
        User use=userService.findByUsername(username);
        model.addAttribute("user", use);
        return "showUserPublic";
    }


    @RequestMapping("/editUserAccount")
    String editUser(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        model.addAttribute("user", user);
        return "editUser";
    }

    @RequestMapping(value = {"/", "/welcome"}, method = RequestMethod.GET)
    public String welcome(Model model) {
        return "welcome";
    }
    @RequestMapping(value = {"/admin/"}, method = RequestMethod.GET)
    public String admin(Model model) {

        return "welcome";
    }
    @RequestMapping(value = {"/bienvenidos"}, method = RequestMethod.GET)
    public String welcome2(Model model) {
        return "bienvenidos";
    }

    @RequestMapping(value = {"/makeAdmin"}, method = RequestMethod.GET)
    public  String makeAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        boolean adm = true;
        user.setAdmin(adm);
        return "redirect:/bienvenidos";
    }

    int cantComentarios(User u){
        int tam=u.getComments().size();
        int cont=0;
        if (tam>0){
            for (Comment c : u.getComments()) {
                cont = cont + 1;
            }
        }

        return  cont;

    }

    List<User> ordenar(List<User> listUsers){
        User aux;
        for (int i=0; i<listUsers.size()-1; i++)
        {
            for (int j=i+1; j<listUsers.size(); j++)
            {
                if(cantComentarios(listUsers.get(i)) <cantComentarios(listUsers.get(j)))
                {
                    aux = listUsers.get(i);
                    listUsers.set(i,listUsers.get(j));
                    listUsers.set(j,aux);
                }
            }
        }
        return listUsers;
    }

    @RequestMapping(value = "/Order/User",method = RequestMethod.GET)
    public String orderRestaurants(/*@PathVariable Model model*/) {
        //Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<User> listUsers = (List<User>) userService.listAllUsers();
        //model.addAttribute("listUsers", listUsers);
        System.out.println(ordenar(listUsers).size());
        return "welcome";
    }

}