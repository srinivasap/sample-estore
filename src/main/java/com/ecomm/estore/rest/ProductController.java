package com.ecomm.estore.rest;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ecomm.estore.data.Product;
import com.ecomm.estore.data.repo.ProductRepository;
import com.ecomm.estore.util.Constants;
import com.ecomm.estore.util.HttpPurgeClient;

@RestController
@RequestMapping("rest/product")
public class ProductController {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ProductController.class);
	
	@Autowired
	private ProductRepository productRepo;

	@Autowired
	@Value("${endpoint}")
	private String endpoint;
	
	// create
	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createProduct(@RequestBody(required=true) Product product) {
		LOG.debug("Creating product <"+product+">");

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(Constants.HTTP_HEADER_CACHE_CONTROL_NAME, Constants.HTTP_HEADER_CACHE_CONTROL_NO_CACHE);
		
		if (product == null) {
			LOG.error("No product to create.");
			return new ResponseEntity<String>("{ \"status\": \"No product to create.\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		
		Product response = productRepo.save(product);
		// purge cache
		HttpPurgeClient.call("http://"+endpoint+"/rest/product/search");
		
		return new ResponseEntity<Product>(response, responseHeaders, HttpStatus.OK);
	}
	
	// read
	@RequestMapping(value="/{product_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Product> readOrder(@PathVariable(value="product_id") long productId) {
		LOG.debug("Fetching product by id <"+productId+">");

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(Constants.HTTP_HEADER_CACHE_CONTROL_NAME, Constants.HTTP_HEADER_CACHE_CONTROL_NO_CACHE);
		responseHeaders.set(Constants.HTTP_HEADER_SURROGATE_CONTROL_NAME, Constants.HTTP_HEADER_SURROGATE_CONTROL_SERVER_STALE);
		
		Product response = productRepo.findOne(productId);
		return new ResponseEntity<Product>(response, responseHeaders, HttpStatus.OK);
	}
	
	// read all
	@RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Iterable<Product>> listProducts() {
		LOG.debug("Fetching all products");

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(Constants.HTTP_HEADER_CACHE_CONTROL_NAME, Constants.HTTP_HEADER_CACHE_CONTROL_NO_CACHE);
		responseHeaders.set(Constants.HTTP_HEADER_SURROGATE_CONTROL_NAME, Constants.HTTP_HEADER_SURROGATE_CONTROL_SERVER_STALE);
		
		Iterable<Product> response = productRepo.findAll();
		return new ResponseEntity<Iterable<Product>>(response, responseHeaders, HttpStatus.OK);
	}
	
	// update
	@RequestMapping(value="/{product_id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateProduct(@RequestBody(required=true) Product product, @PathVariable(value="product_id") long productId) {
		LOG.debug("Updating products <"+productId+"> with <"+product+">");

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(Constants.HTTP_HEADER_CACHE_CONTROL_NAME, Constants.HTTP_HEADER_CACHE_CONTROL_NO_CACHE);
		
		Product entity = productRepo.findOne(productId);
		if (product == null || entity == null) {
			LOG.error("Invalid product update request.");
			return new ResponseEntity<String>("{ \"status\": \"no product records found by id - "+productId+"\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		
		Product response = productRepo.save(product);
		// purge cache
		HttpPurgeClient.call("http://"+endpoint+"/rest/product/"+productId);
		HttpPurgeClient.call("http://"+endpoint+"/rest/product/search");
		
		return new ResponseEntity<Product>(response, responseHeaders, HttpStatus.OK);
	}
	
	// delete
	// TODO: Fix duplicate profile_id
	@RequestMapping(value="/{product_id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> deleteProduct(@PathVariable(value="product_id") long productId) {
		LOG.debug("Deleting product by <"+productId+">");
		
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(Constants.HTTP_HEADER_CACHE_CONTROL_NAME, Constants.HTTP_HEADER_CACHE_CONTROL_NO_CACHE);
		
		Product entity = productRepo.findOne(productId);	
		if (entity == null) {
			LOG.error("No product data found by id <"+productId+">");
            return new ResponseEntity<String>("{ \"status\": \"no product records found by id - "+productId+"\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		productRepo.delete(entity);
		// purge cache
		HttpPurgeClient.call("http://"+endpoint+"/rest/product/"+productId);
		HttpPurgeClient.call("http://"+endpoint+"/rest/product/search");
		
		return new ResponseEntity<String>("{ \"status\": \"success\"}", responseHeaders, HttpStatus.OK);
	}

}
