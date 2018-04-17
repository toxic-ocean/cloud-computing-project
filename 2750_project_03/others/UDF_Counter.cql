CREATE OR REPLACE FUNCTION new_group_and_total( state map<text, int>, type text, amount counter )
CALLED ON NULL INPUT
RETURNS map<text, int>
LANGUAGE java AS '
Integer count = (Integer) state.get(type); 
if (count == null) 
    count = amount.intValue(); 
else 
    count = count + amount.intValue(); 
state.put(type, count); return state; ' ;

CREATE OR REPLACE AGGREGATE group_and_total(text, counter)
SFUNC new_group_and_total
STYPE map<text, int>
INITCOND {};
