package link.halen.dexcomwatch.pojos;

import java.util.NoSuchElementException;

public enum Trend {
    DOUBLEUP("doubleup", -3),//1
    SINGLEUP("singleup",-2),//2
    FORTYFIVEUP("fortyfiveup", -1),//3
    FLAT("flat", 0),//4
    FORTYFIVEDOWN("fortyfivedown",1),//5
    SINGLEDOWN("singledown", 2),//6
    DOUBLEDOWN("doubledown", 3),//7
    NOTCOMPUTABLE("notcomputable",11),//8
    RATEOUTOFRANGE("rateoutofrange",12);//9

    private final String name;
    private final int value;

    private Trend(String name, int value){
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public static Trend valueOfName(String name) {
        for (Trend e : values()) {
            if (e.name.equals(name)) {
                return e;
            }
        }
        throw new NoSuchElementException("No element of type: " + name);
    }

    public static Trend valueOfvalue(int value) {
        for (Trend e : values()) {
            if (e.value == value) {
                return e;
            }
        }
        throw new NoSuchElementException("No element with value: " + value);
    }
}
