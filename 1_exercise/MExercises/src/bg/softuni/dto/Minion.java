package bg.softuni.dto;

public class Minion extends Person{
    private int age;
    private int townId;

    public Minion(int id, String name, int age, int townId) {
        super(id, name);
        this.age = age;
        this.townId = townId;
    }


    public void setAge(int age) {
        this.age = age;
    }

    public void setTownId(int townId) {
        this.townId = townId;
    }

    public int getAge() {
        return age;
    }

    public int getTownId() {
        return townId;
    }
}
