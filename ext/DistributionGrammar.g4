// Define the grammar for conditions on flows
grammar DistributionGrammar;
@header {
    package nl.tue.bpmn.grammars;
}
distribution		: '[' single_term (',' single_term)* ']';
single_term			: '{' TERM ',' distribution_term '}';
TERM				: [a-zA-Z]+;
distribution_term	: 'exp' '(' NUMBER ')' | 'N' '(' NUMBER ',' NUMBER ')' | '[' value_series']';
value_series		: value (',' value)*;
value				: '{' TERM ',' NUMBER '%' '}';   
NUMBER				: [0-9]+ ;

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines
