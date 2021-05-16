package ClientsideEncryption.utils;

import java.util.HashMap;
import java.util.Map;

public class Query {

    private String query;
    private HashMap<Integer,String> parameters = new HashMap<Integer,String>();

    public Query(String query){
        this.query = query;
    }

    public void setParameter(int position, String value){
        System.out.println(position);
        System.out.println(value);
        parameters.put(position, value);
    }

    public String getQuery(){
        return query;
    }

    public Map<Integer, String> getParameters() {
        return parameters;
    }
}
