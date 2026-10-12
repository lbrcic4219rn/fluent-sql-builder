package querybuilder.validator.rule;

import querybuilder.parser.FunctionType;

public class WhereInParameterListRule extends PairedFunctionsRule {

    public WhereInParameterListRule() {
        super(FunctionType.WHERE_IN, FunctionType.PARAMETER_LIST);
    }
}
