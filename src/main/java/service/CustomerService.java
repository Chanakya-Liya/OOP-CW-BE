package service;

import entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.CustomerRepository;

import java.util.List;

@Service
public class CustomerService {
    @Autowired
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

    public void addCustomers(List<Customer> customers) {
        customerRepository.saveAll(customers);
    }

    public void deleteCustomerById(long id) {
        if(customerRepository.existsById(id)){
            customerRepository.deleteById(id);
        }else{
            throw new IllegalArgumentException("Customer with ID " + id + " does not exist.");
        }
    }
}
