package org.aksw.jena_sparql_api.pathlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.facete.v3.api.path.StepImpl;

public class Path
    extends PathBuilder
{
    protected Path parent;
    protected StepImpl step;

    public Path() {
        this(null, null);
    }

    public Path(Path parent, StepImpl step) {
        super();
        this.parent = parent;
        this.step = step;
    }

    public Path getParent() {
        return parent;
    }

    public StepImpl getStep() {
        return step;
    }

    @Override
    public Path appendStep(StepImpl step) {
        return new Path(this, step);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        result = prime * result + ((step == null) ? 0 : step.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Path other = (Path) obj;
        if (parent == null) {
            if (other.parent != null)
                return false;
        } else if (!parent.equals(other.parent))
            return false;
        if (step == null) {
            if (other.step != null)
                return false;
        } else if (!step.equals(other.step))
            return false;
        return true;
    }

    public static Path newPath() {
        return new Path();
    }


    public static List<StepImpl> getSteps(Path path) {
        List<StepImpl> steps = new ArrayList<>();
        Path c = path;
        do {
            StepImpl step = c.getStep();
            if(step != null) {
                steps.add(step);
            }
            c = c.getParent();
        } while(c != null);

        Collections.reverse(steps);
        return steps;
    }

    @Override
    public String toString() {
        return "" + getSteps(this);
    }
}
