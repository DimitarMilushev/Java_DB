package bg.softuni.factory;

import bg.softuni.dto.Minion;
import bg.softuni.dto.Person;
import bg.softuni.dto.Villain;
import bg.softuni.enums.EvilnessFactor;

import java.util.Locale;

public class PersonFactory {

    public Person createPerson(String type, String... tokens) {
        type = type.toLowerCase(Locale.ROOT);

        if(type.equals("minion")) {
            return new Minion(Integer.parseInt(tokens[0]),
                    tokens[1],
                    Integer.parseInt(tokens[2]),
                    Integer.parseInt(tokens[3]));
        }

        if(type.equals("villain")) {
            return new Villain(Integer.parseInt(tokens[0]),
                    tokens[1],
                    EvilnessFactor.valueOf(tokens[2].toUpperCase()));
        }

        return null;
    }
}
