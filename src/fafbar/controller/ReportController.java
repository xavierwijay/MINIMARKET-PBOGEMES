package fafbar.controller;

import fafbar.model.User;
import fafbar.repository.ReportRepository;
import fafbar.repository.UserRepository;
import java.util.Date;
import java.util.List;

public class ReportController {
    
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    
    public ReportController() {
        this.reportRepository = new ReportRepository();
        this.userRepository = new UserRepository(); 
    }
    
    public List<User> getAllUsers() {
        // Asumsi UserRepository.getAllUsers() mengembalikan List<User>
        return userRepository.getAllUsers(); 
    }

    public List<Object[]> getSalesDataForReport(Date dateFrom, Date dateTo, int userId) {
        // Memanggil method repository dengan penanganan tanggal yang kuat
        return reportRepository.getFilteredSalesData(dateFrom, dateTo, userId);
    }
}