package clientsideEncryption.core.database;

import java.util.HashMap;

public class Tuple {
    private final HashMap<String, String> tuple = new HashMap<>();

    public Tuple(){}

    public void setColumn(String columnName, String value){
        tuple.put(columnName,value);
    }

    public HashMap<String, String> getTuple(){
        return tuple;
    }

    @Override
    public String toString(){
        return tuple.toString();
    }
}
