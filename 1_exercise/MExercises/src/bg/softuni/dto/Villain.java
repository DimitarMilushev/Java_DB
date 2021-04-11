package bg.softuni.dto;

import bg.softuni.enums.EvilnessFactor;

public class Villain extends Person{
    private EvilnessFactor evilnessFactor;

    public Villain(int id, String name, EvilnessFactor evilnessFactor) {
        super(id, name);
        this.evilnessFactor = evilnessFactor;
    }

    public EvilnessFactor getEvilnessFactor() {
        return evilnessFactor;
    }
}
