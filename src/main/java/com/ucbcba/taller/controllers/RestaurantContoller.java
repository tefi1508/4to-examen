package com.ucbcba.taller.controllers;


import com.sun.org.apache.xpath.internal.operations.Mod;
import com.ucbcba.taller.entities.*;
import com.ucbcba.taller.services.CategoryService;
import com.ucbcba.taller.services.CityService;
import com.ucbcba.taller.services.RestaurantService;
import com.ucbcba.taller.services.UploadFileService;
import com.ucbcba.taller.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

@Controller
public class RestaurantContoller {

    private RestaurantService restaurantService;
    private CategoryService categoryService;
    private CityService cityService;

    @Autowired
    private UserService userService;

    @Autowired
    public void setRestaurantService(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @Autowired
    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Autowired
    public void setCityService(CityService cityService) {
        this.cityService = cityService;
    }

    @Autowired
    private UploadFileService uploadFileService;

    String calcularRankin(Restaurant rest){
        int tam=rest.getComments().size();
        int cont=0;
        int rankin=0;
        String estrellas="✰ ✰ ✰ ✰ ✰";
        if (tam>0)
        {
            for (Comment c : rest.getComments())
            {
                cont = cont + c.getEstrellas();
            }
            rankin=cont/tam;
        }
        if (rankin == 1){
            estrellas="★ ✰ ✰ ✰ ✰";
        }
        if (rankin == 2){
            estrellas="★ ★ ✰ ✰ ✰";
        }
        if (rankin == 3){
            estrellas="★ ★ ★ ✰ ✰";
        }
        if (rankin == 4){
            estrellas="★ ★ ★ ★ ✰";
        }
        if (rankin == 5){
            estrellas="★ ★ ★ ★ ★";
        }
        return estrellas;
    }

    int promedioRankin(Restaurant rest){
        int tam=rest.getComments().size();
        int cont=0;
        int rankin=0;
        if (tam>0){
            for (Comment c : rest.getComments())
            {
                cont = cont + c.getEstrellas();
            }
            rankin=cont/tam;
        }

        return  rankin;

    }

    List<Restaurant> ordenar(List<Restaurant> listRes){
        Restaurant aux;
        for (int i=0; i<listRes.size()-1; i++)
        {
            for (int j=i+1; j<listRes.size(); j++)
            {
                if(promedioRankin(listRes.get(i)) <promedioRankin(listRes.get(j)))
                {
                    aux = listRes.get(i);
                    listRes.set(i,listRes.get(j));
                    listRes.set(j,aux);
                }
            }
        }
        return listRes;
    }


    @RequestMapping(value = "/restaurant", method = RequestMethod.POST)
    String save(@RequestParam("file")MultipartFile file,Restaurant restaurant) {
        try {

            uploadFileService.saveFile(file ,restaurant.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        restaurantService.saveRestaurant(restaurant);
        return "redirect:/Restaurants";
    }


    @RequestMapping("/newRestaurant")
    String newRestaurant(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        if(user.isAdmin()) {
            Iterable<Category> categories = categoryService.listAllCategories();
            model.addAttribute("categories", categories);
            Iterable<City> cities = cityService.listAllCities();
            model.addAttribute("cities", cities);
            return "newRestaurant";
        }
        else {
            return "redirect:/Restaurants";
        }
    }

    @RequestMapping(value = "/Order/Restaurants/{x}",method = RequestMethod.GET)
    public String orderRestaurants(@PathVariable String x, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        List<Restaurant> restList = (List<Restaurant>) restaurantService.listAllRestaurants();
        for (Restaurant rest: restList){
            rest.setRankin(calcularRankin(rest));
        }
        model.addAttribute("restList", ordenar(restList));
        if(x.equals("public"))
        {
            return "showRestaurantsPublic";

        }
        if(x.equals("user"))
        {
            return "showRestaurantsUser";
        }
        if(x.equals("admin"))
        {
            return "showRestaurantsUser";
        }
        System.out.println(x);
        System.out.println(ordenar(restList).size());
        return "welcome";
    }

    @RequestMapping(value = "/Restaurants",method = RequestMethod.GET)
    public String showRest(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        Iterable<Restaurant> restList = restaurantService.listAllRestaurants();
        for (Restaurant rest: restList){
            rest.setRankin(calcularRankin(rest));
        }
        model.addAttribute("restList", restList);
        return "showRestaurants";
    }


    @RequestMapping(value = "/publicRestaurants",method = RequestMethod.GET)
    public String showRestPub(Model model) {
        /*Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());*/
        Iterable<Restaurant> restList = restaurantService.listAllRestaurants();
        for (Restaurant rest: restList){
                rest.setRankin(calcularRankin(rest));
        }
        model.addAttribute("restList", restList);
        return "showRestaurantsPublic";
    }

    @RequestMapping(value = "/userRestaurants",method = RequestMethod.GET)
    public String showRestUser(Model model) {
        /*Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());*/
        Iterable<Restaurant> restList = restaurantService.listAllRestaurants();
        for (Restaurant rest: restList){
            rest.setRankin(calcularRankin(rest));
        }
        model.addAttribute("restList", restList);
        return "showRestaurantsUser";
    }



    @RequestMapping(value = "/showRestaurants",method = RequestMethod.GET)
    public String showRestaurants(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        if(user.isAdmin()) {
            return "redirect:/Restaurants";
        }
        else{
            return "redirect:/userRestaurants";
        }
    }




    @RequestMapping("/showRestaurant/{id}")
    String showRes(@PathVariable Integer id, Model model) {
        Restaurant rest = restaurantService.getRestaurant(id);
        model.addAttribute("rest", rest);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        model.addAttribute("estrellas",calcularRankin(rest) );
        model.addAttribute("canComment",rest.getUserID(user.getId()));
        if(restaurantService.getRestaurant(id).getComments().contains(user)) {
            model.addAttribute("comment",new Comment(rest,user));
            model.addAttribute("use", user);
            restaurantService.getRestaurant(id).setCommentUs(user.getId(),new Comment(rest, user));
        }
        else{
            model.addAttribute("comment",new Comment(rest,user));
            model.addAttribute("use", user);
        }
        return "showRestaurant";
    }



    @RequestMapping("/showRestaurantPublic/{id}")
    String showRest(@PathVariable Integer id, Model model) {
        Restaurant rest = restaurantService.getRestaurant(id);
        model.addAttribute("rest", rest);
        model.addAttribute("estrellas",calcularRankin(rest) );
        return "showRestaurantPublic";
    }

    @RequestMapping("/showRestaurantAdmin/{id}")
    String showRestAdmin(@PathVariable Integer id, Model model) {
        Restaurant rest = restaurantService.getRestaurant(id);
        model.addAttribute("rest", rest);
        model.addAttribute("estrellas",calcularRankin(rest) );
        return "showRestaurantAdmin";
    }

    @RequestMapping("/showRestaurantUser/{id}")
    String showRestUser(@PathVariable Integer id, Model model) {
        Restaurant rest = restaurantService.getRestaurant(id);
        model.addAttribute("rest", rest);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        if(user.isAdmin()){
            return "redirect:/showRestaurantAdmin/"+id;
        }
        else{
            return "redirect:/showRestaurant/"+id;
        }
    }

    @RequestMapping("/deleteRestaurant/{id}")
    String delete(@PathVariable Integer id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        if(user.isAdmin()) {
            restaurantService.deleteRestaurant(id);
        }
        return "redirect:/Restaurants";
    }

    @RequestMapping("/editRestaurant/{id}")
    String editPost(@PathVariable Integer id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        if(user.isAdmin()) {
            Restaurant rest = restaurantService.getRestaurant(id);
            Iterable<Category> categories = categoryService.listAllCategories();
            model.addAttribute("categories", categories);
            Iterable<City> cities = cityService.listAllCities();
            model.addAttribute("cities", cities);
            model.addAttribute("rest", rest);
            return "editRestaurant";
        }
        else{
            return "redirect:/Restaurants";
        }
    }

    @RequestMapping("/like/{id}")
    String like(@PathVariable Integer id) {
        Restaurant res = restaurantService.getRestaurant(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        if (res.findUserLike(user) == false) {
            res.addLikke(user);
            res.setLikes(res.getLikes()+1);
            restaurantService.saveRestaurant(res);
        }
        return "redirect:/showRestaurant/" + res.getId();
    }

    @RequestMapping(value="/search/{name}", method = RequestMethod.GET)

    public String buscarRestaurant(@PathVariable("name") String name, Model model){
        //Restaurant restaurant = restaurantService.findRestaurantByName(name);
//        model.addAttribute("restaurant", restaurant);
//        return "redirect:/showRestaurant/"+ restaurant.getId();

        List<Restaurant> restaurants=(List<Restaurant>)restaurantService.listAllRestaurants();
        List<Restaurant> aux= new ArrayList<>();
        for (Restaurant restaurant : restaurants){
            if (restaurant.getName().contains(name)){
                aux.add(restaurant);
            }
        }
        for (Restaurant rest: aux){
            rest.setRankin(calcularRankin(rest));
        }
        model.addAttribute("restList", aux);
        return "showRestaurantsUser";

    }

    @RequestMapping(value="/publicSearch/{name}", method = RequestMethod.GET)
    public String buscarRestaurantPublic(@PathVariable("name") String name, Model model){
//        Restaurant restaurant = restaurantService.findRestaurantByName(name);
//        model.addAttribute("restaurant", restaurant);
//        return "redirect:/showRestaurantPublic/"+ restaurant.getId();
        List<Restaurant> restaurants=(List<Restaurant>)restaurantService.listAllRestaurants();
        List<Restaurant> aux= new ArrayList<>();
        for (Restaurant restaurant : restaurants){
            if (restaurant.getName().contains(name)){
                aux.add(restaurant);
            }
        }
        for (Restaurant rest: aux){
            rest.setRankin(calcularRankin(rest));
        }
        model.addAttribute("restList", aux);
        return "showRestaurantsPublic";
    }

    @RequestMapping(value="/categorysearch/{category_id}")
    public String buscar(@PathVariable Integer category_id, Model model){
       Category category = categoryService.getCategory(category_id);
       List<Restaurant> restList = category.getRestaurantList();
        for (Restaurant rest: restList){
            rest.setRankin(calcularRankin(rest));
        }
        model.addAttribute("restList", restList);
       return "showRestaurantsPublic";
   }

    @RequestMapping(value="/usercategorysearch/{category_id}")
    public String buscarusuario(@PathVariable Integer category_id, Model model){
        Category category = categoryService.getCategory(category_id);
        List<Restaurant> restList = category.getRestaurantList();
        model.addAttribute("restList", restList);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        for (Restaurant rest: restList){
            rest.setRankin(calcularRankin(rest));
        }
        if(user.isAdmin()){
            return "showRestaurants";
        }
        else{
            return "showRestaurantsUser";
        }
    }

    @RequestMapping(value="/citysearch/{city_id}")
    public String buscarCiudad(@PathVariable Integer city_id, Model model){
        City city=cityService.getCity(city_id);
        List<Restaurant> resList=city.getRestaurantList();
        for (Restaurant rest: resList){
            rest.setRankin(calcularRankin(rest));
        }
        model.addAttribute("restList", resList);
        return  "showRestaurantsPublic";
    }

    @RequestMapping(value="/usercitysearch/{city_id}")
    public String buscarCiudadUser(@PathVariable Integer city_id, Model model){
        City city=cityService.getCity(city_id);
        List<Restaurant> resList=city.getRestaurantList();
        for (Restaurant rest: resList){
            rest.setRankin(calcularRankin(rest));
        }
        model.addAttribute("restList", resList);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName());
        if(user.isAdmin()){
            return "showRestaurants";
        }
        else{
            return "showRestaurantsUser";
        }
    }

}
