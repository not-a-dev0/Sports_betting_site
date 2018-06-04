package pl.coderslab.sports_betting.Controller.RestApiController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.coderslab.sports_betting.Entity.Country;
import pl.coderslab.sports_betting.Service.CountryService;

import java.util.List;

@RestController
@RequestMapping("/api/country")
public class CountryAPIController {

    @Autowired
    CountryService countryService;

    @RequestMapping(path = "/all")
    public	List<Country> getAllCountries() {
        return	countryService.allCountries();
    }

    /*
    countries: 3,4
     */
    @RequestMapping(path = "/{id}")
    public	Country getCountryById(@PathVariable Long id) {
        return countryService.getCountryById(id);
    }
}
