package querybuilder.validator.rule;

import querybuilder.parser.FunctionType;

public class JoinOnRule extends PairedFunctionsRule {

    public JoinOnRule() {
        super(FunctionType.JOIN, FunctionType.ON);
    }
}
