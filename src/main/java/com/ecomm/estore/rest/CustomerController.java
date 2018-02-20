package com.ecomm.estore.rest;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecomm.estore.data.Customer;
import com.ecomm.estore.data.EmailAddress;
import com.ecomm.estore.data.repo.CustomerRepository;
import com.ecomm.estore.util.Constants;

@RestController
@RequestMapping("rest/customer")
public class CustomerController {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CustomerController.class);
	
	@Autowired
	private CustomerRepository customerRepo;
	
	// create
	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createProfile(@RequestBody(required=true) Customer customer) {
		LOG.debug("Creating customer <"+customer+">");

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(Constants.HTTP_HEADER_CACHE_CONTROL_NAME, Constants.HTTP_HEADER_CACHE_CONTROL_NO_CACHE);
		
		if (customer == null || customer.getEmailAddress() == null) {
			LOG.error("Invalid request without email id.");
			return new ResponseEntity<String>("{ \"status\": \"invalid customer create reqeust\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		Customer response = customerRepo.save(customer);
		
		return new ResponseEntity<Customer>(response, responseHeaders, HttpStatus.OK);
	}
	
	// read
	// TODO: Fix duplicate profile_id
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Iterable<Customer>> readProfile(@RequestParam(value="profile_id", required=true) String profileId) {
		LOG.debug("Fetching customers by <"+profileId+">");
		
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(Constants.HTTP_HEADER_CACHE_CONTROL_NAME, Constants.HTTP_HEADER_CACHE_CONTROL_NO_CACHE);
		responseHeaders.set(Constants.HTTP_HEADER_SURROGATE_CONTROL_NAME, Constants.HTTP_HEADER_SURROGATE_CONTROL_SERVER_STALE);
		
		// surrogate-key builder
		StringBuilder surrogateKeys = new StringBuilder();
		
		Iterable<Customer> response = customerRepo.findByEmailAddress(new EmailAddress(profileId));
		for (Customer cust : response) {
			surrogateKeys.append("customer-").append(cust.getId()).append(" ");
		}
		responseHeaders.set(Constants.HTTP_HEADER_SURROGATE_KEY_NAME, surrogateKeys.toString());
				
		return new ResponseEntity<Iterable<Customer>>(response, responseHeaders, HttpStatus.OK);
	}
	
	// read all
	// TODO: Fix duplicate profile_id
	@RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Iterable<Customer>> listProfiles() {
		LOG.debug("Fetching all customers");

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(Constants.HTTP_HEADER_CACHE_CONTROL_NAME, Constants.HTTP_HEADER_CACHE_CONTROL_NO_CACHE);
		responseHeaders.set(Constants.HTTP_HEADER_SURROGATE_CONTROL_NAME, Constants.HTTP_HEADER_SURROGATE_CONTROL_SERVER_STALE);
		responseHeaders.set(Constants.HTTP_HEADER_SURROGATE_KEY_NAME, "customers");
		
		Iterable<Customer> response = customerRepo.findAll();
		return new ResponseEntity<Iterable<Customer>>(response, responseHeaders, HttpStatus.OK);
	}
	
	// update
	// TODO: Fix duplicate profile_id
	@RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateProfile(@RequestBody(required=true) Customer customer, @RequestParam(value="profile_id", required=true) String profileId) {
		LOG.debug("Updating customer <"+profileId+"> with <"+customer+">");

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(Constants.HTTP_HEADER_CACHE_CONTROL_NAME, Constants.HTTP_HEADER_CACHE_CONTROL_NO_CACHE);
		
		Iterable<Customer> entities = customerRepo.findByEmailAddress(new EmailAddress(profileId));
		if (customer == null || customer.getEmailAddress() == null || entities == null || !entities.iterator().hasNext()) {
			LOG.error("Invalid request without email id.");
			return new ResponseEntity<String>("{ \"status\": \"no customer records found by profile id - "+profileId+"\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		
		Customer response = customerRepo.save(customer);
		
		return new ResponseEntity<Customer>(response, responseHeaders, HttpStatus.OK);
	}
	
	// delete
	// TODO: Fix duplicate profile_id
	@RequestMapping(method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> deleteProfile(@RequestParam(value="profile_id", required=true) String profileId) {
		LOG.debug("Deleting customers by <"+profileId+">");

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(Constants.HTTP_HEADER_CACHE_CONTROL_NAME, Constants.HTTP_HEADER_CACHE_CONTROL_NO_CACHE);
		
		Iterable<Customer> entities = customerRepo.findByEmailAddress(new EmailAddress(profileId));		
		if (entities == null || !entities.iterator().hasNext()) {
			LOG.error("No customer data found by profile id <"+profileId+">");
            return new ResponseEntity<String>("{ \"status\": \"no customer records found by profile id - "+profileId+"\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		
		long custId = entities.iterator().next().getId();
		customerRepo.delete(entities);
		
		return new ResponseEntity<String>("{ \"status\": \"success\"}", responseHeaders, HttpStatus.OK);
	}

}
