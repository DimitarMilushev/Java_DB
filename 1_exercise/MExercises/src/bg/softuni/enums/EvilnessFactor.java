package bg.softuni.enums;

public enum EvilnessFactor {
    GOOD,
    BAD,
    EVIL,
    SUPER_EVIL {
        public String toString() {
            return "super evil";
        }
    }
}
