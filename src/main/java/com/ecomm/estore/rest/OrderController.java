package com.ecomm.estore.rest;

import java.util.Iterator;
import java.util.Set;

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

import com.ecomm.estore.data.Address;
import com.ecomm.estore.data.Customer;
import com.ecomm.estore.data.LineItem;
import com.ecomm.estore.data.Order;
import com.ecomm.estore.data.repo.CustomerRepository;
import com.ecomm.estore.data.repo.LineItemRepository;
import com.ecomm.estore.data.repo.OrderRepository;
import com.ecomm.estore.util.Constants;
import com.ecomm.estore.util.HttpGetClient;
import com.ecomm.estore.util.HttpPurgeClient;

@RestController
@RequestMapping("rest/order")
public class OrderController {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(OrderController.class);
	
	@Autowired
	private OrderRepository orderRepo;

	@Autowired
	private LineItemRepository lineItemRepo;
	
	@Autowired
	private CustomerRepository customerRepo;
	
	@Autowired
	@Value("${endpoint}")
	private String endpoint;

	@Autowired
	@Value("${fastly.service.id}")
	private String fastlyServiceId;
	
	// create
	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createOrder(@RequestBody(required=true) Order order) {
		LOG.debug("Creating order <"+order+">");
		
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(Constants.HTTP_HEADER_CACHE_CONTROL_NAME, Constants.HTTP_HEADER_CACHE_CONTROL_NO_CACHE);
		
		if (order == null) {
			LOG.error("No order to create.");
			return new ResponseEntity<String>("{ \"status\": \"No orders to create.\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		
		if (order.getCustomer() == null || order.getCustomer().getId() <= 0) {
			LOG.error("No customer data to create order.");
			return new ResponseEntity<String>("{ \"status\": \"No customer data to create order.\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		
		Customer customer = customerRepo.findOne(order.getCustomer().getId());
		if (customer.getAddresses() == null || customer.getAddresses().isEmpty()) {
			LOG.error("No customer shipping address to create order.");
			return new ResponseEntity<String>("{ \"status\": \"No customer shipping address to create order.\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		
		Address address = customer.getAddresses().iterator().next();
		// create new order with customer details 
		Order entity = new Order(customer, address);
		// add line item details to order
		Set<LineItem> items = order.getLineItems();
		//Set<LineItem> itemEntities = new HashSet<LineItem>();
		if (items != null && !items.isEmpty()) {
			for (LineItem item : items) {
				//itemEntities.add(lineItemRepo.save(item));
				entity.add(new LineItem(item.getProduct(), item.getAmount()));
				
			}
		}
		Order response = orderRepo.save(entity);
		
		// purge cache
		//HttpPurgeClient.call("http://"+endpoint+"/rest/order/search");
		HttpPurgeClient.call("http://"+endpoint+"/service/"+fastlyServiceId+"/orders");
		
		//update cache
		HttpGetClient.call("http://"+endpoint+"/rest/order/search");
		
		return new ResponseEntity<Order>(response, responseHeaders, HttpStatus.OK);
	}
	
	// read
	@RequestMapping(value="/{order_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Order> readOrder(@PathVariable(value="order_id") long orderId) {
		LOG.debug("Fetching orders by id <"+orderId+">");

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(Constants.HTTP_HEADER_CACHE_CONTROL_NAME, Constants.HTTP_HEADER_CACHE_CONTROL_NO_CACHE);
		responseHeaders.set(Constants.HTTP_HEADER_SURROGATE_CONTROL_NAME, Constants.HTTP_HEADER_SURROGATE_CONTROL_SERVER_STALE);
		
		Order response = orderRepo.findOne(orderId);
		
		// surrogate-key builder
		StringBuilder surrogateKeys = new StringBuilder();
		surrogateKeys.append("order-").append(orderId);
		surrogateKeys.append(" customer-").append(response.getCustomer().getId());
		for (LineItem item : response.getLineItems()) {
			surrogateKeys.append(" product-").append(item.getProduct().getId()).append(" ");
		}	
		responseHeaders.set(Constants.HTTP_HEADER_SURROGATE_KEY_NAME, surrogateKeys.toString());		
		
		return new ResponseEntity<Order>(response, responseHeaders, HttpStatus.OK);
	}
	
	// read all
	@RequestMapping(value = "/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Iterable<Order>> listOrders() {
		LOG.debug("Fetching all orders");

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(Constants.HTTP_HEADER_CACHE_CONTROL_NAME, Constants.HTTP_HEADER_CACHE_CONTROL_NO_CACHE);
		responseHeaders.set(Constants.HTTP_HEADER_SURROGATE_CONTROL_NAME, Constants.HTTP_HEADER_SURROGATE_CONTROL_SERVER_STALE);
		responseHeaders.set(Constants.HTTP_HEADER_SURROGATE_KEY_NAME, "orders");
		
		Iterable<Order> response = orderRepo.findAll();
		return new ResponseEntity<Iterable<Order>>(response, responseHeaders, HttpStatus.OK);
	}
	
	// update
	@RequestMapping(value="/{order_id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateOrder(@RequestBody(required=true) Order order, @PathVariable(value="order_id") long orderId) {
		LOG.debug("Updating order <"+orderId+"> with <"+order+">");

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(Constants.HTTP_HEADER_CACHE_CONTROL_NAME, Constants.HTTP_HEADER_CACHE_CONTROL_NO_CACHE);
				
		Order entity = orderRepo.findOne(orderId);
		if (order == null || entity == null) {
			LOG.error("Invalid order update request.");
			return new ResponseEntity<String>("{ \"status\": \"no order records found by id - "+orderId+"\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		
		// update line items quantity
		Set<LineItem> itemEntities = entity.getLineItems();
		Set<LineItem> items = order.getLineItems();
		// just update item quantities
		if (items != null && !items.isEmpty() && itemEntities != null) {
			Iterator<LineItem> itemEntityIter = itemEntities.iterator();
			for (LineItem item : items) {
				if (itemEntityIter.hasNext()) {
					itemEntityIter.next().setAmount(item.getAmount());
				}
			}
		}
		Order response = orderRepo.save(entity);

		// purge cache
		HttpPurgeClient.call("http://"+endpoint+"/service/"+fastlyServiceId+"/order-"+orderId);
		HttpPurgeClient.call("http://"+endpoint+"/service/"+fastlyServiceId+"/orders");
		//HttpPurgeClient.call("http://"+endpoint+"/rest/order/"+orderId);
		//HttpPurgeClient.call("http://"+endpoint+"/rest/order/search");
		
		//update cache
		HttpGetClient.call("http://"+endpoint+"/rest/order/"+orderId);
		HttpGetClient.call("http://"+endpoint+"/rest/order/search");
		
		return new ResponseEntity<Order>(response, responseHeaders, HttpStatus.OK);
	}
	
	// delete
	@RequestMapping(value="/{order_id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> deleteOrder(@PathVariable(value="order_id") long orderId) {
		LOG.debug("Deleting order by <"+orderId+">");

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set(Constants.HTTP_HEADER_CACHE_CONTROL_NAME, Constants.HTTP_HEADER_CACHE_CONTROL_NO_CACHE);
		
		Order entity = orderRepo.findOne(orderId);	
		if (entity == null) {
			LOG.error("No order data found by id <"+orderId+">");
            return new ResponseEntity<String>("{ \"status\": \"no order records found by id - "+orderId+"\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		orderRepo.delete(entity);

		// purge cache
		HttpPurgeClient.call("http://"+endpoint+"/service/"+fastlyServiceId+"/order-"+orderId);
		HttpPurgeClient.call("http://"+endpoint+"/service/"+fastlyServiceId+"/orders");
		//HttpPurgeClient.call("http://"+endpoint+"/rest/order/"+orderId);
		//HttpPurgeClient.call("http://"+endpoint+"/rest/order/search");

		//update cache
		HttpGetClient.call("http://"+endpoint+"/rest/order/"+orderId);
		HttpGetClient.call("http://"+endpoint+"/rest/order/search");
		
		return new ResponseEntity<String>("{ \"status\": \"success\"}", responseHeaders, HttpStatus.OK);
	}

}
