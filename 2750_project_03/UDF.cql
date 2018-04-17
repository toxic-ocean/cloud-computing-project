USE project_03;

CREATE OR REPLACE FUNCTION state_group_and_count( state map<text, int>, type text )
CALLED ON NULL INPUT
RETURNS map<text, int>
LANGUAGE java AS '
    Integer count = (Integer) state.get(type);
    if (count == null)
        count = 1;
    else
        count++; 
    state.put(type, count);
    return state; ' ;

CREATE OR REPLACE AGGREGATE group_and_count(text) 
SFUNC state_group_and_count 
STYPE map<text, int> 
INITCOND {};

CREATE OR REPLACE FUNCTION ccMapMax(input map<text, int>)
RETURNS NULL ON NULL INPUT
RETURNS map<text, int>
LANGUAGE java AS '
    Integer max = Integer.MIN_VALUE;
    String data="";
    for (String k : input.keySet()) {
        Integer tmp = input.get(k);
        if (tmp > max) { max = tmp; data = k; }
    }
    Map<String,Integer> mm = new HashMap<String,Integer>();
    mm.put(data,max);
    return mm;
';

CREATE OR REPLACE AGGREGATE group_and_count_q34(text)
    SFUNC state_group_and_count
    STYPE map<text, int>
    FINALFUNC ccMapMax
    INITCOND {};

