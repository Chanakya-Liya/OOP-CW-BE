package service;

import entity.Customer;
import repository.CustomerRepository;

import java.util.List;

public class CustomerService {
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(int id) {
        return customerRepository.getReferenceById((long) id);
    }

    public Customer addCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public void deleteCustomerById(long id) {
        if(customerRepository.existsById(id)){
            customerRepository.deleteById(id);
        }else{
            throw new IllegalArgumentException("Customer with ID " + id + " does not exist.");
        }
    }
}
