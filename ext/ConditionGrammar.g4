// Define the grammar for conditions on flows
grammar ConditionGrammar;
@header {
    package nl.tue.bpmn.grammars;
}
condition			: or_term ('OR' or_term)*;
or_term				: and_term ('AND' and_term)*;
and_term			: basic_condition | 'NOT' condition | '(' condition ')' ;
basic_condition		: nominal_condition | numeric_condition | transition_condition ;
nominal_condition	: LABEL 'IN' '{' LABEL (',' LABEL)* '}' ;
numeric_condition	: LABEL COMPARATOR NUMBER ;
transition_condition: '[' LABEL ']' ;
LABEL				: [_a-zA-Z]+ | '\'' [_ a-zA-Z0-9]+ '\'';
COMPARATOR			: '>' | '<' | '=' | '>=' | '<=' ;
NUMBER				: [0-9]+ ;

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines
