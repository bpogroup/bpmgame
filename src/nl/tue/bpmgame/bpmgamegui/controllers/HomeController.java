package nl.tue.bpmgame.bpmgamegui.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

	/**
	 * Displays the home page.
	 * 
	 * @return a webpage.
	 */
	@RequestMapping(value={"","/"})
	public ModelAndView home(){
		ModelAndView mav = new ModelAndView();
		mav.setViewName("home");
		//mav.addObject("price", price);
		return mav;
	}
}
