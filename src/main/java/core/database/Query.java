package core.database;

import java.util.HashMap;
import java.util.Map;

public class Query {

    private String query;
    private HashMap<Integer,String> parameters = new HashMap<>();

    public Query(String query){
        this.query = query;
    }

    public void setParameter(int position, String value){
        parameters.put(position, value);
    }

    public String getQuery(){
        return query;
    }

    public Map<Integer, String> getParameters() {
        return parameters;
    }


}
