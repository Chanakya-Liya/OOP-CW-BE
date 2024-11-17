package service;

import entity.Customer;
import entity.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.VendorRepository;

@Service
public class VendorService {
    @Autowired
    private VendorRepository vendorRepository;

    @Transactional
    public Vendor addVendor(Vendor vendor) {
        return vendorRepository.save(vendor);
    }
}
