package org.aksw.jenax.sparql.fragment.api;

public abstract class EntityFragmentBase
    implements EntityFragment
{
    protected Fragment fragment;

    public EntityFragmentBase(Fragment fragment) {
        super();
        this.fragment = fragment;
    }

    @Override
    public Fragment getFragment() {
        return fragment;
    }
}
