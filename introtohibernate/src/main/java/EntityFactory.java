import entities.Address;

public class EntityFactory {

    public Address getAddress(String name) {
        Address address = new Address();
        address.setText(name);

        return address;
    }
}
