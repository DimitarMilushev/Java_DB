import entities.Address;
import entities.Employee;
import exceptions.RecordNotFound;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {

        DbManager manager = new DbManager("soft_uni@localhost");

        //manager.ChangeCasingByLength(5);
        //manager.ContainsEmployee("Svetlin Nakov");
        //manager.EmployeesWithSalaryOver(BigDecimal.valueOf(50_000)).forEach(System.out::println);
//        manager.EmployeesFromDepartment("Research and Development")
//                .forEach(e -> System.out.println(String.format("%s %s from %s - $%.2f",
//                        e.getFirstName(),
//                        e.getLastName(),
//                        e.getDepartment().getName(),
//                        e.getSalary())));

//        try {
//            Address newAddress = manager.getAddress("Vitoshka 15");
//            manager.setAddressToEmployee(newAddress, "Nakov");
//        } catch (RecordNotFound recordNotFound) {
//            System.out.println(recordNotFound.getMessage());
//            recordNotFound.printStackTrace();
//        }

//        manager.getAddressesWithEmployeeCount(5).forEach(System.out::println);
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
//        manager.getLatestNProjects(10).forEach(x -> System.out.println(String.format("Project name: %s \n" +
//                "     Project Description: %s \n" +
//                "     Project Start Date: %tF \n" +
//                "     Project End Date: %tF \n",
//                x.getName(),
//                x.getDescription(),
//                x.getStartDate(),
//                x.getEndDate())));

        manager.increaseSalaries(new String[] {"Engineering", "Tool Design", "Marketing", "Information Services"})
                .forEach(x -> System.out.println(String.format("%s %s ($%.2f)",
                        x.getFirstName(),
                        x.getLastName(),
                        x.getSalary())));

        //System.out.println(manager.EmployeesWithSalaryOver(BigDecimal.valueOf(50_000)));
    }
}
