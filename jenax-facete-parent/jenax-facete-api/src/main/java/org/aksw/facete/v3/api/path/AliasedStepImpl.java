package org.aksw.facete.v3.api.path;

public class AliasedStepImpl
    implements AliasedStep
{
    protected String alias;
    protected StepSpec stepSpec;

    public AliasedStepImpl(String alias, StepSpec stepSpec) {
        super();
        this.alias = alias;
        this.stepSpec = stepSpec;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public StepSpec getSpec() {
        return stepSpec;
    }
}
