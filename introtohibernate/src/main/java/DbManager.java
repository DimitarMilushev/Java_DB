import entities.*;
import exceptions.RecordNotFound;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collector;

public class DbManager {
    private final EntityManagerFactory entityManagerFactory;
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

    public DbManager(String url) {
        this.entityManagerFactory = Persistence.createEntityManagerFactory(url);
        this.entityManager = entityManagerFactory.createEntityManager();
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    public void ChangeCasingByLength(int length) {
        entityManager.getTransaction().begin();
        CriteriaQuery<Town> townQuery =
                criteriaBuilder.createQuery(Town.class);
        Root<Town> town = townQuery.from(Town.class);

        townQuery.select(town);
        List<Town> towns = entityManager.createQuery(townQuery).getResultList();
        towns.forEach(entityManager::persist);

        entityManager.flush();
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();

        for(Town t : towns) {
            if(t.getName().length() > length) {
                entityManager.detach(t);
            }
            else {
                t.setName(t.getName().toLowerCase(Locale.ROOT));
            }
        }

        entityManager.flush();
        entityManager.getTransaction().commit();
    }

    public String ContainsEmployee(String name) {
        entityManager.getTransaction().begin();

        boolean result = entityManager.createQuery("SELECT e FROM Employee AS e" +
                        " WHERE CONCAT(e.firstName, ' ', e.lastName) LIKE :name")
                        .setParameter("name", name)
                        .getResultList()
                        .size() > 0;

        entityManager.getTransaction().commit();

        return result ? "Yes" : "No";
    }

    public Collection<String> EmployeesWithSalaryOver(BigDecimal salary) {

        List<String> employees;
        entityManager.getTransaction().begin();
        employees = entityManager.createQuery("SELECT e.firstName FROM Employee AS e WHERE e.salary > :salary")
                .setParameter("salary", salary).getResultList();

        entityManager.getTransaction().commit();
        return employees;
    }

    public Collection<Employee> EmployeesFromDepartment(String deptName) {
        entityManager.getTransaction().begin();
        CriteriaQuery<Department> departmentCriteriaQuery = criteriaBuilder.createQuery(Department.class);
        Metamodel metamodel = entityManager.getMetamodel();
        EntityType<Department> Department_ = metamodel.entity(Department.class);
        Root<Department> department = departmentCriteriaQuery.from(Department.class);

        departmentCriteriaQuery.where(department.get("name").in(deptName));

        TypedQuery<Department> q = entityManager.createQuery(departmentCriteriaQuery);
        Department selectedDep = q.getSingleResult();

        Set<Employee> employeeSet = selectedDep.getEmployees();
        entityManager.getTransaction().commit();
        return employeeSet;
    }

    public void addAddress(String name) {
        EntityFactory factory = new EntityFactory();
        Address newAddress = factory.getAddress(name);

        this.entityManager.getTransaction().begin();
        entityManager.persist(newAddress);
        entityManager.flush();
        entityManager.getTransaction().commit();
    }

    public Address getAddress(String name) throws RecordNotFound {
        this.entityManager.getTransaction().begin();
        CriteriaQuery<Address> addressCriteriaQuery = this.criteriaBuilder.createQuery(Address.class);
        Metamodel metamodel = entityManager.getMetamodel();
        EntityType<Address> Address_ = metamodel.entity(Address.class);
        Root<Address> addressRoot = addressCriteriaQuery.from(Address.class);

        addressCriteriaQuery.where(addressRoot.get("text").in(name));

        TypedQuery<Address> q = this.entityManager.createQuery(addressCriteriaQuery);
       Address address = q.getSingleResult();
       if(address == null) {
           throw new RecordNotFound(String.format("No address with name %s found!", name));
       }

       this.entityManager.getTransaction().commit();
       return address;
    }

    public void setAddressToEmployee(Address address, String lastName) throws RecordNotFound {
        entityManager.getTransaction().begin();
        CriteriaQuery<Employee> employeeCriteriaQuery
                = criteriaBuilder.createQuery(Employee.class);
        Root<Employee> employee = employeeCriteriaQuery.from(Employee.class);

        employeeCriteriaQuery.where(employee.get("lastName").in(lastName));

        TypedQuery<Employee> q = entityManager.createQuery(employeeCriteriaQuery);
        Employee selectedEmployee = q.getSingleResult();
        if(selectedEmployee == null) {
            throw new RecordNotFound(String.format("Person with last name %s not found!", lastName));
        }

        selectedEmployee.setAddress(address);
        entityManager.persist(selectedEmployee);
        entityManager.getTransaction().commit();
    }

    public Collection<Object> getAddressesWithEmployeeCount(int limit) {
        this.entityManager.getTransaction().begin();
        String query = """
        SELECT a.text, t.name, COUNT(e.id) AS countE
        FROM Address AS a
        INNER JOIN Town AS t
        INNER JOIN Employee  AS e
        GROUP BY a
        ORDER BY countE
        """;
        List<Object> result = this.entityManager.createQuery(query).getResultList();
        this.entityManager.getTransaction().commit();
        return result;
    }

    public Collection<Project> getLatestNProjects(int n) {
        this.entityManager.getTransaction().begin();
        CriteriaQuery<Project> criteriaQuery = this.criteriaBuilder.createQuery(Project.class);
        Root<Project> projectRoot = criteriaQuery.from(Project.class);

        criteriaQuery.select(projectRoot)
                .orderBy(this.criteriaBuilder.desc(projectRoot.get("startDate")), this.criteriaBuilder.asc(projectRoot.get("name")));

        Collection<Project> projects = this.entityManager.createQuery(criteriaQuery).setMaxResults(n).getResultList();
        this.entityManager.getTransaction().commit();
        return projects;
    }

    public Collection<Employee> increaseSalaries(String[] tags) {
        this.entityManager.getTransaction().begin();
        CriteriaQuery<Employee> cq = this.criteriaBuilder.createQuery(Employee.class);
        Root<Employee> employee = cq.from(Employee.class);
        Metamodel m = this.entityManager.getMetamodel();
        EntityType<Employee> Employee_ = m.entity(Employee.class);
//        EntityType<Department> Department_ = m.entity(Department.class);
        Join<Employee, Department> department =
                employee.join(Employee_.getSingularAttribute("department", Department.class));


        cq.select(employee)
                .where(department.get("name").in(tags));

        List<Employee> employees = this.entityManager.createQuery(cq).getResultList();
        employees.forEach(x -> x.setSalary(x.getSalary().add(x.getSalary().multiply(BigDecimal.valueOf(0.12)))));

        employees.forEach(this.entityManager::persist);
        this.entityManager.flush();
        this.entityManager.getTransaction().commit();

        return employees;
    }
}
